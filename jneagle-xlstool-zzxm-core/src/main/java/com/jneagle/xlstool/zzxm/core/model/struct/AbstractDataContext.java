package com.jneagle.xlstool.zzxm.core.model.struct;

import java.util.Objects;

import org.apache.poi.ss.usermodel.Cell;

/**
 * 抽象数据关联。
 * 
 * @author DwArFeng
 * @since 1.0.0.a
 */
public abstract class AbstractDataContext implements DataContext {

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract boolean isSuitable(DataType dataType) throws NullPointerException;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract Object getValue(DataType dataType) throws NullPointerException, IllegalStateException;

	/**
	 * {@inheritDoc}
	 */
	@Override
	public <T> T getValue(DataType dataType, Class<T> clas)
			throws NullPointerException, IllegalStateException, ClassCastException {
		Objects.requireNonNull(clas, "入口参数 clas 不能为 null。");
		return clas.cast(getValue(dataType));
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public abstract Cell getContextCell();

	/**
	 * 检查该数据上下文是否适合指定的数据类型。
	 * 
	 * <p>
	 * 如果该上下文不适合指定的类型，则抛出异常。
	 * 
	 * @param dataType
	 *            指定的数据类型。
	 * @throws IllegalStateException
	 *             上下文不符合指定的数据类型抛出的异常。
	 * @throws NullPointerException
	 *             指定的入口参数为 <code> null </code>。
	 */
	protected void checkSuitable(DataType dataType) throws NullPointerException, IllegalStateException {
		Objects.requireNonNull(dataType, "入口参数 dataType 不能为 null。");
		if (!isSuitable(dataType)) {
			throw new IllegalStateException(String.format("This data context is not suitable for %s.", dataType));
		}
	}

}
