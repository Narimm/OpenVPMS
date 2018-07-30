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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.estimate;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.RadioButton;
import nextapp.echo2.app.button.ButtonGroup;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.table.DefaultTableColumnModel;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.act.EstimateActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.web.component.edit.AlertListener;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.edit.act.TemplateProductListener;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ListResultSet;
import org.openvpms.web.component.im.table.DefaultDescriptorTableModel;
import org.openvpms.web.component.im.table.PagedIMTable;
import org.openvpms.web.echo.dialog.ModalDialog;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.charge.AbstractInvoicer;
import org.openvpms.web.workspace.customer.charge.ChargeItemRelationshipCollectionEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditDialog;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActEditor;
import org.openvpms.web.workspace.customer.charge.CustomerChargeActItemEditor;
import org.openvpms.web.workspace.patient.charge.TemplateChargeItems;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * Helper to create an invoice from an estimate.
 * <p>
 * The invoice is returned in a dialog to:
 * <ul>
 * <li>enable the user to make changes
 * <li>edit labels and reminders
 * </ul>
 *
 * @author Tim Anderson
 */
public class EstimateInvoicer extends AbstractInvoicer {

    /**
     * Creates an invoice for an estimate.
     * <p>
     * The editor is displayed in a visible dialog, in order for medication and reminder popups to be displayed
     * correctly.
     *
     * @param estimate the estimate to invoice
     * @param invoice  the invoice to add to, or {@code null} to create a new invoice
     * @param context  the layout context
     * @return an editor for the invoice, or {@code null} if the editor cannot be created
     * @throws OpenVPMSException for any error
     */
    public CustomerChargeActEditDialog invoice(Act estimate, FinancialAct invoice, LayoutContext context) {
        estimate.setStatus(EstimateActStatus.INVOICED);
        IMObjectBean estimateBean = new IMObjectBean(estimate);

        if (invoice == null) {
            invoice = createInvoice(estimateBean.getTargetRef("customer"));
        }

        CustomerChargeActEditor editor = createChargeEditor(invoice, context);

        // NOTE: need to display the dialog as the process of populating medications and reminders can display
        // popups which would parent themselves on the wrong window otherwise.
        ChargeDialog dialog = new ChargeDialog(editor, estimate, context.getContext());
        dialog.show();
        invoice(estimate, editor, context);
        return dialog;
    }

    /**
     * Invoices an estimate.
     *
     * @param estimate the estimate to invoice
     * @param editor   the editor to add invoice items to
     * @param context  the layout context
     */
    public void invoice(Act estimate, CustomerChargeActEditor editor, LayoutContext context) {
        InvoicingStrategy strategy = createStrategy(estimate, editor, context);
        strategy.invoice();
    }

    /**
     * Creates a strategy for invoicing an estimate.
     *
     * @param estimate the estimate to invoice
     * @param editor   the invoice editor
     * @param context  the layout context
     * @return a new strategy
     */
    protected InvoicingStrategy createStrategy(Act estimate, CustomerChargeActEditor editor, LayoutContext context) {
        return new InvoicingStrategy(estimate, editor, context);
    }

    /**
     * Strategy for invoicing estimates.
     */
    protected class InvoicingStrategy {

        /**
         * The estimate to invoice.
         */
        private final Act estimate;

        /**
         * The invoice editor.
         */
        private final CustomerChargeActEditor editor;

        /**
         * The layout context.
         */
        private final LayoutContext context;

        /**
         * Collects template products to their corresponding charge items.
         */
        private Map<Product, List<Act>> templateItems = new HashMap<>();

        /**
         * Constructs an {@link InvoicingStrategy}.
         *
         * @param estimate the estimate to invoice
         * @param editor   the editor to add invoice items to
         * @param context  the layout context
         */
        public InvoicingStrategy(Act estimate, CustomerChargeActEditor editor, LayoutContext context) {
            this.estimate = estimate;
            this.editor = editor;
            this.context = context;
        }

        /**
         * Invoices the estimate.
         */
        public void invoice() {
            IMObjectBean bean = new IMObjectBean(estimate);
            ActRelationshipCollectionEditor items = getItems();
            List<CustomerChargeActItemEditor> ratios = new ArrayList<>();

            int outOfStock = 0;
            for (Act estimationItem : bean.getTargets("items", Act.class)) {
                CustomerChargeActItemEditor itemEditor = invoice(estimationItem);
                if (itemEditor != null) {
                    BigDecimal stock = itemEditor.getStock();
                    if (stock != null && stock.compareTo(BigDecimal.ZERO) <= 0) {
                        outOfStock++;
                    }
                    BigDecimal available = itemEditor.getAvailableServiceRatio();
                    BigDecimal actual = itemEditor.getServiceRatio();
                    if (!ObjectUtils.equals(available, actual)) {
                        ratios.add(itemEditor);
                    }
                }
            }
            items.refresh();

            if (!templateItems.isEmpty()) {
                notifyTemplateExpansion(templateItems);
            }

            ActRelationshipCollectionEditor customerNotes = editor.getCustomerNotes();
            if (customerNotes != null) {
                for (Act note : bean.getTargets("customerNotes", Act.class)) {
                    IMObjectEditor noteEditor = customerNotes.getEditor(note);
                    noteEditor.getComponent();
                    customerNotes.addEdited(noteEditor);
                }
            }
            ActRelationshipCollectionEditor documents = editor.getDocuments();
            if (documents != null) {
                for (Act document : bean.getTargets("documents", Act.class)) {
                    IMObjectEditor documentsEditor = documents.getEditor(document);
                    documentsEditor.getComponent();
                    documents.addEdited(documentsEditor);
                }
            }
            if (!ratios.isEmpty()) {
                editor.getEditorQueue().queue(new ServiceRatioDialog(ratios, context));
            }
            if (outOfStock != 0) {
                AlertListener listener = editor.getAlertListener();
                if (listener != null) {
                    listener.onAlert(Messages.format("customer.charge.outofstock", outOfStock));
                }
            }
        }

