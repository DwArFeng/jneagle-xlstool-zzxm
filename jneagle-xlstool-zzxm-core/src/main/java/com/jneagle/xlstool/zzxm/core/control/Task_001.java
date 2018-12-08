package com.jneagle.xlstool.zzxm.core.control;

class DisposeTask extends AbstractZZXMTask {

	public DisposeTask(ZZXM zzxm) {
		super(zzxm);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void todo() throws Exception {
		zzxm.controller.dispose();
	}

}
