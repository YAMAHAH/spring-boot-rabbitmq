package com.wlf.demo;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;

public class SendTest {
	
	private static String QUEUE_NAME="testQueue";

	public static void main(String[] argv) throws Exception{
		//create connection
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost("192.168.58.144");
		factory.setPort(5672);
		factory.setUsername("admin");
		factory.setPassword("nmamtf");
		factory.setVirtualHost("/");
		
		Connection connection = factory.newConnection();
		//create channel
		Channel channel = connection.createChannel();
		//create queue
		channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		String message = "Hello World!";
		channel.basicPublish("", QUEUE_NAME, null, message.getBytes());
		System.out.println(" [x] Sent '" + message + "'");
		//finally
		channel.close();
		connection.close();
	}
	
}
