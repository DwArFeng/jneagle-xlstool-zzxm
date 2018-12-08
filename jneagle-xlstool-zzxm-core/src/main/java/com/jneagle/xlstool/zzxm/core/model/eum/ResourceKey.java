package com.jneagle.xlstool.zzxm.core.model.eum;

import com.dwarfeng.dutil.basic.str.Name;

/**
 * 资源键枚举。
 * 
 * @author DwArFeng
 * @since 1.0.0.a
 */
public enum ResourceKey implements Name {

	/** 记录器处理器配置文件。 */
	LOGGER_SETTING("logger-setting"),
	/** 国际化处理器配置文件。 */
	I18N_SETTING("i18n-setting"),
	/** 程序配置文件。 */
	CONFIG("config"),
	/** 程序模态文件。 */
	MODAL("modal"),

	;

	private final String name;

	private ResourceKey(String name) {
		this.name = name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return name;
	}

}
