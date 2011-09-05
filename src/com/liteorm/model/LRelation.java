package com.liteorm.model;

/**
 * Связь 2-х классов
 * @author kopernik
 *
 */
public class LRelation {
	public static final int MANY2ONE = 0;
	public static final int ONE2MANY = 1;
	
	private LField mainField;
	private LClass mainClass;
	private LClass relClass;
	private LField relField;
	private int type;
	private boolean delete = false;
	private boolean update = false;
	
	public LRelation(LClass mClass, LField mfield, LClass rclass, LField rField) {
		this.mainField = mfield;
		this.mainClass = mClass;
		this.relClass = rclass;
		this.relField = rField;
		if(mfield.isManyToOne()){
			type = MANY2ONE;
		}else{
			type = ONE2MANY;
		}
	}
	
	public LRelation(LClass mClass, LField mfield, LClass rclass, LField rField, boolean update, boolean delete) {
		this(mClass,mfield,rclass,rField);
		this.update = update;
		this.delete = delete;
	}
	
	public LField getMainField() {
		return mainField;
	}
	
	public LClass getRelClass() {
		return relClass;
	}
	
	public LClass getMainClass() {
		return mainClass;
	}
	
	public LField getRelField() {
		return relField;
	}
	
	public boolean isUpdate(){
		return update;
	}
	
	public boolean isDelete(){
		return delete;
	}
	
	public int getType() {
		return type;
	}
}
