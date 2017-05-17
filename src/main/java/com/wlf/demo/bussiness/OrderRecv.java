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
			System.out.println("�յ���Ϣ: " + message);
			//�������ݿ⴦��
			System.out.println("�����Ѵ洢�����ݿ�: " + message);
			throw new NullPointerException();
		}catch(Exception e){
			//�൱��basic.nack����requeueΪtrue
			//�ٷ��ĵ�ԭ��When a listener throws an exception, it is wrapped in a ListenerExecutionFailedException and, normally the message is rejected and requeued by the broker
			//throw new NullPointerException();
			//�൱��basic.nack����requeueΪfalse
			//�ٷ��ĵ�ԭ��the listener can throw an AmqpRejectAndDontRequeueException to conditionally control this behavior
			//��һ�ַ�������SimpleRabbitListenerContainerFactory��defaultRequeueRejectedΪfalse���ٷ�ԭ��Setting defaultRequeueRejected to false will cause messages to be discarded (or routed to a dead letter exchange)
			throw new AmqpRejectAndDontRequeueException(e.getMessage());	
		}
    }
	
}
