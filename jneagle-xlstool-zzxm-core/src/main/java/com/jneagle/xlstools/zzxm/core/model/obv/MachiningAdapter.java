package com.jneagle.xlstools.zzxm.core.model.obv;

import com.jneagle.xlstools.zzxm.core.model.struct.MachiningInfo;

public abstract class MachiningAdapter implements MachiningObverser {

	@Override
	public void fireMachiningInfoAdded(String xmh, MachiningInfo info) {
	}

	@Override
	public void fireMachiningInfoRemoved(String xmh, MachiningInfo info) {
	}

	@Override
	public void fireCleared() {
	}

}
