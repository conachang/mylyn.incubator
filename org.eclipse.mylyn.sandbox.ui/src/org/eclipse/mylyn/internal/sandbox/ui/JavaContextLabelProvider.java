/*******************************************************************************
 * Copyright (c) 2004, 2008 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.internal.sandbox.ui;

import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.internal.ui.viewsupport.AppearanceAwareLabelProvider;
import org.eclipse.jdt.internal.ui.viewsupport.JavaElementImageProvider;
import org.eclipse.jdt.internal.ui.viewsupport.TreeHierarchyLayoutProblemsDecorator;
import org.eclipse.jdt.ui.JavaElementLabels;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.mylyn.commons.ui.CommonImages;
import org.eclipse.mylyn.context.core.IInteractionElement;
import org.eclipse.mylyn.context.core.IInteractionRelation;
import org.eclipse.mylyn.internal.context.core.InteractionContextManager;
import org.eclipse.mylyn.internal.java.ui.JavaStructureBridge;
import org.eclipse.mylyn.internal.java.ui.JavaUiBridgePlugin;
import org.eclipse.mylyn.internal.java.ui.search.AbstractJavaRelationProvider;
import org.eclipse.mylyn.internal.java.ui.search.JUnitReferencesProvider;
import org.eclipse.mylyn.internal.java.ui.search.JavaImplementorsProvider;
import org.eclipse.mylyn.internal.java.ui.search.JavaReadAccessProvider;
import org.eclipse.mylyn.internal.java.ui.search.JavaReferencesProvider;
import org.eclipse.mylyn.internal.java.ui.search.JavaWriteAccessProvider;
import org.eclipse.swt.graphics.Image;

/**
 * @author Mik Kersten
 * @since 3.0
 */
public class JavaContextLabelProvider extends AppearanceAwareLabelProvider {

	private static final String LABEL_ELEMENT_MISSING = "<missing element>";

	private static final ImageDescriptor EDGE_REF_JUNIT = JavaUiBridgePlugin.getImageDescriptor("icons/elcl16/edge-ref-junit.gif");

	public JavaContextLabelProvider() {
		super(AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS | JavaElementLabels.P_COMPRESSED,
				AppearanceAwareLabelProvider.DEFAULT_IMAGEFLAGS | JavaElementImageProvider.SMALL_ICONS);
	}

	@Override
	public String getText(Object object) {
		if (object instanceof IInteractionElement) {
			IInteractionElement node = (IInteractionElement) object;
			if (JavaStructureBridge.CONTENT_TYPE.equals(node.getContentType())) {
				IJavaElement element = JavaCore.create(node.getHandleIdentifier());
				if (element == null) {
					return LABEL_ELEMENT_MISSING;
				} else {
					return getTextForElement(element);
				}
			}
		} else if (object instanceof IInteractionRelation) {
			return getNameForRelationship(((IInteractionRelation) object).getRelationshipHandle());
		} else if (object instanceof IJavaElement) {
			return getTextForElement((IJavaElement) object);
		}
		return super.getText(object);
	}

	private String getTextForElement(IJavaElement element) {
		if (DelegatingContextLabelProvider.isQualifyNamesMode()) {
			if (element instanceof IMember && !(element instanceof IType)) {
				String parentName = ((IMember) element).getParent().getElementName();
				if (parentName != null && parentName != "") {
					return parentName + '.' + super.getText(element);
				}
			}
		}
		if (element.exists()) {
			return super.getText(element);
		} else {
			return LABEL_ELEMENT_MISSING;
		}
	}

	@Override
	public Image getImage(Object object) {
		if (object instanceof IInteractionElement) {
			IInteractionElement node = (IInteractionElement) object;
			if (node.getContentType().equals(JavaStructureBridge.CONTENT_TYPE)) {
				return super.getImage(JavaCore.create(node.getHandleIdentifier()));
			}
		} else if (object instanceof IInteractionRelation) {
			ImageDescriptor descriptor = getIconForRelationship(((IInteractionRelation) object).getRelationshipHandle());
			if (descriptor != null) {
				return CommonImages.getImage(descriptor);
			} else {
				return null;
			}
		}
		return super.getImage(object);
	}

	private ImageDescriptor getIconForRelationship(String relationshipHandle) {
		if (relationshipHandle.equals(AbstractJavaRelationProvider.ID_GENERIC)) {
			return SandboxUiImages.EDGE_REFERENCE;
		} else if (relationshipHandle.equals(JavaReferencesProvider.ID)) {
			return SandboxUiImages.EDGE_REFERENCE;
		} else if (relationshipHandle.equals(JavaImplementorsProvider.ID)) {
			return SandboxUiImages.EDGE_INHERITANCE;
		} else if (relationshipHandle.equals(JUnitReferencesProvider.ID)) {
			return EDGE_REF_JUNIT;
		} else if (relationshipHandle.equals(JavaWriteAccessProvider.ID)) {
			return SandboxUiImages.EDGE_ACCESS_WRITE;
		} else if (relationshipHandle.equals(JavaReadAccessProvider.ID)) {
			return SandboxUiImages.EDGE_ACCESS_READ;
		} else {
			return null;
		}
	}

	private String getNameForRelationship(String relationshipHandle) {
		if (relationshipHandle.equals(AbstractJavaRelationProvider.ID_GENERIC)) {
			return AbstractJavaRelationProvider.NAME;
		} else if (relationshipHandle.equals(JavaReferencesProvider.ID)) {
			return JavaReferencesProvider.NAME;
		} else if (relationshipHandle.equals(JavaImplementorsProvider.ID)) {
			return JavaImplementorsProvider.NAME;
		} else if (relationshipHandle.equals(JUnitReferencesProvider.ID)) {
			return JUnitReferencesProvider.NAME;
		} else if (relationshipHandle.equals(JavaWriteAccessProvider.ID)) {
			return JavaWriteAccessProvider.NAME;
		} else if (relationshipHandle.equals(JavaReadAccessProvider.ID)) {
			return JavaReadAccessProvider.NAME;
		} else if (relationshipHandle.equals(InteractionContextManager.CONTAINMENT_PROPAGATION_ID)) {
			return "Containment"; // TODO: make this generic?
		} else {
			return null;
		}
	}

	public static AppearanceAwareLabelProvider createJavaUiLabelProvider() {
		AppearanceAwareLabelProvider javaUiLabelProvider = new AppearanceAwareLabelProvider(
				AppearanceAwareLabelProvider.DEFAULT_TEXTFLAGS | JavaElementLabels.P_COMPRESSED,
				AppearanceAwareLabelProvider.DEFAULT_IMAGEFLAGS | JavaElementImageProvider.SMALL_ICONS);
		javaUiLabelProvider.addLabelDecorator(new TreeHierarchyLayoutProblemsDecorator());
		return javaUiLabelProvider;
	}
}
