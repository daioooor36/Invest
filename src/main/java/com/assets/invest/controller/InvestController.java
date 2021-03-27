package com.assets.invest.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.assets.invest.service.InvestService;

@RestController
@RequestMapping(value="Invest")
public class InvestController {
	
	@Autowired
	InvestService investService;
	
	/**
     * 전체 투자 상품 조회 API
     */
	@GetMapping("/products")
	public List<Map<String, String>> getAllProducts() {
		return investService.getAllProducts();
	}
	
	/**
     * 투자하기 API
     */ 
	@PostMapping("/invest/{productId}")
	public Map<String, String> investToProduct(@PathVariable("productId") int productId
											, @RequestHeader("X-USER-ID") int userId
											, @RequestHeader("X-INVESTING-AMOUNT") int investingAmount) {
		return investService.investToProduct(userId, productId, investingAmount);
	}

	/**
     * 나의 투자상품 조회 API
     */
	@GetMapping("/search/myInvests")
	public List<Map<String, String>> getAllInvestedProducts(@RequestHeader("X-USER-ID") int userId) {
		return investService.getAllInvestedProducts(userId);
	}
}
