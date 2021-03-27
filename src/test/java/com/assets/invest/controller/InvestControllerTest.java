package com.assets.invest.controller;

import static org.junit.Assert.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.MissingRequestHeaderException;

import com.assets.invest.controller.InvestController;

@RunWith(SpringRunner.class)
@SpringBootTest
public class InvestControllerTest {
	final int USER_ID = 10001;
	final int AMOUNT = 120000;
	final int PRODUCT_ID = 198;
	
	private MockMvc mockMvc;
	
	@Autowired
	private InvestController investController;
	
	
	@Before
	public void mockMvc_셋업() throws Exception {
	     mockMvc = MockMvcBuilders.standaloneSetup(investController).build();
	}
	
	@Test
	public void controller_investToProduct_호출()  throws Exception {
	    mockMvc.perform(post("/Invest/invest/" + PRODUCT_ID).header("X-USER-ID", USER_ID).header("X-INVESTING-AMOUNT", AMOUNT))
	    	.andExpect(status().isOk());
	}
	
	@Test
	public void controller_investToProduct_헤더파라미터_없을_경우()  throws Exception {
	    mockMvc.perform(post("/Invest/invest/" + PRODUCT_ID))
	    	.andExpect(result -> assertTrue(result.getResolvedException().getClass().isAssignableFrom(MissingRequestHeaderException.class)));
	}
}
