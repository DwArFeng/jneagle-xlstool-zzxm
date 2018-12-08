package com.jneagle.xlstools.zzxm.core.control;

import java.util.Set;

import com.dwarfeng.dutil.basic.gui.swing.SwingUtil;
import com.dwarfeng.dutil.basic.io.SaveFailedException;
import com.dwarfeng.dutil.develop.setting.io.PropSettingValueSaver;
import com.jneagle.xlstools.zzxm.core.model.eum.I18nStringKey;
import com.jneagle.xlstools.zzxm.core.model.eum.ResourceKey;

class DisposeProcess extends AbstractZZXMProcess {

	public DisposeProcess(ZZXM zzxm) {
		super(zzxm);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void todo() throws Exception {
		// 释放界面。
		{
			zzxm.loggerHandler.info(zzxm.getI18n(I18nStringKey.LOGGER_8));
			SwingUtil.invokeAndWaitInEventQueue(() -> {
				zzxm.mainFrameModel.get().setVisible(false);
				zzxm.mainFrameModel.get().dispose();
			});
			zzxm.loggerHandler.info(zzxm.getI18n(I18nStringKey.LOGGER_9));
		}

		// 保存模态信息。
		{
			zzxm.loggerHandler.info(zzxm.getI18n(I18nStringKey.LOGGER_18));
			try (PropSettingValueSaver saver = new PropSettingValueSaver(
					zzxm.resourceHandler.openOutputStream(ResourceKey.MODAL))) {
				Set<SaveFailedException> exceptions = saver.countinuousSave(zzxm.modalHandler);
				exceptions.forEach(exception -> {
					zzxm.loggerHandler.warn(zzxm.getI18n(I18nStringKey.LOGGER_20), exception);
				});
			}
			zzxm.loggerHandler.info(zzxm.getI18n(I18nStringKey.LOGGER_19));
		}

		// 清空模型数据。
		{
			zzxm.loggerHandler.info(zzxm.getI18n(I18nStringKey.LOGGER_10));
			// TODO
			zzxm.loggerHandler.info(zzxm.getI18n(I18nStringKey.LOGGER_11));

		}

		// 关闭后台。
		{
			zzxm.background.shutdown();
		}
	}

}