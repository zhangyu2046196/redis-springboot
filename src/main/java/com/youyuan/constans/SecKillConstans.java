package com.youyuan.constans;

/**
 * 类名称：SecKillConstans <br>
 * 类描述： 秒杀常量类 <br>
 *
 * @author zhangyu
 * @version 1.0.0
 * @date 创建时间：2021/8/17 21:56<br>
 */
public class SecKillConstans {

    /**
     * redis库存key
     */
    public static final String INVEL_NUM_PREFIX = "sk:{}:qt";

    /**
     * redis购买key
     */
    public static final String ORDER_USER_PREFIX = "sk:{}:user";

}
