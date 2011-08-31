package com.liteorm.configuration;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.liteorm.exception.LConfigurationException;
import com.liteorm.model.LClass;
import com.liteorm.model.LField;

/**
 * Парсинг конфигов хибрнейта и генерация полезной инфы о мапинге
 * 
 * @author kopernik
 */
public class LConfigurationParser {
	private static final Logger logger = Logger.getLogger(LConfigurationParser.class);
	private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	
	public static List<LClass> parseConfig(String file) throws LConfigurationException{
		Document dom = null;
		try{
			DocumentBuilder builder = factory.newDocumentBuilder();
			dom = builder.parse(file);
		}catch (Exception e) {
			throw new LConfigurationException(e.getMessage(), file);
		}
		Element mapping = (Element)dom.getDocumentElement();
		String pack = mapping.getAttribute("package");
		NodeList classes = dom.getElementsByTagName("class");
		List<LClass> result = new LinkedList<LClass>();
		for(int i=0;i<classes.getLength();i++){
			Element row = (Element)classes.item(i);
			String name = row.getAttribute("name");
			String table = row.getAttribute("table");
			String fullnameClass = pack+'.'+name;
			Class clazz = null;
			try{
				clazz = Class.forName(fullnameClass);
			}catch (ClassNotFoundException e) {
				String error = String.format("Cannot find class %s from configuration file.",fullnameClass);
				throw new LConfigurationException(error, file);
			}
			Method[] methods = clazz.getMethods();
			List<LField> fields = new ArrayList<LField>();
			NodeList params = row.getChildNodes();
			try{
				for(int x=0;x<params.getLength();x++){
					if(params.item(x) instanceof Element){
						Element param = (Element)params.item(x);
						LField field = parseField(methods, param);
						if(field!=null){
							fields.add(field);
						}
					}
				}
				result.add(new LClass(clazz, table, fields));
			}catch (LConfigurationException e) {
				throw new LConfigurationException(e.getMessage(), file);
			}			
		}
		return result;
	}
	
	private static LField parseField(Method[] methods, Element element) throws LConfigurationException{
		String pname = element.getAttribute("name");
		String pcolumn = element.getAttribute("column");
		String ptype = element.getAttribute("type");
		String pclass = element.getAttribute("class");
		String tag = element.getNodeName();
		String key = null;
		
		String setterName = LField.getSetterName(pname);
		String getterName = LField.getGetterName(pname);
		Method setter = null;
		Method getter = null;
		for(Method method:methods){
			if(method.getName().equals(setterName)){
				setter = method;
			}
			if(method.getName().equals(getterName)){
				getter = method;
			}
		}
		if(setter==null){
			throw new LConfigurationException("Cannot find setter for property "+pname);
		}
		if(getter==null){
			throw new LConfigurationException("Cannot find getter for property "+pname);
		}
		short ltype = 0;
		if(tag.equalsIgnoreCase("id")){
			ltype = LField.TYPE_ID;
		}else if (tag.equalsIgnoreCase("property")){
			ltype = LField.TYPE_PROPERTY;
		}else if(tag.equalsIgnoreCase("many-to-one")){
			ltype = LField.TYPE_MANY_TO_ONE;
		}else if(tag.equalsIgnoreCase("set")){
			ltype = LField.TYPE_ONE_TO_MANY;
			NodeList setList = element.getChildNodes();
			for(int i=0;i<setList.getLength();i++){
				if(setList.item(i) instanceof Element){
					Element param = (Element)setList.item(i);
					if("key".equals(param.getNodeName())){
						key = param.getAttribute("column");
					}else if("one-to-many".equals(param.getNodeName())){
						pclass = param.getAttribute("class");
					}
				}
			}
		}else{
			logger.warn("Skip tag "+tag+" in configuration file");
			return null;
		}
		LField field = new LField(pname,
									(!pcolumn.isEmpty())?pcolumn:pname,
									ltype,
									(!ptype.isEmpty())?findClass(ptype):getter.getReturnType(),
									setter,
									getter);
		if(!pclass.isEmpty()){
			field.relationClassName = pclass;
		}
		if(key != null){
			field.relationFieldName = key;
		}
		return field;
	}
	
	private static Class findClass(String name) throws LConfigurationException{
		if("string".equals(name)){
			return String.class;
		}else if("integer".equals(name)){
			return Integer.class;
		}else if("short".equals(name)){
			return Short.class;
		}else if("float".equals(name)){
			return Float.class;
		}else if("date".equals(name)){
			return Date.class;
		}else if("boolean".equals(name)){
			return Boolean.class;
		}else{
			throw new LConfigurationException("Cannot parse type "+name);
		}
		
	}
}
