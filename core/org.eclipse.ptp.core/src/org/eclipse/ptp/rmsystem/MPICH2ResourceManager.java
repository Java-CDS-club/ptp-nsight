/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
package org.eclipse.ptp.rmsystem;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.rtsystem.mpich2.MPICH2ControlSystem;
import org.eclipse.ptp.rtsystem.mpich2.MPICH2MonitoringSystem;
import org.eclipse.ptp.rtsystem.mpich2.MPICH2ProxyRuntimeClient;

public class MPICH2ResourceManager extends RuntimeResourceManager {

	public MPICH2ResourceManager(IPUniverseControl universe,
			IResourceManagerConfiguration config) {
		super(universe, config);
	}

	protected void doStartRuntime(IProgressMonitor monitor)
			throws CoreException {
		/* load up the control and monitoring systems for OMPI */
		monitor.subTask("Starting MPICH2 proxy runtime...");
		MPICH2ProxyRuntimeClient runtimeProxy = new MPICH2ProxyRuntimeClient();
		setRuntimeProxy(runtimeProxy);
		monitor.worked(10);
		
		if(!runtimeProxy.startup(monitor)) {
			System.err.println("Failed to start up the proxy runtime.");
			runtimeProxy = null;
			setRuntimeProxy(runtimeProxy);
			if (monitor.isCanceled()) {
				throw new CoreException(Status.CANCEL_STATUS);
			}
			throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), IStatus.ERROR, 
					"There was an error starting the MPICH2 proxy runtime.  The path to 'ptp_mpich2_proxy' "+
					"may have been incorrect. Try checking the console log or error logs for more detailed information.",
					null));
		}
		monitor.subTask("Starting MPICH2 monitoring system...");
		setMonitoringSystem(new MPICH2MonitoringSystem(runtimeProxy));
		monitor.worked(10);
		monitor.subTask("Starting MPICH2 control system...");
		setControlSystem(new MPICH2ControlSystem(runtimeProxy));
		monitor.worked(10);
	}

	protected void doStop() throws CoreException {
		// TODO Auto-generated method stub

	}

}
