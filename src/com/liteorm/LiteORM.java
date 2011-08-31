package com.liteorm;

import java.util.Collection;
import java.util.List;

/**
 * Основной интерфейс к либе
 * @author kopernik
 *
 */
public interface LiteORM {
	public void insert(Object entity);
	public void update(Object entity);
	public void delete(Object entity);
	public List select(String lql, Object ... args);
	public Object selectFirst(String lql, Object ... args);
	
	public <T> void bulkInsert(Collection<T> entities);
	public <T> void bulkUpdate(Collection<T> entities);
	public <T> void bulkDelete(Collection<T> entities);
	
}
