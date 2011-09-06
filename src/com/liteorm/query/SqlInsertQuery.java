package com.liteorm.query;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.liteorm.exception.LIncorrectDataException;
import com.liteorm.exception.LQueryParsingException;
import com.liteorm.model.LClass;
import com.liteorm.model.LField;
import com.liteorm.model.LFilter;
import com.liteorm.model.LModel;
import com.liteorm.util.LUtils;

public class SqlInsertQuery extends SqlQuery {
	
	public SqlInsertQuery(String sql, Object[] args, LFilter filter){
			super(sql,args,filter);
	}
	
	public SqlInsertQuery(Class clazz, Object entity, LModel model) throws LQueryParsingException, LIncorrectDataException {
		LClass c = model.findClass(clazz);
		if(c == null){
			throw new LQueryParsingException("Cannot find mapping for class.", clazz.getName());
		}
		setSql(c.getInsertSQL());
		setArgs(c.getInsertArgs(entity));
		setTargetClass(c);
	}
	
	public <T> SqlInsertQuery(Class clazz, Collection<T> entity, LModel model) throws LQueryParsingException, LIncorrectDataException {
		LClass c = model.findClass(clazz);
		if(c == null){
			throw new LQueryParsingException("Cannot find mapping for class.", clazz.getName());
		}
		setSql(c.getInsertSQL());
		setArgs(c.getInsertArgs(entity));
		setTargetClass(c);
	}
	
	
	public void setIds(Object entity, int id) throws LQueryParsingException{
		LClass c = getTargetClass();
		if(c.getId()!=null){
			LField idField = c.getId();
			try{
				idField.setValue(id, entity);
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static <T> List<SqlInsertQuery> generateBulkInsertQuery(Class clazz, Collection<T> entities, Integer batchLimit, LModel model) throws LQueryParsingException, LIncorrectDataException{
		LClass c = model.findClass(clazz);
		if(c == null){
			throw new LQueryParsingException("Cannot find mapping for class.",clazz.getName());
		}
		
		int i =0;
		int all = 0;
		int countFields = c.getCountFieldsUPD();
		LFilter filterID = new LFilter(c.getId());
		Object[] args = new Object[Math.min(entities.size(), batchLimit)*countFields];
		Iterator<T> iterator = entities.iterator();
		StringBuilder buff = new StringBuilder(c.getInsertSQL());
		List<SqlInsertQuery> queries = new LinkedList<SqlInsertQuery>();
		while(iterator.hasNext()){
			if(i>0){
				buff.append(',').append(LUtils.getWhats(countFields));
			}
			Object[] temp = c.getInsertArgs(iterator.next());
			for(int x=0;x<countFields;x++){
				args[i*countFields+x] = temp[x];
			}
			i++;
			all++;
			if(i>=batchLimit){
				SqlInsertQuery query = new SqlInsertQuery(buff.toString(),args,filterID);
				query.setTargetClass(c);
				queries.add(query);
				i=0;
				buff = new StringBuilder(c.getInsertSQL());
				args = new Object[Math.min(entities.size()-all, batchLimit)*countFields];
			}
		}
		SqlInsertQuery query = new SqlInsertQuery(buff.toString(), args, filterID);
		queries.add(query);
		return queries;
	}
}
