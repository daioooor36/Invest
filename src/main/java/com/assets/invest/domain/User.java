package com.assets.invest.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

import org.hibernate.annotations.DynamicInsert;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity
@ToString(exclude="investOrders")
@JsonIgnoreProperties("investOrders")
@DynamicInsert
@NoArgsConstructor
@SequenceGenerator(name="USER_SEQ_GENERATOR",
				sequenceName="USER_SEQUENCE",
				initialValue=10001,
				allocationSize=1)
@Table(name = "IV_USER")
public class User {
	@Id @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="USER_SEQ_GENERATOR")
	private int userId;
	
	private String userNm;

	@JsonIgnore
	private String password;
	
	@Column(insertable=false, columnDefinition="TIMESTAMP DEFAULT SYSDATE")
	private Date lastPwChangedAt;

	@JsonIgnore
	@Column(insertable=false, updatable=false, columnDefinition="TIMESTAMP DEFAULT SYSDATE")
	private Date createdAt;

	@JsonIgnore
	@Column(columnDefinition="varchar(255) default 'SYSTEM'")
	private String createdBy;

	@JsonIgnore
	@Column(insertable=false, columnDefinition="TIMESTAMP DEFAULT SYSDATE")
	private Date updatedAt;

	@JsonIgnore
	@Column(columnDefinition="varchar(255) default 'SYSTEM'")
	private String updatedBy;
	
	@JsonManagedReference
	@OneToMany(mappedBy="user", fetch=FetchType.LAZY)
	private List<InvestOrder> investOrders = new ArrayList<>();


	public User(String userNm, String password) {
		super();
		this.userNm = userNm;
		this.password = password;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + userId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		User other = (User) obj;
		if (userId != other.getUserId())
			return false;
		return true;
	}
	
	// 패스워드가 일치할 때만 이름 변경이 가능함
	public boolean setUserNameSafely(String modifiedUserName, String password) {
		if(password.equals(this.password)) {
			this.userNm = modifiedUserName;
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}
}