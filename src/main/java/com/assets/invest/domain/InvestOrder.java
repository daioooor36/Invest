package com.assets.invest.domain;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.validation.ValidationException;

import org.hibernate.annotations.DynamicInsert;

import com.fasterxml.jackson.annotation.JsonBackReference;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString(exclude={"user", "product"})
@Entity
@DynamicInsert
@NoArgsConstructor
@SequenceGenerator(name="ORDER_SEQ_GENERATOR",
				sequenceName="ORDER_SEQUENCE",
				initialValue=1001,
				allocationSize=1)
@Table(name = "IV_INVEST_ORDER")
public class InvestOrder {
	@Id @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="ORDER_SEQ_GENERATOR")
	private int orderId;

	@Column(columnDefinition="NOT NULL")
	private int investingAmount;

	@Column(insertable=false, updatable=false, columnDefinition="TIMESTAMP DEFAULT SYSDATE")
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date investedAt;
	
	@Column(insertable=false, updatable=false, columnDefinition="TIMESTAMP DEFAULT SYSDATE")
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date createdAt;
	
	@Column(columnDefinition="varchar(255) default 'SYSTEM' NOT NULL")
	private String createdBy;
	
	@Column(insertable=false, columnDefinition="TIMESTAMP DEFAULT SYSDATE")
	@Temporal(value = TemporalType.TIMESTAMP)
	private Date updatedAt;

	@Column(columnDefinition="varchar(255) default 'SYSTEM' NOT NULL")
	private String updatedBy;

	@JsonBackReference
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="USER_ID", nullable=false)
	private User user;

	@JsonBackReference
	@ManyToOne(fetch=FetchType.LAZY)
	@JoinColumn(name="PRODUCT_ID", nullable=false)
	private Product product;

	@Transient
	private int currentlyInvestedAmount;
	
	@Transient
	private int numberOfInvestors;

	
	private InvestOrder(User user, Product product, int investingAmount) {
		this.user            = user;
		this.product         = product;
		this.investingAmount = investingAmount;
	}
	
	// [정적 팩토리 메소드] 총 투자 모집 금액 보다 낮거나 같은 투자금으로만 생성자를 호출할 수 있다.
	public static InvestOrder of(User user, Product product, int investingAmount) {
		if(! product.isInvestableAmount(investingAmount)) {
			throw new ValidationException("Validation error: 현재 투자할 수 없는 금액입니다.");
		}
        return new InvestOrder(user, product, investingAmount);
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + orderId;
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
		InvestOrder other = (InvestOrder) obj;
		if (orderId != other.orderId)
			return false;
		return true;
	}
}
