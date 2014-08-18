/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.product.stock;

import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.archetype.rules.stock.StockUpdater;
import org.openvpms.archetype.rules.stock.io.StockData;
import org.openvpms.archetype.rules.stock.io.StockDataImporter;
import org.openvpms.archetype.rules.stock.io.StockDataSet;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.doc.DocumentUploadListener;
import org.openvpms.web.component.im.doc.UploadDialog;
import org.openvpms.web.component.im.edit.ActActions;
import org.openvpms.web.component.im.edit.DefaultActActions;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActEditDialog;
import org.openvpms.web.component.im.util.UserHelper;
import org.openvpms.web.component.retry.Retryable;
import org.openvpms.web.component.retry.Retryer;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workspace.ActCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;


/**
 * Stock CRUD window.
 *
 * @author Tim Anderson
 */
public class StockCRUDWindow extends ActCRUDWindow<Act> {

    /**
     * Export button identifier.
     */
    private static final String EXPORT_ID = "button.export";

    /**
     * Import button identifier.
     */
    private static final String IMPORT_ID = "button.import";

    /**
     * Constructs a {@code StockCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param context    the context
     * @param help       the help context
     */
    public StockCRUDWindow(Archetypes<Act> archetypes, Context context, HelpContext help) {
        super(archetypes, DefaultActActions.getInstance(), context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        super.layoutButtons(buttons);
        buttons.add(createPostButton());
        buttons.add(createPrintButton());
        boolean admin = UserHelper.isAdmin(getContext().getUser());
        if (admin) {
            buttons.add(ButtonFactory.create(EXPORT_ID, new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    onExport();
                }
            }));
            buttons.add(ButtonFactory.create(IMPORT_ID, new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    onImport();
                }
            }));
        }
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param buttons the button set
     * @param enable  determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(ButtonSet buttons, boolean enable) {
        boolean enableEdit = false;
        boolean enableDeletePost = false;
        if (enable) {
            Act object = getObject();
            ActActions<Act> actions = getActions();
            enableEdit = actions.canEdit(object);
            enableDeletePost = actions.canDelete(object);
        }
        buttons.setEnabled(EDIT_ID, enableEdit);
        buttons.setEnabled(DELETE_ID, enableDeletePost);
        buttons.setEnabled(POST_ID, enableDeletePost);
        buttons.setEnabled(PRINT_ID, enable);
    }

    /**
     * Creates a new edit dialog with Apply button disabled for <em>POSTED</em> acts, to workaround OVPMS-733.
     *
     * @param editor the editor
     */
    @Override
    protected EditDialog createEditDialog(IMObjectEditor editor) {
        return new ActEditDialog(editor, getContext());
    }

    /**
     * Updates stock when an <em>act.stockAdjust</em> or <em>act.stockTransfer</em> is posted.
     *
     * @param act the act
     */
    @Override
    protected void onPosted(final Act act) {
        Retryer.run(new Retryable() {
            public boolean run() {
                StockUpdater updater = new StockUpdater(ServiceHelper.getArchetypeService());
                updater.update(act);
                return true;
            }
        });
    }

    /**
     * Invoked when the "export" button is pressed.
     */
    private void onExport() {
        HelpContext help = getHelpContext().subtopic("export");
        StockExportDialog dialog = new StockExportDialog(createLayoutContext(help), help);
        dialog.show();
    }

    /**
     * Invoked when the "import" button is pressed.
     */
    private void onImport() {
        final HelpContext help = getHelpContext().subtopic("import");
        DocumentUploadListener listener = new DocumentUploadListener() {

            @Override
            protected void upload(Document document) {
                try {
                    importDocument(document, help);
                } catch (Throwable exception) {
                    ErrorHelper.show(exception);
                }
            }
        };
        UploadDialog dialog = new UploadDialog(listener, help.subtopic("upload"));
        dialog.show();
    }

    /**
     * Imports a document.
     *
     * @param document the document to import
     * @param help     the help context
     */
    private void importDocument(Document document, HelpContext help) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        StockDataImporter importer = new StockDataImporter(
                service, ServiceHelper.getBean(DocumentHandlers.class),
                StockIOHelper.getFieldSeparator(getContext().getPractice()));
        String reason = Messages.format("product.stock.import.reason", document.getName());
        NodeDescriptor node = DescriptorHelper.getNode(StockArchetypes.STOCK_ADJUST, "reason", service);
        int maxLength = node != null ? node.getMaxLength() : 255;
        if (maxLength > 0 && reason.length() > maxLength) {
            reason = reason.substring(0, maxLength);
        }
        StockDataSet data = importer.load(document, getContext().getUser(), reason);
        if (data.getAdjustment() != null) {
            onRefresh(data.getAdjustment());
        } else {
            if (!data.getErrors().isEmpty()) {
                List<StockData> errors = data.getErrors();
                StockImportErrorDialog dialog = new StockImportErrorDialog(errors, help.subtopic("errors"));
                dialog.show();
            } else {
                InformationDialog.show(Messages.get("product.stock.import.title"),
                                       Messages.get("product.stock.import.nochanges"));
            }
        }
    }

}
