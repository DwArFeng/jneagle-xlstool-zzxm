package com.jneagle.xlstool.zzxm.core.control;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddress;

import com.dwarfeng.dutil.basic.cna.model.DefaultReferenceModel;
import com.dwarfeng.dutil.basic.cna.model.ReferenceModel;
import com.dwarfeng.dutil.basic.gui.swing.SwingUtil;
import com.dwarfeng.dutil.basic.io.FileUtil;
import com.dwarfeng.dutil.basic.io.SaveFailedException;
import com.dwarfeng.dutil.basic.io.StreamSaver;
import com.dwarfeng.dutil.basic.mea.TimeMeasurer;
import com.jneagle.xlstool.zzxm.core.model.cm.SyncMachiningModel;
import com.jneagle.xlstool.zzxm.core.model.eum.ConfigItem;
import com.jneagle.xlstool.zzxm.core.model.eum.I18nStringKey;
import com.jneagle.xlstool.zzxm.core.model.eum.ProgressState;
import com.jneagle.xlstool.zzxm.core.model.struct.CellProcessException;
import com.jneagle.xlstool.zzxm.core.model.struct.DataContext;
import com.jneagle.xlstool.zzxm.core.model.struct.MachiningInfo;
import com.jneagle.xlstool.zzxm.core.model.struct.DataContext.DataType;

class ExportFileTask extends AbstractZZXMTask {

