package com.liteorm.query;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.liteorm.LQLParser;
import com.liteorm.exception.LQueryParsingException;
import com.liteorm.model.LClass;
import com.liteorm.model.LFilter;
import com.liteorm.model.LModel;
import com.liteorm.model.LRelation;
import com.liteorm.util.RelationInfo;
import com.liteorm.util.Tree;
import com.liteorm.util.TreeInspector;
import com.liteorm.util.TreeNode;

public class SqlSelectQuery extends SqlQuery implements TreeInspector<RelationInfo>{
	private static final Logger logger = Logger.getLogger(SqlSelectQuery.class);
	
	private List<LClass> relationClasses = new LinkedList<LClass>();
	private List<LClass> classes = new LinkedList<LClass>();
	private HashMap<String, LClass> classesH = new HashMap<String, LClass>();
	private HashMap<String, String> alias2class = new HashMap<String, String>();
	private HashMap<String, String> class2alias = new HashMap<String, String>();
	private StringBuilder text = null;
	
	
	public SqlSelectQuery(SqlSelectQuery q) {
		setSql(q.getSql());
		setTargetClass(q.getTargetClass());
		setFilter(q.getFilter());
		if(q.getSubQueries()!=null){
			List<SqlSubQuery> list = new ArrayList<SqlSubQuery>(q.getSubQueries().size());
			for(SqlSubQuery sub:q.getSubQueries()){
				list.add(new SqlSubQuery(sub));
			}
			setSubQueries(list);
		}
	}
	
	public SqlSelectQuery(String hql, int n, LModel model) throws LQueryParsingException{
		String[] parts = LQLParser.parseSelect(hql);
		String SELECT = parts[0];
		String FROM = parts[1];
		String WHERE = parts[2];
		String sqlSELECT = "";
		String sqlFROM = "";
		String sqlWHERE = "";

		generateRelationTree(FROM, model);

		// Анализируем поля в WHERE
		if (!WHERE.isEmpty()) {
			sqlWHERE = LQLParser.translateWHERE(WHERE, this);
		}

		if (logger.isDebugEnabled()) {
			logger.debug("LQL:------------------------------------------------");
			logger.debug("LQL:   FROM");
			for (String alias : allAliases()) {
				String table = alias2class(alias);
				LClass clazz = findClass(table);
				logger.debug("LQL     " + table + " " + alias + " -> "
						+ clazz.getTable() + " " + alias);
			}
			if (!WHERE.isEmpty()) {
				logger.debug("LQL:   WHERE");
				logger.debug("LQL:     " + WHERE);
				logger.debug("LQL:     ->");
				logger.debug("LQL:     " + sqlWHERE);
			}
		}

		String limitSql = "";
		if (n > 0) {
			limitSql = " limit " + n;
		}

		LFilter filter = null;
		if (relatedCount() > 0) {
			filter = new LFilter(allClasses(), this);
		} else {
			filter = new LFilter(getTargetClass());
		}

		if (!sqlWHERE.isEmpty()) {
			setSql(String.format("SELECT %s FROM %s WHERE %s%s",
					filter.generateSelectColumns(), generateSQL(),
					sqlWHERE, limitSql));
		} else {
			setSql(String.format("SELECT %s FROM %s%s",
					filter.generateSelectColumns(), generateSQL(),
					limitSql));
		}
		setFilter(filter);
	}
	
	private void generateRelationTree(String fromPart, LModel model) throws LQueryParsingException{
		List<String> classOrder = new LinkedList<String>();
		int i = 50;
		LQLParser.parseFrom(fromPart, classOrder, class2alias);
		
		if(classOrder.size()<1){
			throw new LQueryParsingException("Cannot find target tables in FROM query part.", fromPart);
		}
		
		//Анализируем имена классов из FROM
		int n =0;
		for(String name:classOrder){
			LClass clazz = model.findClassByName(name);
			if(clazz == null){
				throw new LQueryParsingException("Cannot find class "+name+" in liteorm configuration.", fromPart);
			}
			
			classesH.put(name, clazz);
			classes.add(clazz);
			if(n>0){
				relationClasses.add(clazz);
			}
			n++;
			if(!class2alias.containsKey(name)){
				class2alias.put(name,"_a"+i);
				alias2class.put("_a"+i, name);
				i++;
			}else{
				alias2class.put(class2alias.get(name), name);
			}
		}
		setTargetClass(classes.get(0));
		
		Tree<RelationInfo> tree = generateTree(model);
		text = new StringBuilder();
		setSubQueries(new LinkedList<SqlSubQuery>());
		tree.inspect(this);
	}
	
