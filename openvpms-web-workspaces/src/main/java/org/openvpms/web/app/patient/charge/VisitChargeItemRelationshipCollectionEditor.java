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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.patient.charge;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.app.customer.charge.ChargeItemRelationshipCollectionEditor;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.act.ActItemEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.property.CollectionProperty;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A relationship collection editor for visit charges.
 *
 * @author Tim Anderson
 */
public class VisitChargeItemRelationshipCollectionEditor extends ChargeItemRelationshipCollectionEditor {

    /**
     * The templates.
     */
    private List<TemplateChargeItems> templates = new ArrayList<TemplateChargeItems>();

    /**
     * Constructs a {@code VisitChargeItemRelationshipCollectionEditor}.
     *
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public VisitChargeItemRelationshipCollectionEditor(CollectionProperty property, Act act, LayoutContext context) {
        super(property, act, context);
    }

    /**
     * Returns the acts for the current patient.
     *
     * @return the patient's acts
     */
    public List<FinancialAct> getPatientActs() {
        CollectionPropertyEditor editor = getCollectionPropertyEditor();
        List<IMObject> objects = editor.getObjects();
        List<FinancialAct> acts = new ArrayList<FinancialAct>();
        Party patient = getPatient();
        if (patient != null) {
            IMObjectReference patientRef = patient.getObjectReference();
            for (IMObject object : objects) {
                FinancialAct act = (FinancialAct) object;
                ActBean bean = new ActBean(act);
                if (ObjectUtils.equals(patientRef, bean.getNodeParticipantRef("patient"))) {
                    acts.add(act);
                }
            }
        }
        return acts;
    }

    /**
     * Returns the set of acts being edited, including that of the {@link #getCurrentEditor()}.
     *
     * @return the set of acts being edited
     */
    public List<FinancialAct> getCurrentPatientActs() {
        Set<FinancialAct> result = new LinkedHashSet<FinancialAct>(getPatientActs());
        IMObjectEditor current = getCurrentEditor();
        if (current != null) {
            result.add((FinancialAct) current.getObject());
        }
        return new ArrayList<FinancialAct>(result);
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
     * Creates a new result set for display.
     *
     * @return a new result set
     */
    @Override
    @SuppressWarnings("unchecked")
    protected ResultSet<IMObject> createResultSet() {
        List acts = getPatientActs();
        ResultSet<IMObject> set = new IMObjectListResultSet<IMObject>(acts, ROWS);
        set.sort(new SortConstraint[]{new NodeSortConstraint("startTime", false)});
        return set;
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