	public ExportFileTask(ZZXM zzxm) {
		super(zzxm);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void todo() throws Exception {
		// 定义导出文件
		final File exportFile = zzxm.excelExportFileModel.get();
		// 如果目标文件不存在，提示用户并中止。
		if (Objects.isNull(exportFile)) {
			zzxm.loggerHandler.info(zzxm.getI18n(I18nStringKey.LOGGER_35));
			SwingUtil.invokeInEventQueue(() -> {
				zzxm.mainFrameModel.get().notification(zzxm.getI18n(I18nStringKey.LABEL_33));
				JOptionPane.showMessageDialog(zzxm.mainFrameModel.get(), zzxm.getI18n(I18nStringKey.LABEL_32),
						zzxm.getI18n(I18nStringKey.LABEL_8), JOptionPane.WARNING_MESSAGE);
			});
			return;
		}

		// 设置进度条模型。
		zzxm.currentProgressModel.set(0);
		zzxm.totleProgressModel.set(-1);
		// 设置通知区域文本并显示进度条。
		SwingUtil.invokeInEventQueue(() -> {
			zzxm.mainFrameModel.get().notification(zzxm.getI18n(I18nStringKey.LABEL_27));
			zzxm.mainFrameModel.get().setProgressPanelVisible(true);
		});
		// 记录日志。
		zzxm.loggerHandler.info(zzxm.getI18n(I18nStringKey.LOGGER_28));
		// 观察项目号选择模型，如果没有选择项目号，则提示用户。
		if (zzxm.xmhSelectModel.isEmpty()) {
			ReferenceModel<Integer> resultRef = new DefaultReferenceModel<>();
			SwingUtil.invokeAndWaitInEventQueue(() -> {
				resultRef.set(JOptionPane.showConfirmDialog(zzxm.mainFrameModel.get(),
						zzxm.getI18n(I18nStringKey.LABEL_34), zzxm.getI18n(I18nStringKey.LABEL_8),
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE));
			});
			if (resultRef.get() != JOptionPane.YES_OPTION) {
				zzxm.loggerHandler.info(zzxm.getI18n(I18nStringKey.LOGGER_34));
				SwingUtil.invokeInEventQueue(() -> {
					zzxm.mainFrameModel.get().notification(zzxm.getI18n(I18nStringKey.LABEL_38));
					zzxm.mainFrameModel.get().setProgressPanelVisible(false);
				});
				return;
			}
		}
		// 如果文件已经存在，则提示用户是否继续进行。
		if (exportFile.exists()) {
			ReferenceModel<Integer> resultRef = new DefaultReferenceModel<>();
			SwingUtil.invokeAndWaitInEventQueue(() -> {
				resultRef.set(JOptionPane.showConfirmDialog(zzxm.mainFrameModel.get(),
						zzxm.getI18n(I18nStringKey.LABEL_41), zzxm.getI18n(I18nStringKey.LABEL_8),
						JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE));
			});
			if (resultRef.get() != JOptionPane.YES_OPTION) {
				zzxm.loggerHandler.info(zzxm.getI18n(I18nStringKey.LOGGER_34));
				SwingUtil.invokeInEventQueue(() -> {
					zzxm.mainFrameModel.get().notification(zzxm.getI18n(I18nStringKey.LABEL_38));
					zzxm.mainFrameModel.get().setProgressPanelVisible(false);
				});
				return;
			}
		}
		// 设置进度。
		zzxm.exportStateModel.set(ProgressState.STARTED);
		// 尝试创建不存在的文件。
		try {
			FileUtil.createFileIfNotExists(exportFile);
		} catch (IOException e) {
			// 通知用户创建文件发生异常，并中止导出过程。
			SwingUtil.invokeAndWaitInEventQueue(() -> {
				zzxm.mainFrameModel.get().notification(zzxm.getI18n(I18nStringKey.LABEL_35));
				JOptionPane.showMessageDialog(zzxm.mainFrameModel.get(), zzxm.getI18n(I18nStringKey.LABEL_31),
						zzxm.getI18n(I18nStringKey.LABEL_8), JOptionPane.ERROR_MESSAGE);
				zzxm.mainFrameModel.get().setProgressPanelVisible(false);
			});
			zzxm.loggerHandler.error(zzxm.getI18n(I18nStringKey.LOGGER_39), e);
			zzxm.exportStateModel.set(ProgressState.NOT_START);
			return;
		}
		// 获取文件密码。
		String password = zzxm.excelExportPasswordModel.get();
		// 清空密码模型中的密码。
		zzxm.excelExportPasswordModel.set("");
		// 创建计时器。
		TimeMeasurer tm = new TimeMeasurer();
		// 计时器启动。
		tm.start();
		// 存放导出异常的对象。
		Set<SaveFailedException> exceptions = new LinkedHashSet<>();
		// 导出过程。
		{
			try (XlsMachiningSaver saver = new XlsMachiningSaver(new FileOutputStream(exportFile), password,
					zzxm.xmhSelectModel)) {
				exceptions.addAll(saver.countinuousSave(zzxm.filteredMachiningModel));
			} catch (IOException e) {
				// 通知用户创建文件发生异常，并中止导出过程。
				SwingUtil.invokeInEventQueue(() -> {
					zzxm.mainFrameModel.get().notification(zzxm.getI18n(I18nStringKey.LABEL_39));
					JOptionPane.showMessageDialog(zzxm.mainFrameModel.get(), zzxm.getI18n(I18nStringKey.LABEL_40),
							zzxm.getI18n(I18nStringKey.LABEL_8), JOptionPane.ERROR_MESSAGE);
				});
				zzxm.loggerHandler.error(zzxm.getI18n(I18nStringKey.LOGGER_43), e);
				zzxm.exportStateModel.set(ProgressState.NOT_START);
				return;
			}
		}
		// 结束计时。
		tm.stop();
		// 设置通知区域文本并隐藏进度条。
		SwingUtil.invokeInEventQueue(() -> {
			zzxm.mainFrameModel.get().notification(
					String.format(zzxm.getI18n(I18nStringKey.LABEL_36), tm.getTimeMs(), exceptions.size()));
			zzxm.mainFrameModel.get().setProgressPanelVisible(false);
		});
		// 如果错误大于等于1个，则进行通知。
		if (exceptions.size() >= 1) {
			SwingUtil.invokeInEventQueue(() -> {
				JOptionPane.showMessageDialog(zzxm.mainFrameModel.get(),
						String.format(zzxm.getI18n(I18nStringKey.LABEL_37), exceptions.size()),
						zzxm.getI18n(I18nStringKey.LABEL_8), JOptionPane.WARNING_MESSAGE);
			});
			// 获得最大记录数据。
			int maxLogSize = zzxm.configHandler.getParsedValidValue(ConfigItem.LOG_MAX_EXCEL_EXPORT_WARNING,
					Integer.class);
			// 如果异常数大于最大记录数，在日志中通知用户。
			if (exceptions.size() > maxLogSize)
				zzxm.loggerHandler.warn(String.format(zzxm.getI18n(I18nStringKey.LOGGER_40), maxLogSize));
			// 记录日志。
			exceptions.stream().limit(maxLogSize).forEach(exception -> {
				zzxm.loggerHandler.warn(zzxm.getI18n(I18nStringKey.LOGGER_41), exception);
			});
		}
		// 输出总结日志。
		zzxm.loggerHandler.info(String.format(zzxm.getI18n(I18nStringKey.LOGGER_42), tm.getTimeMs()));
		// 设置读取标记。
		zzxm.exportStateModel.set(ProgressState.FINISHED);
	}

	private final class XlsMachiningSaver extends StreamSaver<SyncMachiningModel> {

		private final Collection<String> selectedXmhs;

		private Workbook workbook;
		private boolean saveFlag = false;

		public XlsMachiningSaver(OutputStream out, String password, Collection<String> selectedXmhs)
				throws Exception {
			super(out);
			this.selectedXmhs = selectedXmhs;
			org.apache.poi.hssf.record.crypto.Biff8EncryptionKey
					.setCurrentUserPassword(password.isEmpty() ? null : password);
			workbook = new HSSFWorkbook();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void save(SyncMachiningModel machiningModel) throws SaveFailedException, IllegalStateException {
			if (saveFlag)
				throw new IllegalStateException("Load method can be called only once");

			Objects.requireNonNull(machiningModel, "入口参数 machiningModel 不能为 null。");

			try {
				saveFlag = true;

				// 定义格式映射。
				Map<String, CellStyle> cellStyleMap = new HashMap<>();
				// 构建格式映射。
				initCellStyleMap(cellStyleMap, workbook);

				// 创建汇总表。
				Sheet summarizeSheet = workbook.createSheet(zzxm.getI18n(I18nStringKey.XLS_1));
				// 为总表添加表头。
				exportHeader(cellStyleMap, summarizeSheet);
				// 定义当前行。
				int currentRow = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_ROW_FIRST_DATA,
						Integer.class);

				zzxm.currentProgressModel.set(0);
				zzxm.totleProgressModel.set(machiningModel.xmhSet().size());
				int i = 0;
				// 导出数据。
				for (String xmh : machiningModel.xmhSet()) {
					i++;
					if (selectedXmhs.contains(xmh)) {
						// 导出总表。
						for (MachiningInfo machiningInfo : machiningModel.getMachiningInfos(xmh)) {
							Set<CellProcessException> exportData = exportData(cellStyleMap, machiningInfo,
									summarizeSheet, currentRow++);
							if (!exportData.isEmpty()) {
								throw exportData.stream().findFirst().get();
							}
						}

						// 创建分表。
						Sheet subSheet = workbook.createSheet(xmh);
						// 为分表添加表头。
						exportHeader(cellStyleMap, subSheet);
						// 创建分表当前行。
						int currentSubRow = zzxm.configHandler
								.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_ROW_FIRST_DATA, Integer.class);
						// 导出分表。
						for (MachiningInfo machiningInfo : machiningModel.getMachiningInfos(xmh)) {
							Set<CellProcessException> exportData = exportData(cellStyleMap, machiningInfo, subSheet,
									currentSubRow++);
							if (!exportData.isEmpty()) {
								throw exportData.stream().findFirst().get();
							}
						}
					}
					zzxm.currentProgressModel.set(i);
				}

				zzxm.totleProgressModel.set(-1);
				workbook.write(out);

			} catch (Exception e) {
				throw new SaveFailedException(zzxm.getI18n(I18nStringKey.LOGGER_41), e);
			}

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Set<SaveFailedException> countinuousSave(SyncMachiningModel machiningModel)
				throws IllegalStateException {
			if (saveFlag)
				throw new IllegalStateException("Load method can be called only once");

			Objects.requireNonNull(machiningModel, "入口参数 machiningModel 不能为 null。");

			final Set<SaveFailedException> exceptions = new LinkedHashSet<>();

			try {
				saveFlag = true;

				// 定义格式映射。
				Map<String, CellStyle> cellStyleMap = new HashMap<>();
				// 构建格式映射。
				initCellStyleMap(cellStyleMap, workbook);

				// 创建汇总表。
				Sheet summarizeSheet = workbook.createSheet(zzxm.getI18n(I18nStringKey.XLS_1));
				// 为总表添加表头。
				exportHeader(cellStyleMap, summarizeSheet);
				// 定义当前行。
				int currentRow = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_ROW_FIRST_DATA,
						Integer.class);

				zzxm.currentProgressModel.set(0);
				zzxm.totleProgressModel.set(machiningModel.xmhSet().size());
				int i = 0;
				// 导出数据。
				for (String xmh : machiningModel.xmhSet()) {
					i++;
					if (selectedXmhs.contains(xmh)) {
						// 导出总表。
						for (MachiningInfo machiningInfo : machiningModel.getMachiningInfos(xmh)) {
							exportData(cellStyleMap, machiningInfo, summarizeSheet, currentRow++).forEach(e -> {
								exceptions.add(new SaveFailedException(zzxm.getI18n(I18nStringKey.LOGGER_41), e));
							});
						}

						// 创建分表。
						Sheet subSheet = workbook.createSheet(xmh.replace("/", "-"));
						// 为分表添加表头。
						exportHeader(cellStyleMap, subSheet);
						// 创建分表当前行。
						int currentSubRow = zzxm.configHandler
								.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_ROW_FIRST_DATA, Integer.class);
						// 导出分表。
						for (MachiningInfo machiningInfo : machiningModel.getMachiningInfos(xmh)) {
							exportData(cellStyleMap, machiningInfo, subSheet, currentSubRow++).forEach(e -> {
								exceptions.add(new SaveFailedException(zzxm.getI18n(I18nStringKey.LOGGER_41), e));
							});
						}
						zzxm.currentProgressModel.set(i);
					}
				}

				zzxm.totleProgressModel.set(-1);
				workbook.write(out);

			} catch (Exception e) {
				exceptions.add(new SaveFailedException(zzxm.getI18n(I18nStringKey.LOGGER_41), e));
			}

			return exceptions;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void close() throws IOException {
			super.close();
			workbook.close();
			workbook = null;
		}

		private void initCellStyleMap(Map<String, CellStyle> cellStyleMap, Workbook workbook) { // 定义格式。
			CellStyle header_green = workbook.createCellStyle();
			{
				header_green.setAlignment(HorizontalAlignment.CENTER);
				header_green.setVerticalAlignment(VerticalAlignment.CENTER);
				header_green.setBorderBottom(BorderStyle.THIN);
				header_green.setBorderTop(BorderStyle.THIN);
				header_green.setBorderLeft(BorderStyle.THIN);
				header_green.setBorderRight(BorderStyle.THIN);
				header_green.setFillForegroundColor((short) 50);
				header_green.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				cellStyleMap.put("header.green", header_green);
			}
			CellStyle header_pink = workbook.createCellStyle();
			{
				header_pink.setAlignment(HorizontalAlignment.CENTER);
				header_pink.setVerticalAlignment(VerticalAlignment.CENTER);
				header_pink.setBorderBottom(BorderStyle.THIN);
				header_pink.setBorderTop(BorderStyle.THIN);
				header_pink.setBorderLeft(BorderStyle.THIN);
				header_pink.setBorderRight(BorderStyle.THIN);
				header_pink.setFillForegroundColor((short) 29);
				header_pink.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				cellStyleMap.put("header.pink", header_pink);
			}
			CellStyle header_yellow = workbook.createCellStyle();
			{
				header_yellow.setAlignment(HorizontalAlignment.CENTER);
				header_yellow.setVerticalAlignment(VerticalAlignment.CENTER);
				header_yellow.setBorderBottom(BorderStyle.THIN);
				header_yellow.setBorderTop(BorderStyle.THIN);
				header_yellow.setBorderLeft(BorderStyle.THIN);
				header_yellow.setBorderRight(BorderStyle.THIN);
				header_yellow.setFillForegroundColor((short) 34);
				header_yellow.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				cellStyleMap.put("header.yellow", header_yellow);
			}
			CellStyle header_blue = workbook.createCellStyle();
			{
				header_blue.setAlignment(HorizontalAlignment.CENTER);
				header_blue.setVerticalAlignment(VerticalAlignment.CENTER);
				header_blue.setBorderBottom(BorderStyle.THIN);
				header_blue.setBorderTop(BorderStyle.THIN);
				header_blue.setBorderLeft(BorderStyle.THIN);
				header_blue.setBorderRight(BorderStyle.THIN);
				header_blue.setFillForegroundColor((short) 40);
				header_blue.setFillPattern(FillPatternType.SOLID_FOREGROUND);
				cellStyleMap.put("header.blue", header_blue);
			}
			CellStyle data_normal = workbook.createCellStyle();
			{
				data_normal.setAlignment(HorizontalAlignment.GENERAL);
				data_normal.setBorderBottom(BorderStyle.THIN);
				data_normal.setBorderTop(BorderStyle.THIN);
				data_normal.setBorderLeft(BorderStyle.THIN);
				data_normal.setBorderRight(BorderStyle.THIN);
				cellStyleMap.put("data.normal", data_normal);
			}
			CellStyle data_date = workbook.createCellStyle();
			{
				CreationHelper creationHelper = workbook.getCreationHelper();
				short format = creationHelper.createDataFormat().getFormat(
						zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_DATE_FORMAT, String.class));
				data_date.setDataFormat(format);
				data_date.setAlignment(HorizontalAlignment.GENERAL);
				data_date.setBorderBottom(BorderStyle.THIN);
				data_date.setBorderTop(BorderStyle.THIN);
				data_date.setBorderLeft(BorderStyle.THIN);
				data_date.setBorderRight(BorderStyle.THIN);
				cellStyleMap.put("data.date", data_date);
			}
		}

