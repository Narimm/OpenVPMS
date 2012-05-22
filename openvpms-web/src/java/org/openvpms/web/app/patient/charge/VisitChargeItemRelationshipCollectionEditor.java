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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.app.customer.charge.ChargeItemRelationshipCollectionEditor;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.IMObjectListResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.property.CollectionProperty;

import java.util.List;

/**
 * A relationship collection editor for visit charges.
 *
 * @author Tim Anderson
 */
public class VisitChargeItemRelationshipCollectionEditor extends ChargeItemRelationshipCollectionEditor {

    /**
     * The patient to filter items on.
     */
    private final Party patient;

    /**
     * Constructs a {@code VisitChargeItemRelationshipCollectionEditor}.
     *
     * @param patient  the patient
     * @param property the collection property
     * @param act      the parent act
     * @param context  the layout context
     */
    public VisitChargeItemRelationshipCollectionEditor(Party patient, CollectionProperty property, Act act,
                                                       LayoutContext context) {
        super(property, act, context);
        this.patient = patient;
    }

    /**
     * Creates a new result set for display.
     *
     * @return a new result set
     */
    @Override
    protected ResultSet<IMObject> createResultSet() {
        CollectionPropertyEditor editor = getCollectionPropertyEditor();
        List<IMObject> objects = editor.getObjects();
        ResultSet<IMObject> set = new IMObjectListResultSet<IMObject>(objects, ROWS);
        set.sort(new SortConstraint[]{new NodeSortConstraint("startTime", false)});
        return set;
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context
     * @return an editor to edit <tt>object</tt>
     */
    @Override
    public IMObjectEditor createEditor(IMObject object, LayoutContext context) {
        IMObjectEditor editor = new VisitChargeItemEditor((Act) object, (Act) getObject(), context);
        initialiseEditor(editor);
        return editor;
    }
}
