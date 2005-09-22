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
package org.eclipse.mylar.monitor.reports.ui.views;import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.mylar.core.MylarPlugin;
import org.eclipse.mylar.monitor.MonitorImages;
import org.eclipse.mylar.monitor.MylarMonitorPlugin;
import org.eclipse.mylar.monitor.reports.ReportGenerator;
import org.eclipse.mylar.monitor.reports.internal.InteractionEventSummarySorter;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.Workbench;
import org.eclipse.ui.part.ViewPart;

/**
 * @author Leah Findlater and Mik Kersten
 */public class UsageCountView extends ViewPart {
	private ReportGenerator parser;
	    private TableViewer viewer;    private Table table;    private Action exportToFileAction;    private Action importFromZipAction;        private Action importFromFileAction;    private Action refreshAction;    private File source;    /* Table column property names */    private String[] columnNames = new String[] {    "Type",    "ID",    "Name",    "Usage Count"    };
        public UsageCountView() {
//    	List<IStatsCollector> collectors = new ArrayList<IStatsCollector>();
//		collectors.add(new SummaryCollector());    	parser = new ReportGenerator(MylarMonitorPlugin.getDefault().getInteractionLogger(), false);    }    /**     * This is a callback that will allow us to create the viewer and initialize     * it.     */    @Override    public void createPartControl(Composite parent) {        createTable(parent);        createTableViewer(table);        makeActions();        hookContextMenu();        contributeToActionBars();    }    // public void createPartControl(Composite parent) {    // viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL |    // SWT.V_SCROLL);    // viewer.setContentProvider(new ViewContentProvider());    // viewer.setLabelProvider(new ViewLabelProvider());    // viewer.setSorter(new NameSorter());    // viewer.setInput(getViewSite());    // makeActions();    // hookContextMenu();    // hookDoubleClickAction();    // contributeToActionBars();    // }    private void hookContextMenu() {        MenuManager menuMgr = new MenuManager("#PopupMenu");        menuMgr.setRemoveAllWhenShown(true);        menuMgr.addMenuListener(new IMenuListener() {            public void menuAboutToShow(IMenuManager manager) {                UsageCountView.this.fillContextMenu(manager);            }        });        Menu menu = menuMgr.createContextMenu(viewer.getControl());        viewer.getControl().setMenu(menu);        getSite().registerContextMenu(menuMgr, viewer);    }    private void contributeToActionBars() {        IActionBars bars = getViewSite().getActionBars();        fillLocalPullDown(bars.getMenuManager());        fillLocalToolBar(bars.getToolBarManager());    }    private void fillLocalPullDown(IMenuManager manager) {        // manager.add(exportToFileAction);        manager.add(refreshAction);        manager.add(importFromFileAction);                manager.add(importFromZipAction);    }    private void fillContextMenu(IMenuManager manager) {        // manager.add(exportToFileAction);        manager.add(refreshAction);        manager.add(importFromFileAction);        manager.add(importFromZipAction);        // Other plug-ins can contribute their actions here        manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));    }    private void fillLocalToolBar(IToolBarManager manager) {        // manager.add(exportToFileAction);        manager.add(refreshAction);        manager.add(importFromFileAction);        manager.add(importFromZipAction);    }    private void makeActions() {        exportToFileAction = new Action() {            @Override            public void run() {                // TODO implement export to file            }        };        exportToFileAction.setText("Export to external file");        exportToFileAction.setToolTipText("Save usage summary to file");        importFromFileAction = new Action() {            @Override            public void run() {                FileDialog dialog = new FileDialog(Workbench.getInstance()                        .getActiveWorkbenchWindow().getShell());                dialog.setText("Specify a file to import");                dialog.setFilterExtensions(new String[] { "*.xml", "*.*" });                dialog.setFilterPath(source.getPath());                String sourceString = dialog.open();                if (sourceString != null) {                    source = new File(sourceString);                    viewer.refresh();                }            }        };        importFromFileAction.setText("Import from log file");        importFromFileAction.setToolTipText("Import from log file");        importFromFileAction.setImageDescriptor(PlatformUI.getWorkbench()                .getSharedImages().                getImageDescriptor(ISharedImages.IMG_OBJ_FOLDER));                importFromZipAction = new Action() {        	private byte[] buffer = new byte[8192];        	        	public void transferData(InputStream sourceStream, OutputStream destination) throws IOException {        		int bytesRead = 0;        		while(bytesRead != -1){        			bytesRead = sourceStream.read(buffer, 0, buffer.length);        			if(bytesRead != -1){        				destination.write(buffer, 0, bytesRead);        			}        		}        	}        	            @Override            public void run() {                FileDialog dialog = new FileDialog(Workbench.getInstance()                        .getActiveWorkbenchWindow().getShell());                dialog.setText("Specify a zip file to import");                dialog.setFilterExtensions(new String[] { "*.zip"});                dialog.setFilterPath(source.getPath());                String sourceString = dialog.open();                if (sourceString != null) {                	try{                		ZipFile zip = new ZipFile(sourceString);                		if(zip.entries().hasMoreElements()){                			ZipEntry entry = zip.entries().nextElement();                			File f = File.createTempFile("mylarTemp", "xml");                			InputStream in = zip.getInputStream(entry);                			OutputStream out = new FileOutputStream(f);                			transferData(in, out);                			source = f;                		}                		                	}catch(Exception e){                		MylarPlugin.log(e, "zip writing failed");                	}                    viewer.refresh();                }            }        };        importFromZipAction.setText("Import from zip file");        importFromZipAction.setToolTipText("Import from zip file");        importFromZipAction.setImageDescriptor(MonitorImages.ZIP_FILE);                refreshAction = new Action() {            @Override            public void run() {            	parser.getStatisticsFromInteractionHistory(MylarMonitorPlugin.getDefault().getMonitorFile());                viewer.refresh();            }        };        refreshAction.setText("Refresh report");        refreshAction.setToolTipText("Refresh report");        refreshAction.setImageDescriptor(MonitorImages.REFRESH);    }    /**     * Passing the focus request to the viewer's control.     */    @Override    public void setFocus() {        viewer.getControl().setFocus();    }    /**     * Create table to be used by TableViewer     *      * @param parent     */    private void createTable(Composite parent) {        int style = SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL |        SWT.FULL_SELECTION | SWT.HIDE_SELECTION;//        final int NUMBER_COLUMNS = 4;        table = new Table(parent, style);        GridData gridData = new GridData(GridData.FILL_BOTH);        gridData.grabExcessVerticalSpace = true;        gridData.grabExcessHorizontalSpace = true;        // gridData.horizontalSpan = 4;        table.setLayoutData(gridData);        table.setLinesVisible(true);        table.setHeaderVisible(true);        // Set up the columns and add listeners to sort by column when column is        // selected.        TableColumn column = new TableColumn(table, SWT.LEFT, 0);        column.setText("Type");        column.setWidth(100);        column.addSelectionListener(new SelectionAdapter() {            @Override            public void widgetSelected(SelectionEvent e) {                viewer.setSorter(new InteractionEventSummarySorter(                        InteractionEventSummarySorter.TYPE));            }        });        column = new TableColumn(table, SWT.LEFT, 1);        column.setText("ID");        column.setWidth(30);        column.addSelectionListener(new SelectionAdapter() {            @Override            public void widgetSelected(SelectionEvent e) {                viewer                        .setSorter(new InteractionEventSummarySorter(                                InteractionEventSummarySorter.ID));            }        });        column = new TableColumn(table, SWT.LEFT, 2);        column.setText("Name");        column.setWidth(150);        column.addSelectionListener(new SelectionAdapter() {            @Override            public void widgetSelected(SelectionEvent e) {                viewer.setSorter(new InteractionEventSummarySorter(                        InteractionEventSummarySorter.NAME));            }        });        column = new TableColumn(table, SWT.LEFT, 3);        column.setText("Count");        column.setWidth(50);        column.addSelectionListener(new SelectionAdapter() {            @Override            public void widgetSelected(SelectionEvent e) {                viewer.setSorter(new InteractionEventSummarySorter(                        InteractionEventSummarySorter.USAGE_COUNT));            }        });    }    private void createTableViewer(Composite parent) {        viewer = new TableViewer(table);        viewer.setUseHashlookup(true);        viewer.setColumnProperties(columnNames);
        
        List<File> usageFiles = new ArrayList<File>();
        usageFiles.add(MylarMonitorPlugin.getDefault().getMonitorFile());        viewer.setContentProvider(new UsageCountContentProvider(parser));        
        viewer.setLabelProvider(new UsageCountLabelProvider());        viewer.setInput(getViewSite());    }}