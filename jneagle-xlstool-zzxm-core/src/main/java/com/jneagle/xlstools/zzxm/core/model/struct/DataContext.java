package com.jneagle.xlstools.zzxm.core.model.struct;

import org.apache.poi.ss.usermodel.Cell;

/**
 * 数据关联。
 * 
 * @author DwArFeng
 * @since 1.0.0.a
 */
public interface DataContext {

	/** 数据类型。 */
	public enum DataType {
		/** 文本。 */
		STRING,
		/** 数字。 */
		NUMBER,
		/** 布尔。 */
		BOOLEAN,
		/** 日期。 */
		DATE,
		/** 公式。 */
		FORMULA,

	}

	/**
	 * 
	 * @param dataType
	 * @return
	 * @throws NullPointerException
	 */
	public boolean isSuitable(DataType dataType) throws NullPointerException;

	/**
	 * 
	 * @param dataType
	 * @return
	 * @throws NullPointerException
	 * @throws IllegalStateException
	 */
	public Object getValue(DataType dataType) throws NullPointerException, IllegalStateException;

	/**
	 * 
	 * @param dataType
	 * @param clas
	 * @return
	 * @throws NullPointerException
	 * @throws IllegalStateException
	 * @throws ClassCastException
	 */
	public <T> T getValue(DataType dataType, Class<T> clas)
			throws NullPointerException, IllegalStateException, ClassCastException;

	/**
	 * 
	 * @return
	 */
	public Cell getContextCell();

}
