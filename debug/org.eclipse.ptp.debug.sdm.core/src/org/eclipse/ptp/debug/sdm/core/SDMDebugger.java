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
package org.eclipse.ptp.debug.sdm.core;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.ArrayAttribute;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IntegerAttribute;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.debug.core.IPDebugger;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.pdi.IPDIDebugger;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.Session;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIManagerFactory;
import org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory;
import org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory;
import org.eclipse.ptp.debug.sdm.core.pdi.PDIDebugger;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.rm.remote.core.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;

/**
 * @author clement
 *
 */
public class SDMDebugger implements IPDebugger {
	private IPDIDebugger pdiDebugger = null;
	private IPDIModelFactory modelFactory = null;
	private IPDIManagerFactory managerFactory = null;
	private IPDIEventFactory eventFactory = null;
	private IPDIRequestFactory requestFactory = null;

	IFileStore routingFileStore = null;
	private List<String> dbgArgs;
	private IRemoteProcess sdmProcess = null;
	private IRemoteProcessBuilder sdmProcessBuilder = null;
	
	private class SDMStarter extends Thread {
		private CoreException e;
		private IPLaunch launch;
		
		public SDMStarter(IPLaunch launch) {
			super();
			this.launch = launch;
		}

		@Override
		public void run() {
			synchronized (this) {
				try {
					wait(3000);
					startMasterSDM(launch);
					
					/*
					 * Check if process has completed early (failed)
					 */
					wait(100);
					if (sdmProcess.isCompleted()) {
						throw new CoreException(new Status(IStatus.ERROR, SDMDebugCorePlugin.getUniqueIdentifier(), NLS.bind("Master SDM process finished early with exit code {0}.", sdmProcess.exitValue()), e));
					}
				} catch (InterruptedException e) {
					stopMasterSDM();
					this.e = new CoreException(new Status(IStatus.ERROR, SDMDebugCorePlugin.getUniqueIdentifier(), "Start of master SDM was interrupted", e));
				} catch (CoreException e) {
					this.e = new CoreException(new Status(IStatus.ERROR, SDMDebugCorePlugin.getUniqueIdentifier(), "Failed to create master SDM process", e));
				}
			}
		}
				
