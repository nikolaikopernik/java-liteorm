package com.liteorm.exception;

public class LQueryExecuteException extends RuntimeException {
	public LQueryExecuteException(Exception e) {
		super(e);
	}
}
