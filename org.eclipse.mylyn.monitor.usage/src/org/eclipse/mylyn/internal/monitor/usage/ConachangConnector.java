/*******************************************************************************
 * Copyright (c) 2014 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.internal.monitor.usage;

import java.util.Date;
import java.util.LinkedList;

import jp.ac.titech.tkobaya.mylynreader.StructureHandle;
import log.EventType;

import org.eclipse.core.filebuffers.FileBuffers;
import org.eclipse.core.filebuffers.ITextFileBuffer;
import org.eclipse.core.filebuffers.ITextFileBufferManager;
import org.eclipse.core.filebuffers.LocationKind;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.text.IDocument;
import org.eclipse.mylyn.monitor.core.InteractionEvent;
import org.eclipse.mylyn.monitor.core.InteractionEvent.Kind;
import org.eclipse.mylyn.tasks.core.ITask;
import org.eclipse.mylyn.tasks.ui.TasksUi;

import analyzer.mylyn.ConachangRecordContent;
import analyzer.mylyn.InteractionRecorder;

public class ConachangConnector {

	private StructureHandle lastFileStructureHandle;

	private int lastFileHashCode = 0;

	private final LinkedList<Date> editTime = new LinkedList<Date>();

	log.InteractionHistory ih;

	boolean isNeedToRecord = false;

	/**
	 * The constructor
	 */
	public ConachangConnector() {
		lastFileStructureHandle = null;
	}

	/**
	 * Make conachang InteractionEvent by joining each mylyn's InteractionEvent. If the structureHandle was changed,
	 * this calls {@link ConachangConnector#recordInteraction() recordInteraction()}. This is always Notified new event
	 * from InteractionEventListener. This must be called in condition a Mylyn-Task has started.
	 *
	 * @param event
	 */
	public void newEvent(InteractionEvent event) {
		if (isFileFocusedEvent(event) && InteractionRecorder.isActive()) {

			StructureHandle sh = new StructureHandle(event.getStructureHandle());

			if (sh.isJavaFile()) {
				return;
			}
			if (event.getKind() == Kind.SELECTION
					|| (event.getKind() == Kind.EDIT && !lastFileStructureHandle.equals(sh))) {
				if (lastFileStructureHandle != null) {
					recordInteraction();
				}
				lastFileStructureHandle = sh;
				lastFileHashCode = getHashCode(lastFileStructureHandle);
				editTime.clear();
				editTime.add(event.getDate());
			} else if (event.getKind() == Kind.EDIT && lastFileStructureHandle.equals(sh)) {
				editTime.add(event.getDate());
			}
		} else if (event.getKind() == Kind.COMMAND) {
			if (event.getOriginId().startsWith("org.eclipse.ui.file.save")//Ctrl+S, SaveIcon, :w (Vim) //$NON-NLS-1$
					|| event.getOriginId().startsWith("org.eclipse.debug.ui")//Ctrl+Shift+F11 (Run) //$NON-NLS-1$
					|| event.getOriginId().startsWith("org.eclipse.debug.internal.ui.actions"))//RunIcon, Menubar->Run //$NON-NLS-1$
			{
				if (lastFileStructureHandle != null) {
					recordInteraction();
				}
				lastFileStructureHandle = null;
				lastFileHashCode = 0;
				editTime.clear();
			}

		}

		//Detect the task finishes.
		ITask task = TasksUi.getTaskActivityManager().getActiveTask();
		if (task != null && !InteractionRecorder.isActive()) {
			InteractionRecorder.taskStarted(task);
			lastFileStructureHandle = null;
			editTime.clear();
		} else if (task == null && InteractionRecorder.isActive()) {
			InteractionRecorder.taskStopped();
		}

	}

	/**
	 * @param event
	 * @return true if the event focuses IFile element.
	 */
	private boolean isFileFocusedEvent(InteractionEvent event) {
		if (event.getStructureHandle().equals("null")) { //$NON-NLS-1$
			return false;
		}
		StructureHandle sh = new StructureHandle(event.getStructureHandle());
		IFile iFile = ResourcesPlugin.getWorkspace()
				.getRoot()
				.getProject(sh.getProjectName())
				.getFile(sh.getPathFromSrc());
		return iFile.exists();
	}

	/**
	 * @param lastFileStructureHandle
	 * @param lastFileHashCode
	 * @return return true if and only if the previous interacted file was changed.
	 */
	private boolean isFileChanged(StructureHandle lastFileStructureHandle, int lastFileHashCode) {
		if (lastFileHashCode == 0 || lastFileStructureHandle == null) {
			return false;
		}
		if (lastFileHashCode != getHashCode(lastFileStructureHandle)) {
			return true;
		} else {
			return false;
		}

	}

	/**
	 * returns the hashcode of the file which StructureHandle indicates.
	 *
	 * @param structureHandle
	 * @return hashcode
	 */
	private int getHashCode(StructureHandle structureHandle) {
		int hashCode = 0;
		IFile iFile = ResourcesPlugin.getWorkspace()
				.getRoot()
				.getProject(structureHandle.getProjectName())
				.getFile(structureHandle.getPathFromSrc());
		if (iFile.exists()) {
			IPath path = iFile.getFullPath();
			try {
				ITextFileBufferManager manager = FileBuffers.getTextFileBufferManager();
				manager.connect(path, LocationKind.IFILE, null);
				ITextFileBuffer buffer = manager.getTextFileBuffer(path, LocationKind.IFILE);
				IDocument document = buffer.getDocument();
				hashCode = document.get().hashCode();
				manager.disconnect(path, LocationKind.IFILE, null);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return hashCode;
	}

	public void startMonitoring() {
		isNeedToRecord = true;
		lastFileHashCode = 0;
		lastFileStructureHandle = null;
		InteractionRecorder.startMonitoring();
		ITask task = TasksUi.getTaskActivityManager().getActiveTask();
		if (task != null) {
			InteractionRecorder.taskStarted(task);
		}
	}

	/**
	 * Sometimes, InteractionEventLogger calls stopMonitoring() twice. So, this method ignore the 2nd calling. TODO If
	 * closing eclipse with no saving, isFileChanged() is always being true.
	 */
	public void stopMonitoring() {
		if (!editTime.isEmpty()) {
			recordInteraction();
		}
		if (isNeedToRecord) {
			lastFileHashCode = 0;
			lastFileStructureHandle = null;
			editTime.clear();
			InteractionRecorder.stopMonitoring();
			isNeedToRecord = false;
		}

	}

	private void recordInteraction() {
		@SuppressWarnings("unchecked")
		LinkedList<Date> recordEditTime = (LinkedList<Date>) editTime.clone();
		ConachangRecordContent content = new ConachangRecordContent(lastFileStructureHandle, recordEditTime,
				isFileChanged(lastFileStructureHandle, lastFileHashCode) ? EventType.WRITE : EventType.READ,
						new String());
		InteractionRecorder.receiveInteractionFromMylyn(content);
		//InteractionRecorder.recordInteraction(lastFileStructureHandle, editTime,
		//isFileChanged(lastFileStructureHandle, lastFileHashCode) ? EventType.WRITE : EventType.READ,
		//new String());
	}

}