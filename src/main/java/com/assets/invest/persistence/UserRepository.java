package com.assets.invest.persistence;

import org.springframework.data.repository.CrudRepository;

import com.assets.invest.domain.User;

public interface UserRepository extends CrudRepository<User, Integer>{
	User findByUserId(int userId);
	
	User findByUserNm(String userNm);
}
