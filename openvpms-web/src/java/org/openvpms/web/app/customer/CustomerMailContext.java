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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.app.customer;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.party.Contact;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.web.app.customer.document.CustomerPatientDocumentBrowser;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextMailContext;
import org.openvpms.web.component.app.LocalContext;
import org.openvpms.web.component.im.contact.ContactHelper;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.mail.AttachmentBrowserFactory;
import org.openvpms.web.component.mail.MailContext;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * An {@link MailContext} that uses an {@link Context} to returns 'from' addresses from the practic location or
 * practice, and 'to' addresses from the current customer.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class CustomerMailContext extends ContextMailContext {

    /**
     * Constructs a <tt>CustomerMailContext</tt>
     *
     * @param context the context
     */
    public CustomerMailContext(Context context) {
        super(context);
        setAttachmentBrowserFactory(new AttachmentBrowserFactory() {
            public Browser<Act> createBrowser(MailContext context) {
                Browser<Act> result = null;
                Party customer = getContext().getCustomer();
                Party patient = getContext().getPatient();
                if (customer != null || patient != null) {
                    result = new CustomerPatientDocumentBrowser(customer, patient);
                }
                return result;
            }
        });
    }

    /**
     * Creates a new mail context if the specified act has a customer or patient participation.
     *
     * @param act     the act
     * @param context the context source the practice and location
     * @return a new mail context, or <tt>null</tt> if the act has no customer no patient participation
     */
    public static CustomerMailContext create(Act act, Context context) {
        ActBean bean = new ActBean(act);
        Party customer = getParty(bean, "customer");
        Party patient = getParty(bean, "patient");
        return create(customer, patient, context);
    }

    /**
     * Creates a new mail context if either the customer or patient is non-null.
     *
     * @param customer the customer. May be <tt>null</tt>
     * @param patient  the patient. May be <tt>null</tt>
     * @param context  the context source the practice and location
     * @return a new mail context, or <tt>null</tt> if both customer and patient are null
     */
    public static CustomerMailContext create(Party customer, Party patient, Context context) {
        CustomerMailContext result = null;
        if (customer != null || patient != null) {
            Context local = new LocalContext();
            local.setPractice(context.getPractice());
            local.setLocation(context.getLocation());
            local.setCustomer(customer);
            local.setPatient(patient);
            result = new CustomerMailContext(local);
        }
        return result;
    }

    /**
     * Returns the available ''to' email addresses.
     *
     * @return the 'to' email addresses
     */
    public List<Contact> getToAddresses() {
        List<Contact> result = new ArrayList<Contact>();
        result.addAll(ContactHelper.getEmailContacts(getContext().getCustomer()));
        Party patient = getContext().getPatient();
        if (patient != null) {
            EntityBean bean = new EntityBean(patient);
            for (Entity referral : bean.getNodeTargetEntities("referrals")) {
                if (referral instanceof Party) {
                    result.addAll(ContactHelper.getEmailContacts((Party) referral));
                }
            }
        }
        return result;
    }

    /**
     * Returns variables to be used in macro expansion.
     * <p/>
     * This implementation returns a map of:
     * <ul>
     * <li>customer -> the customer party
     * <li>patient -> the patient party
     * </ul>
     *
     * @return variables to use in macro expansion
     */
    @Override
    public Map<String, Object> getVariables() {
        Map<String, Object> variables = new HashMap<String, Object>();
        Context context = getContext();
        Party customer = context.getCustomer();
        Party patient = context.getPatient();
        if (customer != null) {
            variables.put("customer", customer);
        }
        if (patient != null) {
            variables.put("patient", patient);
        }
        return variables;
    }

    /**
     * Returns the party associated with a node.
     *
     * @param bean the bean
     * @param node the node
     * @return the associated party, or <tt>null</tt> if none exists
     */
    private static Party getParty(ActBean bean, String node) {
        if (bean.hasNode(node)) {
            return (Party) IMObjectHelper.getObject(bean.getNodeParticipantRef(node));
        }
        return null;
    }

}
