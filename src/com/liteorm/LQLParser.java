package com.liteorm;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import com.liteorm.exception.LQueryParsingException;
import com.liteorm.model.LClass;
import com.liteorm.model.SqlQueryTables;

/**
 * Работа со строками при парсинге запросов
 * @author kopernik
 *
 */
public class LQLParser {
	private static final Logger logger = Logger.getLogger(LQLParser.class);
	
	/**
	 * Первоначальный парсинг запроса на части
	 * select {...} from {...} where {....} {order by ...}
	 * части   [0]        [1]          [2]        [3]
	 * @param liteql
	 * @return
	 * @throws LQueryParsingException
	 */
	public static String[] parseSelect(String liteql) throws LQueryParsingException{
		String[] parts = new String[4];
		parts[0] = "";
		parts[1] = "";
		parts[2] = "";
		parts[3] = "";
		
		
		int idx1 = liteql.indexOf("select");
		int idx2 = liteql.indexOf("from");
		int idx3 = liteql.indexOf("where");
		int idx4 = liteql.indexOf(" order by ");
		if(idx4<0){
			idx4 = liteql.indexOf(" limit ");
		}
		if(idx2<0){
			throw new LQueryParsingException("Cannot find FROM clause in query.",liteql);
		}
		if(idx1>=0){
			parts[0] = liteql.substring(idx1+6,idx2).trim();
		}
		if(idx3>0){
			parts[1] = liteql.substring(idx2+4,idx3).trim();
			if(idx4<0){
				parts[2] = liteql.substring(idx3+5).trim();
			}else{
				parts[2] = liteql.substring(idx3+5,idx4).trim();
				parts[3] = liteql.substring(idx4+1).trim();
			}
		}else{
			if(idx4<0){
				parts[1] = liteql.substring(idx2+4).trim();
			}else{
				parts[1] = liteql.substring(idx2+4,idx4).trim();
				parts[3] = liteql.substring(idx4+1).trim();
			}
		}
		return parts;
	}
	
	public static String translateWHERE(String where, SqlQueryTables tables) throws LQueryParsingException{
		List<String> fields = getFieldsFromWHERE(where);
		List<String> columns = new LinkedList<String>();
		for(String field:fields ){
			int tck = field.indexOf('.');
			if(tck>=0){
				String alias = field.substring(0,tck);
				String fieldR = field.substring(tck+1,field.length());
				String classname = tables.alias2class(alias);
				if(classname==null){
					throw new LQueryParsingException("Alias "+alias+" do not define.",where);
				}
				LClass clazz = tables.findClass(classname);
				String column = clazz.field2column(fieldR);
				if(column==null){
					throw new LQueryParsingException("Field "+fieldR+" do not define in th class "+clazz.getClazz().getName(),where);
				}
				columns.add(alias+"."+column);
			}else{
				LClass clazz = null;
				String column = null;
				for(LClass c: tables.allClasses()){
					String columntemp = c.field2column(field);
					if(columntemp!=null && clazz==null){
						clazz = c;
						column = columntemp;
					}else if(columntemp!=null && clazz!=null){
						throw new LQueryParsingException("Two classes have same field "+field+". Need alias.",where);
					}					
				}
				if(clazz!=null){
					String alias = tables.class2alias(clazz.getClazz().getSimpleName());
					columns.add(alias+"."+column);
				}else{
					columns.add(field);
				}
			}
		}
		return replaceFields(where, fields, columns);
	}
	
	
	
	/**
	 * Парсим классы в запросе
	 * Возвращаем рузультат в 
	 * 1. наборе классов (т.к. нудна последовательность заполнения)
	 * 2. в списке алиасов
	 * @param from
	 * @param classes
	 * @param class2alias
	 */
	public static void parseFrom(String from, List<String> classes, HashMap<String, String> class2alias){
		String[] tables = from.split(",");
		for(int i=0;i<tables.length;i++){
			String table = tables[i];
			String[] alias = table.trim().split(" ");
			if(alias.length==1){
				classes.add(alias[0]);
			}else{
				class2alias.put(alias[0],alias[1]);
				classes.add(alias[0]);				
			}	
		}
	}
	
	private static List<String> getFieldsFromWHERE(String where){
		String[] expressions = where.split("( and | or | like | not in\\(| not in | not | between | in\\(| in |\\(|\\)|!|<|>|=)+");
		LinkedList<String> fields = new LinkedList<String>();
 		for(int i=0;i<expressions.length;i++){
			String ex = expressions[i];
			if(ex.isEmpty() || Character.isDigit(ex.charAt(0)) || ex.startsWith("?")){
				continue;
			}
			if(!fields.contains(ex)){
				fields.add(ex.trim());
			}
		}
 		return fields;
	}
	
	private static String replaceFields(String where, List<String> fields, List<String> columns){
		Iterator<String> iterf = fields.iterator();
		Iterator<String> iterc = columns.iterator();
		while(iterf.hasNext()){
			String field = iterf.next();
			String column = iterc.next();
			if(!column.equals(field)){
				where = where.replaceAll(field, column);
			}
		}
		return where;
	}
	
}
