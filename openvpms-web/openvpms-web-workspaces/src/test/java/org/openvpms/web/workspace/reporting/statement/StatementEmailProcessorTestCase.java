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
package org.openvpms.web.workspace.reporting.statement;

import nextapp.echo2.app.ApplicationInstance;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.component.processor.ProcessorListener;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.TemplateHelper;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.finance.account.CustomerAccountRules;
import org.openvpms.archetype.rules.finance.statement.AbstractStatementTest;
import org.openvpms.archetype.rules.finance.statement.Statement;
import org.openvpms.archetype.rules.finance.statement.StatementProcessor;
import org.openvpms.archetype.rules.finance.statement.StatementRules;
import org.openvpms.archetype.rules.party.ContactArchetypes;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.datatypes.quantity.Money;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.doc.DocumentTestHelper;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.OpenVPMSApp;
import org.springframework.mail.javamail.JavaMailSender;

import javax.mail.internet.MimeMessage;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.openvpms.archetype.rules.act.ActStatus.POSTED;


/**
 * Tests the {@link StatementEmailProcessor}.
 *
 * @author Tim Anderson
 */
public class StatementEmailProcessorTestCase extends AbstractStatementTest {

    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        OpenVPMSApp app = (OpenVPMSApp) applicationContext.getBean("openVPMSApp");
        app.setApplicationContext(applicationContext);
        ApplicationInstance.setActive(app);
        app.doInit();

        TemplateHelper helper = new TemplateHelper();
        Entity entity = helper.getTemplateForArchetype(CustomerAccountArchetypes.OPENING_BALANCE);
        if (entity == null) {
            entity = (Entity) create(DocumentArchetypes.DOCUMENT_TEMPLATE);
        }
        IMObjectBean bean = new IMObjectBean(entity);
        bean.setValue("name", "Statement");
        bean.setValue("archetype", CustomerAccountArchetypes.OPENING_BALANCE);
        bean.setValue("emailSubject", "Statement email");
        bean.setValue("emailText", "Statement text");
        if (entity.isNew() || helper.getDocumentAct(entity) == null) {
            DocumentTestHelper.createDocumentTemplate(entity);
        } else {
            bean.save();
        }
    }

    /**
     * Verifies that a statement email is generated if the customer has a billing email address.
     */
    @Test
    public void testEmail() {
        JavaMailSender sender = mock(JavaMailSender.class);
        MimeMessage mimeMessage = mock(MimeMessage.class);
        when(sender.createMimeMessage()).thenReturn(mimeMessage);

        Date statementDate = getDate("2007-01-01");

        Party practice = getPractice();
        Party customer = getCustomer();
        addCustomerEmail(customer);
        save(customer);

        StatementRules rules = new StatementRules(practice, getArchetypeService(),
                                                  ServiceHelper.getLookupService(),
                                                  ServiceHelper.getBean(CustomerAccountRules.class));
        assertFalse(rules.hasStatement(customer, statementDate));
        List<Act> acts = getActs(customer, statementDate);
        assertEquals(0, acts.size());

        Money amount = new Money(100);
        List<FinancialAct> invoice1 = createChargesInvoice(amount, getDatetime("2007-01-01 10:00:00"));
        save(invoice1);

        acts = getActs(customer, statementDate);
        assertEquals(1, acts.size());
        checkAct(acts.get(0), invoice1.get(0), POSTED);

        final List<Statement> statements = new ArrayList<Statement>();
        StatementProcessor processor = new StatementProcessor(statementDate, practice, getArchetypeService(),
                                                              ServiceHelper.getLookupService(),
                                                              ServiceHelper.getBean(CustomerAccountRules.class));
        processor.addListener(new ProcessorListener<Statement>() {
            public void process(Statement statement) {
                statements.add(statement);
            }
        });
        processor.process(customer);
        assertEquals(1, statements.size());
        StatementEmailProcessor emailprocessor =
                new StatementEmailProcessor(sender, "Foo", "foo@bar.com", getPractice());
        emailprocessor.process(statements.get(0));
        Mockito.verify(sender, times(1)).send(mimeMessage);
    }

    /**
     * Helper to create an email contact.
     *
     * @param customer the customer
     */
    private void addCustomerEmail(Party customer) {
        customer.getContacts().clear();
        Contact contact = (Contact) create(ContactArchetypes.EMAIL);
        IMObjectBean bean = new IMObjectBean(contact);
        bean.setValue("emailAddress", "foo@bar.com");
        Lookup lookup = TestHelper.getLookup("lookup.contactPurpose", "BILLING");
        bean.addValue("purposes", lookup);
        customer.addContact(contact);
    }

}
