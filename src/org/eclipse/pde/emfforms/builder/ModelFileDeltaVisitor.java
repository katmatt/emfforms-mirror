/**
 * Copyright (c) 2010 Sierra Wireless and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Sierra Wireless - initial API and implementation
 *
 * $Id: ModelFileDeltaVisitor.java,v 1.1 2010/03/18 11:58:46 bcabe Exp $
 */
package org.eclipse.pde.emfforms.builder;

import java.util.HashMap;
import java.util.Map;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.emf.common.command.BasicCommandStack;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.emf.edit.provider.ComposedAdapterFactory;

public class ModelFileDeltaVisitor implements IResourceDeltaVisitor {
	private Map<Resource, IResource> modifiedResources = new HashMap<Resource, IResource>();
	private String _contentType;

	public ModelFileDeltaVisitor(String contentType) {
		_contentType = contentType;
	}

	public Map<Resource, IResource> getModifiedResources() {
		return modifiedResources;
	}

	/**
	 * @see org.eclipse.core.resources.IResourceDeltaVisitor#visit(org.eclipse
	 *      .core.resources.IResourceDelta)
	 */
	public boolean visit(IResourceDelta delta) throws CoreException {
		IResource resource = delta.getResource();
		switch (delta.getKind()) {
			case IResourceDelta.ADDED :
			case IResourceDelta.REMOVED :
				// Do nothing
				break;
			case IResourceDelta.CHANGED :
				if (resource instanceof IContainer)
					return true;
				if (_contentType != null && !_contentType.equals(((IFile) resource).getContentDescription().getContentType().getId()))
					return false;
				// handle changed resource
				URI resourceURI = URI.createPlatformResourceURI(resource.getFullPath().toString(), true);
				Resource modelResource = new AdapterFactoryEditingDomain(new ComposedAdapterFactory(ComposedAdapterFactory.Descriptor.Registry.INSTANCE), new BasicCommandStack()).getResourceSet().getResource(resourceURI, true);
				if (modelResource != null && modelResource.isLoaded() && !modifiedResources.containsKey(modelResource)) {
					modifiedResources.put(modelResource, resource);
				}
				break;
		}
		// return true to continue visiting children.
		return true;
	}
}