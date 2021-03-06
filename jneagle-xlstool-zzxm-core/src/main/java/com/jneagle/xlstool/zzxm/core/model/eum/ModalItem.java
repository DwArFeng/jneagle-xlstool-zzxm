package com.jneagle.xlstool.zzxm.core.model.eum;

import java.util.Objects;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.dwarfeng.dutil.develop.setting.AbstractSettingInfo;
import com.dwarfeng.dutil.develop.setting.SettingEnumItem;
import com.dwarfeng.dutil.develop.setting.SettingInfo;
import com.dwarfeng.dutil.develop.setting.info.BooleanSettingInfo;
import com.dwarfeng.dutil.develop.setting.info.FileSettingInfo;

public enum ModalItem implements SettingEnumItem {

	/** 密码尝试失败的次数。 */
	PERSISTENCE_PASSWORD_FAILED("persistence.password_failed", new PositiveIntegerSettingInfo("0")),
	/** 上一次读取的文件。 */
	PERSISTENCE_LAST_LOAD_FILE("persistence.last-load-file", new FileSettingInfo("attributes")),
	/** 上一次是否读取。 */
	PERSISTENCE_LAST_LOAD_FLAG("persistence.last-load-flag", new BooleanSettingInfo("false")),

	;
	private static final class PositiveIntegerSettingInfo extends AbstractSettingInfo implements SettingInfo {

		private static final int RADIX = 10;
		private String lastCheckedValue = null;
		private Integer lastParsedValue = null;
		private final Lock lock = new ReentrantLock();

		public PositiveIntegerSettingInfo(String defaultValue) throws NullPointerException, IllegalArgumentException {
			super(defaultValue);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public int hashCode() {
			return PositiveIntegerSettingInfo.class.hashCode() * 61 + defaultValue.hashCode() * 17;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (Objects.isNull(obj))
				return false;
			if (!(obj.getClass() == PositiveIntegerSettingInfo.class))
				return false;

			PositiveIntegerSettingInfo that = (PositiveIntegerSettingInfo) obj;
			return Objects.equals(this.defaultValue, that.defaultValue);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public String toString() {
			return "PositiveIntegerSettingInfo [defaultValue=" + defaultValue + "]";
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected boolean isNonNullValid(String value) {
			lock.lock();
			try {
				if (Objects.equals(value, lastCheckedValue))
					return Objects.nonNull(lastParsedValue);

				try {
					lastCheckedValue = value;
					lastParsedValue = Integer.parseInt(value, RADIX);
					if (lastParsedValue < 0) {
						lastParsedValue = null;
						return false;
					}
				} catch (Exception e) {
					lastParsedValue = null;
					return false;
				}
				return true;
			} finally {
				lock.unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected Object parseValidValue(String value) {
			lock.lock();
			try {
				if (Objects.equals(value, lastCheckedValue))
					return lastParsedValue;

				try {
					lastCheckedValue = value;
					lastParsedValue = Integer.parseInt(value, RADIX);
					return lastParsedValue;
				} catch (Exception e) {
					lastCheckedValue = null;
					lastParsedValue = null;
					throw new IllegalStateException();
				}
			} finally {
				lock.unlock();
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		protected String parseNonNullObject(Object object) {
			if (!(object instanceof Integer))
				return null;

			if ((Integer) object < 0)
				return null;

			return Integer.toString((int) object, RADIX);
		}

	}

	private final String name;
	private final SettingInfo settingInfo;

	private ModalItem(String name, SettingInfo settingInfo) {
		this.name = name;
		this.settingInfo = settingInfo;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public SettingInfo getSettingInfo() {
		return settingInfo;
	}

}
