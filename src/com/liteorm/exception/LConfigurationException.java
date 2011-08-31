package com.liteorm.exception;

public class LConfigurationException extends Exception {
	public LConfigurationException(String error) {
		super(error);
	}
	
	public LConfigurationException(String error, String file) {
		super(error+" File: "+file);
	}
}
