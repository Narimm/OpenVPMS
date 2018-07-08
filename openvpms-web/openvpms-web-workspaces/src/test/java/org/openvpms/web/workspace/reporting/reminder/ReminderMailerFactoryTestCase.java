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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.reporting.reminder;

import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.archetype.rules.practice.MailServer;
import org.openvpms.archetype.rules.practice.PracticeArchetypes;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.mail.DefaultMailerFactory;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.mail.Mailer;
import org.openvpms.web.component.mail.MailerFactory;
import org.openvpms.web.workspace.reporting.ReportingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the {@link ReminderMailerFactory}.
 *
 * @author Tim Anderson
 */
public class ReminderMailerFactoryTestCase extends ArchetypeServiceTest {

    /**
     * The practice rules.
     */
    @Autowired
    private PracticeRules practiceRules;

    /**
     * Tests the {@link ReminderMailerFactory#create(Party, Context)} method.
     */
    @Test
    public void testCreate() {
        MailerFactory mailerFactory = new DefaultMailerFactory(null, Mockito.mock(DocumentHandlers.class)) {
            @Override
            public Mailer create(MailContext context, JavaMailSender sender) {
                return super.create(context, sender);
            }
        };

        Party practice = createPractice();

        Party location1 = createLocation("Main Clinic", practice);

        // add contact and mail server and verify the mailer can be created. The from address will be that of the
        // practice
        practice.addContact(TestHelper.createEmailContact("foo@bar.com"));
        Entity settings1 = createMailSettings("bar.com");
        addMailServer(practice, settings1);

        ReminderMailerFactory factory1 = new TestReminderMailerFactory(practice, mailerFactory);
        Mailer mailer1 = factory1.create(location1, new LocalContext());
        checkMailer(mailer1, "\"VetsRUs\" <foo@bar.com>", settings1);

        // add location settings. These won't be as the location has no reminder contact
        Entity settings2 = createMailSettings("gum.com");
        addMailServer(location1, settings2);
        ReminderMailerFactory factory2 = new TestReminderMailerFactory(practice, mailerFactory);
        Mailer mailer2 = factory2.create(location1, new LocalContext());
        checkMailer(mailer2, "\"VetsRUs\" <foo@bar.com>", settings1);

        // add location contact. As it doesn't have REMINDER classification, the practice will still be used
        location1.addContact(TestHelper.createEmailContact("foo@gum.com"));
        save(location1);
        ReminderMailerFactory factory3 = new TestReminderMailerFactory(practice, mailerFactory);
        Mailer mailer3 = factory3.create(location1, new LocalContext());
        checkMailer(mailer3, "\"VetsRUs\" <foo@bar.com>", settings1);

        // add a REMINDER contact.
        location1.addContact(TestHelper.createEmailContact("bar@gum.com", false, "REMINDER"));
        save(location1);
        ReminderMailerFactory factory4 = new TestReminderMailerFactory(practice, mailerFactory);
        Mailer mailer4 = factory4.create(location1, new LocalContext());
        checkMailer(mailer4, "\"Main Clinic\" <bar@gum.com>", settings2);
    }

    /**
     * Verifies that a {@link ReportingException} is thrown if the practice has no email contact or mail server.
     */
    @Test
    public void testNoPracticeContact() {
        Party practice = createPractice();
        try {
            new ReminderMailerFactory(practice, practiceRules, getArchetypeService(), null);
            fail("Expected ReportingException to be thrown");
        } catch (ReportingException expected) {
            assertEquals("Practice VetsRUs has no email contact", expected.getMessage());
        }

        // now add a contact
        try {
            practice.addContact(TestHelper.createEmailContact("foo@bar.com"));
            new ReminderMailerFactory(practice, practiceRules, getArchetypeService(), null);
            fail("Expected ReportingException to be thrown");
        } catch (ReportingException expected) {
            assertEquals("No Mail Server has been configured for VetsRUs", expected.getMessage());
        }

        // now add a mail server. Construction should succeed
        addMailServer(practice, createMailSettings("bar.com"));
        new ReminderMailerFactory(practice, practiceRules, getArchetypeService(), null);
    }

    /**
     * Verifies that a mailer has the correct from address and settings.
     *
     * @param mailer   the mailer
     * @param from     the expected from address
     * @param settings the expected settings
     */
    private void checkMailer(Mailer mailer, String from, Entity settings) {
        assertTrue(mailer instanceof TestMailer);
        assertEquals(from, mailer.getFrom());
        assertEquals(settings.getId(), ((TestMailer) mailer).getMailServer().getId());
    }

    /**
     * Helper to create a practice.
     *
     * @return a new practice
     */
    private Party createPractice() {
        Party practice = (Party) create(PracticeArchetypes.PRACTICE);
        practice.setName("VetsRUs");
        practice.setActive(false); // make it inactive, to avoid interfering with existing data

        IMObjectBean bean = getArchetypeService().getBean(practice);
        Lookup currency = TestHelper.getCurrency("AUD");
        bean.setValue("currency", currency.getCode());
        save(practice);
        return practice;
    }

