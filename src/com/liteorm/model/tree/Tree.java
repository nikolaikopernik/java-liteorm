package com.liteorm.model.tree;

/**
 * Простое дерево
 * @author kopernik
 *
 * @param <T>
 */
public class Tree<T> {
	private TreeNode<T> root;
	
	public Tree(T rootData) {
		root = new TreeNode<T>(rootData);
	}
	
	public TreeNode<T> root(){
		return root;
	}
	
	public void inspect(TreeInspector<T> inspector){
		inspect(inspector,root);
	}
	
	private void inspect(TreeInspector<T> inspector, TreeNode<T> node){
		inspector.inspect(node);
		if(!node.isLeaf()){
			for(TreeNode<T> child:node.getChilds()){
				inspect(inspector, child);
			}
		}
	}
}