		/**
		 * Gets the exception that describes why SDM master failed to start.
		 * @return A CoreException if SDM failed to start of null if SDM was successful.
		 */
		public CoreException getCoreException() {
			return e;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPDebugger#createDebugSession(long, org.eclipse.ptp.debug.core.launch.IPLaunch, org.eclipse.core.runtime.IPath)
	 */
	public IPDISession createDebugSession(long timeout, final IPLaunch launch, IPath corefile) throws CoreException {
		if (modelFactory == null) {
			modelFactory = new SDMModelFactory();
		}
		if (managerFactory == null) {
			managerFactory = new SDMManagerFactory();
		}
		if (eventFactory == null) {
			eventFactory = new SDMEventFactory();
		}
		if (requestFactory == null) {
			requestFactory = new SDMRequestFactory();
		}
		
		/*
		 * Writing the rounting file actually starts the SDM servers.
		 */
		writeRoutingFile(launch);

		/*
		 * Delay starting the master SDM (aka SDM client), to wait intil SDM servers have started and until the sessions
		 * is listening on the debugger socket.
		 */
		SDMStarter sdmStarter = new SDMStarter(launch);
		sdmStarter.start();
		IPDISession session = createSession(timeout, launch, corefile);
		while (sdmStarter.isAlive()) {
			try {
				sdmStarter.join();
			} catch (InterruptedException e) {
				// Ignore and continue waiting.
			}
		}
		if (sdmStarter.getCoreException() != null) {
			throw sdmStarter.getCoreException();
		}
		return session;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPDebugger#initialize(org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	public void initialize(ILaunchConfiguration configuration, AttributeManager attrMgr, IProgressMonitor monitor) throws CoreException {
		ArrayAttribute<String> dbgArgsAttr = attrMgr.getAttribute(JobAttributes.getDebuggerArgumentsAttributeDefinition());

		if (dbgArgsAttr == null) {
			dbgArgsAttr = JobAttributes.getDebuggerArgumentsAttributeDefinition().create();
			attrMgr.addAttribute(dbgArgsAttr);
		}

		List<String> dbgArgs = dbgArgsAttr.getValue();

		try {
			getDebugger().initialize(configuration, dbgArgs, monitor);
		} catch (PDIException e) {
			throw newCoreException(e);
		}

		prepareRoutingFile(configuration, attrMgr, monitor);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.IPDebugger#getLaunchAttributes(org.eclipse.debug.core.ILaunchConfiguration, org.eclipse.ptp.core.attributes.AttributeManager)
	 */
	public void getLaunchAttributes(ILaunchConfiguration configuration, AttributeManager attrMgr) throws CoreException {
		ArrayAttribute<String> dbgArgsAttr = attrMgr.getAttribute(JobAttributes.getDebuggerArgumentsAttributeDefinition());

		if (dbgArgsAttr == null) {
			dbgArgsAttr = JobAttributes.getDebuggerArgumentsAttributeDefinition().create();
			attrMgr.addAttribute(dbgArgsAttr);
		}

		dbgArgs = dbgArgsAttr.getValue();

		Preferences store = SDMDebugCorePlugin.getDefault().getPluginPreferences();

		String localAddress = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_HOST, "localhost"); //$NON-NLS-1$

		dbgArgs.add("--host=" + localAddress); //$NON-NLS-1$
		dbgArgs.add("--debugger=" + store.getString(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_TYPE)); //$NON-NLS-1$

		String dbgPath = store.getString(SDMPreferenceConstants.SDM_DEBUGGER_BACKEND_PATH);
		if (dbgPath.length() > 0) {
			dbgArgs.add("--debugger_path=" + dbgPath); //$NON-NLS-1$
		}

		String dbgExtraArgs = store.getString(SDMPreferenceConstants.SDM_DEBUGGER_ARGS);
		if (dbgExtraArgs.length() > 0) {
			dbgArgs.addAll(Arrays.asList(dbgExtraArgs.split(" "))); //$NON-NLS-1$
		}

		int numProcs = attrMgr.getAttribute(JobAttributes.getNumberOfProcessesAttributeDefinition()).getValue();
		dbgArgs.add("--numnodes=" + (numProcs+1)); //$NON-NLS-1$



		// remote setting
		String dbgExePath = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, (String)null);;
		if (dbgExePath == null) {
			dbgExePath = store.getString(SDMPreferenceConstants.SDM_DEBUGGER_FILE);
		}
		PTPLaunchPlugin.getDefault().verifyResource(dbgExePath, configuration);

		IPath path = new Path(dbgExePath);
		attrMgr.addAttribute(JobAttributes.getDebuggerExecutableNameAttributeDefinition().create(path.lastSegment()));
		attrMgr.addAttribute(JobAttributes.getDebuggerExecutablePathAttributeDefinition().create(path.removeLastSegments(1).toString()));

		String dbgWD = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_WORKING_DIR, (String)null);
		if (dbgWD != null) {
			StringAttribute wdAttr = (StringAttribute) attrMgr.getAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition());
			if (wdAttr != null) {
				wdAttr.setValueAsString(dbgWD);
			} else {
				attrMgr.addAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition().create(dbgWD));
			}
			attrMgr.addAttribute(JobAttributes.getExecutablePathAttributeDefinition().create(dbgWD + "/Debug")); //$NON-NLS-1$
		}
		attrMgr.addAttribute(JobAttributes.getDebugFlagAttributeDefinition().create(true));
		
		prepareMasterSDM(configuration, attrMgr, new NullProgressMonitor());
	}

