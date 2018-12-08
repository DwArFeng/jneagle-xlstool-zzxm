package com.jneagle.xlstool.zzxm.core.model.struct;

import java.util.Objects;
import java.util.Optional;

import org.apache.poi.ss.usermodel.Cell;

import com.dwarfeng.dutil.basic.prog.ProcessException;

/**
 * 
 * @author DwArFeng
 * @since 1.1.0.a
 */
public class CellProcessException extends ProcessException {

	private static final long serialVersionUID = 6542054852843987605L;

	public final static String DEFAULT_MESSAGE_FORMAT = "Exception at {cell-location}";
	public final static String CELL_LOCATION_TAG = "{cell-location}";

	private final Cell cell;
	private final String messageFormat;

	public CellProcessException(Cell cell) {
		this(cell, DEFAULT_MESSAGE_FORMAT, null);
	}

	public CellProcessException(Cell cell, String messageFormat) {
		this(cell, messageFormat, null);
	}

	public CellProcessException(Cell cell, String messageFormat, Throwable cause) {
		super(cause);
		this.cell = cell;
		this.messageFormat = Optional.ofNullable(messageFormat).orElse(DEFAULT_MESSAGE_FORMAT);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public String getMessage() {
		return messageFormat.replace(CELL_LOCATION_TAG, getCellLocation(cell));
	}

	private String getCellLocation(Cell cell) {
		if (Objects.isNull(cell)) {
			return "[null]";
		}

		String rowName = Integer.toString(cell.getRowIndex() + 1);
		String columnName = "";

		int columnIndex = cell.getColumnIndex();
		if (columnIndex < 0) {
			return "[error]";
		}

		int num = 65;// A的Unicode码
		do {
			if (columnName.length() > 0) {
				columnIndex--;
			}
			int remainder = columnIndex % 26;
			columnName = ((char) (remainder + num)) + columnName;
			columnIndex = (int) ((columnIndex - remainder) / 26);
		} while (columnIndex > 0);

		return new StringBuilder().append('[').append(columnName).append(rowName).append(']').toString();
	}

}
