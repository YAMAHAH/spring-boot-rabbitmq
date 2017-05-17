package com.wlf.demo.bussiness;

import org.springframework.amqp.AmqpRejectAndDontRequeueException;
import org.springframework.amqp.rabbit.annotation.RabbitHandler;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.rabbitmq.client.Channel;

@Component
@RabbitListener(queues = "${spring.rabbitmq.queueName}")
public class OrderRecv {

	@Autowired
	private RabbitTemplate rabbitTemplate;
	
	@RabbitHandler
    public void receive(String message, Channel channel) {
		try{
			System.out.println("收到信息: " + message);
			//订单数据库处理
			System.out.println("订单已存储到数据库: " + message);
			throw new NullPointerException();
		}catch(Exception e){
			//相当于basic.nack并且requeue为true
			//官方文档原话When a listener throws an exception, it is wrapped in a ListenerExecutionFailedException and, normally the message is rejected and requeued by the broker
			//throw new NullPointerException();
			//相当于basic.nack并且requeue为false
			//官方文档原话the listener can throw an AmqpRejectAndDontRequeueException to conditionally control this behavior
			//另一种方法设置SimpleRabbitListenerContainerFactory的defaultRequeueRejected为false，官方原话Setting defaultRequeueRejected to false will cause messages to be discarded (or routed to a dead letter exchange)
			throw new AmqpRejectAndDontRequeueException(e.getMessage());	
		}
    }
	
}
