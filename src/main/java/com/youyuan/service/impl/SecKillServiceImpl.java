package com.youyuan.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.youyuan.constans.SecKillConstans;
import com.youyuan.service.SecKillService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.lang.Nullable;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 类名称：SecKillServiceImpl <br>
 * 类描述： 秒杀业务接口实现 <br>
 *
 * @author zhangyu
 * @version 1.0.0
 * @date 创建时间：2021/8/17 21:44<br>
 */
@Service
@Slf4j
public class SecKillServiceImpl implements SecKillService {

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public String secKill(String userId, String productId) {

        //不能解决超卖
//        String result = overSale(userId, productId);

        //通过乐观锁解决超卖问题(但是会带来库存遗留问题,就是秒杀的库存数量没有卖完)
//        String result = noOverSale(userId, productId);

        //通过lua脚本解决超卖和库存遗留问题
        String result = processorInveLegacy(userId, productId);

        return result;
    }

    /**
     * 方法名: processorInveLegacy <br>
     * 方法描述: 通过lua脚本解决超卖和库存遗留问题 <br>
     *
     * @param userId    用户
     * @param productId 产品
     * @return {@link String 返回执行结果 }
     * @date 创建时间: 2021/8/19 12:05 <br>
     * @author zhangyu
     */
    private String processorInveLegacy(String userId, String productId) {

        // 执行 lua 脚本
        DefaultRedisScript<Long> redisScript = new DefaultRedisScript<>();
        // 指定 lua 脚本
        redisScript.setScriptSource(new ResourceScriptSource(new ClassPathResource("redis/seckill.lua")));
        // 指定返回类型
        redisScript.setResultType(Long.class);
        // 参数一：redisScript，参数二：key列表，参数三：arg（可多个）
        Long result = (Long) redisTemplate.execute(redisScript, Arrays.asList
                (buildInvelNumKey(productId), buildOrderUser(productId)), userId, productId);
        System.out.println(result);
        if (result == 0) {
            return "已抢空！！";
        } else if (result == 1) {
            return "抢购成功！！！！";
        } else if (result == 3) {
            return "该用户已抢过！！";
        } else {
            return "抢购异常！！";
        }
    }

    /**
     * 方法名: noverSale <br>
     * 方法描述: 乐观锁解决超卖 <br>
     *
     * @param userId    用户
     * @param productId 产品
     * @return {@link String 返回秒杀结果信息 }
     * @date 创建时间: 2021/8/18 0:48 <br>
     * @author zhangyu
     */
    private String noOverSale(String userId, String productId) {
        Jedis jedis = new Jedis("192.168.1.22", 6379);
        //1. 判断参数是否为空
        if (StrUtil.isBlank(userId) || StrUtil.isBlank(productId)) {
            return "请求参数为空";
        }
        //2. 监听库存key
        jedis.watch(buildInvelNumKey(productId));
        //3. 判断库存是否为空
        String num = jedis.get(buildInvelNumKey(productId));
        if (StrUtil.isBlank(num)) {
            return "抱歉,秒杀还没有开始";
        }
        //4. 判断用户是否已经购买过了
        Boolean sismember = jedis.sismember(buildOrderUser(productId), userId);
        if (sismember) {
            return "抱歉,您已经购买过了";
        }
        //5. 判断是否还有库存
        if (Integer.parseInt(num) < 1) {
            return "抱歉,秒杀已结束";
        }
        //6. 开启事务
        Transaction multi = jedis.multi();
        //7. 组队
        //7.1 扣减库存数据
        multi.decr(buildInvelNumKey(productId));
        //7.2 添加购买记录
        multi.sadd(buildOrderUser(productId), userId);
        //8. 提交事务数据
        List<Object> exec = multi.exec();
        if (CollUtil.isEmpty(exec)) {
            return "超卖业务问题";
        }
        return "用户" + userId + "购买成功";
    }

    /**
     * 方法名: overSale <br>
     * 方法描述: 不能解决超卖问题 <br>
     *
     * @param userId    用户
     * @param productId 产品
     * @return {@link String 返回购买结果 }
     * @date 创建时间: 2021/8/18 0:36 <br>
     * @author zhangyu
     */
    private String overSale(String userId, String productId) {
        //1. 判断参数是否为空
        if (StrUtil.isBlank(userId) || StrUtil.isBlank(productId)) {
            return "请求参数为空";
        }
        //2. 判断库存是否为空
        Integer num = (Integer) redisTemplate.opsForValue().get(buildInvelNumKey(productId));
        if (ObjectUtil.isEmpty(num)) {
            return "抱歉,秒杀还没有开始";
        }
        //3. 判断用户是否已经购买过了
        Boolean isMember = redisTemplate.opsForSet().isMember(buildOrderUser(productId), userId);
        if (isMember) {
            return "抱歉,您已经购买过了";
        }
        //4. 判断是否还有库存
        if (num < 1) {
            return "抱歉,秒杀已结束";
        }
        //5. 执行秒杀业务逻辑 减库存 保存购买记录
        //5.1减库存
        redisTemplate.opsForValue().decrement(buildInvelNumKey(productId));
        //5.2保存购买记录
        redisTemplate.opsForSet().add(buildOrderUser(productId), userId);
        return "用户" + userId + "购买成功";
    }

    /**
     * 方法名: buildInvelNumKey <br>
     * 方法描述: 生成库存key <br>
     *
     * @param productId 产品
     * @return {@link String 返回库存key}
     * @date 创建时间: 2021/8/17 22:00 <br>
     * @author zhangyu
     */
    private String buildInvelNumKey(String productId) {
        return StrUtil.format(SecKillConstans.INVEL_NUM_PREFIX, productId);
    }

    /**
     * 方法名: buildOrderUser <br>
     * 方法描述: 构建购买用户key <br>
     *
     * @param productId 产品
     * @return {@link String 返回购买用户key }
     * @date 创建时间: 2021/8/17 22:03 <br>
     * @author zhangyu
     */
    private String buildOrderUser(String productId) {
        return StrUtil.format(SecKillConstans.ORDER_USER_PREFIX, productId);
    }
}
