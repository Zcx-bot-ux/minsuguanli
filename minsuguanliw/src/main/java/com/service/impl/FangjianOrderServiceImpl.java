package com.service.impl;

import com.utils.StringUtil;
import org.springframework.stereotype.Service;
import java.lang.reflect.Field;
import java.util.*;
import java.text.SimpleDateFormat;
import com.baomidou.mybatisplus.plugins.Page;
import com.baomidou.mybatisplus.service.impl.ServiceImpl;
import org.springframework.transaction.annotation.Transactional;
import com.baomidou.mybatisplus.mapper.EntityWrapper;
import com.baomidou.mybatisplus.mapper.Wrapper;
import com.utils.PageUtils;
import com.utils.Query;
import org.springframework.web.context.ContextLoader;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import com.dao.FangjianOrderDao;
import com.entity.FangjianOrderEntity;
import com.service.FangjianOrderService;
import com.entity.view.FangjianOrderView;
import com.entity.FangjianEntity;
import com.entity.YonghuEntity;
import com.service.FangjianService;
import com.service.YonghuService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 房间预约 服务实现类
 */
@Service("fangjianOrderService")
@Transactional
public class FangjianOrderServiceImpl extends ServiceImpl<FangjianOrderDao, FangjianOrderEntity> implements FangjianOrderService {
	
	@Autowired
	private FangjianService fangjianService;
	
	@Autowired
	private YonghuService yonghuService;

    @Override
    public PageUtils queryPage(Map<String,Object> params) {
        if(params != null && (params.get("limit") == null || params.get("page") == null)){
            params.put("page","1");
            params.put("limit","10");
        }
        Page<FangjianOrderView> page = new Query<FangjianOrderView>(params).getPage();
        page.setRecords(baseMapper.selectListView(page,params));
        return new PageUtils(page);
    }

    @Override
    public int autoRefundExpiredOrders() {
        // 查询所有已支付且有过期时间的订单（只处理无预约时间的订单）
        Wrapper<FangjianOrderEntity> wrapper = new EntityWrapper<FangjianOrderEntity>()
            .eq("fangjian_order_types", 1) // 已支付状态
            .isNotNull("expire_time")
            .isNull("fangjian_order_time")  // 只处理无预约时间的订单
            .lt("expire_time", new Date()); // 过期时间小于当前时间
        
        List<FangjianOrderEntity> expiredOrders = this.selectList(wrapper);
        
        int refundCount = 0;
        for(FangjianOrderEntity order : expiredOrders){
            try {
                // 执行退款逻辑
                FangjianEntity fangjianEntity = fangjianService.selectById(order.getFangjianId());
                YonghuEntity yonghuEntity = yonghuService.selectById(order.getYonghuId());
                
                if(fangjianEntity != null && yonghuEntity != null){
                    Double money = fangjianEntity.getFangjianMoney();
                    yonghuEntity.setNewMoney(yonghuEntity.getNewMoney() + money);
                    yonghuService.updateById(yonghuEntity);
                    
                    order.setFangjianOrderTypes(2); // 设置为退款状态
                    this.updateById(order);
                    
                    // 自动退款，恢复房间数量
                    fangjianEntity.setFangjianNumber(fangjianEntity.getFangjianNumber() + 1);
                    fangjianService.updateById(fangjianEntity);
                    
                    refundCount++;
                }
            } catch (Exception e) {
                // 记录日志但不抛出异常，继续处理下一个
            }
        }
        
        return refundCount;
    }

    @Override
    public int autoUpdateOrderStatus() {
        Date now = new Date();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        int updateCount = 0;
        
        try {
            Date nowDate = sdf.parse(sdf.format(now));
            
            // 1. 将已支付且预约时间为今天的订单更新为进行中
            Wrapper<FangjianOrderEntity> wrapper1 = new EntityWrapper<FangjianOrderEntity>()
                .eq("fangjian_order_types", 1)
                .isNotNull("fangjian_order_time");
            
            List<FangjianOrderEntity> list1 = this.selectList(wrapper1);
            for(FangjianOrderEntity order : list1){
                Date orderDate = sdf.parse(sdf.format(order.getFangjianOrderTime()));
                if(nowDate.compareTo(orderDate) == 0){
                    order.setFangjianOrderTypes(5); // 进行中
                    this.updateById(order);
                    updateCount++;
                }
            }
            
            // 2. 将进行中的订单，如果已过退房时间（expire_time），更新为已完成
            Wrapper<FangjianOrderEntity> wrapper2 = new EntityWrapper<FangjianOrderEntity>()
                .eq("fangjian_order_types", 5)
                .isNotNull("fangjian_order_time");  // 只处理有预约时间的订单
            
            List<FangjianOrderEntity> list2 = this.selectList(wrapper2);
            for(FangjianOrderEntity order : list2){
                // 判断是否已过退房时间
                if(order.getExpireTime() != null && now.after(order.getExpireTime())){
                    order.setFangjianOrderTypes(3); // 已完成
                    this.updateById(order);
                    
                    // 恢复房间数量
                    FangjianEntity fangjianEntity = fangjianService.selectById(order.getFangjianId());
                    if(fangjianEntity != null){
                        fangjianEntity.setFangjianNumber(fangjianEntity.getFangjianNumber() + 1);
                        fangjianService.updateById(fangjianEntity);
                    }
                    
                    updateCount++;
                }
            }
            
        } catch (Exception e) {
            // 记录日志
        }
        
        return updateCount;
    }

}
