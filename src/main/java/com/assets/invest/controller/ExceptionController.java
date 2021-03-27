package com.assets.invest.controller;

import javax.validation.Validation;
import javax.validation.ValidationException;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.assets.invest.enums.ExceptionMessage;

import lombok.extern.slf4j.Slf4j;

@RestControllerAdvice
@Slf4j
public class ExceptionController {
	
	/**
	 * @exception Missing Request Header Exception
	 * @errorResponse 400 Bad Request
	 * @see RequestHeader에 들어가는 Parameter가 누락되었을 때 처리되는 예외
	 */
	@ExceptionHandler(MissingRequestHeaderException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String MissingRequestHeaderExceptionHandler(MissingRequestHeaderException e) {
		log.warn("error", e);
		return "[MissingRequestHeaderException] " + ExceptionMessage.MISSING_REQUEST_HEADER.getMessage() + " : " + e.getHeaderName();
	}
	
	/**
	 * @exception Number Format Exception
	 * @errorResponse 400 Bad Request
	 * @see /invest/{productId}의 파라미터 자료형이 잘못 들어왔을 때 처리되는 예외
	 */
	@ExceptionHandler(NumberFormatException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String NumberFormatExceptionHandler(NumberFormatException e) {
		log.warn("error", e);
		return "[NumberFormatException] " + ExceptionMessage.BAD_DATA_TYPE.getMessage();
	}
	
	/**
	 * @exception Validation Exception
	 * @errorResponse 400 Bad Request
	 * @see InvestOrder에 불가능한 투자금액으로 인스턴스가 생성될 때 처리되는 예외
	 */
	@ExceptionHandler(ValidationException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String ValidationExceptionHandler(ValidationException e) {
		log.warn("error", e);
		return "[ValidationException] " + ExceptionMessage.BAD_DATA_TYPE.getMessage();
	}
	
	/**
	 * @exception NullPointer Exception
	 * @errorResponse 400 Bad Request
	 * @see Select된 결과가 없을 경우 처리되는 예외
	 */
	@ExceptionHandler(NullPointerException.class)
	@ResponseStatus(HttpStatus.BAD_REQUEST)
	public String MethodArgumentTypeMismatchExceptionHandler(NullPointerException e) {
		log.error("error", e);
		return "[NullPointerException] " + ExceptionMessage.NULL_POINTER.getMessage();
	}
	
	/**
	 * @exception Exception
	 * @errorResponse 500 Internal Server Error
	 * @see 전역 예외처리
	 */
	@ExceptionHandler(Exception.class)
	@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
	public String OtherExceptionHandler(Exception e) {
		log.info(e.getClass().getName());
		log.error("error", e);
		return "[OtherException] " + ExceptionMessage.EXCEPTION.getMessage();
	}
}
