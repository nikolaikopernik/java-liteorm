package com.liteorm.util;

/**
 * Интерфейс для обхода дерева
 * @author kopernik
 *
 * @param <T>
 */
public interface TreeInspector<T> {
	/**
	 * прохождение узла дерева
	 * @param node текущий нод
	 * @param level текущий левел
	 * @return нужно ли дальше спускаться или перейти к следующему брату?
	 */
	public boolean inspect(TreeNode<T> node, int level);
}
