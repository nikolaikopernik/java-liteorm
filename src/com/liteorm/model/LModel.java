package com.liteorm.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.liteorm.exception.LConfigurationException;
import com.liteorm.exception.LIncorrectDataException;
import com.liteorm.exception.LInternalException;

/**
 * В модели хранится вся информация о маппинге - о классах, зависимостях, кеш запросов
 * @author kopernik
 *
 */
public class LModel {
	private static final Logger logger = Logger.getLogger(LModel.class);
	
	HashMap<Class, LClass> classHash = new HashMap<Class, LClass>();
	HashMap<String, LClass> nameSimpleHash = new HashMap<String, LClass>();
	HashMap<String, LClass> nameFullHash = new HashMap<String, LClass>();
	
	public HashMap<LClass, List<LRelation>> many2one = new HashMap<LClass, List<LRelation>>();
	public HashMap<LClass, List<LRelation>> one2many = new HashMap<LClass, List<LRelation>>();
	
	/**
	 * загрузка информации о мапнутых классах
	 * Проверка и структуризация полученной информации
	 * @param clazz 
	 * @param fields
	 */
	public void putIntoModel(List<LClass> list) throws LConfigurationException{
		for(LClass clazz:list){
			nameFullHash.put(clazz.getClazz().getName(), clazz);
			nameSimpleHash.put(clazz.getClazz().getSimpleName(), clazz);
			classHash.put(clazz.getClazz(), clazz);
		}
		
		for(LClass clazz:list){
			for(LField field:clazz.getOrder()){
				if(field.isRelation()){
					LClass relClass =  findClassByName(field.relationClassName);
					if(relClass==null){
						throw new LConfigurationException("Cannot find relation class "+field.relationClassName);
					}
					field.relationClass = relClass;
					LField relField = null;
					if(field.isManyToOne()){
						relField = relClass.getId();
					}else{
						relField = relClass.findFieldByColumn(field.relationFieldName);
					}
					if(relField==null){
						throw new LConfigurationException("Cannot find relation field "+field.relationFieldName);
					}
					HashMap<LClass, List<LRelation>> relations = null;
					if(field.isManyToOne()){
						relations = many2one;
					}else{
						relations = one2many;
					}
					
					List<LRelation> rlist = relations.get(clazz);
					if(rlist==null){
						rlist = new LinkedList<LRelation>();
						relations.put(clazz, rlist);
					}
					rlist.add(new LRelation(clazz, field, relClass, relField));
				}
			}
		}
		
	}
	
	
	
	
	
	
	
	
	public Set updateOne2ManyKeyField(Object entity, LRelation relation) throws LInternalException,LIncorrectDataException{
		LClass rclass = relation.getRelClass();
		LClass mclass = relation.getMainClass();
		LField mfield = relation.getMainField();
		LField rfield = relation.getRelField();
		
		/* Если есть ответная зависимость,
		 * то isIDOnly = false, иначе просто
		 * описано поле
		 */
		boolean isIDOnly = true;
		if(classHash.containsKey(rfield.type)){
			isIDOnly = false;
		}
		
		Object key = null;
		if(isIDOnly){
			key = rclass.getId().getValue(entity);
		}else{
			key = entity;
		}
		Object setObj = mfield.getValue(entity);
		if(setObj == null){
			return null;
		}
		try{
			Set set = (Set) setObj;
			for(Object row: set){
				rfield.setValue(key, row);
			}
			return set;
		}catch (ClassCastException e) {
			throw new LIncorrectDataException("Set object not found - class cast exception. "+mclass.getName()+"."+mfield.name);
		}
	}
	
	public LClass findClassByName(String name){
		if(name.contains(".")){
			return nameFullHash.get(name);
		}else{
			return nameSimpleHash.get(name);
		}
	}
	
	public LClass findClass(Class clazz){
		return classHash.get(clazz);
	}
}
