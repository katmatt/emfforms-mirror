/**
 * Copyright (c) 2009 Anyware Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Anyware Technologies - initial API and implementation
 *
 * $Id: AbstractEmfFormPage.java,v 1.9 2009/12/02 11:28:22 bcabe Exp $
 */
package org.eclipse.pde.emfforms.editor;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.viewers.*;
import org.eclipse.pde.emfforms.internal.validation.RichTooltipHyperlinkAdapter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IEditorActionBarContributor;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.internal.forms.widgets.FormHeading;

/**
 * Generic page for {@link EObject} edition
 * 
 */
public abstract class AbstractEmfFormPage extends FormPage implements IEmfFormPage {

	private int numColumns = 1;

	/** @deprecated Use getMainMasterDetailBlock() method instead */
	private boolean isMasterDetail = false;

	/**
	 * Constructor that creates the page and initializes it with the editor.
	 * By default the page contains only one column.
	 * 
	 * @param editor
	 *            the parent editor
	 */
	public AbstractEmfFormPage(EmfFormEditor<?> editor) {
		super(editor, "", ""); //$NON-NLS-1$ //$NON-NLS-2$
		this.setPartName(getPartName());
	}

	/**
	 * Constructor that creates the page and initializes it with the editor.
	 * Allows to customize the number of columns 
	 * 
	 * @param editor
	 * @param numColumns
	 */
	public AbstractEmfFormPage(EmfFormEditor<?> editor, int numColumns) {
		this(editor, numColumns, ""); //$NON-NLS-1$
		this.setPartName(getPartName());
	}

	/**
	 * Constructor that creates the page and initializes it with the editor.
	 * Allows to customize the number of columns and set explicitly the page name.
	 * 
	 * @param editor
	 * @param numColumns
	 * @param pageName
	 */
	public AbstractEmfFormPage(EmfFormEditor<?> editor, int numColumns, String pageName) {
		this(editor);
		this.numColumns = numColumns;
		setPartName(pageName);
	}

	/**
	 * Constructor that creates the page and initializes it with the editor.
	 * Allows to customize the number of columns, and to indicate that the content will consist in a MasterDetailsBlock 
	 * 
	 * @param editor
	 * @param numColumns
	 * @param isMasterDetail
	 * @deprecated Use the constructor without the isMasterDetail parameter. Instead, override the getMainMasterDetailBlock() method
	 */
	public AbstractEmfFormPage(EmfFormEditor<?> editor, int numColumns, boolean isMasterDetail) {
		this(editor, numColumns, isMasterDetail, "");
		this.setPartName(getPartName());
	}

	/** @deprecated Use the constructor without the isMasterDetail parameter. Instead, override the getMainMasterDetailBlock() method */
	public AbstractEmfFormPage(EmfFormEditor<?> editor, int numColumns, boolean isMasterDetail, String pageName) {
		this(editor);
		this.numColumns = numColumns;
		this.isMasterDetail = isMasterDetail;
		setPartName(pageName);
	}

	@Override
	protected void createFormContent(IManagedForm managedForm) {
		super.createFormContent(managedForm);
		Composite body = managedForm.getForm().getBody();

		GridLayout gl = new GridLayout(getNumColumns(), true);
		gl.verticalSpacing = 20;

		createHeader();

		Composite actualContent = null;

		if (!isMasterDetail()) {
			actualContent = this.getEditor().getToolkit().createComposite(body, SWT.NONE);
			GridDataFactory.swtDefaults().span(getNumColumns(), 1).align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(actualContent);
			actualContent.setLayout(gl);
		}

		createContents(actualContent);

		body.setLayout(gl);

		getFormToolkit().adapt(actualContent);

		DataBindingContext bindingContext = ((EmfFormEditor<?>) getEditor()).getDataBindingContext();
		bind(bindingContext);
		getEditor().validate();

		//select first element if there is one
		if (getViewer() != null && getViewer() instanceof TreeViewer) {
			TreeViewer treeViewer = (TreeViewer) getViewer();
			if (treeViewer.getTree().getItemCount() > 0) {
				treeViewer.getTree().setSelection(treeViewer.getTree().getItem(0));
				treeViewer.getTree().setFocus();
			}
		}

		if (getEditor().getEditorConfig().isUseRichFormsTooltips()) {
			managedForm.getForm().getForm().addMessageHyperlinkListener(new RichTooltipHyperlinkAdapter(managedForm));
			((FormHeading) managedForm.getForm().getForm().getHead()).setMessageToolTipManager(null);
		}
	}

	/**
	 * Subclasses should override this method when an EmfMasterDetailBlock is used as the contents of the page. By default, <code>null<code> is returned.
	 * 
	 * @return EmfMasterDetailBlock
	 */
	protected EmfMasterDetailBlock getMainMasterDetailBlock() {
		return null;
	}

	private void createHeader() {
		Form f = this.getManagedForm().getForm().getForm();
		f.setText(this.getPartName());
		this.getEditor().getToolkit().decorateFormHeading(f);
	}

	/**
	 * @return the editor form toolkit
	 */
	protected PDEFormToolkit getFormToolkit() {
		return (PDEFormToolkit) getEditor().getToolkit();
	}

	public EmfFormEditor<?> getEditor() {
		return (EmfFormEditor<?>) super.getEditor();
	}

	@Override
	public abstract String getId();

	/**
	 * {@inheritDoc}
	 */
	public int getNumColumns() {
		return numColumns;
	}

	public boolean isMasterDetail() {
		return isMasterDetail || getMainMasterDetailBlock() != null;
	}

	/**
	 * {@inheritDoc} <br><br>
	 * The default implementation of this method just returns <code>null</code>.
	 * It must be overriden if the actual page contains an interesting viewer.
	 */
	public Viewer getViewer() {
		if (getMainMasterDetailBlock() != null) {
			return getMainMasterDetailBlock().getTreeViewer();
		}
		return null;
	}

	@Override
	public void setActive(boolean active) {
		if (active) {
			getEditor().validate();

			if (getViewer() != null) {

				// Update the createChild and createSibling filters
				IEditorActionBarContributor actionBarContributor = getEditor().getEditorSite().getActionBarContributor();
				if (actionBarContributor != null && actionBarContributor instanceof EmfActionBarContributor && getMainMasterDetailBlock() != null) {
					((EmfActionBarContributor) actionBarContributor).setCreateChildMenuFilter(getMainMasterDetailBlock().getCreateChildContextMenuFilter());
					((EmfActionBarContributor) actionBarContributor).setCreateSiblingMenuFilter(getMainMasterDetailBlock().getCreateSiblingContextMenuFilter());
				}

				// force the selection on the viewer if there is any, to avoid a bug on the ContextMenu (on tab changed, display menu was inconsistent)
				IStructuredSelection selection = (IStructuredSelection) getViewer().getSelection();
				getViewer().setSelection(selection);
				getViewer().refresh();
			}
		}
	}

	public abstract void bind(DataBindingContext bindingContext);

	public abstract void createContents(Composite parent);
}
