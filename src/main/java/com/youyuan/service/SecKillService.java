package com.youyuan.service;

/**
 * 类名称：SecKillService <br>
 * 类描述： 秒杀业务接口 <br>
 *
 * @author zhangyu
 * @version 1.0.0
 * @date 创建时间：2021/8/17 21:41<br>
 */
public interface SecKillService {

    /**
     * 方法名: secKill <br>
     * 方法描述: 秒杀具体业务 <br>
     *
     * @param userId    用户
     * @param productId 产品
     * @return {@link String 返回秒杀结果 }
     * @date 创建时间: 2021/8/17 21:42 <br>
     * @author zhangyu
     */
    String secKill(String userId, String productId);
}
