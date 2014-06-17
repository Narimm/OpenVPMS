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

package org.openvpms.web.workspace.supplier.vet;

import org.junit.Test;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.query.ResultSetIterator;
import org.openvpms.web.test.AbstractAppTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link VetResultSet} class.
 *
 * @author Tim Anderson
 */
public class VetResultSetTestCase extends AbstractAppTest {

    /**
     * Tests simple iteration.
     */
    @Test
    public void testResultSet() {
        Party practice = (Party) create(SupplierArchetypes.SUPPLIER_VET_PRACTICE);
        practice.setName("ZVetPractice");
        Party vet1 = createVet("ZC", practice);
        Party vet2 = createVet("ZA", practice);
        Party vet3 = createVet("ZB", practice);

        SortConstraint[] sort = {Constraints.sort("name")};
        VetResultSet set = new VetResultSet(Constraints.shortName(SupplierArchetypes.SUPPLIER_VET), "*", false, sort,
                                            20, true);
        checkOrder(set, vet2, vet3, vet1);
    }

    /**
     * Verifies that a result set returns objects in the expected order.
     *
     * @param resultSet the result set
     * @param expected  the expected items, in order
     */
    private void checkOrder(VetResultSet resultSet, Party... expected) {
        int index = 0;
        ResultSetIterator<ObjectSet> iterator = new ResultSetIterator<ObjectSet>(resultSet);
        assertTrue(iterator.hasNext());
        while (iterator.hasNext()) {
            ObjectSet set = iterator.next();
            Party vet = expected[index];
            if (vet.getObjectReference().equals(set.getReference("entity.reference"))) {
                assertEquals(vet.getName(), set.getString("entity.name"));
                assertEquals(vet.getDescription(), set.getString("entity.description"));
                EntityBean bean = new EntityBean(vet);
                Entity practice = bean.getNodeSourceEntity("practices");
                assertNotNull(practice);
                assertEquals(practice.getName(), set.getString("practice.name"));
                index++;
                if (index == expected.length) {
                    break;
                }
            }
        }
        assertEquals(expected.length, index);
    }

    /**
     * Creates a vet linked to a practice.
     *
     * @param name     the vet last name
     * @param practice the practice
     * @return a new vet
     */
    private Party createVet(String name, Party practice) {
        Party vet = (Party) create(SupplierArchetypes.SUPPLIER_VET);
        IMObjectBean bean = new IMObjectBean(vet);
        bean.setValue("firstName", "A");
        bean.setValue("lastName", name);
        bean.setValue("title", TestHelper.getLookup("lookup.personTitle", "MR").getCode());
        EntityBean practiceBean = new EntityBean(practice);
        practiceBean.addNodeTarget("veterinarians", vet);
        save(vet, practice);
        return vet;
    }


}
