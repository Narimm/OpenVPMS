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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.mr;

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.DefaultActQuery;
import org.openvpms.web.component.im.query.DefaultIMObjectTableBrowser;
import org.openvpms.web.component.im.query.TabbedBrowser;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.workspace.patient.history.PatientHistoryBrowser;
import org.openvpms.web.workspace.patient.history.PatientHistoryCRUDWindow;
import org.openvpms.web.workspace.patient.history.PatientHistoryQuery;

import static org.openvpms.archetype.rules.patient.PatientArchetypes.PATIENT_PARTICIPATION;


/**
 * Patient record browser.
 *
 * @author Tim Anderson
 */
public class RecordBrowser extends TabbedBrowser<Act> {

    /**
     * The reminder statuses to query.
     */
    private static final ActStatuses STATUSES = new ActStatuses(ReminderArchetypes.REMINDER);


    /**
     * The document archetypes.
     */
    private final Archetypes<DocumentAct> docArchetypes;

    /**
     * The prescription archetypes.
     */
    private final Archetypes<Act> prescriptionArchetypes;

    /**
     * The patient.
     */
    private Party patient;

    /**
     * The history browser.
     */
    private PatientHistoryBrowser history;

    /**
     * Problem browser tab index.
     */
    private int problemIndex;

    /**
     * Reminder browser tab index.
     */
    private int remindersIndex;

    /**
     * Document browser tab index.
     */
    private int documentsIndex;

    /**
     * Charges browser tab index.
     */
    private int chargesIndex;

    /**
     * Prescription browser tab index.
     */
    private int prescriptionIndex;

    /**
     * Patient charges shortnames supported by the workspace.
     */
    private static final String[] CHARGES_SHORT_NAMES = {
            "act.customerAccountInvoiceItem",
            "act.customerAccountCreditItem"
    };

    /**
     * The default sort constraint.
     */
    private static final SortConstraint[] DEFAULT_SORT
            = new SortConstraint[]{new NodeSortConstraint("startTime", false)};


    /**
     * Constructs a {@link RecordBrowser}.
     *
     * @param query   the patient history query
     * @param context the context
     * @param help    the help context
     */
    public RecordBrowser(Party patient, PatientHistoryQuery query, Context context, HelpContext help) {
        this.patient = patient;
        docArchetypes = Archetypes.create(PatientDocumentQuery.DOCUMENT_SHORT_NAMES, DocumentAct.class,
                                          Messages.get("patient.document.createtype"));
        prescriptionArchetypes = Archetypes.create(PatientArchetypes.PRESCRIPTION, Act.class);

        LayoutContext layout = new DefaultLayoutContext(context, help);

        history = createHistoryBrowser(query, layout);
        addBrowser(Messages.get("button.summary"), history);

        problemIndex = addBrowser(Messages.get("button.problem"), createProblemBrowser(patient, layout));
        remindersIndex = addBrowser(Messages.get("button.reminder"), createReminderAlertBrowser(patient, layout));
        documentsIndex = addBrowser(Messages.get("button.document"), createDocumentBrowser(patient, layout));
        chargesIndex = addBrowser(Messages.get("button.charges"), createChargeBrowser(patient, layout));
        prescriptionIndex = addBrowser(Messages.get("button.prescriptions"),
                                       createPrescriptionBrowser(patient, layout));
    }

    /**
     * Creates a {@link CRUDWindow} for the current browser.
     *
     * @param context the context
     * @param help    the help context
     * @return a new {@link CRUDWindow}
     */
    public CRUDWindow<Act> createCRUDWindow(Context context, HelpContext help) {
        CRUDWindow<Act> result;
        int index = getSelectedBrowserIndex();
        if (index == problemIndex) {
            result = createProblemRecordCRUDWindow(context, help);
        } else if (index == remindersIndex) {
            result = createReminderAlertCRUDWindow(context, help);
        } else if (index == documentsIndex) {
            result = createDocumentCRUDWindow(context, help);
        } else if (index == chargesIndex) {
            result = createChargesCRUDWindow(context, help);
        } else if (index == prescriptionIndex) {
            result = createPrescriptionCRUDWindow(context, help);
        } else {
            result = createHistoryCRUDWindow(context, help);
        }
        return result;
    }

    /**
     * Returns the event associated with the current selected history act.
     *
     * @param act the current selected act. May be {@code null}
     * @return the event associated with the current selected act, or {@code null} if none is found
     */
    public Act getEvent(Act act) {
        if (act == null) {
            act = history.getSelected();
        }
        return history.getEvent(act);
    }

    /**
     * Creates a patient history browser.
     *
     * @param query  the history query
     * @param layout the layout context
     * @return a new patient history browser
     */
    protected PatientHistoryBrowser createHistoryBrowser(PatientHistoryQuery query, LayoutContext layout) {
        return new PatientHistoryBrowser(query, layout);
    }

    /**
     * Creates a {@link CRUDWindow} for the patient history browser.
     *
     * @param context the context
     * @param help    the help context
     * @return a new {@link CRUDWindow}
     */
    protected CRUDWindow<Act> createHistoryCRUDWindow(Context context, HelpContext help) {
        PatientHistoryCRUDWindow result = new PatientHistoryCRUDWindow(context, help);
        result.setQuery((PatientHistoryQuery) history.getQuery());
        result.setEvent(getEvent(null));
        return result;
    }

