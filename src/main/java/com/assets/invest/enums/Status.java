package com.assets.invest.enums;

import lombok.Getter;

@Getter
public enum Status {
	INVESTING("모집 중"),
	COMPLETED("모집완료");
	
	private final String status;

	Status(String status) {
		this.status = status;
	}
}
