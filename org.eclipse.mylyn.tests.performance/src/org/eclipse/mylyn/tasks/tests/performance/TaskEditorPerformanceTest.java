/*******************************************************************************
 * Copyright (c) 2009, 2013 Tasktop Technologies and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/

package org.eclipse.mylyn.tasks.tests.performance;

import java.io.File;
import java.io.IOException;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.mylyn.commons.workbench.WorkbenchUtil;
import org.eclipse.mylyn.internal.tasks.core.AbstractTask;
import org.eclipse.mylyn.internal.tasks.core.LocalRepositoryConnector;
import org.eclipse.mylyn.internal.tasks.ui.editors.TaskEditorExtensions;
import org.eclipse.mylyn.internal.tasks.ui.util.TasksUiInternal;
import org.eclipse.mylyn.tasks.core.TaskRepository;
import org.eclipse.mylyn.tasks.ui.TasksUi;
import org.eclipse.mylyn.tasks.ui.TasksUiUtil;
import org.eclipse.mylyn.tests.performance.PerformanceConstants;
import org.eclipse.mylyn.tests.performance.support.CommonTestUtil;
import org.eclipse.mylyn.tests.performance.support.UiTestUtil;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.test.performance.Dimension;
import org.eclipse.test.performance.PerformanceTestCase;

/**
 * @author Steffen Pingel
 */
public class TaskEditorPerformanceTest extends PerformanceTestCase {

	private static String ID_NONE = "none";

	private static String ID_MEDIA_WIKI = "org.eclipse.mylyn.wikitext.tasks.ui.editor.mediaWikiTaskEditorExtension";

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		UiTestUtil.closeWelcomeView();
	}

	@Override
	protected void tearDown() throws Exception {
		UiTestUtil.closeAllEditors();
		super.tearDown();
	}

	public void testOpenStackTrace() throws IOException {
		TaskRepository repository = TasksUi.getRepositoryManager().getRepository(
				LocalRepositoryConnector.CONNECTOR_KIND, LocalRepositoryConnector.REPOSITORY_URL);
		TaskEditorExtensions.setTaskEditorExtensionId(repository, ID_NONE);

		AbstractTask task = TasksUiInternal.createNewLocalTask("test performance");
		File file = CommonTestUtil.getFile(this, "testdata/stack-trace.txt");
		task.setNotes(CommonTestUtil.read(file));

		for (int i = 0; i < PerformanceConstants.REPEAT; i++) {
			startMeasuring();
			TasksUiUtil.openTask(task);
			stopMeasuring();

			UiTestUtil.closeAllEditors();
		}

		tagAsSummary("Open Bugzilla Task in Editor", Dimension.CPU_TIME);
		commitMeasurements();
		assertPerformance();
	}

	public void testOpenStackTraceMediaWiki() throws IOException {
		TaskRepository repository = TasksUi.getRepositoryManager().getRepository(
				LocalRepositoryConnector.CONNECTOR_KIND, LocalRepositoryConnector.REPOSITORY_URL);
		TaskEditorExtensions.setTaskEditorExtensionId(repository, ID_MEDIA_WIKI);

		AbstractTask task = TasksUiInternal.createNewLocalTask("test performance");
		File file = CommonTestUtil.getFile(this, "testdata/stack-trace.txt");
		task.setNotes(CommonTestUtil.read(file));

		for (int i = 0; i < PerformanceConstants.REPEAT; i++) {
			startMeasuring();
			TasksUiUtil.openTask(task);
			stopMeasuring();

			UiTestUtil.closeAllEditors();
		}

		tagAsSummary("Open Bugzilla Task in Editor", Dimension.CPU_TIME);
		commitMeasurements();
		assertPerformance();
	}

	public void testOpenNewBugzillaTask() throws Exception {
		TaskRepository repository = new TaskRepository("bugzilla", "http://mylyn.org/bugzilla-4.4");
		TasksUi.getRepositoryManager().addRepository(repository);
		TasksUi.getRepositoryConnector(repository.getConnectorKind()).updateRepositoryConfiguration(repository,
				new NullProgressMonitor());

		Shell shell = WorkbenchUtil.getShell();
		for (int i = 0; i < PerformanceConstants.REPEAT; i++) {
			startMeasuring();
			TasksUiUtil.openNewTaskEditor(shell, null, repository);
			stopMeasuring();

			UiTestUtil.closeAllEditors();
		}

		tagAsSummary("Open Bugzilla Task in Editor", Dimension.CPU_TIME);
		commitMeasurements();
		assertPerformance();
	}

	public void testOpenNewLocalTask() {
		Shell shell = WorkbenchUtil.getShell();
		for (int i = 0; i < PerformanceConstants.REPEAT; i++) {
			startMeasuring();
			TasksUiUtil.openNewLocalTaskEditor(shell, null);
			stopMeasuring();

			UiTestUtil.closeAllEditors();
		}

		commitMeasurements();
		assertPerformance();
	}

}
