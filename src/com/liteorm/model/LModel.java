package com.liteorm.model;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.liteorm.LQLParser;
import com.liteorm.LUtils;
import com.liteorm.exception.LConfigurationException;
import com.liteorm.exception.LIncorrectDataException;
import com.liteorm.exception.LInternalException;
import com.liteorm.exception.LQueryParsingException;

/**
 * В модели хранится вся информация о маппинге - о классах, зависимостях, кеш запросов
 * @author kopernik
 *
 */
public class LModel {
	private static final Logger logger = Logger.getLogger(LModel.class);
	
	HashMap<Class, LClass> classHash = new HashMap<Class, LClass>();
	HashMap<String, LClass> nameSimpleHash = new HashMap<String, LClass>();
	HashMap<String, LClass> nameFullHash = new HashMap<String, LClass>();
	private HashMap<String, SqlQuery> lqueries2sql = new HashMap<String, SqlQuery>();
	public HashMap<LClass, List<LRelation>> many2one = new HashMap<LClass, List<LRelation>>();
	public HashMap<LClass, List<LRelation>> one2many = new HashMap<LClass, List<LRelation>>();
	
	/**
	 * загрузка информации о мапнутых классах
	 * Проверка и структуризация полученной информации
	 * @param clazz 
	 * @param fields
	 */
	public void putIntoModel(List<LClass> list) throws LConfigurationException{
		for(LClass clazz:list){
			nameFullHash.put(clazz.getClazz().getName(), clazz);
			nameSimpleHash.put(clazz.getClazz().getSimpleName(), clazz);
			classHash.put(clazz.getClazz(), clazz);
		}
		
		for(LClass clazz:list){
			for(LField field:clazz.getOrder()){
				if(field.isRelation()){
					LClass relClass = nameFullHash.get(field.relationClassName);
					if(relClass==null){
						throw new LConfigurationException("Cannot find relation class "+field.relationClassName);
					}
					field.relationClass = relClass;
					LField relField = null;
					if(field.isManyToOne()){
						relField = relClass.getId();
					}else{
						relField = relClass.findFieldByColumn(field.relationFieldName);
					}
					if(relField==null){
						throw new LConfigurationException("Cannot find relation field "+field.relationFieldName);
					}
					HashMap<LClass, List<LRelation>> relations = null;
					if(field.isManyToOne()){
						relations = many2one;
					}else{
						relations = one2many;
					}
					
					List<LRelation> rlist = relations.get(clazz);
					if(rlist==null){
						rlist = new LinkedList<LRelation>();
						relations.put(clazz, rlist);
					}
					rlist.add(new LRelation(clazz, field, relClass, relField));
				}
			}
		}
		
	}
	
	public SqlQuery generateInsertQuery(Class clazz, Object entity) throws LQueryParsingException, LIncorrectDataException{
		LClass c = classHash.get(clazz);
		if(c == null){
			throw new LQueryParsingException("Cannot find mapping for class.", clazz.getName());
		}
		SqlQuery query = new SqlQuery();
		query.sql = c.getInsertSQL();
		query.args = c.getInsertArgs(entity);
		query.targetClass = c;
		return query;
	}
	
