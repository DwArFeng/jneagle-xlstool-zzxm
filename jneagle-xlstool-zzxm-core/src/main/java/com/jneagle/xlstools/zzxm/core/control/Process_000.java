package com.jneagle.xlstools.zzxm.core.control;

/**
 * 抽象ZZXM任务。
 * 
 * @author DwArFeng
 * @since 1.0.0.a
 */
abstract class AbstractZZXMProcess implements Runnable {

	/** ZZXM实例。 */
	protected final ZZXM zzxm;
	private Throwable throwable;

	/**
	 * 新实例。
	 * 
	 * @param zzxm
	 *            ZZXM实例。
	 */
	protected AbstractZZXMProcess(ZZXM zzxm) {
		this.zzxm = zzxm;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void run() {
		try {
			todo();
		} catch (Throwable e) {
			this.throwable = e;
		}
	}

	/**
	 * @return the throwable
	 */
	public Throwable getThrowable() {
		return throwable;
	}

	protected abstract void todo() throws Exception;

}
