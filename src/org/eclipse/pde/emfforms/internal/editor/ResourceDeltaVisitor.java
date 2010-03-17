package org.eclipse.pde.emfforms.internal.editor;

import java.io.IOException;
import java.util.Collections;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.databinding.swt.SWTObservables;
import org.eclipse.pde.emfforms.editor.EmfFormEditor;
import org.eclipse.pde.emfforms.internal.Activator;
import org.eclipse.swt.widgets.Display;

public class ResourceDeltaVisitor<O extends EObject> implements IResourceDeltaVisitor {
	/**
	 * 
	 */
	private final EmfFormEditor<O> _emfFormEditor;

	/**
	 * @param emfFormEditor
	 */
	public ResourceDeltaVisitor(EmfFormEditor<O> emfFormEditor) {
		_emfFormEditor = emfFormEditor;
	}

	public boolean visit(IResourceDelta delta) throws CoreException {

		if (delta.getResource().getType() == IResource.FILE) {
			if (delta.getKind() == IResourceDelta.REMOVED) {
				String fullPath = delta.getFullPath().toString();
				final URI changedURI = URI.createPlatformResourceURI(fullPath, false);

				Resource currentResource = _emfFormEditor.getCurrentEObject().eResource();
				if (currentResource.getURI().equals(changedURI)) {
					Display.getDefault().asyncExec(new Runnable() {
						public void run() {
							ResourceDeltaVisitor.this._emfFormEditor.getSite().getPage().closeEditor(_emfFormEditor, false);
						}
					});
				}
			} else if (delta.getKind() == IResourceDelta.CHANGED) {
				// filter events related to changes on markers
				if ((delta.getFlags() & IResourceDelta.MARKERS) == IResourceDelta.MARKERS) {
					return false;
				}
				String fullPath = delta.getFullPath().toString();
				final URI changedURI = URI.createPlatformResourceURI(fullPath, false);

				SWTObservables.getRealm(Display.getDefault()).asyncExec(new Runnable() {

					public void run() {
						Resource currentResource = _emfFormEditor.getCurrentEObject().eResource();
						boolean isMainResource = currentResource.getURI().equals(changedURI);
						Resource changedResource = currentResource.getResourceSet().getResource(changedURI, false);

						// The changed resource is contained in the resourceset, it must be reloaded
						if (changedResource != null && changedResource.isLoaded() && !_emfFormEditor.isSaving()) {

							// The editor has pending changes, we must inform the user, the content is going to be reloaded
							if (isMainResource && _emfFormEditor.isDirty()) {
								_emfFormEditor.getEditingDomain().getCommandStack().flush();
							}

							try {
								changedResource.unload();
								changedResource.load(Collections.EMPTY_MAP);

								// If the modified resource is the main resource, we update the current object
								if (isMainResource) {
									_emfFormEditor.setMainResource(changedResource);
								}
							} catch (IOException ioe) {
								Activator.log(ioe);
							}
						}
					}
				});
			}

		}

		return true;
	}
}