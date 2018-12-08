package com.jneagle.xlstools.zzxm.core.control;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.dwarfeng.dutil.basic.cna.model.DefaultReferenceModel;
import com.dwarfeng.dutil.basic.cna.model.DelegateSetModel;
import com.dwarfeng.dutil.basic.cna.model.ModelUtil;
import com.dwarfeng.dutil.basic.cna.model.SyncReferenceModel;
import com.dwarfeng.dutil.basic.cna.model.SyncSetModel;
import com.dwarfeng.dutil.basic.prog.DefaultVersion;
import com.dwarfeng.dutil.basic.prog.ProcessException;
import com.dwarfeng.dutil.basic.prog.RuntimeState;
import com.dwarfeng.dutil.basic.prog.Version;
import com.dwarfeng.dutil.basic.threads.NumberedThreadFactory;
import com.dwarfeng.dutil.develop.backgr.Background;
import com.dwarfeng.dutil.develop.backgr.ExecutorServiceBackground;
import com.dwarfeng.dutil.develop.i18n.DelegateI18nHandler;
import com.dwarfeng.dutil.develop.i18n.I18nUtil;
import com.dwarfeng.dutil.develop.i18n.SyncI18nHandler;
import com.dwarfeng.dutil.develop.logger.DelegateLoggerHandler;
import com.dwarfeng.dutil.develop.logger.LoggerUtil;
import com.dwarfeng.dutil.develop.logger.SyncLoggerHandler;
import com.dwarfeng.dutil.develop.resource.DelegateResourceHandler;
import com.dwarfeng.dutil.develop.resource.ResourceUtil;
import com.dwarfeng.dutil.develop.resource.SyncResourceHandler;
import com.dwarfeng.dutil.develop.setting.DefaultSettingHandler;
import com.dwarfeng.dutil.develop.setting.SettingUtil;
import com.dwarfeng.dutil.develop.setting.SyncSettingHandler;
import com.jneagle.xlstools.zzxm.core.model.cm.DefaultMachiningModel;
import com.jneagle.xlstools.zzxm.core.model.cm.SyncMachiningModel;
import com.jneagle.xlstools.zzxm.core.model.eum.I18nStringKey;
import com.jneagle.xlstools.zzxm.core.model.eum.ProgressState;
import com.jneagle.xlstools.zzxm.core.util.Constants;
import com.jneagle.xlstools.zzxm.core.view.gui.MainFrame;

/**
 * 
 * @author DwArFeng
 * @since 1.0.0.a
 */
public class ZZXM {

	public final class Controller {

		/**
		 * 程序启动。
		 * 
		 * @throws ProcessException 过程异常。
		 */
		public void pose() throws ProcessException {
			INSTANCES.add(ZZXM.this);
			PoseProcess process = new PoseProcess(ZZXM.this);
			process.run();
			Throwable throwable = process.getThrowable();
			if (Objects.nonNull(throwable)) {
				INSTANCES.remove(ZZXM.this);
				exitCodeModel.set(1);
				setProgState(RuntimeState.ENDED);
				throw new ProcessException(Constants.DEFAULT_MESSAGE_1, throwable);
			}
			setProgState(RuntimeState.RUNNING);
		}

		/**
		 * 程序结束。
		 * 
		 * @throws ProcessException 过程异常。
		 */
		public void dispose() throws ProcessException {
			DisposeProcess process = new DisposeProcess(ZZXM.this);
			process.run();
			Throwable throwable = process.getThrowable();
			if (Objects.nonNull(throwable)) {
				INSTANCES.remove(ZZXM.this);
				exitCodeModel.set(1);
				setProgState(RuntimeState.ENDED);
				throw new ProcessException(Constants.DEFAULT_MESSAGE_1, throwable);
			}
			INSTANCES.remove(ZZXM.this);
			exitCodeModel.set(0);
			setProgState(RuntimeState.ENDED);
		}

		/**
		 * 
		 * @throws InterruptedException
		 */
		public void awaitFinish() throws InterruptedException {
			runningLock.lock();
			try {
				while (!Objects.equals(progStateModel.get(), RuntimeState.ENDED)) {
					runningCondition.await();
				}
			} finally {
				runningLock.unlock();
			}
		}

		/**
		 * 
		 * @param timeout
		 * @param unit
		 * @return
		 * @throws InterruptedException
		 */
		public boolean awaitFinish(long timeout, TimeUnit unit) throws InterruptedException {
			runningLock.lock();
			try {
				long nanosTimeout = unit.toNanos(timeout);
				while (!Objects.equals(progStateModel.get(), RuntimeState.ENDED)) {
					if (nanosTimeout > 0)
						nanosTimeout = runningCondition.awaitNanos(nanosTimeout);
					else
						return false;
				}
				return true;
			} finally {
				runningLock.unlock();
			}
		}

		/**
		 * 获取程序的退出代码。
		 * 
		 * @return 程序的退出代码。
		 */
		public int getExitCode() {
			return exitCodeModel.get();
		}

		private Controller() {
			// 禁止外部实例化。
		}
	}

