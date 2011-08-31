package com.liteorm.exception;

public class LQueryParsingException extends Exception {
	public LQueryParsingException(String error) {
		super(error);
	}
	
	public LQueryParsingException(String error, String query) {
		super(error+" Query: "+query);
	}
}
