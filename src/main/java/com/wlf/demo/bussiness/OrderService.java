package com.wlf.demo.bussiness;

import org.springframework.stereotype.Service;

import com.wlf.demo.props.RabbitmqProps;

import java.util.UUID;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; 

@Service("orderService")
public class OrderService implements RabbitTemplate.ConfirmCallback {
	
	@Value("${spring.rabbitmq.keys.orderBounding}")
	private String boundingKey;
	
	@Value("${spring.rabbitmq.exchange}")
	private String testExchange;
	
	@Autowired
	private RabbitmqProps rabbitmqProps;
	
	private RabbitTemplate rabbitTemplate;

	/**
	 * ����ȷ�ϻ��ƣ������߷�����Ϣ��Ļص�.Ҳ�����Ƿ��·��
	 */
	public void confirm(CorrelationData correlationData, boolean ack, String cause) {

		System.out.println(" �ص�id:" + correlationData);  
        if (ack) {  
            System.out.println("�������ͳɹ���");  
        } else {  
            System.out.println("��������ʧ�ܣ������ʼ������Ż�������Ϣ֪ͨ�û�" + cause);  
        }  
		
	}

	@Autowired  
    public OrderService(RabbitTemplate rabbitTemplate) {  
        this.rabbitTemplate = rabbitTemplate;  
        rabbitTemplate.setConfirmCallback(this); 
    }  
	
	public void saveOrder(String id){
		rabbitTemplate.setRoutingKey(rabbitmqProps.getKeys().get("orderRouting"));
		CorrelationData correlationId = new CorrelationData(UUID.randomUUID().toString());
		rabbitTemplate.convertAndSend(testExchange, boundingKey, id, correlationId);
	}
	
}
