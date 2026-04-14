package com.config;

import com.controller.FangjianOrderController;
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
    private FangjianOrderController fangjianOrderController;

    /**
     * 每天凌晨1点执行：自动退款过期订单
     */
    @Scheduled(cron = "0 0 1 * * ?")
    public void autoRefundExpiredOrders() {
        logger.info("开始执行定时任务：自动退款过期订单");
        fangjianOrderController.autoRefundExpiredOrders();
    }

    /**
     * 每天凌晨2点执行：自动更新订单状态
     */
    @Scheduled(cron = "0 0 2 * * ?")
    public void autoUpdateOrderStatus() {
        logger.info("开始执行定时任务：自动更新订单状态");
        fangjianOrderController.autoUpdateOrderStatus();
    }
}

