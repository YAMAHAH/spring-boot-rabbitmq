package com.wlf.demo.bussiness;

import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.wlf.demo.AmqpConfig;
import com.wlf.demo.annotation.MessageCache;
import com.wlf.demo.util.CacheCorrelationData;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; 

@Service("orderService")
public class OrderService {
	
	private String orderSaveKey=AmqpConfig.ROUNTING_KEY_PREFIX+"."+AmqpConfig.ORDER_SAVE_ROUTING_KEY;
	
	@Value("${spring.rabbitmq.exchange}")
	private String orderExchange;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	//@Autowired  
    //public OrderService(RabbitTemplate rabbitTemplate) {  
        //this.rabbitTemplate = rabbitTemplate;  
        //rabbitTemplate.setConfirmCallback(this); 
    //}  
	
	//@MessageCache为自定义注释，用来设置缓存和原交换机、路由键等相关信息
	@MessageCache(cacheName="order",cacheKey="${arg.correlationId}",messageArgMapper="order",exchange="${field.testExchange}",routingKey="${field.orderSaveKey}")
	public void sendOrderMessage(String correlationId,Object order) throws Exception{
		//扩展CorrelationData，使其包含缓存信息，方便确认机制返回失败后重发
		CacheCorrelationData correlation = new CacheCorrelationData(correlationId,"order");
		Message msg=new Message(JSONObject.toJSONString(order).getBytes(),MessagePropertiesBuilder.newInstance().setContentType("text/x-json").build());//.setExpiration("10000").build());
		rabbitTemplate.send(orderExchange, orderSaveKey, msg, correlation);
	}
	
}
