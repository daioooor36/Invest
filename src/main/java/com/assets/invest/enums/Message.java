package com.assets.invest.enums;

import lombok.Getter;

@Getter
public enum Message {
	COMPLETE("Y", "1000", "정상적으로 투자되었습니다."),
	COMPLETELY_SOLD_OUT("SO", "1001", "정상적으로 투자되었습니다."),
	NOT_INVESTABLE_PRODUCT("N", "-2000", "현재 투자가능한 상품이 없습니다."),
	NOT_EXISTS_USER_ID("N", "-2001", "존재하지 않는 유저입니다."),
	MISMATCHED_PRODUCT_ID("N", "-2002", "존재하지 않거나 모집이 종료된 상품입니다."),
	OVERFLOW_INVESTABLE_AMOUNT("N", "-2003", ""),
	ERROR_IN_INVESTING_PROCESS("N", "-2004", "투자과정에서 오류가 발생하였습니다."),
	INVESTING_AMOUNT_LESS_THAN_ZERO("N", "-2005", "투자금액은 1원 이상이어야 합니다."),
	NO_INVESTED_EVER("N", "-2006", "투자하신 이력이 없습니다."),
	WAS_SOLD_OUT("N", "-2007", "상품이 마감되었습니다.");
	
	private final String messageResult;
	private final String messageCode;
	private final String messageContent;
	
	Message(String messageResult, String messageCode, String messageContent) {
		this.messageResult = messageResult;
		this.messageCode = messageCode;
		this.messageContent = messageContent;
	}
}
