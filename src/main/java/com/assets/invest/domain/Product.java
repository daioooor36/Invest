package com.assets.invest.domain;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.DynamicInsert;

import com.assets.invest.enums.Status;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonManagedReference;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@Entity
@ToString(exclude= {"investOrders", "createdAt", "createdBy", "updatedAt", "updatedBy"})
@JsonIgnoreProperties("investOrders")
@DynamicInsert
@NoArgsConstructor
@SequenceGenerator(name="PRODUCT_SEQ_GENERATOR",
				sequenceName="PRODUCT_SEQUENCE",
				initialValue=101,
				allocationSize=1)
@Table(name = "IV_PRODUCT")
public class Product {
	@Id @GeneratedValue(strategy=GenerationType.SEQUENCE, generator="PRODUCT_SEQ_GENERATOR")
	private int productId;
	
	private String productNm;
	
	@Column(columnDefinition="integer default 0")
	private int totalInvestingAmount;
	
	private Date startedAt;
	
	private Date finishedAt;
	
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
	@OneToMany(mappedBy="product", fetch=FetchType.LAZY)
	private List<InvestOrder> investOrders = new ArrayList<>();
	
	@Transient
	private int numberOfInvestors;
	
	@Transient
	private int currentlyInvestedAmount;
	
	@Transient
	private String investmentStatus;

	
	public Product(String productNm, int totalInvestingAmount, Date startedAt, Date finishedAt) {
		super();
		this.productNm = productNm;
		this.totalInvestingAmount = totalInvestingAmount;
		this.startedAt = startedAt;
		this.finishedAt = finishedAt;
	}
	
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + productId;
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		Product other = (Product) obj;
		if (productId != other.getProductId()) {
			return false;
		}
		return true;
	}
	
	// @Transient ?????? setting ?????????
	public void setCurrentStatus(int numberOfInvestors, int currentlyInvestedAmount, Status status) {
		this.numberOfInvestors = numberOfInvestors;
		this.currentlyInvestedAmount = currentlyInvestedAmount;
		this.investmentStatus = status.getStatus();
	}
	
	// ??? ?????? ???????????? ??????(?????????????????? ?????? ????????? ?????? ???????????? ?????????)
	public boolean setTotalInvestingAmountSafely(int modifedAmount) {
		if(this.nowInvestableDate() && this.totalInvestingAmount < modifedAmount) {
			this.totalInvestingAmount = modifedAmount;
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}
	
	// ?????? ??????????????? ?????? ???????????? ??????
	public boolean nowFullAmount() {
		InvestOrders orders = new InvestOrders(this.getInvestOrders());
		int investedAmount = orders.sumInvestedAmountByProduct(this);
		
		if(this.totalInvestingAmount == investedAmount) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}
	
	// ????????? ???????????? ?????? ?????? ???????????? ??????
	public boolean isInvestableAmount(int amount) {
		if(amount <= 0)
			return Boolean.FALSE;
		
		InvestOrders orders = new InvestOrders(this.getInvestOrders());
		int investedAmount = orders.sumInvestedAmountByProduct(this);
		
		if(this.totalInvestingAmount >= (investedAmount + amount)) {
			return Boolean.TRUE;
		} else {
			return Boolean.FALSE;
		}
	}
	
	// ?????? ???????????? ?????????????????? ??????
	public boolean nowInvestableDate() {
		Date startedAt = this.startedAt;
		Date finishedAt = this.finishedAt;
		Date now = new Date();
		
		if((now.compareTo(startedAt) >= 0) && (now.compareTo(finishedAt) <= 0))
			return Boolean.TRUE;
		else
			return Boolean.FALSE;
	}
	
	// Product -> Map ??????
	public Map<String, String> convertToMapForView() {
		Map<String, String> map = new HashMap<>();
		
		map.put("productId"				 , Integer.toString(this.productId));
		map.put("productNm"				 , this.productNm);
		map.put("totalInvestingAmount"	 , Integer.toString(this.totalInvestingAmount));
		map.put("currentlyInvestedAmount", Integer.toString(this.currentlyInvestedAmount));
		map.put("numberOfInvestors"		 , Integer.toString(this.numberOfInvestors));
		map.put("investmentStatus"		 , this.investmentStatus);
		map.put("startedAt"				 , this.startedAt.toString());
		map.put("finishedAt"			 , this.finishedAt.toString());
		
		return map;
	}
}