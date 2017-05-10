package com.wlf.demo.controller;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.wlf.demo.bussiness.OrderService;

@RestController
public class OrderController {

	@Autowired
	private OrderService orderService;
	
	@RequestMapping(value = "/rest/order/{id}",
			method = RequestMethod.POST, 
			produces = MediaType.TEXT_PLAIN_VALUE+";charset=UTF-8")
	public void saveOrder(@PathVariable String id,HttpServletRequest request,HttpServletResponse response){
		orderService.saveOrder(id);
	}
	
}
