package com.liteorm;

public class LLexem {
	public static short TABLE = 0;
	public static short ALIAS = 1;
	public static short COLUMN = 2;
	public static short OPERAND = 3;
	public static short OTHER = 4;
	
	public String value;
	public String table;	
	public short type;
	
	public LLexem(String value, short type) {
		this.value = value;
		this.type = type;
	}
}
