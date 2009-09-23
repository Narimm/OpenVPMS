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

package org.openvpms.archetype.rules.product;

import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;


/**
 * Tests the {@link DemographicUpdater} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DemographicUpdaterTestCase extends ArchetypeServiceTest {

    /**
     * Tests the {@link DemographicUpdater#evaluate(IMObject, Lookup)} method
     * without a node name.
     */
    public void testEvaluate() {
        Lookup desex = createDemographicUpdate(null, "party:setPatientDesexed(.)");
        Party patient = TestHelper.createPatient();
        checkEvaluateDesex(desex, patient, patient);

        Lookup deceased = createDemographicUpdate(null, "openvpms:set(., 'deceased', true())");
        checkEvaluateDeceased(deceased, patient, patient);
    }

    /**
     * Tests the {@link DemographicUpdater#evaluate(IMObject, Lookup)} method
     * with a node name.
     */
    public void testEvaluateWithNode() {
        Party patient = TestHelper.createPatient();
        Act invoiceItem = (Act) create("act.customerAccountInvoiceItem");
        ActBean bean = new ActBean(invoiceItem);
        bean.addParticipation("participation.patient", patient);

        Lookup desex = createDemographicUpdate("patient.entity", "party:setPatientDesexed(.)");
        checkEvaluateDesex(desex, invoiceItem, patient);

        Lookup deceased = createDemographicUpdate("patient.entity", "openvpms:set(., 'deceased', true())");
        checkEvaluateDeceased(deceased, invoiceItem, patient);
    }

    /**
     * Tests the
     * {@link DemographicUpdater#evaluate(IMObject, Collection<Lookup>)} method.
     */
    public void testEvaluateCollection() {
        // Verify that a patient is flagged as desexed and deceased.
        Lookup desex = createDemographicUpdate(null, "party:setPatientDesexed(.)");
        Lookup deceased = createDemographicUpdate(null, "party:setPatientDeceased(.)");

        List<Lookup> lookups = Arrays.asList(desex, deceased);
        Party patient = TestHelper.createPatient();
        DemographicUpdater updater = new DemographicUpdater();
        updater.evaluate(patient, lookups);

        IMObjectBean bean = new IMObjectBean(get(patient));
        assertTrue(bean.getBoolean("desexed"));
        assertTrue(bean.getBoolean("deceased"));
    }

    /**
     * Verifies that a desexing lookup evaluates correctly to desex a patient.
     *
     * @param desex   the desexing <em>lookup.demographicUpdate</em>
     * @param context the context object to evaluate against
     * @param patient the patient that will be updated
     */
    private void checkEvaluateDesex(Lookup desex, IMObject context, Party patient) {
        IMObjectBean bean = new IMObjectBean(patient);
        assertFalse(bean.getBoolean("desexed"));

        DemographicUpdater updater = new DemographicUpdater();
        updater.evaluate(context, desex);

        patient = get(patient);
        bean = new IMObjectBean(patient);
        assertTrue(bean.getBoolean("desexed"));
    }

    /**
     * Verifies that a deceased lookup evaluates correctly to mark a patient deceased.
     *
     * @param deceased the deceased <em>lookup.demographicUpdate</em>
     * @param context  the context object to evaluate against
     * @param patient  the patient that will be updated
     */
    private void checkEvaluateDeceased(Lookup deceased, IMObject context, Party patient) {
        IMObjectBean bean = new IMObjectBean(patient);
        assertFalse(bean.getBoolean("deceased"));

        DemographicUpdater updater = new DemographicUpdater();
        updater.evaluate(context, deceased);

        patient = get(patient);
        bean = new IMObjectBean(patient);
        assertTrue(bean.getBoolean("deceased"));
    }

    /**
     * Creates a new <em>lookup.demographicUpdate</em>.
     *
     * @param node       the node name. May be <tt>null</tt>
     * @param expression the expression
     * @return a new lookup
     */
    private Lookup createDemographicUpdate(String node, String expression) {
        Lookup lookup = (Lookup) create("lookup.demographicUpdate");
        lookup.setCode("XDemographicUpdate-" + System.currentTimeMillis());
        IMObjectBean bean = new IMObjectBean(lookup);
        bean.setValue("nodeName", node);
        bean.setValue("expression", expression);
        return lookup;
    }

}
