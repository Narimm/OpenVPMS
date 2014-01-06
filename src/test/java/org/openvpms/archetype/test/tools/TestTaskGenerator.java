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

package org.openvpms.archetype.test.tools;

import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.workflow.ScheduleTestHelper;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.context.support.FileSystemXmlApplicationContext;

import java.io.File;
import java.io.InputStream;
import java.util.Date;

import static org.junit.Assert.assertNotNull;
import static org.openvpms.archetype.test.TestHelper.createClinician;
import static org.openvpms.archetype.test.TestHelper.save;

/**
 * Tool to generate <em>act.customerTask</em> acts.
 *
 * @author Tim Anderson
 */
public class TestTaskGenerator {

    /**
     * Main line.
     *
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        String contextPath = "applicationContext.xml";
        if (!new File(contextPath).exists()) {
            new ClassPathXmlApplicationContext(contextPath);
        } else {
            new FileSystemXmlApplicationContext(contextPath);
        }

        Party[] workLists = new Party[10];
        for (int i = 0; i < workLists.length; ++i) {
            Entity taskType = ScheduleTestHelper.createTaskType("XTaskType-" + (i + 1), true);
            Party workList = ScheduleTestHelper.createWorkList(100, taskType);
            workList.setName("XWorkList-" + (i + 1));
            save(workList);
            workLists[i] = workList;
        }
        ScheduleTestHelper.createWorkListView(workLists);

        Date startTime = DateRules.getDate(DateRules.getTomorrow());
        User clinician = createClinician();
        for (int i = 0; i < 100; ++i) {
            Party customer = TestHelper.createCustomer("", "ZCustomer " + (i + 1), true);
            Party patient = TestHelper.createPatient(customer);
            patient.setName("ZPatient " + (i + 1));
            for (Party workList : workLists) {
                Act task = ScheduleTestHelper.createTask(startTime, null, workList, customer, patient, clinician,
                                                         clinician);
                save(task);

            }
        }
    }

    /**
     * Creates an <em>entity.documentTemplate</em> with associated document.
     *
     * @return the new template
     */
    private static Entity createDocumentTemplate() {
        String file = "/vaccination first reminder.odt";
        String mimeType = "application/vnd.oasis.opendocument.text";
        InputStream stream = TestReminderGenerator.class.getResourceAsStream(file);
        assertNotNull(stream);

        DocumentHandlers handlers = new DocumentHandlers();
        DocumentHandler handler = handlers.get(file, mimeType);
        Document document = handler.create(file, stream, mimeType, -1);

        Entity template = (Entity) TestHelper.create(DocumentArchetypes.DOCUMENT_TEMPLATE);
        EntityBean bean = new EntityBean(template);
        bean.setValue("name", "XDocumentTemplate");
        bean.setValue("archetype", PatientArchetypes.DOCUMENT_FORM);
        bean.save();

        DocumentAct act = (DocumentAct) TestHelper.create(DocumentArchetypes.DOCUMENT_TEMPLATE_ACT);
        act.setFileName(document.getName());
        act.setMimeType(document.getMimeType());

        act.setDescription(DescriptorHelper.getDisplayName(document));
        act.setDocument(document.getObjectReference());
        ActBean actBean = new ActBean(act);
        actBean.addNodeParticipation("template", template);

        String name = template.getName();
        if (name == null) {
            name = document.getName();
        }
        template.setName(name);
        save(document, template, act);
        return template;
    }

}
