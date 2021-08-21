package com.youyuan.controller;

import com.youyuan.service.SecKillService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Random;

/**
 * 类名称：SecKillController <br>
 * 类描述： 秒杀处理类 <br>
 *
 * @author zhangyu
 * @version 1.0.0
 * @date 创建时间：2021/8/17 22:11<br>
 */
@RestController
@RequestMapping("/seckill")
public class SecKillController {

    @Autowired
    private SecKillService secKillService;

    /**
     * 方法名: order <br>
     * 方法描述: 购买 <br>
     *
     * @param productId 产品
     * @return {@link String 返回秒杀结果 }
     * @date 创建时间: 2021/8/17 22:12 <br>
     * @author zhangyu
     */
    @GetMapping("/order")
    public String order(String productId) {
        StringBuilder stringBuilder = new StringBuilder();
        Random random = new Random();
        for (int i = 0; i < 6; i++) {
            stringBuilder.append(random.nextInt(10));
        }
        String userId = stringBuilder.toString();
        return secKillService.secKill(userId, productId);
    }
}
