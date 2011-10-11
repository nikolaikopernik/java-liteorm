package com.liteorm;

import java.sql.Connection;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.InitializingBean;

import com.liteorm.configuration.LConfigurationParser;
import com.liteorm.exception.LConfigurationException;
import com.liteorm.exception.LQueryExecuteException;
import com.liteorm.model.LClass;
import com.liteorm.model.LModel;
import com.liteorm.model.LRelation;
import com.liteorm.query.SqlDeleteQuery;
import com.liteorm.query.SqlInsertQuery;
import com.liteorm.query.SqlQuery;
import com.liteorm.query.SqlSelectQuery;
import com.liteorm.query.SqlSubQuery;
import com.liteorm.query.SqlUpdateQuery;
import com.liteorm.sql.SQL;

public class LiteORMImpl implements LiteORM, InitializingBean{
	private Logger logger = Logger.getLogger(LiteORMImpl.class);
	
	private Integer BULK_SIZE_LIMIT = 1000;
	private String[] mappingFiles;
	private LModel model = null;
	private DataSource dataSource;
	private SQL sqlHolder;
	private HashMap<String, SqlSelectQuery> lqueriesCache = new HashMap<String, SqlSelectQuery>();
	
	public LiteORMImpl(){
		// TODO Auto-generated constructor stub
	}
	
	public LiteORMImpl(String[] mappingFiles, DataSource dataSource) throws LConfigurationException{
		this.mappingFiles = mappingFiles;
		this.dataSource = dataSource;
		afterPropertiesSet();
	}
	
	@Override
	public void afterPropertiesSet()throws LConfigurationException {
		configure();
	}
	
	private void configure() throws LConfigurationException{
		logger.info("Configure lite ORM connection...");
		if(dataSource==null){
			throw new LConfigurationException("DataSource field is null.");
		}
		sqlHolder = new SQL(dataSource);
		model = new LModel();
		List<LClass> list = new LinkedList<LClass>();
		for(String file:mappingFiles){
			logger.info("Lite ORM. Add mapping "+file);
			list.addAll(LConfigurationParser.parseConfig(file));
		}
		model.putIntoModel(list);
	}
	
	@Override
	public void insert(Object entity) throws LQueryExecuteException{
		Class clazz = entity.getClass();
		SqlInsertQuery query = null;
		Connection connection = null;
		try{
			query = new SqlInsertQuery(clazz, entity, model);
			connection = sqlHolder.getConnection();
			int id = sqlHolder.insert(query, connection);
			query.setIds(entity, id);
			List<LRelation> relations = model.one2many.get(query.getTargetClass());
			if(relations != null){
				for(LRelation relation:relations){
					Set set = model.updateOne2ManyKeyField(entity, relation);
					bulkInsert(set, connection);
				}
			}
		}catch (Exception e) {
			throw new LQueryExecuteException(e);
		}finally{
			sqlHolder.releaseConnection(connection);
		}
	}
	
	@Override
	public  List select(String lql, Object ... objects)  throws LQueryExecuteException{
		return selectInner(lql, 0, objects);
	}
	
