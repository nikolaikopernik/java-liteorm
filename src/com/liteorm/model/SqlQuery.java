package com.liteorm.model;

import java.util.List;

/**
 * Класс для хранения всех нужной информации вокруг запроса
 * @author kopernik
 *
 */
public class SqlQuery {
	public String sql;
	public Object[] args;
	public LClass targetClass;
	public List<LClass> allClasses;
	public LFilter filter;
	public List<SqlSubQuery> subQueries;
}
