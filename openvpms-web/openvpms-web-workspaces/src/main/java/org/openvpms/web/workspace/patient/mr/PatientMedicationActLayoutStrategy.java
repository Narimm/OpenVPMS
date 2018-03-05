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

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.WindowPaneEvent;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.prescription.PrescriptionRules;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.cache.IMObjectCache;
import org.openvpms.web.component.im.edit.EditDialog;
import org.openvpms.web.component.im.edit.EditDialogFactory;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.layout.ArchetypeNodes;
import org.openvpms.web.component.im.layout.ComponentGrid;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.layout.PrintObjectLayoutStrategy;
import org.openvpms.web.component.im.util.IMObjectCreator;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.im.view.ReadOnlyComponentFactory;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.PropertySet;
import org.openvpms.web.component.property.ReadOnlyProperty;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.dialog.ConfirmationDialog;
import org.openvpms.web.echo.dialog.PopupDialogListener;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.text.TextArea;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;

import java.util.List;


/**
 * Layout strategy that includes a 'Print Label' button to print the act.
 */
public class PatientMedicationActLayoutStrategy extends PrintObjectLayoutStrategy {

    /**
     * Determines if the product node should be displayed. False if the parent act has a product.
     * Ignored if {@code showProductReadOnly} is {@code true}
     */
    private boolean showProduct;

    /**
     * Determines if the product node should be displayed read-only.
     */
    private boolean showProductReadOnly;

    /**
     * Determines if the medication was dispensed from a prescription.
     * If so, then the quantity and label node should be displayed read-only.
     */
    private boolean prescription = false;

    /**
     * The prescriptions. If non-null, any prescription created via the medication will be added.
     */
    private Prescriptions prescriptions;

    /**
     * Button to create new prescriptions from the medication.
     */
    private Button newPrescription;

    /**
     * Factory for read-only components.
     */
    private ReadOnlyComponentFactory factory;

    /**
     * The product node.
     */
    static final String PRODUCT = "product";

    /**
     * The quantity node.
     */
    static final String QUANTITY = "quantity";

    /**
     * The notes node.
     */
    static final String LABEL = "label";


    /**
     * Constructs a {@link PatientMedicationActLayoutStrategy}.
     */
    public PatientMedicationActLayoutStrategy() {
        super("button.printlabel");
    }

    /**
     * Determines if the product should be displayed read-only.
     *
     * @param readOnly if {@code true} display the product read-only.
     */
    public void setProductReadOnly(boolean readOnly) {
        showProduct = true;
        showProductReadOnly = readOnly;
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
     * Registers the prescriptions.
     *
     * @param prescriptions the prescriptions. May be {@code null}
     */
    public void setPrescriptions(Prescriptions prescriptions) {
        this.prescriptions = prescriptions;
    }

    /**
     * Apply the layout strategy.
     * <p>
     * This renders an object in a {@code Component}, using a factory to create the child components.
     *
     * @param object     the object to apply
     * @param properties the object's properties
     * @param parent     the parent object. May be {@code null}
     * @param context    the layout context
     * @return the component containing the rendered {@code object}
     */
    @Override
    public ComponentState apply(final IMObject object, PropertySet properties, IMObject parent,
                                final LayoutContext context) {
        ComponentState result;

        newPrescription = ButtonFactory.create("button.newprescription", new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onNewPrescription((Act) object, context);
            }
        });

        ArchetypeNodes nodes = new ArchetypeNodes().exclude(LABEL);
        if (!showProduct) {
            nodes.exclude(PRODUCT);
        }
        setArchetypeNodes(nodes);

        try {
            if (!showProductReadOnly) {
                if (parent instanceof Act) {
                    ActBean bean = new ActBean((Act) parent);
                    showProduct = !bean.hasNode(PRODUCT);
                } else {
                    showProduct = true;
                }
            } else {
                addComponent(getReadOnlyComponent(properties.get(PRODUCT), parent, context));
            }

            Property label = properties.get(LABEL);
            if (prescription) {
                addComponent(getReadOnlyComponent(properties.get(QUANTITY), parent, context));
                label = new ReadOnlyProperty(label);
            }
            addComponent(createNotes(parent, label, context));
            result = super.apply(object, properties, parent, context);
        } finally {
            factory = null;
        }
        return result;
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
    protected ComponentGrid createGrid(IMObject object, List<Property> properties, LayoutContext context, int columns) {
        ComponentGrid grid = super.createGrid(object, properties, context, columns);
        grid.add(getComponent(LABEL), columns);
        return grid;
    }

