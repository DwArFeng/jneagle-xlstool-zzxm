package com.jneagle.xlstool.zzxm.core.view.gui;

import java.awt.Cursor;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;
import java.util.Queue;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import com.dwarfeng.dutil.basic.cna.model.ReferenceModel;
import com.dwarfeng.dutil.basic.cna.model.SyncReferenceModel;
import com.dwarfeng.dutil.basic.cna.model.obv.ReferenceAdapter;
import com.dwarfeng.dutil.basic.cna.model.obv.ReferenceObverser;
import com.dwarfeng.dutil.basic.gui.swing.SwingUtil;
import com.dwarfeng.dutil.basic.io.CT;
import com.dwarfeng.dutil.develop.i18n.SyncI18nHandler;
import com.jneagle.xlstool.zzxm.core.model.eum.I18nStringKey;
import com.jneagle.xlstool.zzxm.core.model.eum.ProgressState;
import com.jneagle.xlstool.zzxm.core.util.Constants;
import com.jneagle.xlstool.zzxm.core.view.struct.ViewControlBridge;

public class FileInfoPanel extends JPanel {

	private final JTextField loadFileTextField;
	private final JPasswordField loadFilePasswordField;
	private final JLabel loadFileLabel;
	private final JLabel loadFilePasswordLabel;
	private final JButton loadFileButton;
	private final JLabel exportFileLabel;
	private final JLabel exportFilePasswordLabel;
	private final JTextField exportFileTextField;
	private final JPasswordField exportFilePasswordField;
	private final JButton exportFileButton;

	private ViewControlBridge vcb;
	private SyncI18nHandler i18nHandler;
	private SyncReferenceModel<File> excelLoadFileModel;
	private SyncReferenceModel<String> excelLoadPasswordModel;
	private SyncReferenceModel<File> excelExportFileModel;
	private SyncReferenceModel<String> excelExportPasswordModel;
	private SyncReferenceModel<ProgressState> loadStateModel;

	private final ReferenceObverser<File> excelLoadFileObverser = new ReferenceAdapter<File>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireSet(File oldValue, File newValue) {
			SwingUtil.invokeInEventQueue(() -> {
				loadFileTextField.setText(Optional.ofNullable(newValue).map(file -> file.getAbsolutePath()).orElse(""));
			});
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireCleared() {
			SwingUtil.invokeInEventQueue(() -> {
				loadFileTextField.setText("");
			});
		}

	};

	private final ReferenceObverser<String> excelLoadPasswordObverser = new ReferenceAdapter<String>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireSet(String oldValue, String newValue) {
			SwingUtil.invokeInEventQueue(() -> {
				if (checkLfDuplexingForecast(new Object[] { "fireSet", oldValue, newValue })) {
					return;
				}

				loadFilePasswordField.setText(Optional.ofNullable(newValue).orElse(""));
			});
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireCleared() {
			SwingUtil.invokeInEventQueue(() -> {
				loadFilePasswordField.setText("");
			});
		}

	};
	private final ReferenceObverser<File> excelExportFileObverser = new ReferenceAdapter<File>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireSet(File oldValue, File newValue) {
			SwingUtil.invokeInEventQueue(() -> {
				exportFileTextField
						.setText(Optional.ofNullable(newValue).map(file -> file.getAbsolutePath()).orElse(""));
			});
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireCleared() {
			SwingUtil.invokeInEventQueue(() -> {
				exportFileTextField.setText("");
			});
		}

	};
	private final ReferenceObverser<String> excelExportPasswordObverser = new ReferenceAdapter<String>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireSet(String oldValue, String newValue) {
			SwingUtil.invokeInEventQueue(() -> {
				if (checkEfDuplexingForecast(new Object[] { "fireSet", oldValue, newValue })) {
					return;
				}

				exportFilePasswordField.setText(Optional.ofNullable(newValue).orElse(""));
			});
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireCleared() {
			SwingUtil.invokeInEventQueue(() -> {
				exportFilePasswordField.setText("");
			});
		}

	};
	private final ReferenceObverser<ProgressState> excelLoadStateObverser = new ReferenceAdapter<ProgressState>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireSet(ProgressState oldValue, ProgressState newValue) {
			SwingUtil.invokeInEventQueue(() -> {
				if (Objects.equals(newValue, ProgressState.FINISHED)) {
					exportFileButton.setEnabled(true);
				} else {
					exportFileButton.setEnabled(false);
				}
			});
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireCleared() {
			SwingUtil.invokeInEventQueue(() -> {
				exportFileButton.setEnabled(false);
			});
		}

	};

