package com.assets.invest.persistence;

import java.util.List;

import org.springframework.data.repository.CrudRepository;

import com.assets.invest.domain.InvestOrder;
import com.assets.invest.domain.Product;
import com.assets.invest.domain.User;

public interface InvestOrderRepository extends CrudRepository<InvestOrder, Integer>{
	List<InvestOrder> findAll();
	
	List<InvestOrder> findAllByUser(User user);

	List<InvestOrder> findAllByProduct(Product product);
	
	InvestOrder findByOrderId(int orderId);
}