	/** 版本。 */
	public static final Version VERSION = new DefaultVersion.Builder().setFirstVersion((byte) 1)
			.setSecondVersion((byte) 1).setThirdVersion((byte) 0).setBuildDate("20180828").setBuildVersion('A').build();

	/** 实例引用。 */
	public static final Collection<ZZXM> INSTANCES = new HashSet<>();

	static final ThreadFactory THREAD_FACTORY = new NumberedThreadFactory("ZZXM");

	/** 控制器文件。 */
	final Controller controller = new Controller();

	/** 后台。 */
	final Background background = new ExecutorServiceBackground(Executors.newFixedThreadPool(4, THREAD_FACTORY),
			Collections.newSetFromMap(new WeakHashMap<>()));
	/** 资源处理器。 */
	final SyncResourceHandler resourceHandler = ResourceUtil.syncResourceHandler(new DelegateResourceHandler());
	/** 记录器处理器。 */
	final SyncLoggerHandler loggerHandler = LoggerUtil.syncLoggerHandler(new DelegateLoggerHandler());
	/** 国际化处理器。 */
	final SyncI18nHandler i18nHandler = I18nUtil.syncI18nHandler(new DelegateI18nHandler());

	/** 配置处理器。 */
	final SyncSettingHandler configHandler = SettingUtil.syncSettingHandler(new DefaultSettingHandler());
	/** 配置处理器。 */
	final SyncSettingHandler modalHandler = SettingUtil.syncSettingHandler(new DefaultSettingHandler());

	/** 加载表格文件引用模型。 */
	final SyncReferenceModel<File> excelLoadFileModel = ModelUtil.syncReferenceModel(new DefaultReferenceModel<>(null));
	/** 加载表格密码引用。 */
	final SyncReferenceModel<String> excelLoadPasswordModel = ModelUtil
			.syncReferenceModel(new DefaultReferenceModel<>(""));
	/** 导出表格文件引用模型。 */
	final SyncReferenceModel<File> excelExportFileModel = ModelUtil
			.syncReferenceModel(new DefaultReferenceModel<>(null));
	/** 导出表格密码引用。 */
	final SyncReferenceModel<String> excelExportPasswordModel = ModelUtil
			.syncReferenceModel(new DefaultReferenceModel<>(""));

	/** 表格读取状态模型。 */
	final SyncReferenceModel<ProgressState> loadStateModel = ModelUtil
			.syncReferenceModel(new DefaultReferenceModel<>());
	/** 表格过滤状态模型。 */
	final SyncReferenceModel<ProgressState> filterStateModel = ModelUtil
			.syncReferenceModel(new DefaultReferenceModel<>());
	/** 表格导出状态模型。 */
	final SyncReferenceModel<ProgressState> exportStateModel = ModelUtil
			.syncReferenceModel(new DefaultReferenceModel<>());

	/** 机加工模型。 */
	final SyncMachiningModel machiningModel = com.jneagle.xlstools.zzxm.core.util.ModelUtil
			.syncMachiningModel(new DefaultMachiningModel());
	/** 过滤的机加工模型。 */
	final SyncMachiningModel filteredMachiningModel = com.jneagle.xlstools.zzxm.core.util.ModelUtil
			.syncMachiningModel(new DefaultMachiningModel());
	/** 失败的机加工模型。 */
	final SyncMachiningModel failedMachiningModel = com.jneagle.xlstools.zzxm.core.util.ModelUtil
			.syncMachiningModel(new DefaultMachiningModel());
	/** 项目号选择模型。 */
	final SyncSetModel<String> xmhSelectModel = ModelUtil.syncSetModel(
			new DelegateSetModel<>(new LinkedHashSet<>(), Collections.newSetFromMap(new WeakHashMap<>())));

	/** 主界面引用模型。 */
	final SyncReferenceModel<MainFrame> mainFrameModel = ModelUtil
			.syncReferenceModel(new DefaultReferenceModel<>(null));

	/** 当前进度模型。 */
	final SyncReferenceModel<Integer> currentProgressModel = ModelUtil
			.syncReferenceModel(new DefaultReferenceModel<>(0));
	/** 总进度模型。 */
	final SyncReferenceModel<Integer> totleProgressModel = ModelUtil
			.syncReferenceModel(new DefaultReferenceModel<>(-1));

	/** 主程序的运行锁与运行标记。 */
	final Lock runningLock = new ReentrantLock();
	final Condition runningCondition = runningLock.newCondition();
	final SyncReferenceModel<RuntimeState> progStateModel = ModelUtil
			.syncReferenceModel(new DefaultReferenceModel<>(RuntimeState.NOT_START));
	final SyncReferenceModel<Integer> exitCodeModel = ModelUtil.syncReferenceModel(new DefaultReferenceModel<>(0));

	public ZZXM() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * 获取控制器。
	 * 
	 * @return 控制器。
	 */
	public Controller getController() {
		return controller;
	}

	String getI18n(I18nStringKey i18nKey) {
		return i18nHandler.getStringOrDefault(i18nKey, Constants.MISSING_LABEL);
	}

	void setProgState(RuntimeState runtimeState) {
		progStateModel.set(runtimeState);
		runningLock.lock();
		try {
			runningCondition.signalAll();
		} finally {
			runningLock.unlock();
		}
	}

}
