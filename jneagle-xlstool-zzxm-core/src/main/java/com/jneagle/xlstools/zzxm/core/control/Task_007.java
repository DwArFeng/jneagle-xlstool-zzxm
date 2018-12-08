package com.jneagle.xlstools.zzxm.core.control;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

import javax.swing.JOptionPane;

import org.apache.poi.ss.usermodel.Cell;

import com.dwarfeng.dutil.basic.gui.swing.SwingUtil;
import com.dwarfeng.dutil.basic.mea.TimeMeasurer;
import com.dwarfeng.dutil.basic.prog.ProcessException;
import com.jneagle.xlstools.zzxm.core.model.cm.SyncMachiningModel;
import com.jneagle.xlstools.zzxm.core.model.eum.ConfigItem;
import com.jneagle.xlstools.zzxm.core.model.eum.I18nStringKey;
import com.jneagle.xlstools.zzxm.core.model.struct.CellProcessException;
import com.jneagle.xlstools.zzxm.core.model.struct.DataContext;
import com.jneagle.xlstools.zzxm.core.model.struct.DataContext.DataType;
import com.jneagle.xlstools.zzxm.core.model.struct.MachiningInfo;

class FilterMachiningTask extends AbstractZZXMTask {

	public FilterMachiningTask(ZZXM zzxm) {
		super(zzxm);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void todo() throws Exception {
		// 设置进度条模型。
		zzxm.currentProgressModel.set(0);
		zzxm.totleProgressModel.set(-1);
		// 设置通知区域文本并显示进度条。
		SwingUtil.invokeInEventQueue(() -> {
			zzxm.mainFrameModel.get().notification(zzxm.getI18n(I18nStringKey.LABEL_28));
			zzxm.mainFrameModel.get().setProgressPanelVisible(true);
		});
		// 记录日志。
		zzxm.loggerHandler.info(zzxm.getI18n(I18nStringKey.LOGGER_29));

		// 计时器。
		TimeMeasurer tm = new TimeMeasurer();
		tm.start();
		// 定义变量。
		final Set<ProcessException> exceptions = new LinkedHashSet<>();
		// 筛选过程。
		{
			MachiningFilterLoaderProcessor processor = new MachiningFilterLoaderProcessor(zzxm.machiningModel);
			exceptions.addAll(processor.countinuousProcess(
					new FilterModelContext(zzxm.filteredMachiningModel, zzxm.failedMachiningModel)));
		}
		// 计时结束
		tm.stop();
		// 设置通知区域文本并隐藏进度条。
		SwingUtil.invokeInEventQueue(() -> {
			zzxm.mainFrameModel.get().notification(
					String.format(zzxm.getI18n(I18nStringKey.LABEL_29), tm.getTimeMs(), exceptions.size()));
			zzxm.mainFrameModel.get().setProgressPanelVisible(false);
		});

		// 如果发生至少一个异常，则警告用户。
		if (!exceptions.isEmpty()) {
			SwingUtil.invokeInEventQueue(() -> {
				JOptionPane.showMessageDialog(zzxm.mainFrameModel.get(),
						String.format(zzxm.getI18n(I18nStringKey.LABEL_30), exceptions.size()),
						zzxm.getI18n(I18nStringKey.LABEL_8), JOptionPane.WARNING_MESSAGE);
			});
		}

		// 获得最大记录数据。
		int maxLogSize = zzxm.configHandler.getParsedValidValue(ConfigItem.LOG_MAX_EXCEL_FILTER_WARNING, Integer.class);
		// 如果异常数大于最大记录数，在日志中通知用户。
		if (exceptions.size() > maxLogSize)
			zzxm.loggerHandler.warn(String.format(zzxm.getI18n(I18nStringKey.LOGGER_37), maxLogSize));
		// 记录日志。
		exceptions.stream().limit(maxLogSize).forEach(exception -> {
			zzxm.loggerHandler.warn(zzxm.getI18n(I18nStringKey.LOGGER_36), exception);
		});
		// 输出总结日志。
		zzxm.loggerHandler.info(String.format(zzxm.getI18n(I18nStringKey.LOGGER_33), tm.getTimeMs(),
				zzxm.filteredMachiningModel.size(), zzxm.filteredMachiningModel.machiningInfoSize(),
				zzxm.failedMachiningModel.machiningInfoSize()));
	}

	private final class MachiningFilterLoaderProcessor {

		/** 机加工模型。 */
		private final SyncMachiningModel machiningModel;

		/** 机加工筛选器。 */
		private final MachiningInfoFilter filter = new MachiningInfoFilter();

		private boolean readFlag = false;

		public MachiningFilterLoaderProcessor(SyncMachiningModel machiningModel) {
			this.machiningModel = machiningModel;
		}

		public Set<ProcessException> countinuousProcess(FilterModelContext context) throws IllegalStateException {
			if (readFlag)
				throw new IllegalStateException("Load method can be called only once");

			Objects.requireNonNull(context, "入口参数 context 不能为 null。");

			final Set<ProcessException> exceptions = new LinkedHashSet<>();

			try {
				zzxm.currentProgressModel.set(0);
				zzxm.totleProgressModel.set(machiningModel.machiningInfoSize());
				int i = 0;
				for (String xmh : machiningModel.xmhSet()) {
					for (MachiningInfo machiningInfo : machiningModel.getMachiningInfos(xmh)) {
						i++;
						try {
							filterMachiningInfo(xmh, machiningInfo, context.getFilteredMachiningModel(),
									context.getFailedMachiningModel());
						} catch (Exception e) {
							exceptions.add(new ProcessException(zzxm.getI18n(I18nStringKey.LOGGER_12), e));
						}
						zzxm.currentProgressModel.set(i);
					}
				}
			} catch (Exception e) {
				exceptions.add(new ProcessException(zzxm.getI18n(I18nStringKey.LOGGER_12), e));
			}

			return exceptions;
		}

		private void filterMachiningInfo(String xmh, MachiningInfo machiningInfo,
				SyncMachiningModel filteredMachiningModel, SyncMachiningModel failedMachiningModel) throws Exception {
			try {
				if (filter.accept(machiningInfo)) {
					filteredMachiningModel.addMachiningInfo(xmh, machiningInfo);
				}
			} catch (Exception e) {
				failedMachiningModel.addMachiningInfo(xmh, machiningInfo);
				throw e;
			}
		}

	}

	class MachiningInfoFilter {

		public boolean accept(MachiningInfo info) throws CellProcessException {
			/**
			 * 搜索条件是03号表中四次、七次、八次、九次工序四个栏目中， 只要有一个栏目中“委托方“非零，而缺件为零，就不用提取，
			 * 否则就将相应信息写入《在制项目零件加工完成情况》中。 当零件号包含 / 字符是，则不导出。
			 */
			return !notAccept(info);
		}

		private boolean notAccept(MachiningInfo info) throws CellProcessException {
			Cell currentCell = null;

			try {
				// 取零件号
				{
					currentCell = Optional.ofNullable(info.get("ljh")).map(DataContext::getContextCell).orElse(null);
					String ljh = info.get("ljh").getValue(DataType.STRING, String.class).trim();
					if (ljh.matches("^.*/.*$"))
						return true;
				}

				// 取四次工序
				{
					currentCell = Optional.ofNullable(info.get("scgx.wtf")).map(DataContext::getContextCell)
							.orElse(null);
					String wtf = info.get("scgx.wtf").getValue(DataType.STRING, String.class).trim();
					currentCell = Optional.ofNullable(info.get("scgx.qj")).map(DataContext::getContextCell)
							.orElse(null);
					int qj = info.get("scgx.qj").getValue(DataType.NUMBER, Double.class).intValue();
					if (wtf.length() > 0 && qj == 0)
						return true;
				}
				// 取七次工序
				{
					currentCell = Optional.ofNullable(info.get("qcgx.wtf")).map(DataContext::getContextCell)
							.orElse(null);
					String wtf = info.get("qcgx.wtf").getValue(DataType.STRING, String.class).trim();
					currentCell = Optional.ofNullable(info.get("qcgx.qj")).map(DataContext::getContextCell)
							.orElse(null);
					int qj = info.get("qcgx.qj").getValue(DataType.NUMBER, Double.class).intValue();
					if (wtf.length() > 0 && qj == 0)
						return true;
				}
				// 取八次工序
				{
					currentCell = Optional.ofNullable(info.get("bcgx.wtf")).map(DataContext::getContextCell)
							.orElse(null);
					String wtf = info.get("bcgx.wtf").getValue(DataType.STRING, String.class).trim();
					currentCell = Optional.ofNullable(info.get("bcgx.qj")).map(DataContext::getContextCell)
							.orElse(null);
					int qj = info.get("bcgx.qj").getValue(DataType.NUMBER, Double.class).intValue();
					if (wtf.length() > 0 && qj == 0)
						return true;
				}
				// 取九次工序
				{
					currentCell = Optional.ofNullable(info.get("jcgx.wtf")).map(DataContext::getContextCell)
							.orElse(null);
					String wtf = info.get("jcgx.wtf").getValue(DataType.STRING, String.class).trim();
					currentCell = Optional.ofNullable(info.get("jcgx.qj")).map(DataContext::getContextCell)
							.orElse(null);
					int qj = info.get("jcgx.qj").getValue(DataType.NUMBER, Double.class).intValue();
					if (wtf.length() > 0 && qj == 0)
						return true;
				}
				return false;
			} catch (Exception e) {
				throw new CellProcessException(currentCell, zzxm.getI18n(I18nStringKey.LOGGER_46), e);
			}
		}

	}

	private final static class FilterModelContext {
		/** 过滤的机加工模型。 */
		private final SyncMachiningModel filteredMachiningModel;
		/** 失败的机加工模型。 */
		private final SyncMachiningModel failedMachiningModel;

		public FilterModelContext(SyncMachiningModel filteredMachiningModel, SyncMachiningModel failedMachiningModel) {
			this.filteredMachiningModel = filteredMachiningModel;
			this.failedMachiningModel = failedMachiningModel;
		}

		public SyncMachiningModel getFilteredMachiningModel() {
			return filteredMachiningModel;
		}

		public SyncMachiningModel getFailedMachiningModel() {
			return failedMachiningModel;
		}

	}

}
