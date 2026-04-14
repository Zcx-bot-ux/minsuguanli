package com.config;

import com.service.FangjianOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

/**
 * 定时任务配置类
 */
@Configuration
@EnableScheduling
public class ScheduledConfig {

    private static final Logger logger = LoggerFactory.getLogger(ScheduledConfig.class);

    @Autowired
    private FangjianOrderService fangjianOrderService;

    /**
     * 每天凌晨1点执行：自动退款过期订单
     */
    @Scheduled(cron = "0 30 4 * * ?")
    public void autoRefundExpiredOrders() {
        logger.info("开始执行定时任务：自动退款过期订单");
        int count = fangjianOrderService.autoRefundExpiredOrders();
        logger.info("自动退款完成，共退款{}个订单", count);
    }

    /**
     * 每天早上7点执行：自动更新订单状态
     */
    @Scheduled(cron = "0 0 5 * * ?")
    public void autoUpdateOrderStatus() {
        logger.info("开始执行定时任务：自动更新订单状态");
        int count = fangjianOrderService.autoUpdateOrderStatus();
        logger.info("自动更新订单状态完成，共更新{}个订单", count);
    }
}

