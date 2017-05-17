package com.wlf.demo;

import java.util.HashMap;
import java.util.Map;

import org.springframework.amqp.core.AcknowledgeMode;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.annotation.EnableRabbit;
import org.springframework.amqp.rabbit.config.RetryInterceptorBuilder;
import org.springframework.amqp.rabbit.config.SimpleRabbitListenerContainerFactory;
import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.connection.SimpleRoutingConnectionFactory;
import org.springframework.amqp.rabbit.core.ChannelAwareMessageListener;
import org.springframework.amqp.rabbit.core.RabbitAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.core.RabbitTemplate.ReturnCallback;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.retry.RepublishMessageRecoverer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.retry.interceptor.RetryOperationsInterceptor;

import com.rabbitmq.client.Channel;
import com.wlf.demo.props.RabbitmqProps;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationProperties;

@SpringBootApplication
@EnableAutoConfiguration
@EnableRabbit //使用@RabbitListener必须加该注释
public class AmqpConfig {
	
	@Autowired
	private RabbitmqProps rabbitmqProps;

	@Bean
	@ConfigurationProperties(prefix="spring.rabbitmq")   
    public ConnectionFactory connectionFactory() {  
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();  
        connectionFactory.setChannelCacheSize(100);
        connectionFactory.setAddresses(rabbitmqProps.getAddresses());  
        connectionFactory.setUsername(rabbitmqProps.getUsername());  
        connectionFactory.setPassword(rabbitmqProps.getPassword());  
        connectionFactory.setVirtualHost("/");  
        connectionFactory.setPublisherConfirms(rabbitmqProps.isPublisherConfirms()); //消息回调，必须要设置  
        connectionFactory.setPublisherReturns(true);
        return connectionFactory;  
    }  
	
	@Bean
	@ConfigurationProperties(prefix="spring.rabbitmq")   
    public ConnectionFactory orderConnectionFactory() {  
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory();  
        connectionFactory.setAddresses(rabbitmqProps.getAddresses());  
        connectionFactory.setUsername(rabbitmqProps.getUsername());  
        connectionFactory.setPassword(rabbitmqProps.getPassword());  
        connectionFactory.setVirtualHost("/order-1");  
        connectionFactory.setPublisherConfirms(rabbitmqProps.isPublisherConfirms()); //消息回调，必须要设置  
        return connectionFactory;  
    }  
	
	@Bean
	public AmqpAdmin amqpAdmin() {
		return new RabbitAdmin(connectionFactory());
	}
	
	//通过simpleRoutingConnectionFactory可以设置多个virtual host
	@Bean
	public SimpleRoutingConnectionFactory simpleRoutingConnectionFactory(){
		SimpleRoutingConnectionFactory simpleRoutingConnectionFactory=new SimpleRoutingConnectionFactory();
		simpleRoutingConnectionFactory.setDefaultTargetConnectionFactory(connectionFactory());
		Map<Object,ConnectionFactory> targetConnectionFactory=new HashMap<Object,ConnectionFactory>();
		targetConnectionFactory.put("vhostA", connectionFactory());
		targetConnectionFactory.put("vhostB", orderConnectionFactory());
		simpleRoutingConnectionFactory.setTargetConnectionFactories(targetConnectionFactory);
		return simpleRoutingConnectionFactory;
	}
	
    //必须是prototype类型  
    @Bean  
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)  
    public RabbitTemplate rabbitTemplate() {  
        RabbitTemplate template = new RabbitTemplate(connectionFactory()); 
        
        template.setConfirmCallback((correlationData,ack,cause)->{
            if (ack) {  
                System.out.println(correlationData+"成功到达交换机！");  
            } else {
                System.out.println(correlationData+"发送失败" + cause);  
            }  
        });
        
        template.setReturnCallback((message,replyCode,replyText,exchange,routingKey)->{
        	System.out.println("没有找到任何队列！"+
        					  "message:"+message+
        					  ",replyCode:"+replyCode+
        					  ",replyText:"+replyText+
        					  ",exchange:"+exchange+
        					  ",routingKey:"+routingKey);
        });
        
        //template.setCorrelationDataPostProcessor((message, correlationData) ->
			//new CompleteMessageCorrelationData(correlationData != null ? correlationData.getId() : null, message));
    	//RabbitTemplate template = new RabbitTemplate(simpleRoutingConnectionFactory());  
        return template;  
    }  
	
    /**  
     * 针对消费者配置  
     * 1. 设置交换机类型  
     * 2. 将队列绑定到交换机  
     *   
     *   
        FanoutExchange: 将消息分发到所有的绑定队列，无routingkey的概念  
        HeadersExchange ：通过添加属性key-value匹配  
        DirectExchange:按照routingkey分发到指定队列  
        TopicExchange:多关键字匹配  
     */  

    @Bean  
    public DirectExchange defaultExchange() {  
        return new DirectExchange(rabbitmqProps.getExchange());  
    }
    
    /**
     * 
     * 死信交换机
     * 
     * @return
     */
    @Bean  
    public DirectExchange dlxExchange() {  
        return new DirectExchange("dlxExchange");  
    }

    @Bean  
    public Queue queue() {  
    	Map<String,Object> params=new HashMap<String,Object>();
    	params.put("x-dead-letter-exchange", "dlxExchange");
    	params.put("x-message-ttl", 10000);
        return new Queue(rabbitmqProps.getQueueName(), true, false, false,params); 
    }  
    
    /**
     * 
     * 死信队列
     * 
     * @return
     */
    @Bean
    public Queue dlxQueue(){
    	Map<String,Object> params=new HashMap<String,Object>();
    	Queue queue=new Queue("dlxQueue", true, false, false,params);
    	return queue;
    }
    
    @Bean  
    public Binding binding() {  
        return BindingBuilder.bind(queue()).to(defaultExchange()).with(rabbitmqProps.getKeys().get("orderRouting"));  
    }  

    /**
     * 
     * 死信绑定
     * 
     * @return
     */
    @Bean
    public Binding dlxBinding() {
    	return BindingBuilder.bind(dlxQueue()).to(dlxExchange()).with(rabbitmqProps.getKeys().get("orderRouting"));
    }
    
    /*
    @Bean  
    public SimpleMessageListenerContainer messageContainer() {  
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer(connectionFactory());  
        container.setQueues(queue());  
        container.setExposeListenerChannel(true);  
        container.setMaxConcurrentConsumers(10);  
        container.setConcurrentConsumers(3);  
        container.setAcknowledgeMode(AcknowledgeMode.MANUAL); //设置确认模式手工确认
        container.setMessageListener(new ChannelAwareMessageListener(){

			public void onMessage(Message message, Channel channel) throws Exception {
				byte[] body = message.getBody();  
                System.out.println("receive msg : " + new String(body));  
                channel.basicAck(message.getMessageProperties().getDeliveryTag(), false); //确认消息成功消费  
			}
        	
        });

        return container;  
    }  
    */
    
    //使用@RabbitListener必须配置该选项
    @Bean
    public SimpleRabbitListenerContainerFactory rabbitListenerContainerFactory() {
        SimpleRabbitListenerContainerFactory factory = new SimpleRabbitListenerContainerFactory();
        factory.setConnectionFactory(connectionFactory());
        factory.setConcurrentConsumers(3);
        factory.setMaxConcurrentConsumers(10);
        factory.setDefaultRequeueRejected(true);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        return factory;
    }
    
}