	/** 双工通信预测 */
	private final Queue<Object[]> lfDuplexingForecast = new ArrayDeque<>();
	private final Queue<Object[]> efDuplexingForecast = new ArrayDeque<>();

	/**
	 * Create the panel.
	 */
	public FileInfoPanel() {
		this(null, null, null, null, null, null, null);
	}

	/**
	 * Create the panel.
	 * 
	 * @param vcb
	 * @param i18nHandler
	 * @param excelLoadFileModel
	 * @param excelLoadPasswordModel
	 * @param excelExportFileModel
	 * @param excelExportPasswordModel
	 * @param loadStateModel
	 */
	public FileInfoPanel(ViewControlBridge vcb, SyncI18nHandler i18nHandler,
			SyncReferenceModel<File> excelLoadFileModel, SyncReferenceModel<String> excelLoadPasswordModel,
			SyncReferenceModel<File> excelExportFileModel, SyncReferenceModel<String> excelExportPasswordModel,
			SyncReferenceModel<ProgressState> loadStateModel) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 32, 0, 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		loadFileLabel = new JLabel(Constants.MISSING_LABEL); // 文件路径。
		GridBagConstraints gbc_loadFileLabel = new GridBagConstraints();
		gbc_loadFileLabel.insets = new Insets(0, 0, 5, 5);
		gbc_loadFileLabel.anchor = GridBagConstraints.EAST;
		gbc_loadFileLabel.gridx = 0;
		gbc_loadFileLabel.gridy = 0;
		add(loadFileLabel, gbc_loadFileLabel);