    /**
     * Creates an <em>entity.mailServer</em> for the specified host.
     *
     * @param host the host
     * @return new mail settings
     */
    private Entity createMailSettings(String host) {
        Entity settings = (Entity) create("entity.mailServer");
        IMObjectBean bean = getArchetypeService().getBean(settings);
        bean.setValue("name", host);
        bean.setValue("host", host);
        bean.save();
        return settings;
    }

    /**
     * Creates a location linked to the practice.
     *
     * @param name     the location name
     * @param practice the practice name
     * @return a new location
     */
    private Party createLocation(String name, Party practice) {
        Party location = (Party) create(PracticeArchetypes.LOCATION);
        location.setName(name);
        IMObjectBean bean = getArchetypeService().getBean(practice);
        bean.addTarget("locations", location, "practice");
        bean.save(location);
        return location;
    }

    /**
     * Adds a mail server to a location
     *
     * @param location the location
     * @param settings the mail server settings
     */
    private void addMailServer(Party location, Entity settings) {
        IMObjectBean bean = getArchetypeService().getBean(location);
        bean.addTarget("mailServer", settings);
    }

    private static class TestMailer implements Mailer {

        private final Mailer mailer;

        private final MailServer settings;

        public TestMailer(Mailer mailer, MailServer settings) {
            this.mailer = mailer;
            this.settings = settings;
        }

        /**
         * Returns the mail server settings.
         *
         * @return the settings, or {@code null} if none is configured
         */
        public MailServer getMailServer() {
            return settings;
        }

        /**
         * Returns the mail context.
         *
         * @return the mail context
         */
        @Override
        public MailContext getContext() {
            return mailer.getContext();
        }

        /**
         * Sets the from address.
         *
         * @param from the from address
         */
        @Override
        public void setFrom(String from) {
            mailer.setFrom(from);
        }

        /**
         * Returns the from address.
         *
         * @return the from address
         */
        @Override
        public String getFrom() {
            return mailer.getFrom();
        }

        /**
         * Sets the to address.
         *
         * @param to the to addresses. May be {@code null}
         */
        @Override
        public void setTo(String[] to) {
            mailer.setTo(to);
        }

        /**
         * Returns the to addresses.
         *
         * @return the to addresses. May be {@code null}
         */
        @Override
        public String[] getTo() {
            return mailer.getTo();
        }

        /**
         * Sets the CC addresses.
         *
         * @param cc the CC addresses. May be {@code null}
         */
        @Override
        public void setCc(String[] cc) {
            mailer.setCc(cc);
        }

        /**
         * Returns the CC addresses.
         *
         * @return the CC addresses. May be {@code null}
         */
        @Override
        public String[] getCc() {
            return mailer.getCc();
        }

        /**
         * Sets the BCC addresses.
         *
         * @param bcc the BCC addresses. May be {@code null}
         */
        @Override
        public void setBcc(String[] bcc) {
            mailer.setBcc(bcc);
        }

        /**
         * Returns the BCC addresses.
         *
         * @return the BCC addresses. May be {@code null}
         */
        @Override
        public String[] getBcc() {
            return mailer.getBcc();
        }

        /**
         * Sets the subject.
         *
         * @param subject the subject
         */
        @Override
        public void setSubject(String subject) {
            mailer.setSubject(subject);
        }

        /**
         * Returns the subject.
         *
         * @return the subject
         */
        @Override
        public String getSubject() {
            return mailer.getSubject();
        }

        /**
         * Sets the body.
         *
         * @param body the body
         */
        @Override
        public void setBody(String body) {
            mailer.setBody(body);
        }

        /**
         * Returns the body.
         *
         * @return the body
         */
        @Override
        public String getBody() {
            return mailer.getBody();
        }

        /**
         * Adds an attachment.
         *
         * @param document the document to attach
         */
        @Override
        public void addAttachment(Document document) {
            mailer.addAttachment(document);
        }

        /**
         * Returns the attachments.
         *
         * @return the attachments
         */
        @Override
        public List<Document> getAttachments() {
            return mailer.getAttachments();
        }

        /**
         * Sends the mail.
         *
         * @throws OpenVPMSException for any error
         */
        @Override
        public void send() {
            mailer.send();
        }
    }

    private class TestReminderMailerFactory extends ReminderMailerFactory {
        public TestReminderMailerFactory(Party practice, MailerFactory mailerFactory) {
            super(practice, practiceRules, getArchetypeService(), mailerFactory);
        }

        /**
         * Creates a mailer for the specified context and mail server.
         *
         * @param context  the context
         * @param settings the mail server settings
         * @return a new mailer
         */
        @Override
        protected Mailer create(Context context, MailServer settings) {
            return new TestMailer(super.create(context, settings), settings);
        }
    }
}
