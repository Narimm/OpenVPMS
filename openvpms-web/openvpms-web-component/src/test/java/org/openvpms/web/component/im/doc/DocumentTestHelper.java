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

package org.openvpms.web.component.im.doc;

import org.apache.commons.io.IOUtils;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.deposit.DepositArchetypes;
import org.openvpms.archetype.rules.finance.till.TillArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.rules.product.ProductArchetypes;
import org.openvpms.archetype.rules.stock.StockArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.system.ServiceHelper;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import static org.junit.Assert.assertNotNull;


/**
 * Helper for document tests.
 *
 * @author Tim Anderson
 */
public class DocumentTestHelper {

    /**
     * Creates a document from a resource.
     *
     * @param path the resource path
     * @return a new document
     */
    public static Document createDocument(String path) {
        DocumentHandlers handlers = ServiceHelper.getDocumentHandlers();
        DocumentHandler handler = handlers.get(path, null);
        assertNotNull(handler);

        InputStream stream = DocumentTestHelper.class.getResourceAsStream(path);
        assertNotNull(stream);
        return handler.create(path, stream, null, -1);
    }

    /**
     * Creates a new <em>entity.documentTemplate</em>, associated with an <em>act.documentTemplate</em>.
     *
     * @param archetype the archetype the template is associated with
     * @return a new template
     */
    public static Entity createDocumentTemplate(String archetype) {
        Document document = createDocument("/blank.jrxml");
        return createDocumentTemplate(archetype, document);
    }

    /**
     * Creates a new <em>entity.documentTemplate</em>, associated with an <em>act.documentTemplate</em>.
     *
     * @param archetype the archetype the template is associated with
     * @param document  the document
     * @return a new template
     */
    public static Entity createDocumentTemplate(String archetype, Document document) {
        Entity entity = (Entity) TestHelper.create("entity.documentTemplate");
        IMObjectBean template = new IMObjectBean(entity);
        template.setValue("name", document.getName());
        template.setValue("archetype", archetype);

        createDocumentTemplate(entity, document);
        return entity;
    }

    /**
     * Creates a blank document and associates it with a <em>entity.documentTemplate</em>.
     *
     * @param template the <em>entity.documentTemplate</em>
     */
    public static void createDocumentTemplate(Entity template) {
        Document document = createDocument("/blank.jrxml");
        createDocumentTemplate(template, document);
    }

    /**
     * Creates an <em>act.documentTemplate</em>, associating it with the supplied document and
     * <em>entity.documentTemplate</em>.
     *
     * @param template the <em>entity.documentTemplate</em>
     * @param document the document
     */
    public static void createDocumentTemplate(Entity template, Document document) {
        DocumentAct act = (DocumentAct) TestHelper.create("act.documentTemplate");
        act.setDocument(document.getObjectReference());
        act.setFileName(document.getName());
        act.setMimeType(document.getMimeType());
        act.setDescription(DescriptorHelper.getDisplayName(document));
        ActBean bean = new ActBean(act);
        bean.addNodeParticipation("template", template);
        TestHelper.save(Arrays.asList(act, template, document));
    }

    /**
     * Converts a document to string.
     *
     * @param document the document
     * @param handlers the document handlers
     * @return the string form of the document
     * @throws IOException for any I/O error
     */
    public static String toString(Document document, DocumentHandlers handlers) throws IOException {
        return IOUtils.toString(handlers.get(document).getContent(document), "UTF-8");
    }

    public static Context createReportContext() {
        Context context = new LocalContext();
        addContext(context, PracticeArchetypes.PRACTICE, "Vets R Us");
        addContext(context, PracticeArchetypes.LOCATION, "Main Clinic");
        addContext(context, StockArchetypes.STOCK_LOCATION, "Main Stock");
        context.setCustomer(TestHelper.createCustomer("J", "Smith", false));
        addContext(context, PatientArchetypes.PATIENT, "Fido");
        addContext(context, SupplierArchetypes.SUPPLIER_ORGANISATION, "Vet Supplies");
        addContext(context, ProductArchetypes.MEDICATION, "Acepromazine");
        addContext(context, DepositArchetypes.DEPOSIT_ACCOUNT, "Main Deposit");
        addContext(context, TillArchetypes.TILL, "Main Till");
        User clinician = TestHelper.createClinician(false);
        clinician.setName("Vet");
        context.setClinician(clinician);
        User user = TestHelper.createUser("User", false);
        context.setUser(user);
        addContext(context, PatientArchetypes.CLINICAL_EVENT);
        addContext(context, CustomerAccountArchetypes.INVOICE);
        addContext(context, ScheduleArchetypes.APPOINTMENT);
        addContext(context, ScheduleArchetypes.TASK);
        return context;
    }

    /**
     * Creates an adds an object to the context, setting its name.
     *
     * @param context   the context
     * @param shortName the archetype short name of the object to add
     * @param name      the object name
     */
    private static void addContext(Context context, String shortName, String name) {
        addContext(context, shortName).setName(name);
    }

    /**
     * Creates an adds an object to the context.
     *
     * @param context   the context
     * @param shortName the archetype short name of the object to add
     * @return the added object
     */
    private static IMObject addContext(Context context, String shortName) {
        IMObject object = TestHelper.create(shortName);
        context.addObject(object);
        return object;
    }

}
