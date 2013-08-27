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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.product;

import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.archetype.rules.product.ProductRules;
import org.openvpms.archetype.rules.product.io.FilterResult;
import org.openvpms.archetype.rules.product.io.ProductCSVReader;
import org.openvpms.archetype.rules.product.io.ProductData;
import org.openvpms.archetype.rules.product.io.ProductDataFilter;
import org.openvpms.archetype.rules.product.io.ProductImporter;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.doc.DocumentUploadListener;
import org.openvpms.web.component.im.doc.UploadDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.UserHelper;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workspace.ResultSetCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.product.io.ProductExportDialog;
import org.openvpms.web.workspace.product.io.ProductImportDialog;
import org.openvpms.web.workspace.product.io.ProductImportErrorDialog;

import java.util.List;


/**
 * CRUD window for products.
 *
 * @author Tim Anderson
 */
public class ProductCRUDWindow extends ResultSetCRUDWindow<Product> {

    /**
     * Copy button identifier.
     */
    private static final String COPY_ID = "copy";

    /**
     * Export button identifier.
     */
    private static final String EXPORT_ID = "button.export";

    /**
     * Import button identifier.
     */
    private static final String IMPORT_ID = "button.import";


    /**
     * Constructs a {@code ProductCRUDWindow}.
     *
     * @param archetypes the archetypes that this may create
     * @param query      the query. May be {@code null}
     * @param set        the result set. May be {@code null}
     * @param context    the context the context
     * @param help       the help context
     */
    public ProductCRUDWindow(Archetypes<Product> archetypes, Query<Product> query, ResultSet<Product> set,
                             Context context, HelpContext help) {
        super(archetypes, query, set, context, help);
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(ButtonSet buttons) {
        buttons.add(createNewButton());
        buttons.add(createViewButton());
        // If the logged in user is an admin, show the copy, edit, delete, import and export buttons
        boolean admin = UserHelper.isAdmin(getContext().getUser());
        if (admin) {
            buttons.add(createEditButton());
            buttons.add(createDeleteButton());
            buttons.add(ButtonFactory.create(COPY_ID, new ActionListener() {
                public void onAction(ActionEvent event) {
                    onCopy();
                }
            }));
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
        buttons.setEnabled(VIEW_ID, getResultSet() != null && enable);
        buttons.setEnabled(EDIT_ID, enable);
        buttons.setEnabled(DELETE_ID, enable);
        buttons.setEnabled(COPY_ID, enable);
    }

    /**
     * Invoked when the 'copy' button is pressed.
     */
    protected void onCopy() {
        final Product product = getObject();
        if (product != null) {
            String displayName = getArchetypeDescriptor().getDisplayName();
            String name = product.getName();
            if (StringUtils.isEmpty(name)) {
                name = displayName;
            }
            String title = Messages.format("product.information.copy.title", displayName);
            String message = Messages.format("product.information.copy.message", name);
            HelpContext help = getHelpContext().subtopic("copy");
            final ConfirmationDialog dialog = new ConfirmationDialog(title, message, help);
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onOK() {
                    copy(product);
                }
            });
            dialog.show();
        }
    }

    /**
     * Copy the product, and edit it.
     *
     * @param product the product to copy
     */
    private void copy(Product product) {
        try {
            ProductRules rules = new ProductRules();
            String name = Messages.format("product.copy.name", product.getName());
            Product copy = rules.copy(product, name);

            // NOTE: can't use the parent edit(IMObject) method as it relies on the object being edited
            // being in the current result set.
            HelpContext edit = createEditTopic(product);
            LayoutContext context = createLayoutContext(edit);
            IMObjectEditor editor = createEditor(copy, context);
            edit(editor);
        } catch (OpenVPMSException exception) {
            String title = Messages.format("product.information.copy.failed", getArchetypeDescriptor().getDisplayName());
            ErrorHelper.show(title, exception);
        }
    }

    private void onExport() {
        HelpContext help = getHelpContext();
        ProductExportDialog dialog = new ProductExportDialog(createLayoutContext(help), help);
        dialog.show();
    }


    private void onImport() {
        DocumentUploadListener listener = new DocumentUploadListener() {

            @Override
            protected void upload(Document document) {
                ProductCSVReader importer = new ProductCSVReader(ServiceHelper.getBean(DocumentHandlers.class));
                try {
                    List<ProductData> data = importer.read(document);
                    ProductDataFilter filter = new ProductDataFilter(ServiceHelper.getArchetypeService(),
                                                                     ServiceHelper.getBean(ProductPriceRules.class));
                    FilterResult result = filter.filter(data);
                    if (result.getErrors().isEmpty()) {
                        final List<ProductData> output = result.getData();
                        if (!output.isEmpty()) {
                            ProductImportDialog dialog = new ProductImportDialog(output);
                            dialog.show();
                            dialog.addWindowPaneListener(new PopupDialogListener() {
                                @Override
                                public void onOK() {
                                    onImport(output);
                                }
                            });
                        } else {
                            InformationDialog.show(Messages.get("product.io.import.title"),
                                                   Messages.get("product.io.import.nochanges"));
                        }
                    } else {
                        List<ProductData> errors = result.getErrors();
                        ProductImportErrorDialog dialog = new ProductImportErrorDialog(errors);
                        dialog.show();
                    }
                } catch (Throwable exception) {
                    ErrorHelper.show(exception);
                }
            }
        };
        UploadDialog dialog = new UploadDialog(listener, getHelpContext());
        dialog.show();
    }

    private void onImport(List<ProductData> data) {
        ProductImporter importer = new ProductImporter(ServiceHelper.getArchetypeService(),
                                                       ServiceHelper.getBean(ProductPriceRules.class));
        importer.run(data, getContext().getPractice());
        InformationDialog.show(Messages.get("product.io.import.title"), Messages.get("product.io.import.imported"));
    }
}
