/*******************************************************************************
 * Copyright (c) 2011 Tasktop Technologies.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Tasktop Technologies - initial API and implementation
 *******************************************************************************/
package org.eclipse.mylyn.internal.sandbox.search.ui;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.DialogPage;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.mylyn.sandbox.search.ui.SearchCriteria;
import org.eclipse.osgi.util.NLS;
import org.eclipse.search.ui.ISearchPage;
import org.eclipse.search.ui.ISearchPageContainer;
import org.eclipse.search.ui.NewSearchUI;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * A search page for desktop search
 * 
 * @author David Green
 */
public class DesktopSearchPage extends DialogPage implements ISearchPage {

	private static final String PAGE_NAME = "DesktopSearch"; //$NON-NLS-1$

	public static final String PAGE_ID = "org.eclipse.mylyn.internal.sandbox.search.ui.desktopSearchPage"; //$NON-NLS-1$

	private static final int MAX_HISTORY = 20;

	private ISearchPageContainer container;

	private Combo searchText;

	private boolean initialized;

	private List<SearchCriteria> searchHistory;

	private Combo filenamePatternText;

	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NONE);
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(false).applyTo(container);

		addTextControls(container);
		addFileNamePatternControls(container);

		Dialog.applyDialogFont(container);
		setControl(container);
	}

	private void addTextControls(Composite container) {
		Label searchLabel = new Label(container, SWT.LEAD);
		searchLabel.setText(Messages.DesktopSearchPage_TextLabel);
		GridDataFactory.swtDefaults().span(2, 1).applyTo(searchLabel);

		searchText = new Combo(container, SWT.SINGLE | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(searchText);
		searchText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				searchTextSelected();
				updateStatus();
			}
		});
		searchText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateStatus();
			}
		});

	}

	private void addFileNamePatternControls(Composite container) {
		Label searchLabel = new Label(container, SWT.LEAD);
		searchLabel.setText(Messages.DesktopSearchPage_FilenamePatterns);
		GridDataFactory.swtDefaults().span(2, 1).applyTo(searchLabel);

		filenamePatternText = new Combo(container, SWT.SINGLE | SWT.BORDER);
		GridDataFactory.fillDefaults().grab(true, false).span(2, 1).applyTo(filenamePatternText);
		filenamePatternText.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				updateStatus();
			}
		});
		filenamePatternText.addModifyListener(new ModifyListener() {
			public void modifyText(ModifyEvent e) {
				updateStatus();
			}
		});
	}

	private void searchTextSelected() {
		int index = searchText.getSelectionIndex();
		if (index >= 0 && index < getSearchHistory().size()) {
			SearchCriteria item = getSearchHistory().get(index);
			if (!searchText.getText().equals(item.getText())) {
				return;
			}
			filenamePatternText.setText(item.getFilenamePatternsAsText());
		}
	}

	public boolean performAction() {
		SearchCriteria criteria = computeSearchCriteria();
		getSearchHistory().remove(criteria);
		getSearchHistory().add(0, criteria);
		saveSearchHistory();

		try {
			NewSearchUI.runQueryInBackground(new DesktopSearchQuery(SearchProviders.getSearchProvider(), criteria));
		} catch (CoreException e) {
			ErrorDialog.openError(getShell(), Messages.DesktopSearchPage_SearchFailedTitle,
					NLS.bind(Messages.DesktopSearchPage_SearchFailedMessage, e.getStatus().getMessage()), e.getStatus());
			return false;
		}
		return true;
	}

	public void setContainer(ISearchPageContainer container) {
		this.container = container;
	}

	public ISearchPageContainer getContainer() {
		return container;
	}

	@Override
	public void setVisible(boolean visible) {
		if (!initialized) {
			initialized = true;
			String[] previousSearchItems = getPreviousSearchItems();
			searchText.setItems(previousSearchItems);
			if (!initializeSearchSettings()) {
				if (!getSearchHistory().isEmpty()) {
					initializeSearchSettings(getSearchHistory().get(0));
				}
			}
			if (filenamePatternText.getText().length() == 0) {
				filenamePatternText.setText("*"); //$NON-NLS-1$
			}
			updateStatus();
		}
		super.setVisible(visible);
	}

	private String[] getPreviousSearchItems() {
		List<SearchCriteria> searchHistory = getSearchHistory();
		List<String> items = new ArrayList<String>();
		for (SearchCriteria item : searchHistory) {
			items.add(item.getText());
		}
		return items.toArray(new String[items.size()]);
	}

	private SearchCriteria computeSearchCriteria() {
		SearchCriteria item = new SearchCriteria();
		item.setText(searchText.getText());
		item.setFilenamePatternsAsText(filenamePatternText.getText());
		return item;
	}

	private IDialogSettings getDialogSettings() {
		return SearchPlugin.getDefault().getDialogSettings(PAGE_NAME);
	}

	public List<SearchCriteria> getSearchHistory() {
		if (searchHistory == null) {
			loadSearchHistory();
		}
		return searchHistory;
	}

	private void loadSearchHistory() {
		List<SearchCriteria> newSearchHistory = new ArrayList<SearchCriteria>();

		SearchCriteriaPersistence persistence = new SearchCriteriaPersistence();

		IDialogSettings settings = getDialogSettings();
		final String historyPrefix = "history"; //$NON-NLS-1$
		for (int x = 0; x < MAX_HISTORY; ++x) {
			IDialogSettings historySection = settings.getSection(historyPrefix + x);
			if (historySection == null) {
				break;
			}
			SearchCriteria item = new SearchCriteria();
			persistence.load(item, historySection);
			newSearchHistory.add(item);
		}
		this.searchHistory = newSearchHistory;
	}

	private void saveSearchHistory() {
		SearchCriteriaPersistence persistence = new SearchCriteriaPersistence();

		IDialogSettings settings = getDialogSettings();
		final String historyPrefix = "history"; //$NON-NLS-1$
		for (int x = 0; x < MAX_HISTORY; ++x) {
			String itemName = historyPrefix + x;
			IDialogSettings historySection = settings.getSection(itemName);
			if (historySection == null) {
				if (x >= searchHistory.size()) {
					break;
				}
				historySection = settings.addNewSection(itemName);
			}
			persistence.save(searchHistory.get(x), historySection);
		}
	}

	private void updateStatus() {
		boolean ok = true;
		SearchCriteria item = computeSearchCriteria();
		if (item.getText() == null || item.getText().trim().length() == 0) {
			ok = false;
			setMessage(Messages.DesktopSearchPage_SpecifyTextPrompt, ERROR);
		}
		if (ok && item.getFilenamePatterns().length == 0) {
			ok = false;
			setMessage(Messages.DesktopSearchPage_SpecifyFilenamePatternsPrompt, ERROR);
		}
		if (ok) {
			setMessage(null);
		}
		getContainer().setPerformActionEnabled(ok);
	}

	private boolean initializeSearchSettings() {
		ISelection selection = getContainer().getSelection();
		if (selection instanceof ITextSelection && !selection.isEmpty()) {
			String text = ((ITextSelection) selection).getText();
			if (text != null && text.trim().length() > 0) {
				SearchCriteria item = computeSearchHistoryItem(text);
				initializeSearchSettings(item);
				return true;
			}
		}
		return false;
	}

	private void initializeSearchSettings(SearchCriteria item) {
		searchText.setText(item.getText());
		filenamePatternText.setText(item.getFilenamePatternsAsText());
	}

	private SearchCriteria computeSearchHistoryItem(String text) {
		for (SearchCriteria historyItem : getSearchHistory()) {
			if (historyItem.getText().equals(text)) {
				return historyItem;
			}
		}
		// default
		SearchCriteria historyItem = new SearchCriteria();
		historyItem.setText(text);
		historyItem.setFilenamePatternsAsText("*"); //$NON-NLS-1$
		return historyItem;
	}
}
