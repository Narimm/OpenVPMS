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

import org.junit.Test;
import org.openvpms.archetype.rules.customer.CustomerArchetypes;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.DocumentTemplate;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.test.AbstractAppTest;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

/**
 * Tests the {@link FileNameFormatter}.
 *
 * @author Tim Anderson
 */
public class FileNameFormatterTestCase extends AbstractAppTest {

    /**
     * Tests formatting when there is no context object.
     */
    @Test
    public void testSimpleFormat() {
        DocumentTemplate template = createTemplate("$file");
        FileNameFormatter formatter = new FileNameFormatter();
        assertEquals("foo", formatter.format("foo.txt", null, template));
    }

    /**
     * Tests formatting when the format uses a customer.
     */
    @Test
    public void testCustomerFormat() {
        DocumentTemplate template = createTemplate("concat($file, ' - ', party:getPartyFullName($customer))");
        Party customer = TestHelper.createCustomer("Foo", "Bar", true);
        IMObjectBean bean = new IMObjectBean(customer);
        bean.setValue("title", null);
        bean.save();
        FileNameFormatter formatter = new FileNameFormatter();
        Act act = (Act) create(CustomerArchetypes.DOCUMENT_LETTER);
        ActBean actBean = new ActBean(act);
        actBean.setNodeParticipant("customer", customer);
        assertEquals("Statement - Foo Bar", formatter.format("Statement.pdf", act, template));
    }

    /**
     * Tests formatting when the format uses a patient.
     */
    @Test
    public void testPatientFormat() {
        DocumentTemplate template = createTemplate("concat($file, ' - ', $patient.name)");
        Party patient = TestHelper.createPatient();
        patient.setName("Fido");
        save(patient);
        FileNameFormatter formatter = new FileNameFormatter();
        Act act = (Act) create(PatientArchetypes.DOCUMENT_LETTER);
        ActBean bean = new ActBean(act);
        bean.setNodeParticipant("patient", patient);
        assertEquals("Referral - Fido", formatter.format("Referral.pdf", act, template));
    }

    /**
     * Tests formatting when the format uses a customer and patient.
     */
    @Test
    public void testCustomerPatientFormat() {
        DocumentTemplate template = createTemplate("concat($file, ' - ', $patient.name, ' ', $customer.lastName)");
        Party customer = TestHelper.createCustomer("Foo", "Bar", true);
        Party patient = TestHelper.createPatient(customer);
        patient.setName("Fido");
        save(patient);
        FileNameFormatter formatter = new FileNameFormatter();
        Act act = (Act) create(PatientArchetypes.DOCUMENT_LETTER);
        ActBean bean = new ActBean(act);
        bean.setNodeParticipant("patient", patient);
        assertEquals("Referral - Fido Bar", formatter.format("Referral.pdf", act, template));
    }

    /**
     * Tests formatting when the format uses a supplier.
     */
    @Test
    public void testSupplierFormat() {
        DocumentTemplate template = createTemplate("concat($supplier.name, ' ', $file)");
        Party supplier = (Party) create(SupplierArchetypes.SUPPLIER_VET_PRACTICE);
        supplier.setName("Eastside");
        save(supplier);
        FileNameFormatter formatter = new FileNameFormatter();
        Act act = (Act) create(SupplierArchetypes.DOCUMENT_LETTER);
        ActBean bean = new ActBean(act);
        bean.setNodeParticipant("supplier", supplier);
        assertEquals("Eastside Referral", formatter.format("Referral.pdf", act, template));
    }

    /**
     * Verifies that illegal characters are replaced with underscores.
     */
    @Test
    public void testIllegalCharacters() {
        DocumentTemplate template = createTemplate("concat($file, ' - ', $patient.name)");
        Party patient = TestHelper.createPatient();
        patient.setName("\\/:*?<>|");
        save(patient);
        FileNameFormatter formatter = new FileNameFormatter();
        Act act = (Act) create(PatientArchetypes.DOCUMENT_LETTER);
        ActBean bean = new ActBean(act);
        bean.setNodeParticipant("patient", patient);
        assertEquals("Referral - ________", formatter.format("Referral.pdf", act, template));
    }

    /**
     * Creates a document template.
     *
     * @param expression the file name formatter expression.
     * @return a new template
     */
    private DocumentTemplate createTemplate(String expression) {
        Entity entity = (Entity) create(DocumentArchetypes.DOCUMENT_TEMPLATE);
        Lookup lookup = (Lookup) create(DocumentArchetypes.FILE_NAME_FORMAT);

        DocumentTemplate template = new DocumentTemplate(entity, getArchetypeService());
        assertNull(template.getFileNameExpression());

        IMObjectBean bean = new IMObjectBean(lookup);
        bean.setValue("expression", expression);
        entity.addClassification(lookup);
        return new DocumentTemplate(entity, getArchetypeService());
    }
}
