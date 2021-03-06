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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.product;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.list.ListCellRenderer;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.product.ProductPriceRules;
import org.openvpms.archetype.rules.product.ProductRules;
import org.openvpms.archetype.rules.product.io.ProductCSVReader;
import org.openvpms.archetype.rules.product.io.ProductData;
import org.openvpms.archetype.rules.product.io.ProductDataFilter;
import org.openvpms.archetype.rules.product.io.ProductDataSet;
import org.openvpms.archetype.rules.product.io.ProductImporter;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.model.archetype.NodeDescriptor;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.smartflow.client.FlowSheetServiceFactory;
import org.openvpms.smartflow.client.InventoryService;
import org.openvpms.smartflow.client.SyncState;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.doc.DocumentUploadListener;
import org.openvpms.web.component.im.doc.UploadDialog;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.util.UserHelper;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.workspace.ResultSetCRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.InformationDialog;
import org.openvpms.web.echo.dialog.MessageDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.product.io.ProductExportDialog;
import org.openvpms.web.workspace.product.io.ProductImportDialog;
import org.openvpms.web.workspace.product.io.ProductImportErrorDialog;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;


/**
 * CRUD window for products.
 *
 * @author Tim Anderson
 */
public class ProductCRUDWindow extends ResultSetCRUDWindow<Product> {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The Smart Flow Sheet service factory.
     */
    private final FlowSheetServiceFactory flowSheetServiceFactory;

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
     * Synchronise with Smart Flow Sheet button identifier.
     */
    private static final String SYNCH_ID = "button.synchwithsfs";


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
        service = ServiceHelper.getArchetypeService();
        flowSheetServiceFactory = ServiceHelper.getBean(FlowSheetServiceFactory.class);
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
            Party location = getContext().getLocation();
            if (location != null && flowSheetServiceFactory.isSmartFlowSheetEnabled(location)) {
                buttons.add(ButtonFactory.create(SYNCH_ID, new ActionListener() {
                    @Override
                    public void onAction(ActionEvent event) {
                        onSynchronise();
                    }
                }));
            }
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
        final Product product = IMObjectHelper.reload(getObject());
        if (product != null) {
            String displayName = getArchetypeDescriptor().getDisplayName();
            String name = product.getName();
            if (StringUtils.isEmpty(name)) {
                name = displayName;
            }
            String title = Messages.format("product.information.copy.title", displayName);
            String message = Messages.format("product.information.copy.message", name);
            HelpContext help = getHelpContext().subtopic("copy");
            ConfirmationDialog.show(title, message, ConfirmationDialog.YES_NO, help, new PopupDialogListener() {
                @Override
                public void onYes() {
                    copy(product);
                }
            });
        }
    }

    /**
     * Invoked when the 'synchronise with SFS' button is pressed.
     */
    protected void onSynchronise() {
        HelpContext help = getHelpContext().subtopic("sync");
        ConfirmationDialog.show(Messages.get("product.information.sync.title"),
                                Messages.get("product.information.sync.message"),
                                ConfirmationDialog.YES_NO, help, new PopupDialogListener() {
                    @Override
                    public void onYes() {
                        synchroniseProducts();
                    }
                });
    }

    /**
     * Copy the product, and edit it.
     *
     * @param product the product to copy
     */
    private void copy(Product product) {
        try {
            ProductRules rules = ServiceHelper.getBean(ProductRules.class);
            String name = product.getName();
            IMObjectBean bean = service.getBean(product);
            int length = bean.getMaxLength("name");
            if (length <= 0) {
                length = NodeDescriptor.DEFAULT_MAX_LENGTH;
            }
            String copyName = Messages.format("product.copy.name", name);
            if (copyName.length() > length) {
                name = StringUtils.abbreviate(name, length - (copyName.length() - length));
                copyName = Messages.format("product.copy.name", name);
            }
            Product copy = rules.copy(product, copyName);

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

    /**
     * Synchronises products.
     */
    private void synchroniseProducts() {
        Party location = getContext().getLocation();
        Party stockLocation = getContext().getStockLocation();
        Party practice = getContext().getPractice();
        boolean useLocationProducts = (practice != null)
                                      && service.getBean(practice).getBoolean("useLocationProducts");
        InventoryService service = flowSheetServiceFactory.getInventoryService(location);
        SyncState sync = service.synchronise(useLocationProducts, location, stockLocation);
        String title = Messages.get("product.information.sync.title");
        String message = sync.changed() ? Messages.get("product.information.sync.updated")
                                        : Messages.get("product.information.sync.noupdate");
        InformationDialog.show(title, message);
    }

    /**
     * Invoked when the "export" button is pressed.
     */
    private void onExport() {
        HelpContext help = getHelpContext().subtopic("export");
        ProductExportDialog dialog = new ProductExportDialog(createLayoutContext(help), help);
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
        ProductCSVReader reader = new ProductCSVReader(ServiceHelper.getBean(DocumentHandlers.class),
                                                       ServiceHelper.getLookupService());
        List<SimpleDateFormat> formats = reader.getDateFormats(document);
        boolean ambiguousDates = false;
        if (formats.size() > 1) {
            List<SimpleDateFormat> dmy = getFormats(formats, ProductCSVReader.DAY_MONTH_YEAR_FORMATS);
            List<SimpleDateFormat> mdy = getFormats(formats, ProductCSVReader.MONTH_DAY_YEAR_FORMATS);
            List<SimpleDateFormat> ymd = getFormats(formats, ProductCSVReader.YEAR_MONTH_DAY_FORMATS);
            List<List<SimpleDateFormat>> formatList = new ArrayList<>();
            if (!dmy.isEmpty()) {
                formatList.add(dmy);
            }
            if (!mdy.isEmpty()) {
                formatList.add(mdy);
            }
            if (!ymd.isEmpty()) {
                formatList.add(ymd);
            }
            if (formatList.size() > 1) {
                ambiguousDates = true;
                selectDateFormatAndImport(document, formatList, reader, help);
            }
        }
        if (!ambiguousDates) {
            reader.setDateFormats(formats);
            importDocument(document, reader, help);
        }
    }

    /**
     * Imports a document.
     *
     * @param document the document to import
     * @param reader   the document reader
     * @param help     the help context
     */
    private void importDocument(Document document, ProductCSVReader reader, HelpContext help) {
        ProductDataSet data = reader.read(document);
        if (data.getErrors().isEmpty()) {
            ProductDataFilter filter = new ProductDataFilter(ServiceHelper.getBean(ProductPriceRules.class), service);
            data = filter.filter(data.getData());
        }
        if (data.getErrors().isEmpty()) {
            final List<ProductData> output = data.getData();
            if (!output.isEmpty()) {
                ProductImportDialog dialog = new ProductImportDialog(output, help);
                dialog.show();
                dialog.addWindowPaneListener(new PopupDialogListener() {
                    @Override
                    public void onOK() {
                        onImport(output);
                    }
                });
            } else {
                InformationDialog.show(Messages.get("product.import.title"),
                                       Messages.get("product.import.nochanges"));
            }
        } else {
            List<ProductData> errors = data.getErrors();
            ProductImportErrorDialog dialog = new ProductImportErrorDialog(errors, help.subtopic("errors"));
            dialog.show();
        }
    }

    /**
     * Prompts to select the date format, before importing the prices.
     * <p/>
     * This is used if multiple date formats can be used to interpret dates
     * (e.g. 1/3/2013 could be parsed using d/m/y or m/d/y).
     *
     * @param document   the document to import
     * @param formatList the possible date formats to parse dates
     * @param reader     the document reader
     * @param help       the help context
     */
    private void selectDateFormatAndImport(final Document document, final List<List<SimpleDateFormat>> formatList,
                                           final ProductCSVReader reader, final HelpContext help) {
        final DateFormatDialog dialog = new DateFormatDialog(formatList, help.subtopic("date"));
        dialog.addWindowPaneListener(new PopupDialogListener() {
            @Override
            public void onOK() {
                reader.setDateFormats(dialog.getDateFormats());
                importDocument(document, reader, help);
            }
        });
        dialog.show();
    }

    /**
     * Returns the matches that correspond to the supplied formats.
     * <p/>
     * This is used to organise formats into families (i.e. d/m/y, m/d/y, y/m/d).
     *
     * @param matches the matches that can be used to parse the dates
     * @param formats the formats to compare with
     * @return the matches that appear in formats
     */
    private List<SimpleDateFormat> getFormats(List<SimpleDateFormat> matches, SimpleDateFormat[] formats) {
        List<SimpleDateFormat> result = new ArrayList<>();
        for (SimpleDateFormat match : matches) {
            for (SimpleDateFormat format : formats) {
                if (match == format) {
                    result.add(match);
                }
            }
        }
        return result;
    }

    /**
     * Imports prices.
     *
     * @param data the data to import
     */
    private void onImport(List<ProductData> data) {
        ProductImporter importer = new ProductImporter(ServiceHelper.getBean(ProductPriceRules.class), service);
        importer.run(data);
        InformationDialog.show(Messages.get("product.import.title"), Messages.get("product.import.imported"));
    }

    static class DateFormatDialog extends MessageDialog {

        /**
         * The date format field.
         */
        private final SelectField field;

        /**
         * Constructs a {@link DateFormatDialog}.
         *
         * @param formats the date formats
         * @param help    the help context
         */
        public DateFormatDialog(List<List<SimpleDateFormat>> formats, HelpContext help) {
            super(Messages.get("product.import.dateformat.title"), Messages.get("product.import.dateformat.message"),
                  OK_CANCEL, help);
            field = SelectFieldFactory.create(formats);
            field.setCellRenderer(new ListCellRenderer() {
                @Override
                @SuppressWarnings("unchecked")
                public Object getListCellRendererComponent(Component list, Object value, int index) {
                    List<SimpleDateFormat> formats = (List<SimpleDateFormat>) value;
                    return formats.get(0).toPattern().toLowerCase();
                }
            });
        }

        /**
         * Returns the selected date formats.
         *
         * @return the date formats
         */
        @SuppressWarnings("unchecked")
        public List<SimpleDateFormat> getDateFormats() {
            return (List<SimpleDateFormat>) field.getSelectedItem();
        }

        /**
         * Lays out the component prior to display.
         */
        @Override
        protected void doLayout() {
            Label message = LabelFactory.create();
            message.setText(getMessage());
            Column column = ColumnFactory.create("WideCellSpacing", message, field);
            Row row = RowFactory.create("Inset.Large", column);
            getLayout().add(row);
        }
    }
}
