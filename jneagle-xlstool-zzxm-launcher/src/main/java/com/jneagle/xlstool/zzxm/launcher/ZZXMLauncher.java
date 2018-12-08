package com.jneagle.xlstool.zzxm.launcher;

import com.dwarfeng.dutil.basic.io.CT;
import com.dwarfeng.dutil.basic.prog.ProcessException;
import com.jneagle.xlstool.zzxm.core.control.ZZXM;

public class ZZXMLauncher {

	public static void main(String[] args) {
		// TODO 处理命令行。

		ZZXM zzxm = new ZZXM();
		try {
			zzxm.getController().pose();
			zzxm.getController().awaitFinish();
		} catch (ProcessException | InterruptedException e) {
			System.exit(1);
		}
		CT.trace("Exit code is: " + zzxm.getController().getExitCode());
		System.exit(zzxm.getController().getExitCode());
	}

}