    /**
     * Creates a component for a note node.
     *
     * @param property the property
     * @param object   the parent object
     * @param context  the layout context
     * @return a new component
     */
    protected ComponentState createNotes(IMObject object, Property property, LayoutContext context) {
        ComponentState notes = createComponent(property, object, context);
        Component component = notes.getComponent();
        if (component instanceof TextArea) {
            TextArea text = (TextArea) component;
            text.setWidth(Styles.FULL_WIDTH);
        }
        return notes;
    }

    /**
     * Adds the print button.
     * <p>
     * This implementation also adds a New Prescription button.
     *
     * @param set the button set
     */
    @Override
    protected void addButton(ButtonSet set) {
        super.addButton(set);
        set.add(newPrescription);
    }

    /**
     * Helper to return a read-only component. This uses an {@link ReadOnlyComponentFactory} rather than the default
     * factory as it renders differently (fields aren't greyed out).
     *
     * @param property the property
     * @param parent   the parent object
     * @param context  the layout context
     * @return a read-only component to display the property
     */
    private ComponentState getReadOnlyComponent(Property property, IMObject parent, LayoutContext context) {
        if (factory == null) {
            factory = new ReadOnlyComponentFactory(context);
        }
        return factory.create(property, parent);
    }

    /**
     * Invoked to create a new prescription.
     *
     * @param medication the medication act
     * @param context    the layout context
     */
    private void onNewPrescription(Act medication, final LayoutContext context) {
        final ActBean bean = new ActBean(medication);
        PrescriptionRules rules = ServiceHelper.getBean(PrescriptionRules.class);
        IMObjectCache cache = context.getCache();
        final Product product = (Product) cache.get(bean.getNodeParticipantRef("product"));
        final Party patient = (Party) cache.get(bean.getNodeParticipantRef("patient"));
        if (product != null && patient != null) {
            Act existing = rules.getPrescription(patient, product);
            if (existing != null) {
                int repeats = rules.getRemainingRepeats(existing);
                String title = Messages.get("patient.prescription.existing.title");
                String message = Messages.format("patient.prescription.existing.message", product.getName(), repeats);
                ConfirmationDialog.show(title, message, ConfirmationDialog.YES_NO, new PopupDialogListener() {
                    @Override
                    public void onYes() {
                        createPrescription(bean, patient, product, context);
                    }
                });
            } else {
                createPrescription(bean, patient, product, context);
            }
        }
    }

    /**
     * Creates a new prescription.
     *
     * @param medication the medication to copy from
     * @param patient    the patient
     * @param product    the product
     * @param context    the context
     */
    private void createPrescription(ActBean medication, Party patient, Product product, LayoutContext context) {
        final Act prescription = (Act) IMObjectCreator.create(PatientArchetypes.PRESCRIPTION);
        if (prescription != null) {
            ActBean bean = new ActBean(prescription);
            bean.setNodeParticipant("patient", patient);
            bean.setNodeParticipant("product", product);
            bean.setValue("quantity", medication.getValue("quantity"));
            bean.setValue("label", medication.getValue("label"));
            bean.setNodeParticipant("clinician", context.getContext().getClinician());
            IMObjectEditorFactory factory = ServiceHelper.getBean(IMObjectEditorFactory.class);
            final IMObjectEditor editor = factory.create(
                    prescription, null, new DefaultLayoutContext(context.getContext(), context.getHelpContext()));
            EditDialog dialog = EditDialogFactory.create(editor, context.getContext());
            dialog.addWindowPaneListener(new PopupDialogListener() {
                @Override
                public void onClose(WindowPaneEvent event) {
                    if (prescriptions != null && editor.isSaved()) {
                        // register the prescription so that it is availble for invoicing
                        prescriptions.add(prescription);
                    }
                    super.onClose(event);
                }
            });
            dialog.show();
        }
    }

}
