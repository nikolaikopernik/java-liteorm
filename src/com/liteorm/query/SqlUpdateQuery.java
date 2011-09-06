package com.liteorm.query;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.liteorm.exception.LIncorrectDataException;
import com.liteorm.exception.LInternalException;
import com.liteorm.exception.LQueryParsingException;
import com.liteorm.model.LClass;
import com.liteorm.model.LFilter;
import com.liteorm.model.LModel;
import com.liteorm.util.LUtils;

public class SqlUpdateQuery extends SqlQuery {
	
	public SqlUpdateQuery(String sql, Object[] args, LFilter filter){
		super(sql,args,filter);
}
	
	public SqlUpdateQuery(Class clazz, Object entity, LModel model) throws LQueryParsingException, LIncorrectDataException{
		LClass c = model.findClass(clazz);
		if(c == null){
			throw new LQueryParsingException("Cannot find mapping for class "+ clazz);
		}
		setSql(c.getUpdateSQL());
		setArgs(c.getUpdateArgs(entity));
	}
	
	public static <T> List<SqlQuery> generateBulkUpdateQuery(Class clazz, Collection<T> entities, Integer batchLimit, LModel model) throws LQueryParsingException{
		LClass c = model.findClass(clazz);
		if(c == null){
			throw new LQueryParsingException("Cannot find mapping for class",clazz.getName());
		}
		
		int i =0;
		int all = 0;
		int countFields = c.getCountFields();
		LFilter filterID = new LFilter(c.getId());
		Object[] args = new Object[Math.min(entities.size(), batchLimit)*countFields];
		Iterator<T> iterator = entities.iterator();
		StringBuilder buff = new StringBuilder(c.getReplaceSQL());
		List<SqlQuery> queries = new LinkedList<SqlQuery>();
		while(iterator.hasNext()){
			if(i>0){
				buff.append(',').append(LUtils.getWhats(countFields));
			}
			Object[] temp = c.getFieldArgs(iterator.next());
			for(int x=0;x<countFields;x++){
				args[i*countFields+x] = temp[x];
			}
			i++;
			all++;
			if(i>=batchLimit){
				SqlQuery query = new SqlUpdateQuery(buff.toString(), args, filterID);
				queries.add(query);
				i=0;
				buff = new StringBuilder(c.getInsertSQL());
				args = new Object[Math.min(entities.size()-all, batchLimit)*countFields];
			}
		}
		SqlQuery query = new SqlUpdateQuery(buff.toString(), args, filterID);
		queries.add(query);
		return queries;
	}
}
