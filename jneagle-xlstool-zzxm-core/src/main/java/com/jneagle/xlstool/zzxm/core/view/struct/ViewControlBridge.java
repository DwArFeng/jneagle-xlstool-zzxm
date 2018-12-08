package com.jneagle.xlstool.zzxm.core.view.struct;

import java.util.Collection;

/**
 * 视图控制桥。
 * 
 * @author DwArFeng
 * @since 1.0.0.a
 */
public interface ViewControlBridge {

	/**
	 * 释放资源，通知系统结束进程。
	 */
	public void dispose();

	/**
	 * 选择加载文件。
	 */
	public void selectLoadFile();

	/**
	 * 更改加载密码。
	 * 
	 * @param password
	 *            新密码。
	 */
	public void changeLoadPassword(String password);

	/**
	 * 读取文件。
	 */
	public void loadFile();

	/**
	 * 选择导出文件。
	 */
	public void selectExportFile();

	/**
	 * 更改导出密码。
	 * 
	 * @param password
	 *            新密码。
	 */
	public void changeExportPassword(String password);

	/**
	 * 导出文件。
	 */
	public void exportFile();

	/**
	 * 提交选择的项目号。
	 * 
	 * @param xmh
	 *            选择的项目号。
	 */
	public void commitSelectedXmh(String xmh);

	/**
	 * 提交所有项目号。
	 */
	public void commitAllXmh();

	/**
	 * 取消提交所有项目号。
	 */
	public void uncommitAllXmh();

	/**
	 * 取消提交指定的项目号。
	 * 
	 * @param c
	 *            指定的项目号组成的集合。
	 */
	public void uncommitSpecifiedXmh(Collection<String> c);

}
