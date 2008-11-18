/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.mpich2.core.rtsystem;


import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.rm.core.rmsystem.AbstractEffectiveTollRMConfiguration;
import org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem;
import org.eclipse.ptp.rm.mpi.mpich2.core.messages.Messages;
import org.eclipse.ptp.rm.mpi.mpich2.core.parameters.Parameters;
import org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem.EffectiveMPICH2ResourceManagerConfiguration;
import org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem.MPICH2ResourceManagerConfiguration;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class MPICH2RuntimeSystem extends AbstractToolRuntimeSystem {

	private Parameters params = new Parameters();

	/** The machine where open mpi is running on. */
	private String machineID;
	/** The queue that dispatches jobs to mpi. */
	private String queueID;
	/** List of hosts discovered for the machine. */
	private MPICH2HostMap hostMap;
	/** Mapping of discovered hosts and their ID for IPHost elements. */
	private Map<String,String> hostToElementMap = new HashMap<String, String>();

	public MPICH2RuntimeSystem(Integer openmpi_rmid,
			MPICH2ResourceManagerConfiguration config,
			AttributeDefinitionManager attrDefMgr) {
		super(openmpi_rmid, config, attrDefMgr);
	}

	protected void setMachineID(String machineID) {
		this.machineID = machineID;
	}

	protected void setQueueID(String queueID) {
		this.queueID = queueID;
	}

	public String getMachineID() {
		return machineID;
	}

	public String getQueueID() {
		return queueID;
	}

	public Parameters getParameters() {
		return params;
	}

	public String getNodeIDforName(String hostname) {
		return hostToElementMap.get(hostname);
	}

	public MPICH2HostMap getHostMap() {
		return hostMap;
	}

	protected void setHostMap(MPICH2HostMap hostMap) {
		this.hostMap = hostMap;
	}

	public Map<String, String> getHostToElementMap() {
		return hostToElementMap;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#doShutdown(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doShutdown(IProgressMonitor monitor) throws CoreException {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#doStartEvents()
	 */
	@Override
	protected void doStartEvents() throws CoreException {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#doStartup(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doStartup(IProgressMonitor monitor) throws CoreException {
		// Nothing to do
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.rm.core.rtsystem.AbstractToolRuntimeSystem#doStopEvents()
	 */
	@Override
	protected void doStopEvents() throws CoreException {
		// Nothing to do
	}

	@Override
	protected Job createDiscoverJob() {
		if (! rmConfiguration.hasDiscoverCmd())
			return null;
		Job job = new MPICH2DiscoverJob(this);
		job.setPriority(Job.INTERACTIVE);
		job.setSystem(false);
		job.setUser(false);
		return job;
	}

	@Override
	protected Job createPeriodicMonitorJob() {
		return null;
	}

	@Override
	protected Job createContinuousMonitorJob() {
		return null;
	}

	@Override
	public Job createRuntimeSystemJob(String jobID, String queueID, AttributeManager attrMgr) {
		return new MPICH2RuntimeSystemJob(jobID, queueID, Messages.MPICH2RuntimeSystem_JobName, this, attrMgr);
	}


	@Override
	public AbstractEffectiveTollRMConfiguration retrieveEffectiveToolRmConfiguration() {
		return new EffectiveMPICH2ResourceManagerConfiguration(getRmConfiguration());
	}
}
