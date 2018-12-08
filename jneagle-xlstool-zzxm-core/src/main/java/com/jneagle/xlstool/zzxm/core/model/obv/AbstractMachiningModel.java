package com.jneagle.xlstool.zzxm.core.model.obv;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.WeakHashMap;

import com.jneagle.xlstool.zzxm.core.model.cm.MachiningModel;
import com.jneagle.xlstool.zzxm.core.model.struct.MachiningInfo;

/**
 * 抽象加工模型。
 * 
 * @author DwArFeng
 * @since 1.0.0.a
 */
public abstract class AbstractMachiningModel implements MachiningModel {

	/** 观察器集合 */
	protected final Set<MachiningObverser> obversers;

	/**
	 * 生成一个默认的抽象加工模型。
	 */
	public AbstractMachiningModel() {
		this(Collections.newSetFromMap(new WeakHashMap<>()));
	}

	/**
	 * 生成一个具有指定的侦听器集合的的抽象加工模型。
	 * 
	 * @param obversers
	 *            指定的侦听器集合。
	 * @throws NullPointerException
	 *             入口参数为 <code>null</code>。
	 */
	public AbstractMachiningModel(Set<MachiningObverser> obversers) {
		Objects.requireNonNull(obversers, "入口参数 obversers 不能为 null。");
		this.obversers = obversers;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<MachiningObverser> getObversers() {
		return Collections.unmodifiableSet(obversers);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addObverser(MachiningObverser obverser) throws UnsupportedOperationException {
		if (Objects.isNull(obverser))
			return false;
		return obversers.add(obverser);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeObverser(MachiningObverser obverser) throws UnsupportedOperationException {
		return obversers.remove(obverser);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clearObverser() throws UnsupportedOperationException {
		obversers.clear();
	}

	/**
	 * 通知观察器指定的加工信息被添加。
	 * 
	 * @param xmh
	 *            加工信息的项目号。
	 * @param info
	 *            加工信息。
	 */
	protected void fireMachiningInfoAdded(String xmh, MachiningInfo info) {
		for (MachiningObverser obverser : obversers) {
			if (Objects.nonNull(obverser))
				try {
					obverser.fireMachiningInfoAdded(xmh, info);
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * 通知观察器指定的加工信息被移除。
	 * 
	 * @param xmh
	 *            加工信息的项目号。
	 * @param info
	 *            加工信息。
	 */
	protected void fireMachiningInfoRemoved(String xmh, MachiningInfo info) {
		for (MachiningObverser obverser : obversers) {
			if (Objects.nonNull(obverser))
				try {
					obverser.fireMachiningInfoRemoved(xmh, info);
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}

	/**
	 * 通知模型被清除。
	 */
	protected void fireCleared() {
		for (MachiningObverser obverser : obversers) {
			if (Objects.nonNull(obverser))
				try {
					obverser.fireCleared();
				} catch (Exception e) {
					e.printStackTrace();
				}
		}
	}

}
