package com.young.mall.rabbitmq;


import com.young.mall.entity.MallOrder;
import com.young.mall.entity.MallUser;
import com.young.mall.redis.RedisService;
import com.young.mall.service.GoodsService;
import com.young.mall.service.MallService;
import com.young.mall.service.OrderService;
import com.young.mall.vo.GoodsVo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MQReceiver {

		private static Logger log = LoggerFactory.getLogger(MQReceiver.class);
		
		@Autowired
		RedisService redisService;
		
		@Autowired
		GoodsService goodsService;
		
		@Autowired
		OrderService orderService;
		
		@Autowired
		MallService mallService;
		
		@RabbitListener(queues=MQConfig.Mall_QUEUE)
		public void receive(String message) {
			log.info("receive message:"+message);
			MallMessage mm  = RedisService.stringToBean(message, MallMessage.class);
			MallUser user = mm.getUser();
			long goodsId = mm.getGoodsId();
			
			GoodsVo goods = goodsService.getGoodsVoByGoodsId(goodsId);
	    	int stock = goods.getStockCount();
	    	if(stock <= 0) {
	    		return;
	    	}
	    	//判断是否已经秒杀到了
	    	MallOrder order = orderService.getMallOrderByUserIdGoodsId(user.getId(), goodsId);
	    	if(order != null) {
	    		return;
	    	}
	    	//减库存 下订单 写入秒杀订单
	    	mallService.mall(user, goods);
		}

		@RabbitListener(queues=MQConfig.QUEUE)
		public void receive2(String message) {
			log.info("receive message:"+message);
		}
//		
//		@RabbitListener(queues=MQConfig.TOPIC_QUEUE1)
//		public void receiveTopic1(String message) {
//			log.info(" topic  queue1 message:"+message);
//		}
//		
//		@RabbitListener(queues=MQConfig.TOPIC_QUEUE2)
//		public void receiveTopic2(String message) {
//			log.info(" topic  queue2 message:"+message);
//		}
//		
//		@RabbitListener(queues=MQConfig.HEADER_QUEUE)
//		public void receiveHeaderQueue(byte[] message) {
//			log.info(" header  queue message:"+new String(message));
//		}
//		
		
}
