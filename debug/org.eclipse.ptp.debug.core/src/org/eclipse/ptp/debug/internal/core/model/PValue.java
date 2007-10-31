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
package org.eclipse.ptp.debug.internal.core.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.ptp.debug.core.model.IPDebugElementStatus;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.core.model.PVariableFormat;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;
import org.eclipse.ptp.debug.core.pdi.model.aif.AIFException;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFType;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeChar;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeFloat;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeInt;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypePointer;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeReference;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFTypeString;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValue;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueChar;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueFloat;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueInt;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValuePointer;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueReference;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIFValueString;
import org.eclipse.ptp.debug.core.pdi.model.aif.ITypeAggregate;
import org.eclipse.ptp.debug.core.pdi.model.aif.ITypeDerived;

/**
 * @author Clement chu
 *
 */
public class PValue extends AbstractPValue {
	private String fValueString = null;
	private List<IVariable> fVariables = Collections.EMPTY_LIST;
	private IPDIVariable fVariable;

	protected PValue(PVariable parent, IPDIVariable variable) {
		super(parent);
		fVariable = variable;		
	}
	protected PValue(PVariable parent, String message) {
		super(parent);
		setStatus(IPDebugElementStatus.ERROR, message);
	}
	public String getReferenceTypeName() throws DebugException {
		return (getParentVariable() != null) ? getParentVariable().getReferenceTypeName() : null;
	}
	public String getValueString() throws DebugException {
		if (fValueString == null && getAIF() != null) {
			resetStatus();
			IPStackFrame pframe = getParentVariable().getStackFrame();
			boolean isSuspended = (pframe == null) ? getPDISession().isSuspended(getTasks()) : pframe.isSuspended();
			if (isSuspended) {
				try {
					if (fVariable == null) {
						targetRequestFailed("No variable found", null);
					}
					fValueString = processUnderlyingValue(getAIF());
				} catch (AIFException pe) {
					setStatus(IPDebugElementStatus.ERROR, pe.getMessage());
				}
			}
		}
		return fValueString;
	}
	public boolean isAllocated() throws DebugException {
		return true;
	}
	public IVariable[] getVariables() throws DebugException {
		List<IVariable> list = getVariables0();
		return (IVariable[]) list.toArray(new IVariable[list.size()]);
	}
	protected synchronized List<IVariable> getVariables0() throws DebugException {
		if (!isAllocated() || !hasVariables())
			return Collections.EMPTY_LIST;
		if (fVariables.size() == 0) {
			try {
				List<IPDIVariable> vars = getPDIVariables();
				fVariables = new ArrayList<IVariable>(vars.size());
				Iterator<IPDIVariable> it = vars.iterator();
				while (it.hasNext()) {
					fVariables.add(PVariableFactory.createLocalVariable(this, (IPDIVariable)it.next()));
				}
				resetStatus();
			} catch (DebugException e) {
				setStatus(IPDebugElementStatus.ERROR, e.getMessage());
			}
		}
		return fVariables;
	}
	public boolean hasVariables() throws DebugException {
		IAIF aif = getAIF();
		if (aif != null) {
			IAIFType type = aif.getType();
			if (type instanceof ITypeAggregate || type instanceof ITypeDerived) {
				return true;
			}
		}
		return false;
	}
	public IAIF getAIF() throws DebugException {
		try {
			return fVariable.getAIF();
		} catch (PDIException e) {
			targetRequestFailed(e.getMessage(), e);
			return null;
		}
	}
	protected List<IPDIVariable> getPDIVariables() throws DebugException {
		IPDIVariable[] vars = null;
		try {
			if (fVariable != null) {
				vars = fVariable.getChildren();
				if (vars == null) {
					vars = new IPDIVariable[0];
				}
			}
		} catch (PDIException e) {
			requestFailed(e.getMessage(), e);
		}
		return Arrays.asList(vars);
	}
	protected synchronized void setChanged(boolean changed) {
		if (changed) {
			fValueString = null;
			resetStatus();
		}
		Iterator<IVariable> it = fVariables.iterator();
		while (it.hasNext()) {
			((AbstractPVariable) it.next()).setChanged(changed);
		}
	}
	public void dispose() {
		Iterator<IVariable> it = fVariables.iterator();
		while (it.hasNext()) {
			((AbstractPVariable) it.next()).dispose();
		}
	}
	protected String processUnderlyingValue(IAIF aif) throws AIFException {
		if (aif != null) {
			return processUnderlyingValue(aif.getType(), aif.getValue());
		}
		return null;
	}
	private String processUnderlyingValue(IAIFType type, IAIFValue value) throws AIFException {
		if (type instanceof IAIFTypeChar)
			return getCharValueString((IAIFValueChar) value);
		else if (type instanceof IAIFTypeInt)
			return getIntValueString((IAIFValueInt) value);
		else if (type instanceof IAIFTypeFloat)
			return getFloatingPointValueString((IAIFValueFloat) value);
		else if (type instanceof IAIFTypePointer)
			return getPointerValueString((IAIFValuePointer) value);
		else if (type instanceof IAIFTypeReference)
			return processUnderlyingValue(type, ((IAIFValueReference) value).getParent());
		else if (type instanceof IAIFTypeString)
			return getWCharValueString((IAIFValueString) value);
		else if (type instanceof ITypeAggregate)
			return "{...}";
		else
			return value.getValueString();
	}
	private String getCharValueString(IAIFValueChar value) throws AIFException {
		PVariableFormat format = getParentVariable().getFormat();
		char charValue = value.charValue();
		if (PVariableFormat.NATURAL.equals(format)) {
			return ((Character.isISOControl(charValue) && charValue != '\b' && charValue != '\t' && charValue != '\n' && charValue != '\f' && charValue != '\r') || charValue < 0) ? "" : "\'" + value.getValueString() + "\'";
		} else if (PVariableFormat.DECIMAL.equals(format)) {
			return Integer.toString((byte)charValue);
		} else if (PVariableFormat.HEXADECIMAL.equals(format)) {
			StringBuffer sb = new StringBuffer("0x");
			String stringValue = Integer.toString((byte)charValue);
			sb.append((stringValue.length() > 2) ? stringValue.substring(stringValue.length() - 2) : stringValue);
			return sb.toString();
		}
		return null;
	}
	private String getIntValueString(IAIFValueInt value) throws AIFException {
		PVariableFormat format = getParentVariable().getFormat();
		String stringValue = value.getValueString();
		if (PVariableFormat.NATURAL.equals(format) || PVariableFormat.DECIMAL.equals(format)) {
			return stringValue;
		} else if (PVariableFormat.HEXADECIMAL.equals(format)) {
			StringBuffer sb = new StringBuffer("0x");
			if (value.isShort()) {
				stringValue = Integer.toHexString(value.shortValue());
			} else if (value.isInt()) {
				stringValue = Integer.toHexString(value.intValue());
			} else if (value.isLong()) {
				stringValue = Long.toHexString(value.longValue());
			}
			sb.append((stringValue.length() > 8) ? stringValue.substring(stringValue.length() - 8) : stringValue);
			return sb.toString();
		}
		return null;
	}
	private String getFloatingPointValueString(IAIFValueFloat value) throws AIFException {
		if (value.isDouble()) {
			return getDoubleValueString(value.getValueString());
		} else if (value.isFloat()) {
			return getFloatValueString(value.getValueString());
		} else {
			return value.getValueString();
		}
	}
	private String getFloatValueString(String floatValue) throws AIFException {
		PVariableFormat format = getParentVariable().getFormat();
		if (PVariableFormat.NATURAL.equals(format)) {
			return floatValue;
		}
		Float flt = new Float(floatValue);
		if (flt.isNaN() || flt.isInfinite())
			return "";
		long longValue = flt.longValue();
		if (PVariableFormat.DECIMAL.equals(format)) {
			return Long.toString(longValue);
		} else if (PVariableFormat.HEXADECIMAL.equals(format)) {
			StringBuffer sb = new StringBuffer("0x");
			String stringValue = Long.toHexString(longValue);
			sb.append((stringValue.length() > 8) ? stringValue.substring(stringValue.length() - 8) : stringValue);
			return sb.toString();
		}
		return floatValue;
	}
	private String getDoubleValueString(String doubleValue) throws AIFException {
		PVariableFormat format = getParentVariable().getFormat();
		if (PVariableFormat.NATURAL.equals(format)) {
			return doubleValue;
		}
		Double dbl = new Double(doubleValue);
		if (dbl.isNaN() || dbl.isInfinite())
			return "";
		long longValue = dbl.longValue();
		if (PVariableFormat.DECIMAL.equals(format)) {
			return Long.toString(longValue);
		} else if (PVariableFormat.HEXADECIMAL.equals(format)) {
			StringBuffer sb = new StringBuffer("0x");
			String stringValue = Long.toHexString(longValue);
			sb.append((stringValue.length() > 16) ? stringValue.substring(stringValue.length() - 16) : stringValue);
			return sb.toString();
		}
		return doubleValue;
	}
	private String getPointerValueString(IAIFValuePointer value) throws AIFException {
		BigInteger pv = value.pointerValue();
		if (pv == null)
			return "";
		PVariableFormat format = getParentVariable().getFormat();
		if (PVariableFormat.NATURAL.equals(format) || PVariableFormat.HEXADECIMAL.equals(format))
			return pv.toString(16);
		if (PVariableFormat.DECIMAL.equals(format))
			return pv.toString(10);
		return null;
	}
	private String getWCharValueString(IAIFValueString value) throws AIFException {
		return value.getValueString();
	}
	protected void reset() {
		resetStatus();
		fValueString = null;
		Iterator<IVariable> it = fVariables.iterator();
		while (it.hasNext()) {
			((AbstractPVariable) it.next()).resetValue();
		}
	}
	protected void preserve() {
		setChanged(false);
		resetStatus();
		Iterator<IVariable> it = fVariables.iterator();
		while (it.hasNext()) {
			((AbstractPVariable) it.next()).preserve();
		}
	}
}
