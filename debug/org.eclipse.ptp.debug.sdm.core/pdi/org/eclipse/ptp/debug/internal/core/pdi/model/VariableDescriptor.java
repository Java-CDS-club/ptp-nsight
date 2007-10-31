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
package org.eclipse.ptp.debug.internal.core.pdi.model;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariableDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.internal.core.pdi.Session;
import org.eclipse.ptp.debug.internal.core.pdi.SessionObject;
import org.eclipse.ptp.debug.internal.core.pdi.request.GetPartialAIFRequest;

/**
 * @author clement
 *
 */
public abstract class VariableDescriptor extends SessionObject implements IPDIVariableDescriptor {
	// Casting info.
	protected String[] castingTypes;
	protected int castingIndex;
	protected int castingLength;

	protected String fName;
	protected int position;
	protected StackFrame fStackFrame;
	protected Thread fThread;
	protected int stackdepth;

	protected String qualifiedName = null;
	protected String fFullName = null;
	protected String fTypename = null;
	protected String varid = null;	
	protected IAIF aif = null;
	protected String sizeof = null;
	
	public VariableDescriptor(Session session, VariableDescriptor desc) {
		super(session, desc.getTasks());
		this.fName = desc.getName();
		this.fFullName = desc.fFullName;
		this.sizeof = desc.sizeof;
		try {
			this.fStackFrame = (StackFrame)desc.getStackFrame();
			this.fThread = (Thread)desc.getThread();
		} catch (PDIException e) {
		}
		this.position = desc.getPosition();
		this.stackdepth = desc.getStackDepth();
		this.castingIndex = desc.getCastingArrayStart();
		this.castingLength = desc.getCastingArrayEnd();
		this.castingTypes = desc.getCastingTypes();
	}
	public VariableDescriptor(Session session, BitList tasks, Thread thread, StackFrame stack, String n, String fn, int pos, int depth) {
		super(session, tasks);
		fName = n;
		fFullName = fn;
		fStackFrame = stack;
		fThread = thread;
		position = pos;
		stackdepth = depth;
	}
	public String getVarId() {
		return varid;
	}
	public int getPosition() {
		return position;
	}
	public int getStackDepth() {
		return stackdepth;
	}
	public void setCastingArrayEnd(int end) {
		castingLength = end;
	}
	public void setCastingArrayStart(int start) {
		castingIndex = start;
	}
	public int getCastingArrayEnd() {
		return castingLength;
	}
	public int getCastingArrayStart() {
		return castingIndex;
	}
	public void setCastingTypes(String[] t) {
		castingTypes = t;
	}
	public String[] getCastingTypes() {
		return castingTypes;
	}

	/*
	 * FIXME -- it designs for GDB
	 */
	public String encodeVariable() {
		String fn = getFullName();
		if (castingLength > 0 || castingIndex > 0) {
			StringBuffer buffer = new StringBuffer();
			buffer.append("*(");
			buffer.append('(').append(fn).append(')');
			buffer.append('+').append(castingIndex).append(')');
			buffer.append('@').append(castingLength);
			fn = buffer.toString();
		} else if (castingTypes != null && castingTypes.length > 0) {
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < castingTypes.length; ++i) {
				if (castingTypes[i] != null && castingTypes[i].length() > 0) {
					if (buffer.length() == 0) {
						buffer.append('(').append(castingTypes[i]).append(')');
						buffer.append(fn);
					} else {
						buffer.insert(0, '(');
						buffer.append(')');
						StringBuffer b = new StringBuffer();
						b.append('(').append(castingTypes[i]).append(')');
						buffer.insert(0, b.toString());
					}
				}
			}
			fn = buffer.toString();
		}
		return fn;
	}	
	public String getFullName() {
		if (fFullName == null) {
			fFullName = getName();
		}
		return fFullName;
	}
	public String getName() {
		return fName;
	}
	public IPDIStackFrame getStackFrame() throws PDIException {
		return fStackFrame;
	}
	public IPDIThread getThread() throws PDIException {
		return fThread;
	}
	public String getQualifiedName() throws PDIException {
		if (qualifiedName == null) {
			qualifiedName = encodeVariable();
		}
		return qualifiedName;
	}
	public String getTypeName() throws PDIException {
		if (fTypename == null) {
			fTypename = getAIF().getType().toString();
		}
		return fTypename;
	}
	
	public static boolean equalsCasting(VariableDescriptor var1, VariableDescriptor var2) {
		String[] castings1 = var1.getCastingTypes();
		String[] castings2 = var2.getCastingTypes();
		if (castings1 == null && castings2 == null) {
			return true;
		} else if (castings1 != null && castings2 != null && castings1.length == castings2.length) {
			for (int i = 0; i < castings1.length; ++i) {
				if (!castings1[i].equals(castings2[i])) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	public int sizeof() throws PDIException {
		return getAIF().getType().sizeof();
	}
	
	public IAIF getAIF() throws PDIException {
		if (aif == null) {
			Target target = (Target)fStackFrame.getTarget();
			Thread currentThread = (Thread)target.getCurrentThread();
			StackFrame currentFrame = currentThread.getCurrentStackFrame();
			target.lockTarget();
			try {
				target.setCurrentThread(fStackFrame.getThread(), false);				
				((Thread)fStackFrame.getThread()).setCurrentStackFrame(fStackFrame, false);
			
				GetPartialAIFRequest request = new GetPartialAIFRequest(getTasks(), getQualifiedName(), varid);
				session.getEventRequestManager().addEventRequest(request);
				aif = request.getPartialAIF(getTasks());
				varid = request.getVarId(getTasks());
			} 
			finally {
				target.setCurrentThread(currentThread, false);
				currentThread.setCurrentStackFrame(currentFrame, false);
				target.releaseTarget();
			}
		}
		return aif;
	}
	public void setAIF(IAIF aif) {
		this.aif = aif;
	}

	public boolean equals(IPDIVariableDescriptor varDesc) {
		if (varDesc instanceof VariableDescriptor) {
			VariableDescriptor desc = (VariableDescriptor) varDesc;
			if (desc.getName().equals(getName())
				&& desc.getCastingArrayStart() == getCastingArrayStart()
				&& desc.getCastingArrayEnd() == getCastingArrayEnd()
				&& equalsCasting(desc, this)) {

				// Check the threads
				IPDIThread varThread = null;
				IPDIThread ourThread = null;
				try {
					varThread = desc.getThread();
					ourThread = getThread();
				} catch (PDIException e) {
					// ignore
				}
				if ((ourThread == null && varThread == null) || (varThread != null && ourThread != null && varThread.equals(ourThread))) {
					// check the stackFrames
					IPDIStackFrame varFrame = null;
					IPDIStackFrame ourFrame = null;
					try {
						varFrame = desc.getStackFrame();
						ourFrame = getStackFrame();
					} catch (PDIException e) {
						// ignore
					}
					if (ourFrame == null && varFrame == null) {
						return true;
					} else if (varFrame != null && ourFrame != null && varFrame.equals(ourFrame)) {
						if (desc.getStackDepth() == getStackDepth()) {
							if (desc.getPosition() == getPosition()) {
								return true;
							}
						}
					}
				}
				return false;
			}
		}
		return super.equals(varDesc);
	}
	public IPDIVariableDescriptor getVariableDescriptorAsArray(int start, int length) throws PDIException {
		return session.getVariableManager().getVariableDescriptorAsArray(this, start, length);
	}
	public IPDIVariableDescriptor getVariableDescriptorAsType(String type) throws PDIException {
		return session.getVariableManager().getVariableDescriptorAsType(this, type);
	}
}
