package com.wlf.demo;

import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.junit.Test;

public class OrderTest {

	@Test
	public void loginTest() throws Exception{
		String url="http://localhost:8080/rest/order/order_123_666_888";
		CloseableHttpClient httpclient=HttpClients.createDefault();
        HttpPost httppost=new HttpPost(url);
        httpclient.execute(httppost);
	}
	
}