    /**
     * Creates a patient problem browser.
     *
     * @param patient the patient
     * @param layout  the layout context
     * @return a new {@link CRUDWindow}
     */
    protected Browser<Act> createProblemBrowser(Party patient, LayoutContext layout) {
        String[] shortNames = {PatientArchetypes.CLINICAL_PROBLEM};
        DefaultActQuery<Act> query = new DefaultActQuery<Act>(patient, "patient", PATIENT_PARTICIPATION, shortNames);
        query.setDefaultSortConstraint(DEFAULT_SORT);
        return BrowserFactory.create(query, layout);
    }

    /**
     * Creates a {@link CRUDWindow} for the problem browser.
     *
     * @param context the context
     * @param help    the help context
     * @return a new {@link CRUDWindow}
     */
    protected CRUDWindow<Act> createProblemRecordCRUDWindow(Context context, HelpContext help) {
        ProblemRecordCRUDWindow result = new ProblemRecordCRUDWindow(context, help);
        result.setEvent(getEvent(null));
        return result;
    }

    /**
     * Creates a patient reminder/alert browser.
     *
     * @param patient the patient
     * @param layout  the layout context
     * @return a new browser
     */
    protected Browser<Act> createReminderAlertBrowser(Party patient, LayoutContext layout) {
        // todo - should be able to register ReminderActTableModel in IMObjectTableFactory.properties for
        // act.patientReminder and act.patientAlert
        String[] shortNames = {ReminderArchetypes.REMINDER, PatientArchetypes.ALERT};
        DefaultActQuery<Act> query = new DefaultActQuery<Act>(patient, "patient", PATIENT_PARTICIPATION, shortNames,
                                                              STATUSES);
        query.setDefaultSortConstraint(DEFAULT_SORT);
        IMObjectTableModel<Act> model = new ReminderActTableModel(query.getShortNames(), layout);
        return new DefaultIMObjectTableBrowser<Act>(query, model, layout);
    }

    /**
     * Creates a {@link CRUDWindow} for the reminder/alert browser.
     *
     * @param context the context
     * @param help    the help context
     * @return a new {@link CRUDWindow}
     */
    protected CRUDWindow<Act> createReminderAlertCRUDWindow(Context context, HelpContext help) {
        return new ReminderCRUDWindow(patient, context, help);
    }

    /**
     * Creates a browser for patient documents.
     *
     * @param patient the patient
     * @param layout  the layout context
     * @return a new document browser
     */
    protected Browser<Act> createDocumentBrowser(Party patient, LayoutContext layout) {
        return BrowserFactory.create(new PatientDocumentQuery<Act>(patient), layout);
    }

    /**
     * Creates a {@link CRUDWindow} for the document browser.
     *
     * @param context the context
     * @param help    the help context
     * @return a new {@link CRUDWindow}
     */
    @SuppressWarnings("unchecked")
    protected CRUDWindow<Act> createDocumentCRUDWindow(Context context, HelpContext help) {
        return (CRUDWindow) new PatientDocumentCRUDWindow(docArchetypes, context, help);
    }

    /**
     * Creates a browser for patient charges.
     *
     * @param patient the patient
     * @param layout  the layout context
     * @return a new browser
     */
    protected Browser<Act> createChargeBrowser(Party patient, LayoutContext layout) {
        String[] statuses = {};
        DefaultActQuery<Act> query = new DefaultActQuery<Act>(patient, "patient", PATIENT_PARTICIPATION,
                                                              CHARGES_SHORT_NAMES, false, statuses);
        query.setDefaultSortConstraint(DEFAULT_SORT);
        query.setMaxResults(10);
        IMObjectTableModel<Act> chargeModel = new ChargesActTableModel(query.getShortNames(), layout);
        return new DefaultIMObjectTableBrowser<Act>(query, chargeModel, layout);
    }

    /**
     * Creates a {@link CRUDWindow} for the charges browser.
     *
     * @param context the context
     * @param help    the help context
     * @return a new {@link CRUDWindow}
     */
    protected CRUDWindow<Act> createChargesCRUDWindow(Context context, HelpContext help) {
        return new ChargesCRUDWindow(context, help);
    }

    /**
     * Creates a browser for prescriptions.
     *
     * @param patient the payment
     * @param layout  the layout context
     * @return a new browser
     */
    private Browser<Act> createPrescriptionBrowser(Party patient, LayoutContext layout) {
        return BrowserFactory.create(new PatientPrescriptionQuery(patient), layout);
    }

    /**
     * Creates a {@link CRUDWindow} for the prescriptions browser.
     *
     * @param context the context
     * @param help    the help context
     * @return a new {@link CRUDWindow}
     */
    protected CRUDWindow<Act> createPrescriptionCRUDWindow(Context context, HelpContext help) {
        return new PatientPrescriptionCRUDWindow(prescriptionArchetypes, context, help);
    }

}
