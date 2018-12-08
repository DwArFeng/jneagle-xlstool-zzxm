package com.jneagle.xlstools.zzxm.core.view.gui;

import java.awt.BorderLayout;
import java.util.Objects;
import java.util.Optional;

import javax.swing.JPanel;
import javax.swing.JProgressBar;

import com.dwarfeng.dutil.basic.cna.model.SyncReferenceModel;
import com.dwarfeng.dutil.basic.cna.model.obv.ReferenceAdapter;
import com.dwarfeng.dutil.basic.cna.model.obv.ReferenceObverser;
import com.dwarfeng.dutil.basic.gui.swing.SwingUtil;

/**
 * 
 * @author DwArFeng
 * @since 1.1.0.a
 */
public class ProgressPanel extends JPanel {

	private final JProgressBar progressBar;

	/** 当前进度模型。 */
	private SyncReferenceModel<Integer> currentProgressModel;
	/** 总进度模型。 */
	private SyncReferenceModel<Integer> totleProgressModel;

	private final ReferenceObverser<Integer> currentProgressObverser = new ReferenceAdapter<Integer>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireSet(Integer oldValue, Integer newValue) {
			SwingUtil.invokeInEventQueue(() -> {
				progressStruct.setCurrentProgress(newValue);
			});
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireCleared() {
			SwingUtil.invokeInEventQueue(() -> {
				progressStruct.setCurrentProgress(0);
			});
		}

	};
	private final ReferenceObverser<Integer> totleProgressObverser = new ReferenceAdapter<Integer>() {

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireSet(Integer oldValue, Integer newValue) {
			SwingUtil.invokeInEventQueue(() -> {
				progressStruct.setTotleProgress(newValue);
				if (newValue >= 0) {
					progressStruct.setIndeterminate(false);
				} else {
					progressStruct.setIndeterminate(true);
				}
			});
		}

		/**
		 * {@inheritDoc}
		 */
		@Override
		public void fireCleared() {
			SwingUtil.invokeInEventQueue(() -> {
				progressStruct.setTotleProgress(-1);
				progressStruct.setIndeterminate(true);
			});
		}

	};

	private final ProgressStruct progressStruct = new ProgressStruct();

	/**
	 * 新实例。
	 */
	public ProgressPanel() {
		this(null, null);
	}

	/**
	 * Create the panel.
	 * 
	 * @param currentProgressModel
	 * @param totleProgressModel
	 */
	public ProgressPanel(SyncReferenceModel<Integer> currentProgressModel,
			SyncReferenceModel<Integer> totleProgressModel) {
		setLayout(new BorderLayout(0, 0));

		progressBar = new JProgressBar();
		add(progressBar, BorderLayout.CENTER);

		this.currentProgressModel = currentProgressModel;
		this.totleProgressModel = totleProgressModel;

		Optional.ofNullable(this.currentProgressModel).ifPresent(model -> model.addObverser(currentProgressObverser));
		Optional.ofNullable(this.totleProgressModel).ifPresent(model -> model.addObverser(totleProgressObverser));

		syncCurrentProgressModel();
		syncTotleProgressModel();
	}

	/**
	 * @return the currentProgressModel
	 */
	public SyncReferenceModel<Integer> getCurrentProgressModel() {
		return currentProgressModel;
	}

	/**
	 * @param currentProgressModel
	 *            the currentProgressModel to set
	 */
	public void setCurrentProgressModel(SyncReferenceModel<Integer> currentProgressModel) {
		Optional.ofNullable(this.currentProgressModel)
				.ifPresent(model -> model.removeObverser(currentProgressObverser));
		this.currentProgressModel = currentProgressModel;
		Optional.ofNullable(this.currentProgressModel).ifPresent(model -> model.addObverser(currentProgressObverser));
		syncCurrentProgressModel();
	}

	/**
	 * @return the totleProgressModel
	 */
	public SyncReferenceModel<Integer> getTotleProgressModel() {
		return totleProgressModel;
	}

	/**
	 * @param totleProgressModel
	 *            the totleProgressModel to set
	 */
	public void setTotleProgressModel(SyncReferenceModel<Integer> totleProgressModel) {
		Optional.ofNullable(this.totleProgressModel).ifPresent(model -> model.removeObverser(totleProgressObverser));
		this.totleProgressModel = totleProgressModel;
		Optional.ofNullable(this.totleProgressModel).ifPresent(model -> model.addObverser(totleProgressObverser));
		syncTotleProgressModel();
	}

	public void dispose() {
		Optional.ofNullable(this.currentProgressModel)
				.ifPresent(model -> model.removeObverser(currentProgressObverser));
		Optional.ofNullable(this.totleProgressModel).ifPresent(model -> model.removeObverser(totleProgressObverser));
	}

	private void syncCurrentProgressModel() {
		progressStruct.setIndeterminate(true);

		if (Objects.isNull(currentProgressModel))
			return;

		currentProgressModel.getLock().readLock().lock();
		try {
			progressStruct.setIndeterminate(true);
			progressStruct.setCurrentProgress(currentProgressModel.get());
		} finally {
			currentProgressModel.getLock().readLock().unlock();
		}
	}

	private void syncTotleProgressModel() {
		progressStruct.setIndeterminate(true);

		if (Objects.isNull(totleProgressModel))
			return;

		totleProgressModel.getLock().readLock().lock();
		try {
			if (totleProgressModel.get() >= 0) {
				progressStruct.setIndeterminate(false);
			}
			progressStruct.setTotleProgress(totleProgressModel.get());
		} finally {
			totleProgressModel.getLock().readLock().unlock();
		}
	}

	private final class ProgressStruct {

		private int currentProgress = 0;
		private int totleProgress = 0;
		private boolean indeterminate = true;

		// 遵守Bean规则，故保留。
		@SuppressWarnings("unused")
		public int getCurrentProgress() {
			return currentProgress;
		}

		public void setCurrentProgress(int currentProgress) {
			this.currentProgress = currentProgress;
			progressBar.setValue(currentProgress);
		}

		// 遵守Bean规则，故保留。
		@SuppressWarnings("unused")
		public int getTotleProgress() {
			return totleProgress;
		}

		public void setTotleProgress(int totleProgress) {
			this.totleProgress = totleProgress;
			progressBar.setMaximum(totleProgress);
		}

		// 遵守Bean规则，故保留。
		@SuppressWarnings("unused")
		public boolean isIndeterminate() {
			return indeterminate;
		}

		public void setIndeterminate(boolean indeterminate) {
			this.indeterminate = indeterminate;
			progressBar.setIndeterminate(indeterminate);
		}

	}

}
