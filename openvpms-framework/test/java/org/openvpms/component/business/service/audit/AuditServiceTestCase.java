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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: AuditServiceTestCase.java 328 2005-12-07 13:31:09Z jalateras $
 */

package org.openvpms.component.business.service.audit;


import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.openvpms.component.business.domain.im.audit.AuditRecord;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.AbstractArchetypeServiceTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;

import java.util.List;

/**
 * Tests the {@link org.openvpms.component.business.service.audit.AuditService}.
 *
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate: 2005-12-08 00:31:09 +1100 (Thu, 08 Dec 2005) $
 */
@ContextConfiguration("audit-service-appcontext.xml")
public class AuditServiceTestCase extends AbstractArchetypeServiceTest {

    /**
     * The audit service
     */
    @Autowired
    private IAuditService audit;


    /**
     * Test that audit recrods are successfully created on save
     */
    @Test
    public void testAuditOnSave() {
        Party person = createPerson("MR", "Jim", "Alateras");
        save(person);
        List<AuditRecord> records = audit.getByObjectId(person.getArchetypeIdAsString(), person.getId());

        assertTrue("The size " + records.size() + " for " + person.getId(), (records.size() == 1));
    }

    /**
     * Test that audit records are successfully created on update
     */
    @Test
    public void testAuditOnUpdate() {
        Party person = createPerson("MR", "Jim", "Alateras");
        save(person);
        assertTrue(audit.getByObjectId(person.getArchetypeIdAsString(),
                                       person.getId()).size() == 1);

        person.getDetails().put("firstName", "James");
        save(person);
        assertTrue(audit.getByObjectId(person.getArchetypeIdAsString(),
                                       person.getId()).size() == 2);
    }

    /**
     * Test that audit records are successfull created on multiple updates
     */
    @Test
    public void testAuditOnMultipleUpdates() {
        Party person = createPerson("MR", "Jim", "Alateras");
        save(person);

        for (int index = 0; index < 5; index++) {
            person.getDetails().put("firstName",
                                    (String) person.getDetails().get("firstName") + index);
            save(person);
        }
        assertTrue(audit.getByObjectId(person.getArchetypeIdAsString(),
                                       person.getId()).size() == 6);
    }

    /**
     * Test that we can retrieve audit records by id
     */
    @Test
    public void testRetrievalById() {
        Party person = createPerson("MR", "Jim", "Alateras");
        save(person);
        save(person);
        save(person);
        List<AuditRecord> records = audit.getByObjectId(
                person.getArchetypeIdAsString(), person.getId());
        assertTrue(records.size() == 3);

        for (AuditRecord record : records) {
            assertTrue(audit.getById(record.getId()) != null);
        }
    }

    /**
     * Test that an audit record is generated for a delete
     */
    @Test
    public void testAuditOnDelete() {
        Party person = createPerson("MR", "Jim", "Alateras");
        save(person);
        remove(person);
        List<AuditRecord> records = audit.getByObjectId(
                person.getArchetypeIdAsString(), person.getId());
        assertTrue(records.size() == 2);
        for (AuditRecord record : records) {
            if (record.getOperation().equals("save") ||
                record.getOperation().equals("remove")) {
                // no opn
            } else {
                fail("Unexpected audit record. Operation must either be save or remove");
            }
        }
    }

    /**
     * Create a person
     *
     * @param title     the person's title
     * @param firstName the person's first name
     * @param lastName  the person's last name
     * @return Person
     */
    public Party createPerson(String title, String firstName, String lastName) {
        Party person = (Party) create("party.person");
        person.getDetails().put("lastName", lastName);
        person.getDetails().put("firstName", firstName);
        person.getDetails().put("title", title);

        return person;
    }

}
