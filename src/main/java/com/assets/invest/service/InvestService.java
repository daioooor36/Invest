package com.assets.invest.service;

import java.util.List;
import java.util.Map;

public interface InvestService {
	// 전체 투자 상품 조회
	List<Map<String, String>> getAllProducts();
	
	// 상품 투자하기
	Map<String, String> investToProduct(int userId, int productId, int investingAmount);
	
	// 나의 투자상품 조회
	List<Map<String, String>> getAllInvestedProducts(int userId);
}
