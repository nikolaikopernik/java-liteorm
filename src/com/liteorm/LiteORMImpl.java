package com.liteorm;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import javax.naming.ConfigurationException;
import javax.sql.DataSource;

import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;

import com.liteorm.configuration.LConfigurationParser;
import com.liteorm.exception.LConfigurationException;
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

public class LiteORMImpl implements LiteORM{
	private Logger logger = Logger.getLogger(LiteORMImpl.class);
	
	private Integer BULK_SIZE_LIMIT = 1000;
	private String[] mappingFiles;
	private LModel model = null;
	private DataSource dataSource;
	private SQL sqlHolder;
	
	private HashMap<String, SqlQuery> lqueriesCache = new HashMap<String, SqlQuery>();
	
	
	public LiteORMImpl(){
		// TODO Auto-generated constructor stub
	}
	
	public LiteORMImpl(String[] mappingFiles, DataSource dataSource) throws LConfigurationException{
		this.mappingFiles = mappingFiles;
		this.dataSource = dataSource;
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
	public void insert(Object entity){
		Class clazz = entity.getClass();
		SqlInsertQuery query = null;
		try{
			query = new SqlInsertQuery(clazz, entity, model);
			int id = sqlHolder.insert(query);
			query.setIds(entity, id);
			List<LRelation> relations = model.one2many.get(query.getTargetClass());
			if(relations != null){
				for(LRelation relation:relations){
					Set set = model.updateOne2ManyKeyField(entity, relation);
					bulkInsert(set);
				}
			}
		}catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	@Override
	public  List select(String lql, Object ... objects){
		return selectInner(lql, 0, objects);
	}
	
	@Override
	public Object selectFirst(String lql, Object... objects) {
		List list = selectInner(lql, 1, objects);
		if(list.size()>0){
			return list.get(0);
		}
		return null;
	}
	
	@SuppressWarnings("unchecked")
	private List selectInner(String lql, int n, Object ... objects){
		try{
			SqlSelectQuery query = new SqlSelectQuery(lql, n, model);
			query.setArgs(objects);
			SqlRowSet set = sqlHolder.select(query);
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
			return result;
		}catch (Exception e) {
			logger.error("Error parsing query",e);
		}
		return null;
	}
	
	@Override
	public void delete(Object entity) {
		Class clazz = entity.getClass();
		SqlQuery query = null;
		try{
			LClass targetClass = model.findClass(clazz);
			List<LRelation> relations = model.one2many.get(targetClass);
			if(relations != null){
				for(LRelation relation:relations){
					Object obj = relation.getMainField().getValue(entity);
					if(obj!=null){
						Set set = (Set)obj;
						bulkDelete(set);
					}
				}
			}
			query = new SqlDeleteQuery(targetClass, entity, model);
			int id = sqlHolder.update(query);
		}catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	@Override
	public void update(Object entity) {
		Class clazz = entity.getClass();
		SqlQuery query = null;
		try{
			query = new SqlUpdateQuery(clazz, entity, model);
			int id = sqlHolder.update(query);
		}catch (Exception e) {
			e.printStackTrace();
			return;
		}
	}
	
	@Override
	public <T> void bulkInsert(Collection<T> entities) {
		if(!entities.isEmpty()){
			Iterator<T> iterator = entities.iterator();
			Class clazz = iterator.next().getClass();
			try{
				List<SqlInsertQuery> queries = SqlInsertQuery.generateBulkInsertQuery(clazz, entities, BULK_SIZE_LIMIT, model);
				iterator = entities.iterator();
				for(SqlQuery query:queries){
					SqlRowSet set = sqlHolder.insertBulk(query);
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
						bulkInsert(relset);
					}
				}
			}catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
	@Override
	public <T> void bulkUpdate(Collection<T> entities) {
		if(!entities.isEmpty()){
			Iterator<T> iterator = entities.iterator();
			Class clazz = iterator.next().getClass();
			try{
				List<SqlQuery> queries = SqlUpdateQuery.generateBulkUpdateQuery(clazz, entities, BULK_SIZE_LIMIT, model);
				iterator = entities.iterator();
				for(SqlQuery query:queries){
					int n = sqlHolder.update(query);
				}
			}catch (Exception e) {
				e.printStackTrace();
				return;
			}
		}
	}
	
	@Override
	public <T> void bulkDelete(Collection<T> entities) {
		if(!entities.isEmpty()){
			Iterator<T> iterator = entities.iterator();
			Class clazz = iterator.next().getClass();
			try{
				List<SqlQuery> queries = SqlDeleteQuery.generateBulkDeleteQuery(clazz, entities, BULK_SIZE_LIMIT, model);
				for(SqlQuery query:queries){
					int n = sqlHolder.update(query);
				}
			}catch (Exception e) {
				e.printStackTrace();
				return;
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
