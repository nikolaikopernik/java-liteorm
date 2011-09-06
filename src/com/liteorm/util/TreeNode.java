package com.liteorm.util;

import java.util.LinkedList;
import java.util.List;

/**
 * Узел дерева
 * @author kopernik
 *
 * @param <T>
 */
public class TreeNode<T> {
	private T data;
	private TreeNode<T> parent;
	private List<TreeNode<T>> childs;
	
	public TreeNode(T data) {
		this.data = data;
	}
	
	public TreeNode(T data, TreeNode<T> parent) {
		this.data = data;
		this.parent = parent;
	}
	
	public boolean isLeaf(){
		return childs==null || childs.isEmpty();
	}
	
	public T data(){
		return data;
	}
	
	public List<TreeNode<T>> getChilds() {
		return childs;
	}
	
	public TreeNode<T> addChild(T data){
		TreeNode<T> node = new TreeNode<T>(data, this);
		if(childs == null){
			childs = new LinkedList<TreeNode<T>>();
		}
		childs.add(node);
		return node;
	}
	
	public TreeNode<T> getParent() {
		return parent;
	}
	
	@Override
	public String toString() {
		return data.toString();
	}
}
