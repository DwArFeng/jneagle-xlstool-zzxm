package com.jneagle.xlstool.zzxm.core.model.eum;

import com.dwarfeng.dutil.basic.str.DefaultName;
import com.dwarfeng.dutil.basic.str.Name;

/**
 * 图片键。
 * 
 * @author DwArFeng
 * @since 1.0.0.a
 */
public enum ImageKey implements Name {

	ARROW_BLUE(new DefaultName("/com/jneagle/xlstools/zzxm/resources/img/arrow-blue.png")), //
	CHECKED_GREEN(new DefaultName("/com/jneagle/xlstools/zzxm/resources/img/checked-green.png")), //
	CHECKED_YELLOW(new DefaultName("/com/jneagle/xlstools/zzxm/resources/img/checked-yellow.png")), //
	CHECKED_RED(new DefaultName("/com/jneagle/xlstools/zzxm/resources/img/checked-red.png")), //
	UNKNOWN_BLUE(new DefaultName("/com/jneagle/xlstools/zzxm/resources/img/unknown-blue.png")),//

	;

	private Name name;

	private ImageKey(Name name) {
		this.name = name;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getName() {
		return name.getName();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String toString() {
		return name.getName();
	}

}
