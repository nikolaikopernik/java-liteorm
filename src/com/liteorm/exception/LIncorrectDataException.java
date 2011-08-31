package com.liteorm.exception;

/**
 * Ошибка некорректных данных
 * @author kopernik
 *
 */
public class LIncorrectDataException extends Exception{
	public LIncorrectDataException(String error) {
		super(error);
	}
}
