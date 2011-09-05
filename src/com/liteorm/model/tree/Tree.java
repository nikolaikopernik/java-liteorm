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
		inspect(inspector,root,0);
	}
	
	private void inspect(TreeInspector<T> inspector, TreeNode<T> node, int level){
		boolean go = inspector.inspect(node, level);
		if(go && !node.isLeaf()){
			for(TreeNode<T> child:node.getChilds()){
				inspect(inspector, child, level+1);
			}
		}
	}
}
