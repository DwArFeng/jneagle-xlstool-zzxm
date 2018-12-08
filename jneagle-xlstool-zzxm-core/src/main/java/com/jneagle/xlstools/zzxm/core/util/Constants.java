package com.jneagle.xlstools.zzxm.core.util;

/**
 * 常量。
 * 
 * <p>
 * 由于该类是只含有静态字段与静态方法的工具类，故该类无法继承与实例化。
 * 
 * @author DwArFeng
 * @since 1.0.0.a
 */
public final class Constants {

	// 在国际化处理器加载之前使用的默认文本字段。
	public static final String DEFAULT_MESSAGE_0 = "读取国际化处理器配置时发生异常，信息如下：";
	public static final String DEFAULT_MESSAGE_1 = "程序启动时发生异常";
	public static final String DEFAULT_MESSAGE_2 = "记录器处理器配置加载完成。";
	public static final String DEFAULT_MESSAGE_3 = "加载国际化处理器配置...";

	/** 默认字段（文本丢失字段） */
	public final static String MISSING_LABEL = "!文本丢失";
	/** 资源列表所在的路径。 */
	public final static String RESOURCE_LIST_PATH = "/com/jneagle/xlstools/zzxm/resources/resource-list.xml";

	private Constants() {
		// 禁止外部实例化。
	}
}
