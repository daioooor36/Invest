package com.assets.invest.service;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.assets.invest.domain.InvestOrder;
import com.assets.invest.domain.InvestOrders;
import com.assets.invest.domain.Product;
import com.assets.invest.domain.User;
import com.assets.invest.enums.Message;
import com.assets.invest.enums.Status;
import com.assets.invest.function.ReturnSettable;
import com.assets.invest.persistence.InvestOrderRepository;
import com.assets.invest.persistence.ProductRepository;
import com.assets.invest.persistence.UserRepository;

@Service
public class InvestServiceImpl implements InvestService {
	static final int ZERO = 0;
	static final ReturnSettable RETURN_SETTER = (message) -> {
		Map<String, String> map = new HashMap<>();
		map.put("RESULT_FLAG"   , message.getMessageResult());
		map.put("RESULT_CODE"   , message.getMessageCode());
		map.put("RESULT_MESSAGE", message.getMessageContent());
		return map;
	};

	@Autowired
	private UserRepository userRepo;

	@Autowired
	private ProductRepository productRepo;

	@Autowired
	private InvestOrderRepository investOrderRepo;

	/**
	 * @title : 판매 중인 전체 상품 정보 조회
	 * @content : 
	 * 1. 현재 날짜 기준으로 판매 중인 상품 List를 조회한다.
	 * 2. 해당 상품들의 각 투자자 수, 모집 한도, 현재 모집 금액, 모집 상태를 구한다.
	 * 
	 * @return 현재 투자가능한 상품 목록들
	 * 
	 * @date 2021/03/12
	 * @author Iksoo Shin
	 */
	@Override
	public List<Map<String, String>> getAllProducts() {
		final List<Product> products = productRepo.findByStartedAtLessThanEqualAndFinishedAtGreaterThanEqual(new Date(), new Date());
		List<Map<String, String>> validProducts = new ArrayList<>();
		
		for(Product product : products) {
			final InvestOrders orders = new InvestOrders(product.getInvestOrders());
			
			final int numberOfInvestors	= orders.numberOfInvestors(product);			// 총 투자자 수
			final int sumInvestedAmount	= orders.sumInvestedAmountByProduct(product);	// 현재 모집 금액
			final Status status = product.nowFullAmount() ? Status.COMPLETED : Status.INVESTING; // 모집 상태
			
			product.setCurrentStatus(numberOfInvestors, sumInvestedAmount, status);
			validProducts.add(product.convertToMapForView());
		}
		
		// [예외] 투자가능한 상품이 없을 경우
		if(validProducts.size() == ZERO) {
			validProducts.add(RETURN_SETTER.returnSet(Message.NOT_INVESTABLE_PRODUCT));
		}
		
		return validProducts;
	}
	
	/**
	 * @title : 특정 상품에 투자하기
	 * @content : 
	 * 1. Validation 체크를 수행한다.
	 * 2. 해당 상품의 현재 모집된 금액, 투자가능 잔액을 조회한다.
	 * 3. 투자할 금액이 투자가능 잔액보다 작다면 투자한다.
	 * 
	 * @param USER_ID : 유저 ID
	 * @param PRODUCT_ID : 상품 ID
	 * @param INVESTING_AMOUNT : 투자할 금액
	 * @return 투자 결과 상태메시지
	 * 
	 * @date 2021/03/12
	 * @author Iksoo Shin
	 */
	@Override
	@Transactional
	public Map<String, String> investToProduct(final int USER_ID, final int PRODUCT_ID, final int INVESTING_AMOUNT) {
		final User user = userRepo.findByUserId(USER_ID);
		final Product product = productRepo.findByProductId(PRODUCT_ID);
		
		// [예외] 투자 금액 0원 이하일 경우
		if(INVESTING_AMOUNT <= ZERO) {
			return RETURN_SETTER.returnSet(Message.INVESTING_AMOUNT_LESS_THAN_ZERO);
		}		
		// [예외] Header Parameter로 User정보를 못 받은 경우
		if(user == null) {
			return RETURN_SETTER.returnSet(Message.NOT_EXISTS_USER_ID);
		}		
		// [예외] 상품 코드가 잘못되거나 모집기간이 아닌 경우
		if(! product.nowInvestableDate()) {
			return RETURN_SETTER.returnSet(Message.MISMATCHED_PRODUCT_ID);
		}
		
		int remainingAmount = ZERO;				// 현재 투자 가능 금액
		boolean isCompleted = Boolean.FALSE;	// 투자 완료 여부
		
		// 투자를 실행하는 부분으로 동기화 처리함
		synchronized(this) {
			final InvestOrders orders = new InvestOrders(product.getInvestOrders());
			remainingAmount = orders.investableAmount(product);	// 현재 투자 가능 금액
			
			// [예외] SOLD_OUT된 상품의 경우
			if(remainingAmount == ZERO) {
				return RETURN_SETTER.returnSet(Message.WAS_SOLD_OUT); // 결과 세팅 : 매진된 상태
			}
			
			// 입력 금액의 투자가능 여부 확인
			if(remainingAmount >= INVESTING_AMOUNT) {
				isCompleted = doInvest(user, product, INVESTING_AMOUNT);	// 투자 실행
				remainingAmount -= INVESTING_AMOUNT; 						// 투자 가능 금액 차감
			} else {
				Map<String, String> resultMap = RETURN_SETTER.returnSet(Message.OVERFLOW_INVESTABLE_AMOUNT); // 결과 세팅 : 투자가능 금액 초과
				resultMap.put("RESULT_MESSAGE", "[" + product.getProductNm() + "]상품의 투자가능 금액이 초과하였습니다.<br>현재 투자가능 금액: " + remainingAmount);
				resultMap.put("RESULT_INVESTABLE_AMOUNT", Integer.toString(remainingAmount));
				return resultMap;
			}
		}
		
		// 투자완료 후, 리턴 값 세팅
		if(isCompleted) {
			return afterCompleted(product, INVESTING_AMOUNT, remainingAmount);
		} else {
			return RETURN_SETTER.returnSet(Message.ERROR_IN_INVESTING_PROCESS);
		}
	}

