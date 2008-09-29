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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.practice;

import org.openvpms.archetype.rules.finance.deposit.DepositTestHelper;
import org.openvpms.archetype.rules.util.EntityRelationshipHelper;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.EntityBean;


/**
 * Tests the {@link LocationRules} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LocationRulesTestCase extends ArchetypeServiceTest {

    /**
     * The rules.
     */
    private LocationRules rules;


    /**
     * Tests the {@link LocationRules#getDefaultDepositAccount} method.
     */
    public void testGetDefaultDepositAccount() {
        Party location = TestHelper.createLocation();
        assertNull(rules.getDefaultDepositAccount(location));

        Party account1 = DepositTestHelper.createDepositAccount();
        Party account2 = DepositTestHelper.createDepositAccount();
        EntityBean bean = new EntityBean(location);
        bean.addRelationship("entityRelationship.locationDeposit", account1);

        assertEquals(account1, rules.getDefaultDepositAccount(location));

        EntityRelationship rel2 = bean.addRelationship(
                "entityRelationship.locationDeposit", account2);
        EntityRelationshipHelper.setDefault(location, "depositAccounts", rel2,
                                            getArchetypeService());

        assertEquals(account2, rules.getDefaultDepositAccount(location));
    }

    /**
     * Tests the {@link LocationRules#getDefaultTill} method.
     */
    public void testGetDefaultTill() {
        Party location = TestHelper.createLocation();
        assertNull(rules.getDefaultTill(location));

        Party till1 = createTill();
        Party till2 = createTill();
        EntityBean bean = new EntityBean(location);
        bean.addRelationship("entityRelationship.locationTill", till1);

        assertEquals(till1, rules.getDefaultTill(location));

        EntityRelationship rel2 = bean.addRelationship(
                "entityRelationship.locationTill", till2);
        EntityRelationshipHelper.setDefault(location, "tills", rel2,
                                            getArchetypeService());

        assertEquals(till2, rules.getDefaultTill(location));
    }

    /**
     * Tests the {@link LocationRules#getDefaultScheduleView} method.
     */
    public void testGetDefaultScheduleView() {
        Party location = TestHelper.createLocation();
        assertNull(rules.getDefaultScheduleView(location));

        Party schedule1 = ScheduleTestHelper.createSchedule();
        Party schedule2 = ScheduleTestHelper.createSchedule();
        Entity view1 = ScheduleTestHelper.createScheduleView(schedule1);
        Entity view2 = ScheduleTestHelper.createScheduleView(schedule1,
                                                             schedule2);
        EntityBean bean = new EntityBean(location);
        bean.addRelationship("entityRelationship.locationView", view1);

        assertEquals(view1, rules.getDefaultScheduleView(location));

        EntityRelationship rel2 = bean.addRelationship(
                "entityRelationship.locationView", view2);
        EntityRelationshipHelper.setDefault(location, "scheduleViews", rel2,
                                            getArchetypeService());

        assertEquals(view2, rules.getDefaultScheduleView(location));
    }

    /**
     * Tests the {@link LocationRules#getDefaultWorkListView(Party)} method.
     */
    public void testGetDefaultWorkListView() {
        Party location = TestHelper.createLocation();
        assertNull(rules.getDefaultWorkListView(location));

        Party worklist1 = ScheduleTestHelper.createWorkList();
        Party worklist2 = ScheduleTestHelper.createWorkList();
        Entity view1 = ScheduleTestHelper.createWorkListView(worklist1);
        Entity view2 = ScheduleTestHelper.createWorkListView(worklist1,
                                                             worklist2);
        EntityBean bean = new EntityBean(location);
        bean.addRelationship("entityRelationship.locationWorkListView", view1);

        assertEquals(view1, rules.getDefaultWorkListView(location));

        EntityRelationship rel2 = bean.addRelationship(
                "entityRelationship.locationWorkListView", view2);
        EntityRelationshipHelper.setDefault(location, "workListViews", rel2,
                                            getArchetypeService());

        assertEquals(view2, rules.getDefaultWorkListView(location));
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        rules = new LocationRules();
    }

    /**
     * Helper to create a till.
     *
     * @return a new till
     */
    private Party createTill() {
        Party party = (Party) create("party.organisationTill");
        party.setName("ZTill" + System.currentTimeMillis());
        save(party);
        return party;
    }
}
