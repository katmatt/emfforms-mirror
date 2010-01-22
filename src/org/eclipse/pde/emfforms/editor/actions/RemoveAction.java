/**
 * Copyright (c) 2010 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Jacques Lescot, Sierra Wireless - initial API and implementation (bug 300462)
 *
 * $Id: RemoveAction.java,v 1.1 2010/01/22 16:42:08 bcabe Exp $
 */
package org.eclipse.pde.emfforms.editor.actions;

import org.eclipse.emf.edit.ui.action.EditingDomainActionBarContributor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.pde.emfforms.editor.EmfActionBarContributor;
import org.eclipse.pde.emfforms.editor.EmfMasterDetailBlock;
import org.eclipse.pde.emfforms.internal.Activator;
import org.eclipse.pde.emfforms.internal.editor.IEmfFormsImages;

public class RemoveAction extends Action {

	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		getDeleteAction().addPropertyChangeListener(listener);
	}

	public int getAccelerator() {
		return getDeleteAction().getAccelerator();
	}

	public String getActionDefinitionId() {
		return getDeleteAction().getActionDefinitionId();
	}

	public String getDescription() {
		return getDeleteAction().getDescription();
	}

	public String getToolTipText() {
		return getDeleteAction().getToolTipText();
	}

	@Override
	public void setEnabled(boolean enabled) {
		getDeleteAction().setEnabled(enabled);
	}

	public boolean isEnabled() {
		return getDeleteAction().isEnabled();
	}

	@Override
	public String getText() {
		return getDeleteAction().getText();
	}

	@Override
	public void run() {
		TreeViewer treeViewer = masterDetail.getTreeViewer();
		int selIndex = treeViewer.getTree().indexOf(treeViewer.getTree().getSelection()[0]);

		getDeleteAction().run();

		treeViewer.refresh();
		if (treeViewer.getTree().getItemCount() > 0) {
			// if we delete the last line, select the new last line
			if (selIndex >= treeViewer.getTree().getItemCount()) {
				selIndex = selIndex - 1;
			}
			// treeViewer.getTree().indexOf(TreeItem) method is returning -1 when the provided item is not a root element ...
			// In that case we select the first root element.
			if (selIndex > -1)
				treeViewer.getTree().setSelection(treeViewer.getTree().getItem(selIndex));
			else
				treeViewer.getTree().setSelection(treeViewer.getTree().getItem(0));
			treeViewer.getTree().setFocus();
		}
	}

	private EmfMasterDetailBlock masterDetail;

	public RemoveAction(EmfMasterDetailBlock masterDetail) {
		super();
		this.masterDetail = masterDetail;
		getDeleteAction();

	}

	private IAction getDeleteAction() {
		EditingDomainActionBarContributor actionBarContributor = masterDetail.getEditor().getActionBarContributor();
		if (actionBarContributor != null && actionBarContributor instanceof EmfActionBarContributor)
			return ((EmfActionBarContributor) actionBarContributor).getDeleteAction();
		return null;
	}

	@Override
	public ImageDescriptor getImageDescriptor() {
		return ImageDescriptor.createFromURL(Activator.getDefault().getBundle().getResource(IEmfFormsImages.REMOVE_TOOLBAR_BUTTON));
	}
}
