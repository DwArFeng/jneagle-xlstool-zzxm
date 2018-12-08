package com.jneagle.xlstool.zzxm.core.control;

import java.io.File;
import java.util.Collection;
import java.util.Locale;
import java.util.Set;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.nimbus.NimbusLookAndFeel;

import com.dwarfeng.dutil.basic.gui.swing.SwingUtil;
import com.dwarfeng.dutil.basic.io.LoadFailedException;
import com.dwarfeng.dutil.develop.i18n.io.XmlPropFileI18nLoader;
import com.dwarfeng.dutil.develop.logger.io.Log4jLoggerLoader;
import com.dwarfeng.dutil.develop.resource.io.XmlJar2FileResourceLoader;
import com.dwarfeng.dutil.develop.setting.SettingUtil;
import com.dwarfeng.dutil.develop.setting.io.PropSettingValueLoader;
import com.jneagle.xlstool.zzxm.core.model.eum.ConfigItem;
import com.jneagle.xlstool.zzxm.core.model.eum.I18nStringKey;
import com.jneagle.xlstool.zzxm.core.model.eum.ModalItem;
import com.jneagle.xlstool.zzxm.core.model.eum.ResourceKey;
import com.jneagle.xlstool.zzxm.core.util.Constants;
import com.jneagle.xlstool.zzxm.core.view.gui.MainFrame;
import com.jneagle.xlstool.zzxm.core.view.struct.ViewControlBridge;

class PoseProcess extends AbstractZZXMProcess {

	static class ViewControlBridgeImpl implements ViewControlBridge {

		private final ZZXM zzxm;