	/**
	 * @title 유저가 투자한 상품들을 조회한다.
	 * @content
	 * 1. 상품에 투자한 이력이 있는지 조회한다.
	 * 2. 유저가 투자한 상품에 대한 상품 Entity를 불러온다
	 * 3. 해당 상품에서 유저가 투자한 금액 및 총 투자된 금액 등을 조회한다.
	 * 
	 * @param USER_ID : 유저 ID
	 * @return 유저가 투자한 상품들의 List
	 * 
	 * @date 2021/03/12
	 * @author Iksoo Shin
	 */
	@Override
	public List<Map<String, String>> getAllInvestedProducts(final int USER_ID) {
		final User user = userRepo.findByUserId(USER_ID);
		List<Map<String, String>> myInvestments = new ArrayList<>();
		
		// [예외] 투자한 이력이 없을 경우
		if(user.getInvestOrders().size() == ZERO) {
			myInvestments.add(RETURN_SETTER.returnSet(Message.NO_INVESTED_EVER));
			return myInvestments;
		}
		
		List<Product> products = productRepo.findAllByInvestOrders(USER_ID);		
		for(Product product : products) {
			myInvestments.add(getMyInvestedForProduct(product, user));
		}
		
		return myInvestments;
	}

	/*****************************************************************************/
	
	// 투자 실행
	private boolean doInvest(User user, Product product, final int INVESTING_AMOUNT) {
		InvestOrder order = InvestOrder.of(user, product, INVESTING_AMOUNT);
		
		order = investOrderRepo.save(order);
		
		return investOrderRepo.findByOrderId(order.getOrderId()).equals(order);
	}
	
	// 투자 완료 후, 반환메시지 입력
	private Map<String, String> afterCompleted(Product targetProduct, final int INVESTING_AMOUNT, final int REMAINING_INVESTABLE_AMOUNT) {
		Map<String, String> returnValue = null;
		
		if(REMAINING_INVESTABLE_AMOUNT <= ZERO)
			returnValue = RETURN_SETTER.returnSet(Message.COMPLETELY_SOLD_OUT);	// 결과 세팅 : RESULT_FLAG = "SO"
		else
			returnValue = RETURN_SETTER.returnSet(Message.COMPLETE);			// 결과 세팅 : RESULT_FLAG = "Y"
		
		setReturnValues(returnValue, targetProduct, INVESTING_AMOUNT);
		
		return returnValue;
	}

	// 투자 완료 후, 반환 값 세팅
	private void setReturnValues(Map<String, String> result, Product validProduct, final int INVESTING_AMOUNT) {
		result.put("investedProductId", Integer.toString(validProduct.getProductId()));
		result.put("investedProductNm", validProduct.getProductNm());
		result.put("investedAmount"   , Integer.toString(INVESTING_AMOUNT));
	}

	// 특정 상품의 유저가 투자한 내역을 세팅하여 Map으로 반환한다.
	private Map<String, String> getMyInvestedForProduct(Product product, User user) {
		final InvestOrders ordersByProduct = new InvestOrders(product.getInvestOrders());
		
		int totalInvestedAmount  = ordersByProduct.sumInvestedAmountByProduct(product);		// 상품의 총 모집 금액
		int myInvestingAmount    = ordersByProduct.sumInvestedAmountByUser(product, user);	// 유저의 상품 별 투자금
		String firstInvestedDate = ordersByProduct.firstInvestedDate(product, user);		// 유저의 해당 상품 최초 투자 일시
		
		Map<String, String> myInvestment = new HashMap<>();
		myInvestment.put("productId"           , Integer.toString(product.getProductId())); // 상품 ID
		myInvestment.put("productNm"           , product.getProductNm()); 					// 상품 명
		myInvestment.put("totalInvestingAmount", Integer.toString(totalInvestedAmount));	// 상품의 총 모집 금액
		myInvestment.put("myInvestedAmount"    , Integer.toString(myInvestingAmount));      // 나의 투자 금액
		myInvestment.put("investedAt"          , firstInvestedDate);      					// 나의 최초 투자 일시		
		return myInvestment;
	}
}