//	public void cleanup(ILaunchConfiguration configuration,
//			AttributeManager attrMgr, IPLaunch launch) {
//		if (process != null) {
//			process.destroy();
//			process = null;
//		}
//	}

	/**
	 * Get the PDI debugger implementation. Creates the class if necessary.
	 *
	 * @return IPDIDebugger
	 */
	private IPDIDebugger getDebugger() {
		if (pdiDebugger == null) {
			pdiDebugger = new PDIDebugger();
		}
		return pdiDebugger;
	}

	/**
	 * Work out the expected number of processes in the job. If it hasn't been
	 * specified, assume one.
	 *
	 * @param job job that was launched
	 * @return number of processes
	 */
	private int getJobSize(IPJob job) {
		IntegerAttribute numProcAttr = job.getAttribute(JobAttributes.getNumberOfProcessesAttributeDefinition());
		if (numProcAttr != null) {
			return numProcAttr.getValue();
		}
		return 1;
	}

	/**
	 * Create a CoreException that can be thrown
	 *
	 * @param exception
	 * @return CoreException
	 */
	private CoreException newCoreException(Throwable exception) {
		MultiStatus status = new MultiStatus(SDMDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR, "Cannot start debugging", exception);
		status.add(new Status(IStatus.ERROR, SDMDebugCorePlugin.getUniqueIdentifier(), PTPDebugCorePlugin.INTERNAL_ERROR, exception == null ? new String() : exception.getLocalizedMessage(), exception));
		return new CoreException(status);
	}

	/**
	 * Create a PDI session
	 *
	 * @param timeout
	 * @param launch
	 * @param corefile
	 * @param monitor
	 * @return Session
	 * @throws CoreException
	 */
	protected Session createSession(long timeout, IPLaunch launch, IPath corefile) throws CoreException {
		IPJob job = launch.getPJob();
		int job_size = getJobSize(job);
		try {
			return new Session(managerFactory, requestFactory, eventFactory, modelFactory,
					launch.getLaunchConfiguration(), timeout, getDebugger(), job.getID(), job_size);
		}
		catch (PDIException e) {
			throw newCoreException(e);
		}
	}

	private IResourceManager getResourceManager(ILaunchConfiguration configuration) throws CoreException {
		IPUniverse universe = PTPCorePlugin.getDefault().getUniverse();
		IResourceManager[] rms = universe.getResourceManagers();
		String rmUniqueName = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME, (String)null);
		for (IResourceManager rm : rms) {
			if (rm.getState() == ResourceManagerAttributes.State.STARTED &&
					rm.getUniqueName().equals(rmUniqueName)) {
				return rm;
			}
		}
		return null;
	}

	private void prepareRoutingFile(ILaunchConfiguration configuration,
			AttributeManager attrMgr, IProgressMonitor monitor)
			throws CoreException {
		IPath routingFilePath = new Path(attrMgr.getAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition()).getValue());
		routingFilePath = routingFilePath.append("routing_file");

		IResourceManagerControl rm = (IResourceManagerControl) getResourceManager(configuration);
		IResourceManagerConfiguration conf = rm.getConfiguration();
		AbstractRemoteResourceManagerConfiguration remConf = (AbstractRemoteResourceManagerConfiguration)conf;
		IRemoteServices remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(remConf.getRemoteServicesId());
		IRemoteConnectionManager rconnMgr = remoteServices.getConnectionManager();
		IRemoteConnection rconn = rconnMgr.getConnection(remConf.getConnectionName());
		IRemoteFileManager remoteFileManager = remoteServices.getFileManager(rconn);

		try {
			this.routingFileStore = remoteFileManager.getResource(routingFilePath, monitor);
		} catch (IOException e) {
			throw newCoreException(e);
		}

		IFileInfo info = routingFileStore.fetchInfo();
		if (info.exists()) {
			try {
				routingFileStore.delete(0, monitor);
			} catch (CoreException e) {
				throw newCoreException(e);
			}
			routingFileStore.fetchInfo();
		}
	}

	private void writeRoutingFile(IPLaunch launch) throws CoreException {
		System.out.println("Write");
		IProgressMonitor monitor = new NullProgressMonitor();
		OutputStream os = null;
		try {
			os = routingFileStore.openOutputStream(0, monitor);
		} catch (CoreException e) {
			throw newCoreException(e);
		}
		PrintWriter pw = new PrintWriter(os);
		IPProcess processes[] = launch.getPJob().getProcesses();
		pw.format("%d\n", processes.length);
		int base = 10000;
		int range = 10000;
		Random random = new Random();
		for (IPProcess process : processes) {
			String index = process.getProcessIndex();
			IPNode node = process.getNode();
			String nodeName = node.getName();
			int portNumber = base + random.nextInt(range);
			pw.format("%s %s %d\n", index, nodeName, portNumber);
		}
		pw.close();
		try {
			os.close();
		} catch (IOException e) {
			throw newCoreException(e);
		}
	}
	
	private void prepareMasterSDM(ILaunchConfiguration configuration,
			AttributeManager attrMgr, IProgressMonitor monitor) throws CoreException {
		IResourceManagerControl rm = null;
		
		List<String> cmd = new ArrayList<String>();
		cmd.add(attrMgr.getAttribute(JobAttributes.getDebuggerExecutablePathAttributeDefinition()).getValue()+"/"+attrMgr.getAttribute(JobAttributes.getDebuggerExecutableNameAttributeDefinition()).getValue());
		cmd.addAll(dbgArgs);

		try {
			rm = (IResourceManagerControl) getResourceManager(configuration);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		IResourceManagerConfiguration conf = rm.getConfiguration();
		AbstractRemoteResourceManagerConfiguration remConf = (AbstractRemoteResourceManagerConfiguration)conf;
		IRemoteServices remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(remConf.getRemoteServicesId());
		IRemoteConnectionManager connectionManager = remoteServices.getConnectionManager();
		IRemoteConnection connection = connectionManager.getConnection(remConf.getConnectionName());
		sdmProcessBuilder = remoteServices.getProcessBuilder(connection, cmd);
		String workdir = attrMgr.getAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition()).getValue();
		IRemoteFileManager fileManager = remoteServices.getFileManager(connection);
		IFileStore directory = null;
		try {
			directory = fileManager.getResource(new Path(workdir), monitor);
		} catch (IOException e) {
			throw newCoreException(e);
		}
		sdmProcessBuilder.directory(directory);
	}
	
	private void startMasterSDM(IPLaunch launch) throws CoreException {
		assert sdmProcess == null;
		assert sdmProcessBuilder != null;
		try {
			sdmProcess = sdmProcessBuilder.start();
		} catch (IOException e) {
			throw newCoreException(e);
		}
		
		final BufferedReader err_reader = new BufferedReader(new InputStreamReader(sdmProcess.getErrorStream()));
		final BufferedReader out_reader = new BufferedReader(new InputStreamReader(sdmProcess.getInputStream()));

		new Thread(new Runnable() {
			public void run() {
				try {
					String output;
					while ((output = out_reader.readLine()) != null) {
						System.out.println("sdm master: " + output); //$NON-NLS-1$
					}
				} catch (IOException e) {
					// Ignore
				}
			}
		}, "SDM master standard output thread").start(); //$NON-NLS-1$
		
		new Thread(new Runnable() {
			public void run() {
				try {
					String line;
					while ((line = err_reader.readLine()) != null) {
						System.err.println("sdm master: " + line); //$NON-NLS-1$
					}
				} catch (IOException e) {
					// Ignore
				}
			}
		}, "SDM master error output thread").start(); //$NON-NLS-1$

		sdmProcessBuilder = null;
	}
	
	public void stopMasterSDM() {
		if (sdmProcess != null) {
			sdmProcess.destroy();
			sdmProcess = null;
		}
	}

}
