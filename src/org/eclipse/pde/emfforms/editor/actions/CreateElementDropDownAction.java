/**
 * Copyright (c) 2009 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *
 * $Id: CreateElementDropDownAction.java,v 1.2 2009/12/09 09:15:38 bcabe Exp $
 */
package org.eclipse.pde.emfforms.editor.actions;

import java.util.ArrayList;
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
import org.eclipse.swt.graphics.Point;
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
		IAction titleCreateChildAction = new Action(createChildActions.isEmpty() ? "(no new child)" : "-- New Child --") {};
		titleCreateChildAction.setEnabled(false);
		new ActionContributionItem(titleCreateChildAction).fill(fMenu, -1);

		for (IAction createChildAction : createChildActions) {
			addActionToMenu(fMenu, createChildAction);
		}

		// Add a separator
		new MenuItem(fMenu, SWT.SEPARATOR);

		// Add all CreateSibling actions for the current selection
		IAction titleCreateSiblingAction = new Action(createSiblingActions.isEmpty() ? "(no new sibling)" : "-- New Sibling --") {};
		titleCreateSiblingAction.setEnabled(false);
		new ActionContributionItem(titleCreateSiblingAction).fill(fMenu, -1);
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
				selection = getRootElement();
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

	protected IStructuredSelection getRootElement() {
		return new StructuredSelection(masterDetailBlock.getEditor().getCurrentEObject());
	}

	@Override
	public void runWithEvent(Event event) {
		ToolItem toolItem = (ToolItem) event.widget;

		// Update the menu
		getMenu(toolItem.getParent());

		Collection<IAction> allEnabledActions = getAllEnabledActions();
		if (allEnabledActions.isEmpty()) {
			// 1. None enabled action to execute - Just exit
			return;
		} else if (allEnabledActions.size() == 1) {
			// 2. A single action to execute - Run it and exit
			allEnabledActions.iterator().next().run();
			return;
		} else {
			// 3. Several actions are enabled - Show the menu
			Point point = toolItem.getParent().toDisplay(new Point(event.x, event.y));
			// Position the menu below the drop down item. Need to translate the menu 20 pixels to the bottom for the same rendering as it was created from the DOWN_ARROW area
			fMenu.setLocation(point.x, point.y + 20);
			fMenu.setVisible(true);
			return; // we don't fire the action
		}
	}

	private Collection<IAction> getAllEnabledActions() {
		Collection<IAction> allEnabledActions = new ArrayList<IAction>();

		for (IAction createChildAction : createChildActions) {
			if (createChildAction.isEnabled()) {
				allEnabledActions.add(createChildAction);
			}
		}
		for (IAction createSiblingAction : createSiblingActions) {
			if (createSiblingAction.isEnabled()) {
				allEnabledActions.add(createSiblingAction);
			}
		}
		return allEnabledActions;
	}
}
