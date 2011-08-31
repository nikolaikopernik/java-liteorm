package com.liteorm.model;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;


import com.liteorm.LQLParser;
import com.liteorm.exception.LQueryParsingException;
import com.liteorm.model.tree.RelationInfo;
import com.liteorm.model.tree.Tree;
import com.liteorm.model.tree.TreeInspector;
import com.liteorm.model.tree.TreeNode;

/**
 * Вспомогательный класс для парсинга запросов
 * Строит дерево зависимостей, генерирует на основании него SQL
 * генерирует фильтры
 * @author kopernik
 *
 */
public class SqlQueryTables implements TreeInspector<RelationInfo>{
	private List<LClass> classes = new LinkedList<LClass>();
	private HashMap<String, LClass> classesH = new HashMap<String, LClass>();
	private HashMap<String, String> alias2class = new HashMap<String, String>();
	private HashMap<String, String> class2alias = new HashMap<String, String>();
	private LClass targetClass = null;
	private StringBuilder text = null;
	
	public SqlQueryTables(String fromPart, LModel model) throws LQueryParsingException{
		List<String> classOrder = new LinkedList<String>();
		int i = 50;
		LQLParser.parseFrom(fromPart, classOrder, class2alias);
		
		if(classOrder.size()<1){
			throw new LQueryParsingException("Cannot find target tables in FROM query part.", fromPart);
		}
		
		//Анализируем имена классов из FROM
		for(String name:classOrder){
			LClass clazz = model.findClassByName(name);
			if(clazz == null){
				throw new LQueryParsingException("Cannot find class "+name+" in liteorm configuration.", fromPart);
			}
			classesH.put(name, clazz);
			classes.add(clazz);
			if(!class2alias.containsKey(name)){
				class2alias.put(name,"_a"+i);
				alias2class.put("_a"+i, name);
				i++;
			}else{
				alias2class.put(class2alias.get(name), name);
			}
		}
		targetClass = classes.get(0);
		
		Tree<RelationInfo> tree = generateTree(model);
		text = new StringBuilder();
		tree.inspect(this);
	}
	
	
	private Tree<RelationInfo> generateTree(LModel model){
		RelationInfo target = new RelationInfo(targetClass, class2alias.get(targetClass.getName()));
		Tree<RelationInfo> tree = new Tree<RelationInfo>(target);
		generateTreeInner(tree.root(), 1, model);
		return tree;
	}
	
	private void generateTreeInner(TreeNode<RelationInfo> node, int level, LModel model){
		if(level<3){
			List<LRelation> list = model.many2one.get(node.data().clazz);
			if(list!=null && !list.isEmpty()){
				for(LRelation rel:list){
					if(classes.contains(rel.getRelClass())){
						RelationInfo info = new RelationInfo(rel.getRelClass(), rel, class2alias.get(rel.getRelClass().getName()));
						generateTreeInner(node.addChild(info), level+1, model);
					}
				}
			}
			list = model.one2many.get(node.data().clazz);
			if(list!=null && !list.isEmpty()){
				for(LRelation rel:list){
					if(classes.contains(rel.getRelClass())){
						RelationInfo info = new RelationInfo(rel.getRelClass(), rel, class2alias.get(rel.getRelClass().getName()));
						generateTreeInner(node.addChild(info), level+1, model);
					}
				}
			}
		}
	}
	
	@Override
	public void inspect(TreeNode<RelationInfo> node) {
		LClass clazz = node.data().clazz;
		LRelation relation = node.data().relation;
		String alias = node.data().alias;
		if(relation!=null){
			boolean isMany2one = relation.getMainField().isManyToOne();
			TreeNode<RelationInfo> parent = node.getParent();
			text.append(" LEFT JOIN ")
				.append(clazz.getTable()).append(' ').append(alias);
			text.append(" ON ");
			if(isMany2one){
				text.append(parent.data().alias).append('.').append(relation.getMainField().column)
					.append('=')
					.append(alias).append('.').append(clazz.getId().column);
			}else{
				text.append(parent.data().alias).append('.').append(relation.getMainClass().getId().column)
					.append('=')
					.append(alias).append('.').append(relation.getRelField().column);
			}
		}else{
			text.append(clazz.getTable()).append(' ').append(alias);
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
	
	public LClass getTargetClass() {
		return targetClass;
	}
	
	public int allClassesCount(){
		return classes.size();
	}
}
