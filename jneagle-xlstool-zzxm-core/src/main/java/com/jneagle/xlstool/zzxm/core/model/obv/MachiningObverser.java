package com.jneagle.xlstool.zzxm.core.model.obv;

import com.dwarfeng.dutil.basic.prog.Obverser;
import com.jneagle.xlstool.zzxm.core.model.struct.MachiningInfo;

/**
 * 加工观察器。
 * 
 * @author DwArFeng
 * @since 1.0.0.a
 */
public interface MachiningObverser extends Obverser {

	/**
	 * 通知观察器指定的加工信息被添加。
	 * 
	 * @param xmh
	 *            加工信息的项目号。
	 * @param info
	 *            加工信息。
	 */
	public void fireMachiningInfoAdded(String xmh, MachiningInfo info);

	/**
	 * 通知观察器指定的加工信息被移除。
	 * 
	 * @param xmh
	 *            加工信息的项目号。
	 * @param info
	 *            加工信息。
	 */
	public void fireMachiningInfoRemoved(String xmh, MachiningInfo info);

	/**
	 * 通知模型被清除。
	 */
	public void fireCleared();

}
