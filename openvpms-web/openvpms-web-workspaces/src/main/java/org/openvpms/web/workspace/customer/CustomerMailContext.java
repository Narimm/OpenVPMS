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

package org.openvpms.web.workspace.customer;

import org.apache.commons.collections.ComparatorUtils;
import org.openvpms.archetype.rules.supplier.SupplierArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.model.object.IMObject;
import org.openvpms.component.model.object.Reference;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextMailContext;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.contact.ContactHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.MultiSelectBrowser;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.mail.AddressFormatter;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.component.mail.ToAddressFormatter;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.customer.document.CustomerPatientDocumentBrowser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.openvpms.component.model.bean.Policies.active;


/**
 * An {@link MailContext} that uses an {@link Context} to returns 'from' addresses from the practice location or
 * practice, and 'to' addresses from the current customer and the current patient's referrals.
 *
 * @author Tim Anderson
 */
public class CustomerMailContext extends ContextMailContext {

    /**
     * Constructs a {@link CustomerMailContext}.
     *
     * @param context the context
     */
    public CustomerMailContext(Context context) {
        super(context);
    }

    /**
     * Constructs a {@link CustomerMailContext} that registers an attachment browser.
     *
     * @param context the context
     * @param help    the help context
     */
    public CustomerMailContext(Context context, final HelpContext help) {
        super(context);
        final DefaultLayoutContext layout = new DefaultLayoutContext(context, help);

        setAttachmentBrowserFactory(mailContext -> {
            MultiSelectBrowser<Act> result = null;
            Party customer = getContext().getCustomer();
            Party patient = getContext().getPatient();
            if (customer != null || patient != null) {
                result = new CustomerPatientDocumentBrowser(customer, patient, layout);
            }
            return result;
        });
    }

    /**
     * Returns the customer.
     *
     * @return the customer. May be {@code null}
     */
    public Party getCustomer() {
        return getContext().getCustomer();
    }

    /**
     * Returns the patient.
     *
     * @return the patient. May be {@code null}
     */
    public Party getPatient() {
        return getContext().getPatient();
    }

    /**
     * Returns the practice location.
     *
     * @return the practice location. May be {@code null}
     */
    public Party getLocation() {
        return getContext().getLocation();
    }

    /**
     * Returns the available 'to' email addresses.
     *
     * @return the 'to' email addresses
     */
    public List<Contact> getToAddresses() {
        List<Contact> contacts = new ArrayList<>();
        getToAddresses(contacts);
        if (contacts.size() > 1) {
            sort(contacts);
        }
        return contacts;
    }

    /**
     * Returns a formatter to format 'to' addresses.
     *
     * @return the 'to' address formatter
     */
    @Override
    public AddressFormatter getToAddressFormatter() {
        return new ReferringAddressFormatter();
    }

    /**
     * Creates a new mail context if the specified act has a customer or patient participation.
     *
     * @param act     the act
     * @param context the context source the practice and location
     * @param help    the help context
     * @return a new mail context, or {@code null} if the act has no customer no patient participation
     */
    public static CustomerMailContext create(Act act, Context context, HelpContext help) {
        ActBean bean = new ActBean(act);
        Party customer = getParty(bean, "customer", context);
        Party patient = getParty(bean, "patient", context);
        return create(customer, patient, context, help);
    }

    /**
     * Creates a new mail context if either the customer or patient is non-null.
     *
     * @param customer the customer. May be {@code null}
     * @param patient  the patient. May be {@code null}
     * @param context  the context to source the practice and location from
     * @param help     the help context
     * @return a new mail context, or {@code null} if both customer and patient are null
     */
    public static CustomerMailContext create(Party customer, Party patient, Context context, HelpContext help) {
        CustomerMailContext result = null;
        if (customer != null || patient != null) {
            Context local = new LocalContext(context);
            local.setCustomer(customer);
            local.setPatient(patient);
            result = new CustomerMailContext(local, help);
        }
        return result;
    }

    /**
     * Collects to available 'to' email addresses.
     *
     * @param addresses the list to collect addresses in
     */
    protected void getToAddresses(List<Contact> addresses) {
        addresses.addAll(ContactHelper.getEmailContacts(getContext().getCustomer()));
        Party patient = getContext().getPatient();
        if (patient != null) {
            EntityBean bean = new EntityBean(patient);
            Set<Reference> practices = new HashSet<>();
            // tracks processed practices to avoid retrieving them more than once

            for (IMObject referral : bean.getTargets("referrals", active())) {
                if (referral instanceof Party) {
                    addresses.addAll(ContactHelper.getEmailContacts((Party) referral));
                    if (TypeHelper.isA(referral, SupplierArchetypes.SUPPLIER_VET)) {
                        // add any contacts of the practices that the vet belongs to
                        IMObjectBean vet = new IMObjectBean(referral);
                        for (Reference ref : vet.getSourceRefs("practices")) {
                            if (!practices.contains(ref)) {
                                Party practice = (Party) IMObjectHelper.getObject(ref, getContext());
                                if (practice != null) {
                                    addresses.addAll(ContactHelper.getEmailContacts(practice));
                                    practices.add(ref);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the party associated with a node.
     *
     * @param bean    the bean
     * @param node    the node
     * @param context the context
     * @return the associated party, or {@code null} if none exists
     */
    private static Party getParty(ActBean bean, String node, Context context) {
        if (bean.hasNode(node)) {
            return (Party) IMObjectHelper.getObject(bean.getNodeParticipantRef(node), context);
        }
        return null;
    }

    /**
     * Sorts contacts by party name.
     *
     * @param contacts the contacts to sort
     */
    @SuppressWarnings("unchecked")
    private void sort(List<Contact> contacts) {
        final Comparator<Object> comparator = (Comparator<Object>) ComparatorUtils.nullHighComparator(null);
        // sort the contacts on party name
        Collections.sort(contacts, new Comparator<Contact>() {
            public int compare(Contact o1, Contact o2) {
                return comparator.compare(o1.getParty().getName(), o2.getParty().getName());
            }
        });
    }

    /**
     * An address formatter that indicates that a vet/vet practice is the referring vet/vet practice.
     */
    private static class ReferringAddressFormatter extends ToAddressFormatter {

        /**
         * Returns the type of a contact.
         *
         * @param contact the contact
         * @return the type of the contact. May be {@code null}
         */
        @Override
        public String getType(Contact contact) {
            String type = super.getType(contact);
            if (TypeHelper.isA(contact.getParty(), SupplierArchetypes.SUPPLIER_VET,
                               SupplierArchetypes.SUPPLIER_VET_PRACTICE)) {
                type = Messages.format("mail.type.referring", type);
            }
            return type;
        }
    }

}
