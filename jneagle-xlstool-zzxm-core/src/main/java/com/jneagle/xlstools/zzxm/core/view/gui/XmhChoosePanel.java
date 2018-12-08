package com.jneagle.xlstools.zzxm.core.view.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import javax.swing.AbstractAction;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import com.dwarfeng.dutil.basic.cna.model.DefaultReferenceModel;
import com.dwarfeng.dutil.basic.cna.model.ReferenceModel;
import com.dwarfeng.dutil.basic.cna.model.SyncReferenceModel;
import com.dwarfeng.dutil.basic.cna.model.SyncSetModel;
import com.dwarfeng.dutil.basic.cna.model.obv.ReferenceAdapter;
import com.dwarfeng.dutil.basic.cna.model.obv.ReferenceObverser;
import com.dwarfeng.dutil.basic.cna.model.obv.SetAdapter;
import com.dwarfeng.dutil.basic.cna.model.obv.SetObverser;
import com.dwarfeng.dutil.basic.gui.awt.ImageSize;
import com.dwarfeng.dutil.basic.gui.awt.ImageUtil;
import com.dwarfeng.dutil.basic.gui.swing.SwingUtil;
import com.dwarfeng.dutil.develop.i18n.SyncI18nHandler;
import com.jneagle.xlstools.zzxm.core.model.cm.SyncMachiningModel;
import com.jneagle.xlstools.zzxm.core.model.eum.I18nStringKey;
import com.jneagle.xlstools.zzxm.core.model.eum.ImageKey;
import com.jneagle.xlstools.zzxm.core.model.eum.ProgressState;
import com.jneagle.xlstools.zzxm.core.model.obv.MachiningAdapter;
import com.jneagle.xlstools.zzxm.core.model.obv.MachiningObverser;
import com.jneagle.xlstools.zzxm.core.model.struct.MachiningInfo;
import com.jneagle.xlstools.zzxm.core.util.Constants;
import com.jneagle.xlstools.zzxm.core.view.struct.UpdatableListModel;
import com.jneagle.xlstools.zzxm.core.view.struct.ViewControlBridge;

public class XmhChoosePanel extends JPanel {

	private final JTextField textField;
	private final JList<String> xmhSelectList;
	private final JList<String> xmhIndicateList;
	private final JButton arrowButton;
	private final JButton selectAllButton;
	private final JButton removeAllButton;
	private final JButton removeButton;
	private final JPopupMenu popup;
	// label.19 = %s - 共%s条数据，%s条筛选数据，%s条无效数据
	private final ReferenceModel<String> labelRef_1;
	// label.43 = [无效]
	private final ReferenceModel<String> labelRef_2;

	private ViewControlBridge vcb;
	private SyncI18nHandler i18nHandler;
	private SyncMachiningModel machiningModel;
	private SyncMachiningModel filteredMachiningModel;
	private SyncMachiningModel failedMachiningModel;
	private SyncSetModel<String> xmhSelectModel;
	private SyncReferenceModel<ProgressState> loadStateModel;
	private SyncReferenceModel<ProgressState> filterStateModel;
	private SyncReferenceModel<ProgressState> exportStateModel;

	private final UpdatableListModel<String> xmhSelectListModel = new UpdatableListModel<>();
	private final UpdatableListModel<String> xmhIndicateListModel = new UpdatableListModel<>();

	private final ListCellRenderer<Object> xmhCellRenderer = new DefaultListCellRenderer() {

		private final Icon unknown_blue = new ImageIcon(
				ImageUtil.getInternalImage(ImageKey.UNKNOWN_BLUE, ImageUtil.getDefaultImage(), ImageSize.ICON_SMALL));
		private final Icon checked_green = new ImageIcon(
				ImageUtil.getInternalImage(ImageKey.CHECKED_GREEN, ImageUtil.getDefaultImage(), ImageSize.ICON_SMALL));
		private final Icon checked_yellow = new ImageIcon(
				ImageUtil.getInternalImage(ImageKey.CHECKED_YELLOW, ImageUtil.getDefaultImage(), ImageSize.ICON_SMALL));
		private final Icon checked_red = new ImageIcon(
				ImageUtil.getInternalImage(ImageKey.CHECKED_RED, ImageUtil.getDefaultImage(), ImageSize.ICON_SMALL));

		/**
		 * {@inheritDoc}
		 */
		@Override
		public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

			if (!(value instanceof String))
				return this;

			// 定义变量。
			String xmh = (String) value;
			RenderInfo renderInfo = Optional.ofNullable(renderInfoMap.get(xmh)).orElse(new RenderInfo());
			// 设置图标。
			setIcon(iconOfRenderInfo(renderInfo));
			// 设置标签。
			String machiningString = Optional.ofNullable(renderInfo).filter(info -> info.isMachiningFlag())
					.map(info -> Integer.toString(info.getMachiningCount())).orElse(labelRef_2.get());
			String filterString = Optional.ofNullable(renderInfo).filter(info -> info.isFilterFlag())
					.map(info -> Integer.toString(info.getFilterCount())).orElse(labelRef_2.get());
			String failString = Optional.ofNullable(renderInfo).filter(info -> info.isFailFlag())
					.map(info -> Integer.toString(info.getFailCount())).orElse(labelRef_2.get());
			setText(String.format(labelRef_1.get(), xmh, machiningString, filterString, failString));
			// 返回自身。
			return this;
		}

