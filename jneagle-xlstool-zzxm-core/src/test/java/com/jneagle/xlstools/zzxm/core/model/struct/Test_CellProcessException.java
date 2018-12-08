package com.jneagle.xlstools.zzxm.core.model.struct;

import static org.junit.Assert.assertEquals;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.jneagle.xlstools.zzxm.core.model.struct.CellProcessException;

public class Test_CellProcessException {

	private static Workbook workbook;
	private static Cell cell_A1;
	private static Cell cell_AA1;
	private static Cell cell_ADC1;

	@BeforeClass
	public static void setUpBeforeClass() throws Exception {
		workbook = new XSSFWorkbook();
		Sheet sheet = workbook.createSheet();
		Row row = sheet.createRow(0);
		cell_A1 = row.createCell(0);
		cell_AA1 = row.createCell(26);
		cell_ADC1 = row.createCell(782);
	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
		workbook.close();
	}

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public final void test() {
		assertEquals("[A1]", new CellProcessException(cell_A1, CellProcessException.CELL_LOCATION_TAG).getMessage());
		assertEquals("[AA1]", new CellProcessException(cell_AA1, CellProcessException.CELL_LOCATION_TAG).getMessage());
		assertEquals("[ADC1]", new CellProcessException(cell_ADC1, CellProcessException.CELL_LOCATION_TAG).getMessage());
	}

}
