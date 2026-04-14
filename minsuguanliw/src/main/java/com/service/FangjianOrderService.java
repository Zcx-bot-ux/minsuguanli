package com.service;

import com.baomidou.mybatisplus.service.IService;
import com.utils.PageUtils;
import com.entity.FangjianOrderEntity;
import java.util.Map;
import javax.servlet.http.HttpServletRequest;

/**
 * 房间预约 服务类
 */
public interface FangjianOrderService extends IService<FangjianOrderEntity> {

    /**
    * @param params 查询参数
    * @return 带分页的查询出来的数据
    */
     PageUtils queryPage(Map<String, Object> params);
     
     /**
      * 自动退款过期订单
      * @return 退款的订单数量
      */
     int autoRefundExpiredOrders();
     
     /**
      * 自动更新订单状态（进行中、已完成）
      * @return 更新的订单数量
      */
     int autoUpdateOrderStatus();
}