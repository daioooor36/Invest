package com.assets.invest.persistence;

import java.util.Date;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import com.assets.invest.domain.Product;

public interface ProductRepository extends CrudRepository<Product, Integer>{
	Product findByProductId(int productId);
	
	List<Product> findByStartedAtLessThanEqualAndFinishedAtGreaterThanEqual(Date currentDate1, Date currentDate2);

	Product findByProductIdAndStartedAtLessThanEqualAndFinishedAtGreaterThanEqual(int productId, Date currentDate1, Date currentDate2);
	
	@Query(value="SELECT DISTINCT PRD.*                  "
			   + "  FROM IV_USER         USR,            "
			   + "       IV_PRODUCT      PRD,            "
			   + "       IV_INVEST_ORDER ORD             "
			   + " WHERE ORD.USER_ID    = USR.USER_ID    "
			   + "   AND ORD.PRODUCT_ID = PRD.PRODUCT_ID "
			   + "   AND USR.USER_ID    = ?1", nativeQuery=true)
	List<Product> findAllByInvestOrders(int userId);
}
