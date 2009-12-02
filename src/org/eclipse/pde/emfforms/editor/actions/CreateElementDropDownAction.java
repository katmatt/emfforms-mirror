/**
 * Copyright (c) 2009 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *
 * $Id: CreateElementDropDownAction.java,v 1.1 2009/12/02 13:17:55 bcabe Exp $
 */
package org.eclipse.pde.emfforms.editor.actions;

import java.util.Collection;
import org.eclipse.emf.edit.domain.EditingDomain;
import org.eclipse.jface.action.*;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.*;
import org.eclipse.pde.emfforms.editor.EmfActionBarContributor;
import org.eclipse.pde.emfforms.editor.EmfMasterDetailBlock;
import org.eclipse.pde.emfforms.internal.Activator;
import org.eclipse.pde.emfforms.internal.editor.IEmfFormsImages;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.*;

public class CreateElementDropDownAction extends Action implements IMenuCreator {

	private EmfMasterDetailBlock masterDetailBlock;

	private Menu fMenu;

	/** The create child actions. */
	private Collection<IAction> createChildActions;

	/** The create sibling actions */
	protected Collection<IAction> createSiblingActions;

	public CreateElementDropDownAction(EmfMasterDetailBlock masterDetailBlck) {
		masterDetailBlock = masterDetailBlck;
		fMenu = null;
		setToolTipText("Create a new child/sibling element");
		setImageDescriptor(ImageDescriptor.createFromURL(Activator.getDefault().getBundle().getResource(IEmfFormsImages.ADD_TOOLBAR_BUTTON)));
		setMenuCreator(this);
	}

	public void dispose() {
		// action is reused, can be called several times.
		if (fMenu != null) {
			fMenu.dispose();
			fMenu = null;
		}
	}

	public Menu getMenu(Menu parent) {
		return null;
	}

	public Menu getMenu(Control parent) {
		if (fMenu != null) {
			fMenu.dispose();
		}
		fMenu = new Menu(parent);

		updateActions();

		// Add all CreateChild actions for the current selection
		for (IAction createChildAction : createChildActions) {
			addActionToMenu(fMenu, createChildAction);
		}
		// Add a separator if needed
		if (!createChildActions.isEmpty() && !createSiblingActions.isEmpty()) {
			new MenuItem(fMenu, SWT.SEPARATOR);
		}
		// Add all CreateSibling actions for the current selection
		for (IAction createSiblingAction : createSiblingActions) {
			addActionToMenu(fMenu, createSiblingAction);
		}

		return fMenu;
	}

	private void addActionToMenu(Menu parent, IAction action) {
		ActionContributionItem item = new ActionContributionItem(action);
		item.fill(parent, -1);
	}

	private void updateActions() {
		// Query the new selection for appropriate new child/sibling descriptors
		Collection<?> newChildDescriptors = null;
		Collection<?> newSiblingDescriptors = null;

		ISelection selection = masterDetailBlock.getTreeViewer().getSelection();
		if (selection.isEmpty()) {
			if (masterDetailBlock.getTreeViewer().getInput() != null) {
				selection = new StructuredSelection(masterDetailBlock.getTreeViewer().getInput());
			} else {
				selection = new StructuredSelection(masterDetailBlock.getEditor().getCurrentEObject());
			}
		}
		if (selection instanceof IStructuredSelection && ((IStructuredSelection) selection).size() == 1) {
			Object object = ((IStructuredSelection) selection).getFirstElement();

			EditingDomain domain = masterDetailBlock.getEditor().getEditingDomain();
			newChildDescriptors = domain.getNewChildDescriptors(object, null);
			newSiblingDescriptors = domain.getNewChildDescriptors(null, object);

			// Generate actions for selection
			createChildActions = ((EmfActionBarContributor) masterDetailBlock.getEditor().getActionBarContributor()).generateCreateChildActions(newChildDescriptors, selection);
			createSiblingActions = ((EmfActionBarContributor) masterDetailBlock.getEditor().getActionBarContributor()).generateCreateSiblingActions(newSiblingDescriptors, selection);
		}
	}

	@Override
	public void run() {
		updateActions();

		// Find the first action that can be executed. Priority is for createChild actions
		for (IAction createChildAction : createChildActions) {
			if (createChildAction.isEnabled()) {
				createChildAction.run();
				return;
			}
		}
		for (IAction createSiblingAction : createSiblingActions) {
			if (createSiblingAction.isEnabled()) {
				createSiblingAction.run();
				return;
			}
		}
	}
}
