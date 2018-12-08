package com.jneagle.xlstools.zzxm.core.control;

import java.util.Collection;

class ChangeLoadPasswordTask extends AbstractZZXMTask {

	private final String password;

	public ChangeLoadPasswordTask(ZZXM zzxm, String password) {
		super(zzxm);
		this.password = password;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void todo() throws Exception {
		zzxm.excelLoadPasswordModel.set(password);
	}

}

class ChangeExportPasswordTask extends AbstractZZXMTask {

	private final String password;

	public ChangeExportPasswordTask(ZZXM zzxm, String password) {
		super(zzxm);
		this.password = password;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void todo() throws Exception {
		zzxm.excelExportPasswordModel.set(password);
	}

}

class CommitSelectedXmhTask extends AbstractZZXMTask {

	private final String xmh;

	public CommitSelectedXmhTask(ZZXM zzxm, String xmh) {
		super(zzxm);
		this.xmh = xmh;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void todo() throws Exception {
		zzxm.xmhSelectModel.add(xmh);
	}

}

class CommitAllXmhTask extends AbstractZZXMTask {

	public CommitAllXmhTask(ZZXM zzxm) {
		super(zzxm);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void todo() throws Exception {
		zzxm.xmhSelectModel.addAll(zzxm.machiningModel.xmhSet());
	}

}

class UncommitAllXmhTask extends AbstractZZXMTask {

	public UncommitAllXmhTask(ZZXM zzxm) {
		super(zzxm);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void todo() throws Exception {
		zzxm.xmhSelectModel.clear();
	}

}

class UncommitSpecifiedXmhTask extends AbstractZZXMTask {

	private final Collection<String> c;

	public UncommitSpecifiedXmhTask(ZZXM zzxm, Collection<String> c) {
		super(zzxm);
		this.c = c;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void todo() throws Exception {
		zzxm.xmhSelectModel.removeAll(c);
	}

}
