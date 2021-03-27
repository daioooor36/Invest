package com.assets.invest.domain;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class InvestOrders {
	private final List<InvestOrder> orders;

	public InvestOrders(List<InvestOrder> orders) {
		this.orders = orders;
	}
	
	// 전체 오더 수 반환
	public int getSize() {
		return orders.size();
	}
	
	// 특정 순번 오더 반환
	public InvestOrder get(int sequence) {
		return orders.get(sequence);
	}
	
	// 전체 투자금액 계산
	public int sumInvestedAmount() {
		int sum = 0;
		for(InvestOrder order : this.orders) {
			sum += order.getInvestingAmount();
		}
		return sum;
	}
	
	// 특정 상품의 투자 금액 합계
	public int sumInvestedAmountByProduct(Product product) {
		int sum = 0;
		for(InvestOrder order : this.orders) {
			if( product.equals(order.getProduct()) ) {
				sum += order.getInvestingAmount();
			}
		}
		return sum;
	}

	// 유저의 특정 상품 투자액 구하기
	public int sumInvestedAmountByUser(Product product, User user) {
		int sum = 0;
		boolean isEqualProduct = Boolean.FALSE;
		boolean isEqualUser = Boolean.FALSE;
		
		for(InvestOrder order : this.orders) {
			isEqualProduct = product.equals(order.getProduct());
			isEqualUser = user.equals(order.getUser());
			
			if( isEqualProduct && isEqualUser) {
				sum += order.getInvestingAmount();
			}
		}
		return sum;
	}
	
	// 특정 상품의 현재 투자 가능 금액
	public int investableAmount(Product product) {
		final int ZERO = 0;
		
		final int CURRENTLY_INVESTED_AMOUNT = this.sumInvestedAmountByProduct(product);
		final int INVESTING_LIMIT = product.getTotalInvestingAmount();
		final int BALANCE = INVESTING_LIMIT - CURRENTLY_INVESTED_AMOUNT;
		
		return BALANCE <= ZERO ? ZERO : BALANCE;
	}
	
	// 중복없이 특정 상품의 전체 투자자 구하기
	public int numberOfInvestors(Product product) {
		Map<Integer, Integer> investors = new HashMap<>();
		int userId = 0;

		for(InvestOrder order : this.orders) {
			if( product.equals(order.getProduct()) ) {
				userId = order.getUser().getUserId();
				investors.put(userId, 1);
			}
		}

		return investors.size();
	}
	
	// 상품의 최초 투자일자 조회
	public String firstInvestedDate(Product product, User user) {
		boolean isEqualProduct = Boolean.FALSE;
		boolean isEqualUser = Boolean.FALSE;
		Date targetDate = new Date();
		
		for(InvestOrder order : this.orders) {
			isEqualProduct = product.equals(order.getProduct());
			isEqualUser = user.equals(order.getUser());
			
			Date investedDate = order.getInvestedAt();
			
			if(isEqualProduct && isEqualUser && targetDate.compareTo(investedDate) > 0)
				targetDate = investedDate;
		}
		
		return targetDate.toString();
	}
}
