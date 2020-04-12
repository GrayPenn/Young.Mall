package com.young.mall.service;

import com.young.mall.dao.OrderDao;
import com.young.mall.entity.MallOrder;
import com.young.mall.entity.MallUser;
import com.young.mall.entity.OrderInfo;
import com.young.mall.redis.OrderKey;
import com.young.mall.redis.RedisService;
import com.young.mall.vo.GoodsVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;

@Service
public class OrderService {

    @Autowired
    OrderDao orderDao;

    @Autowired
    RedisService redisService;

    public MallOrder getMallOrderByUserIdGoodsId(long userId, long goodsId) {
        return orderDao.getMallOrderByUserIdGoodsId(userId, goodsId);
        //TODO 这里修改了
//        return redisService.get(OrderKey.getMallOrderByUidGid, "" + userId + "_" + goodsId, MallOrder.class);
    }

    public OrderInfo getOrderById(long orderId) {
        return orderDao.getOrderById(orderId);
    }


    @Transactional
    public OrderInfo createOrder(MallUser user, GoodsVo goods) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setDeliveryAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getMallPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId(user.getId());
        orderDao.insert(orderInfo);
        MallOrder mallOrder = new MallOrder();
        mallOrder.setGoodsId(goods.getId());
        mallOrder.setOrderId(orderInfo.getId());
        mallOrder.setUserId(user.getId());
        orderDao.insertMallOrder(mallOrder);

        redisService.set(OrderKey.getMallOrderByUidGid, "" + user.getId() + "_" + goods.getId(), mallOrder);

        return orderInfo;
    }

    public List<MallOrder> getAllMallOrdersByGoodsId(long goodsId) {
        return orderDao.listByGoodsId(goodsId);
    }

    public void deleteOrders() {
        orderDao.deleteOrders();
        orderDao.deleteMallOrders();
    }

}
