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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.archetype.rules.doc;

import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.ActBean;

import java.util.List;


/**
 * Tests the {@link DocumentRules} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentRulesTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link DocumentRules#addDocument} method.
     */
    public void testAddDocument() {
        // create an act.patientClinicalEvent and act.patientDocumentImage and add a relationship between them
        Party patient = TestHelper.createPatient();
        Act event = (Act) create("act.patientClinicalEvent");
        ActBean eventBean = new ActBean(event);
        eventBean.addParticipation("participation.patient", patient);
        eventBean.save();

        DocumentAct act = (DocumentAct) create("act.patientDocumentImage");
        ActBean bean = new ActBean(act);
        bean.addParticipation("participation.patient", patient);

        eventBean.addNodeRelationship("items", act);
        save(act, event);

        // now add a document.
        DocumentRules rules = new DocumentRules();
        Document document1 = createDocument();
        List<IMObject> objects = rules.addDocument(act, document1);
        save(objects);

        assertEquals(document1.getObjectReference(), act.getDocument());

        Document document2 = createDocument();
        objects = rules.addDocument(act, document2);
        save(objects);

        assertEquals(document2.getObjectReference(), act.getDocument());

        List<Act> acts = bean.getNodeActs("versions");
        assertEquals(1, acts.size());
        DocumentAct old = (DocumentAct) acts.get(0);
        assertEquals(document1.getObjectReference(), old.getDocument());
    }

    /**
     * Helper to create a document.
     *
     * @return a new document
     */
    private Document createDocument() {
        Document document = (Document) create("document.other");
        document.setName("test.gif");
        document.setMimeType("image/gif");
        return document;
    }

}
