package com.jneagle.xlstools.zzxm.core.control;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.poi.hssf.usermodel.HSSFDataFormatter;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import com.dwarfeng.dutil.basic.gui.swing.SwingUtil;
import com.dwarfeng.dutil.basic.io.LoadFailedException;
import com.dwarfeng.dutil.basic.io.StreamLoader;
import com.dwarfeng.dutil.basic.mea.TimeMeasurer;
import com.jneagle.xlstools.zzxm.core.model.cm.SyncMachiningModel;
import com.jneagle.xlstools.zzxm.core.model.eum.ConfigItem;
import com.jneagle.xlstools.zzxm.core.model.eum.I18nStringKey;
import com.jneagle.xlstools.zzxm.core.model.eum.ModalItem;
import com.jneagle.xlstools.zzxm.core.model.eum.ProgressState;
import com.jneagle.xlstools.zzxm.core.model.struct.AbstractDataContext;
import com.jneagle.xlstools.zzxm.core.model.struct.CellProcessException;
import com.jneagle.xlstools.zzxm.core.model.struct.DataContext;
import com.jneagle.xlstools.zzxm.core.model.struct.DataContext.DataType;
import com.jneagle.xlstools.zzxm.core.model.struct.MachiningInfo;

class LoadFileTask extends AbstractZZXMTask {

