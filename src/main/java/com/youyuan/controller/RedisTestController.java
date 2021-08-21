package com.youyuan.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import redis.clients.jedis.JedisCluster;

/**
 * 类名称：RedisTestController <br>
 * 类描述： redis测试类 <br>
 *
 * @author zhangyu
 * @version 1.0.0
 * @date 创建时间：2021/8/17 12:14<br>
 */
@RestController
@RequestMapping("/redistest")
public class RedisTestController {
    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private JedisCluster jedisCluster;

    /**
     * 方法名: getName <br>
     * 方法描述: 获取用户名 <br>
     *
     * @return {@link String 返回获取用户名 }
     * @date 创建时间: 2021/8/17 12:15 <br>
     * @author zhangyu
     */
    @GetMapping("/getName")
    public String getName() {
        redisTemplate.opsForValue().set("name", "赵熏琴");
        return (String) redisTemplate.opsForValue().get("name");
    }

    /**
     * 方法名: saveAndGet <br>
     * 方法描述: 保存并且查询 <br>
     *
     * @return {@link String 返回保存且查询结果 }
     * @date 创建时间: 2021/8/21 12:39 <br>
     * @author zhangyu
     */
    @GetMapping("/saveAndGet")
    public String saveAndGet() {
        jedisCluster.set("address", "上海金融中心");
        return jedisCluster.get("address");
    }
}