        /**
         * Invoices an estimate item.
         *
         * @param item the estimate item
         * @return the item editor, or {@code null} if the item wasn't invoiced
         */
        protected CustomerChargeActItemEditor invoice(Act item) {
            ActBean itemBean = new ActBean(item);
            CustomerChargeActItemEditor itemEditor = getItemEditor(editor);
            itemEditor.setPatientRef(itemBean.getNodeParticipantRef("patient"));
            itemEditor.setPrint(itemBean.getBoolean("print", true));

            // NOTE: setting the product can trigger popups - want the popups to get the correct
            // property values from above
            itemEditor.setMinimumQuantity(itemBean.getBigDecimal("lowQty"));
            IMObjectReference templateRef = itemBean.getNodeParticipantRef("template");
            IMObjectReference product = itemBean.getNodeParticipantRef("product");
            setProduct(itemEditor, product, templateRef);
            itemEditor.setServiceRatio(itemBean.getBigDecimal("serviceRatio"));
            itemEditor.setQuantity(itemBean.getBigDecimal("highQty")); // replaces any doses

            itemEditor.setFixedPrice(itemBean.getBigDecimal("fixedPrice"));
            itemEditor.setUnitPrice(itemBean.getBigDecimal("highUnitPrice"));
            itemEditor.setDiscount(itemBean.getBigDecimal("highDiscount"));
            return itemEditor;
        }

        /**
         * Sets the product.
         *
         * @param itemEditor  the item editor
         * @param productRef  the product reference
         * @param templateRef the template reference. May be {@code null}
         */
        protected void setProduct(CustomerChargeActItemEditor itemEditor, IMObjectReference productRef,
                                  IMObjectReference templateRef) {
            itemEditor.setProduct(productRef, templateRef);
            Product template = itemEditor.getTemplate();
            if (template != null) {
                List<Act> items = templateItems.get(template);
                if (items == null) {
                    items = new ArrayList<>();
                    templateItems.put(template, items);
                }
                items.add(itemEditor.getObject());
            }
        }

        /**
         * Notifies that a one or more expanded templates were encountered when invoicing the estimate.
         *
         * @param items the map of items to their corresponding template products
         */
        protected void notifyTemplateExpansion(Map<Product, List<Act>> items) {
            List<TemplateChargeItems> templates = getItems().getTemplates();
            for (Map.Entry<Product, List<Act>> entry : items.entrySet()) {
                Product template = entry.getKey();
                List<Act> acts = entry.getValue();
                templates.add(new TemplateChargeItems(template, acts));
            }
            TemplateProductListener listener = getItems().getTemplateProductListener();
            if (listener != null) {
                for (Map.Entry<Product, List<Act>> entry : items.entrySet()) {
                    listener.expanded(entry.getKey());
                }
            }
        }

        /**
         * Returns the items collection editor.
         *
         * @return the items collection editor
         */
        protected ChargeItemRelationshipCollectionEditor getItems() {
            return editor.getItems();
        }
    }

    /**
     * Dialog to display charges where the service ratio from the estimate is different to that available.
     */
    private static class ServiceRatioDialog extends ModalDialog {

        /**
         * The editors.
         */
        private final List<CustomerChargeActItemEditor> editors;

        /**
         * The estimate service ratios.
         */
        private final List<BigDecimal> ratios = new ArrayList<>();

        /**
         * The table to display the differences.
         */
        private final PagedIMTable<Act> table;

        /**
         * The table model.
         */
        private final Model model;

        /**
         * Radio button to indicate that the estimate service ratios should be used.
         */
        private final RadioButton estimate;

        /**
         * Radio button to indicate that the current service ratios should be used.
         */
        private final RadioButton current;

