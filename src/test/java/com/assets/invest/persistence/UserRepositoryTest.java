package com.assets.invest.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;

import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.MethodSorters;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

import com.assets.invest.domain.User;
import com.assets.invest.persistence.UserRepository;

@FixMethodOrder(MethodSorters.NAME_ASCENDING)
@RunWith(SpringRunner.class)
@SpringBootTest
public class UserRepositoryTest {
	final String USER_NAME = "앨런머스크";
	final String USER_PASSWORD = "4321";
	
	static int _targetUserId = 0; // 21/03/10 SIS : static이 아니면 전체테스트 완수가 안됨. 멤버변수는 Test마다 초기 값이 보장되는 듯하다.
	
	@Autowired
	private UserRepository userRepo;
	
	@Test
	public void _1유저_생성하기() {
		_targetUserId = createUser(USER_NAME, USER_PASSWORD);
		
		Optional<User> maybeUser = userRepo.findById(_targetUserId);
		assertThat(maybeUser.isPresent()).isTrue();
	}
	
	@Test
	@Transactional
	public void _2유저_비교하기_ID가_다르면_다른_객체이다() {
		int newUserId = createUser(USER_NAME, USER_PASSWORD);
		
		User selectedUser1 = userRepo.findByUserId(_targetUserId);
		User selectedUser2 = userRepo.findByUserId(newUserId);
		
		assertThat(selectedUser1.equals(selectedUser2)).isFalse();
	}
	
	@Test
	public void _3유저_명_일치_확인하기() {
		Optional<User> maybeUser = userRepo.findById(_targetUserId);
		
		assertThat(maybeUser.get().getUserNm()).isEqualTo(USER_NAME);
	}
	
	@Test
	public void _4유저_암호_일치_확인하기() {
		Optional<User> maybeUser = userRepo.findById(_targetUserId);
		
		assertThat(maybeUser.get().getPassword()).isEqualTo(USER_PASSWORD);
	}
	
	@Test
	public void _5유저_명_수정하기() {
		final String modifiedUserName = "앨런";
		
		User selectedUser = userRepo.findByUserId(_targetUserId);
		selectedUser.setUserNameSafely(modifiedUserName, selectedUser.getPassword());
		userRepo.save(selectedUser);
		
		User modifiedUser = userRepo.findByUserId(_targetUserId);
		assertThat(modifiedUser.getUserNm()).isEqualTo(modifiedUserName);
	}
	
	@Test
	public void _6잘못된_유저_ID_검지하기() {
		final int NON_EXISTING_USER_ID = -9999;
		User selectedUser = userRepo.findByUserId(NON_EXISTING_USER_ID);
		
		assertThatThrownBy(() -> selectedUser.getUserId()).isInstanceOf(NullPointerException.class);
	}
	
	@Test
	public void _99유저_삭제하기() {
		User selectedProduct = userRepo.findByUserId(_targetUserId);
		userRepo.delete(selectedProduct);
		
		Optional<User> maybeUser = userRepo.findById(_targetUserId);
		assertThat(maybeUser.isPresent()).isFalse();
	}
	
	//////////////////////////////////////////////////////////////////////////////////

	// 더미 User 생성
	private int createUser(String userName, String userPassword) {
		User user = new User(userName, userPassword);
		userRepo.save(user);
		
		return user.getUserId();
	}
}
