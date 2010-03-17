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
 * $Id: SharedClipboardAdapterFactoryEditingDomain.java,v 1.1 2010/03/17 10:44:45 bcabe Exp $
 */
package org.eclipse.pde.emfforms.internal.editor;

import java.util.Collection;
import java.util.Map;
import org.eclipse.emf.common.command.CommandStack;
import org.eclipse.emf.common.notify.AdapterFactory;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;

/**
 * A custom {@link AdapterFactoryEditingDomain} with a static clipboard (to deal with cross-editor copy/paste) 
 * @author Benjamin Cabé
 */
public class SharedClipboardAdapterFactoryEditingDomain extends AdapterFactoryEditingDomain {
	private static Collection<Object> clipboard;

	/**
	 * @param adapterFactory
	 * @param commandStack
	 * @param resourceToReadOnlyMap
	 */
	public SharedClipboardAdapterFactoryEditingDomain(AdapterFactory adapterFactory, CommandStack commandStack, Map<Resource, Boolean> resourceToReadOnlyMap) {
		super(adapterFactory, commandStack, resourceToReadOnlyMap);

	}

	@Override
	public Collection<Object> getClipboard() {
		return SharedClipboardAdapterFactoryEditingDomain.clipboard;
	}

	@Override
	public void setClipboard(Collection<Object> clipboard) {
		SharedClipboardAdapterFactoryEditingDomain.clipboard = clipboard;
	}
}