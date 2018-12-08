package com.jneagle.xlstools.zzxm.core.model.cm;

import java.util.Collection;
import java.util.Set;

import com.dwarfeng.dutil.basic.prog.ObverserSet;
import com.jneagle.xlstools.zzxm.core.model.obv.MachiningObverser;
import com.jneagle.xlstools.zzxm.core.model.struct.MachiningInfo;

/**
 * 加工模型。
 * 
 * @author DwArFeng
 * @since 1.0.0.a
 */
public interface MachiningModel extends ObverserSet<MachiningObverser> {

	/**
	 * 
	 * @return
	 */
	public int size();

	/**
	 * 
	 * @return
	 */
	public int machiningInfoSize();

	/**
	 * 
	 * @return
	 */
	public boolean isEmpty();

	/**
	 * 
	 * @param xmh
	 * @return
	 */
	public boolean containsXmh(String xmh);

	/**
	 * 
	 * @return
	 */
	public Set<String> xmhSet();

	/**
	 * 
	 * @param xmh
	 * @return
	 */
	public Collection<MachiningInfo> getMachiningInfos(String xmh);

	/**
	 * 
	 * @param xmh
	 * @param info
	 * @return
	 * @throws UnsupportedOperationException
	 */
	public boolean addMachiningInfo(String xmh, MachiningInfo info) throws UnsupportedOperationException;

	/**
	 * 
	 * @param xmh
	 * @param info
	 * @return
	 * @throws UnsupportedOperationException
	 */
	public boolean removeMachiningInfo(String xmh, MachiningInfo info) throws UnsupportedOperationException;

	/**
	 * 
	 * @throws UnsupportedOperationException
	 */
	public void clear() throws UnsupportedOperationException;

	/**
	 * 
	 * @return
	 */
	@Override
	public int hashCode();

	/**
	 * 
	 * @param obj
	 * @return
	 */
	@Override
	public boolean equals(Object obj);

}
