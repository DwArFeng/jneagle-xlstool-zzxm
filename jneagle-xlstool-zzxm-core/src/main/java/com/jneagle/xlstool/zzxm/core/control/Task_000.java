package com.jneagle.xlstool.zzxm.core.control;

import com.dwarfeng.dutil.develop.backgr.AbstractTask;

abstract class AbstractZZXMTask extends AbstractTask {

	/** ZZXM实例。 */
	protected final ZZXM zzxm;

	protected AbstractZZXMTask(ZZXM zzxm) {
		this.zzxm = zzxm;
	}

}
