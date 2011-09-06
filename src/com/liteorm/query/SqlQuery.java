package com.liteorm.query;

import java.util.List;

import com.liteorm.model.LClass;
import com.liteorm.model.LFilter;

/**
 * Класс для хранения всех нужной информации вокруг запроса
 * @author kopernik
 *
 */
public abstract class SqlQuery {
	private String sql;
	private Object[] args;
	private LClass targetClass;
	private LFilter filter;
	private List<SqlSubQuery> subQueries;
	
	public SqlQuery() {
		// TODO Auto-generated constructor stub
	}
	
	public SqlQuery(String sql, Object[] args, LFilter filter){
		setSql(sql);
		setArgs(args);
		setFilter(filter);
	}

	public String getSql() {
		return sql;
	}

	public void setSql(String sql) {
		this.sql = sql;
	}

	public Object[] getArgs() {
		return args;
	}

	public void setArgs(Object[] args) {
		this.args = args;
	}

	public LClass getTargetClass() {
		return targetClass;
	}

	public void setTargetClass(LClass targetClass) {
		this.targetClass = targetClass;
	}

	public LFilter getFilter() {
		return filter;
	}

	public void setFilter(LFilter filter) {
		this.filter = filter;
	}

	public List<SqlSubQuery> getSubQueries() {
		return subQueries;
	}

	public void setSubQueries(List<SqlSubQuery> subQueries) {
		this.subQueries = subQueries;
	}
	
	
	
}