	private Tree<RelationInfo> generateTree(LModel model){
		RelationInfo target = new RelationInfo(getTargetClass(), class2alias.get(getTargetClass().getName()));
		Tree<RelationInfo> tree = new Tree<RelationInfo>(target);
		generateTreeInner(tree.root(), 1, model);
		return tree;
	}
	
	private void generateTreeInner(TreeNode<RelationInfo> node, int level, LModel model){
		if(level<3){
			List<LRelation> list = model.many2one.get(node.data().clazz);
			if(list!=null && !list.isEmpty()){
				for(LRelation rel:list){
					if(relationClasses.contains(rel.getRelClass())){
						RelationInfo info = new RelationInfo(rel.getRelClass(), rel, class2alias.get(rel.getRelClass().getName()));
						generateTreeInner(node.addChild(info), level+1, model);
					}
				}
			}
			list = model.one2many.get(node.data().clazz);
			if(list!=null && !list.isEmpty()){
				for(LRelation rel:list){
					if(relationClasses.contains(rel.getRelClass())){
						RelationInfo info = new RelationInfo(rel.getRelClass(), rel, class2alias.get(rel.getRelClass().getName()));
						generateTreeInner(node.addChild(info), level+1, model);
					}
				}
			}
		}
	}
	
	@Override
	public boolean inspect(TreeNode<RelationInfo> node, int level) {
		LClass clazz = node.data().clazz;
		LRelation relation = node.data().relation;
		String alias = node.data().alias;
		if(relation!=null){
			if(relation.getType() == LRelation.MANY2ONE){
				TreeNode<RelationInfo> parent = node.getParent();
				text.append(" LEFT JOIN ")
					.append(clazz.getTable()).append(' ').append(alias);
				text.append(" ON ");
				text.append(parent.data().alias).append('.').append(relation.getMainField().column)
					.append('=')
					.append(alias).append('.').append(clazz.getId().column);
				return true;
			}else{
				LClass target = clazz;
				Set<LClass> relations = new HashSet<LClass>();
				findRelations(node, relations);  
				SqlSubQuery q = new SqlSubQuery(target, relations, relation.getMainField(), relation.getRelField());
				getSubQueries().add(q);
				return false;
			}	
		}else{
			text.append(clazz.getTable()).append(' ').append(alias);
			return true;
		}
	}
	
	private void findRelations(TreeNode<RelationInfo> node, Set<LClass> array){
		if(!array.contains(node.data().clazz)){
			array.add(node.data().clazz);
		}
		if(node.getChilds()!=null){
			for(TreeNode<RelationInfo> child:node.getChilds()){
				findRelations(child, array);
			}
		}
	}
	
	public String generateSQL(){
		return text.toString();
	}
	
	public String alias2class(String alias){
		return alias2class.get(alias);
	}
	
	public String class2alias(String name){
		return class2alias.get(name);
	}
	
	public LClass findClass(String name){
		return classesH.get(name);
	}
	
	public List<LClass> allClasses(){
		return classes;
	}
	
	public Set<String> allAliases(){
		return alias2class.keySet();
	}
	
	public int relatedCount(){
		return relationClasses.size();
	}
	
	public HashMap<String, String> getAlias2class() {
		return alias2class;
	}
	
	public HashMap<String, String> getClass2alias() {
		return class2alias;
	}
	
	public List<LClass> getClasses() {
		return classes;
	}
	
}
