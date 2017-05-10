package com.wlf.demo;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

import com.rabbitmq.client.Channel;
import com.wlf.demo.props.RabbitmqProps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;

@SpringBootApplication
@EnableAutoConfiguration
@EnableRabbit //ʹ��@RabbitListener����Ӹ�ע��
public class AmqpConfig {
	
	@Autowired
	private RabbitmqProps rabbitmqProps;

	@Bean
	@ConfigurationProperties(prefix="spring.rabbitmq")   
    public ConnectionFactory connectionFactory() {  
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();  
        connectionFactory.setAddresses(rabbitmqProps.getAddresses());  
        connectionFactory.setUsername(rabbitmqProps.getUsername());  
        connectionFactory.setPassword(rabbitmqProps.getPassword());  
        connectionFactory.setVirtualHost("/");  
        connectionFactory.setPublisherConfirms(rabbitmqProps.isPublisherConfirms()); //��Ϣ�ص�������Ҫ����  
        return connectionFactory;  
    }  
	
    //������prototype����  
    @Bean  
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)  
    public RabbitTemplate rabbitTemplate() {  
        RabbitTemplate template = new RabbitTemplate(connectionFactory());  
        return template;  
    }  
	
    /**  
     * �������������  
     * 1. ���ý���������  
     * 2. �����а󶨵�������  
     *   
     *   
        FanoutExchange: ����Ϣ�ַ������еİ󶨶��У���routingkey�ĸ���  
        HeadersExchange ��ͨ���������key-valueƥ��  
        DirectExchange:����routingkey�ַ���ָ������  
        TopicExchange:��ؼ���ƥ��  
     */  

    @Bean  
    public DirectExchange defaultExchange() {  
        return new DirectExchange(rabbitmqProps.getExchange());  
    }

    @Bean  
    public Queue queue() {  
        return new Queue(rabbitmqProps.getQueueName(), true); //���г־�  
    }  

    @Bean  
    public Binding binding() {  
        return BindingBuilder.bind(queue()).to(defaultExchange()).with(rabbitmqProps.getKeys().get("orderBounding"));  
    }  

    /*
    @Bean  
    public SimpleMessageListenerContainer messageContainer() {  
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());  
        container.setQueues(queue());  
        container.setExposeListenerChannel(true);  
        container.setMaxConcurrentConsumers(10);  
        container.setConcurrentConsumers(3);  
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL); //����ȷ��ģʽ�ֹ�ȷ��
        container.setMessageListener(new ChannelAwareMessageListener(){

			public void onMessage(Message message, Channel channel) throws Exception {
				byte[] body = message.getBody();  
                System.out.println("receive msg : " + new String(body));  
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false); //ȷ����Ϣ�ɹ�����  
			}
        	
        });

        return container;  
    }  
    */
    
    //ʹ��@RabbitListener�������ø�ѡ��
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        return factory;
    }
    
}
