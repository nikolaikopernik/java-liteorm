package com.liteorm.model.tree;

import com.liteorm.model.LClass;
import com.liteorm.model.LRelation;

/**
 * Класс для хранения нужной информации в дереве
 * Просто нужно было хранить несколько полей в одном узле
 * Никакого функционала
 * @author kopernik
 *
 */
public class RelationInfo {
	public LClass clazz;
	public LRelation relation;
	public String alias;
	
	public RelationInfo(LClass clazz, String alias) {
		this.clazz = clazz;
		this.alias = alias;
	}
	
	public RelationInfo(LClass clazz, LRelation relation, String alias) {
		this.clazz = clazz;
		this.relation = relation;
		this.alias = alias;
	}
	
	@Override
	public String toString() {
		return clazz.getName()+"^"+alias+":"+relation;
	}
}
