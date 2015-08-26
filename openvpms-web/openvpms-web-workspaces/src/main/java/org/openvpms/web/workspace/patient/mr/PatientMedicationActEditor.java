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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.mr;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.product.ProductRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.patient.PatientActEditor;
import org.openvpms.web.component.im.product.BatchParticipationEditor;
import org.openvpms.web.component.im.product.ProductParticipationEditor;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.SimpleProperty;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.charge.Quantity;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import static org.openvpms.web.workspace.patient.mr.PatientMedicationActLayoutStrategy.LABEL;
import static org.openvpms.web.workspace.patient.mr.PatientMedicationActLayoutStrategy.PRODUCT;
import static org.openvpms.web.workspace.patient.mr.PatientMedicationActLayoutStrategy.QUANTITY;


/**
 * Editor for <em>act.patientMedication</em> acts.
 *
 * @author Tim Anderson
 */
public class PatientMedicationActEditor extends PatientActEditor {

    /**
     * The medication quantity.
     */
    private Quantity quantity;

    /**
     * Dispensing units label.
     */
    private Label dispensingUnits;

    /**
     * Usage notes text.
     */
    private SimpleProperty usageNotes;

    /**
     * Determines if the product node should be displayed read-only.
     */
    private boolean showProductReadOnly = false;

    /**
     * Determines if the medication was dispensed from a prescription. If so, the quantity and label nodes should be
     * displayed read-only.
     */
    private boolean prescription = false;

    /**
     * The expiry date component. Cached as it needs to be disabled if a batch is selected.
     */
    private ComponentState expiryDate;

    /**
     * Component to display the usage notes.
     */
    private ComponentState usageComponent;

    /**
     * Listener for batch update events.
     */
    private final ModifiableListener batchListener;

    /**
     * Medication product usage notes node name.
     */
    private static final String USAGE_NOTES = "usageNotes";

    /**
     * Dispensing instructions node name.
     */
    private static final String INSTRUCTIONS = "dispInstructions";

    /**
     * Batch node name.
     */
    private static final String BATCH = "batch";

