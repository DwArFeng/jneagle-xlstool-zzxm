package com.jneagle.xlstools.zzxm.core.control;

import java.io.File;
import java.util.Locale;
import java.util.Objects;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.jneagle.xlstools.zzxm.core.model.eum.ConfigItem;
import com.jneagle.xlstools.zzxm.core.model.eum.I18nStringKey;

class SelectExportFileTask extends AbstractZZXMTask {

	public SelectExportFileTask(ZZXM zzxm) {
		super(zzxm);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void todo() throws Exception {
		// 创建文件选择器并进行设置。
		JFileChooser jfc = new JFileChooser();
		jfc.setCurrentDirectory(zzxm.excelLoadFileModel.get().getParentFile());
		jfc.setLocale(zzxm.configHandler.getParsedValidValue(ConfigItem.I18N_LOCALE, Locale.class));
		jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		FileNameExtensionFilter xlsFilter = new FileNameExtensionFilter(zzxm.getI18n(I18nStringKey.LABEL_5), "xls");
		jfc.setFileFilter(xlsFilter);
		jfc.setAcceptAllFileFilterUsed(false);
		int result = jfc.showSaveDialog(zzxm.mainFrameModel.get());
		// 如果用户按下了确定键，则将文件模型设置为用户选择的文件。
		if (result == JFileChooser.APPROVE_OPTION) {
			zzxm.excelExportPasswordModel.set("");
			File file = jfc.getSelectedFile();
			if (Objects.equals(jfc.getFileFilter(), xlsFilter)) {
				if (!file.getAbsolutePath().endsWith(".xls")) {
					file = new File(file.getAbsolutePath() + ".xls");
				}
			}
			zzxm.excelExportFileModel.set(file);
		}
	}

}
