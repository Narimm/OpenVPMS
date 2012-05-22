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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.customer.charge.AbstractCustomerChargeActEditor;
import org.openvpms.web.component.im.edit.act.ActRelationshipCollectionEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.CollectionProperty;

/**
 * Visit charge editor.
 *
 * @author Tim Anderson
 */
public class VisitChargeEditor extends AbstractCustomerChargeActEditor {

    /**
     * The patient.
     */
    private final Party patient;


    /**
     * Constructs a {@code VisitChargeActEditor}.
     *
     * @param act     the act to edit
     * @param context the layout context
     */
    public VisitChargeEditor(Party patient, FinancialAct act, LayoutContext context) {
        super(act, null, context);
        this.patient = patient;
    }

    /**
     * Creates a collection editor for the items collection.
     *
     * @param act   the act
     * @param items the items collection
     * @return a new collection editor
     */
    @Override
    protected ActRelationshipCollectionEditor createItemsEditor(Act act, CollectionProperty items) {
        return new VisitChargeItemRelationshipCollectionEditor(patient, items, act, getLayoutContext());
    }
}
