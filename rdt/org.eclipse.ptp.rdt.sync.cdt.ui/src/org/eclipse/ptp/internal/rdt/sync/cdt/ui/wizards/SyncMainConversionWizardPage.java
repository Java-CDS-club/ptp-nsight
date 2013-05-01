/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.cdt.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;

import org.eclipse.cdt.core.CCProjectNature;
import org.eclipse.cdt.core.CProjectNature;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSWizardHandler;
import org.eclipse.cdt.ui.CDTSharedImages;
import org.eclipse.cdt.ui.newui.PageLayout;
import org.eclipse.cdt.ui.wizards.CNewWizard;
import org.eclipse.cdt.ui.wizards.CWizardHandler;
import org.eclipse.cdt.ui.wizards.EntryDescriptor;
import org.eclipse.cdt.ui.wizards.IWizardItemsListListener;
import org.eclipse.cdt.ui.wizards.conversion.ConvertProjectWizardPage;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.osgi.util.TextProcessor;
import org.eclipse.ptp.internal.rdt.sync.cdt.core.Activator;
import org.eclipse.ptp.internal.rdt.sync.cdt.ui.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.SyncFileFilter;
import org.eclipse.ptp.rdt.sync.core.resources.RemoteSyncNature;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeParticipant;
import org.eclipse.ptp.rdt.sync.ui.widgets.SyncProjectWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.accessibility.AccessibleAdapter;
import org.eclipse.swt.accessibility.AccessibleEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.ide.IDEWorkbenchPlugin;
import org.eclipse.ui.internal.ide.IIDEHelpContextIds;

/**
 * Main wizard page for creating new synchronized projects. All elements needed for a synchronized project are configured here. This
 * includes: 1) Project name and workspace location 2) Remote connection and directory 3) Project type 4) Local and remote
 * toolchains
 * 
 * Since this wizard page's operation differs greatly from a normal CDT wizard page, this class simply reimplements (overrides) all
 * functionality in the two immediate superclasses (CDTMainWizardPage and WizardNewProjectCreationPage) but borrows much of the code
 * from those two classes. Thus, except for very basic functionality, such as jface methods, this class is self-contained.
 */
