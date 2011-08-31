package com.liteorm.model.tree;

/**
 * Интерфейс для обхода дерева
 * @author kopernik
 *
 * @param <T>
 */
public interface TreeInspector<T> {
	public void inspect(TreeNode<T> node);
}
