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
package org.openvpms.web.workspace.patient.charge;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActItemEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.workspace.customer.charge.ChargeItemRelationshipCollectionEditor;

import java.util.ArrayList;
import java.util.List;

/**
 * A relationship collection editor for visit charge items that only shows items for the current patient.
 *
 * @author Tim Anderson
 */
public class VisitChargeItemRelationshipCollectionEditor extends ChargeItemRelationshipCollectionEditor {

    /**
     * The templates.
     */
    private List<TemplateChargeItems> templates = new ArrayList<TemplateChargeItems>();

    /**
     * Constructs a {@link VisitChargeItemRelationshipCollectionEditor}.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public VisitChargeItemRelationshipCollectionEditor(CollectionProperty property, Act act, LayoutContext context) {
        super(property, act, context, new PatientCollectionResultSetFactory(context.getContext()));
    }

    /**
     * Returns the acts for the current patient.
     *
     * @return the patient's acts
     */
    public List<Act> getPatientActs() {
        return getActs(new PatientPredicate<Act>(getPatient()));
    }

    /**
     * Returns the set of acts being edited, including that of the {@link #getCurrentEditor()}.
     *
     * @return the set of acts being edited
     */
    public List<Act> getCurrentPatientActs() {
        return getCurrentActs(new PatientPredicate<Act>(getPatient()));
    }

    /**
     * Returns the templates that were expanded.
     *
     * @return the templates
     */
    public List<TemplateChargeItems> getTemplates() {
        return templates;
    }

    /**
     * Clears the templates.
     */
    public void clearTemplates() {
        templates.clear();
    }

    /**
     * Returns the current patient.
     *
     * @return the current patient. May be {@code null}
     */
    public Party getPatient() {
        return getContext().getContext().getPatient();
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context
     * @return an editor to edit {@code object}
     */
    @Override
    public IMObjectEditor createEditor(IMObject object, LayoutContext context) {
        VisitChargeItemEditor editor = new VisitChargeItemEditor((Act) object, (Act) getObject(), context);
        initialiseEditor(editor);
        editor.setProductListener(getProductListener());
        return editor;
    }

    /**
     * Copies an act item for each product referred to in its template.
     *
     * @param editor   the editor
     * @param template the product template
     * @return the acts generated from the template
     */
    @Override
    protected List<Act> createTemplateActs(ActItemEditor editor, Product template) {
        List<Act> acts = super.createTemplateActs(editor, template);
        if (!acts.isEmpty()) {
            TemplateChargeItems items = new TemplateChargeItems(template, acts);
            templates.add(items);
        }
        return acts;
    }

}
