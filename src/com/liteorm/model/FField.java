package com.liteorm.model;


/**
 * Поле в фильтре
 * Упрощает операцию выборки полей из результирующего ответа базы
 * @author kopernik
 *
 */
public class FField {
	public String column;
	public String alias;
	public boolean isrelation = false;
	public boolean loadrelation = false;
	public LField field;
	
	public FField(LField field) {
		this.column = field.column;
		this.isrelation = field.isManyToOne();
		this.field = field;
	}
	
	public FField(LField field, String alias) {
		this(field);
		this.alias = alias;
	}
	
	public FField(LField field, boolean b) {
		this(field);
		this.loadrelation = b;
	}
	
	public FField(LField field, String alias, boolean b) {
		this(field,b);
		this.alias = alias;
	}
	
}
