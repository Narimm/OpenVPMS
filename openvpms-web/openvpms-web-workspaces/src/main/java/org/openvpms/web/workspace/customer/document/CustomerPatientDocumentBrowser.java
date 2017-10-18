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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.customer.document;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.TabbedBrowser;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.patient.mr.PatientDocumentQuery;

/**
 * A browser for customer and patient documents.
 *
 * @author Tim Anderson
 */
public class CustomerPatientDocumentBrowser extends TabbedBrowser<Act> {

    /**
     * The customer. May be {@code null}
     */
    private final Party customer;

    /**
     * The patient. May be {@code null}
     */
    private final Party patient;

    /**
     * If {@code true} display the customer tab first, otherwise display the patient tab.
     */
    private final boolean customerFirst;

    /**
     * The layout context.
     */
    private final LayoutContext context;

    /**
     * The customer document browser.
     */
    private Browser<Act> customerDocuments;

    /**
     * The patient document browser.
     */
    private Browser<Act> patientDocuments;

    /**
     * Constructs a {@link CustomerPatientDocumentBrowser}.
     *
     * @param customer the customer. May be {@code null}
     * @param patient  the patient. May be {@code null}
     * @param context  the layout context
     */
    public CustomerPatientDocumentBrowser(Party customer, Party patient, LayoutContext context) {
        this(customer, patient, true, context);
    }

    /**
     * Constructs a {@link CustomerPatientDocumentBrowser}.
     *
     * @param customer      the customer. May be {@code null}
     * @param patient       the patient. May be {@code null}
     * @param customerFirst if {@code true} display the customer tab first, otherwise display the patient tab
     * @param context       the layout context
     */
    public CustomerPatientDocumentBrowser(Party customer, Party patient, boolean customerFirst, LayoutContext context) {
        this.customer = customer;
        this.patient = patient;
        this.customerFirst = customerFirst;
        this.context = context;
    }

    /**
     * Returns the browser component.
     *
     * @return the browser component
     */
    public Component getComponent() {
        if (customerFirst) {
            addCustomerBrowser();
            addPatientBrowser();
        } else {
            addPatientBrowser();
            addCustomerBrowser();
        }
        return super.getComponent();
    }

    /**
     * Adds the customer browser, if it the customer exists.
     */
    private void addCustomerBrowser() {
        if (customerDocuments == null && customer != null) {
            CustomerDocumentQuery<Act> query = new CustomerDocumentQuery<>(customer);
            customerDocuments = BrowserFactory.create(query, context);
            addBrowser(Messages.get("customer.documentbrowser.customer"), customerDocuments);
        }
    }

    /**
     * Adds the patient browser, if it the patient exists.
     */
    private void addPatientBrowser() {
        if (patientDocuments == null && patient != null) {
            PatientDocumentQuery<Act> query = new PatientDocumentQuery<>(patient);
            patientDocuments = BrowserFactory.create(query, context);
            addBrowser(Messages.get("customer.documentbrowser.patient"), patientDocuments);
        }
    }

}
