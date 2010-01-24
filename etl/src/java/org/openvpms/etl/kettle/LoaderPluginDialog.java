/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.kettle;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CCombo;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.ShellAdapter;
import org.eclipse.swt.events.ShellEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;
import org.openvpms.etl.load.Mapping;
import org.openvpms.etl.load.Mappings;
import org.pentaho.di.core.Const;
import org.pentaho.di.core.Props;
import org.pentaho.di.core.exception.KettleException;
import org.pentaho.di.core.row.RowMetaInterface;
import org.pentaho.di.trans.TransMeta;
import org.pentaho.di.trans.step.StepDialogInterface;
import org.pentaho.di.ui.core.dialog.ErrorDialog;
import org.pentaho.di.ui.core.widget.ColumnInfo;
import org.pentaho.di.ui.core.widget.TableView;
import org.pentaho.di.ui.trans.step.BaseStepDialog;


/**
 * The OpenVPMS Loader plugin dialog.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LoaderPluginDialog extends BaseStepDialog
        implements StepDialogInterface {

    /**
     * The plugin meta data.
     */
    private LoaderPluginMeta input;

    /**
     * The tab folder.
     */
    private CTabFolder tabFolder;

    /**
     * The table containing the fields to map.
     */
    private TableView mappingTable;

    /**
     * Text field containing the name of the id field.
     */
    private Text idName;

    /**
     * Text field containing the batch size.
     */
    private Text batchSize;

    /**
     * The database connection combo box.
     */
    private CCombo connection;

    private static final String YES = "Y";  // NON-NLS

    private static final String NO = "N";   // NON-NLS

    /**
     * Check-box to determine if lookups should be generated.
     */
    private Button generateLookups;

    /**
     * Check-box to determine if processed rows should be skipped.
     */
    private Button skipProcessed;


    /**
     * Constructs a new <tt>LoaderPluginDialog</tt>.
     *
     * @param parent    the parent shell
     * @param in        the plugin meta data
     * @param transMeta the transformation meta data
     * @param stepName  the step name
     */
    public LoaderPluginDialog(Shell parent, LoaderPluginMeta in,
                              TransMeta transMeta, String stepName) {
        super(parent, in, transMeta, stepName);
        input = in;
    }

    /**
     * Opens the dialog.
     *
     * @return the step name
     */
    public String open() {
        Shell parent = getParent();
        Display display = parent.getDisplay();

        shell = new Shell(parent, SWT.DIALOG_TRIM | SWT.RESIZE | SWT.MAX | SWT.MIN);
        props.setLook(shell);
        setShellImage(shell, input);

        ModifyListener modifyListener = new ModifyListener() {
            public void modifyText(ModifyEvent event) {
                input.setChanged();
            }
        };
        changed = input.hasChanged();

        FormLayout formLayout = new FormLayout();
        formLayout.marginWidth = Const.FORM_MARGIN;
        formLayout.marginHeight = Const.FORM_MARGIN;

        shell.setLayout(formLayout);
        shell.setText(Messages.get("LoaderPluginDialog.Shell.Label"));

        int middle = props.getMiddlePct();
        int margin = Const.MARGIN;

        // Stepname line
        Label stepNameLabel = new Label(shell, SWT.RIGHT);
        stepNameLabel.setText(Messages.get(
                "LoaderPluginDialog.Stepname.Label"));
        props.setLook(stepNameLabel);
        fdlStepname = new FormData();
        fdlStepname.left = new FormAttachment(0, 0);
        fdlStepname.right = new FormAttachment(middle, -margin);
        fdlStepname.top = new FormAttachment(0, margin);
        stepNameLabel.setLayoutData(fdlStepname);
        wStepname = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        wStepname.setText(stepname);
        props.setLook(wStepname);
        wStepname.addModifyListener(modifyListener);
        fdStepname = new FormData();
        fdStepname.left = new FormAttachment(middle, 0);
        fdStepname.top = new FormAttachment(0, margin);
        fdStepname.right = new FormAttachment(100, 0);
        wStepname.setLayoutData(fdStepname);

        // connection line
        connection = addConnectionLine(shell, wStepname, middle, margin);
        if (input.getMappings().getConnection() == null
            && transMeta.nrDatabases() == 1) {
            connection.select(0);
        }
        connection.addModifyListener(modifyListener);

        // legacy id field line
        Label idLabel = new Label(shell, SWT.RIGHT);
        idLabel.setText(
                Messages.get("LoaderPluginDialog.IdField.Label"));
        props.setLook(idLabel);
        FormData idLabelFormData = new FormData();
        idLabelFormData.left = new FormAttachment(0, 0);
        idLabelFormData.right = new FormAttachment(middle, -margin);
        idLabelFormData.top = new FormAttachment(connection, margin * 2);
        idLabel.setLayoutData(idLabelFormData);

        idName = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(idName);
        idName.addModifyListener(modifyListener);
        FormData idNameFormData = new FormData();
        idNameFormData.left = new FormAttachment(middle, 0);
        idNameFormData.top = new FormAttachment(connection, margin * 2);
        idNameFormData.right = new FormAttachment(100, 0);
        idName.setLayoutData(idNameFormData);

        // batch size line
        Label batchLabel = new Label(shell, SWT.RIGHT);
        batchLabel.setText(Messages.get("LoaderPluginDialog.BatchField.Label"));
        props.setLook(batchLabel);
        FormData batchLabelFormData = new FormData();
        batchLabelFormData.left = new FormAttachment(0, 0);
        batchLabelFormData.right = new FormAttachment(middle, -margin);
        batchLabelFormData.top = new FormAttachment(idName, margin * 2);
        batchLabel.setLayoutData(batchLabelFormData);

        batchSize = new Text(shell, SWT.SINGLE | SWT.LEFT | SWT.BORDER);
        props.setLook(batchSize);
        batchSize.addModifyListener(modifyListener);
        FormData batchSizeFormData = new FormData();
        batchSizeFormData.left = new FormAttachment(middle, 0);
        batchSizeFormData.top = new FormAttachment(idName, margin * 2);
        batchSizeFormData.right = new FormAttachment(100, 0);
        batchSize.setLayoutData(batchSizeFormData);

        // The folders
        tabFolder = new CTabFolder(shell, SWT.BORDER);
        props.setLook(tabFolder, Props.WIDGET_STYLE_TAB);

        // the mapping tab
        CTabItem mapTab = new CTabItem(tabFolder, SWT.NONE);
        mapTab.setText(Messages.get(
                "LoaderPluginDialog.MapTab.TabItem"));

        Composite wSelectComp = new Composite(tabFolder, SWT.NONE);
        props.setLook(wSelectComp);

        FormLayout selectLayout = new FormLayout();
        selectLayout.marginWidth = margin;
        selectLayout.marginHeight = margin;
        wSelectComp.setLayout(selectLayout);

        Label wlFields = new Label(wSelectComp, SWT.NONE);
        wlFields.setText(Messages.get("LoaderPluginDialog.Fields.Label"));
        props.setLook(wlFields);
        FormData fdlFields = new FormData();
        fdlFields.left = new FormAttachment(0, 0);
        fdlFields.top = new FormAttachment(0, 0);
        wlFields.setLayoutData(fdlFields);

        final int rowCount = input.getMappings().getMappingCount();

        ColumnInfo[] columns = new ColumnInfo[]{
                new ColumnInfo(Messages.get("LoaderPluginDialog.ColumnInfo.Fieldname"),
                               ColumnInfo.COLUMN_TYPE_TEXT, false),
                new ColumnInfo(Messages.get("LoaderPluginDialog.ColumnInfo.MapTo"), ColumnInfo.COLUMN_TYPE_TEXT, false),
                new ColumnInfo(Messages.get("LoaderPluginDialog.ColumnInfo.ExcludeIfNull"),
                               ColumnInfo.COLUMN_TYPE_CCOMBO, getYesNo(), true),
                new ColumnInfo(Messages.get("LoaderPluginDialog.ColumnInfo.Value"), ColumnInfo.COLUMN_TYPE_TEXT, false),
                new ColumnInfo(Messages.get("LoaderPluginDialog.ColumnInfo.RemoveDefaultObjects"),
                               ColumnInfo.COLUMN_TYPE_CCOMBO, getYesNo(), true)};

        mappingTable = new TableView(transMeta, wSelectComp, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI,
                                     columns, rowCount, modifyListener, props);

        Button getButton = new Button(wSelectComp, SWT.PUSH);
        getButton.setText(Messages.get("LoaderPluginDialog.GetMap.Button"));
        getButton.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                get();
            }
        });
        FormData fdGetSelect = new FormData();
        fdGetSelect.right = new FormAttachment(100, 0);
        fdGetSelect.top = new FormAttachment(50, 0);
        getButton.setLayoutData(fdGetSelect);

        FormData fdFields = new FormData();
        fdFields.left = new FormAttachment(0, 0);
        fdFields.top = new FormAttachment(wlFields, margin);
        fdFields.right = new FormAttachment(getButton, -margin);
        fdFields.bottom = new FormAttachment(100, 0);
        mappingTable.setLayoutData(fdFields);

        FormData fdSelectComp = new FormData();
        fdSelectComp.left = new FormAttachment(0, 0);
        fdSelectComp.top = new FormAttachment(0, 0);
        fdSelectComp.right = new FormAttachment(100, 0);
        fdSelectComp.bottom = new FormAttachment(100, 0);
        wSelectComp.setLayoutData(fdSelectComp);

        wSelectComp.layout();
        mapTab.setControl(wSelectComp);

        FormData fdTabFolder = new FormData();
        fdTabFolder.left = new FormAttachment(0, 0);
        fdTabFolder.top = new FormAttachment(batchSize, margin);
        fdTabFolder.right = new FormAttachment(100, 0);
        fdTabFolder.bottom = new FormAttachment(100, -50);
        tabFolder.setLayoutData(fdTabFolder);
        // end of the tab folder

        Button ok = new Button(shell, SWT.PUSH);
        ok.setText(Messages.get("System.Button.OK"));  // NON-NLS
        Button cancel = new Button(shell, SWT.PUSH);
        cancel.setText(Messages.get("System.Button.Cancel")); // NON-NLS

        generateLookups = new Button(shell, SWT.CHECK);
        generateLookups.setText(
                Messages.get("LoaderPluginDialog.GenerateLookups"));

        skipProcessed = new Button(shell, SWT.CHECK);
        skipProcessed.setText(
                Messages.get("LoaderPluginDialog.SkipProcessed"));

        setButtonPositions(new Button[]{ok, cancel, generateLookups,
                                        skipProcessed}, margin, tabFolder);

        // Add listeners
        ok.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                ok();
            }
        });
        cancel.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                cancel();
            }
        });

        SelectionAdapter selectionAdapter = new SelectionAdapter() {
            public void widgetDefaultSelected(SelectionEvent event) {
                ok();
            }
        };

        wStepname.addSelectionListener(selectionAdapter);
        idName.addSelectionListener(selectionAdapter);

        // Detect X or ALT-F4 or something that kills this window...
        shell.addShellListener(new ShellAdapter() {
            public void shellClosed(ShellEvent event) {
                cancel();
            }
        });

        // Set the shell size, based upon previous time...
        setSize();

        getData();
        input.setChanged(changed);

        shell.open();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
        return stepname;
    }

    private String[] getYesNo() {
        return new String[]{YES, NO};
    }

    /**
     * Copy information from the meta-data input to the dialog fields.
     */
    private void getData() {
        Mappings mappings = input.getMappings();
        if (mappings.getConnection() != null) {
            connection.setText(input.getMappings().getConnection());
        }

        tabFolder.setSelection(0);
        if (mappings.getIdColumn() != null) {
            idName.setText(mappings.getIdColumn());
        }

        batchSize.setText(Integer.toString(mappings.getBatchSize()));
        generateLookups.setSelection(mappings.getGenerateLookups());

        skipProcessed.setSelection(mappings.getSkipProcessed());

        Mapping[] list = mappings.getMapping();
        for (int i = 0; i < list.length; ++i) {
            TableItem item = mappingTable.table.getItem(i);
            Mapping mapping = list[i];
            if (mapping.getSource() != null) {
                item.setText(1, mapping.getSource());
            }
            if (mapping.getTarget() != null) {
                item.setText(2, mapping.getTarget());
            }
            if (mapping.getExcludeNull()) {
                item.setText(3, YES);
            } else {
                item.setText(3, NO);
            }
            if (mapping.getValue() != null) {
                item.setText(4, mapping.getValue());
            }
            if (mapping.getRemoveDefaultObjects()) {
                item.setText(5, YES);
            } else {
                item.setText(5, NO);
            }
        }
        mappingTable.setRowNums();
        mappingTable.optWidth(true);
        tabFolder.setSelection(0);

        wStepname.setFocus();
        wStepname.selectAll();
    }

    /**
     * Invoked when the 'OK' button is pressed.
     */
    private void ok() {
        stepname = wStepname.getText(); // return value
        Mappings mappings = new Mappings();
        mappings.setConnection(connection.getText());
        mappings.setIdColumn(idName.getText());
        int value = 1000;
        try {
            value = Integer.valueOf(batchSize.getText());
        } catch (NumberFormatException ignore) {
            batchSize.setText(Integer.toString(value));
        }
        mappings.setBatchSize(value);
        mappings.setGenerateLookups(generateLookups.getSelection());
        mappings.setSkipProcessed(skipProcessed.getSelection());
        int count = mappingTable.nrNonEmpty();
        for (int i = 0; i < count; i++) {
            TableItem item = mappingTable.getNonEmpty(i);
            Mapping mapping = new Mapping();
            mapping.setSource(item.getText(1));
            mapping.setTarget(item.getText(2));
            boolean exclude = YES.equals(item.getText(3));
            mapping.setExcludeNull(exclude);
            mapping.setValue(item.getText(4));
            boolean removeDefaultObjects = YES.equals(item.getText(5));
            mapping.setRemoveDefaultObjects(removeDefaultObjects);
            mappings.addMapping(mapping);
        }
        input.setMappings(mappings);

        dispose();
    }

    /**
     * Invoked when the 'Cancel' button is pressed.
     */
    private void cancel() {
        stepname = null;
        input.setChanged(changed);
        dispose();
    }

    /**
     * Invoked when the 'Get fields' button is pressed.
     */
    private void get() {
        try {
            RowMetaInterface row = transMeta.getPrevStepFields(stepname);
            if (row != null) {
                switch (tabFolder.getSelectionIndex()) {
                    case 0:
                        BaseStepDialog.getFieldsFromPrevious(row, mappingTable, 1, new int[]{1}, new int[]{},
                                                             -1, -1, null);
                        break;
                }
            }
        } catch (KettleException exception) {
            new ErrorDialog(shell, Messages.get("LoaderPluginDialog.FailedToGetFields.DialogTitle"),
                            Messages.get("LoaderPluginDialog.FailedToGetFields.DialogMessage"),
                            exception);
        }
    }
}