public class SyncMainConversionWizardPage extends ConvertProjectWizardPage implements IWizardItemsListListener {
	public static final String REMOTE_SYNC_WIZARD_PAGE_ID = "org.eclipse.ptp.rdt.sync.cdt.ui.remoteSyncWizardPage"; //$NON-NLS-1$
	public static final String SERVICE_PROVIDER_PROPERTY = "org.eclipse.ptp.rdt.sync.cdt.ui.remoteSyncWizardPage.serviceProvider"; //$NON-NLS-1$
	private static final String RDT_PROJECT_TYPE = "org.eclipse.ptp.rdt"; //$NON-NLS-1$
	private static final Image IMG_CATEGORY = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_SEARCHFOLDER);
	private static final Image IMG_ITEM = CDTSharedImages.getImage(CDTSharedImages.IMG_OBJS_VARIABLE);
	private static final String EXTENSION_POINT_ID = "org.eclipse.cdt.ui.CDTWizard"; //$NON-NLS-1$
	private static final String ELEMENT_NAME = "wizard"; //$NON-NLS-1$
	private static final String CLASS_NAME = "class"; //$NON-NLS-1$
	public static final String DESC = "EntryDescriptor"; //$NON-NLS-1$

	// widgets
	private Combo projectSelectionCombo;
	private Tree projectTypeTree;
	private Composite allToolChainsHiddenComposite;
	private Composite remoteToolChain;
	private Composite localToolChain;
	private Table remoteToolChainTable;
	private Table localToolChainTable;
	private Button showSupportedOnlyButton;
	private Label projectRemoteOptionsLabel;
	private Label projectLocalOptionsLabel;
	private Label categorySelectedForRemoteLabel;
	private Label categorySelectedForLocalLabel;
	private SyncProjectWidget fSyncWidget;
	private Composite compositeCDT;

	private SortedMap<String, IToolChain> toolChainMap;
	private String message = null;
	private int messageType = IMessageProvider.NONE;
	private String errorMessage = null;

	private CWizardHandler h_selected = null;

	private Set<String> localToolChainsSet = null;
	private Set<String> remoteToolChainsSet = null;

	/**
	 * Creates a new project creation wizard page.
	 * 
	 * @param pageName
	 *            the name of this page
	 */
	public SyncMainConversionWizardPage(String pageName) {
		super(pageName);
		setPageComplete(false);
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.conversion.ConvertProjectWizardPage#createControl(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);

		initializeDialogUnits(parent);
		PlatformUI.getWorkbench().getHelpSystem().setHelp(composite, IIDEHelpContextIds.NEW_PROJECT_WIZARD_PAGE);

		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		setControl(composite);

		createProjectBasicInfoGroup(composite);
		createProjectRemoteInfoGroup(composite);
		createProjectDetailedInfoGroup(composite);
		this.switchTo(this.updateData(projectTypeTree, allToolChainsHiddenComposite, false, SyncMainConversionWizardPage.this, getWizard()),
				getDescriptor(projectTypeTree));

		handleProjectSelected(null);
		setPageComplete(false);
		errorMessage = null;
		message = null;
		messageType = IMessageProvider.NONE;
		Dialog.applyDialogFont(composite);
	}

	private void createProjectDetailedInfoGroup(Composite parent) {
		compositeCDT = new Composite(parent, SWT.NONE);
		compositeCDT.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		compositeCDT.setLayout(new GridLayout(2, true));

		Label left_label = new Label(compositeCDT, SWT.NONE);
		left_label.setText(Messages.SyncMainWizardPage_0);
		left_label.setFont(parent.getFont());
		left_label.setLayoutData(new GridData(GridData.BEGINNING));

		projectRemoteOptionsLabel = new Label(compositeCDT, SWT.NONE);
		projectRemoteOptionsLabel.setFont(parent.getFont());
		projectRemoteOptionsLabel.setLayoutData(new GridData(GridData.BEGINNING));
		projectRemoteOptionsLabel.setText(Messages.SyncMainWizardPage_1);

		projectTypeTree = new Tree(compositeCDT, SWT.SINGLE | SWT.BORDER);
		projectTypeTree.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 3));
		projectTypeTree.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeItem[] tis = projectTypeTree.getSelection();
				if (tis == null || tis.length == 0) {
					return;
				}
				switchTo((CWizardHandler) tis[0].getData(), (EntryDescriptor) tis[0].getData(DESC));
				setPageComplete(validatePage());
				getWizard().getContainer().updateMessage();
			}
		});
		projectTypeTree.getAccessible().addAccessibleListener(new AccessibleAdapter() {
			@Override
			public void getName(AccessibleEvent e) {
				for (int i = 0; i < projectTypeTree.getItemCount(); i++) {
					if (projectTypeTree.getItem(i).getText().equals(e.result)) {
						return;
					}
				}
				e.result = Messages.SyncMainWizardPage_2;
			}
		});

		allToolChainsHiddenComposite = new Composite(compositeCDT.getParent(), SWT.NONE);
		allToolChainsHiddenComposite.setVisible(false);

		remoteToolChain = new Composite(compositeCDT, SWT.NONE);
		remoteToolChain.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		remoteToolChain.setLayout(new PageLayout());
		remoteToolChainTable = new Table(remoteToolChain, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		remoteToolChainTable.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateHiddenToolChainList();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				updateHiddenToolChainList();
			}
		});
		remoteToolChainTable.setVisible(true);

		projectLocalOptionsLabel = new Label(compositeCDT, SWT.NONE);
		projectLocalOptionsLabel.setFont(parent.getFont());
		projectLocalOptionsLabel.setLayoutData(new GridData(GridData.BEGINNING));
		projectLocalOptionsLabel.setText(Messages.SyncMainWizardPage_3);

		localToolChain = new Composite(compositeCDT, SWT.NONE);
		localToolChain.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		localToolChain.setLayout(new PageLayout());
		localToolChainTable = new Table(localToolChain, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
		localToolChainTable.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateHiddenToolChainList();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				updateHiddenToolChainList();
			}
		});
		localToolChainTable.setVisible(true);

		showSupportedOnlyButton = new Button(compositeCDT, SWT.CHECK);
		showSupportedOnlyButton.setText(Messages.SyncMainWizardPage_4);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		showSupportedOnlyButton.setLayoutData(gd);
		showSupportedOnlyButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				switchTo(updateData(projectTypeTree, allToolChainsHiddenComposite, false, SyncMainConversionWizardPage.this, getWizard()),
						getDescriptor(projectTypeTree));
			}
		});
		showSupportedOnlyButton.setSelection(false);
	}

	@Override
	public IWizardPage getNextPage() {
		return (h_selected == null) ? null : h_selected.getSpecificPage();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.conversion.ConvertProjectWizardPage#validatePage()
	 */
	@Override
	protected boolean validatePage() {
		message = null;
		messageType = IMessageProvider.NONE;
		errorMessage = null;
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
		if (!validateProjectName()) {
			return false;
		}
		if (!isCDTProject(project) && !validateProjectTypeSelection()) {
			return false;
		}
		if (!isSyncProject(project) && !fSyncWidget.isPageComplete()) {
			message = fSyncWidget.getMessage();
			messageType = fSyncWidget.getMessageType();
			errorMessage = fSyncWidget.getErrorMessage();
			return false;
		}
		return true;
	}

	protected boolean validateProjectName() {
		// Check if name is empty
		String projectFieldContents = getProjectName();
		if (projectFieldContents.equals("")) { //$NON-NLS-1$
			message = Messages.SyncMainWizardPage_5;
			messageType = IMessageProvider.NONE;
			return false;
		}

		// General name check
		IWorkspace workspace = IDEWorkbenchPlugin.getPluginWorkspace();
		IStatus nameStatus = workspace.validateName(projectFieldContents, IResource.PROJECT);
		if (!nameStatus.isOK()) {
			errorMessage = nameStatus.getMessage();
			return false;
		}

		// Do not allow # in the name
		if (getProjectName().indexOf('#') >= 0) {
			errorMessage = Messages.SyncMainWizardPage_6;
			return false;
		}
		return true;
	}

	protected boolean validateProjectTypeSelection() {
		if (projectTypeTree.getItemCount() == 0) {
			errorMessage = Messages.SyncMainWizardPage_10;
			return false;
		}

		if (h_selected == null) {
			message = Messages.SyncMainWizardPage_11;
			messageType = IMessageProvider.NONE;
			return false;
		}

		String s = h_selected.getErrorMessage();
		if (s != null) {
			errorMessage = s;
			return false;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getMessage()
	 */
	@Override
	public String getMessage() {
		setPageComplete(validatePage()); // Necessary to update message when participant changes
		return message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getMessageType()
	 */
	@Override
	public int getMessageType() {
		setPageComplete(validatePage()); // Necessary to update message when participant changes
		return messageType;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.dialogs.IDialogPage#getErrorMessage()
	 */
	@Override
	public String getErrorMessage() {
		setPageComplete(validatePage()); // Necessary to update message when participant changes
		return errorMessage;
	}

	/**
	 * 
	 * @param tree
	 * @param right
	 * @param show_sup
	 * @param ls
	 * @param wizard
	 * @return : selected Wizard Handler.
	 */
	public CWizardHandler updateData(Tree tree, Composite right, boolean show_sup, IWizardItemsListListener ls, IWizard wizard) {
		// remember selected item
		TreeItem[] selection = tree.getSelection();
		TreeItem selectedItem = selection.length > 0 ? selection[0] : null;
		String savedLabel = selectedItem != null ? selectedItem.getText() : null;
		String savedParentLabel = getParentText(selectedItem);

		tree.removeAll();
		IExtensionPoint extensionPoint = Platform.getExtensionRegistry().getExtensionPoint(EXTENSION_POINT_ID);
		if (extensionPoint == null) {
			return null;
		}
		IExtension[] extensions = extensionPoint.getExtensions();
		if (extensions == null) {
			return null;
		}

		List<EntryDescriptor> items = new ArrayList<EntryDescriptor>();
		for (IExtension extension : extensions) {
			IConfigurationElement[] elements = extension.getConfigurationElements();
			for (IConfigurationElement element : elements) {
				if (element.getName().equals(ELEMENT_NAME)) {
					CNewWizard w = null;
					try {
						w = (CNewWizard) element.createExecutableExtension(CLASS_NAME);
					} catch (CoreException e) {
						System.out.println(Messages.SyncMainWizardPage_12 + e.getLocalizedMessage());
						return null;
					}
					if (w == null) {
						return null;
					}
					w.setDependentControl(right, ls);
					for (EntryDescriptor ed : w.createItems(show_sup, wizard)) {
						items.add(ed);
					}
				}
			}
		}
		// If there is a EntryDescriptor which is default for category, make sure it
		// is in the front of the list.
		for (int i = 0; i < items.size(); ++i) {
			EntryDescriptor ed = items.get(i);
			if (ed.isDefaultForCategory()) {
				items.remove(i);
				items.add(0, ed);
				break;
			}
		}

		// bug # 211935 : allow items filtering.
		if (ls != null) {
			items = ls.filterItems(items);
		}
		addItemsToTree(tree, items);

		if (tree.getItemCount() > 0) {
			TreeItem target = null;
			// try to search item which was selected before
			if (savedLabel != null) {
				target = findItem(tree, savedLabel, savedParentLabel);
			}
			if (target == null) {
				target = tree.getItem(0);
				// We don't select the first element within the first group, because this causes a display bug with Autotools and
				// Windows
				// if (target.getItemCount() != 0) {
				// target = target.getItem(0);
				// }
			}
			tree.setSelection(target);
			return (CWizardHandler) target.getData();
		}
		return null;
	}

	private static String getParentText(TreeItem item) {
		if (item == null || item.getParentItem() == null) {
			return ""; //$NON-NLS-1$
		}
		return item.getParentItem().getText();
	}

	private static TreeItem findItem(Tree tree, String label, String parentLabel) {
		for (TreeItem item : tree.getItems()) {
			TreeItem foundItem = findTreeItem(item, label, parentLabel);
			if (foundItem != null) {
				return foundItem;
			}
		}
		return null;
	}

	private static TreeItem findTreeItem(TreeItem item, String label, String parentLabel) {
		if (item.getText().equals(label) && getParentText(item).equals(parentLabel)) {
			return item;
		}

		for (TreeItem child : item.getItems()) {
			TreeItem foundItem = findTreeItem(child, label, parentLabel);
			if (foundItem != null) {
				return foundItem;
			}
		}
		return null;
	}

	private static void addItemsToTree(Tree tree, List<EntryDescriptor> items) {
		// Sorting is disabled because of users requests
		// Collections.sort(items, CDTListComparator.getInstance());

		ArrayList<TreeItem> placedTreeItemsList = new ArrayList<TreeItem>(items.size());
		ArrayList<EntryDescriptor> placedEntryDescriptorsList = new ArrayList<EntryDescriptor>(items.size());
		for (EntryDescriptor wd : items) {
			if (wd.getParentId() == null) {
				wd.setPath(wd.getId());
				TreeItem ti = new TreeItem(tree, SWT.NONE);
				ti.setText(TextProcessor.process(wd.getName()));
				ti.setData(wd.getHandler());
				ti.setData(DESC, wd);
				ti.setImage(calcImage(wd));
				placedTreeItemsList.add(ti);
				placedEntryDescriptorsList.add(wd);
			}
		}
		while (true) {
			boolean found = false;
			Iterator<EntryDescriptor> it2 = items.iterator();
			while (it2.hasNext()) {
				EntryDescriptor wd1 = it2.next();
				if (wd1.getParentId() == null) {
					continue;
				}
				for (int i = 0; i < placedEntryDescriptorsList.size(); i++) {
					EntryDescriptor wd2 = placedEntryDescriptorsList.get(i);
					if (wd2.getId().equals(wd1.getParentId())) {
						found = true;
						wd1.setParentId(null);
						CWizardHandler h = wd2.getHandler();
						/*
						 * If neither wd1 itself, nor its parent (wd2) have a handler associated with them, and the item is not a
						 * category, then skip it. If it's category, then it's possible that children will have a handler associated
						 * with them.
						 */
						if (h == null && wd1.getHandler() == null && !wd1.isCategory()) {
							break;
						}

						wd1.setPath(wd2.getPath() + "/" + wd1.getId()); //$NON-NLS-1$
						wd1.setParent(wd2);
						if (h != null) {
							if (wd1.getHandler() == null && !wd1.isCategory()) {
								wd1.setHandler((CWizardHandler) h.clone());
							}
							if (!h.isApplicable(wd1)) {
								break;
							}
						}

						TreeItem p = placedTreeItemsList.get(i);
						TreeItem ti = new TreeItem(p, SWT.NONE);
						ti.setText(wd1.getName());
						ti.setData(wd1.getHandler());
						ti.setData(DESC, wd1);
						ti.setImage(calcImage(wd1));
						placedTreeItemsList.add(ti);
						placedEntryDescriptorsList.add(wd1);
						break;
					}
				}
			}
			// repeat iterations until all items are placed.
			if (!found) {
				break;
			}
		}
		// orphan elements (with not-existing parentId) are ignored
	}

	private static Image calcImage(EntryDescriptor ed) {
		if (ed.getImage() != null) {
			return ed.getImage();
		}
		if (ed.isCategory()) {
			return IMG_CATEGORY;
		}
		return IMG_ITEM;
	}

	private void switchTo(CWizardHandler h, EntryDescriptor ed) {
		if (h == null) {
			h = ed.getHandler();
		}
		if (ed.isCategory()) {
			h = null;
		}
		try {
			if (h != null) {
				h.initialize(ed);
			}
		} catch (CoreException e) {
			h = null;
		}
		if (h_selected != null) {
			h_selected.handleUnSelection();
		}
		h_selected = h;
		if (h == null) {
			if (ed.isCategory()) {
				if (categorySelectedForRemoteLabel == null) {
					categorySelectedForRemoteLabel = new Label(remoteToolChain, SWT.WRAP);
					categorySelectedForRemoteLabel.setText(Messages.SyncMainWizardPage_13);
					remoteToolChain.layout();
				}

				if (categorySelectedForLocalLabel == null) {
					categorySelectedForLocalLabel = new Label(localToolChain, SWT.WRAP);
					categorySelectedForLocalLabel.setText(Messages.SyncMainWizardPage_13);
					localToolChain.layout();
				}
			}
			return;
		}
		if (categorySelectedForRemoteLabel != null) {
			categorySelectedForRemoteLabel.setVisible(false);
		}
		if (categorySelectedForLocalLabel != null) {
			categorySelectedForLocalLabel.setVisible(false);
		}
		h_selected.handleSelection();
		h_selected.setSupportedOnly(false);

		// Create remote view
		remoteToolChainTable.removeAll();
		toolChainMap = ((MBSWizardHandler) h_selected).getToolChains();
		for (String name : toolChainMap.keySet()) {
			TableItem ti = new TableItem(remoteToolChainTable, SWT.NONE);
			ti.setText(name);
		}
		if (toolChainMap.keySet().size() > 0) {
			remoteToolChainTable.select(0);
		}

		// Create local view
		localToolChainTable.removeAll();
		toolChainMap = ((MBSWizardHandler) h_selected).getToolChains();
		boolean filterToolChains = showSupportedOnlyButton.getSelection();
		for (Map.Entry<String, IToolChain> entry : toolChainMap.entrySet()) {
			String name = entry.getKey();
			IToolChain tc = entry.getValue();

			if (filterToolChains) {
				if (tc != null && (!tc.isSupported() || !ManagedBuildManager.isPlatformOk(tc))) {
					continue;
				}
			}
			TableItem ti = new TableItem(localToolChainTable, SWT.NONE);
			ti.setText(name);
		}

		this.updateHiddenToolChainList();
	}

	public static EntryDescriptor getDescriptor(Tree _tree) {
		TreeItem[] sel = _tree.getSelection();
		if (sel == null || sel.length == 0) {
			return null;
		}
		return (EntryDescriptor) sel[0].getData(DESC);
	}

	@Override
	public void toolChainListChanged(int count) {
		setPageComplete(validatePage());
		getWizard().getContainer().updateButtons();
	}

	@Override
	public boolean isCurrent() {
		return isCurrentPage();
	}

	@Override
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public List filterItems(List items) {
		/*
		 * Remove RDT project types as these will not work with synchronized projects
		 */
		Iterator iterator = items.iterator();

		List<EntryDescriptor> filteredList = new LinkedList<EntryDescriptor>();

		while (iterator.hasNext()) {
			EntryDescriptor ed = (EntryDescriptor) iterator.next();
			if (!ed.getId().startsWith(RDT_PROJECT_TYPE)) {
				filteredList.add(ed);
			}
		}

		return filteredList;
	}

	/**
	 * Creates the project name specification controls.
	 * 
	 * @param parent
	 *            the parent composite
	 */
	private final void createProjectBasicInfoGroup(Composite parent) {
        Group localGroup = new Group(parent, SWT.SHADOW_ETCHED_IN);
        localGroup.setText(Messages.SyncMainConversionWizardPage_Project_to_convert_);
        localGroup.setLayout(new GridLayout(2, false));
        localGroup.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));

        Label label = new Label(localGroup, SWT.NONE);
        label.setText(Messages.SyncMainConversionWizardPage_Project_to_convert_label);
        label.setFont(parent.getFont());

        projectSelectionCombo = new Combo(localGroup, SWT.READ_ONLY);
        this.populateProjectCombo();
        projectSelectionCombo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        projectSelectionCombo.addSelectionListener(new SelectionListener() {
            @Override
            public void widgetDefaultSelected(SelectionEvent arg0) {
                // nothing to do
            }
            @Override
            public void widgetSelected(SelectionEvent arg0) {
            	handleProjectSelected(getProjectName());
                setPageComplete(validatePage());
                getWizard().getContainer().updateMessage();
            }
        });
	}

	/**
	 * Candidate projects are any open project that is not already both a synchronized project and a CDT project.
	 */
    private void populateProjectCombo() {
        IProject[] allProjects = ResourcesPlugin.getWorkspace().getRoot().getProjects();
        for (IProject p : allProjects) {
            if ((p.isOpen())) {
            	if (!(isSyncProject(p) && isCDTProject(p))) {
            		projectSelectionCombo.add(p.getName());
            	}
            }
        }
    }

	private final void createProjectRemoteInfoGroup(Composite parent) {
		fSyncWidget = SyncProjectWidget.convertProjectWidget(parent, SWT.NONE, getWizard().getContainer());
		fSyncWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false));
		fSyncWidget.addListener(SWT.Modify, new Listener() {
			@Override
			public void handleEvent(Event e) {
				setPageComplete(validatePage());
				getWizard().getContainer().updateMessage();
			}
		});
	}

	/**
	 * Returns the current project name as entered by the user, or its anticipated initial value.
	 * 
	 * @return the project name, its anticipated initial value, or <code>null</code> if no project name is known
	 */
	public String getProjectName() {
		return projectSelectionCombo.getText();
	}

	/*
	 * see @DialogPage.setVisible(boolean)
	 */
	@Override
	public void setVisible(boolean visible) {
		this.getControl().setVisible(visible);
		if (visible) {
			projectSelectionCombo.setFocus();
		}
	}

	/**
	 * Get the synchronize participant, which contains remote information
	 * 
	 * @return participant
	 */
	public ISynchronizeParticipant getSynchronizeParticipant() {
		return fSyncWidget.getSynchronizeParticipant();
	}

	/**
	 * Get the selected local tool chain
	 * 
	 * @return tool chain or null if either none selected or name does not map to a value (such as "Other Toolchain")
	 */
	public IToolChain getLocalToolChain() {
		TableItem[] selectedToolChains = localToolChainTable.getSelection();
		if (selectedToolChains.length < 1) {
			return null;
		} else {
			String toolChainName = selectedToolChains[0].getText();
			return toolChainMap.get(toolChainName);
		}
	}

	// Select toolchains in the hidden table that are selected in the visible remote and local tables
	private void updateHiddenToolChainList() {
		Set<String> currentSelections = new HashSet<String>();
		// Don't allow zero toolchains to be selected
		if (remoteToolChainTable.getSelectionCount() == 0 && remoteToolChainTable.getItemCount() > 0) {
			remoteToolChainTable.select(0);
		}
		for (TableItem ti : remoteToolChainTable.getSelection()) {
			currentSelections.add(ti.getText());
		}
		for (TableItem ti : localToolChainTable.getSelection()) {
			currentSelections.add(ti.getText());
		}

		Table hiddenTable = ((MBSWizardHandler) h_selected).getToolChainsTable();
		hiddenTable.deselectAll();
		for (int i = 0; i < hiddenTable.getItemCount(); i++) {
			String toolChainName = hiddenTable.getItem(i).getText();
			if (currentSelections.contains(toolChainName)) {
				hiddenTable.select(i);
			}
		}
	}

	/**
	 * Get the set of local configs
	 * @return configs
	 */
	public Set<String> getLocalToolChains() {
		if (localToolChainsSet == null) {
			this.gatherLocalToolChains();
		}
		return new HashSet<String>(localToolChainsSet);
	}

	/**
	 * Get the set of remote configs
	 * @return configs
	 */
	public Set<String> getRemoteToolChains() {
		if (remoteToolChainsSet == null) {
			this.gatherRemoteToolChains();
		}
		return new HashSet<String>(remoteToolChainsSet);
	}

	private void gatherLocalToolChains() {
		localToolChainsSet = new HashSet<String>();
		for (TableItem ti : localToolChainTable.getSelection()) {
			String name = ti.getText();
			if (name.equals("No ToolChain")) { //$NON-NLS-1$
				name = "-- Other Toolchain --"; //$NON-NLS-1$
			}
			localToolChainsSet.add(ti.getText());
		}
	}
	
	private void gatherRemoteToolChains() {
		remoteToolChainsSet = new HashSet<String>();
		for (TableItem ti : remoteToolChainTable.getSelection()) {
			String name = ti.getText();
			if (name.equals("No ToolChain")) { //$NON-NLS-1$
				name = "-- Other Toolchain --"; //$NON-NLS-1$
			}
			remoteToolChainsSet.add(ti.getText());
		}
	}

	/**
	 * Return the file filter created by the user or null if no changes were made.
	 * @return
	 */
	public SyncFileFilter getCustomFileFilter() {
		return fSyncWidget.getCustomFileFilter();
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.conversion.ConvertProjectWizardPage#getCheckedElements()
	 */
	@Override
	protected Object[] getCheckedElements() {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(getProjectName());
        return new Object[]{project};
	}
	/*
	 * (non-Javadoc)
	 * @see org.eclipse.cdt.ui.wizards.conversion.ConvertProjectWizardPage#getWzDescriptionResource()
	 */
    @Override
    protected String getWzDescriptionResource() {
        return "Converts a project to a synchronized C/C++ or Fortran project"; //$NON-NLS-1$
    }

    /*
     * (non-Javadoc)
     * @see org.eclipse.cdt.ui.wizards.conversion.ConvertProjectWizardPage#getWzTitleResource()
     */
    @Override
    protected String getWzTitleResource() {
        return "Convert to a synchronized project"; //$NON-NLS-1$
    }

    private void handleProjectSelected(String projectName) {
    	IProject project;
    	boolean isSync = true;
    	boolean isCDT = true;
    	// Disable all for null project
    	if (projectName == null) {
    		project = null;
    	} else {
    		project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
        	isSync = this.isSyncProject(project);
        	isCDT = this.isCDTProject(project);
    	}
    	// Assert that we are never passed projects that are both Sync and CDT projects.
    	assert(project == null || isSync == false || isCDT == false);

        if (fSyncWidget != null) {
            fSyncWidget.setProjectName(projectName);
            if (isSync) {
            	fSyncWidget.setEnabled(false);
            } else {
            	fSyncWidget.setEnabled(true);
            }
        }
        
        if (compositeCDT != null) {
        	if (isCDT) {
        		compositeCDT.setEnabled(false);
        	} else {
        		compositeCDT.setEnabled(true);
        	}
        }
    }
   
    // This function must be defined but is not used for this wizard (part of the original UI). Instead we use the other methods
    // below to filter projects.
	@Override
	public boolean isCandidate(IProject project) {
		return false;
	}

	/**
	 * Test if given project is a CDT project.
	 * @param project
	 * @return whether a CDT project 
	 */
	private boolean isCDTProject(IProject project) {
		try {
			return (project.hasNature(CProjectNature.C_NATURE_ID) || project.hasNature(CCProjectNature.CC_NATURE_ID));
		} catch (CoreException e) {
			Activator.log(e);
			return false;
		}
	}

	/**
	 * Test if given project is a synchronized project
	 * @param project
	 * @return whether a synchronized project
	 */
	private boolean isSyncProject(IProject project) {
		return RemoteSyncNature.hasNature(project);
	}

	/*
	 * It is necessary to disable/enable a control's widgets recursively to get visual feedback ("grayed out" look)
	 * SWT controls may not have a "getChildren" method, so doing this in full generality requires reflection.
	 * TODO: Fix and test. Currently we still only disable the main composite or widget.
	 * http://stackoverflow.com/questions/2957657/disable-and-grey-out-an-eclipse-widget
	 */
	private void recursiveSetEnabled(Control ctrl, boolean enabled) {
		ctrl.setEnabled(enabled);
		Method method;
		try {
			method = ctrl.getClass().getMethod("getChildren"); //$NON-NLS-1$
		} catch (NoSuchMethodException e) {
			return;
		}
		if (method.getReturnType() == Control.class.getClass() && method.getReturnType().isArray()) {
			try {
				for (Control c : (Control []) method.invoke(ctrl))
					recursiveSetEnabled(c, enabled);
			} catch (IllegalArgumentException e) {
				Activator.log(e);
			} catch (IllegalAccessException e) {
				Activator.log(e);
			} catch (InvocationTargetException e) {
				Activator.log(e);
			}
		}
	}
}