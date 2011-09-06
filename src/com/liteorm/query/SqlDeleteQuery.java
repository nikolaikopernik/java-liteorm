package com.liteorm.query;

import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import com.liteorm.exception.LIncorrectDataException;
import com.liteorm.exception.LQueryParsingException;
import com.liteorm.model.LClass;
import com.liteorm.model.LFilter;
import com.liteorm.model.LModel;

public class SqlDeleteQuery extends SqlQuery {
	
	public SqlDeleteQuery(String sql, Object[] args, LFilter filter){
		super(sql,args,filter);
	}
	
	public SqlDeleteQuery(LClass clazz, Object entity, LModel model) throws LQueryParsingException{
		if(clazz == null){
			throw new LQueryParsingException("Cannot find mapping for class "+ clazz);
		}
		setSql(clazz.getDeleteSQL());
		setArgs(clazz.getIDArgs(entity));
	}
	
	
	public static <T> List<SqlQuery> generateBulkDeleteQuery(Class clazz, Collection<T> entities, Integer batchLimit, LModel model) throws LQueryParsingException, LIncorrectDataException{
		LClass c = model.findClass(clazz);
		if(c == null){
			throw new LQueryParsingException("Cannot find mapping for class",clazz.getName());
		}
		Object[] args = new Object[Math.min(entities.size(), batchLimit)];
		Iterator<T> iterator = entities.iterator();
		StringBuilder buff = new StringBuilder(c.getDeleteBulkSQL()).append("(");
		List<SqlQuery> queries = new LinkedList<SqlQuery>();
		int idx=0;
		int all=0;
		while(iterator.hasNext()){
			Object[] temp = c.getIDArgs(iterator.next());
			if(idx>0){
				buff.append(',');
			}
			buff.append('?');
			if(temp[0] == null){
				throw new LIncorrectDataException("Found null id entity in bulk delete query. Class "+c );
			}
			args[idx] = temp[0];
			idx++;
			all++;
			if(idx >= batchLimit){
				SqlQuery query = new SqlDeleteQuery(buff.append(")").toString(),args,null);
				queries.add(query);
				idx=0;
				buff = new StringBuilder(c.getDeleteBulkSQL()).append("(");
				args = new Object[Math.min(entities.size()-all, batchLimit)];
			}
		}
		SqlQuery query =  new SqlDeleteQuery(buff.append(")").toString(),args,null);
		queries.add(query);
		return queries;
	}
}
