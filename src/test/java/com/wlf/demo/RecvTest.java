package com.wlf.demo;

import java.io.IOException;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;


public class RecvTest {
	
	private static String QUEUE_NAME="test";

	public static void main(String[] argv) throws Exception{
		
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("192.168.58.144");
		factory.setPort(5672);
		factory.setUsername("admin");
		factory.setPassword("nmamtf");
		factory.setVirtualHost("/");
	    Connection connection = factory.newConnection();
	    Channel channel = connection.createChannel();

	    //we declare the queue here, as well. Because we might start the consumer before the publisher 
	    channel.queueDeclare(QUEUE_NAME, false, false, false, null);
	    System.out.println(" [*] Waiting for messages.");
	    
	    Consumer consumer = new DefaultConsumer(channel) {
    	  @Override
    	  public void handleDelivery(String consumerTag, Envelope envelope,
    	                             AMQP.BasicProperties properties, byte[] body)
    	      throws IOException {
    	    String message = new String(body, "UTF-8");
    	    System.out.println(" [x] Received '" + message + "'");
    	  }
    	};
    	channel.basicConsume(QUEUE_NAME, true, consumer);
    	
	}
	
}
