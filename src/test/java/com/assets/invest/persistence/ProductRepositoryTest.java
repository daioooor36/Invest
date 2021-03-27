package com.assets.invest.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.assets.invest.domain.Product;
import com.assets.invest.persistence.ProductRepository;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest
public class ProductRepositoryTest {
	final int INVEST_AMOUNT  = 1_000_000;
	final String PRODUCT_NM  = "개인신용 포트폴리오";
	final String TARGET_DATE = "2021-03-15T12:30:30+0900";
	
	static int _targetProductId = 0; // 21/03/10 SIS : static이 아니면 전체테스트 완수가 안됨. 멤버변수는 Test마다 초기 값이 보장되는 듯하다.
	
	@Autowired
	private ProductRepository productRepo;
	
	
	@Test
	public void _1상품_생성하기() {
		_targetProductId = createProduct(PRODUCT_NM, INVEST_AMOUNT);
		
		Optional<Product> maybeProduct = productRepo.findById(_targetProductId);
		assertThat(maybeProduct.isPresent()).isTrue();
	}
	
	@Test
	public void _2상품_명_일치_확인하기() {
		Optional<Product> maybeProduct = productRepo.findById(_targetProductId);
		
		assertThat(maybeProduct.get().getProductNm()).isEqualTo(PRODUCT_NM);
	}
	
	@Test
	public void _3상품_금액_일치_확인하기() {
		Optional<Product> maybeProduct = productRepo.findById(_targetProductId);
		
		assertThat(maybeProduct.get().getTotalInvestingAmount()).isEqualTo(INVEST_AMOUNT);
	}
	
	@Test
	public void _4모집중_상품의_금액_수정하기() {
		final int MODIFIED_AMOUNT = 2_000_000;
		
		Product selectedProduct = productRepo.findByProductId(_targetProductId);
		selectedProduct.setTotalInvestingAmountSafely(MODIFIED_AMOUNT);
		productRepo.save(selectedProduct);
		
		Product modifiedProduct = productRepo.findByProductId(_targetProductId);
		assertThat(modifiedProduct.getTotalInvestingAmount()).isEqualTo(MODIFIED_AMOUNT);
	}
	
	@Test
	@Transactional
	public void _5모집완료_상품의_금액_수정하기() {
		final int EXPIRED_PRODUCT_ID = createExpiredProduct(PRODUCT_NM, INVEST_AMOUNT);
		final int MODIFIED_AMOUNT = 2_000_000;
		
		Product selectedProduct = productRepo.findByProductId(EXPIRED_PRODUCT_ID);
		selectedProduct.setTotalInvestingAmountSafely(MODIFIED_AMOUNT);
		productRepo.save(selectedProduct);
		
		Product modifiedProduct = productRepo.findByProductId(EXPIRED_PRODUCT_ID);
		assertThat(modifiedProduct.getTotalInvestingAmount()).isNotEqualTo(MODIFIED_AMOUNT);
	}
	
	@Test
	public void _6해당_상품이_현재_모집중인지_확인하기() {
		Date date = getTargetDate(TARGET_DATE);
		Product product = productRepo.findByProductIdAndStartedAtLessThanEqualAndFinishedAtGreaterThanEqual(_targetProductId, date, date);
		
		assertThat(product.getProductId()).isEqualTo(_targetProductId);
	}
	
	@Test
	public void _7모집중인_상품_검색하기() {
		Date date = getTargetDate(TARGET_DATE);
		List<Product> selectedProducts = productRepo.findByStartedAtLessThanEqualAndFinishedAtGreaterThanEqual(date, date);
		int getSizeOfSelectedProducts = selectedProducts.size();

		assertThat(selectedProducts).filteredOn(product -> product.getStartedAt().compareTo(date) <= 0)
			.filteredOn(product -> product.getFinishedAt().compareTo(date) >= 0)
			.size()
			.isEqualTo(getSizeOfSelectedProducts);
	}
	
	@Test
	public void _8모집완료된_상품인지_검지하기() {
		final String FINISHED_DATE_TIME = "2021-03-15T12:30:31+0900"; // 해당상품의 모집 기간에서 1초가 지난 시각
		final Date finishedDate = getTargetDate(FINISHED_DATE_TIME);
		
		Product product = productRepo.findByProductIdAndStartedAtLessThanEqualAndFinishedAtGreaterThanEqual(_targetProductId, finishedDate, finishedDate);
		
		assertThatThrownBy(() -> product.getProductId()).isInstanceOf(NullPointerException.class);
	}
	
	@Test
	public void _99상품_삭제하기() {
		Product selectedProduct = productRepo.findByProductId(_targetProductId);
		productRepo.delete(selectedProduct);
		
		Optional<Product> maybeProduct = productRepo.findById(_targetProductId);
		assertThat(maybeProduct.isPresent()).isFalse();
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
	private int createProduct(String productNm, int investAmount) {
		Product product = new Product(productNm, investAmount, new Date(), getTargetDate(TARGET_DATE));
		productRepo.save(product);
		
		return product.getProductId();
	}

	// 기간만료 더미 Product 생성
	private int createExpiredProduct(String productNm, int investAmount) {
		final String EXPIRED_DATE_TIME = "2021-01-01T12:30:31+0900";
		final Date expiredDate = getTargetDate(EXPIRED_DATE_TIME);
		
		Product product = new Product(productNm, investAmount, expiredDate, expiredDate);
		productRepo.save(product);
		
		return product.getProductId();
	}
}
