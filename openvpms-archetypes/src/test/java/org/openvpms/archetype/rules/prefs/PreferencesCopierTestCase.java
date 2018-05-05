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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.prefs;

import org.junit.Test;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.functor.SequenceComparator;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests the {@link PreferencesCopier}.
 *
 * @author Tim Anderson
 */
public class PreferencesCopierTestCase extends ArchetypeServiceTest {

    /**
     * Tests preference copying.
     */
    @Test
    public void testCopy() {
        Entity user1 = TestHelper.createUser();
        Entity root1 = (Entity) create(PreferenceArchetypes.PREFERENCES);
        Entity group1a = (Entity) create(PreferenceArchetypes.GENERAL);
        Entity group1b = (Entity) create(PreferenceArchetypes.SUMMARY);

        IMObjectBean bean = new IMObjectBean(root1);
        bean.addNodeTarget("groups", group1a);
        bean.addNodeTarget("groups", group1b);
        bean.addNodeTarget("user", user1);

        IMObjectBean group1aBean = new IMObjectBean(group1a);
        group1aBean.setValue("homePage", "workflow.scheduling");
        group1aBean.setValue("customerHistory", "not copied"); // hidden nodes not copied
        group1aBean.setValue("patientHistory", "not copied");

        IMObjectBean group1bBean = new IMObjectBean(group1b);
        group1bBean.setValue("showReferral", "ALWAYS");

        save(root1, group1a, group1b);

        Entity user2 = TestHelper.createUser();
        List<IMObject> objects = PreferencesCopier.copy(root1, user2.getObjectReference(), getArchetypeService());
        assertEquals(3, objects.size());

        Entity root2 = (Entity) get(objects, PreferenceArchetypes.PREFERENCES);
        Entity group2a = (Entity) get(objects, PreferenceArchetypes.GENERAL);
        Entity group2b = (Entity) get(objects, PreferenceArchetypes.SUMMARY);

        assertNotEquals(root2.getId(), root1.getId());
        assertNotEquals(group2a.getId(), group1a.getId());
        assertNotEquals(group2b.getId(), group1b.getId());
        save(objects);

        // check links
        IMObjectBean bean2 = new IMObjectBean(root2);
        List<IMObject> groups = bean2.getNodeTargetObjects("groups", SequenceComparator.INSTANCE);
        assertEquals(2, groups.size());
        assertEquals(group2a, groups.get(0));
        assertEquals(group2b, groups.get(1));
        assertEquals(user2, bean2.getNodeTargetObject("user"));

        IMObjectBean group2aBean = new IMObjectBean(group2a);
        assertEquals("workflow.scheduling", group2aBean.getString("homePage"));
        assertNull(group2aBean.getString("customerHistory"));
        assertNull(group2aBean.getString("patientHistory"));

        IMObjectBean group2bBean = new IMObjectBean(group2b);
        assertEquals("ALWAYS", group2bBean.getString("showReferral"));
    }

    /**
     * Returns an object given its archetype.
     *
     * @param objects   the objects to search
     * @param shortName the object archetype short name
     * @return the corresponding object
     * @throws IllegalStateException if the object can't be found
     */
    private IMObject get(List<IMObject> objects, String shortName) {
        for (IMObject object : objects) {
            if (TypeHelper.isA(object, shortName)) {
                return object;
            }
        }
        throw new IllegalStateException("Failed to find object with short name " + shortName);
    }

}
