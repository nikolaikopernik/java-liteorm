package com.liteorm.exception;

public class LInternalException extends Exception {
	public LInternalException(String error) {
		super(error);
	}
	
	public LInternalException(String error, String file) {
		super(error+" File: "+file);
	}
}
