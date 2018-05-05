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

package org.openvpms.web.workspace.patient.mr;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextSwitchListener;
import org.openvpms.web.component.im.archetype.Archetypes;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.ActQuery;
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
import org.openvpms.web.workspace.customer.communication.CustomerAlertTableModel;
import org.openvpms.web.workspace.patient.communication.PatientCommunicationCRUDWindow;
import org.openvpms.web.workspace.patient.communication.PatientCommunicationQuery;
import org.openvpms.web.workspace.patient.history.AbstractPatientHistoryBrowser;
import org.openvpms.web.workspace.patient.history.PatientHistoryBrowser;
import org.openvpms.web.workspace.patient.history.PatientHistoryCRUDWindow;
import org.openvpms.web.workspace.patient.history.PatientHistoryQuery;
import org.openvpms.web.workspace.patient.insurance.InsuranceBrowser;
import org.openvpms.web.workspace.patient.insurance.InsuranceCRUDWindow;
import org.openvpms.web.workspace.patient.insurance.InsuranceQuery;
import org.openvpms.web.workspace.patient.problem.ProblemBrowser;
import org.openvpms.web.workspace.patient.problem.ProblemQuery;
import org.openvpms.web.workspace.patient.problem.ProblemRecordCRUDWindow;
import org.openvpms.web.workspace.patient.visit.VisitEditor;

import static org.openvpms.archetype.rules.patient.PatientArchetypes.PATIENT_PARTICIPATION;


/**
 * Patient record browser.
 * <p>
 * TODO - refactor along the lines of {@link VisitEditor}.
 *
 * @author Tim Anderson
 */
public class RecordBrowser extends TabbedBrowser<Act> {

    /**
     * The document archetypes.
     */
    private final Archetypes<DocumentAct> docArchetypes;

    /**
     * The prescription archetypes.
     */
    private final Archetypes<Act> prescriptionArchetypes;

    /**
     * The history browser.
     */
    private final PatientHistoryBrowser history;

    /**
     * The problem browser.
     */
    private final ProblemBrowser problems;

    /**
     * The communication act browser.
     */
    private final Browser<Act> communication;

    /**
     * The insurance act browser.
     */
    private final Browser<Act> insurance;

    /**
     * History browser tab index.
     */
    private final int historyIndex;

    /**
     * Problem browser tab index.
     */
    private final int problemIndex;

    /**
     * Reminder browser tab index.
     */
    private final int remindersIndex;

    /**
     * Alert browser tab index.
     */
    private final int alertsIndex;

    /**
     * Document browser tab index.
     */
    private final int documentsIndex;

    /**
     * Charges browser tab index.
     */
    private final int chargesIndex;

    /**
     * Prescription browser tab index.
     */
    private final int prescriptionIndex;

    /**
     * The communication browser tab index.
     */
    private final int communicationIndex;

    /**
     * The insurance browser tab index.
     */
    private final int insuranceIndex;

    /**
     * Patient charges shortnames supported by the workspace.
     */
    private static final String[] CHARGES_SHORT_NAMES = {CustomerAccountArchetypes.INVOICE_ITEM,
                                                         CustomerAccountArchetypes.CREDIT_ITEM};

    /**
     * The default sort constraint.
     */
    private static final SortConstraint[] DEFAULT_SORT
            = new SortConstraint[]{new NodeSortConstraint("startTime", false)};

    /**
     * The reminder statuses to query.
     */
    private static final ActStatuses REMINDER_STATUSES = new ActStatuses(ReminderArchetypes.REMINDER);

    /**
     * The alert statuses to query.
     */
    private static final ActStatuses ALERT_STATUSES = new ActStatuses(PatientArchetypes.ALERT);

    /**
     * The problem statuses to query.
     */
    private static final ActStatuses PROBLEM_STATUSES;

