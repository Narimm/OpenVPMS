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

package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.v25.message.RDS_O13;
import org.junit.Test;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.FinancialTestHelper;
import org.openvpms.archetype.rules.patient.PatientRules;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.hl7.patient.PatientContext;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;


/**
 * Tests the {@link RDSProcessor}.
 *
 * @author Tim Anderson
 */
public class RDSProcessorTestCase extends AbstractRDSTest {

    /**
     * The patient rules.
     */
    private PatientRules rules;

    /**
     * The user rules.
     */
    private UserRules userRules;

    /**
     * The product.
     */
    private Product product;

    /**
     * The RDS O13 test message.
     */
    private RDS_O13 rds;

    /**
     * Sets up the test case.
     */
    @Override
    public void setUp() {
        super.setUp();
        rules = new PatientRules(getArchetypeService(), LookupServiceHelper.getLookupService());
        userRules = new UserRules(getArchetypeService());
        product = createProduct();
        try {
            rds = createRDS(product);
            log("RDS", rds);
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }

    /**
     * Verifies that orders are created from dispense messages.
     *
     * @throws HL7Exception for any HL7 exception
     */
    @Test
    public void testCreateOrder() throws HL7Exception {
        RDSProcessor processor = new RDSProcessor(getArchetypeService(), rules, userRules);
        List<Act> acts = processor.process(rds, getContext().getLocation().getObjectReference());
        assertEquals(2, acts.size());
        ActBean order = new ActBean(acts.get(0));
        ActBean item = new ActBean(acts.get(1));
        Party customer = getContext().getCustomer();
        Party location = getContext().getLocation();
        User clinician = getContext().getClinician();
        Party patient = getContext().getPatient();
        assertEquals(customer.getObjectReference(), order.getNodeParticipantRef("customer"));
        assertEquals(clinician.getObjectReference(), order.getNodeParticipantRef("clinician"));
        assertEquals(location.getObjectReference(), order.getNodeParticipantRef("location"));
        assertEquals(patient.getObjectReference(), item.getNodeParticipantRef("patient"));
        assertEquals(product.getObjectReference(), item.getNodeParticipantRef("product"));
        assertEquals(clinician.getObjectReference(), item.getNodeParticipantRef("clinician"));
        checkEquals(BigDecimal.valueOf(2), item.getBigDecimal("quantity"));
        assertEquals("90032145", item.getString("reference"));
        assertEquals(ActStatus.IN_PROGRESS, order.getStatus());
        save(acts);
    }

    /**
     * Verifies that orders are created from dispense messages.
     *
     * @throws HL7Exception for any HL7 exception
     */
    @Test
    public void testUnknownPatient() throws HL7Exception {
        RDSProcessor processor = new RDSProcessor(getArchetypeService(), rules, userRules);
        rds.getPATIENT().getPID().getPatientID().getIDNumber().setValue("UNKNOWN");
        log("RDS: ", rds);
        List<Act> acts = processor.process(rds, getContext().getLocation().getObjectReference());
        assertEquals(2, acts.size());
        ActBean order = new ActBean(acts.get(0));
        ActBean item = new ActBean(acts.get(1));
        assertNull(order.getNodeParticipantRef("customer"));
        assertNull(item.getNodeParticipantRef("patient"));
        assertEquals(product.getObjectReference(), item.getNodeParticipantRef("product"));
        checkEquals(BigDecimal.valueOf(2), item.getBigDecimal("quantity"));
        assertEquals("90032145", item.getString("reference"));
        assertEquals("Unknown patient, Id='UNKNOWN', name='Fido Bar'", order.getString("notes"));
        save(acts);
    }

    /**
     * Verifies that orders are created from dispense messages.
     *
     * @throws HL7Exception for any HL7 exception
     */
    @Test
    public void testUnknownProduct() throws HL7Exception {
        RDSProcessor processor = new RDSProcessor(getArchetypeService(), rules, userRules);
        rds.getORDER().getRXD().getDispenseGiveCode().getIdentifier().setValue("UNKNOWN");
        log("RDS: ", rds);
        List<Act> acts = processor.process(rds, getContext().getLocation().getObjectReference());
        assertEquals(2, acts.size());
        ActBean order = new ActBean(acts.get(0));
        ActBean item = new ActBean(acts.get(1));
        Party customer = getContext().getCustomer();
        Party patient = getContext().getPatient();
        assertEquals(customer.getObjectReference(), order.getNodeParticipantRef("customer"));
        assertEquals(patient.getObjectReference(), item.getNodeParticipantRef("patient"));
        assertNull(item.getNodeParticipantRef("product"));
        checkEquals(BigDecimal.valueOf(2), item.getBigDecimal("quantity"));
        assertEquals("90032145", item.getString("reference"));
        assertEquals("Unknown Dispense Give Code, id='UNKNOWN', name='Valium 2mg'", order.getString("notes"));
        save(acts);
    }

    /**
     * Verifies that a note is added if the patient changes.
     *
     * @throws HL7Exception for any HL7 exception
     */
    @Test
    public void testDifferentPatient() throws HL7Exception {
        RDSProcessor processor = new RDSProcessor(getArchetypeService(), rules, userRules);
        PatientContext context = getContext();
        Party patient1 = context.getPatient();
        Party patient2 = TestHelper.createPatient();
        List<FinancialAct> invoice = FinancialTestHelper.createChargesInvoice(
                BigDecimal.TEN, context.getCustomer(), patient2, product, ActStatus.IN_PROGRESS);
        save(invoice);
        FinancialAct invoiceItem = invoice.get(1);
        String itemId = Long.toString(invoiceItem.getId());
        rds.getORDER().getORC().getPlacerOrderNumber().getEntityIdentifier().setValue(itemId);
        List<Act> acts = processor.process(rds, context.getLocation().getObjectReference());
        assertEquals(2, acts.size());
        ActBean order = new ActBean(acts.get(0));
        assertEquals("Patient is different to that in the original invoice. Was '" + patient2.getName()
                     + "' (" + patient2.getId() + "). Now '" + patient1.getName() + "' (" + patient1.getId() + ")",
                     order.getString("notes"));
        save(acts);
    }
}