		private Set<CellProcessException> exportData(Map<String, CellStyle> cellStyleMap, MachiningInfo machiningInfo,
				Sheet sheet, int rowIndex) {
			Set<CellProcessException> exceptions = new LinkedHashSet<>();

			Row row = sheet.createRow(rowIndex);

			Cell currentCell = null;

			// 项目号。
			{
				final DataContext dataContext = machiningInfo.get("xmh");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_XMH,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column xmh is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 部件号。
			{
				final DataContext dataContext = machiningInfo.get("bjh");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BJH,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column bjh is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 零件号。
			{
				final DataContext dataContext = machiningInfo.get("ljh");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_LJH,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column ljh is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 零件名称。
			{
				final DataContext dataContext = machiningInfo.get("ljmc");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_LJMC,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column ljmc is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 材质。
			{
				final DataContext dataContext = machiningInfo.get("cz");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_CZ,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column cz is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 单需。
			{
				final DataContext dataContext = machiningInfo.get("dx");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_DX,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class).intValue());
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column dx is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 单重。
			{
				final DataContext dataContext = machiningInfo.get("dz");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_DZ,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class));
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column dz is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 总重。
			{
				final DataContext dataContext = machiningInfo.get("zz");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_ZZ,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class));
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column zz is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 台数。
			{
				final DataContext dataContext = machiningInfo.get("ts");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_TS,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class).intValue());
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column ts is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 总数。
			{
				final DataContext dataContext = machiningInfo.get("zs");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_ZS,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class).intValue());
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column zs is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 下图日期。
			{
				final DataContext dataContext = machiningInfo.get("xtrq");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_XTRQ,
						Integer.class);
				currentCell = row.createCell(column);
				try {
					if (dataContext.isSuitable(DataType.DATE)) {
						Date value = dataContext.getValue(DataType.DATE, Date.class);
						if (Objects.nonNull(value)) {
							currentCell.setCellStyle(cellStyleMap.get("data.date"));
							currentCell.setCellValue(value);
						} else {
							currentCell.setCellStyle(cellStyleMap.get("data.normal"));
							currentCell.setCellValue((String) null);
						}
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column xtrq is not suitable for date or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 配套日期。
			{
				final DataContext dataContext = machiningInfo.get("ptrq");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_PTRQ,
						Integer.class);
				currentCell = row.createCell(column);
				try {
					if (dataContext.isSuitable(DataType.DATE)) {
						Date value = dataContext.getValue(DataType.DATE, Date.class);
						if (Objects.nonNull(value)) {
							currentCell.setCellStyle(cellStyleMap.get("data.date"));
							currentCell.setCellValue(value);
						} else {
							currentCell.setCellStyle(cellStyleMap.get("data.normal"));
							currentCell.setCellValue((String) null);
						}
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column ptrq is not suitable for date or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 备料-类别。
			{
				final DataContext dataContext = machiningInfo.get("bl-lb");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BL_LB,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column bl-lbis not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 四次工序.委托方。
			{
				final DataContext dataContext = machiningInfo.get("scgx.wtf");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_SCGX_WTF,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column scgx.wtf is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 四次工序.数量。
			{
				final DataContext dataContext = machiningInfo.get("scgx.sl");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_SCGX_SL,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class).intValue());
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column scgx.sl is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 四次工序.加工内容。
			{
				final DataContext dataContext = machiningInfo.get("scgx.jgnr");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_SCGX_JGNR,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column scgx.jgnr is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 四次工序.单价。
			{
				final DataContext dataContext = machiningInfo.get("scgx.dj");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_SCGX_DJ,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class));
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column scgx.dj is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 四次工序.开单日期。
			{
				final DataContext dataContext = machiningInfo.get("scgx.kdrq");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_SCGX_KDRQ,
						Integer.class);
				currentCell = row.createCell(column);
				try {
					if (dataContext.isSuitable(DataType.DATE)) {
						Date value = dataContext.getValue(DataType.DATE, Date.class);
						if (Objects.nonNull(value)) {
							currentCell.setCellStyle(cellStyleMap.get("data.date"));
							currentCell.setCellValue(value);
						} else {
							currentCell.setCellStyle(cellStyleMap.get("data.normal"));
							currentCell.setCellValue((String) null);
						}
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column scgx.kdrq is not suitable for date or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 四次工序.入库日期。
			{
				final DataContext dataContext = machiningInfo.get("scgx.rkrq");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_SCGX_RKRQ,
						Integer.class);
				currentCell = row.createCell(column);
				try {
					if (dataContext.isSuitable(DataType.DATE)) {
						Date value = dataContext.getValue(DataType.DATE, Date.class);
						if (Objects.nonNull(value)) {
							currentCell.setCellStyle(cellStyleMap.get("data.date"));
							currentCell.setCellValue(value);
						} else {
							currentCell.setCellStyle(cellStyleMap.get("data.normal"));
							currentCell.setCellValue((String) null);
						}
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column scgx.rkrq is not suitable for date or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 四次工序.入库数。
			{
				final DataContext dataContext = machiningInfo.get("scgx.rks");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_SCGX_RKS,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class).intValue());
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column scgx.rks is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 四次工序.缺件。
			{
				final DataContext dataContext = machiningInfo.get("scgx.qj");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_SCGX_QJ,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class).intValue());
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column scgx.rks is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 四次工序.序号。
			{
				final DataContext dataContext = machiningInfo.get("scgx.xh");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_SCGX_XH,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class).intValue());
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column scgx.xh is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 四次工序.备注。
			{
				final DataContext dataContext = machiningInfo.get("scgx.bz");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_SCGX_BZ,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column scgx.bz is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 四次工序.检验员。
			{
				final DataContext dataContext = machiningInfo.get("scgx.jyy");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_SCGX_JYY,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column scgx.jyy is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 四次工序.操作者。
			{
				final DataContext dataContext = machiningInfo.get("scgx.czz");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_SCGX_CZZ,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column scgx.czz is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 七次工序.委托方。
			{
				final DataContext dataContext = machiningInfo.get("qcgx.wtf");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_QCGX_WTF,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column qcgx.wtf is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 七次工序.数量。
			{
				final DataContext dataContext = machiningInfo.get("qcgx.sl");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_QCGX_SL,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class).intValue());
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column qcgx.sl is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 七次工序.加工内容。
			{
				final DataContext dataContext = machiningInfo.get("qcgx.jgnr");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_QCGX_JGNR,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column qcgx.jgnr is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 七次工序.单价。
			{
				final DataContext dataContext = machiningInfo.get("qcgx.dj");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_QCGX_DJ,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class));
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column qcgx.dj is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 七次工序.开单日期。
			{
				final DataContext dataContext = machiningInfo.get("qcgx.kdrq");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_QCGX_KDRQ,
						Integer.class);
				currentCell = row.createCell(column);
				try {
					if (dataContext.isSuitable(DataType.DATE)) {
						Date value = dataContext.getValue(DataType.DATE, Date.class);
						if (Objects.nonNull(value)) {
							currentCell.setCellStyle(cellStyleMap.get("data.date"));
							currentCell.setCellValue(value);
						} else {
							currentCell.setCellStyle(cellStyleMap.get("data.normal"));
							currentCell.setCellValue((String) null);
						}
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column qcgx.kdrq is not suitable for date or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 七次工序.入库日期。
			{
				final DataContext dataContext = machiningInfo.get("qcgx.rkrq");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_QCGX_RKRQ,
						Integer.class);
				currentCell = row.createCell(column);
				try {
					if (dataContext.isSuitable(DataType.DATE)) {
						Date value = dataContext.getValue(DataType.DATE, Date.class);
						if (Objects.nonNull(value)) {
							currentCell.setCellStyle(cellStyleMap.get("data.date"));
							currentCell.setCellValue(value);
						} else {
							currentCell.setCellStyle(cellStyleMap.get("data.normal"));
							currentCell.setCellValue((String) null);
						}
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column qcgx.rkrq is not suitable for date or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 七次工序.入库数。
			{
				final DataContext dataContext = machiningInfo.get("qcgx.rks");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_QCGX_RKS,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class).intValue());
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column qcgx.rks is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 七次工序.缺件。
			{
				final DataContext dataContext = machiningInfo.get("qcgx.qj");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_QCGX_QJ,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class).intValue());
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column qcgx.rks is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 七次工序.序号。
			{
				final DataContext dataContext = machiningInfo.get("qcgx.xh");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_QCGX_XH,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class).intValue());
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column qcgx.xh is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 七次工序.备注。
			{
				final DataContext dataContext = machiningInfo.get("qcgx.bz");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_QCGX_BZ,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column qcgx.bz is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 七次工序.检验员。
			{
				final DataContext dataContext = machiningInfo.get("qcgx.jyy");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_QCGX_JYY,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column qcgx.jyy is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 七次工序.操作者。
			{
				final DataContext dataContext = machiningInfo.get("qcgx.czz");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_QCGX_CZZ,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column qcgx.czz is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 八次工序.委托方。
			{
				final DataContext dataContext = machiningInfo.get("bcgx.wtf");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BCGX_WTF,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column bcgx.wtf is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 八次工序.数量。
			{
				final DataContext dataContext = machiningInfo.get("bcgx.sl");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BCGX_SL,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class).intValue());
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column bcgx.sl is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 八次工序.加工内容。
			{
				final DataContext dataContext = machiningInfo.get("bcgx.jgnr");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BCGX_JGNR,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column bcgx.jgnr is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 八次工序.单价。
			{
				final DataContext dataContext = machiningInfo.get("bcgx.dj");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BCGX_DJ,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class));
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column bcgx.dj is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 八次工序.开单日期。
			{
				final DataContext dataContext = machiningInfo.get("bcgx.kdrq");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BCGX_KDRQ,
						Integer.class);
				currentCell = row.createCell(column);
				try {
					if (dataContext.isSuitable(DataType.DATE)) {
						Date value = dataContext.getValue(DataType.DATE, Date.class);
						if (Objects.nonNull(value)) {
							currentCell.setCellStyle(cellStyleMap.get("data.date"));
							currentCell.setCellValue(value);
						} else {
							currentCell.setCellStyle(cellStyleMap.get("data.normal"));
							currentCell.setCellValue((String) null);
						}
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column bcgx.kdrq is not suitable for date or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 八次工序.入库日期。
			{
				final DataContext dataContext = machiningInfo.get("bcgx.rkrq");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BCGX_RKRQ,
						Integer.class);
				currentCell = row.createCell(column);
				try {
					if (dataContext.isSuitable(DataType.DATE)) {
						Date value = dataContext.getValue(DataType.DATE, Date.class);
						if (Objects.nonNull(value)) {
							currentCell.setCellStyle(cellStyleMap.get("data.date"));
							currentCell.setCellValue(value);
						} else {
							currentCell.setCellStyle(cellStyleMap.get("data.normal"));
							currentCell.setCellValue((String) null);
						}
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column bcgx.rkrq is not suitable for date or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 八次工序.入库数。
			{
				final DataContext dataContext = machiningInfo.get("bcgx.rks");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BCGX_RKS,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class).intValue());
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column bcgx.rks is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 八次工序.缺件。
			{
				final DataContext dataContext = machiningInfo.get("bcgx.qj");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BCGX_QJ,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class).intValue());
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column bcgx.rks is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 八次工序.序号。
			{
				final DataContext dataContext = machiningInfo.get("bcgx.xh");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BCGX_XH,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class).intValue());
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column bcgx.xh is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 八次工序.备注。
			{
				final DataContext dataContext = machiningInfo.get("bcgx.bz");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BCGX_BZ,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column bcgx.bz is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 八次工序.检验员。
			{
				final DataContext dataContext = machiningInfo.get("bcgx.jyy");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BCGX_JYY,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column bcgx.jyy is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 八次工序.操作者。
			{
				final DataContext dataContext = machiningInfo.get("bcgx.czz");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BCGX_CZZ,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column bcgx.czz is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 九次工序.委托方。
			{
				final DataContext dataContext = machiningInfo.get("jcgx.wtf");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_JCGX_WTF,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column jcgx.wtf is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 九次工序.数量。
			{
				final DataContext dataContext = machiningInfo.get("jcgx.sl");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_JCGX_SL,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class).intValue());
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column jcgx.sl is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 九次工序.加工内容。
			{
				final DataContext dataContext = machiningInfo.get("jcgx.jgnr");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_JCGX_JGNR,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column jcgx.jgnr is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 九次工序.单价。
			{
				final DataContext dataContext = machiningInfo.get("jcgx.dj");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_JCGX_DJ,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class));
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column jcgx.dj is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 九次工序.开单日期。
			{
				final DataContext dataContext = machiningInfo.get("jcgx.kdrq");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_JCGX_KDRQ,
						Integer.class);
				currentCell = row.createCell(column);
				try {
					if (dataContext.isSuitable(DataType.DATE)) {
						Date value = dataContext.getValue(DataType.DATE, Date.class);
						if (Objects.nonNull(value)) {
							currentCell.setCellStyle(cellStyleMap.get("data.date"));
							currentCell.setCellValue(value);
						} else {
							currentCell.setCellStyle(cellStyleMap.get("data.normal"));
							currentCell.setCellValue((String) null);
						}
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column jcgx.kdrq is not suitable for date or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 九次工序.入库日期。
			{
				final DataContext dataContext = machiningInfo.get("jcgx.rkrq");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_JCGX_RKRQ,
						Integer.class);
				currentCell = row.createCell(column);
				try {
					if (dataContext.isSuitable(DataType.DATE)) {
						Date value = dataContext.getValue(DataType.DATE, Date.class);
						if (Objects.nonNull(value)) {
							currentCell.setCellStyle(cellStyleMap.get("data.date"));
							currentCell.setCellValue(value);
						} else {
							currentCell.setCellStyle(cellStyleMap.get("data.normal"));
							currentCell.setCellValue((String) null);
						}
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column jcgx.rkrq is not suitable for date or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 九次工序.入库数。
			{
				final DataContext dataContext = machiningInfo.get("jcgx.rks");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_JCGX_RKS,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class).intValue());
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column jcgx.rks is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 九次工序.缺件。
			{
				final DataContext dataContext = machiningInfo.get("jcgx.qj");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_JCGX_QJ,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class).intValue());
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column jcgx.rks is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 九次工序.序号。
			{
				final DataContext dataContext = machiningInfo.get("jcgx.xh");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_JCGX_XH,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.NUMBER)) {
						currentCell.setCellValue(dataContext.getValue(DataType.NUMBER, Double.class).intValue());
					} else if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column jcgx.xh is not suitable for number or string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 九次工序.备注。
			{
				final DataContext dataContext = machiningInfo.get("jcgx.bz");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_JCGX_BZ,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column jcgx.bz is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 九次工序.检验员。
			{
				final DataContext dataContext = machiningInfo.get("jcgx.jyy");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_JCGX_JYY,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column jcgx.jyy is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			// 九次工序.操作者。
			{
				final DataContext dataContext = machiningInfo.get("jcgx.czz");
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_JCGX_CZZ,
						Integer.class);
				currentCell = row.createCell(column);
				currentCell.setCellStyle(cellStyleMap.get("data.normal"));
				try {
					if (dataContext.isSuitable(DataType.STRING)) {
						currentCell.setCellValue(dataContext.getValue(DataType.STRING, String.class));
					} else {
						throw new IllegalStateException("Column jcgx.czz is not suitable for string.");
					}
				} catch (Exception e) {
					exceptions.add(new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_47), e));
				}
			}

			return exceptions;
		}

		private void exportHeader(Map<String, CellStyle> cellStyleMap, Sheet sheet) {
			final int firstHeader = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_ROW_FIRST_HEADER,
					Integer.class);
			sheet.createRow(firstHeader);
			sheet.createRow(firstHeader + 1);

			// 项目号。
			{
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_XMH,
						Integer.class);
				sheet.addMergedRegion(new CellRangeAddress(firstHeader, firstHeader + 1, column, column));
				Cell cell_1 = sheet.getRow(firstHeader).createCell(column);
				cell_1.setCellStyle(cellStyleMap.get("header.green"));
				cell_1.setCellValue(zzxm.getI18n(I18nStringKey.XLS_2));
				Cell cell_2 = sheet.getRow(firstHeader + 1).createCell(column);
				cell_2.setCellStyle(cellStyleMap.get("header.green"));
			}
			// 部件号。
			{
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BJH,
						Integer.class);
				sheet.addMergedRegion(new CellRangeAddress(firstHeader, firstHeader + 1, column, column));
				Cell cell_1 = sheet.getRow(firstHeader).createCell(column);
				cell_1.setCellStyle(cellStyleMap.get("header.green"));
				cell_1.setCellValue(zzxm.getI18n(I18nStringKey.XLS_3));
				Cell cell_2 = sheet.getRow(firstHeader + 1).createCell(column);
				cell_2.setCellStyle(cellStyleMap.get("header.green"));
			}
			// 零件号。
			{
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_LJH,
						Integer.class);
				sheet.addMergedRegion(new CellRangeAddress(firstHeader, firstHeader + 1, column, column));
				Cell cell_1 = sheet.getRow(firstHeader).createCell(column);
				cell_1.setCellStyle(cellStyleMap.get("header.green"));
				cell_1.setCellValue(zzxm.getI18n(I18nStringKey.XLS_4));
				Cell cell_2 = sheet.getRow(firstHeader + 1).createCell(column);
				cell_2.setCellStyle(cellStyleMap.get("header.green"));
			}
			// 零件名称。
			{
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_LJMC,
						Integer.class);
				sheet.addMergedRegion(new CellRangeAddress(firstHeader, firstHeader + 1, column, column));
				Cell cell_1 = sheet.getRow(firstHeader).createCell(column);
				cell_1.setCellStyle(cellStyleMap.get("header.pink"));
				cell_1.setCellValue(zzxm.getI18n(I18nStringKey.XLS_5));
				Cell cell_2 = sheet.getRow(firstHeader + 1).createCell(column);
				cell_2.setCellStyle(cellStyleMap.get("header.pink"));
			}
			// 材质。
			{
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_CZ,
						Integer.class);
				sheet.addMergedRegion(new CellRangeAddress(firstHeader, firstHeader + 1, column, column));
				Cell cell_1 = sheet.getRow(firstHeader).createCell(column);
				cell_1.setCellStyle(cellStyleMap.get("header.pink"));
				cell_1.setCellValue(zzxm.getI18n(I18nStringKey.XLS_6));
				Cell cell_2 = sheet.getRow(firstHeader + 1).createCell(column);
				cell_2.setCellStyle(cellStyleMap.get("header.pink"));
			}
			// 单需。
			{
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_DX,
						Integer.class);
				sheet.addMergedRegion(new CellRangeAddress(firstHeader, firstHeader + 1, column, column));
				Cell cell_1 = sheet.getRow(firstHeader).createCell(column);
				cell_1.setCellStyle(cellStyleMap.get("header.pink"));
				cell_1.setCellValue(zzxm.getI18n(I18nStringKey.XLS_7));
				Cell cell_2 = sheet.getRow(firstHeader + 1).createCell(column);
				cell_2.setCellStyle(cellStyleMap.get("header.pink"));
			}
			// 单重。
			{
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_DZ,
						Integer.class);
				sheet.addMergedRegion(new CellRangeAddress(firstHeader, firstHeader + 1, column, column));
				Cell cell_1 = sheet.getRow(firstHeader).createCell(column);
				cell_1.setCellStyle(cellStyleMap.get("header.pink"));
				cell_1.setCellValue(zzxm.getI18n(I18nStringKey.XLS_8));
				Cell cell_2 = sheet.getRow(firstHeader + 1).createCell(column);
				cell_2.setCellStyle(cellStyleMap.get("header.pink"));
			}
			// 总重。
			{
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_ZZ,
						Integer.class);
				sheet.addMergedRegion(new CellRangeAddress(firstHeader, firstHeader + 1, column, column));
				Cell cell_1 = sheet.getRow(firstHeader).createCell(column);
				cell_1.setCellStyle(cellStyleMap.get("header.pink"));
				cell_1.setCellValue(zzxm.getI18n(I18nStringKey.XLS_9));
				Cell cell_2 = sheet.getRow(firstHeader + 1).createCell(column);
				cell_2.setCellStyle(cellStyleMap.get("header.pink"));
			}
			// 台数。
			{
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_TS,
						Integer.class);
				sheet.addMergedRegion(new CellRangeAddress(firstHeader, firstHeader + 1, column, column));
				Cell cell_1 = sheet.getRow(firstHeader).createCell(column);
				cell_1.setCellStyle(cellStyleMap.get("header.pink"));
				cell_1.setCellValue(zzxm.getI18n(I18nStringKey.XLS_10));
				Cell cell_2 = sheet.getRow(firstHeader + 1).createCell(column);
				cell_2.setCellStyle(cellStyleMap.get("header.pink"));
			}
			// 总数。
			{
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_ZS,
						Integer.class);
				sheet.addMergedRegion(new CellRangeAddress(firstHeader, firstHeader + 1, column, column));
				Cell cell_1 = sheet.getRow(firstHeader).createCell(column);
				cell_1.setCellStyle(cellStyleMap.get("header.pink"));
				cell_1.setCellValue(zzxm.getI18n(I18nStringKey.XLS_11));
				Cell cell_2 = sheet.getRow(firstHeader + 1).createCell(column);
				cell_2.setCellStyle(cellStyleMap.get("header.pink"));
			}
			// 下图日期。
			{
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_XTRQ,
						Integer.class);
				sheet.addMergedRegion(new CellRangeAddress(firstHeader, firstHeader + 1, column, column));
				Cell cell_1 = sheet.getRow(firstHeader).createCell(column);
				cell_1.setCellStyle(cellStyleMap.get("header.pink"));
				cell_1.setCellValue(zzxm.getI18n(I18nStringKey.XLS_12));
				Cell cell_2 = sheet.getRow(firstHeader + 1).createCell(column);
				cell_2.setCellStyle(cellStyleMap.get("header.pink"));
			}
			// 配套日期。
			{
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_PTRQ,
						Integer.class);
				sheet.addMergedRegion(new CellRangeAddress(firstHeader, firstHeader + 1, column, column));
				Cell cell_1 = sheet.getRow(firstHeader).createCell(column);
				cell_1.setCellStyle(cellStyleMap.get("header.pink"));
				cell_1.setCellValue(zzxm.getI18n(I18nStringKey.XLS_13));
				Cell cell_2 = sheet.getRow(firstHeader + 1).createCell(column);
				cell_2.setCellStyle(cellStyleMap.get("header.pink"));
			}
			// 备料-类别。
			{
				final int column = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BL_LB,
						Integer.class);
				Cell cell_1 = sheet.getRow(firstHeader).createCell(column);
				cell_1.setCellStyle(cellStyleMap.get("header.pink"));
				cell_1.setCellValue(zzxm.getI18n(I18nStringKey.XLS_14));
				Cell cell_2 = sheet.getRow(firstHeader + 1).createCell(column);
				cell_2.setCellStyle(cellStyleMap.get("header.pink"));
				cell_2.setCellValue(zzxm.getI18n(I18nStringKey.XLS_15));
			}

			// 四次工序。
			{
				int column_wtf = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_SCGX_WTF,
						Integer.class);
				int column_sl = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_SCGX_SL,
						Integer.class);
				int column_jgnr = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_SCGX_JGNR,
						Integer.class);
				int column_dj = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_SCGX_DJ,
						Integer.class);
				int column_kdrq = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_SCGX_KDRQ,
						Integer.class);
				int column_rkrq = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_SCGX_RKRQ,
						Integer.class);
				int column_rks = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_SCGX_RKS,
						Integer.class);
				int column_qj = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_SCGX_QJ,
						Integer.class);
				int column_xh = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_SCGX_XH,
						Integer.class);
				int column_bz = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_SCGX_BZ,
						Integer.class);
				int column_jyy = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_SCGX_JYY,
						Integer.class);
				int column_czz = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_SCGX_CZZ,
						Integer.class);

				boolean continuous = checkColumnContinuous(
						new int[] { column_wtf, column_sl, column_jgnr, column_dj, column_kdrq, column_rkrq, column_rks,
								column_qj, column_xh, column_bz, column_jyy, column_czz });

				String headerLabel = zzxm.getI18n(I18nStringKey.XLS_16);

				if (continuous) {
					sheet.addMergedRegion(new CellRangeAddress(firstHeader, firstHeader, column_wtf, column_czz));
					Cell cell = sheet.getRow(firstHeader).createCell(column_wtf);
					cell.setCellValue(headerLabel);
					cell.setCellStyle(cellStyleMap.get("header.yellow"));
					sheet.getRow(firstHeader).createCell(column_sl).setCellStyle(cellStyleMap.get("header.yellow"));
					sheet.getRow(firstHeader).createCell(column_jgnr).setCellStyle(cellStyleMap.get("header.yellow"));
					sheet.getRow(firstHeader).createCell(column_dj).setCellStyle(cellStyleMap.get("header.yellow"));
					sheet.getRow(firstHeader).createCell(column_kdrq).setCellStyle(cellStyleMap.get("header.yellow"));
					sheet.getRow(firstHeader).createCell(column_rkrq).setCellStyle(cellStyleMap.get("header.yellow"));
					sheet.getRow(firstHeader).createCell(column_rks).setCellStyle(cellStyleMap.get("header.yellow"));
					sheet.getRow(firstHeader).createCell(column_qj).setCellStyle(cellStyleMap.get("header.yellow"));
					sheet.getRow(firstHeader).createCell(column_xh).setCellStyle(cellStyleMap.get("header.yellow"));
					sheet.getRow(firstHeader).createCell(column_bz).setCellStyle(cellStyleMap.get("header.yellow"));
					sheet.getRow(firstHeader).createCell(column_jyy).setCellStyle(cellStyleMap.get("header.yellow"));
					sheet.getRow(firstHeader).createCell(column_czz).setCellStyle(cellStyleMap.get("header.yellow"));
				}

				// 委托方。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_wtf);
						headerCell.setCellStyle(cellStyleMap.get("header.yellow"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_wtf);
					cell.setCellStyle(cellStyleMap.get("header.yellow"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_17));
				}
				// 数量。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_sl);
						headerCell.setCellStyle(cellStyleMap.get("header.yellow"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_sl);
					cell.setCellStyle(cellStyleMap.get("header.yellow"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_18));
				}
				// 加工内容。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_jgnr);
						headerCell.setCellStyle(cellStyleMap.get("header.yellow"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_jgnr);
					cell.setCellStyle(cellStyleMap.get("header.yellow"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_19));
				}
				// 单价。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_dj);
						headerCell.setCellStyle(cellStyleMap.get("header.yellow"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_dj);
					cell.setCellStyle(cellStyleMap.get("header.yellow"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_20));
				}
				// 开单日期。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_kdrq);
						headerCell.setCellStyle(cellStyleMap.get("header.yellow"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_kdrq);
					cell.setCellStyle(cellStyleMap.get("header.yellow"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_21));
				}
				// 入库日期。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_rkrq);
						headerCell.setCellStyle(cellStyleMap.get("header.yellow"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_rkrq);
					cell.setCellStyle(cellStyleMap.get("header.yellow"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_22));
				}
				// 入库数。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_rks);
						headerCell.setCellStyle(cellStyleMap.get("header.yellow"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_rks);
					cell.setCellStyle(cellStyleMap.get("header.yellow"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_23));
				}
				// 缺件。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_qj);
						headerCell.setCellStyle(cellStyleMap.get("header.yellow"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_qj);
					cell.setCellStyle(cellStyleMap.get("header.yellow"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_24));
				}
				// 序号。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_xh);
						headerCell.setCellStyle(cellStyleMap.get("header.yellow"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_xh);
					cell.setCellStyle(cellStyleMap.get("header.yellow"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_25));
				}
				// 备注。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_bz);
						headerCell.setCellStyle(cellStyleMap.get("header.yellow"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_bz);
					cell.setCellStyle(cellStyleMap.get("header.yellow"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_26));
				}
				// 质检员。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_jyy);
						headerCell.setCellStyle(cellStyleMap.get("header.yellow"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_jyy);
					cell.setCellStyle(cellStyleMap.get("header.yellow"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_27));
				}
				// 操作者。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_czz);
						headerCell.setCellStyle(cellStyleMap.get("header.yellow"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_czz);
					cell.setCellStyle(cellStyleMap.get("header.yellow"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_28));
				}
			}

			// 七次工序。
			{
				int column_wtf = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_QCGX_WTF,
						Integer.class);
				int column_sl = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_QCGX_SL,
						Integer.class);
				int column_jgnr = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_QCGX_JGNR,
						Integer.class);
				int column_dj = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_QCGX_DJ,
						Integer.class);
				int column_kdrq = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_QCGX_KDRQ,
						Integer.class);
				int column_rkrq = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_QCGX_RKRQ,
						Integer.class);
				int column_rks = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_QCGX_RKS,
						Integer.class);
				int column_qj = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_QCGX_QJ,
						Integer.class);
				int column_xh = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_QCGX_XH,
						Integer.class);
				int column_bz = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_QCGX_BZ,
						Integer.class);
				int column_jyy = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_QCGX_JYY,
						Integer.class);
				int column_czz = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_QCGX_CZZ,
						Integer.class);

				boolean continuous = checkColumnContinuous(
						new int[] { column_wtf, column_sl, column_jgnr, column_dj, column_kdrq, column_rkrq, column_rks,
								column_qj, column_xh, column_bz, column_jyy, column_czz });

				String headerLabel = zzxm.getI18n(I18nStringKey.XLS_29);

				if (continuous) {
					sheet.addMergedRegion(new CellRangeAddress(firstHeader, firstHeader, column_wtf, column_czz));
					Cell cell = sheet.getRow(firstHeader).createCell(column_wtf);
					cell.setCellValue(headerLabel);
					cell.setCellStyle(cellStyleMap.get("header.green"));
					sheet.getRow(firstHeader).createCell(column_sl).setCellStyle(cellStyleMap.get("header.green"));
					sheet.getRow(firstHeader).createCell(column_jgnr).setCellStyle(cellStyleMap.get("header.green"));
					sheet.getRow(firstHeader).createCell(column_dj).setCellStyle(cellStyleMap.get("header.green"));
					sheet.getRow(firstHeader).createCell(column_kdrq).setCellStyle(cellStyleMap.get("header.green"));
					sheet.getRow(firstHeader).createCell(column_rkrq).setCellStyle(cellStyleMap.get("header.green"));
					sheet.getRow(firstHeader).createCell(column_rks).setCellStyle(cellStyleMap.get("header.green"));
					sheet.getRow(firstHeader).createCell(column_qj).setCellStyle(cellStyleMap.get("header.green"));
					sheet.getRow(firstHeader).createCell(column_xh).setCellStyle(cellStyleMap.get("header.green"));
					sheet.getRow(firstHeader).createCell(column_bz).setCellStyle(cellStyleMap.get("header.green"));
					sheet.getRow(firstHeader).createCell(column_jyy).setCellStyle(cellStyleMap.get("header.green"));
					sheet.getRow(firstHeader).createCell(column_czz).setCellStyle(cellStyleMap.get("header.green"));
				}

				// 委托方。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_wtf);
						headerCell.setCellStyle(cellStyleMap.get("header.green"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_wtf);
					cell.setCellStyle(cellStyleMap.get("header.green"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_30));
				}
				// 数量。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_sl);
						headerCell.setCellStyle(cellStyleMap.get("header.green"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_sl);
					cell.setCellStyle(cellStyleMap.get("header.green"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_31));
				}
				// 加工内容。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_jgnr);
						headerCell.setCellStyle(cellStyleMap.get("header.green"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_jgnr);
					cell.setCellStyle(cellStyleMap.get("header.green"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_32));
				}
				// 单价。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_dj);
						headerCell.setCellStyle(cellStyleMap.get("header.green"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_dj);
					cell.setCellStyle(cellStyleMap.get("header.green"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_33));
				}
				// 开单日期。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_kdrq);
						headerCell.setCellStyle(cellStyleMap.get("header.green"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_kdrq);
					cell.setCellStyle(cellStyleMap.get("header.green"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_34));
				}
				// 入库日期。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_rkrq);
						headerCell.setCellStyle(cellStyleMap.get("header.green"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_rkrq);
					cell.setCellStyle(cellStyleMap.get("header.green"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_35));
				}
				// 入库数。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_rks);
						headerCell.setCellStyle(cellStyleMap.get("header.green"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_rks);
					cell.setCellStyle(cellStyleMap.get("header.green"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_36));
				}
				// 缺件。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_qj);
						headerCell.setCellStyle(cellStyleMap.get("header.green"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_qj);
					cell.setCellStyle(cellStyleMap.get("header.green"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_37));
				}
				// 序号。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_xh);
						headerCell.setCellStyle(cellStyleMap.get("header.green"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_xh);
					cell.setCellStyle(cellStyleMap.get("header.green"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_38));
				}
				// 备注。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_bz);
						headerCell.setCellStyle(cellStyleMap.get("header.green"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_bz);
					cell.setCellStyle(cellStyleMap.get("header.green"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_39));
				}
				// 质检员。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_jyy);
						headerCell.setCellStyle(cellStyleMap.get("header.green"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_jyy);
					cell.setCellStyle(cellStyleMap.get("header.green"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_40));
				}
				// 操作者。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_czz);
						headerCell.setCellStyle(cellStyleMap.get("header.green"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_czz);
					cell.setCellStyle(cellStyleMap.get("header.green"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_41));
				}
			}

			// 八次工序。
			{
				int column_wtf = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BCGX_WTF,
						Integer.class);
				int column_sl = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BCGX_SL,
						Integer.class);
				int column_jgnr = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BCGX_JGNR,
						Integer.class);
				int column_dj = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BCGX_DJ,
						Integer.class);
				int column_kdrq = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BCGX_KDRQ,
						Integer.class);
				int column_rkrq = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BCGX_RKRQ,
						Integer.class);
				int column_rks = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BCGX_RKS,
						Integer.class);
				int column_qj = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BCGX_QJ,
						Integer.class);
				int column_xh = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BCGX_XH,
						Integer.class);
				int column_bz = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BCGX_BZ,
						Integer.class);
				int column_jyy = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BCGX_JYY,
						Integer.class);
				int column_czz = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_BCGX_CZZ,
						Integer.class);

				boolean continuous = checkColumnContinuous(
						new int[] { column_wtf, column_sl, column_jgnr, column_dj, column_kdrq, column_rkrq, column_rks,
								column_qj, column_xh, column_bz, column_jyy, column_czz });

				String headerLabel = zzxm.getI18n(I18nStringKey.XLS_42);

				if (continuous) {
					sheet.addMergedRegion(new CellRangeAddress(firstHeader, firstHeader, column_wtf, column_czz));
					Cell cell = sheet.getRow(firstHeader).createCell(column_wtf);
					cell.setCellValue(headerLabel);
					cell.setCellStyle(cellStyleMap.get("header.pink"));
					sheet.getRow(firstHeader).createCell(column_sl).setCellStyle(cellStyleMap.get("header.pink"));
					sheet.getRow(firstHeader).createCell(column_jgnr).setCellStyle(cellStyleMap.get("header.pink"));
					sheet.getRow(firstHeader).createCell(column_dj).setCellStyle(cellStyleMap.get("header.pink"));
					sheet.getRow(firstHeader).createCell(column_kdrq).setCellStyle(cellStyleMap.get("header.pink"));
					sheet.getRow(firstHeader).createCell(column_rkrq).setCellStyle(cellStyleMap.get("header.pink"));
					sheet.getRow(firstHeader).createCell(column_rks).setCellStyle(cellStyleMap.get("header.pink"));
					sheet.getRow(firstHeader).createCell(column_qj).setCellStyle(cellStyleMap.get("header.pink"));
					sheet.getRow(firstHeader).createCell(column_xh).setCellStyle(cellStyleMap.get("header.pink"));
					sheet.getRow(firstHeader).createCell(column_bz).setCellStyle(cellStyleMap.get("header.pink"));
					sheet.getRow(firstHeader).createCell(column_jyy).setCellStyle(cellStyleMap.get("header.pink"));
					sheet.getRow(firstHeader).createCell(column_czz).setCellStyle(cellStyleMap.get("header.pink"));
				}

				// 委托方。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_wtf);
						headerCell.setCellStyle(cellStyleMap.get("header.pink"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_wtf);
					cell.setCellStyle(cellStyleMap.get("header.pink"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_43));
				}
				// 数量。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_sl);
						headerCell.setCellStyle(cellStyleMap.get("header.pink"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_sl);
					cell.setCellStyle(cellStyleMap.get("header.pink"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_44));
				}
				// 加工内容。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_jgnr);
						headerCell.setCellStyle(cellStyleMap.get("header.pink"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_jgnr);
					cell.setCellStyle(cellStyleMap.get("header.pink"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_45));
				}
				// 单价。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_dj);
						headerCell.setCellStyle(cellStyleMap.get("header.pink"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_dj);
					cell.setCellStyle(cellStyleMap.get("header.pink"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_46));
				}
				// 开单日期。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_kdrq);
						headerCell.setCellStyle(cellStyleMap.get("header.pink"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_kdrq);
					cell.setCellStyle(cellStyleMap.get("header.pink"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_47));
				}
				// 入库日期。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_rkrq);
						headerCell.setCellStyle(cellStyleMap.get("header.pink"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_rkrq);
					cell.setCellStyle(cellStyleMap.get("header.pink"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_48));
				}
				// 入库数。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_rks);
						headerCell.setCellStyle(cellStyleMap.get("header.pink"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_rks);
					cell.setCellStyle(cellStyleMap.get("header.pink"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_49));
				}
				// 缺件。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_qj);
						headerCell.setCellStyle(cellStyleMap.get("header.pink"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_qj);
					cell.setCellStyle(cellStyleMap.get("header.pink"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_50));
				}
				// 序号。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_xh);
						headerCell.setCellStyle(cellStyleMap.get("header.pink"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_xh);
					cell.setCellStyle(cellStyleMap.get("header.pink"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_51));
				}
				// 备注。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_bz);
						headerCell.setCellStyle(cellStyleMap.get("header.pink"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_bz);
					cell.setCellStyle(cellStyleMap.get("header.pink"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_52));
				}
				// 质检员。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_jyy);
						headerCell.setCellStyle(cellStyleMap.get("header.pink"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_jyy);
					cell.setCellStyle(cellStyleMap.get("header.pink"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_53));
				}
				// 操作者。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_czz);
						headerCell.setCellStyle(cellStyleMap.get("header.pink"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_czz);
					cell.setCellStyle(cellStyleMap.get("header.pink"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_54));
				}
			}

			// 九次工序。
			{
				int column_wtf = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_JCGX_WTF,
						Integer.class);
				int column_sl = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_JCGX_SL,
						Integer.class);
				int column_jgnr = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_JCGX_JGNR,
						Integer.class);
				int column_dj = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_JCGX_DJ,
						Integer.class);
				int column_kdrq = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_JCGX_KDRQ,
						Integer.class);
				int column_rkrq = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_JCGX_RKRQ,
						Integer.class);
				int column_rks = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_JCGX_RKS,
						Integer.class);
				int column_qj = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_JCGX_QJ,
						Integer.class);
				int column_xh = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_JCGX_XH,
						Integer.class);
				int column_bz = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_JCGX_BZ,
						Integer.class);
				int column_jyy = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_JCGX_JYY,
						Integer.class);
				int column_czz = zzxm.configHandler.getParsedValidValue(ConfigItem.EXPTABLE_INDEX_COLUMN_JCGX_CZZ,
						Integer.class);

				boolean continuous = checkColumnContinuous(
						new int[] { column_wtf, column_sl, column_jgnr, column_dj, column_kdrq, column_rkrq, column_rks,
								column_qj, column_xh, column_bz, column_jyy, column_czz });

				String headerLabel = zzxm.getI18n(I18nStringKey.XLS_55);

				if (continuous) {
					sheet.addMergedRegion(new CellRangeAddress(firstHeader, firstHeader, column_wtf, column_czz));
					Cell cell = sheet.getRow(firstHeader).createCell(column_wtf);
					cell.setCellValue(headerLabel);
					cell.setCellStyle(cellStyleMap.get("header.blue"));
					sheet.getRow(firstHeader).createCell(column_sl).setCellStyle(cellStyleMap.get("header.blue"));
					sheet.getRow(firstHeader).createCell(column_jgnr).setCellStyle(cellStyleMap.get("header.blue"));
					sheet.getRow(firstHeader).createCell(column_dj).setCellStyle(cellStyleMap.get("header.blue"));
					sheet.getRow(firstHeader).createCell(column_kdrq).setCellStyle(cellStyleMap.get("header.blue"));
					sheet.getRow(firstHeader).createCell(column_rkrq).setCellStyle(cellStyleMap.get("header.blue"));
					sheet.getRow(firstHeader).createCell(column_rks).setCellStyle(cellStyleMap.get("header.blue"));
					sheet.getRow(firstHeader).createCell(column_qj).setCellStyle(cellStyleMap.get("header.blue"));
					sheet.getRow(firstHeader).createCell(column_xh).setCellStyle(cellStyleMap.get("header.blue"));
					sheet.getRow(firstHeader).createCell(column_bz).setCellStyle(cellStyleMap.get("header.blue"));
					sheet.getRow(firstHeader).createCell(column_jyy).setCellStyle(cellStyleMap.get("header.blue"));
					sheet.getRow(firstHeader).createCell(column_czz).setCellStyle(cellStyleMap.get("header.blue"));
				}

				// 委托方。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_wtf);
						headerCell.setCellStyle(cellStyleMap.get("header.blue"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_wtf);
					cell.setCellStyle(cellStyleMap.get("header.blue"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_56));
				}
				// 数量。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_sl);
						headerCell.setCellStyle(cellStyleMap.get("header.blue"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_sl);
					cell.setCellStyle(cellStyleMap.get("header.blue"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_57));
				}
				// 加工内容。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_jgnr);
						headerCell.setCellStyle(cellStyleMap.get("header.blue"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_jgnr);
					cell.setCellStyle(cellStyleMap.get("header.blue"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_58));
				}
				// 单价。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_dj);
						headerCell.setCellStyle(cellStyleMap.get("header.blue"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_dj);
					cell.setCellStyle(cellStyleMap.get("header.blue"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_59));
				}
				// 开单日期。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_kdrq);
						headerCell.setCellStyle(cellStyleMap.get("header.blue"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_kdrq);
					cell.setCellStyle(cellStyleMap.get("header.blue"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_60));
				}
				// 入库日期。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_rkrq);
						headerCell.setCellStyle(cellStyleMap.get("header.blue"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_rkrq);
					cell.setCellStyle(cellStyleMap.get("header.blue"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_61));
				}
				// 入库数。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_rks);
						headerCell.setCellStyle(cellStyleMap.get("header.blue"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_rks);
					cell.setCellStyle(cellStyleMap.get("header.blue"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_62));
				}
				// 缺件。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_qj);
						headerCell.setCellStyle(cellStyleMap.get("header.blue"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_qj);
					cell.setCellStyle(cellStyleMap.get("header.blue"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_63));
				}
				// 序号。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_xh);
						headerCell.setCellStyle(cellStyleMap.get("header.blue"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_xh);
					cell.setCellStyle(cellStyleMap.get("header.blue"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_64));
				}
				// 备注。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_bz);
						headerCell.setCellStyle(cellStyleMap.get("header.blue"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_bz);
					cell.setCellStyle(cellStyleMap.get("header.blue"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_65));
				}
				// 质检员。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_jyy);
						headerCell.setCellStyle(cellStyleMap.get("header.blue"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_jyy);
					cell.setCellStyle(cellStyleMap.get("header.blue"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_66));
				}
				// 操作者。
				{
					if (!continuous) {
						Cell headerCell = sheet.getRow(firstHeader).createCell(column_czz);
						headerCell.setCellStyle(cellStyleMap.get("header.blue"));
						headerCell.setCellValue(headerLabel);
					}
					Cell cell = sheet.getRow(firstHeader + 1).createCell(column_czz);
					cell.setCellStyle(cellStyleMap.get("header.blue"));
					cell.setCellValue(zzxm.getI18n(I18nStringKey.XLS_67));
				}
			}

		}

		private boolean checkColumnContinuous(int[] indexes) {
			if (indexes.length == 0 || indexes.length == 1)
				return true;
			int col = indexes[0];
			for (int i = 1; i < indexes.length; i++) {
				int nextCol = indexes[i];
				if (nextCol != col + i)
					return false;
			}
			return true;
		}

	}

}
