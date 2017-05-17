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
@EnableRabbit //ʹ��@RabbitListener����Ӹ�ע��
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
        connectionFactory.setPublisherConfirms(rabbitmqProps.isPublisherConfirms()); //��Ϣ�ص�������Ҫ����  
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
        connectionFactory.setPublisherConfirms(rabbitmqProps.isPublisherConfirms()); //��Ϣ�ص�������Ҫ����  
        return connectionFactory;  
    }  
	
	@Bean
	public AmqpAdmin amqpAdmin() {
		return new RabbitAdmin(connectionFactory());
	}
	
	//ͨ��simpleRoutingConnectionFactory�������ö��virtual host
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
	
    //������prototype����  
    @Bean  
    @Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)  
    public RabbitTemplate rabbitTemplate() {  
        RabbitTemplate template = new RabbitTemplate(connectionFactory()); 
        
        template.setConfirmCallback((correlationData,ack,cause)->{
            if (ack) {  
                System.out.println(correlationData+"�ɹ����ｻ������");  
            } else {
                System.out.println(correlationData+"����ʧ��" + cause);  
            }  
        });
        
        template.setReturnCallback((message,replyCode,replyText,exchange,routingKey)->{
        	System.out.println("û���ҵ��κζ��У�"+
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
    
    /**
     * 
     * ���Ž�����
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
     * ���Ŷ���
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
     * ���Ű�
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
        factory.setDefaultRequeueRejected(true);
        factory.setAcknowledgeMode(AcknowledgeMode.AUTO);
        return factory;
    }
    
}
