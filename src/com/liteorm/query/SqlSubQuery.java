package com.liteorm.query;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.liteorm.exception.LInternalException;
import com.liteorm.model.LClass;
import com.liteorm.model.LField;

public class SqlSubQuery {
	private static String alias = "_a50";
	private String hql;
	private Map<Object, Object> objects = new HashMap<Object, Object>();
	private LField base;
	private LField related;
	
	
	public SqlSubQuery(LClass target, Set<LClass> relations, LField base, LField related) {
		// query = from <target> _a50,{<relations>} where _a50.related in (?)
		StringBuffer query = new StringBuffer("from ").append(target.getName()).append(" ").append(alias);
		if(relations!=null && !relations.isEmpty()){
			for(LClass r:relations){
				query.append(',').append(r.getName());
			}
		}
		query.append(" where ").append(alias).append('.').append(related.name).append(" in (?)");
		hql = query.toString();
		query = null;
		
		this.base = base;
		this.related = related;
	}
	
	public void newObject(Object entity) throws LInternalException{
		Object id = base.parentClass.getId().getValue(entity);
		objects.put(id, entity);
	}
	
	public String generateParam(){
		return merge(objects.keySet(), ",");
	}
	
	/**
	 * Полученную выборку объектов нужно назначить нужным владельцам
	 * @param results
	 */
	public void setValues(Collection<Object> results) throws LInternalException{
		if(!results.isEmpty()){
			Map<Object, Set<Object>> map = sort(results);
			for(Entry<Object, Set<Object>> row:map.entrySet()){
				Object id = row.getKey();
				Set<Object> values = row.getValue();
				
				Object main = objects.get(id);
				if(main == null){
					throw new LInternalException("Finded not selected objects " + values);
				}
				base.setValue(values, main);
				for(Object value:values){
					related.setValue(main, value);
				}
			}
		}
	}
	
	private Map<Object, Set<Object>> sort(Collection<Object> entities) throws LInternalException{
		Boolean fake = null;
		
		Map<Object, Set<Object>> map = new HashMap<Object, Set<Object>>();
		for(Object entity:entities){
			Object id = related.getValue(entity);
			if(fake == null){
				fake = id.getClass().equals(base.parentClass.getClazz());
			}
			if(fake){
				// id в фейковой оболочке
				id = base.parentClass.getId().getValue(id);
			}
			Set<Object> values = map.get(id);
			if(values==null){
				values = new HashSet<Object>();
				map.put(id, values);
			}
			values.add(entity);
		}
		return map;
	}
	
	private static String merge(Collection<Object> objects, String delim){
		StringBuilder buff = new StringBuilder();
		int i=0;
		for(Object o:objects){
			if(i>0){
				buff.append(delim);
			}
			buff.append(o.toString());
			i++;
		}
		return buff.toString();
	}
	
	public String getHql() {
		return hql;
	}
}
