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

package org.openvpms.web.component.im.edit.act;

import org.junit.Before;
import org.junit.Test;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.estimate.EstimateArchetypes;
import org.openvpms.archetype.rules.patient.insurance.InsuranceArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;
import org.openvpms.web.workspace.customer.charge.ChargeEditContext;
import org.openvpms.web.workspace.customer.charge.CustomerChargeEditContext;
import org.openvpms.web.workspace.customer.charge.DefaultCustomerChargeActItemEditor;
import org.openvpms.web.workspace.customer.estimate.EstimateItemEditor;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;


/**
 * Verifies that for each act that has an "author" node, the corresponding editor populates it with the current user,
 * as per OVPMS-559.
 * <p/>
 * TODO - perhaps author information should be populated on save by a rule.
 *
 * @author Tim Anderson
 */
public class ActEditorInitAuthorTestCase extends AbstractAppTest {

    /**
     * The test user.
     */
    private User user;

    /**
     * Test layout context.
     */
    private LayoutContext layout;

    /**
     * Sets up the test case.
     */
    @Before
    @Override
    public void setUp() {
        super.setUp();
        Context context = new LocalContext();
        user = TestHelper.createUser();
        context.setUser(user);
        context.setPractice(TestHelper.getPractice());
        context.setCustomer(TestHelper.createCustomer());
        layout = new DefaultLayoutContext(context, new HelpContext("foo", null));
    }

    /**
     * Verifies that for each act archetype with an "author" node, the node is populated when the act is edited.
     */
    @Test
    public void testInitAuthor() {
        String[] exclusions = {CustomerAccountArchetypes.INVOICE_ITEM, CustomerAccountArchetypes.CREDIT_ITEM,
                               CustomerAccountArchetypes.COUNTER_ITEM, EstimateArchetypes.ESTIMATE_ITEM,
                               InsuranceArchetypes.CLAIM};
        // archetypes to excluse as their editors have special construction requirements

        IArchetypeService service = ServiceHelper.getArchetypeService();
        int count = 0;

        // find all act archetypes
        List<ArchetypeDescriptor> archetypes = service.getArchetypeDescriptors("act.*");
        for (ArchetypeDescriptor archetype : archetypes) {
            if (!TypeHelper.isA(new ArchetypeId(archetype.getShortName()), exclusions)
                && archetype.getNodeDescriptor("author") != null) {
                // found an archetype with an author node
                ++count;

                // create an instance
                String shortName = archetype.getType().getShortName();
                IMObject object = service.create(shortName);
                assertNotNull(object);
                assertTrue(object instanceof Act);

                // create an editor for the act
                IMObjectEditor editor = ServiceHelper.getBean(IMObjectEditorFactory.class).create(object, layout);

                // verify the author node has been populated
                checkAuthor(editor);
            }
        }
        assertFalse(count == 0);
    }

    /**
     * Verifies that the author node is populated by {@link DefaultCustomerChargeActItemEditor}.
     */
    @Test
    public void testInitAuthorForChargeItem() {
        FinancialAct invoice = (FinancialAct) TestHelper.create(CustomerAccountArchetypes.INVOICE);
        ActBean bean = new ActBean(invoice);
        Party customer = TestHelper.createCustomer();
        bean.addNodeParticipation("customer", customer);
        FinancialAct item = (FinancialAct) TestHelper.create(CustomerAccountArchetypes.INVOICE_ITEM);
        DefaultCustomerChargeActItemEditor editor = new DefaultCustomerChargeActItemEditor(
                item, invoice, new CustomerChargeEditContext(customer, null, layout), layout);
        checkAuthor(editor);
    }

    /**
     * Verifies that the author node is populated by {@link EstimateItemEditor}.
     */
    @Test
    public void testInitAuthorForEstimateItem() {
        Act estimate = (Act) TestHelper.create(EstimateArchetypes.ESTIMATE);
        ActBean bean = new ActBean(estimate);
        Party customer = TestHelper.createCustomer();
        bean.addNodeParticipation("customer", customer);
        Act item = (Act) TestHelper.create(EstimateArchetypes.ESTIMATE_ITEM);
        EstimateItemEditor editor = new EstimateItemEditor(item, estimate,
                                                           new ChargeEditContext(customer, null, layout), layout);
        checkAuthor(editor);
    }

    /**
     * Verifies that the author node has been populated correctly.
     *
     * @param editor the editor
     */
    protected void checkAuthor(IMObjectEditor editor) {
        ActBean bean = new ActBean((Act) editor.getObject());
        assertEquals(user.getObjectReference(), bean.getNodeParticipantRef("author"));
    }

}
