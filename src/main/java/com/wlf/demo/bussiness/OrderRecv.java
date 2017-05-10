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
			System.out.println("�յ���Ϣ: " + message);
			//�������ݿ⴦��
			System.out.println("�����Ѵ洢�����ݿ�: " + message);
		}catch(Exception e){
			System.out.println("����ʧ����Ϣ���û����䣬���Ͷ��ţ�������Ϣ");
		}
    }
	
}
