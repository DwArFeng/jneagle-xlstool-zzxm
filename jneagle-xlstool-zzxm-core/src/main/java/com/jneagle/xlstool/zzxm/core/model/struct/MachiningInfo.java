package com.jneagle.xlstool.zzxm.core.model.struct;

import java.util.Map;

/**
 * 机加工信息，不含有项目号。
 * 
 * @author DwArFeng
 * @since 1.0.0.a
 */
public class MachiningInfo {

	private final Map<String, DataContext> properties;

	public MachiningInfo(Map<String, DataContext> properties) {
		this.properties = properties;
	}

	/**
	 * @param key
	 * @return
	 * @see java.util.Map#containsKey(java.lang.Object)
	 */
	public boolean containsKey(Object key) {
		return properties.containsKey(key);
	}

	/**
	 * @param key
	 * @return
	 * @see java.util.Map#get(java.lang.Object)
	 */
	public DataContext get(Object key) {
		return properties.get(key);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return "MachiningInfo [properties=" + properties + "]";
	}

}
