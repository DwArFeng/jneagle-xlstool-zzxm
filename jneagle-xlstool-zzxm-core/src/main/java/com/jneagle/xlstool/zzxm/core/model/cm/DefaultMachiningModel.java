package com.jneagle.xlstool.zzxm.core.model.cm;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.jneagle.xlstool.zzxm.core.model.obv.AbstractMachiningModel;
import com.jneagle.xlstool.zzxm.core.model.struct.MachiningInfo;

/**
 * 默认加工模型。
 * 
 * @author DwArFeng
 * @since 1.0.0.a
 */
public class DefaultMachiningModel extends AbstractMachiningModel {

	/**
	 * 集合工厂。
	 * 
	 * @author DwArFeng
	 * @since 1.0.0.a
	 */
	public interface CollectionFactory {

		public Collection<MachiningInfo> newCollection();

	}

	/** 默认的集合工厂。 */
	public final static CollectionFactory DEFAULT_COLLECTION_FACTORY = new CollectionFactory() {
		@Override
		public Collection<MachiningInfo> newCollection() {
			return new LinkedHashSet<>();
		}
	};

	/** 代理映射。 */
	protected final Map<String, Collection<MachiningInfo>> delegate;
	/** 集合工厂。 */
	protected final CollectionFactory collectionFactory;

	/**
	 * 新实例。
	 */
	public DefaultMachiningModel() {
		this(new LinkedHashMap<>(), DEFAULT_COLLECTION_FACTORY);
	}

	/**
	 * 新实例。
	 * 
	 * @param delegate
	 *            指定的代理映射。
	 * @param collectionFactory
	 *            指定的集合工厂。
	 * @throws NullPointerException
	 *             指定的入口参数为 <code> null </code>。
	 */
	public DefaultMachiningModel(Map<String, Collection<MachiningInfo>> delegate, CollectionFactory collectionFactory) {
		Objects.requireNonNull(delegate, "入口参数 delegate 不能为 null。");
		Objects.requireNonNull(collectionFactory, "入口参数 collectionFactory 不能为 null。");

		this.delegate = delegate;
		this.collectionFactory = collectionFactory;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int size() {
		return delegate.size();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int machiningInfoSize() {
		return delegate.entrySet().stream().map(entry -> entry.getValue().size()).reduce(0, (sum, item) -> sum + item);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean isEmpty() {
		return delegate.isEmpty();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean containsXmh(String xmh) {
		return delegate.containsKey(xmh);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Set<String> xmhSet() {
		return Collections.unmodifiableSet(delegate.keySet());
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Collection<MachiningInfo> getMachiningInfos(String xmh) {
		return Collections.unmodifiableCollection(delegate.get(xmh));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean addMachiningInfo(String xmh, MachiningInfo info) {
		boolean aFlag;
		if (delegate.containsKey(xmh)) {
			aFlag = delegate.get(xmh).add(info);
		} else {
			Collection<MachiningInfo> newCollection = collectionFactory.newCollection();
			delegate.put(xmh, newCollection);
			aFlag = newCollection.add(info);
		}
		if (aFlag) {
			fireMachiningInfoAdded(xmh, info);
		}
		return aFlag;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean removeMachiningInfo(String xmh, MachiningInfo info) {
		boolean aFlag;
		if (!delegate.containsKey(xmh)) {
			aFlag = false;
		} else {
			aFlag = delegate.get(xmh).remove(info);
		}
		if (aFlag) {
			fireMachiningInfoRemoved(xmh, info);
		}
		return aFlag;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void clear() {
		delegate.clear();
		fireCleared();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public int hashCode() {
		return delegate.hashCode();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj == this)
			return true;
		if (obj == delegate)
			return true;

		return delegate.equals(obj);
	}

}
