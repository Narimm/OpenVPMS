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

package org.openvpms.etl.load.tools.doc;

import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.RelationalOp;
import org.openvpms.etl.tools.doc.DocumentFactory;
import org.openvpms.etl.tools.doc.DocumentLoader;
import org.springframework.test.AbstractDependencyInjectionSpringContextTests;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;


/**
 * Tests the {@link DocumentLoader} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentLoaderTestCase
        extends AbstractDependencyInjectionSpringContextTests {

    /**
     * The archetype service.
     */
    private IArchetypeService service;


    /**
     * Tests the {@link DocumentLoader}.
     */
    public void test() {
        final int count = 10;
        final String shortName = "act.patientDocumentAttachment";
        for (int i = 0; i < count; ++i) {
            createDocumentAct(shortName, "file" + i);
        }

        // find all documents matching the short name with null doc references.
        ArchetypeQuery query = new ArchetypeQuery(shortName, false, true);
        query.add(new NodeConstraint("docReference", RelationalOp.IsNULL));
        List<DocumentAct> expected = new ArrayList<DocumentAct>();
        Iterator<DocumentAct> iter = new IMObjectQueryIterator<DocumentAct>(
                query);
        while (iter.hasNext()) {
            expected.add(iter.next());
        }
        assertTrue(expected.size() >= count);

        // load documents for all matching documents
        TestFactory creator = new TestFactory();
        DocumentLoader loader = new DocumentLoader(service, creator);
        loader.load(shortName);

        // verify there a no acts left to load documents for.
        iter = new IMObjectQueryIterator<DocumentAct>(query);
        assertFalse(iter.hasNext());
    }

    /**
     * Returns the location of the spring config files.
     *
     * @return an array of config locations
     */
    protected String[] getConfigLocations() {
        return new String[]{"applicationContext.xml"};
    }

    /**
     * Sets up the test case.
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();

        service = (IArchetypeService) applicationContext.getBean(
                "archetypeService");
    }

    /**
     * Creates a new document act.
     *
     * @param shortName the document act short name
     * @param fileName  the file name
     * @return a new document act
     */
    private DocumentAct createDocumentAct(String shortName, String fileName) {
        Party patient = (Party) service.create("party.patientpet");
        patient.setName("ZTestPet-" + System.currentTimeMillis());
        IMObjectBean bean = new IMObjectBean(patient);
        bean.setValue("species", "CANINE");
        bean.save();
        DocumentAct act = (DocumentAct) service.create(shortName);
        assertNotNull(act);
        act.setFileName(fileName);
        ActBean actBean = new ActBean(act);
        actBean.addParticipation("participation.patient", patient);
        actBean.save();
        return act;
    }

    /**
     * Test document factory that simply creates a new document for a
     * document act.
     */
    private class TestFactory implements DocumentFactory {

        public Document create(DocumentAct act) {
            Document doc = (Document) service.create("document.other");
            doc.setName(act.getFileName());
            return doc;
        }
    }

}
