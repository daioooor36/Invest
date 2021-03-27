package com.assets.invest.function;

import java.util.Map;

import com.assets.invest.enums.Message;

@FunctionalInterface
public interface ReturnSettable {
	Map<String, String> returnSet(Message message);
}