    /**
     * Constructs a {@link RecordBrowser}.
     *
     * @param query   the patient history query
     * @param context the context
     * @param help    the help context
     */
    public RecordBrowser(Party patient, PatientHistoryQuery query, Context context, HelpContext help) {
        docArchetypes = Archetypes.create(PatientDocumentQuery.DOCUMENT_SHORT_NAMES, DocumentAct.class,
                                          Messages.get("patient.document.createtype"));
        prescriptionArchetypes = Archetypes.create(PatientArchetypes.PRESCRIPTION, Act.class);

        LayoutContext layout = new DefaultLayoutContext(context, help);
        layout.setContextSwitchListener(new ContextSwitchListener() {
            @Override
            public void switchTo(IMObject object) {
                followHyperlink(object);
            }

            @Override
            public void switchTo(String shortName) {
            }
        });

        history = createHistoryBrowser(query, layout);
        historyIndex = addBrowser(Messages.get("button.summary"), history);
        problems = createProblemBrowser(patient, layout);
        problemIndex = addBrowser(Messages.get("button.problem"), problems);
        remindersIndex = addBrowser(Messages.get("button.reminder"), createReminderBrowser(patient, layout));
        alertsIndex = addBrowser(Messages.get("button.alert"), createAlertBrowser(patient, layout));
        documentsIndex = addBrowser(Messages.get("button.document"), createDocumentBrowser(patient, layout));
        chargesIndex = addBrowser(Messages.get("button.charges"), createChargeBrowser(patient, layout));
        prescriptionIndex = addBrowser(Messages.get("button.prescriptions"),
                                       createPrescriptionBrowser(patient, layout));
        communication = createCommunicationBrowser(patient, context, help);
        communicationIndex = addBrowser(Messages.get("button.communication"), communication);
        insurance = createInsuranceBrowser(patient, context, help.subtopic("insurance"));
        insuranceIndex = addBrowser(Messages.get("button.insurance"), insurance);
    }

    /**
     * Displays the history tab.
     */
    public void showHistory() {
        setSelectedBrowser(historyIndex);
    }

    /**
     * Displays the problems tab.
     */
    public void showProblems() {
        setSelectedBrowser(problemIndex);
    }