		public ViewControlBridgeImpl(ZZXM zzxm) {
			this.zzxm = zzxm;
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void dispose() {
			zzxm.background.submit(new DisposeTask(zzxm));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void selectLoadFile() {
			zzxm.background.submit(new SelectLoadFileTask(zzxm));

		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void changeLoadPassword(String password) {
			zzxm.background.submit(new ChangeLoadPasswordTask(zzxm, password));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void loadFile() {
			zzxm.background.submit(new LoadFileTask(zzxm));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void selectExportFile() {
			zzxm.background.submit(new SelectExportFileTask(zzxm));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void changeExportPassword(String password) {
			zzxm.background.submit(new ChangeExportPasswordTask(zzxm, password));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void exportFile() {
			zzxm.background.submit(new ExportFileTask(zzxm));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void commitSelectedXmh(String xmh) {
			zzxm.background.submit(new CommitSelectedXmhTask(zzxm, xmh));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void commitAllXmh() {
			zzxm.background.submit(new CommitAllXmhTask(zzxm));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void uncommitAllXmh() {
			zzxm.background.submit(new UncommitAllXmhTask(zzxm));
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void uncommitSpecifiedXmh(Collection<String> c) {
			zzxm.background.submit(new UncommitSpecifiedXmhTask(zzxm, c));
		}

	}

	public PoseProcess(ZZXM zzxm) {
		super(zzxm);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void todo() throws Exception {
		// 生成资源。
		{
			try (XmlJar2FileResourceLoader loader = new XmlJar2FileResourceLoader(
					PoseProcess.class.getResourceAsStream(Constants.RESOURCE_LIST_PATH), true)) {
				loader.load(zzxm.resourceHandler);
			}
		}

		// 加载记录器处理器。
		{
			try (Log4jLoggerLoader loader = new Log4jLoggerLoader(
					zzxm.resourceHandler.openInputStream(ResourceKey.LOGGER_SETTING))) {
				Set<LoadFailedException> exceptions = loader.countinuousLoad(zzxm.loggerHandler);
				zzxm.loggerHandler.useAll();
				exceptions.forEach(exception -> {
					zzxm.loggerHandler.warn(Constants.DEFAULT_MESSAGE_0, exception);
				});
				zzxm.loggerHandler.info(Constants.DEFAULT_MESSAGE_2);
			}
		}

		// 加载国际化处理器。
		{
			zzxm.loggerHandler.info(Constants.DEFAULT_MESSAGE_3);
			try (XmlPropFileI18nLoader loader = new XmlPropFileI18nLoader(
					zzxm.resourceHandler.openInputStream(ResourceKey.I18N_SETTING))) {
				Set<LoadFailedException> exceptions = loader.countinuousLoad(zzxm.i18nHandler);
				zzxm.i18nHandler.setCurrentLocale(null);
				exceptions.forEach(exception -> {
					zzxm.loggerHandler.warn(zzxm.getI18n(I18nStringKey.LOGGER_1), exception);
				});
				zzxm.loggerHandler.info(zzxm.getI18n(I18nStringKey.LOGGER_2));
			}
		}

		// 加载应用配置文件。
		{
			zzxm.loggerHandler.info(zzxm.getI18n(I18nStringKey.LOGGER_3));
			SettingUtil.putEnumItems(ConfigItem.class, zzxm.configHandler);
			try (PropSettingValueLoader loader = new PropSettingValueLoader(
					zzxm.resourceHandler.openInputStream(ResourceKey.CONFIG))) {
				Set<LoadFailedException> exceptions = loader.countinuousLoad(zzxm.configHandler);
				exceptions.forEach(exception -> {
					zzxm.loggerHandler.warn(zzxm.getI18n(I18nStringKey.LOGGER_4), exception);
				});
				zzxm.loggerHandler.info(zzxm.getI18n(I18nStringKey.LOGGER_5));
			}
			zzxm.i18nHandler
					.setCurrentLocale(zzxm.configHandler.getParsedValidValue(ConfigItem.I18N_LOCALE, Locale.class));
		}

		// 加载模态配置文件。
		{
			zzxm.loggerHandler.info(zzxm.getI18n(I18nStringKey.LOGGER_15));
			SettingUtil.putEnumItems(ModalItem.class, zzxm.modalHandler);
			try (PropSettingValueLoader loader = new PropSettingValueLoader(
					zzxm.resourceHandler.openInputStream(ResourceKey.MODAL))) {
				Set<LoadFailedException> exceptions = loader.countinuousLoad(zzxm.modalHandler);
				exceptions.forEach(exception -> {
					zzxm.loggerHandler.warn(zzxm.getI18n(I18nStringKey.LOGGER_44), exception);
				});
				zzxm.loggerHandler.info(zzxm.getI18n(I18nStringKey.LOGGER_45));
			}
			zzxm.i18nHandler
					.setCurrentLocale(zzxm.configHandler.getParsedValidValue(ConfigItem.I18N_LOCALE, Locale.class));
		}

		// 加载部分持久化对象。
		{
			File file = zzxm.modalHandler.getParsedValidValue(ModalItem.PERSISTENCE_LAST_LOAD_FILE, File.class);
			Boolean flag = zzxm.modalHandler.getParsedValidValue(ModalItem.PERSISTENCE_LAST_LOAD_FLAG, Boolean.class);
			if (flag)
				zzxm.excelLoadFileModel.set(file);
		}

		// 加载界面
		{
			zzxm.loggerHandler.info(zzxm.getI18n(I18nStringKey.LOGGER_6));
			SwingUtil.invokeAndWaitInEventQueue(() -> {
				try {
					UIManager.setLookAndFeel(new NimbusLookAndFeel());
				} catch (UnsupportedLookAndFeelException e) {
					zzxm.loggerHandler.warn(zzxm.getI18n(I18nStringKey.LOGGER_27), e);
				}

				zzxm.mainFrameModel.set(new MainFrame(new ViewControlBridgeImpl(zzxm), zzxm.i18nHandler,
						zzxm.excelLoadFileModel, zzxm.excelLoadPasswordModel, zzxm.excelExportFileModel,
						zzxm.excelExportPasswordModel, zzxm.machiningModel, zzxm.filteredMachiningModel,
						zzxm.failedMachiningModel, zzxm.xmhSelectModel, zzxm.loadStateModel, zzxm.currentProgressModel,
						zzxm.totleProgressModel));
				zzxm.mainFrameModel.get().notification(zzxm.getI18n(I18nStringKey.LABEL_12));
				zzxm.mainFrameModel.get().setVisible(true);
			});
			zzxm.loggerHandler.info(zzxm.getI18n(I18nStringKey.LOGGER_7));
		}

	}

}