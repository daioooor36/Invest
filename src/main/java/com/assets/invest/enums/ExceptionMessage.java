package com.assets.invest.enums;

import lombok.Getter;

@Getter
public enum ExceptionMessage {
	MISSING_REQUEST_HEADER("Header값이 잘못되었습니다."),
	BAD_DATA_TYPE("잘못된 값이 입력되었습니다."),
	NULL_POINTER("조회된 값이 없습니다."),
	EXCEPTION("서버에 오류가 발생하여 요청을 수행할 수 없습니다.");
	
	private final String message;

	ExceptionMessage(String message) {
		this.message = message;
	}
}
