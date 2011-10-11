package com.liteorm.model;

import java.sql.ResultSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;

import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.liteorm.query.SqlSelectQuery;

/**
 * Фильтр для запроса полей и загрузки информаци из колонок в поля
 * @author kopernik
 *
 */
public class LFilter {
	private List<FField> fields = new LinkedList<FField>();
	
	public LFilter(LField f) {
		fields.add(new FField(f));
	}
	
	public LFilter(LClass clazz) {
		for(LField f:clazz.getOrder()){
			if(f.isProperty() || f.isID()){
				fields.add(new FField(f));
				continue;
			}else if(f.isManyToOne()){
				fields.add(new FField(f,false));
			}
		}
	}
	
	public LFilter(List<LClass> classes, SqlSelectQuery tables) {
		LClass target = classes.get(0);
		String alias = tables.class2alias(target.getName());
		for(LField f:target.getOrder()){
			if(f.isProperty() || f.isID()){
				fields.add(new FField(f, alias));
				continue;
			}else if(f.isManyToOne()){
				LClass relation = findClassByName(f.relationClassName, classes);
				if(relation==null){
					fields.add(new FField(f, alias, false));
				}else{
					fields.add(new FField(f, alias, true));
					loadRelationFields(relation, classes, tables);
				}
			}
		}
	}
	
	/**
	 * Генерация объекта по набору фильтров и ответу БД
	 * @param set
	 * @param entity
	 * @throws Exception
	 */
	public void readSimpleResult(SqlRowSet set, Object entity) throws Exception{
		int idx=1;
		int toendrelation = 1000;
		Stack<Object> stack = new Stack<Object>();
		Stack<Integer> stackEnd = new Stack<Integer>();
		Object current = entity;
		for(FField field:fields){
			if(!field.isrelation){
				setValue(set, idx, current, field.field);
				toendrelation--;
			}else{
				Object value = set.getObject(idx);
				LClass rclass = field.field.relationClass;
				Object subobject = null;
				toendrelation--;
				if(value!=null){
					subobject = rclass.getClazz().newInstance();
					field.field.setValue(subobject, current);
					if(!field.loadrelation){
						setValue(set, idx, subobject, rclass.getId());
					}
				}
				if(field.loadrelation){
					stack.push(current);
					stackEnd.push(toendrelation);
					current = subobject;
					toendrelation = rclass.getCountFields();
				}
			}
			idx++;
			while(toendrelation<=0){
				current = stack.pop();
				toendrelation = stackEnd.pop();
			}
		}
	}
	
	
	private void setValue(SqlRowSet set, int idx, Object entity, LField field) throws Exception{
		if(entity!=null){
			Object value = null;
			Class fieldClass  = field.type;
			if(fieldClass.equals(Short.class)){
				value = set.getShort(idx);
			}else if(fieldClass.equals(Integer.class)){
				value = set.getInt(idx);
			}else if(fieldClass.equals(Long.class)){
				value = set.getLong(idx);
			}else if(fieldClass.equals(Boolean.class)){
				value = set.getBoolean(idx);
			}else{
				value = set.getObject(idx);
			}
			if(set.wasNull()){
				value = null;
			}
			field.setValue(value, entity);		
		}
	}
	
	
	private void loadRelationFields(LClass clazz, List<LClass> classes, SqlSelectQuery tables){
		String alias = tables.class2alias(clazz.getName());
		for(LField f:clazz.getOrder()){
			if(f.isProperty() || f.isID()){
				fields.add(new FField(f,alias));
				continue;
			}else if(f.isManyToOne()){
				LClass relation = findClassByName(f.relationClassName, classes);
				if(relation==null){
					fields.add(new FField(f, alias, false));
				}else{
					fields.add(new FField(f, alias, false));
					loadRelationFields(relation, classes, tables);
				}
			}
		}
	}
	
	public String generateSelectColumns(){
		StringBuilder b = new StringBuilder();
		int i=0;
		for(FField field:fields){
			if(i>0){
				b.append(',');
			}
			if(field.alias!=null){
				b.append(field.alias).append('.');
			}
			b.append(field.column);
			i++;
		}
		return b.toString();
	}
	
	private static LClass findClassByName(String name, List<LClass> classes){
		for(LClass clazz:classes){
			if(clazz.getClazz().getName().equals(name)){
				return clazz;
			}
		}
		return null;
	}
	
	private static String findAliasByClass(String name, Map<String,String> aliases){
		for(Entry<String, String> row :aliases.entrySet()){
			if(row.getValue().equals(name)){
				return row.getKey();
			}
		}
		return null;
	}
	
}
