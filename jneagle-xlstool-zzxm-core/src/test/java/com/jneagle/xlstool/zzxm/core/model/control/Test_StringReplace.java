package com.jneagle.xlstool.zzxm.core.model.control;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class Test_StringReplace {

	@Test
	public final void test() {
		assertEquals("bbb", "aaa".replace("a", "b"));
	}

}