	public <T> List<SqlQuery> generateBulkInsertQuery(Class clazz, Collection<T> entities, Integer batchLimit) throws LQueryParsingException, LIncorrectDataException{
		LClass c = classHash.get(clazz);
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
		List<SqlQuery> queries = new LinkedList<SqlQuery>();
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
				SqlQuery query = new SqlQuery();
				query.sql = buff.toString();
				query.args = args;
				query.filter = filterID;
				queries.add(query);
				i=0;
				buff = new StringBuilder(c.getInsertSQL());
				args = new Object[Math.min(entities.size()-all, batchLimit)*countFields];
			}
		}
		SqlQuery query = new SqlQuery();
		query.sql = buff.toString();
		query.args = args;
		query.filter = filterID;
		queries.add(query);
		return queries;
	}
	
	public <T> List<SqlQuery> generateBulkUpdateQuery(Class clazz, Collection<T> entities, Integer batchLimit) throws LQueryParsingException{
		LClass c = classHash.get(clazz);
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
				SqlQuery query = new SqlQuery();
				query.sql = buff.toString();
				query.args = args;
				query.filter = filterID;
				queries.add(query);
				i=0;
				buff = new StringBuilder(c.getInsertSQL());
				args = new Object[Math.min(entities.size()-all, batchLimit)*countFields];
			}
		}
		SqlQuery query = new SqlQuery();
		query.sql = buff.toString();
		query.args = args;
		query.filter = filterID;
		queries.add(query);
		return queries;
	}
	
	public <T> List<SqlQuery> generateBulkDeleteQuery(Class clazz, Collection<T> entities, Integer batchLimit) throws LQueryParsingException, LIncorrectDataException{
		LClass c = classHash.get(clazz);
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
				SqlQuery query = new SqlQuery();
				query.sql = buff.append(")").toString();
				query.args = args;
				queries.add(query);
				idx=0;
				buff = new StringBuilder(c.getDeleteBulkSQL()).append("(");
				args = new Object[Math.min(entities.size()-all, batchLimit)];
			}
		}
		SqlQuery query = new SqlQuery();
		query.sql = buff.append(")").toString();
		query.args = args;
		queries.add(query);
		return queries;
	}
	
	
	
	public SqlQuery generateSelectQuery(String sql, int n) throws LQueryParsingException{
		SqlQuery query = lqueries2sql.get(sql);
		if(query == null){
			String[] parts = LQLParser.parseSelect(sql);
			String SELECT = parts[0];
			String FROM = parts[1];
			String WHERE = parts[2];
			String sqlSELECT = "";
			String sqlFROM = "";
			String sqlWHERE = "";
			
			SqlQueryTables sqlTables = new SqlQueryTables(FROM, this);
			
			//Анализируем поля в WHERE
			if(!WHERE.isEmpty()){
				sqlWHERE = LQLParser.translateWHERE(WHERE, sqlTables);
			}
			
			if(logger.isDebugEnabled()){
				logger.debug("LQL:------------------------------------------------");
				logger.debug("LQL:   FROM");
				for(String alias:sqlTables.allAliases()){
					String table = sqlTables.alias2class(alias);
					LClass clazz = sqlTables.findClass(table);
					logger.debug("LQL     "+table+" "+alias+" -> "+clazz.getTable()+" "+alias);
				}				
				if(!WHERE.isEmpty()){
					logger.debug("LQL:   WHERE");
					logger.debug("LQL:     "+WHERE);
					logger.debug("LQL:     ->");
					logger.debug("LQL:     "+sqlWHERE);
				}
			}

			String limitSql = "";
			if(n>0){
				limitSql=" limit "+n;
			}
			
			LFilter filter = null;
			if(sqlTables.allClassesCount()>1){
				filter = new LFilter(sqlTables.allClasses(), sqlTables);
			}else{
				filter = new LFilter(sqlTables.getTargetClass());
			}
			
			query = new SqlQuery();
			query.targetClass = sqlTables.getTargetClass();
			query.allClasses = sqlTables.allClasses();
			if(!sqlWHERE.isEmpty()){
				query.sql = String.format("SELECT %s FROM %s WHERE %s%s", filter.generateSelectColumns(), sqlTables.generateSQL(),sqlWHERE,limitSql);
			}else{
				query.sql = String.format("SELECT %s FROM %s%s", filter.generateSelectColumns(), sqlTables.generateSQL(),limitSql);
			}
			query.filter = filter;
			lqueries2sql.put(sql, query);
		} 
		return query;
	}
	
	public SqlQuery generateUpdateQuery(Class clazz, Object entity) throws LQueryParsingException, LIncorrectDataException{
		LClass c = classHash.get(clazz);
		if(c == null){
			throw new LQueryParsingException("Cannot find mapping for class "+clazz);
		}
		SqlQuery query = new SqlQuery();
		query.sql = c.getUpdateSQL();
		query.args = c.getUpdateArgs(entity);
		return query;
	}
	
	public SqlQuery generateDeleteQuery(Class clazz, Object entity) throws LQueryParsingException{
		LClass c = classHash.get(clazz);
		if(c == null){
			throw new LQueryParsingException("Cannot find mapping for class "+ clazz);
		}
		SqlQuery query = new SqlQuery();
		query.sql = c.getDeleteSQL();
		query.args = c.getIDArgs(entity);
		return query;
	}
	
	public void setIds(Class clazz, Object entity, int id) throws LQueryParsingException{
		LClass c = classHash.get(clazz);
		if(c == null){
			throw new LQueryParsingException("Cannot find mapping for class "+ clazz);
		}
		if(c.getId()!=null){
			LField idField = c.getId();
			try{
				idField.setValue(id, entity);
			}catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	public Set updateOne2ManyKeyField(Object entity, LRelation relation) throws LInternalException,LIncorrectDataException{
		LClass rclass = relation.getRelClass();
		LClass mclass = relation.getMainClass();
		LField mfield = relation.getMainField();
		LField rfield = relation.getRelField();
		
		/* Если есть ответная зависимость,
		 * то isIDOnly = false, иначе просто
		 * описано поле
		 */
		boolean isIDOnly = true;
		if(classHash.containsKey(rfield.type)){
			isIDOnly = false;
		}
		
		Object key = null;
		if(isIDOnly){
			key = rclass.getId().getValue(entity);
		}else{
			key = entity;
		}
		Object setObj = mfield.getValue(entity);
		if(setObj == null){
			return null;
		}
		try{
			Set set = (Set) setObj;
			for(Object row: set){
				rfield.setValue(key, row);
			}
			return set;
		}catch (ClassCastException e) {
			throw new LIncorrectDataException("Set object not found - class cast exception. "+mclass.getName()+"."+mfield.name);
		}
	}
	
	public LClass findClassByName(String name){
		if(name.contains(".")){
			return nameFullHash.get(name);
		}else{
			return nameSimpleHash.get(name);
		}
	}
	
	public LClass findClass(Class clazz){
		return classHash.get(clazz);
	}
}