    /**
     * Constructs a {@link PatientMedicationActEditor}.
     *
     * @param act     the act to edit
     * @param parent  the parent act. May be {@code null}
     * @param context the layout context
     */
    public PatientMedicationActEditor(Act act, Act parent, LayoutContext context) {
        super(act, parent, context);
        if (!TypeHelper.isA(act, PatientArchetypes.PATIENT_MEDICATION)) {
            throw new IllegalArgumentException("Invalid act type:" + act.getArchetypeId().getShortName());
        }

        quantity = new Quantity(getProperty(QUANTITY), act, getLayoutContext());

        // create a property to hold the medication usage notes, if any.
        NodeDescriptor node = DescriptorHelper.getNode(ProductArchetypes.MEDICATION, USAGE_NOTES,
                                                       ServiceHelper.getArchetypeService());
        usageNotes = new SimpleProperty(node.getName(), null, String.class, node.getDisplayName());
        usageNotes.setMaxLength(node.getMaxLength());
        usageNotes.setReadOnly(true);

        dispensingUnits = LabelFactory.create();
        expiryDate = getLayoutContext().getComponentFactory().create(getProperty("endTime"), act);


        ActBean medBean = new ActBean(act);
        prescription = medBean.hasRelationship(PatientArchetypes.PRESCRIPTION_MEDICATION);

        batchListener = new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                onBatchChanged();
            }
        };

        boolean updated = false;
        if (parent != null) {
            // update the product from the parent if possible
            ActBean bean = new ActBean(parent);
            if (bean.hasNode(PRODUCT)) {
                Product product = (Product) getObject(bean.getNodeParticipantRef(PRODUCT));
                if (TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
                    updated = setProduct(product);
                    setQuantity(bean.getBigDecimal(QUANTITY));
                } else {
                    updated = setProduct(null);
                }
            }
        }
        if (!updated) {
            Product product = getProduct();
            updateDispensingUnits(product);
            updateUsageNotes(product);
        }
    }

    /**
     * Updates the product.
     *
     * @param product the product. May be {@code null}
     * @return {@code true} if the product was modified
     */
    public boolean setProduct(Product product) {
        boolean result = setParticipant(PRODUCT, product);
        if (result) {
            if (getProductEditor() == null) {
                productModified(product); // only invoke if the product participation changed
            }
        }
        return result;
    }

    /**
     * Returns the product.
     *
     * @return the product. May be {@code null}
     */
    public Product getProduct() {
        return (Product) getParticipant(PRODUCT);
    }

    /**
     * Determines if the product should be displayed read-only.
     *
     * @param readOnly if {@code true} display the product read-only.
     */
    public void setProductReadOnly(boolean readOnly) {
        showProductReadOnly = readOnly;
    }

    /**
     * Sets the quantity.
     *
     * @param quantity the quantity
     */
    public void setQuantity(BigDecimal quantity) {
        this.quantity.setValue(quantity, false);
    }

    /**
     * Sets the quantity.
     *
     * @param quantity the quantity
     */
    public void setQuantity(Quantity quantity) {
        this.quantity.setValue(quantity.getValue(), quantity.isDefault());
    }

    /**
     * Returns the quantity.
     *
     * @return the quantity
     */
    public BigDecimal getQuantity() {
        return quantity.getValue();
    }

    /**
     * Determines if the medication was dispensed from a prescription.
     * If {@code true}, then the quantity and label should be displayed read-only.
     *
     * @param prescription if {@code true} display the quantity and label read-only
     */
    public void setDispensedFromPrescription(boolean prescription) {
        this.prescription = prescription;
    }

    /**
     * Sets the dispensing instructions label.
     *
     * @param instructions the dispensing instructions
     */
    public void setLabel(String instructions) {
        Property label = getProperty(LABEL);
        label.setValue(instructions);
    }

    /**
     * Sets the stock location, used to constrain batch searches.
     *
     * @param stockLocation the stock location. May be {@code null}
     */
    public void setStockLocation(IMObjectReference stockLocation) {
        BatchParticipationEditor batchEditor = getBatchEditor();
        if (batchEditor != null) {
            batchEditor.setStockLocation(stockLocation);
        }
    }

    /**
     * Sets the batch.
     *
     * @param batch the batch. May be {@code null}
     */
    public void setBatch(Entity batch) {
        setParticipant(BATCH, batch);
    }

    /**
     * Returns the batch.
     *
     * @return the batch. May be {@code null}
     */
    public Entity getBatch() {
        return (Entity) getParticipant(BATCH);
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        PatientMedicationActLayoutStrategy strategy = new PatientMedicationActLayoutStrategy() {

            /**
             * Apply the layout strategy.
             * <p/>
             * This renders an object in a {@code Component}, using a factory to create the child components.
             *
             * @param object     the object to apply
             * @param properties the object's properties
             * @param parent     the parent object. May be {@code null}
             * @param context    the layout context
             * @return the component containing the rendered {@code object}
             */
            @Override
            public ComponentState apply(IMObject object, PropertySet properties, IMObject parent, LayoutContext context) {
                Row row = RowFactory.create(Styles.CELL_SPACING, quantity.getComponent(), dispensingUnits);
                addComponent(new ComponentState(row, quantity.getProperty()));
                return super.apply(object, properties, parent, context);
            }

            /**
             * Lays out components in a grid.
             *
             * @param object     the object to lay out
             * @param properties the properties
             * @param context    the layout context
             * @param columns    the no. of columns to use
             */
            @Override
            protected ComponentGrid createGrid(IMObject object, List<Property> properties, LayoutContext context,
                                               int columns) {
                ComponentGrid grid = super.createGrid(object, properties, context, columns);
                usageComponent = createNotes(object, usageNotes, context);
                Component label = ColumnFactory.create(usageComponent.getLabel());
                Component text = ColumnFactory.create(usageComponent.getComponent());
                text.setLayoutData(ComponentGrid.layout(1, columns * 2 - 1));
                usageComponent.setVisible(usageNotes.getValue() != null);
                grid.add(label, text);
                return grid;
            }
        };
        strategy.setProductReadOnly(showProductReadOnly);
        strategy.setDispensedFromPrescription(prescription);
        strategy.addComponent(expiryDate);
        return strategy;
    }

    /**
     * Invoked when layout has completed. This can be used to perform
     * processing that requires all editors to be created.
     */
    @Override
    protected void onLayoutCompleted() {
        final ProductParticipationEditor product = getProductEditor();
        if (product != null) {
            product.addModifiableListener(new ModifiableListener() {
                public void modified(Modifiable modifiable) {
                    productModified(product.getEntity());
                }
            });
        }
        updateBatch(getProduct());
        super.onLayoutCompleted();
    }

    /**
     * Invoked when the product is modified.
     *
     * @param product the product. May be {@code null}
     */
    protected void productModified(Product product) {
        if (product != null) {
            IMObjectBean bean = new IMObjectBean(product);
            if (bean.hasNode(INSTRUCTIONS)) {
                String dispInstructions = bean.getString(INSTRUCTIONS);
                setLabel(dispInstructions);
            }
        }
        updateDispensingUnits(product);
        updateUsageNotes(product);
        updateBatch(product);
    }

    /**
     * Updates the dispensing units label.
     *
     * @param product the product. May be {@code null}
     */
    private void updateDispensingUnits(Product product) {
        String units = "";
        if (TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
            units = LookupNameHelper.getName(product, "dispensingUnits");
        }
        dispensingUnits.setText(units);
    }

    /**
     * Updates the usage notes.
     *
     * @param product the product. May be {@code null}
     */
    private void updateUsageNotes(Product product) {
        String notes = "";
        if (TypeHelper.isA(product, ProductArchetypes.MEDICATION)) {
            IMObjectBean bean = new IMObjectBean(product);
            notes = bean.getString(USAGE_NOTES);
        }
        usageNotes.setValue(notes);
        if (usageComponent != null) {
            usageComponent.setVisible(notes != null);
        }
    }

    /**
     * Updates the batch.
     *
     * @param product the product. May be {@code null}
     */
    private void updateBatch(Product product) {
        BatchParticipationEditor editor = getBatchEditor();
        if (editor != null) {
            editor.removeModifiableListener(batchListener);
            try {
                editor.setProduct(product);
            } finally {
                editor.addModifiableListener(batchListener);
            }
            onBatchChanged();
        }
    }

    /**
     * Returns the product editor.
     *
     * @return the product editor, or {@code null} if none exists
     */
    private ProductParticipationEditor getProductEditor() {
        ParticipationEditor<Product> editor = getParticipationEditor(PRODUCT, false);
        return (ProductParticipationEditor) editor;
    }

    /**
     * Returns the product batch participation editor.
     *
     * @return the product batch participation, or {@code null} if none exists
     */
    protected BatchParticipationEditor getBatchEditor() {
        ParticipationEditor<Entity> editor = getParticipationEditor(BATCH, false);
        return (BatchParticipationEditor) editor;
    }

    /**
     * Invoked when the batch is changed. This updates the expiry date if a batch is selected, and disables
     * the field.
     */
    private void onBatchChanged() {
        Entity batch = getBatch();
        if (batch != null) {
            Date date = ServiceHelper.getBean(ProductRules.class).getBatchExpiry(batch);
            expiryDate.getProperty().setValue(date);
        }
        expiryDate.getComponent().setEnabled(batch == null);
    }

}
