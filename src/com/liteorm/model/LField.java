package com.liteorm.model;

import java.lang.reflect.Method;

import com.liteorm.exception.LInternalException;

/**
 * Настройки меппинга полей
 * @author kopernik
 *
 */
public class LField {
	public static final short TYPE_ID = 0;
	public static final short TYPE_PROPERTY = 1;
	public static final short TYPE_MANY_TO_ONE = 2;
	public static final short TYPE_ONE_TO_MANY = 3;
	
	public String name;
	public String column;
	public short ltype = TYPE_PROPERTY;
	public Class type;
	private Method setter;
	private Method getter;
	public LClass parentClass;
	public String relationClassName;
	public String relationFieldName;
	public LClass relationClass;

	public LField() {
		// TODO Auto-generated constructor stub
	}
	
	public LField(String name, String column, short ltype, Class type, Method setter, Method getter) {
		this.name = name;
		this.column = column;
		this.type = type;
		this.ltype = ltype;
		this.setter = setter;
		this.getter = getter;
	}
	
	public LField(String name, String column, short ltype, Class type, Method setter, Method getter, String relationclass) {
		this(name, column, ltype, type, setter, getter);
		this.relationClassName = relationclass;
	}
	
	
	@Override
	public String toString() {
		return name+"-"+column;
	}
	
	public static String getSetterName(String name){
		return "set"+name.substring(0,1).toUpperCase()+name.substring(1);
	}
	
	public static String getGetterName(String name){
		return "get"+name.substring(0,1).toUpperCase()+name.substring(1);
	}
	
	public boolean isID(){
		return ltype == TYPE_ID;
	}
	
	public boolean isManyToOne(){
		return ltype==TYPE_MANY_TO_ONE;
	}
	
	public boolean isOneToMany(){
		return ltype==TYPE_ONE_TO_MANY;
	}
	
	public boolean isProperty(){
		return ltype == TYPE_PROPERTY;
	}
	
	public boolean isRelation(){
		return ltype == TYPE_ONE_TO_MANY || ltype == TYPE_MANY_TO_ONE;
	}
	
	public void setValue(Object value, Object entity) throws LInternalException{
		try{
			setter.invoke(entity, value);
		}catch(Exception e){
			e.printStackTrace();
			throw new LInternalException("Error invoking setter for "+parentClass.getName()+"."+name);
		}
	}
	
	public Object getValue(Object entity) throws LInternalException{
		try{
			return getter.invoke(entity);
		}catch (Exception e) {
			throw new LInternalException("Error invoking getter for "+parentClass.getName()+"."+name);
		}
	}
}
