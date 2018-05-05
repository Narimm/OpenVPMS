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

package org.openvpms.web.workspace.customer.document;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.DateRangeActQuery;
import org.openvpms.web.component.im.query.MultiSelectBrowser;
import org.openvpms.web.component.im.query.MultiSelectTableBrowser;
import org.openvpms.web.component.im.query.TabbedBrowser;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.patient.mr.PatientDocumentQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * A browser for customer and patient documents.
 *
 * @author Tim Anderson
 */
public class CustomerPatientDocumentBrowser extends TabbedBrowser<Act> implements MultiSelectBrowser<Act> {

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
     * The date to query from. May be {@code null}.
     */
    private final Date from;

    /**
     * The date to query to. May be {@code null}.
     */
    private final Date to;

    /**
     * The layout context.
     */
    private final LayoutContext context;

    /**
     * The customer document browser.
     */
    private MultiSelectTableBrowser<Act> customerDocuments;

    /**
     * The patient document browser.
     */
    private MultiSelectTableBrowser<Act> patientDocuments;

    /**
     * Constructs a {@link CustomerPatientDocumentBrowser}.
     *
     * @param customer the customer. May be {@code null}
     * @param patient  the patient. May be {@code null}
     * @param context  the layout context
     */
    public CustomerPatientDocumentBrowser(Party customer, Party patient, LayoutContext context) {
        this(customer, patient, true, null, null, context);
    }

    /**
     * Constructs a {@link CustomerPatientDocumentBrowser}.
     *
     * @param customer      the customer. May be {@code null}
     * @param patient       the patient. May be {@code null}
     * @param customerFirst if {@code true} display the customer tab first, otherwise display the patient tab
     * @param from          the from date. May  be {@code null}
     * @param to            the to date. May be {@code null}
     * @param context       the layout context
     */
    public CustomerPatientDocumentBrowser(Party customer, Party patient, boolean customerFirst, Date from,
                                          Date to, LayoutContext context) {
        this.customer = customer;
        this.patient = patient;
        this.customerFirst = customerFirst;
        this.from = from;
        this.to = to;
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
     * Returns the selections from each browser.
     *
     * @return the selections
     */
    public Collection<Act> getSelections() {
        List<Act> result = new ArrayList<>();
        if (customerFirst) {
            addSelections(result, customerDocuments);
            addSelections(result, patientDocuments);
        } else {
            addSelections(result, patientDocuments);
            addSelections(result, customerDocuments);
        }
        return result;
    }

    /**
     * Clears the selections.
     */
    @Override
    public void clearSelections() {
        if (customerDocuments != null) {
            customerDocuments.clearSelections();
        }
        if (patientDocuments != null) {
            patientDocuments.clearSelections();
        }
    }

    private void addSelections(List<Act> documents, MultiSelectTableBrowser<Act> browser) {
        if (browser != null) {
            documents.addAll(browser.getSelections());
        }
    }

    /**
     * Adds the customer browser, if it the customer exists.
     */
    private void addCustomerBrowser() {
        if (customerDocuments == null && customer != null) {
            CustomerDocumentQuery<Act> query = init(new CustomerDocumentQuery<>(customer));
            customerDocuments = new MultiSelectTableBrowser<>(query, context);
            addBrowser(Messages.get("customer.documentbrowser.customer"), customerDocuments);
        }
    }

    /**
     * Adds the patient browser, if it the patient exists.
     */
    private void addPatientBrowser() {
        if (patientDocuments == null && patient != null) {
            PatientDocumentQuery<Act> query = init(new PatientDocumentQuery<>(patient));
            patientDocuments = new MultiSelectTableBrowser<>(query, context);
            addBrowser(Messages.get("customer.documentbrowser.patient"), patientDocuments);
        }
    }

    /**
     * Initialises a query.
     *
     * @param query the query
     * @return the query
     */
    private <T extends DateRangeActQuery<Act>> T init(T query) {
        if (from != null || to != null) {
            query.getComponent();
            query.setAllDates(false);
            query.setFrom(from);
            query.setTo(to);
        }
        return query;
    }

}
