package com.wlf.demo.bussiness;

import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RabbitListener(queues = "${spring.rabbitmq.queueName}")
public class OrderRecv {

	@RabbitHandler
    public void receive(String message) {
		try{
			System.out.println("收到信息: " + message);
			//订单数据库处理
			System.out.println("订单已存储到数据库: " + message);
		}catch(Exception e){
			System.out.println("发送失败信息给用户邮箱，或发送短信，推送消息");
		}
    }
	
}
