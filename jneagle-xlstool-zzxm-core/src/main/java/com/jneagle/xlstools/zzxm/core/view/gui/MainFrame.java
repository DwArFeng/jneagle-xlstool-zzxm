package com.jneagle.xlstools.zzxm.core.view.gui;

import java.awt.BorderLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.Objects;
import java.util.Optional;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import com.dwarfeng.dutil.basic.cna.model.SyncReferenceModel;
import com.dwarfeng.dutil.basic.cna.model.SyncSetModel;
import com.dwarfeng.dutil.develop.i18n.SyncI18nHandler;
import com.jneagle.xlstools.zzxm.core.control.ZZXM;
import com.jneagle.xlstools.zzxm.core.model.cm.SyncMachiningModel;
import com.jneagle.xlstools.zzxm.core.model.eum.I18nStringKey;
import com.jneagle.xlstools.zzxm.core.model.eum.ProgressState;
import com.jneagle.xlstools.zzxm.core.util.Constants;
import com.jneagle.xlstools.zzxm.core.view.struct.ViewControlBridge;

public class MainFrame extends JFrame {

	private final JPanel contentPane;
	private final FileInfoPanel fileInfoPanel;
	private final XmhChoosePanel xmhChoosePanel;
	private final JLabel notificationLabel;
	private final ProgressPanel progressPanel;

	private ViewControlBridge vcb;
	private SyncI18nHandler i18nHandler;
	private SyncReferenceModel<File> excelLoadFileModel;
	private SyncReferenceModel<String> excelLoadPasswordModel;

	/**
	 * Create the frame.
	 */
	public MainFrame() {
		this(null, null, null, null, null, null, null, null, null, null, null, null, null);
	}

	public MainFrame(ViewControlBridge vcb, SyncI18nHandler i18nHandler, SyncReferenceModel<File> excelLoadFileModel,
			SyncReferenceModel<String> excelLoadPasswordModel, SyncReferenceModel<File> excelExportFileModel,
			SyncReferenceModel<String> excelExportPasswordModel, SyncMachiningModel machiningModel,
			SyncMachiningModel filteredMachiningModel, SyncMachiningModel failedMachiningModel,
			SyncSetModel<String> xmhSelectModel, SyncReferenceModel<ProgressState> loadStateModel,
			SyncReferenceModel<Integer> currentProgressModel, SyncReferenceModel<Integer> totleProgressModel) {
		setTitle(Constants.MISSING_LABEL);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		setBounds(100, 100, 600, 750);
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				Optional.ofNullable(vcb).ifPresent(vcb -> vcb.dispose());
			}
		});
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		fileInfoPanel = new FileInfoPanel(vcb, i18nHandler, excelLoadFileModel, excelLoadPasswordModel,
				excelExportFileModel, excelExportPasswordModel, loadStateModel);
		contentPane.add(fileInfoPanel, BorderLayout.NORTH);

		xmhChoosePanel = new XmhChoosePanel(vcb, i18nHandler, machiningModel, filteredMachiningModel,
				failedMachiningModel, xmhSelectModel, loadStateModel);
		contentPane.add(xmhChoosePanel, BorderLayout.CENTER);

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.SOUTH);
		panel.setLayout(new BorderLayout(0, 0));

		notificationLabel = new JLabel();
		panel.add(notificationLabel, BorderLayout.CENTER);

		progressPanel = new ProgressPanel(currentProgressModel, totleProgressModel);
		progressPanel.setVisible(false);
		panel.add(progressPanel, BorderLayout.NORTH);

		this.vcb = vcb;
		this.i18nHandler = i18nHandler;
		this.excelLoadFileModel = excelLoadFileModel;
		this.excelLoadPasswordModel = excelLoadPasswordModel;

		syncI18nHandler();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void dispose() {
		super.dispose();
		fileInfoPanel.dispose();
		xmhChoosePanel.dispose();
		progressPanel.dispose();
	}

	/**
	 * @return the vcb
	 */
	public ViewControlBridge getVcb() {
		return vcb;
	}

	/**
	 * @param vcb
	 *            the vcb to set
	 */
	public void setVcb(ViewControlBridge vcb) {
		this.vcb = vcb;
		fileInfoPanel.setVcb(vcb);

	}

	/**
	 * @return the i18nHandler
	 */
	public SyncI18nHandler getI18nHandler() {
		return i18nHandler;
	}

	/**
	 * @param i18nHandler
	 *            the i18nHandler to set
	 */
	public void setI18nHandler(SyncI18nHandler i18nHandler) {
		this.i18nHandler = i18nHandler;
		fileInfoPanel.setI18nHandler(i18nHandler);
	}

	/**
	 * @return the excelLoadFileModel
	 */
	public SyncReferenceModel<File> getExcelLoadFileModel() {
		return excelLoadFileModel;
	}

	/**
	 * @param excelLoadFileModel
	 *            the excelLoadFileModel to set
	 */
	public void setExcelLoadFileModel(SyncReferenceModel<File> excelLoadFileModel) {
		this.excelLoadFileModel = excelLoadFileModel;
		fileInfoPanel.setExcelLoadFileModel(excelLoadFileModel);
	}

	/**
	 * @return the excelLoadPasswordModel
	 */
	public SyncReferenceModel<String> getExcelLoadPasswordModel() {
		return excelLoadPasswordModel;
	}

	/**
	 * @param excelLoadPasswordModel
	 *            the excelLoadPasswordModel to set
	 */
	public void setExcelLoadPasswordModel(SyncReferenceModel<String> excelLoadPasswordModel) {
		this.excelLoadPasswordModel = excelLoadPasswordModel;
		fileInfoPanel.setExcelLoadPasswordModel(excelLoadPasswordModel);
	}

	public void notification(String msg) {
		notificationLabel.setText(msg);
	}

	public boolean getProgressPanelVisible() {
		return progressPanel.isVisible();
	}

	public void setProgressPanelVisible(boolean aFlag) {
		progressPanel.setVisible(aFlag);
	}

	private void syncI18nHandler() {
		setTitle(Constants.MISSING_LABEL);

		if (Objects.isNull(i18nHandler))
			return;

		i18nHandler.getLock().readLock().lock();
		try {
			setTitle(String.format(i18nHandler.getStringOrDefault(I18nStringKey.LABEL_42, Constants.MISSING_LABEL),
					ZZXM.VERSION.getLongName()));
		} finally {
			i18nHandler.getLock().readLock().unlock();
		}
	}

}
