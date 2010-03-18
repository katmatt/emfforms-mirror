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
 * $Id: IncrementalModelBuilder.java,v 1.2 2010/03/18 11:58:45 bcabe Exp $
 */
package org.eclipse.pde.emfforms.builder;

import java.util.Hashtable;
import java.util.Map;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.common.util.BasicDiagnostic;
import org.eclipse.emf.common.util.Diagnostic;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.Diagnostician;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.IItemLabelProvider;
import org.eclipse.emf.transaction.RunnableWithResult;
import org.eclipse.emf.transaction.TransactionalEditingDomain;
import org.eclipse.pde.emfforms.internal.Activator;

/**
 * An abstract class to subclass to launch background jobs on the modified model
 */
public abstract class IncrementalModelBuilder extends IncrementalProjectBuilder {
	private String _contentType;

	/**
	 * @see org.eclipse.core.resources.IncrementalProjectBuilder#build(int,
	 *      java.util.Map, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IProject[] build(int kind, Map args, IProgressMonitor monitor) throws CoreException {
		switch (kind) {
			case FULL_BUILD :
				fullBuild(monitor);
				break;
			case CLEAN_BUILD :
				clean(monitor);
				break;
			default :
				IResourceDelta delta = getDelta(getProject());
				if (delta == null) {
					fullBuild(monitor);
				} else {
					incrementalBuild(delta, monitor);
				}

				break;
		}
		return null;
	}

	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		// Subclasses must override this method
	}

	protected void incrementalBuild(IResourceDelta delta, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		// the visitor does the work.
		ModelFileDeltaVisitor visitor = new ModelFileDeltaVisitor(getContentType());
		delta.accept(visitor);

		Map<Resource, IResource> modifiedResources = visitor.getModifiedResources();

		monitor.beginTask("Incremental Build", modifiedResources.size()); //$NON-NLS-1$

		for (Resource modelResource : modifiedResources.keySet()) {
			build(modelResource.getContents().get(0), modifiedResources.get(modelResource), new SubProgressMonitor(monitor, 1));
		}

		monitor.done();

	}

	/**
	 * If the model files to build have a content-type, this method must return
	 * it, in order to optimize the build process (fail-fast mode)
	 * 
	 * @return the content-type of model files, or null if no content-type
	 *         exists
	 */
	protected String getContentType() {
		return _contentType;
	}

	protected abstract void fullBuild(IProgressMonitor monitor) throws CoreException;

	protected void build(EObject modelObject, IResource resource, IProgressMonitor monitor) throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		monitor.beginTask("Model Validation", 4); //$NON-NLS-1$

		monitor.subTask("Validation"); //$NON-NLS-1$

		Diagnostic diagnostic = validate(modelObject);

		monitor.worked(3);

		if (resource instanceof IContainer) {
			MarkerHelper.cleanMarkers((IContainer) resource);
		} else if (resource instanceof IFile) {
			MarkerHelper.cleanMarkers((IFile) resource);
		}

		MarkerHelper.createMarkers(diagnostic, new SubProgressMonitor(monitor, 1));

		monitor.done();
	}

	protected Diagnostic validate(final EObject modelObject) {
		TransactionalEditingDomain domain = TransactionalEditingDomain.Factory.INSTANCE.createEditingDomain();
		final AdapterFactory adapterFactory = domain instanceof AdapterFactoryEditingDomain ? ((AdapterFactoryEditingDomain) domain).getAdapterFactory() : null;

		try {
			return (Diagnostic) domain.runExclusive(new RunnableWithResult.Impl<Diagnostic>() {
				public void run() {

					Diagnostic diagnostic = new Diagnostician() {

						@Override
						public String getObjectLabel(EObject eObject) {
							if (adapterFactory != null && !eObject.eIsProxy()) {
								IItemLabelProvider itemLabelProvider = (IItemLabelProvider) adapterFactory.adapt(eObject, IItemLabelProvider.class);
								if (itemLabelProvider != null) {
									return itemLabelProvider.getText(eObject);
								}
							}

							return super.getObjectLabel(eObject);
						}
					}.validate(modelObject);
					setResult(diagnostic);
				}
			});
		} catch (InterruptedException ie) {
			// Log and return the exception in a diagnostic
			Activator.log(ie);
			return new BasicDiagnostic(Diagnostic.ERROR, "ModelChecker", 0, ie.getMessage(), new Object[] {modelObject}); //$NON-NLS-1$
		}
	}

	/**
	 * The content-type this builder is bound to is defined in the extension 
	 */
	@Override
	@SuppressWarnings("unchecked")
	public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException {
		if (data != null && data instanceof Hashtable<?, ?>) {
			Hashtable<String, String> map = (Hashtable<String, String>) data;
			_contentType = map.get("content-type"); //$NON-NLS-1$
		}

	}

}
