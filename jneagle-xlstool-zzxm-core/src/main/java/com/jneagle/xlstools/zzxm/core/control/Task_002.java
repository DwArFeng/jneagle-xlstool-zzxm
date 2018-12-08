package com.jneagle.xlstools.zzxm.core.control;

import java.io.File;
import java.util.Locale;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.jneagle.xlstools.zzxm.core.model.eum.ConfigItem;
import com.jneagle.xlstools.zzxm.core.model.eum.I18nStringKey;

class SelectLoadFileTask extends AbstractZZXMTask {

	public SelectLoadFileTask(ZZXM zzxm) {
		super(zzxm);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void todo() throws Exception {
		// 创建文件选择器并进行设置。
		JFileChooser jfc = new JFileChooser();
		jfc.setLocale(zzxm.configHandler.getParsedValidValue(ConfigItem.I18N_LOCALE, Locale.class));
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		jfc.setFileFilter(new FileNameExtensionFilter(zzxm.getI18n(I18nStringKey.LABEL_5), "xls"));
		jfc.setAcceptAllFileFilterUsed(false);
		int result = jfc.showOpenDialog(zzxm.mainFrameModel.get());
		// 如果用户按下了确定键，则将文件模型设置为用户选择的文件。
		if (result == JFileChooser.APPROVE_OPTION) {
			zzxm.excelLoadPasswordModel.set("");
			File file = jfc.getSelectedFile();
			zzxm.excelLoadFileModel.set(file);
		}
	}

}
