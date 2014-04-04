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
package org.openvpms.web.workspace.patient.history;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.im.edit.AbstractIMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.act.SingleParticipationCollectionEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.product.ProductParticipationEditor;
import org.openvpms.web.component.im.product.ProductQuery;
import org.openvpms.web.component.im.product.ProductResultSet;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.workspace.patient.mr.PatientDocumentActEditor;
import org.openvpms.web.workspace.patient.mr.PatientInvestigationActLayoutStrategy;


/**
 * An editor for <em>act.patientInvestigation</em> acts.
 *
 * @author Tim Anderson
 */
public class PatientInvestigationActEditor extends PatientDocumentActEditor {

    /**
     * Flag to indicate if the Print Form button should be enabled or disabled.
     */
    private boolean enableButton;

    /**
     * The product participation editor.
     */
    private final SingleParticipationCollectionEditor productEditor;

    /**
     * Constructs an {@link PatientInvestigationActEditor}.
     *
     * @param act     the act
     * @param parent  the parent act. May be {@code null}
     * @param context the layout context
     */
    public PatientInvestigationActEditor(DocumentAct act, Act parent, LayoutContext context) {
        super(act, parent, context);
        productEditor = new SingleParticipationCollectionEditor(getCollectionProperty("product"), act, context) {
            @Override
            protected IMObjectEditor createEditor(IMObject object, LayoutContext context) {
                return new ProductParticipationEditor((Participation) object, (Act) getObject(), context) {
                    @Override
                    protected IMObjectReferenceEditor<Product> createEntityEditor(Property property) {
                        return new ProductInvestigationTypeReferenceEditor(property, getObject(), getContext());
                    }
                };
            }
        };
        getEditors().add(productEditor);
    }

    /**
     * Updates the investigation type, if it is not the same as the existing one.
     * On update, the description will be set to the description of the investigation type.
     *
     * @param investigationType the investigation type. May be {@code null}
     */
    public void setInvestigationType(Entity investigationType) {
        IMObjectReference current = getParticipantRef("investigationType");
        if (!ObjectUtils.equals(current, investigationType)) {
            setParticipant("investigationType", investigationType);
            if (investigationType != null) {
                Property description = getProperty("description");
                description.setValue(investigationType.getDescription());
            }
        }
    }

    /**
     * Sets the product.
     *
     * @param product the product. May be {@code null}
     */
    public void setProduct(Product product) {
        setParticipant("product", product);
    }

    /**
     * Save any edits.
     *
     * @return {@code true} if the save was successful
     */
    @Override
    public boolean save() {
        boolean isNew = getObject().isNew();
        boolean saved = super.save();
        if (saved && isNew) {
            // enable printing of the form if the act has been saved and was previously unsaved
            enableButton = true; // getObject().isNew() will be true until transaction commits, so need this flag
            onLayout();
        }
        return saved;
    }

    /**
     * Returns the investigation type.
     *
     * @return the investigation type. May be {@code null}
     */
    public Entity getInvestigationType() {
        return (Entity) getParticipant("investigationType");
    }

    /**
     * Returns the investigation type reference.
     *
     * @return the investigation type reference. May be {@code null}
     */
    public IMObjectReference getInvestigationTypeRef() {
        return getParticipantRef("investigationType");
    }

    /**
     * Sets the patient.
     *
     * @param patient the patient reference. May be {@code null}
     */
    public void setPatient(IMObjectReference patient) {
        setParticipant("patient", patient);
    }

    /**
     * Sets the clinician.
     *
     * @param clinician the clinician reference. May be {@code null}.
     */
    public void setClinician(IMObjectReference clinician) {
        setParticipant("clinician", clinician);
    }

    /**
     * Determines if an editor should be disposed on layout change.
     *
     * @param editor the editor
     * @return {@code true} if the editor should be disposed
     */
    @Override
    protected boolean disposeOnChangeLayout(Editor editor) {
        return editor != getDocumentEditor() && editor != getVersionsEditor() && editor != productEditor;
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        PatientInvestigationActLayoutStrategy strategy = new PatientInvestigationActLayoutStrategy(getDocumentEditor(),
                                                                                                   getVersionsEditor(),
                                                                                                   productEditor);
        if (isProductReadOnly()) {
            strategy.setShowProductReadOnly(true);
        }
        strategy.setEnableButton(enablePrintForm());
        return strategy;
    }


    /**
     * Determines if the product should be read-only.
     *
     * @return {@code true} if the parent act is an invoice item, or the investigation is linked to an invoice item
     */
    private boolean isProductReadOnly() {
        boolean result;
        if (TypeHelper.isA(getParent(), CustomerAccountArchetypes.INVOICE_ITEM)) {
            result = true;
        } else {
            ActBean bean = new ActBean((Act) getObject());
            result = (bean.getRelationship("actRelationship.invoiceItemInvestigation") != null);
        }
        return result;
    }

    /**
     * Determines if the Print Form button should be displayed.
     * <p/>
     * Note that getObject().isNew() returns true until the transaction commits
     *
     * @return {@code true} if it should be displayed, otherwise {@code false}
     */
    private boolean enablePrintForm() {
        return enableButton || !getObject().isNew();
    }

    /**
     * A product reference editor that constrains products to those linked to the investigation type.
     */
    private class ProductInvestigationTypeReferenceEditor extends AbstractIMObjectReferenceEditor<Product> {

        /**
         * Constructs a {@link ProductInvestigationTypeReferenceEditor}.
         *
         * @param property the reference property
         * @param parent   the parent object. May be {@code null}
         * @param context  the layout context
         */
        public ProductInvestigationTypeReferenceEditor(Property property, IMObject parent, LayoutContext context) {
            super(property, parent, context);
        }

        /**
         * Creates a query to select objects.
         *
         * @param name the name to filter on. May be {@code null}
         * @return a new query
         * @throws ArchetypeQueryException if the short names don't match any archetypes
         */
        @Override
        protected Query<Product> createQuery(String name) {
            ProductQuery query = new ProductQuery(getProperty().getArchetypeRange(), getLayoutContext().getContext()) {
                @Override
                protected ResultSet<Product> createResultSet(SortConstraint[] sort) {
                    return new ProductInvestigationTypeResultSet(getArchetypeConstraint(), getValue(),
                                                                 isIdentitySearch(), getSpecies(), getStockLocation(),
                                                                 sort, getMaxResults());
                }
            };
            query.setValue(name);
            query.setAuto(true);
            return query;
        }
    }

    /**
     * A product result set that restricts products to the selected investigation type.
     */
    private class ProductInvestigationTypeResultSet extends ProductResultSet {


        /**
         * Constructs a {@link ProductInvestigationTypeResultSet}.
         *
         * @param archetypes       the archetypes to query
         * @param value            the value to query on. May be {@code null}
         * @param searchIdentities if {@code true} search on identity name
         * @param sort             the sort criteria. May be {@code null}
         * @param rows             the maximum no. of rows per page
         */
        public ProductInvestigationTypeResultSet(ShortNameConstraint archetypes, String value, boolean searchIdentities,
                                                 String species, Party stockLocation, SortConstraint[] sort, int rows) {
            super(archetypes, value, searchIdentities, species, stockLocation, sort, rows);
        }

        /**
         * Creates a new archetype query.
         *
         * @return a new archetype query
         */
        @Override
        protected ArchetypeQuery createQuery() {
            ArchetypeQuery query = super.createQuery();

            IMObjectReference ref = getInvestigationTypeRef();
            if (ref != null) {
                query.add(Constraints.join("investigationTypes").add(Constraints.eq("target", ref)));
            }
            return query;
        }
    }
}