	@Override
	public Object selectFirst(String lql, Object... objects) throws LQueryExecuteException{
		List list = selectInner(lql, 1, objects);
		if(list.size()>0){
			return list.get(0);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private List selectInner(String lql, int n, Object ... objects) throws LQueryExecuteException{
		boolean exist = true;
		SqlSelectQuery query = findInCache(lql);
		Connection connection = null;
		try{
			if(query == null){
				exist = false;
				query = new SqlSelectQuery(lql, n, model);
			}
			query.setArgs(objects);
			connection = sqlHolder.getConnection();
			ResultSet set = sqlHolder.select(query, connection);
			List result = new ArrayList();
			while(set.next()){
				Object o = query.getTargetClass().getClazz().newInstance();
				query.getFilter().readSimpleResult(set, o);
				result.add(o);
			}

			if(query.getSubQueries()!=null && !query.getSubQueries().isEmpty()){
				for(Object o:result){
					for(SqlSubQuery q:query.getSubQueries()){
						q.newObject(o);
					}
				}
				for(SqlSubQuery q:query.getSubQueries()){
					List subResult = select(q.getHql(), q.generateParam());
					q.setValues(subResult);
				}
			}
			if(!exist){
				putInCache(lql, query);
			}
			return result;
		}catch (Exception e) {
			throw new LQueryExecuteException(e);
		}finally{
			sqlHolder.releaseConnection(connection);
		}
	}
	
	@Override
	public void delete(Object entity) throws LQueryExecuteException {
		Class clazz = entity.getClass();
		SqlQuery query = null;
		Connection connection = null;
		try{
			LClass targetClass = model.findClass(clazz);
			List<LRelation> relations = model.one2many.get(targetClass);
			connection = sqlHolder.getConnection();
			if(relations != null){
				for(LRelation relation:relations){
					Object obj = relation.getMainField().getValue(entity);
					if(obj!=null){
						Set set = (Set)obj;
						bulkDelete(set, connection);
					}
				}
			}
			query = new SqlDeleteQuery(targetClass, entity, model);
			int id = sqlHolder.update(query, connection);
		}catch (Exception e) {
			throw new LQueryExecuteException(e);
		}finally{
			sqlHolder.releaseConnection(connection);
		}
	}
	
	@Override
	public void update(Object entity) throws LQueryExecuteException {
		Class clazz = entity.getClass();
		SqlQuery query = null;
		Connection connection = null;
		try{
			query = new SqlUpdateQuery(clazz, entity, model);
			connection = sqlHolder.getConnection();
			int id = sqlHolder.update(query, connection);
		}catch (Exception e) {
			throw new LQueryExecuteException(e);
		}finally{
			sqlHolder.releaseConnection(connection);
		}
	}
	
	@Override
	public <T> void bulkInsert(Collection<T> entities) throws LQueryExecuteException {
		bulkInsert(entities, null);
	}
	
	private <T> void bulkInsert(Collection<T> entities, Connection conn) throws LQueryExecuteException {
		if(!entities.isEmpty()){
			Iterator<T> iterator = entities.iterator();
			Class clazz = iterator.next().getClass();
			Connection connection = conn;
			try{
				List<SqlInsertQuery> queries = SqlInsertQuery.generateBulkInsertQuery(clazz, entities, BULK_SIZE_LIMIT, model);
				iterator = entities.iterator();
				if(connection==null){
					connection = sqlHolder.getConnection();
				}
				for(SqlQuery query:queries){
					ResultSet set = sqlHolder.insertBulk(query, connection);
					int i =0;
					while(iterator.hasNext() && i<BULK_SIZE_LIMIT && set.next()){
						T o = iterator.next();
						query.getFilter().readSimpleResult(set, o);
					}
				}
				// позаботимся о сохранении one2many связей
				List<LRelation> relations = model.one2many.get(model.findClass(clazz));
				if(relations != null){
					for(LRelation relation:relations){
						Set relset = new HashSet(); 
						for(T entity:entities){
							relset.addAll(model.updateOne2ManyKeyField(entity, relation));
						}
						bulkInsert(relset, connection);
					}
				}
			}catch (Exception e) {
				throw new LQueryExecuteException(e);
			}finally{
				sqlHolder.releaseConnection(connection);
			}
		}

	}
	
	private SqlSelectQuery findInCache(String lql){
		synchronized (lqueriesCache) {
			SqlSelectQuery query = lqueriesCache.get(lql);
			if(query!=null){
				return new SqlSelectQuery(query);
			}	
		}
		return null;
	}
	
	private void putInCache(String lql, SqlSelectQuery q){
		synchronized (lqueriesCache) {
			lqueriesCache.put(lql, q);
		}
	}
	
	@Override
	public <T> void bulkUpdate(Collection<T> entities) throws LQueryExecuteException {
		if(!entities.isEmpty()){
			Iterator<T> iterator = entities.iterator();
			Class clazz = iterator.next().getClass();
			Connection connection = null;
			try{
				List<SqlQuery> queries = SqlUpdateQuery.generateBulkUpdateQuery(clazz, entities, BULK_SIZE_LIMIT, model);
				iterator = entities.iterator();
				connection = sqlHolder.getConnection();
				for(SqlQuery query:queries){
					int n = sqlHolder.update(query,connection);
				}
			}catch (Exception e) {
				throw new LQueryExecuteException(e);
			}finally{
				sqlHolder.releaseConnection(connection);
			}
		}
	}
	
	@Override
	public <T> void bulkDelete(Collection<T> entities) throws LQueryExecuteException {
		bulkDelete(entities, null);
	}
	
	private <T> void bulkDelete(Collection<T> entities, Connection conn) throws LQueryExecuteException {
		if(!entities.isEmpty()){
			Iterator<T> iterator = entities.iterator();
			Class clazz = iterator.next().getClass();
			Connection connection = conn;
			try{
				List<SqlQuery> queries = SqlDeleteQuery.generateBulkDeleteQuery(clazz, entities, BULK_SIZE_LIMIT, model);
				if(connection==null){
					connection = sqlHolder.getConnection();
				}
				for(SqlQuery query:queries){
					int n = sqlHolder.update(query,connection);
				}
			}catch (Exception e) {
				throw new LQueryExecuteException(e);
			}finally{
				sqlHolder.releaseConnection(connection);
			}
		}
	}
	
	
	public void setMappingFiles(String[] mappingFiles) {
		this.mappingFiles = mappingFiles;
	}

	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
}