    /**
     * Displays the insurance tab.
     */
    public void showInsurance() {
        setSelectedBrowser(insuranceIndex);
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
            result = createReminderCRUDWindow(context, help);
        } else if (index == alertsIndex) {
            result = createAlertCRUDWindow(context, help);
        } else if (index == documentsIndex) {
            result = createDocumentCRUDWindow(context, help);
        } else if (index == chargesIndex) {
            result = createChargesCRUDWindow(context, help);
        } else if (index == prescriptionIndex) {
            result = createPrescriptionCRUDWindow(context, help);
        } else if (index == communicationIndex) {
            result = new PatientCommunicationCRUDWindow(context, help);
        } else if (index == insuranceIndex) {
            result = new InsuranceCRUDWindow(context, help.subtopic("insurance"));
        } else {
            result = createHistoryCRUDWindow(context, help);
        }
        return result;
    }

    /**
     * Returns the event associated with the current selected browser act.
     * <p>
     * Only applies if the history or problem browser is visible.
     *
     * @param act the current selected act. May be {@code null}
     * @return the event associated with the current selected act, or {@code null} if none is found
     */
    public Act getEvent(Act act) {
        int index = getSelectedBrowserIndex();
        AbstractPatientHistoryBrowser browser;
        if (index == historyIndex) {
            browser = history;
        } else if (index == problemIndex) {
            browser = problems;
        } else {
            browser = null;
        }
        if (browser != null) {
            if (act == null) {
                act = browser.getSelected();
            }
            return browser.getEvent(act);
        }
        return null;
    }

    /**
     * Returns the history browser.
     *
     * @return the history browser
     */
    public PatientHistoryBrowser getHistory() {
        return history;
    }

    /**
     * Returns the problems browser.
     *
     * @return te problems browser
     */
    public ProblemBrowser getProblems() {
        return problems;
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
        result.setQuery(history.getQuery());
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
    protected ProblemBrowser createProblemBrowser(Party patient, LayoutContext layout) {
        ProblemQuery query = new ProblemQuery(patient, layout.getPreferences());
        return new ProblemBrowser(query, layout);
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
        result.setQuery(problems.getQuery());
        return result;
    }

    /**
     * Creates a patient reminder browser.
     *
     * @param patient the patient
     * @param layout  the layout context
     * @return a new browser
     */
    protected Browser<Act> createReminderBrowser(Party patient, LayoutContext layout) {
        // todo - should be able to register ReminderActTableModel in IMObjectTableFactory.properties for
        DefaultActQuery<Act> query = new DefaultActQuery<>(patient, "patient", PATIENT_PARTICIPATION,
                                                           ReminderArchetypes.REMINDER, REMINDER_STATUSES);
        query.setStatus(ActStatus.IN_PROGRESS);
        query.setDefaultSortConstraint(DEFAULT_SORT);
        IMObjectTableModel<Act> model = new ReminderActTableModel(query.getShortNames(), layout);
        return new DefaultIMObjectTableBrowser<>(query, model, layout);
    }

    /**
     * Creates a {@link CRUDWindow} for the reminder browser.
     *
     * @param context the context
     * @param help    the help context
     * @return a new {@link CRUDWindow}
     */
    protected CRUDWindow<Act> createReminderCRUDWindow(Context context, HelpContext help) {
        return new ReminderCRUDWindow(context, help);
    }

    /**
     * Creates a patient alert browser.
     *
     * @param patient the patient
     * @param layout  the layout context
     * @return a new browser
     */
    protected Browser<Act> createAlertBrowser(Party patient, LayoutContext layout) {
        DefaultActQuery<Act> query = new DefaultActQuery<>(patient, "patient", PATIENT_PARTICIPATION,
                                                           PatientArchetypes.ALERT, ALERT_STATUSES);
        query.setStatus(ActStatus.IN_PROGRESS);
        query.setDefaultSortConstraint(DEFAULT_SORT);
        IMObjectTableModel<Act> model = new CustomerAlertTableModel(query.getShortNames(), layout);
        return new DefaultIMObjectTableBrowser<>(query, model, layout);
    }

    /**
     * Creates a {@link CRUDWindow} for the alert browser.
     *
     * @param context the context
     * @param help    the help context
     * @return a new {@link CRUDWindow}
     */
    protected CRUDWindow<Act> createAlertCRUDWindow(Context context, HelpContext help) {
        return new AlertCRUDWindow(context, help);
    }

    /**
     * Creates a browser for patient documents.
     *
     * @param patient the patient
     * @param layout  the layout context
     * @return a new document browser
     */
    protected Browser<Act> createDocumentBrowser(Party patient, LayoutContext layout) {
        return BrowserFactory.create(new PatientDocumentQuery<>(patient), layout);
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
        DefaultActQuery<Act> query = new DefaultActQuery<>(patient, "patient", PATIENT_PARTICIPATION,
                                                           CHARGES_SHORT_NAMES, false, statuses);
        query.setDefaultSortConstraint(DEFAULT_SORT);
        query.setMaxResults(10);
        IMObjectTableModel<Act> chargeModel = new ChargesActTableModel(query.getShortNames(), layout);
        return new DefaultIMObjectTableBrowser<>(query, chargeModel, layout);
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
     * Creates a {@link CRUDWindow} for the prescriptions browser.
     *
     * @param context the context
     * @param help    the help context
     * @return a new {@link CRUDWindow}
     */
    protected CRUDWindow<Act> createPrescriptionCRUDWindow(Context context, HelpContext help) {
        return new PatientPrescriptionCRUDWindow(prescriptionArchetypes, context, help.subtopic("prescription"));
    }

    /**
     * Creates the communication browser.
     *
     * @param patient the patient to browse communication logs for
     * @param context the context
     * @param help    the help context
     * @return a new browser
     */
    protected Browser<Act> createCommunicationBrowser(Party patient, Context context, HelpContext help) {
        ActQuery<Act> query = new PatientCommunicationQuery(patient);
        return BrowserFactory.create(query, new DefaultLayoutContext(context, help));
    }

    /**
     * Creates the insurance browser.
     *
     * @param patient the patient to browse insurance for
     * @param context the context
     * @param help    the help context
     * @return a new browser
     */
    protected Browser<Act> createInsuranceBrowser(Party patient, Context context, HelpContext help) {
        ActQuery<Act> query = new InsuranceQuery(patient);
        return new InsuranceBrowser(query, new DefaultLayoutContext(context, help));
    }

    /**
     * Invoked when a browser is selected.
     * <p>
     * This notifies any registered listener.
     *
     * @param selected the selected index
     */
    @Override
    protected void onBrowserSelected(int selected) {
        super.onBrowserSelected(selected);
        if (selected == communicationIndex) {
            query(communication); // always refresh
        }
    }

    /**
     * Follow a hyperlink.
     * <p>
     * If the object is a:
     * <ul>
     * <li>problem, the Problems tab will be shown, and the problem selected</li>
     * <li>event, the Summary tab will be shown, and the event selected</li>
     * </ul>
     *
     * @param object the object to display
     */
    protected void followHyperlink(IMObject object) {
        if (TypeHelper.isA(object, PatientArchetypes.CLINICAL_PROBLEM)) {
            showProblem((Act) object);
        } else if (TypeHelper.isA(object, PatientArchetypes.CLINICAL_EVENT)) {
            showEvent((Act) object);
        }
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
     * Switches to the history browser, selecting an event.
     *
     * @param object the event
     */
    private void showEvent(Act object) {
        showHistory();
        history.setSelected(object, true);
    }

    /**
     * Switches to the problem browser, selecting a problem.
     *
     * @param object the problem
     */
    private void showProblem(Act object) {
        showProblems();
        problems.setSelected(object, true);
    }

    static {
        PROBLEM_STATUSES = new ActStatuses(PatientArchetypes.CLINICAL_PROBLEM);
        PROBLEM_STATUSES.setDefault((String) null);
    }

}
