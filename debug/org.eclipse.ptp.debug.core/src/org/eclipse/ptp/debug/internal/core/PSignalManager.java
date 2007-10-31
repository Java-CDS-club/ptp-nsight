/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.debug.internal.core;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.PDebugModel;
import org.eclipse.ptp.debug.core.model.IPSignal;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEventListener;
import org.eclipse.ptp.debug.core.pdi.model.IPDISignal;
import org.eclipse.ptp.debug.internal.core.model.PDebugTarget;
import org.eclipse.ptp.debug.internal.core.model.PSignal;

/**
 * @author Clement chu
 */
public class PSignalManager implements IAdaptable, IPDIEventListener {
	private class PSignalSet {
		PDebugTarget debugTarget = null;
		BitList sTasks;
		
		PSignalSet(BitList sTasks, PDebugTarget debugTarget) {
			this.sTasks = sTasks;
			this.debugTarget = debugTarget;
		}
		PDebugTarget getDebugTarget() {
			if (debugTarget == null)
				debugTarget = session.findDebugTarget(sTasks);
			return debugTarget;
		}
		IPSignal[] fSignals = null;
		boolean fIsDisposed = false;
		IPSignal[] getSignals() throws DebugException {
			if (!isDisposed() && fSignals == null) {
				try {
					IPDISignal[] pdiSignals = session.getPDISession().getSignalManager().getSignals(sTasks);
					ArrayList<IPSignal> list = new ArrayList<IPSignal>(pdiSignals.length);
					for(int i = 0; i < pdiSignals.length; ++i) {
						list.add(new PSignal(session, sTasks, pdiSignals[i]));
					}
					fSignals = (IPSignal[])list.toArray(new IPSignal[list.size()]);
				}
				catch(PDIException e) {
					throwDebugException(e.getMessage(), DebugException.TARGET_REQUEST_FAILED, e);
				}
			}
			return (fSignals != null) ? fSignals : new IPSignal[0];
		}
		void dispose() {
			if (fSignals != null)
				for(int i = 0; i < fSignals.length; ++i) {
					((PSignal)fSignals[i]).dispose();
				}
			fSignals = null;
			fIsDisposed = true;
		}
		void signalChanged(IPDISignal pdiSignal) {
			PSignal signal = find(pdiSignal);
			if (signal != null) {
				signal.fireChangeEvent(DebugEvent.STATE);
			}
		}
		PSignal find(IPDISignal pdiSignal) {
			try {
				IPSignal[] signals = getSignals();
				for(int i = 0; i < signals.length; ++i)
					if (signals[i].getName().equals(pdiSignal.getName()))
						return (PSignal)signals[i];
			}
			catch(DebugException e) {
			}
			return null;
		}
		boolean isDisposed() {
			return fIsDisposed;
		}
	}
	
	protected Map<BitList, PSignalSet> fPSignalSetMap;
	private PSession session;

	public PSignalManager(PSession session) {
		this.session = session;
	}
	public void initialize(IProgressMonitor monitor) {
		fPSignalSetMap = new Hashtable<BitList, PSignalSet>();
		//session.getPDISession().getEventManager().addEventListener(this);
	}
	public void dispose(IProgressMonitor monitor) {
		DebugPlugin.getDefault().asyncExec(new Runnable() {
			public void run() {
				synchronized(fPSignalSetMap) {
					Iterator<PSignalSet> it = fPSignalSetMap.values().iterator();
					while(it.hasNext()) {
						((PSignalSet)it.next()).dispose();
					}
					fPSignalSetMap.clear();
				}
			}
		});
		//session.getPDISession().getEventManager().removeEventListener(this);
	}	
	public PSignalSet getSignalSet(BitList qTasks) {
		synchronized (fPSignalSetMap) {
			PSignalSet set = (PSignalSet)fPSignalSetMap.get(qTasks);
			if (set == null) {
				set = new PSignalSet(qTasks, null);
				fPSignalSetMap.put(qTasks, set);
			}
			return set;
		}
	}
	public void dispose(BitList qTasks) {
		getSignalSet(qTasks).dispose();
	}
	public void signalChanged(BitList qTasks, IPDISignal pdiSignal) {
		getSignalSet(qTasks).signalChanged(pdiSignal);
	}
	public IPSignal[] getSignals(BitList qTasks) throws DebugException {
		return getSignalSet(qTasks).getSignals();
	}
	public Object getAdapter(Class adapter) {
		if (adapter.equals(IPDISession.class))
			return getSession();
		if (adapter.equals(PSignalManager.class))
			return this;
		return null;
	}
	protected void throwDebugException(String message, int code, Throwable exception) throws DebugException {
		throw new DebugException(new Status(IStatus.ERROR, PDebugModel.getPluginIdentifier(), code, message, exception));
	}
	protected IPSession getSession() {
		return session;
	}
	/****************************************
	 * IPDIEventListener
	 ****************************************/
	public void handleDebugEvents(IPDIEvent[] events) {
	}
}