        /**
         * Constructs a {@link ServiceRatioDialog}.
         *
         * @param editors the charge editors with different service ratios to those available
         * @param context the layout context
         */
        public ServiceRatioDialog(List<CustomerChargeActItemEditor> editors, LayoutContext context) {
            super(Messages.get("customer.estimate.serviceratio.title"), "MediumWidthHeightDialog", OK,
                  context.getHelpContext());
            this.editors = editors;
            Map<Act, CustomerChargeActItemEditor> map = new HashMap<>();
            for (CustomerChargeActItemEditor editor : editors) {
                ratios.add(editor.getServiceRatio()); // preserve the existing ratios
                map.put(editor.getObject(), editor);
            }

            List<Act> acts = new ArrayList<>(map.keySet());
            model = new Model(map, context);
            table = new PagedIMTable<>(model);
            table.setResultSet(new ListResultSet<>(acts, 25));

            ButtonGroup group = new ButtonGroup();
            estimate = ButtonFactory.create("customer.estimate.serviceratio.estimateprice", group,
                                            new ActionListener() {
                                                @Override
                                                public void onAction(ActionEvent event) {
                                                    useEstimateServiceRatios();
                                                }
                                            });
            current = ButtonFactory.create("customer.estimate.serviceratio.currentprice", group, new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    useCurrentRatios();
                }
            });
            enableOK();
        }

        /**
         * Lays out the component prior to display.
         * This implementation is a no-op.
         */
        @Override
        protected void doLayout() {
            Label content = LabelFactory.create(true, true);
            content.setText(Messages.get("customer.estimate.serviceratio.message"));
            Label prompt = LabelFactory.create("customer.estimate.serviceratio.prompt");
            Column buttons = ColumnFactory.create(Styles.CELL_SPACING, prompt, estimate, current);
            Column column = ColumnFactory.create(Styles.WIDE_CELL_SPACING, content, buttons, table.getComponent());
            getLayout().add(ColumnFactory.create(Styles.LARGE_INSET, column));
        }

        /**
         * Updates the charges with the original estimate service ratios.
         */
        private void useEstimateServiceRatios() {
            for (int i = 0; i < editors.size(); ++i) {
                CustomerChargeActItemEditor editor = editors.get(i);
                BigDecimal ratio = ratios.get(i);
                editor.setServiceRatio(ratio);
            }
            refresh();
        }

        /**
         * Updates the charges with the available service ratios.
         */
        private void useCurrentRatios() {
            for (CustomerChargeActItemEditor editor : editors) {
                editor.setServiceRatio(editor.getAvailableServiceRatio());
            }
            refresh();
        }

        /**
         * Refreshes the display.
         * <p>
         * This updates the table and enables/disables the OK button.
         */
        private void refresh() {
            model.fireTableDataChanged();
            enableOK();
        }

        /**
         * Enables the OK button if the estimate or current radio button is selected, otherwise disables it.
         */
        private void enableOK() {
            getButtons().setEnabled(OK_ID, estimate.isSelected() || current.isSelected());
        }

        private class Model extends DefaultDescriptorTableModel<Act> {

            /**
             * The charge item editors, keyed on their act.
             */
            private final Map<Act, CustomerChargeActItemEditor> editors;

            /**
             * The service ratio column index.
             */
            private final int ratioIndex;

            /**
             * The available service ratio column index.
             */
            private final int availableRatioIndex;

            /**
             * Constructs a {@link DefaultDescriptorTableModel}.
             *
             * @param context the layout context
             */
            public Model(Map<Act, CustomerChargeActItemEditor> editors, LayoutContext context) {
                super(CustomerAccountArchetypes.INVOICE_ITEM, context, "product", "quantity", "total");
                this.editors = editors;
                DefaultTableColumnModel model = (DefaultTableColumnModel) getColumnModel();
                ratioIndex = getNextModelIndex(model);
                availableRatioIndex = ratioIndex + 1;
                model.addColumn(createTableColumn(ratioIndex, "customer.estimate.serviceratio.current"));
                model.addColumn(createTableColumn(availableRatioIndex, "customer.estimate.serviceratio.new"));
            }

            /**
             * Creates a column model for one or more archetypes.
             * If there are multiple archetypes, the intersection of the descriptors
             * will be used.
             *
             * @param archetypes the archetypes
             * @param context    the layout context
             * @return a new column model
             */
            @Override
            protected TableColumnModel createColumnModel(List<ArchetypeDescriptor> archetypes, LayoutContext context) {
                return super.createColumnModel(archetypes, context);
            }

            /**
             * Returns the value found at the given coordinate within the table.
             *
             * @param object the object
             * @param column the table column
             * @param row    the table row
             */
            @Override
            protected Object getValue(Act object, TableColumn column, int row) {
                Object result;
                int index = column.getModelIndex();
                if (index == ratioIndex) {
                    CustomerChargeActItemEditor editor = editors.get(object);
                    BigDecimal ratio = editor.getServiceRatio();
                    result = (ratio != null) ? ratio : Messages.get("customer.estimate.serviceratio.none");
                } else if (index == availableRatioIndex) {
                    CustomerChargeActItemEditor editor = editors.get(object);
                    BigDecimal ratio = editor.getAvailableServiceRatio();
                    result = (ratio != null) ? ratio : Messages.get("customer.estimate.serviceratio.none");
                } else {
                    result = super.getValue(object, column, row);
                }
                return result;
            }

        }

    }
}