		loadFileTextField = new JTextField();
		loadFileTextField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		loadFileTextField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Optional.ofNullable(vcb).ifPresent(vcb -> vcb.selectLoadFile());
			}
		});
		loadFileTextField.setEditable(false);
		loadFileTextField.setToolTipText(Constants.MISSING_LABEL);// 单击以更改文件路径\n支持Excel97-03文件。
		GridBagConstraints gbc_loadFileTextField = new GridBagConstraints();
		gbc_loadFileTextField.gridwidth = 2;
		gbc_loadFileTextField.insets = new Insets(0, 0, 5, 0);
		gbc_loadFileTextField.fill = GridBagConstraints.BOTH;
		gbc_loadFileTextField.gridx = 1;
		gbc_loadFileTextField.gridy = 0;
		add(loadFileTextField, gbc_loadFileTextField);
		loadFileTextField.setColumns(10);

		loadFilePasswordLabel = new JLabel(Constants.MISSING_LABEL); // 读取密码
		GridBagConstraints gbc_loadFilePasswordLabel = new GridBagConstraints();
		gbc_loadFilePasswordLabel.anchor = GridBagConstraints.EAST;
		gbc_loadFilePasswordLabel.insets = new Insets(0, 0, 5, 5);
		gbc_loadFilePasswordLabel.gridx = 0;
		gbc_loadFilePasswordLabel.gridy = 1;
		add(loadFilePasswordLabel, gbc_loadFilePasswordLabel);

		loadFilePasswordField = new JPasswordField();
		loadFilePasswordField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				changePassword();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				changePassword();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				// Do nothing
			}

			private void changePassword() {
				Optional.ofNullable(vcb).ifPresent(vcb -> {
					if (Objects.isNull(excelLoadPasswordModel))
						return;
					String oldPassWord = excelLoadPasswordModel.get();
					String newPassword = new String(loadFilePasswordField.getPassword());
					lfDuplexingForecast.offer(new Object[] { "fireSet", oldPassWord, newPassword });
					vcb.changeLoadPassword(newPassword);
				});
			}

		});
		loadFilePasswordField.setToolTipText(Constants.MISSING_LABEL);// 如果文件有密码保护，则输入读取密码，否则不填。
		GridBagConstraints gbc_loadFilePasswordField = new GridBagConstraints();
		gbc_loadFilePasswordField.insets = new Insets(0, 0, 5, 5);
		gbc_loadFilePasswordField.fill = GridBagConstraints.BOTH;
		gbc_loadFilePasswordField.gridx = 1;
		gbc_loadFilePasswordField.gridy = 1;
		add(loadFilePasswordField, gbc_loadFilePasswordField);
		loadFileButton = new JButton(Constants.MISSING_LABEL);// 加载文件。
		loadFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Optional.ofNullable(vcb).ifPresent(vcb -> vcb.loadFile());
			}
		});
		GridBagConstraints gbc_loadFileButton = new GridBagConstraints();
		gbc_loadFileButton.insets = new Insets(0, 0, 5, 0);
		gbc_loadFileButton.fill = GridBagConstraints.BOTH;
		gbc_loadFileButton.gridx = 2;
		gbc_loadFileButton.gridy = 1;
		add(loadFileButton, gbc_loadFileButton);

		exportFileLabel = new JLabel(Constants.MISSING_LABEL);// 导出文件。
		GridBagConstraints gbc_exportFileLabel = new GridBagConstraints();
		gbc_exportFileLabel.anchor = GridBagConstraints.EAST;
		gbc_exportFileLabel.insets = new Insets(0, 0, 5, 5);
		gbc_exportFileLabel.gridx = 0;
		gbc_exportFileLabel.gridy = 2;
		add(exportFileLabel, gbc_exportFileLabel);

		exportFileTextField = new JTextField();
		exportFileTextField.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		exportFileTextField.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Optional.ofNullable(vcb).ifPresent(vcb -> vcb.selectExportFile());
			}
		});
		exportFileTextField.setEditable(false);
		GridBagConstraints gbc_exportFileTextField = new GridBagConstraints();
		gbc_exportFileTextField.gridwidth = 2;
		gbc_exportFileTextField.insets = new Insets(0, 0, 5, 0);
		gbc_exportFileTextField.fill = GridBagConstraints.BOTH;
		gbc_exportFileTextField.gridx = 1;
		gbc_exportFileTextField.gridy = 2;
		add(exportFileTextField, gbc_exportFileTextField);
		exportFileTextField.setColumns(10);

		exportFilePasswordLabel = new JLabel(Constants.MISSING_LABEL);// 导出密码。
		GridBagConstraints gbc_exportFilePasswordLabel = new GridBagConstraints();
		gbc_exportFilePasswordLabel.anchor = GridBagConstraints.EAST;
		gbc_exportFilePasswordLabel.insets = new Insets(0, 0, 0, 5);
		gbc_exportFilePasswordLabel.gridx = 0;
		gbc_exportFilePasswordLabel.gridy = 3;
		add(exportFilePasswordLabel, gbc_exportFilePasswordLabel);

		exportFilePasswordField = new JPasswordField();
		exportFilePasswordField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				changePassword();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				changePassword();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				// Do nothing
			}

			private void changePassword() {
				Optional.ofNullable(vcb).ifPresent(vcb -> {
					if (Objects.isNull(excelExportPasswordModel))
						return;
					String oldPassWord = excelExportPasswordModel.get();
					String newPassword = new String(exportFilePasswordField.getPassword());
					efDuplexingForecast.offer(new Object[] { "fireSet", oldPassWord, newPassword });
					vcb.changeExportPassword(newPassword);
				});
			}

		});
		GridBagConstraints gbc_exportFilePasswordField = new GridBagConstraints();
		gbc_exportFilePasswordField.insets = new Insets(0, 0, 0, 5);
		gbc_exportFilePasswordField.fill = GridBagConstraints.BOTH;
		gbc_exportFilePasswordField.gridx = 1;
		gbc_exportFilePasswordField.gridy = 3;
		add(exportFilePasswordField, gbc_exportFilePasswordField);

		exportFileButton = new JButton(Constants.MISSING_LABEL); // 导出。
		exportFileButton.setEnabled(false);
		exportFileButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Optional.ofNullable(vcb).ifPresent(vcb -> vcb.exportFile());
			}
		});
		GridBagConstraints gbc_exportFileButton = new GridBagConstraints();
		gbc_exportFileButton.fill = GridBagConstraints.BOTH;
		gbc_exportFileButton.gridx = 2;
		gbc_exportFileButton.gridy = 3;
		add(exportFileButton, gbc_exportFileButton);

		loadFilePasswordField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				"load-file");
		loadFilePasswordField.getActionMap().put("load-file", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				Optional.ofNullable(vcb).ifPresent(vcb -> vcb.loadFile());
			}
		});
		
		exportFilePasswordField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				"export-file");
		exportFilePasswordField.getActionMap().put("export-file", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				Optional.ofNullable(vcb).ifPresent(vcb -> vcb.exportFile());
			}
		});

		this.vcb = vcb;
		this.i18nHandler = i18nHandler;
		this.excelLoadFileModel = excelLoadFileModel;
		this.excelLoadPasswordModel = excelLoadPasswordModel;
		this.excelExportFileModel = excelExportFileModel;
		this.excelExportPasswordModel = excelExportPasswordModel;
		this.loadStateModel = loadStateModel;

		Optional.ofNullable(this.excelLoadFileModel).ifPresent(model -> model.addObverser(excelLoadFileObverser));
		Optional.ofNullable(this.excelLoadPasswordModel)
				.ifPresent(model -> model.addObverser(excelLoadPasswordObverser));
		Optional.ofNullable(this.excelExportFileModel).ifPresent(model -> model.addObverser(excelExportFileObverser));
		Optional.ofNullable(this.excelExportPasswordModel)
				.ifPresent(model -> model.addObverser(excelExportPasswordObverser));
		Optional.ofNullable(this.loadStateModel).ifPresent(model -> model.addObverser(excelLoadStateObverser));

		syncI18nHandler();
		syncExcelLoadFileModel();
		syncExcelLoadPasswordModel();
		syncExcelExportFileModel();
		syncExcelExportPasswordModel();
		syncloadStateModel();
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
		syncI18nHandler();
	}

	/**
	 * @return the excelLoadFileModel
	 */
	public ReferenceModel<File> getExcelLoadFileModel() {
		return excelLoadFileModel;
	}

	/**
	 * @param excelLoadFileModel
	 *            the excelLoadFileModel to set
	 */
	public void setExcelLoadFileModel(SyncReferenceModel<File> excelLoadFileModel) {
		Optional.ofNullable(this.excelLoadFileModel).ifPresent(model -> model.removeObverser(excelLoadFileObverser));
		this.excelLoadFileModel = excelLoadFileModel;
		Optional.ofNullable(this.excelLoadFileModel).ifPresent(model -> model.addObverser(excelLoadFileObverser));
		syncExcelLoadFileModel();
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
		Optional.ofNullable(this.excelLoadPasswordModel)
				.ifPresent(model -> model.removeObverser(excelLoadPasswordObverser));
		this.excelLoadPasswordModel = excelLoadPasswordModel;
		Optional.ofNullable(this.excelLoadPasswordModel)
				.ifPresent(model -> model.addObverser(excelLoadPasswordObverser));
		syncExcelLoadPasswordModel();
	}

	/**
	 * @return the excelExportFileModel
	 */
	public SyncReferenceModel<File> getExcelExportFileModel() {
		return excelExportFileModel;
	}

	/**
	 * @param excelExportFileModel
	 *            the excelExportFileModel to set
	 */
	public void setExcelExportFileModel(SyncReferenceModel<File> excelExportFileModel) {
		Optional.ofNullable(this.excelExportFileModel)
				.ifPresent(model -> model.removeObverser(excelExportFileObverser));
		this.excelExportFileModel = excelExportFileModel;
		Optional.ofNullable(this.excelExportFileModel).ifPresent(model -> model.addObverser(excelExportFileObverser));
		syncExcelExportFileModel();
	}

	/**
	 * @return the excelExportPasswordModel
	 */
	public SyncReferenceModel<String> getExcelExportPasswordModel() {
		return excelExportPasswordModel;
	}

	/**
	 * @param excelExportPasswordModel
	 *            the excelExportPasswordModel to set
	 */
	public void setExcelExportPasswordModel(SyncReferenceModel<String> excelExportPasswordModel) {
		Optional.ofNullable(this.excelExportPasswordModel)
				.ifPresent(model -> model.removeObverser(excelExportPasswordObverser));
		this.excelExportPasswordModel = excelExportPasswordModel;
		Optional.ofNullable(this.excelExportPasswordModel)
				.ifPresent(model -> model.addObverser(excelExportPasswordObverser));
		syncExcelExportPasswordModel();
	}

	/**
	 * 释放资源。
	 */
	public void dispose() {
		Optional.ofNullable(this.excelLoadFileModel).ifPresent(model -> model.removeObverser(excelLoadFileObverser));
	}

	private void syncI18nHandler() {
		loadFileLabel.setText(Constants.MISSING_LABEL);
		loadFilePasswordLabel.setText(Constants.MISSING_LABEL);
		loadFileTextField.setToolTipText(Constants.MISSING_LABEL);
		loadFilePasswordField.setToolTipText(Constants.MISSING_LABEL);
		loadFileButton.setText(Constants.MISSING_LABEL);
		exportFileLabel.setText(Constants.MISSING_LABEL);
		exportFilePasswordLabel.setText(Constants.MISSING_LABEL);
		exportFileButton.setText(Constants.MISSING_LABEL);

		if (Objects.isNull(i18nHandler))
			return;

		i18nHandler.getLock().readLock().lock();
		try {
			loadFileLabel.setText(i18nHandler.getStringOrDefault(I18nStringKey.LABEL_1, Constants.MISSING_LABEL));
			loadFilePasswordLabel
					.setText(i18nHandler.getStringOrDefault(I18nStringKey.LABEL_2, Constants.MISSING_LABEL));
			loadFileTextField
					.setToolTipText(i18nHandler.getStringOrDefault(I18nStringKey.LABEL_3, Constants.MISSING_LABEL));
			loadFilePasswordField
					.setToolTipText(i18nHandler.getStringOrDefault(I18nStringKey.LABEL_4, Constants.MISSING_LABEL));
			loadFileButton.setText(i18nHandler.getStringOrDefault(I18nStringKey.LABEL_6, Constants.MISSING_LABEL));
			exportFileLabel.setText(i18nHandler.getStringOrDefault(I18nStringKey.LABEL_24, Constants.MISSING_LABEL));
			exportFilePasswordLabel
					.setText(i18nHandler.getStringOrDefault(I18nStringKey.LABEL_25, Constants.MISSING_LABEL));
			exportFileButton.setText(i18nHandler.getStringOrDefault(I18nStringKey.LABEL_26, Constants.MISSING_LABEL));

		} finally {
			i18nHandler.getLock().readLock().unlock();
		}
	}

	private void syncExcelLoadFileModel() {
		loadFileTextField.setText("");

		if (Objects.isNull(excelLoadFileModel))
			return;

		excelLoadFileModel.getLock().readLock().lock();
		try {
			loadFileTextField.setText(
					Optional.ofNullable(excelLoadFileModel.get()).map(file -> file.getAbsolutePath()).orElse(""));
		} finally {
			excelLoadFileModel.getLock().readLock().unlock();
		}
	}

	private void syncExcelLoadPasswordModel() {
		loadFilePasswordField.setText("");

		if (Objects.isNull(excelLoadPasswordModel))
			return;

		excelLoadPasswordModel.getLock().readLock().lock();
		try {
			loadFilePasswordField.setText(Optional.ofNullable(excelLoadPasswordModel.get()).orElse(""));
		} finally {
			excelLoadPasswordModel.getLock().readLock().unlock();
		}
	}

	private void syncExcelExportFileModel() {
		exportFileTextField.setText("");

		if (Objects.isNull(excelExportFileModel))
			return;

		excelExportFileModel.getLock().readLock().lock();
		try {
			exportFileTextField.setText(
					Optional.ofNullable(excelExportFileModel.get()).map(file -> file.getAbsolutePath()).orElse(""));
		} finally {
			excelExportFileModel.getLock().readLock().unlock();
		}
	}

	private void syncExcelExportPasswordModel() {
		exportFilePasswordField.setText("");

		if (Objects.isNull(excelExportPasswordModel))
			return;

		excelExportPasswordModel.getLock().readLock().lock();
		try {
			exportFilePasswordField.setText(Optional.ofNullable(excelExportPasswordModel.get()).orElse(""));
		} finally {
			excelExportPasswordModel.getLock().readLock().unlock();
		}
	}

	private void syncloadStateModel() {
		exportFileButton.setEnabled(false);

		if (Objects.isNull(loadStateModel))
			return;

		loadStateModel.getLock().readLock().lock();
		try {
			if (Objects.equals(loadStateModel.get(), ProgressState.FINISHED)) {
				exportFileButton.setEnabled(true);
			} else {
				exportFileButton.setEnabled(false);
			}
		} finally {
			loadStateModel.getLock().readLock().unlock();
		}

	}

	private boolean checkLfDuplexingForecast(Object[] objs) {
		if (lfDuplexingForecast.isEmpty()) {
			return false;
		}

		if (Arrays.equals(lfDuplexingForecast.peek(), objs)) {
			lfDuplexingForecast.poll();
			return true;
		} else {
			CT.trace("ooooooooh");
			syncExcelLoadPasswordModel();
			lfDuplexingForecast.clear();
			return false;
		}
	}

	private boolean checkEfDuplexingForecast(Object[] objs) {
		if (efDuplexingForecast.isEmpty()) {
			return false;
		}

		if (Arrays.equals(efDuplexingForecast.peek(), objs)) {
			efDuplexingForecast.poll();
			return true;
		} else {
			CT.trace("ooooooooh");
			syncExcelLoadPasswordModel();
			efDuplexingForecast.clear();
			return false;
		}
	}

}
