package com.wlf.demo.bussiness;

import org.springframework.stereotype.Service;

import com.wlf.demo.props.RabbitmqProps;

import java.util.UUID;

import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePropertiesBuilder;
import org.springframework.amqp.rabbit.connection.Connection;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.SimpleResourceHolder;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnCallback;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value; 

@Service("orderService")
public class OrderService {
	
	@Value("${spring.rabbitmq.keys.orderBounding}")
	private String boundingKey;
	
	@Value("${spring.rabbitmq.exchange}")
	private String testExchange;
	
	@Autowired
	private RabbitmqProps rabbitmqProps;

	@Autowired
	private RabbitTemplate rabbitTemplate;

	//@Autowired  
    //public OrderService(RabbitTemplate rabbitTemplate) {  
        //this.rabbitTemplate = rabbitTemplate;  
        //rabbitTemplate.setConfirmCallback(this); 
    //}  
	
	public void saveOrder(String id){
		//rabbitTemplate.setRoutingKey("aaa"/*rabbitmqProps.getKeys().get("orderRouting")*/);
		CorrelationData correlationId = new CorrelationData(id);
		//SimpleResourceHolder.bind(rabbitTemplate.getConnectionFactory(), "vhostA");
		rabbitTemplate.setMandatory(true);
		
		//Message msg=new Message(id.getBytes(),MessagePropertiesBuilder.newInstance().setExpiration("10000").build());
		rabbitTemplate.convertAndSend(testExchange, "order", id, correlationId);

		//SimpleResourceHolder.unbind(rabbitTemplate.getConnectionFactory());
	}
	
}
