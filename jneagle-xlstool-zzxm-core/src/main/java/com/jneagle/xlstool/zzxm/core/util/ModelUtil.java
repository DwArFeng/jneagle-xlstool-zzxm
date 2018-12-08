package com.jneagle.xlstool.zzxm.core.util;

import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import com.jneagle.xlstool.zzxm.core.model.cm.MachiningModel;
import com.jneagle.xlstool.zzxm.core.model.cm.SyncMachiningModel;
import com.jneagle.xlstool.zzxm.core.model.obv.MachiningObverser;
import com.jneagle.xlstool.zzxm.core.model.struct.MachiningInfo;

/**
 * 模型工具。
 * 
 * <p>
 * 由于该类是只含有静态方法的工具类，故该类无法继承与实例化。
 * 
 * @author DwArFeng
 * @since 1.0.0.a
 */
public final class ModelUtil {

	public static SyncMachiningModel syncMachiningModel(MachiningModel machiningModel) throws NullPointerException {
		Objects.requireNonNull(machiningModel, "入口参数 machiningModel 不能为 null。");
		return new SyncMachiningModelImpl(machiningModel);
	}

	private static final class SyncMachiningModelImpl implements SyncMachiningModel {

		private final MachiningModel delegate;
		private final ReadWriteLock lock = new ReentrantReadWriteLock();

		public SyncMachiningModelImpl(MachiningModel delegate) {
			this.delegate = delegate;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int size() {
			lock.readLock().lock();
			try {
				return delegate.size();
			} finally {
				lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int machiningInfoSize() {
			lock.readLock().lock();
			try {
				return delegate.machiningInfoSize();
			} finally {
				lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isEmpty() {
			lock.readLock().lock();
			try {
				return delegate.isEmpty();
			} finally {
				lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean containsXmh(String xmh) {
			lock.readLock().lock();
			try {
				return delegate.containsXmh(xmh);
			} finally {
				lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Set<String> xmhSet() {
			lock.readLock().lock();
			try {
				return delegate.xmhSet();
			} finally {
				lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Collection<MachiningInfo> getMachiningInfos(String xmh) {
			lock.readLock().lock();
			try {
				return delegate.getMachiningInfos(xmh);
			} finally {
				lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean addMachiningInfo(String xmh, MachiningInfo info) throws UnsupportedOperationException {
			lock.writeLock().lock();
			try {
				return delegate.addMachiningInfo(xmh, info);
			} finally {
				lock.writeLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean removeMachiningInfo(String xmh, MachiningInfo info) throws UnsupportedOperationException {
			lock.writeLock().lock();
			try {
				return delegate.removeMachiningInfo(xmh, info);
			} finally {
				lock.writeLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void clear() throws UnsupportedOperationException {
			lock.writeLock().lock();
			try {
				delegate.clear();
			} finally {
				lock.writeLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Set<MachiningObverser> getObversers() {
			lock.readLock().lock();
			try {
				return delegate.getObversers();
			} finally {
				lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean addObverser(MachiningObverser obverser) throws UnsupportedOperationException {
			lock.writeLock().lock();
			try {
				return delegate.addObverser(obverser);
			} finally {
				lock.writeLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean removeObverser(MachiningObverser obverser) throws UnsupportedOperationException {
			lock.writeLock().lock();
			try {
				return delegate.removeObverser(obverser);
			} finally {
				lock.writeLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void clearObverser() throws UnsupportedOperationException {
			lock.writeLock().lock();
			try {
				delegate.clearObverser();
			} finally {
				lock.writeLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public ReadWriteLock getLock() {
			return lock;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			lock.readLock().lock();
			try {
				return delegate.hashCode();
			} finally {
				lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			lock.readLock().lock();
			try {
				if (obj == this)
					return true;
				if (obj == delegate)
					return true;

				return delegate.equals(obj);
			} finally {
				lock.readLock().unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			lock.readLock().lock();
			try {
				return delegate.toString();
			} finally {
				lock.readLock().unlock();
			}
		}
	}

	private ModelUtil() {
		// 禁止外部实例化。
	}
}
