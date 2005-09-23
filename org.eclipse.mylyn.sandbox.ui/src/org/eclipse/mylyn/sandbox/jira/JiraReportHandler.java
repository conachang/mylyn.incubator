/*******************************************************************************
 * Copyright (c) 2004 - 2005 University Of British Columbia and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     University Of British Columbia - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylar.sandbox.jira;

import org.eclipse.jface.action.Action;
import org.eclipse.mylar.tasklist.ICategory;
import org.eclipse.mylar.tasklist.ITask;
import org.eclipse.mylar.tasklist.ITaskHandler;
import org.eclipse.mylar.tasklist.ITaskListElement;
import org.eclipse.mylar.tasklist.internal.TaskCategory;
import org.eclipse.mylar.tasklist.ui.views.TaskListView;
import org.eclipse.ui.IWorkbenchPage;

public class JiraReportHandler implements ITaskHandler {

	public void itemDeleted(ITaskListElement element) {
		// TODO Auto-generated method stub

	}

	public void taskCompleted(ITask task) {
		// TODO Auto-generated method stub

	}

	public void itemOpened(ITaskListElement element) {
		// TODO Auto-generated method stub

	}

	public void taskClosed(ITask element, IWorkbenchPage page) {
		// TODO Auto-generated method stub

	}

	public boolean acceptsItem(ITaskListElement element) {
		// TODO Auto-generated method stub
		return false;
	}

	public void dropItem(ITaskListElement element, TaskCategory category) {
		// TODO Auto-generated method stub

	}

	public ITask taskAdded(ITask newTask) {
		// TODO Auto-generated method stub
		return null;
	}

	public void restoreState(TaskListView taskListView) {
		// TODO Auto-generated method stub

	}

	public boolean enableAction(Action action, ITaskListElement element) {
		// TODO Auto-generated method stub
		return false;
	}

	public void itemRemoved(ITaskListElement element, ICategory category) {
		// TODO Auto-generated method stub

	}

	public ITask dropItemToPlan(ITaskListElement element) {
		// TODO Auto-generated method stub
		return null;
	}

}