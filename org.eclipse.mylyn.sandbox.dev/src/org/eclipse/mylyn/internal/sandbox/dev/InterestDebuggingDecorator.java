/*******************************************************************************
 * Copyright (c) 2004, 2007 Mylyn project committers and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.eclipse.mylyn.internal.sandbox.dev;

import java.util.ConcurrentModificationException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.IDecoration;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.jface.viewers.ILightweightLabelDecorator;
import org.eclipse.mylyn.commons.core.StatusHandler;
import org.eclipse.mylyn.context.core.AbstractContextStructureBridge;
import org.eclipse.mylyn.context.core.ContextCore;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.internal.context.core.ContextCorePlugin;
import org.eclipse.mylyn.internal.context.core.InteractionContextRelation;
import org.eclipse.mylyn.internal.context.ui.ColorMap;

/**
 * @author Mik Kersten
 */
public class InterestDebuggingDecorator implements ILightweightLabelDecorator {

	public InterestDebuggingDecorator() {
		super();
	}

	public void decorate(Object element, IDecoration decoration) {
		AbstractContextStructureBridge bridge = null;
		try {
			if (ContextCorePlugin.getDefault() == null) {
				return;
			}
			bridge = ContextCore.getStructureBridge(element);
		} catch (ConcurrentModificationException cme) {
			// ignored, because we can add structure bridges during decoration
		}
		try {
			IInteractionElement node = null;
			if (element instanceof InteractionContextRelation) {
				decoration.setForegroundColor(ColorMap.RELATIONSHIP);
			} else if (element instanceof IInteractionElement) {
				node = (IInteractionElement) element;
			} else {
				if (bridge != null && bridge.getContentType() != null) {
					node = ContextCore.getContextManager().getElement(bridge.getHandleIdentifier(element));
				}
			}
			if (node != null) {
				decoration.addSuffix(" {" + node.getInterest().getValue() + " [" + node.getInterest().getEncodedValue() + "] " + "}");
			}
		} catch (Exception e) {
			StatusHandler.log(new Status(IStatus.WARNING, MylynDevPlugin.ID_PLUGIN, "Decoration failed", e));
		}
	}

	public void addListener(ILabelProviderListener listener) {
		// don't care about listeners
	}

	public void dispose() {
		// don't care when we are disposed
	}

	public boolean isLabelProperty(Object element, String property) {
		return false;
	}

	public void removeListener(ILabelProviderListener listener) {
		// don't care about listeners
	}

}
