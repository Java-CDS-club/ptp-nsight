package org.eclipse.ptp.debug.internal.ui.sourcelookup;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ptp.debug.core.sourcelookup.MappingSourceContainer;
import org.eclipse.ptp.debug.internal.core.sourcelookup.MapEntrySourceContainer;
import org.eclipse.ptp.debug.internal.ui.PDebugImage;
import org.eclipse.ui.model.IWorkbenchAdapter;

/**
 * @author clement
 *
 */
public class SourceContainerWorkbenchAdapter implements IWorkbenchAdapter {
	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getChildren(java.lang.Object)
	 */
	public Object[] getChildren(Object o) {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getImageDescriptor(java.lang.Object)
	 */
	public ImageDescriptor getImageDescriptor(Object o) {
		if (o instanceof MappingSourceContainer) {
			//TODO change image
			return PDebugImage.getDescriptor(PDebugImage.ICON_REGISTER_NORMAL);
		}
		if (o instanceof MapEntrySourceContainer) {
			//TODO change image
			return PDebugImage.getDescriptor(PDebugImage.ICON_REGISTER_NORMAL);
		}
		if (o instanceof ProjectSourceContainer) {
			IProject project = ((ProjectSourceContainer)o).getProject();
			if (project != null)
				return getImageDescriptor(project);
		}
		return null;
	}

	protected ImageDescriptor getImageDescriptor(IProject project) {
		IWorkbenchAdapter adapter = (IWorkbenchAdapter)project.getAdapter(IWorkbenchAdapter.class);
		if (adapter != null) {
			return adapter.getImageDescriptor(project);
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getLabel(java.lang.Object)
	 */
	public String getLabel(Object o) {
		if (o instanceof MappingSourceContainer) {
			return SourceLookupUIMessages.getString("SourceContainerWorkbenchAdapter.0") + ((MappingSourceContainer)o).getName(); 
		}
		if (o instanceof MapEntrySourceContainer) {
			return ((MapEntrySourceContainer)o).getName();
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ui.model.IWorkbenchAdapter#getParent(java.lang.Object)
	 */
	public Object getParent(Object o) {
		return null;
	}

	public String getQualifiedName(IPath path) {
		StringBuffer buffer = new StringBuffer();
		String[] segments = path.segments();
		if (segments.length > 0) {
			buffer.append(path.lastSegment());
			if (segments.length > 1) {
				buffer.append(" - "); 
				if (path.getDevice() != null) {
					buffer.append(path.getDevice());
				}
				for(int i = 0; i < segments.length - 1; i++) {
					buffer.append(File.separatorChar);
					buffer.append(segments[i]);
				}
			}
			return buffer.toString();
		}
		return "";
	}
}
