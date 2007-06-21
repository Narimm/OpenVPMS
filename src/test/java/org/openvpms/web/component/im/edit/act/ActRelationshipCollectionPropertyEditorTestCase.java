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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
package org.openvpms.web.component.im.edit.act;

import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.web.component.im.edit.AbstractCollectionPropertyEditorTest;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.test.TestHelper;


/**
 * {@link ActRelationshipCollectionPropertyEditor} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ActRelationshipCollectionPropertyEditorTestCase
        extends AbstractCollectionPropertyEditorTest {

    /**
     * Tests {@link CollectionPropertyEditor#getArchetypeRange()}.
     */
    public void testGetArchetypeRange() {
        IMObject parent = createParent();
        CollectionProperty property = getCollectionProperty(parent);
        CollectionPropertyEditor editor = createEditor(property, parent);
        String[] range = editor.getArchetypeRange();
        assertEquals(1, range.length);
        assertEquals("act.customerEstimationItem", range[0]);
    }

    /**
     * Tests {@link ActRelationshipCollectionPropertyEditor#getRelationshipShortName()}.
     */
    public void testGetRelationshipShortName() {
        IMObject parent = createParent();
        CollectionProperty property = getCollectionProperty(parent);
        ActRelationshipCollectionPropertyEditor editor
                = (ActRelationshipCollectionPropertyEditor) createEditor(
                property, parent);
        assertEquals("actRelationship.customerEstimationItem",
                     editor.getRelationshipShortName());
    }

    /**
     * Returns the parent of the collection.
     *
     * @return the parent object
     */
    protected IMObject createParent() {
        Act act = (Act) TestHelper.create("act.customerEstimation");
        IMObject customer = TestHelper.createCustomer();
        Participation participation
                = TestHelper.createParticipation("participation.customer",
                                                 customer, act);
        act.addParticipation(participation);
        act.setStatus(FinancialActStatus.IN_PROGRESS);
        return act;
    }

    /**
     * Returns the name of the collection node.
     *
     * @return the node name
     */
    protected String getCollectionNode() {
        return "items";
    }

    /**
     * Returns an editor for a collection property.
     *
     * @param property the collection property
     * @param parent   the parent of the collection
     * @return a new editor for the property
     */
    protected CollectionPropertyEditor createEditor(
            CollectionProperty property, IMObject parent) {
        return new ActRelationshipCollectionPropertyEditor(
                property, (Act) parent);
    }

    /**
     * Returns an object to add to the collection.
     *
     * @return a new object to add to the collection
     */
    protected IMObject createObject() {
        Act act = (Act) TestHelper.create("act.customerEstimationItem");
        assertNotNull(act);

        IMObject product = TestHelper.createProduct();
        IMObject patient = TestHelper.createPatient();
        Participation patientParticipation
                = TestHelper.createParticipation("participation.patient",
                                                 patient, act);

        Participation productParticipation
                = TestHelper.createParticipation("participation.product",
                                                 product, act);
        act.addParticipation(patientParticipation);
        act.addParticipation(productParticipation);
        act.setStatus(FinancialActStatus.IN_PROGRESS);
        return act;
    }

}
