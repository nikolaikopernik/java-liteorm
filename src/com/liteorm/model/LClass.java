package com.liteorm.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import com.liteorm.LUtils;
import com.liteorm.exception.LIncorrectDataException;
import com.liteorm.exception.LInternalException;

/**
 * Класс для хранения настроек маппинга классов
 * @author kopernik
 *
 */
public class LClass {
	private Class clazz;
	private String table;
	private String name;
	private LField id;
	private LinkedList<LField> order = new LinkedList<LField>();
	private HashMap<String,LField> name2Fields = new HashMap<String, LField>();
	private HashMap<String,LField> column2Fields = new HashMap<String, LField>();
	private String updateFields;
	private String selectFields;
	private String insertSQL;
	private String updateSQL;
	private String deleteSQL;
	private String replaceSQL;
	private String deleteBulkSQL;
	private Integer countFields = 0;
	private Integer countFieldsUPD = 0;
	
	
	public LClass(Class clazz, String table, List<LField> fields){
		this.clazz = clazz;
		this.table = table;
		this.name = clazz.getSimpleName();
		for(LField f:fields){
			if(f.isID()){
				id = f;
				order.add(0,f);
			}else{
				order.add(f);
				countFieldsUPD++;
			}
			name2Fields.put(f.name, f);
			column2Fields.put(f.column, f);
			if(!f.isOneToMany()){
				countFields++;
			}
			f.parentClass = this;
		}
		generateSimpleSQLS();
	}
	
	public Class getClazz() {
		return clazz;
	}
	
	public String getTable() {
		return table;
	}
	
	public String getName() {
		return name;
	}
	
	public LField getId() {
		return id;
	}
	
	public LField findFieldByName(String name){
		return name2Fields.get(name);
	}
	
	public LField findFieldByColumn(String column){
		return column2Fields.get(column);
	}
	
	public Object[] getFieldArgs(Object entity){
		List<Object> args = new LinkedList<Object>();
		for(LField field:order){
			try{
				args.add(field.getValue(entity));
			}catch (Exception e) {
				e.printStackTrace();
				return null;
			}
		}
		return args.toArray();
	}
	
	public Object[] getInsertArgs(Object entity) throws LIncorrectDataException{
		List<Object> args = new LinkedList<Object>();
		for(LField field:order){
			if(field.isID()){
				// халявим, для инсерта не нужен id
			}else if(field.isProperty()){
				try{
					args.add(field.getValue(entity));
				}catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}else if(field.isManyToOne()){
				LClass lclass = field.relationClass;
				try{
					Object o = field.getValue(entity);
					if(o == null){
						args.add(null);
					}else{
						Object[] ids = lclass.getIDArgs(o);
						// если присоединен объект без id (пустышка)
						// люто бешенно негодуем
						if(ids.length<1 || ids[0]==null){
							throw new LIncorrectDataException("Unpersistant relation object detected: "+lclass);
						}
						args.add(ids[0]);
					}
				}catch (Exception e) {
					e.printStackTrace();
					return null;
				}
			}
		}
		return args.toArray();
	}
	
	public Object[] getUpdateArgs(Object entity) throws LIncorrectDataException{
		List<Object> args = new LinkedList<Object>();
		try{
			Object id = null;
			for(LField field:order){
				if(field.isID()){
					// запрашиваем, но не вставляем
					id = field.getValue(entity);
				}else if(field.isProperty()){
					Object o = field.getValue(entity);
					args.add(o);
				}else if(field.isManyToOne()){
					LClass lclass = field.relationClass;
					Object o = field.getValue(entity);
					if(o == null){
						args.add(null);
					}else{
						Object[] ids = lclass.getIDArgs(o);
						// если присоединен объект без id (пустышка)
						// люто бешенно негодуем
						if(ids.length<1 || ids[0]==null){
							throw new LIncorrectDataException("Unpersistant relation object detected: "+lclass);
						}
						args.add(ids[0]);
					}
				}
			}
			if(id==null){
				throw new LIncorrectDataException("NULL id detected in updated class: "+this);
			}
			args.add(id);
		}catch (LInternalException e) {
			throw new LIncorrectDataException(e.getMessage());
		}
		return args.toArray();
	}
	
	public Object[] getIDArgs(Object entity){
		Object[] args = new Object[1];
		try{
			args[0] = id.getValue(entity);
			return args;
		}catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	public String field2column(String name){
		LField field = name2Fields.get(name);
		if(field!=null){
			return field.column;
		}
		return null;
	}
	
	private void generateSimpleSQLS(){
		//FIELDS
		StringBuilder bufferINS = new StringBuilder();
		StringBuilder bufferSEL = new StringBuilder();
		for(LField f:order){
			if(bufferSEL.length()>0){
				bufferSEL.append(',');
			}
			bufferSEL.append(f.column);
			if(!f.isID() && !f.isOneToMany()){
				if(bufferINS.length()>0){
					bufferINS.append(',');
				}
				bufferINS.append(f.column);
			}
		}
		updateFields = bufferINS.toString();
		selectFields = bufferSEL.toString();
		
		//SQLS
		//insert
		StringBuilder buff = new StringBuilder("INSERT INTO ");
		buff.append(getTable());
		buff.append("(");
		buff.append(updateFields);
		buff.append(")VALUES(");
		int i = 0;
		for(LField field:order){
			if(!field.isID()&&!field.isOneToMany()){
				if(i>0){
					buff.append(',');
				}
				i++;
				buff.append('?');
			}
		}
		buff.append(")");
		insertSQL = buff.toString();
		
		//delete
		deleteSQL = "DELETE FROM "+getTable()+" WHERE "+id.column+"=?";
		deleteBulkSQL = "DELETE FROM "+getTable()+" WHERE "+id.column+" in ";

		//update
		buff = new StringBuilder("UPDATE ");
		buff.append(getTable());
		buff.append(" SET ");
		i = 0;
		for(LField field:order){
			if(!field.isID() && !field.isOneToMany()){
				if(i>0){
					buff.append(',');
				}
				i++;
				buff.append(field.column)
					.append("=?");
			}
		}
		buff.append(" WHERE ");
		buff.append(id.column);
		buff.append("=?");
		updateSQL = buff.toString();
		replaceSQL = String.format("REPLACE INTO %s (%s) VALUES %s", table, selectFields, LUtils.getWhats(countFields));
	}
	
	public String getInsertSQL() {
		return insertSQL;
	}
	
	public String getDeleteSQL() {
		return deleteSQL;
	}
	
	public String getUpdateSQL() {
		return updateSQL;
	}
	
	public String getSelectFields() {
		return selectFields;
	}
	
	public String getUpdateFields() {
		return updateFields;
	}
	
	public List<LField> getOrder() {
		return order;
	}
	
	public Integer getCountFields() {
		return countFields;
	}
	
	public Integer getCountFieldsUPD() {
		return countFieldsUPD;
	}
	
	public String getReplaceSQL() {
		return replaceSQL;
	}
	
	public String getDeleteBulkSQL() {
		return deleteBulkSQL;
	}
	
	
	@Override
	public String toString() {
		return String.format("ORClass(%s,%s,%s)",clazz.getName(),table,name2Fields.values().toString());
	}
}