		private Icon iconOfRenderInfo(RenderInfo renderInfo) {
			if (!(renderInfo.isMachiningFlag() && renderInfo.isFilterFlag() && renderInfo.isFailFlag())) {
				return unknown_blue;
			} else

			if (renderInfo.getFailCount() > 0) {
				return checked_red;
			} else if (renderInfo.getFilterCount() > 0) {
				return checked_green;
			} else {
				return checked_yellow;
			}
		}

	};
	private final SetObverser<String> xmhSelectObverser = new SetAdapter<String>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireAdded(String element) {
			SwingUtil.invokeInEventQueue(() -> {
				xmhSelectListModel.add(element);
			});
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireRemoved(String element) {
			SwingUtil.invokeInEventQueue(() -> {
				xmhSelectListModel.remove(element);
			});
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireCleared() {
			SwingUtil.invokeInEventQueue(() -> {
				xmhSelectListModel.clear();
			});
		}

	};
	private final ReferenceObverser<ProgressState> loadStateObverser = new ReferenceAdapter<ProgressState>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireSet(ProgressState oldValue, ProgressState newValue) {
			SwingUtil.invokeInEventQueue(() -> {
				if (Objects.equals(newValue, ProgressState.FINISHED)) {
					arrowButton.setEnabled(true);
					selectAllButton.setEnabled(true);
					xmhSelectList.repaint();
					xmhIndicateList.repaint();
				} else {
					arrowButton.setEnabled(false);
					selectAllButton.setEnabled(false);
				}
			});
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireCleared() {
			SwingUtil.invokeInEventQueue(() -> {
				arrowButton.setEnabled(false);
			});
		}

	};
	private final MachiningObverser machiningObverser = new MachiningAdapter() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireMachiningInfoAdded(String xmh, MachiningInfo info) {
			SwingUtil.invokeInEventQueue(() -> {
				if (!renderInfoMap.containsKey(xmh)) {
					renderInfoMap.put(xmh, new RenderInfo(1, 0, 0, true, true, true));
					if (xmh.indexOf(textField.getText()) >= 0 && !xmhSelectModel.contains(xmh)) {
						xmhIndicateListModel.add(xmh);
					}
				} else {
					renderInfoMap.get(xmh).setMachiningFlag(true);
					renderInfoMap.get(xmh).setMachiningCount(renderInfoMap.get(xmh).getMachiningCount() + 1);
					updateListModel(xmh);
				}
			});
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireMachiningInfoRemoved(String xmh, MachiningInfo info) {
			SwingUtil.invokeInEventQueue(() -> {
				if (!renderInfoMap.containsKey(xmh)) {
					renderInfoMap.put(xmh, new RenderInfo(0, 0, 0, true, true, true));
					xmhIndicateListModel.add(xmh);
				} else {
					renderInfoMap.get(xmh).setMachiningFlag(true);
					renderInfoMap.get(xmh)
							.setMachiningCount(Optional.ofNullable(renderInfoMap.get(xmh).getMachiningCount() + 1)
									.filter(value -> value >= 0).orElse(0));
					updateListModel(xmh);
				}
			});
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireCleared() {
			SwingUtil.invokeInEventQueue(() -> {
				disableAllMachiningFlag();
			});
		}

	};
	private final MachiningObverser filteredMachiningObverser = new MachiningAdapter() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireMachiningInfoAdded(String xmh, MachiningInfo info) {
			SwingUtil.invokeInEventQueue(() -> {
				if (!renderInfoMap.containsKey(xmh)) {
					renderInfoMap.put(xmh, new RenderInfo(0, 1, 0, true, true, true));
					if (xmh.indexOf(textField.getText()) >= 0 && !xmhSelectModel.contains(xmh)) {
						xmhIndicateListModel.add(xmh);
					}
				} else {
					renderInfoMap.get(xmh).setFilterFlag(true);
					renderInfoMap.get(xmh).setFilterCount(renderInfoMap.get(xmh).getFilterCount() + 1);
					updateListModel(xmh);
				}
			});
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireMachiningInfoRemoved(String xmh, MachiningInfo info) {
			SwingUtil.invokeInEventQueue(() -> {
				SwingUtil.invokeInEventQueue(() -> {
					if (!renderInfoMap.containsKey(xmh)) {
						renderInfoMap.put(xmh, new RenderInfo(0, 0, 0, true, true, true));
						xmhIndicateListModel.add(xmh);
					} else {
						renderInfoMap.get(xmh).setFilterFlag(true);
						renderInfoMap.get(xmh)
								.setFilterCount(Optional.ofNullable(renderInfoMap.get(xmh).getFilterCount() + 1)
										.filter(value -> value >= 0).orElse(0));
						updateListModel(xmh);
					}
				});
			});
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireCleared() {
			SwingUtil.invokeInEventQueue(() -> {
				disableAllFilterFlag();
			});
		}

	};
	private final MachiningObverser failedMachiningObverser = new MachiningAdapter() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireMachiningInfoAdded(String xmh, MachiningInfo info) {
			SwingUtil.invokeInEventQueue(() -> {
				if (!renderInfoMap.containsKey(xmh)) {
					renderInfoMap.put(xmh, new RenderInfo(0, 0, 1, true, false, true));
					if (xmh.indexOf(textField.getText()) >= 0 && !xmhSelectModel.contains(xmh)) {
						xmhIndicateListModel.add(xmh);
					}
				} else {
					renderInfoMap.get(xmh).setFailFlag(true);
					renderInfoMap.get(xmh).setFailCount(renderInfoMap.get(xmh).getFailCount() + 1);
					updateListModel(xmh);
				}
			});
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireMachiningInfoRemoved(String xmh, MachiningInfo info) {
			SwingUtil.invokeInEventQueue(() -> {
				SwingUtil.invokeInEventQueue(() -> {
					if (!renderInfoMap.containsKey(xmh)) {
						renderInfoMap.put(xmh, new RenderInfo(0, 0, 0, true, true, true));
						xmhIndicateListModel.add(xmh);
					} else {
						renderInfoMap.get(xmh).setFailFlag(true);
						renderInfoMap.get(xmh)
								.setFailCount(Optional.ofNullable(renderInfoMap.get(xmh).getFailCount() + 1)
										.filter(value -> value >= 0).orElse(0));
						updateListModel(xmh);
					}
				});
			});
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireCleared() {
			SwingUtil.invokeInEventQueue(() -> {
				disableAddFailFlag();
			});
		}

	};
	private final ReferenceObverser<String> xmhCellRendererI18nObverser = new ReferenceAdapter<String>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireSet(String oldValue, String newValue) {
			xmhSelectListModel.updateAll();
			xmhIndicateListModel.updateAll();
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireCleared() {
			xmhSelectListModel.updateAll();
			xmhIndicateListModel.updateAll();
		}

	};

	private final Map<String, RenderInfo> renderInfoMap = new LinkedHashMap<>();
	private boolean textFieldAdjustFlag = false;

	public XmhChoosePanel() {
		this(null, null, null, null, null, null, null);
	}

	/**
	 * Create the panel.
	 * 
	 * @param vcb
	 * @param i18nHandler
	 * @param machiningModel
	 * @param xmhSelectModel
	 * @param loadStateModel
	 */
	public XmhChoosePanel(ViewControlBridge vcb, SyncI18nHandler i18nHandler, SyncMachiningModel machiningModel,
			SyncMachiningModel filteredMachiningModel, SyncMachiningModel failedMachiningModel,
			SyncSetModel<String> xmhSelectModel, SyncReferenceModel<ProgressState> loadStateModel) {
		GridBagLayout gridBagLayout = new GridBagLayout();
		gridBagLayout.columnWidths = new int[] { 0, 0, 0 };
		gridBagLayout.rowHeights = new int[] { 0, 0, 0 };
		gridBagLayout.columnWeights = new double[] { 1.0, 0.0, Double.MIN_VALUE };
		gridBagLayout.rowWeights = new double[] { 0.0, 1.0, Double.MIN_VALUE };
		setLayout(gridBagLayout);

		labelRef_1 = new DefaultReferenceModel<String>(Constants.MISSING_LABEL);
		labelRef_1.addObverser(xmhCellRendererI18nObverser);
		labelRef_2 = new DefaultReferenceModel<String>(Constants.MISSING_LABEL);
		labelRef_2.addObverser(xmhCellRendererI18nObverser);

		JPanel panel = new JPanel();
		GridBagConstraints gbc_panel = new GridBagConstraints();
		gbc_panel.gridwidth = 2;
		gbc_panel.insets = new Insets(5, 0, 5, 0);
		gbc_panel.fill = GridBagConstraints.BOTH;
		gbc_panel.gridx = 0;
		gbc_panel.gridy = 0;
		add(panel, gbc_panel);
		panel.setLayout(new BorderLayout(0, 0));

		textField = new JTextField();
		textField.setToolTipText(Constants.MISSING_LABEL);
		textField.getDocument().addDocumentListener(new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				update();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				update();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				update();
			}

			private void update() {
				if (!Objects.equals(XmhChoosePanel.this.loadStateModel.get(), ProgressState.FINISHED)) {
					popup.setVisible(false);
					return;
				}
				if (textFieldAdjustFlag)
					return;
				updateFilter(textField.getText());
				maySelectFirstPerson();
				if (!popup.isVisible()) {
					showPopup();
				}
			}

		});
		panel.add(textField, BorderLayout.CENTER);
		textField.setColumns(10);

		arrowButton = new JButton();
		arrowButton.setPreferredSize(new Dimension(24, 12));
		arrowButton.setBorder(null);
		arrowButton.setIcon(new ImageIcon(ImageUtil.getInternalImage(ImageKey.ARROW_BLUE, ImageSize.ICON_SMALL)));
		arrowButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (!Objects.equals(XmhChoosePanel.this.loadStateModel.get(), ProgressState.FINISHED)) {
					popup.setVisible(false);
					return;
				}
				if (textFieldAdjustFlag)
					return;
				updateFilter(textField.getText());
				maySelectFirstPerson();
				if (!popup.isVisible()) {
					showPopup();
				}
			}
		});
		panel.add(arrowButton, BorderLayout.EAST);

		JScrollPane scrollPane = new JScrollPane();
		GridBagConstraints gbc_scrollPane = new GridBagConstraints();
		gbc_scrollPane.fill = GridBagConstraints.BOTH;
		gbc_scrollPane.gridx = 0;
		gbc_scrollPane.gridy = 1;
		add(scrollPane, gbc_scrollPane);

		xmhSelectList = new JList<>();
		xmhSelectList.setModel(xmhSelectListModel);
		xmhSelectList.setCellRenderer(xmhCellRenderer);
		xmhSelectList.setBorder(null);
		scrollPane.setViewportView(xmhSelectList);

		xmhIndicateList = new JList<>();
		xmhIndicateList.setModel(xmhIndicateListModel);
		xmhIndicateList.setCellRenderer(xmhCellRenderer);
		xmhIndicateList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		xmhIndicateList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					String xmh = xmhIndicateList.getSelectedValue();
					if (Objects.nonNull(xmh)) {
						textFieldAdjustFlag = true;
						textField.setText(xmh);
						textFieldAdjustFlag = false;
						if (popup.isVisible()) {
							popup.setVisible(false);
							textField.requestFocus();
						}
					}
				}
			}
		});

		JPanel panel_1 = new JPanel();
		GridBagConstraints gbc_panel_1 = new GridBagConstraints();
		gbc_panel_1.fill = GridBagConstraints.BOTH;
		gbc_panel_1.gridx = 1;
		gbc_panel_1.gridy = 1;
		add(panel_1, gbc_panel_1);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0, 0, 0, 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 1.0, 0.0, 0.0, 0.0, 1.0, Double.MIN_VALUE };
		panel_1.setLayout(gbl_panel_1);

		selectAllButton = new JButton(Constants.MISSING_LABEL);// 全部选择。
		selectAllButton.setEnabled(false);
		selectAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Optional.ofNullable(vcb).ifPresent(vcb -> vcb.commitAllXmh());
			}
		});
		GridBagConstraints gbc_selectAllButton = new GridBagConstraints();
		gbc_selectAllButton.insets = new Insets(0, 0, 5, 0);
		gbc_selectAllButton.fill = GridBagConstraints.BOTH;
		gbc_selectAllButton.gridx = 0;
		gbc_selectAllButton.gridy = 1;
		panel_1.add(selectAllButton, gbc_selectAllButton);

		removeAllButton = new JButton(Constants.MISSING_LABEL);// 全部删除。
		removeAllButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Optional.ofNullable(vcb).ifPresent(vcb -> vcb.uncommitAllXmh());
			}
		});
		GridBagConstraints gbc_removeAllButton = new GridBagConstraints();
		gbc_removeAllButton.insets = new Insets(0, 0, 5, 0);
		gbc_removeAllButton.gridx = 0;
		gbc_removeAllButton.gridy = 2;
		panel_1.add(removeAllButton, gbc_removeAllButton);

		removeButton = new JButton(Constants.MISSING_LABEL);// 删除选中。
		removeButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Optional.ofNullable(vcb)
						.ifPresent(vcb -> vcb.uncommitSpecifiedXmh(xmhSelectList.getSelectedValuesList()));
			}
		});
		GridBagConstraints gbc_removeButton = new GridBagConstraints();
		gbc_removeButton.insets = new Insets(0, 0, 5, 0);
		gbc_removeButton.gridx = 0;
		gbc_removeButton.gridy = 3;
		panel_1.add(removeButton, gbc_removeButton);

		popup = new JPopupMenu();
		popup.setFocusable(false);
		popup.addPopupMenuListener(new PopupMenuListener() {

			@Override
			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			@Override
			public void popupMenuCanceled(PopupMenuEvent e) {
				textField.requestFocus();
			}
		});
		popup.add(new XmhIndicatePopupPanel());

		textField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "hide-popup");
		textField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), "focus-popup");
		textField.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "commit-xmh");
		textField.getActionMap().put("hide-popup", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (popup.isVisible()) {
					popup.setVisible(false);
				}
			}
		});
		textField.getActionMap().put("focus-popup", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (!popup.isVisible()) {
					if (!Objects.equals(XmhChoosePanel.this.loadStateModel.get(), ProgressState.FINISHED)) {
						popup.setVisible(false);
						return;
					}
					if (textFieldAdjustFlag)
						return;
					updateFilter(textField.getText());
					maySelectFirstPerson();
					if (!popup.isVisible()) {
						showPopup();
					}
				}
				xmhIndicateList.requestFocus();
			}
		});
		textField.getActionMap().put("commit-xmh", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				String xmh = textField.getText();
				if (Objects.isNull(xmh) || xmh.length() == 0)
					return;
				textFieldAdjustFlag = true;
				textField.setText("");
				textFieldAdjustFlag = false;
				if (popup.isVisible()) {
					popup.setVisible(false);
				}
				Optional.ofNullable(vcb).ifPresent(vcb -> vcb.commitSelectedXmh(xmh));
			}
		});

		xmhIndicateList.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
				.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "hide-popup");
		xmhIndicateList.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0),
				"commit");
		xmhIndicateList.getActionMap().put("hide-popup", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				if (popup.isVisible()) {
					popup.setVisible(false);
				}
				textField.requestFocus();
			}
		});
		xmhIndicateList.getActionMap().put("commit", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				String xmh = xmhIndicateList.getSelectedValue();
				if (Objects.nonNull(xmh)) {
					textFieldAdjustFlag = true;
					textField.setText(xmh);
					textFieldAdjustFlag = false;
				}
				if (popup.isVisible()) {
					popup.setVisible(false);
					textField.requestFocus();
				}
			}
		});

		xmhSelectList.getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0),
				"uncommit-selected-xmh");
		xmhSelectList.getActionMap().put("uncommit-selected-xmh", new AbstractAction() {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				Collection<String> c = xmhSelectList.getSelectedValuesList();
				if (!c.isEmpty())
					Optional.of(vcb).ifPresent(vcb -> vcb.uncommitSpecifiedXmh(c));
			}
		});

		this.vcb = vcb;
		this.i18nHandler = i18nHandler;
		this.machiningModel = machiningModel;
		this.filteredMachiningModel = filteredMachiningModel;
		this.failedMachiningModel = failedMachiningModel;
		this.xmhSelectModel = xmhSelectModel;
		this.loadStateModel = loadStateModel;

		Optional.ofNullable(this.xmhSelectModel).ifPresent(model -> model.addObverser(xmhSelectObverser));
		Optional.ofNullable(this.loadStateModel).ifPresent(model -> model.addObverser(loadStateObverser));
		Optional.ofNullable(this.machiningModel).ifPresent(model -> model.addObverser(machiningObverser));
		Optional.ofNullable(this.filteredMachiningModel)
				.ifPresent(model -> model.addObverser(filteredMachiningObverser));
		Optional.ofNullable(this.failedMachiningModel).ifPresent(model -> model.addObverser(failedMachiningObverser));

		syncI18nHandler();
		syncXmhSelectModel();
		syncloadStateModel();
		syncMachiningModel();
		syncFilterMachiningModel();
		syncFailedMachiningModel();
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
	}

	/**
	 * @return the xmhSelectModel
	 */
	public SyncSetModel<String> getXmhSelectModel() {
		return xmhSelectModel;
	}

	/**
	 * @param xmhSelectModel
	 *            the xmhSelectModel to set
	 */
	public void setXmhSelectModel(SyncSetModel<String> xmhSelectModel) {
		Optional.ofNullable(this.xmhSelectModel).ifPresent(model -> model.removeObverser(xmhSelectObverser));
		this.xmhSelectModel = xmhSelectModel;
		Optional.ofNullable(this.xmhSelectModel).ifPresent(model -> model.addObverser(xmhSelectObverser));
		syncXmhSelectModel();
	}

	/**
	 * @return the loadStateModel
	 */
	public SyncReferenceModel<ProgressState> getloadStateModel() {
		return loadStateModel;
	}

	/**
	 * @param loadStateModel
	 *            the loadStateModel to set
	 */
	public void setloadStateModel(SyncReferenceModel<ProgressState> loadStateModel) {
		Optional.ofNullable(this.loadStateModel).ifPresent(model -> model.removeObverser(loadStateObverser));
		this.loadStateModel = loadStateModel;
		Optional.ofNullable(this.loadStateModel).ifPresent(model -> model.addObverser(loadStateObverser));
		syncloadStateModel();
	}

	/**
	 * @return the machiningModel
	 */
	public SyncMachiningModel getMachiningModel() {
		return machiningModel;
	}

	/**
	 * @param machiningModel
	 *            the machiningModel to set
	 */
	public void setMachiningModel(SyncMachiningModel machiningModel) {
		Optional.ofNullable(this.machiningModel).ifPresent(model -> model.removeObverser(machiningObverser));
		this.machiningModel = machiningModel;
		Optional.ofNullable(this.machiningModel).ifPresent(model -> model.addObverser(machiningObverser));
		syncMachiningModel();
	}

	/**
	 * @return the filteredMachiningModel
	 */
	public SyncMachiningModel getFilteredMachiningModel() {
		return filteredMachiningModel;
	}

	/**
	 * @param filteredMachiningModel
	 *            the filteredMachiningModel to set
	 */
	public void setFilteredMachiningModel(SyncMachiningModel filteredMachiningModel) {
		Optional.ofNullable(this.filteredMachiningModel)
				.ifPresent(model -> model.removeObverser(filteredMachiningObverser));
		this.filteredMachiningModel = filteredMachiningModel;
		Optional.ofNullable(this.filteredMachiningModel)
				.ifPresent(model -> model.addObverser(filteredMachiningObverser));
		syncFilterMachiningModel();
	}

	/**
	 * @return the failedMachiningModel
	 */
	public SyncMachiningModel getFailedMachiningModel() {
		return failedMachiningModel;
	}

	/**
	 * @param failedMachiningModel
	 *            the failedMachiningModel to set
	 */
	public void setFailedMachiningModel(SyncMachiningModel failedMachiningModel) {
		Optional.ofNullable(this.failedMachiningModel)
				.ifPresent(model -> model.removeObverser(failedMachiningObverser));
		this.failedMachiningModel = failedMachiningModel;
		Optional.ofNullable(this.failedMachiningModel).ifPresent(model -> model.addObverser(failedMachiningObverser));
		syncFailedMachiningModel();
	}

	/**
	 * 释放资源。
	 */
	public void dispose() {
		Optional.ofNullable(this.xmhSelectModel).ifPresent(model -> model.removeObverser(xmhSelectObverser));
		Optional.ofNullable(this.loadStateModel).ifPresent(model -> model.removeObverser(loadStateObverser));
		Optional.ofNullable(this.machiningModel).ifPresent(model -> model.removeObverser(machiningObverser));
		Optional.ofNullable(this.filteredMachiningModel)
				.ifPresent(model -> model.removeObverser(filteredMachiningObverser));
		Optional.ofNullable(this.failedMachiningModel)
				.ifPresent(model -> model.removeObverser(failedMachiningObverser));
	}

	private void syncI18nHandler() {
		xmhSelectList.repaint();
		xmhIndicateList.repaint();
		selectAllButton.setText(Constants.MISSING_LABEL);
		removeAllButton.setText(Constants.MISSING_LABEL);
		removeButton.setText(Constants.MISSING_LABEL);
		textField.setToolTipText(Constants.MISSING_LABEL);
		labelRef_1.set(Constants.MISSING_LABEL);
		labelRef_2.set(Constants.MISSING_LABEL);

		if (Objects.isNull(i18nHandler))
			return;

		i18nHandler.getLock().readLock().lock();
		try {
			xmhSelectList.repaint();
			xmhIndicateList.repaint();
			selectAllButton.setText(i18nHandler.getStringOrDefault(I18nStringKey.LABEL_20, Constants.MISSING_LABEL));
			removeAllButton.setText(i18nHandler.getStringOrDefault(I18nStringKey.LABEL_21, Constants.MISSING_LABEL));
			removeButton.setText(i18nHandler.getStringOrDefault(I18nStringKey.LABEL_22, Constants.MISSING_LABEL));
			textField.setToolTipText(i18nHandler.getStringOrDefault(I18nStringKey.LABEL_23, Constants.MISSING_LABEL));
			labelRef_1.set(i18nHandler.getStringOrDefault(I18nStringKey.LABEL_19, Constants.MISSING_LABEL));
			labelRef_2.set(i18nHandler.getStringOrDefault(I18nStringKey.LABEL_43, Constants.MISSING_LABEL));
		} finally {
			i18nHandler.getLock().readLock().unlock();
		}
	}

	private void syncXmhSelectModel() {
		xmhSelectListModel.clear();

		if (Objects.isNull(xmhSelectModel))
			return;

		xmhSelectModel.getLock().readLock().lock();
		try {
			xmhSelectModel.forEach(item -> xmhSelectListModel.add(item));
		} finally {
			xmhSelectModel.getLock().readLock().unlock();
		}

	}

	private void syncloadStateModel() {
		arrowButton.setEnabled(false);
		selectAllButton.setEnabled(false);
		xmhSelectList.repaint();
		xmhIndicateList.repaint();

		if (Objects.isNull(loadStateModel))
			return;

		loadStateModel.getLock().readLock().lock();
		try {
			if (Objects.equals(loadStateModel.get(), ProgressState.FINISHED)) {
				arrowButton.setEnabled(true);
				selectAllButton.setEnabled(true);
				xmhSelectList.repaint();
				xmhIndicateList.repaint();
			} else {
				arrowButton.setEnabled(false);
			}
		} finally {
			loadStateModel.getLock().readLock().unlock();
		}
	}

	private void syncMachiningModel() {
		disableAllMachiningFlag();

		if (Objects.isNull(machiningModel))
			return;

		machiningModel.getLock().readLock().lock();
		try {
			for (String xmh : machiningModel.xmhSet()) {
				int count = machiningModel.getMachiningInfos(xmh).size();
				if (!renderInfoMap.containsKey(xmh)) {
					renderInfoMap.put(xmh, new RenderInfo(count, 0, 0, true, true, true));
					if (xmh.indexOf(textField.getText()) >= 0 && !xmhSelectModel.contains(xmh)) {
						xmhIndicateListModel.add(xmh);
					}
				} else {
					renderInfoMap.get(xmh).setMachiningFlag(true);
					renderInfoMap.get(xmh).setMachiningCount(count);
					updateListModel(xmh);
				}
			}
		} finally {
			machiningModel.getLock().readLock().unlock();
		}
	}

	private void syncFilterMachiningModel() {
		disableAllFilterFlag();

		if (Objects.isNull(filteredMachiningModel))
			return;

		filteredMachiningModel.getLock().readLock().lock();
		try {
			for (String xmh : filteredMachiningModel.xmhSet()) {
				int count = filteredMachiningModel.getMachiningInfos(xmh).size();
				if (!renderInfoMap.containsKey(xmh)) {
					renderInfoMap.put(xmh, new RenderInfo(0, count, 0, true, true, true));
					if (xmh.indexOf(textField.getText()) >= 0 && !xmhSelectModel.contains(xmh)) {
						xmhIndicateListModel.add(xmh);
					}
				} else {
					renderInfoMap.get(xmh).setFilterFlag(true);
					renderInfoMap.get(xmh).setFilterCount(count);
					updateListModel(xmh);
				}
			}
		} finally {
			filteredMachiningModel.getLock().readLock().unlock();
		}
	}

	private void syncFailedMachiningModel() {
		disableAllFilterFlag();

		if (Objects.isNull(failedMachiningModel))
			return;

		failedMachiningModel.getLock().readLock().lock();
		try {
			for (String xmh : failedMachiningModel.xmhSet()) {
				int count = failedMachiningModel.getMachiningInfos(xmh).size();
				if (!renderInfoMap.containsKey(xmh)) {
					renderInfoMap.put(xmh, new RenderInfo(0, 0, count, true, true, true));
					if (xmh.indexOf(textField.getText()) >= 0 && !xmhSelectModel.contains(xmh)) {
						xmhIndicateListModel.add(xmh);
					}
				} else {
					renderInfoMap.get(xmh).setFilterFlag(true);
					renderInfoMap.get(xmh).setFilterCount(count);
					updateListModel(xmh);
				}
			}
		} finally {
			failedMachiningModel.getLock().readLock().unlock();
		}
	}

	private void updateFilter(String filterString) {
		xmhIndicateListModel.clear();
		renderInfoMap.keySet().forEach(xmh -> {
			if (Objects.isNull(xmh))
				return;
			if (xmh.indexOf(filterString) >= 0 && !xmhSelectModel.contains(xmh)) {
				xmhIndicateListModel.add(xmh);
			}
		});
	}

	private void maySelectFirstPerson() {
		if (xmhIndicateListModel.size() >= 0) {
			xmhIndicateList.setSelectedIndex(0);
			xmhIndicateList.ensureIndexIsVisible(0);
		}
	}

	private void showPopup() {
		Point point = SwingUtilities.convertPoint(textField, 2, textField.getHeight(), this);
		popup.setPreferredSize(new Dimension(textField.getWidth() + arrowButton.getWidth() - 4, 150));
		popup.show(this, point.x, point.y);
	}

	private void updateListModel(String xmh) {
		Optional.ofNullable(xmhSelectListModel.indexOf(xmh)).filter(index -> index >= 0)
				.ifPresent(index -> xmhSelectListModel.update(index));
		Optional.ofNullable(xmhIndicateListModel.indexOf(xmh)).filter(index -> index >= 0)
				.ifPresent(index -> xmhIndicateListModel.update(index));
	}

	private void disableAllMachiningFlag() {
		for (Iterator<Map.Entry<String, RenderInfo>> it = renderInfoMap.entrySet().iterator(); it.hasNext();) {
			Entry<String, RenderInfo> entry = it.next();
			RenderInfo renderInfo = entry.getValue();
			String xmh = entry.getKey();
			if (!renderInfo.isFilterFlag() && !renderInfo.isFailFlag()) {
				it.remove();
				xmhSelectListModel.remove(xmh);
				xmhIndicateListModel.remove(xmh);
			} else {
				renderInfo.setMachiningFlag(false);
				updateListModel(xmh);
			}
		}
	}

	private void disableAllFilterFlag() {
		for (Iterator<Map.Entry<String, RenderInfo>> it = renderInfoMap.entrySet().iterator(); it.hasNext();) {
			Entry<String, RenderInfo> entry = it.next();
			RenderInfo renderInfo = entry.getValue();
			String xmh = entry.getKey();
			if (!renderInfo.isMachiningFlag() && !renderInfo.isFailFlag()) {
				it.remove();
				xmhSelectListModel.remove(xmh);
				xmhIndicateListModel.remove(xmh);
			} else {
				renderInfo.setFilterFlag(false);
				updateListModel(xmh);
			}
		}
	}

	private void disableAddFailFlag() {
		for (Iterator<Map.Entry<String, RenderInfo>> it = renderInfoMap.entrySet().iterator(); it.hasNext();) {
			Entry<String, RenderInfo> entry = it.next();
			RenderInfo renderInfo = entry.getValue();
			String xmh = entry.getKey();
			if (!renderInfo.isMachiningFlag() && !renderInfo.isFilterFlag()) {
				it.remove();
				xmhSelectListModel.remove(xmh);
				xmhIndicateListModel.remove(xmh);
			} else {
				renderInfo.setFailFlag(false);
				updateListModel(xmh);
			}
		}
	}

	private final class XmhIndicatePopupPanel extends JPanel {

		private static final long serialVersionUID = 1L;

		public XmhIndicatePopupPanel() {
			setLayout(new BorderLayout());

			JScrollPane scrollPane = new JScrollPane();
			scrollPane.setBorder(null);
			add(scrollPane, BorderLayout.CENTER);
			scrollPane.getViewport().setView(xmhIndicateList);
		}

	}

	private static class RenderInfo {
		private int machiningCount = 0;
		private int filterCount = 0;
		private int failCount = 0;

		private boolean machiningFlag = false;
		private boolean filterFlag = false;
		private boolean failFlag = false;

		public RenderInfo() {
			this(0, 0, 0, false, false, false);
		}

		public RenderInfo(int machiningCount, int filterCount, int failCount, boolean machiningFlag, boolean filterFlag,
				boolean failFlag) {
			this.machiningCount = machiningCount;
			this.filterCount = filterCount;
			this.failCount = failCount;
			this.machiningFlag = machiningFlag;
			this.filterFlag = filterFlag;
			this.failFlag = failFlag;
		}

		public int getMachiningCount() {
			return machiningCount;
		}

		public void setMachiningCount(int machiningCount) {
			this.machiningCount = machiningCount;
		}

		public int getFilterCount() {
			return filterCount;
		}

		public void setFilterCount(int filterCount) {
			this.filterCount = filterCount;
		}

		public int getFailCount() {
			return failCount;
		}

		public void setFailCount(int failCount) {
			this.failCount = failCount;
		}

		public boolean isMachiningFlag() {
			return machiningFlag;
		}

		public void setMachiningFlag(boolean machiningFlag) {
			this.machiningFlag = machiningFlag;
		}

		public boolean isFilterFlag() {
			return filterFlag;
		}

		public void setFilterFlag(boolean filterFlag) {
			this.filterFlag = filterFlag;
		}

		public boolean isFailFlag() {
			return failFlag;
		}

		public void setFailFlag(boolean failFlag) {
			this.failFlag = failFlag;
		}

	}

}