	public LoadFileTask(ZZXM zzxm) {
		super(zzxm);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void todo() throws Exception {
		// 判断选择文件是否为 null。
		File file = zzxm.excelLoadFileModel.get();
		if (Objects.isNull(file)) {
			JOptionPane.showMessageDialog(zzxm.mainFrameModel.get(), zzxm.getI18n(I18nStringKey.LABEL_7),
					zzxm.getI18n(I18nStringKey.LABEL_8), JOptionPane.WARNING_MESSAGE);
			return;
		}

		// 设置进度。
		{
			zzxm.loadStateModel.set(ProgressState.STARTED);
			zzxm.filterStateModel.set(ProgressState.NOT_START);
			zzxm.exportStateModel.set(ProgressState.NOT_START);
		}

		// 临时记录密码并清除模型密码。
		String password = Optional.ofNullable(zzxm.excelLoadPasswordModel.get()).orElse("");
		zzxm.excelLoadPasswordModel.set("");

		// 读取文件信息持久化。
		zzxm.modalHandler.setParsedValue(ModalItem.PERSISTENCE_LAST_LOAD_FILE, file);
		zzxm.modalHandler.setParsedValue(ModalItem.PERSISTENCE_LAST_LOAD_FLAG, true);

		// 记录日志。
		zzxm.loggerHandler.info(zzxm.getI18n(I18nStringKey.LOGGER_25));
		// 定义计时器。
		TimeMeasurer tm = new TimeMeasurer();
		// 设置进度条模型。
		zzxm.currentProgressModel.set(0);
		zzxm.totleProgressModel.set(-1);
		// 设置标签并显示进度条。
		SwingUtil.invokeInEventQueue(() -> {
			zzxm.mainFrameModel.get().notification(zzxm.getI18n(I18nStringKey.LABEL_16));
			zzxm.mainFrameModel.get().setProgressPanelVisible(true);
		});
		// 开始计时。
		tm.start();
		// 清除有关模型。
		{
			zzxm.machiningModel.clear();
			zzxm.filteredMachiningModel.clear();
			zzxm.failedMachiningModel.clear();
		}
		// 存放读取异常的对象。
		Set<LoadFailedException> exceptions = new LinkedHashSet<>();
		// 读取流程。
		{
			try (XlsMachiningLoader loader = new XlsMachiningLoader(new FileInputStream(file), password)) {
				exceptions.addAll(loader.countinuousLoad(zzxm.machiningModel));
			}
			// 密码错误异常处理。
			catch (org.apache.poi.EncryptedDocumentException e) {
				Integer passwordFailedTimes = zzxm.modalHandler
						.getParsedValidValue(ModalItem.PERSISTENCE_PASSWORD_FAILED, Integer.class);
				zzxm.modalHandler.setParsedValue(ModalItem.PERSISTENCE_PASSWORD_FAILED, ++passwordFailedTimes);
				zzxm.loggerHandler.error(String.format(zzxm.getI18n(I18nStringKey.LOGGER_17), passwordFailedTimes), e);
				zzxm.loadStateModel.set(ProgressState.NOT_START);
				SwingUtil.invokeInEventQueue(() -> {
					zzxm.mainFrameModel.get().notification(zzxm.getI18n(I18nStringKey.LABEL_13));
					JOptionPane.showMessageDialog(zzxm.mainFrameModel.get(), zzxm.getI18n(I18nStringKey.LABEL_9),
							zzxm.getI18n(I18nStringKey.LABEL_8), JOptionPane.ERROR_MESSAGE);
					zzxm.mainFrameModel.get().setProgressPanelVisible(false);
				});
				return;
			}
			// IO错误异常处理。
			catch (IOException e) {
				zzxm.loggerHandler.error(zzxm.getI18n(I18nStringKey.LOGGER_14), e);
				zzxm.loadStateModel.set(ProgressState.NOT_START);
				SwingUtil.invokeInEventQueue(() -> {
					zzxm.mainFrameModel.get().notification(zzxm.getI18n(I18nStringKey.LABEL_14));
					JOptionPane.showMessageDialog(zzxm.mainFrameModel.get(), zzxm.getI18n(I18nStringKey.LABEL_10),
							zzxm.getI18n(I18nStringKey.LABEL_8), JOptionPane.ERROR_MESSAGE);
					zzxm.mainFrameModel.get().setProgressPanelVisible(false);
				});
				return;
			}
			// 其它异常处理。
			catch (Exception e) {
				zzxm.loggerHandler.error(zzxm.getI18n(I18nStringKey.LOGGER_16), e);
				zzxm.loadStateModel.set(ProgressState.NOT_START);
				SwingUtil.invokeInEventQueue(() -> {
					zzxm.mainFrameModel.get().notification(zzxm.getI18n(I18nStringKey.LABEL_15));
					JOptionPane.showMessageDialog(zzxm.mainFrameModel.get(), zzxm.getI18n(I18nStringKey.LABEL_11),
							zzxm.getI18n(I18nStringKey.LABEL_8), JOptionPane.ERROR_MESSAGE);
					zzxm.mainFrameModel.get().setProgressPanelVisible(false);
				});
				return;
			}
		}
		// 结束计时。
		tm.stop();
		// 设置通知区域文本并关闭进度条。
		SwingUtil.invokeInEventQueue(() -> {
			zzxm.mainFrameModel.get().notification(
					String.format(zzxm.getI18n(I18nStringKey.LABEL_17), tm.getTimeMs(), exceptions.size()));
			zzxm.mainFrameModel.get().setProgressPanelVisible(false);
		});
		// 如果错误大于等于1个，则进行通知。
		if (exceptions.size() >= 1) {
			SwingUtil.invokeInEventQueue(() -> {
				JOptionPane.showMessageDialog(zzxm.mainFrameModel.get(),
						String.format(zzxm.getI18n(I18nStringKey.LABEL_18), exceptions.size()),
						zzxm.getI18n(I18nStringKey.LABEL_8), JOptionPane.WARNING_MESSAGE);
			});
			// 获得最大记录数据。
			int maxLogSize = zzxm.configHandler.getParsedValidValue(ConfigItem.LOG_MAX_EXCEL_LOAD_WARNING,
					Integer.class);
			// 如果异常数大于最大记录数，在日志中通知用户。
			if (exceptions.size() > maxLogSize)
				zzxm.loggerHandler.warn(String.format(zzxm.getI18n(I18nStringKey.LOGGER_23), maxLogSize));
			// 记录日志。
			exceptions.stream().limit(maxLogSize).forEach(exception -> {
				zzxm.loggerHandler.warn(zzxm.getI18n(I18nStringKey.LOGGER_21), exception);
			});
		}
		// 输出总结日志。
		zzxm.loggerHandler.info(String.format(zzxm.getI18n(I18nStringKey.LOGGER_26), tm.getTimeMs(),
				zzxm.machiningModel.size(), zzxm.machiningModel.machiningInfoSize()));
		// 设置读取标记。
		zzxm.loadStateModel.set(ProgressState.FINISHED);
		// 启动过滤任务。
		zzxm.background.submit(new FilterMachiningTask(zzxm));
	}

	private final class XlsMachiningLoader extends StreamLoader<SyncMachiningModel> {

		private Workbook workbook;
		private boolean readFlag = false;

		public XlsMachiningLoader(InputStream in, String password) throws Exception {
			super(in);
			org.apache.poi.hssf.record.crypto.Biff8EncryptionKey
					.setCurrentUserPassword(password.isEmpty() ? null : password);
			workbook = new HSSFWorkbook(in);
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void load(SyncMachiningModel machiningModel) throws LoadFailedException, IllegalStateException {
			if (readFlag)
				throw new IllegalStateException("Load method can be called only once");

			Objects.requireNonNull(machiningModel, "入口参数 machiningModel 不能为 null。");

			try {
				readFlag = true;

				Sheet sheet = getSheet(workbook);
				String filter = zzxm.configHandler.getParsedValidValue(ConfigItem.POLICY_XMH_FILTER, String.class);
				Boolean filterEnabled = zzxm.configHandler.getParsedValue(ConfigItem.POLICY_XMH_FILTER_ENABLED,
						Boolean.class);

				zzxm.currentProgressModel.set(0);
				zzxm.totleProgressModel.set(sheet.getLastRowNum());
				for (int rowIndex = zzxm.configHandler.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_ROW_FIRST_DATA,
						Integer.class); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
					loadRow(machiningModel, sheet.getRow(rowIndex), filter, filterEnabled);
					zzxm.currentProgressModel.set(rowIndex);
				}

			} catch (Exception e) {
				throw new LoadFailedException(zzxm.getI18n(I18nStringKey.LOGGER_21), e);
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Set<LoadFailedException> countinuousLoad(SyncMachiningModel machiningModel)
				throws IllegalStateException {
			if (readFlag)
				throw new IllegalStateException("Load method can be called only once");

			Objects.requireNonNull(machiningModel, "入口参数 machiningModel 不能为 null。");

			final Set<LoadFailedException> exceptions = new LinkedHashSet<>();

			try {
				readFlag = true;

				Sheet sheet = getSheet(workbook);
				String filter = zzxm.configHandler.getParsedValidValue(ConfigItem.POLICY_XMH_FILTER, String.class);
				Boolean filterEnabled = zzxm.configHandler.getParsedValue(ConfigItem.POLICY_XMH_FILTER_ENABLED,
						Boolean.class);

				zzxm.currentProgressModel.set(0);
				zzxm.totleProgressModel.set(sheet.getLastRowNum());
				for (int rowIndex = zzxm.configHandler.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_ROW_FIRST_DATA,
						Integer.class); rowIndex <= sheet.getLastRowNum(); rowIndex++) {
					try {
						loadRow(machiningModel, sheet.getRow(rowIndex), filter, filterEnabled);
					} catch (Exception e) {
						exceptions.add(new LoadFailedException(zzxm.getI18n(I18nStringKey.LOGGER_21), e));
					}
					zzxm.currentProgressModel.set(rowIndex);
				}

			} catch (Exception e) {
				exceptions.add(new LoadFailedException(zzxm.getI18n(I18nStringKey.LOGGER_21), e));
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

		/**
		 * 获取源文件统计表单。
		 * 
		 * @param workbook
		 *            指定的工作表。
		 * @return 获取的统计表单。
		 */
		private Sheet getSheet(Workbook workbook) {
			return workbook.getSheetAt(
					zzxm.configHandler.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COUNT_SHEET, Integer.class));
		}

		private void loadRow(SyncMachiningModel machiningModel, Row row, String filter, boolean filterEnabled)
				throws CellProcessException {
			Cell currentCell = null;
			try {
				Map<String, DataContext> properties = new HashMap<>();

				// 读取项目号。
				currentCell = makeCell(row,
						zzxm.configHandler.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_XMH, Integer.class));
				DataContext dataContext_xmh = getDataContextAt(currentCell, RowLoadingPolicy.NORMAL);
				// 过滤项目号。
				String xmh = dataContext_xmh.getValue(DataType.STRING, String.class);
				if (filterEnabled && !xmh.matches(filter)) {
					zzxm.loggerHandler.info(String.format(zzxm.getI18n(I18nStringKey.LOGGER_24), xmh));
					return;
				}

				// 项目号。
				properties.put("xmh", dataContext_xmh);

				currentCell = makeCell(row,
						zzxm.configHandler.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_BJH, Integer.class));
				properties.put("bjh", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row,
						zzxm.configHandler.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_LJH, Integer.class));
				properties.put("ljh", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row,
						zzxm.configHandler.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_LJMC, Integer.class));
				properties.put("ljmc", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row,
						zzxm.configHandler.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_CZ, Integer.class));
				properties.put("cz", getDataContextAt(currentCell, RowLoadingPolicy.NUM2STRING));
				currentCell = makeCell(row,
						zzxm.configHandler.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_DX, Integer.class));
				properties.put("dx", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row,
						zzxm.configHandler.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_DZ, Integer.class));
				properties.put("dz", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row,
						zzxm.configHandler.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_ZZ, Integer.class));
				properties.put("zz", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row,
						zzxm.configHandler.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_TS, Integer.class));
				properties.put("ts", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row,
						zzxm.configHandler.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_ZS, Integer.class));
				properties.put("zs", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row,
						zzxm.configHandler.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_XTRQ, Integer.class));
				properties.put("xtrq", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row,
						zzxm.configHandler.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_PTRQ, Integer.class));
				properties.put("ptrq", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row,
						zzxm.configHandler.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_BL_LB, Integer.class));
				properties.put("bl-lb", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));

				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_SCGX_WTF, Integer.class));
				properties.put("scgx.wtf", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_SCGX_SL, Integer.class));
				properties.put("scgx.sl", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_SCGX_JGNR, Integer.class));
				properties.put("scgx.jgnr", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_SCGX_DJ, Integer.class));
				properties.put("scgx.dj", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_SCGX_KDRQ, Integer.class));
				properties.put("scgx.kdrq", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_SCGX_RKRQ, Integer.class));
				properties.put("scgx.rkrq", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_SCGX_RKS, Integer.class));
				properties.put("scgx.rks", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_SCGX_QJ, Integer.class));
				properties.put("scgx.qj", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_SCGX_XH, Integer.class));
				properties.put("scgx.xh", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_SCGX_BZ, Integer.class));
				properties.put("scgx.bz", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_SCGX_JYY, Integer.class));
				properties.put("scgx.jyy", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_SCGX_CZZ, Integer.class));
				properties.put("scgx.czz", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));

				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_QCGX_WTF, Integer.class));
				properties.put("qcgx.wtf", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_QCGX_SL, Integer.class));
				properties.put("qcgx.sl", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_QCGX_JGNR, Integer.class));
				properties.put("qcgx.jgnr", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_QCGX_DJ, Integer.class));
				properties.put("qcgx.dj", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_QCGX_KDRQ, Integer.class));
				properties.put("qcgx.kdrq", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_QCGX_RKRQ, Integer.class));
				properties.put("qcgx.rkrq", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_QCGX_RKS, Integer.class));
				properties.put("qcgx.rks", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_QCGX_QJ, Integer.class));
				properties.put("qcgx.qj", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_QCGX_XH, Integer.class));
				properties.put("qcgx.xh", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_QCGX_BZ, Integer.class));
				properties.put("qcgx.bz", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_QCGX_JYY, Integer.class));
				properties.put("qcgx.jyy", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_QCGX_CZZ, Integer.class));
				properties.put("qcgx.czz", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));

				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_BCGX_WTF, Integer.class));
				properties.put("bcgx.wtf", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_BCGX_SL, Integer.class));
				properties.put("bcgx.sl", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_BCGX_JGNR, Integer.class));
				properties.put("bcgx.jgnr", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_BCGX_DJ, Integer.class));
				properties.put("bcgx.dj", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_BCGX_KDRQ, Integer.class));
				properties.put("bcgx.kdrq", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_BCGX_RKRQ, Integer.class));
				properties.put("bcgx.rkrq", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_BCGX_RKS, Integer.class));
				properties.put("bcgx.rks", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_BCGX_QJ, Integer.class));
				properties.put("bcgx.qj", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_BCGX_XH, Integer.class));
				properties.put("bcgx.xh", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_BCGX_BZ, Integer.class));
				properties.put("bcgx.bz", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_BCGX_JYY, Integer.class));
				properties.put("bcgx.jyy", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_BCGX_CZZ, Integer.class));
				properties.put("bcgx.czz", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));

				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_JCGX_WTF, Integer.class));
				properties.put("jcgx.wtf", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_JCGX_SL, Integer.class));
				properties.put("jcgx.sl", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_JCGX_JGNR, Integer.class));
				properties.put("jcgx.jgnr", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_JCGX_DJ, Integer.class));
				properties.put("jcgx.dj", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_JCGX_KDRQ, Integer.class));
				properties.put("jcgx.kdrq", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_JCGX_RKRQ, Integer.class));
				properties.put("jcgx.rkrq", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_JCGX_RKS, Integer.class));
				properties.put("jcgx.rks", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_JCGX_QJ, Integer.class));
				properties.put("jcgx.qj", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_JCGX_XH, Integer.class));
				properties.put("jcgx.xh", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_JCGX_BZ, Integer.class));
				properties.put("jcgx.bz", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_JCGX_JYY, Integer.class));
				properties.put("jcgx.jyy", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));
				currentCell = makeCell(row, zzxm.configHandler
						.getParsedValidValue(ConfigItem.SRCTABLE_INDEX_COLUMN_JCGX_CZZ, Integer.class));
				properties.put("jcgx.czz", getDataContextAt(currentCell, RowLoadingPolicy.NORMAL));

				// 将加工信息按照目录式添加进加工信息模型中。
				machiningModel.getLock().writeLock().lock();
				try {
					machiningModel.addMachiningInfo(xmh, new MachiningInfo(properties));
				} finally {
					machiningModel.getLock().writeLock().unlock();
				}
			} catch (Exception e) {
				throw new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_13), e);
			}
		}

		private DataContext getDataContextAt(Cell cell, RowLoadingPolicy policy) {
			switch (policy) {
			case NUM2STRING:
				if (cell.getCellTypeEnum() == CellType.NUMERIC) {
					String cellValue = new HSSFDataFormatter().formatCellValue(cell);
					cell.setCellValue(cellValue);
				}
				break;
			default:
				break;
			}
			return new CellDataContext(cell);
		}

		private Cell makeCell(Row row, int index) {
			return Optional.ofNullable(row.getCell(index)).orElse(row.createCell(index));
		}
	}

	private static class CellDataContext extends AbstractDataContext {

		private final Cell cell;

		public CellDataContext(Cell cell) {
			this.cell = cell;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public boolean isSuitable(DataType dataType) throws NullPointerException {
			CellType cellType = cell.getCellTypeEnum();
			switch (dataType) {
			case BOOLEAN:
				if (cellType == CellType.BOOLEAN || cellType == CellType.BLANK) {
					return true;
				} else if (cellType == CellType.FORMULA) {
					return cell.getCachedFormulaResultTypeEnum() == CellType.BOOLEAN;
				} else {
					return false;
				}
			case DATE:
				if (cellType == CellType.NUMERIC || cellType == CellType.BLANK) {
					return true;
				} else if (cellType == CellType.FORMULA) {
					return cell.getCachedFormulaResultTypeEnum() == CellType.NUMERIC;
				} else {
					return false;
				}
			case FORMULA:
				if (cellType == CellType.FORMULA) {
					return true;
				} else {
					return false;
				}
			case NUMBER:
				if (cellType == CellType.NUMERIC || cellType == CellType.BLANK) {
					return true;
				} else if (cellType == CellType.FORMULA) {
					return cell.getCachedFormulaResultTypeEnum() == CellType.NUMERIC;
				} else {
					return false;
				}
			case STRING:
				if (cellType == CellType.STRING || cellType == CellType.BLANK) {
					return true;
				} else if (cellType == CellType.FORMULA) {
					return cell.getCachedFormulaResultTypeEnum() == CellType.STRING;
				} else {
					return false;
				}
			default:
				return false;
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Object getValue(DataType dataType) throws NullPointerException, IllegalStateException {
			checkSuitable(dataType);
			switch (dataType) {
			case BOOLEAN:
				return cell.getBooleanCellValue();
			case DATE:
				return cell.getDateCellValue();
			case FORMULA:
				return cell.getCellFormula();
			case NUMBER:
				return cell.getNumericCellValue();
			case STRING:
				return cell.getStringCellValue();
			default:
				throw new IllegalStateException(String.format("Illegal data type : %s.", dataType));
			}
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Cell getContextCell() {
			return cell;
		}

	}

	private enum RowLoadingPolicy {
		NORMAL, NUM2STRING
	}

}
