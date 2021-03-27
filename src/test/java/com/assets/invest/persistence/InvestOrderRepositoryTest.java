package com.assets.invest.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.validation.ValidationException;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.assets.invest.domain.InvestOrder;
import com.assets.invest.domain.InvestOrders;
import com.assets.invest.domain.Product;
import com.assets.invest.domain.User;
import com.assets.invest.persistence.InvestOrderRepository;
import com.assets.invest.persistence.ProductRepository;
import com.assets.invest.persistence.UserRepository;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest
public class InvestOrderRepositoryTest {
	/* for dummyProdct */
	final int INVEST_AMOUNT_LIMIT  = 600_000;
	final String PRODUCT_NM  = "개인신용 포트폴리오";
	final String TARGET_DATE = "2021-03-15T12:30:30+0900";
	/* for dummyUser */
	final String USER_NAME = "앨런머스크";
	final String USER_NAME2 = "앨런머스크";
	final String USER_PASSWORD = "4321";
	/* for testClass */
	final int FIRST_ORDER_SEQ = 2_000;
	final int INVEST_AMOUNT = 100_000;
	final int NUMBER_OF_CREATED_ORDER = 3;
	
	static Product _dummyProduct = null;
	static User _dummyUser = null;
	static InvestOrders _investOrders = null;
	static int totalInvestedAmount = 0;
		
	@Autowired
	private UserRepository userRepo;

	@Autowired
	private ProductRepository productRepo;

	@Autowired
	private InvestOrderRepository investOrderRepo;
	
	
	@Test
	public void _1투자_데이터_생성() {
		_dummyProduct = createProduct(PRODUCT_NM, INVEST_AMOUNT_LIMIT);
		_dummyUser    = createUser(USER_NAME, USER_PASSWORD);
		
		_investOrders = new InvestOrders(createInvestOrder(_dummyProduct, _dummyUser, INVEST_AMOUNT));
		assertThat(_investOrders.getSize()).isEqualTo(NUMBER_OF_CREATED_ORDER);
	}

	@Test
	@Transactional
	public void _2모집_금액_벗어나는_투자_데이터_생성방지_by_FactoryMethod() {
		assertThatThrownBy(() -> createInvestOrder(_dummyProduct, _dummyUser, INVEST_AMOUNT_LIMIT * 2)).isInstanceOf(ValidationException.class);
	}

	@Test
	public void _3투자정보_있을때_유저를_삭제_하면_에러발생() {
	    assertThatThrownBy(() -> userRepo.delete(_dummyUser)).isInstanceOf(DataIntegrityViolationException.class);
	}
	
	@Test
	public void _4투자정보_있을때_상품을_삭제_하면_에러발생() {
	    assertThatThrownBy(() -> productRepo.delete(_dummyProduct)).isInstanceOf(DataIntegrityViolationException.class);
	}

	@Test
	public void _5상품의_총_투자금_비교() {
		int totalInvestedAmount  = _investOrders.sumInvestedAmountByProduct(_dummyProduct);
		assertThat(totalInvestedAmount).isEqualTo(totalInvestedAmount);
	}

	@Test
	public void _6투자_객체간_비교_ONLY_ID_COMPARING() {
		final int FIRST = 0;
		
		InvestOrder orderSample = _investOrders.get(FIRST);
		InvestOrder dummyInvestOrder = InvestOrder.of(orderSample.getUser(), orderSample.getProduct(), orderSample.getInvestingAmount());

		assertThat(orderSample.equals(dummyInvestOrder)).isFalse();
	}
	
	@Test
	public void _7상품의_유저_투자금_비교() {
		assertThat(_investOrders.sumInvestedAmountByUser(_dummyProduct, _dummyUser)).isEqualTo(INVEST_AMOUNT_LIMIT);
	}
	
	@Test
	@Transactional
	public void _8상품의_중복없이_투자자_수() {
		User _dummyUser2 = createUser(USER_NAME2, USER_PASSWORD);
		createInvestOrder(_dummyProduct, _dummyUser2, INVEST_AMOUNT);
		
		InvestOrders orders = new InvestOrders(investOrderRepo.findAll());
		assertThat(orders.numberOfInvestors(_dummyProduct)).isEqualTo(2);
	}
	
	@Test
	public void _99투자정보_삭제() {
		final int PRODUCT_ID = _dummyProduct.getProductId();
		final int USER_ID    = _dummyUser.getUserId();
		List<Integer> investOrderIds = deleteAndGetOrderIds(_investOrders); // 삭제 & 삭제된 PK를 반환한다.
		
		productRepo.delete(_dummyProduct);
		userRepo.delete(_dummyUser);
		
		//////////////////////////////////////////////////////
		
		for(Integer investOrderId : investOrderIds) {
			Optional<InvestOrder> maybeinvestOrder = investOrderRepo.findById(investOrderId);
			assertThat(maybeinvestOrder.isPresent()).isFalse();
		}

		Optional<Product> maybeProduct = productRepo.findById(PRODUCT_ID);
		assertThat(maybeProduct.isPresent()).isFalse();
		
		Optional<User> maybeUser = userRepo.findById(USER_ID);
		assertThat(maybeUser.isPresent()).isFalse();
	}

	//////////////////////////////////////////////////////////////////////////////////

	// Product 생성용 더미 Date 반환
	private Date getTargetDate(String targetDate) {
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Date date = null;
        
        try {
			date = df.parse(targetDate);
		} catch (ParseException e) {
			e.printStackTrace();
		}
        
        return date;	        
	}

	// 더미 Product 생성
	private Product createProduct(String productNm, int investAmount) {
		Product product = new Product(productNm, investAmount, new Date(), getTargetDate(TARGET_DATE));
		productRepo.save(product);
		
		return product;
	}

	// 더미 User 생성
	private User createUser(String userName, String userPassword) {
		User user = new User(userName, userPassword);
		userRepo.save(user);
		
		return user;
	}

	// 더미 InvestOrders 생성
	private List<InvestOrder> createInvestOrder(Product product, User user, int investAmount) {
		List<InvestOrder> investOrders = new ArrayList<>();
		
		for(int i = 1; i <= NUMBER_OF_CREATED_ORDER; i++) {
			InvestOrder order = InvestOrder.of(user, product, investAmount * i);
			order = investOrderRepo.save(order);
			investOrders.add(order);

			totalInvestedAmount += investAmount * i;
		}
		return investOrders;
	}

	// List<InvestOrder> 에 대한 모든 entity를 삭제하고 PK를 반환한다.
	private List<Integer> deleteAndGetOrderIds(InvestOrders investOrders) {
		List<Integer> ids = new ArrayList<>();
		
		for(int i=0; i<investOrders.getSize(); i++) {
			ids.add(investOrders.get(i).getOrderId());
			
			investOrderRepo.delete(investOrders.get(i));
		}
		
		return ids;
	}
}
