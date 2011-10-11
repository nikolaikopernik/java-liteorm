package com.liteorm;

import java.util.Collection;
import java.util.List;

import com.liteorm.exception.LQueryExecuteException;


/**
 * Основной интерфейс к либе
 * @author kopernik
 *
 */
public interface LiteORM {
	public void insert(Object entity) throws LQueryExecuteException;
	public void update(Object entity) throws LQueryExecuteException;
	public void delete(Object entity) throws LQueryExecuteException;
	public List select(String lql, Object ... args) throws LQueryExecuteException;
	public Object selectFirst(String lql, Object ... args) throws LQueryExecuteException;
	
	public <T> void bulkInsert(Collection<T> entities) throws LQueryExecuteException;
	public <T> void bulkUpdate(Collection<T> entities) throws LQueryExecuteException;
	public <T> void bulkDelete(Collection<T> entities) throws LQueryExecuteException;
	
}
