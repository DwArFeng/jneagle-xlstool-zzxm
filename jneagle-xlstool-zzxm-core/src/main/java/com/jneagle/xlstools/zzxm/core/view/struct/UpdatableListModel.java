package com.jneagle.xlstools.zzxm.core.view.struct;

import com.dwarfeng.dutil.basic.gui.swing.MuaListModel;

/**
 * 可更新某一栏数据的列表模型。
 * 
 * @author DwArFeng
 * @since 1.1.0.a
 */
public class UpdatableListModel<E> extends MuaListModel<E> {

	private static final long serialVersionUID = -1757306092833980970L;

	/**
	 * 更新指定行号的数据。
	 * 
	 * @param index
	 *            指定的行号。
	 */
	public void update(int index) {
		fireContentsChanged(this, index, index);
	}

	/**
	 * 更新所有的行号。
	 */
	public void updateAll() {
		fireContentsChanged(this, 0, size() - 1);
	}

}
