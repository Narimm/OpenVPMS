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

package org.openvpms.web.workspace.patient.visit;

import echopointng.TabbedPane;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ChangeEvent;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.retry.AbstractRetryable;
import org.openvpms.web.component.retry.Retryer;
import org.openvpms.web.component.workspace.AbstractCRUDWindow;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.echo.button.ButtonSet;
import org.openvpms.web.echo.event.ChangeListener;
import org.openvpms.web.echo.event.VetoListener;
import org.openvpms.web.echo.event.Vetoable;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.TabbedPaneFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.tabpane.ObjectTabPaneModel;
import org.openvpms.web.echo.tabpane.VetoableSingleSelectionModel;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.charge.VisitChargeEditor;
import org.openvpms.web.workspace.patient.history.AbstractPatientHistoryCRUDWindow;
import org.openvpms.web.workspace.patient.history.PatientHistoryBrowser;
import org.openvpms.web.workspace.patient.history.PatientHistoryQuery;
import org.openvpms.web.workspace.patient.history.PatientHistoryQueryFactory;
import org.openvpms.web.workspace.patient.mr.PatientDocumentQuery;
import org.openvpms.web.workspace.patient.problem.ProblemBrowser;
import org.openvpms.web.workspace.patient.problem.ProblemQuery;
import org.openvpms.web.workspace.patient.problem.ProblemRecordCRUDWindow;


/**
 * The visit editor.
 *
 * @author Tim Anderson
 */
public class VisitEditor {

    /**
     * The id of the patient history tab.
     */
    public static final int HISTORY_TAB = 0;

    /**
     * The id of the patient problem tab.
     */
    public static final int PROBLEM_TAB = 1;

    /**
     * The id of the invoice tab.
     */
    public static final int INVOICE_TAB = 2;

    /**
     * The id of the reminders/alerts tab.
     */
    public static final int REMINDER_TAB = 3;

    /**
     * The id of the document tab.
     */
    public static final int DOCUMENT_TAB = 4;

    /**
     * The id of the prescription tab.
     */
    public static final int PRESCRIPTION_TAB = 5;

    /**
     * The id of the estimates tab.
     */
    public static final int ESTIMATE_TAB = 6;

    /**
     * The CRUD window for editing events and their items.
     */
    private final VisitHistoryBrowserCRUDWindow historyWindow;

    /**
     * The event.
     */
    private final Act event;

    /**
     * The patient history query.
     */
    private final PatientHistoryQuery query;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The help context.
     */
    private final HelpContext help;

    /**
     * The problem CRUD window, or {@code null} if problem view is disabled.
     */
    private BrowserCRUDWindow<Act> problemWindow;

    /**
     * The invoice CRUD window.
     */
    private final VisitChargeCRUDWindow chargeWindow;

    /**
     * The reminders/alerts CRUD window.
     */
    private BrowserCRUDWindow<Act> reminderWindow;

    /**
     * The patient document browser window.
     */
    private BrowserCRUDWindow<DocumentAct> documentWindow;

    /**
     * The prescription CRUD window.
     */
    private PrescriptionBrowserCRUDWindow prescriptionWindow;

    /**
     * The estimates CRUD window.
     */
    private EstimateBrowserCRUDWindow estimateWindow;

    /**
     * The listener to notify of visit browser events. May be {@code null}
     */
    private VisitEditorListener listener;

    /**
     * The patient.
     */
    private final Party patient;

    /**
     * The container.
     */
    private Component container;

    /**
     * The tabbed pane.
     */
    private TabbedPane tabbedPane;

    /**
     * The focus group.
     */
    private FocusGroup focusGroup = new FocusGroup(getClass().getName());


    /**
     * Constructs a {@code VisitEditor}.
     *
     * @param customer the customer
     * @param patient  the patient
     * @param event    the <em>act.patientClinicalEvent</em>
     * @param invoice  the invoice
     * @param context  the context
     * @param help     the help context
     */
    public VisitEditor(Party customer, Party patient, Act event, FinancialAct invoice, Context context,
                       HelpContext help) {
        this.patient = patient;
        this.event = event;
        this.context = context;
        this.help = help;

        query = PatientHistoryQueryFactory.create(patient, context.getPractice());
        query.setAllDates(true);
        query.setFrom(event.getActivityStartTime());
        query.setTo(DateRules.getDate(event.getActivityStartTime(), 1, DateUnits.DAYS));

        historyWindow = createHistoryBrowserCRUDWindow(context);
        historyWindow.setEvent(event);
        historyWindow.setSelected(event);

        if (showProblems(context)) {
            problemWindow = createProblemBrowserCRUDWindow(context);
        }

        chargeWindow = createVisitChargeCRUDWindow(event, context);
        chargeWindow.setObject(invoice);

        reminderWindow = createReminderCRUDWindow(context);

        documentWindow = createDocumentBrowserCRUDWindow(context);

        prescriptionWindow = createPrescriptionCRUDWindow(context);

        estimateWindow = createEstimateBrowserCRUDWindow(customer, patient, context, help);
    }

    /**
     * Returns the <em>act.patientClinicalEvent</em>.
     *
     * @return the event
     */
    public Act getEvent() {
        return event;
    }

    /**
     * Returns the patient history browser.
     *
     * @return the patient history browser
     */
    public PatientHistoryBrowser getHistoryBrowser() {
        return historyWindow.getBrowser();
    }

    /**
     * Returns the patient history CRUD window.
     *
     * @return the history CRUD window
     */
    public AbstractPatientHistoryCRUDWindow getHistoryWindow() {
        return historyWindow.getWindow();
    }

    /**
     * Returns the problem CRUD window.
     *
     * @return the problem CRUD window, or {@code null} if it has been suppressed
     */
    public ProblemRecordCRUDWindow getProblemWindow() {
        return problemWindow != null ? (ProblemRecordCRUDWindow) problemWindow.getWindow() : null;
    }

    /**
     * Returns the charge window.
     *
     * @return the charge window
     */
    public VisitChargeCRUDWindow getCharge() {
        return chargeWindow;
    }

    /**
     * Returns the charge editor.
     *
     * @return the charge editor. May be {@code null}
     */
    public VisitChargeEditor getChargeEditor() {
        return chargeWindow.getEditor();
    }

    /**
     * Selects the charges tab.
     */
    public void selectCharges() {
        selectTab(INVOICE_TAB);
    }

    /**
     * Sets the buttons for the current tab.
     *
     * @param buttons the buttons
     */
    public void setButtons(ButtonSet buttons) {
        CRUDWindow<? extends Act> window = getWindow(tabbedPane.getSelectedIndex());
        if (window instanceof AbstractCRUDWindow) {
            ((AbstractCRUDWindow) window).setButtons(buttons);
        }
    }

    /**
     * Returns the patient history query.
     *
     * @return the patient history query
     */
    public PatientHistoryQuery getQuery() {
        return query;
    }

    /**
     * Registers a listener for visit browser events.
     *
     * @param listener the listener. May be {@code null}
     */
    public void setListener(VisitEditorListener listener) {
        this.listener = listener;
    }

    /**
     * Returns the help context for the selected tab.
     *
     * @return the help context
     */
    public HelpContext getHelpContext() {
        HelpContext result = getBaseHelpContext();
        CRUDWindow<? extends Act> window = getWindow(tabbedPane.getSelectedIndex());
        if (window != null) {
            result = window.getHelpContext();
        }
        return result;
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        if (container == null) {
            container = ColumnFactory.create("InsetY");
            ObjectTabPaneModel<VisitEditorTab> model = new ObjectTabPaneModel<VisitEditorTab>(container);
            addTabs(model);
            tabbedPane = TabbedPaneFactory.create(model);
            tabbedPane.setStyleName("VisitEditor.TabbedPane");
            VetoableSingleSelectionModel selectionModel = new VetoableSingleSelectionModel();
            tabbedPane.setSelectionModel(selectionModel);
            selectionModel.setVetoListener(new VetoListener() {
                @Override
                public void onVeto(Vetoable action) {
                    VetoableSingleSelectionModel.Change change = (VetoableSingleSelectionModel.Change) action;
                    action.veto(!switchTabs(change.getOldIndex(), change.getNewIndex()));
                }
            });
            tabbedPane.getSelectionModel().addChangeListener(new ChangeListener() {
                @Override
                public void onChange(ChangeEvent event) {
                    onTabSelected(tabbedPane.getSelectedIndex());
                }
            });
            focusGroup.add(tabbedPane);
            container.add(tabbedPane);
            tabbedPane.setSelectedIndex(0);
            historyWindow.getBrowser().setFocusOnResults();
        }
        return container;
    }

    /**
     * Saves the invoice.
     *
     * @return {@code true} if the invoice was saved
     */
    public boolean save() {
        boolean saved = chargeWindow.save();
        if (saved) {
            updateVisitStatus();
        }
        return saved;
    }

    /**
     * Marks the charge IN_PROGRESS and saves it.
     *
     * @return {@code true} if the charge was updated
     */
    public boolean saveAsInProgress() {
        boolean saved = chargeWindow.inProgress();
        if (saved) {
            updateVisitStatus();
        }
        return saved;
    }

    /**
     * Marks the charge COMPLETED and saves it.
     *
     * @return {@code true} if the charge was updated
     */
    public boolean saveAsCompleted() {
        boolean saved = chargeWindow.complete();
        if (saved) {
            updateVisitStatus();
        }
        return saved;
    }

    /**
     * Returns the patient.
     *
     * @return the patient
     */
    public Party getPatient() {
        return patient;
    }

    /**
     * Returns the context.
     *
     * @return the context
     */
    protected Context getContext() {
        return context;
    }

    /**
     * Returns the base help context for the editor.
     *
     * @return the help context
     */
    protected HelpContext getBaseHelpContext() {
        return help;
    }

    /**
     * Selects the specified tab.
     *
     * @param index the tab model index
     */
    protected void selectTab(int index) {
        tabbedPane.setSelectedIndex(getTabIndex(index));
    }

    /**
     * Returns the {@link CRUDWindow} associated with the tab index.
     *
     * @param index the tab index
     * @return the corresponding {@link CRUDWindow} or {@code null} if none is found
     */
    protected CRUDWindow<? extends Act> getWindow(int index) {
        VisitEditorTab tab = getModel().getObject(index);
        return (tab != null) ? tab.getWindow() : null;
    }

    /**
     * Returns the tab model index, given its position index.
     *
     * @param tabIndex the tab position index
     * @return the tab model index
     */
    protected int getModelIndex(int tabIndex) {
        VisitEditorTab tab = getModel().getObject(tabIndex);
        return tab != null ? tab.getId() : -1;
    }

    /**
     * Returns the tab position index, given its model index.
     *
     * @param modelIndex the tab model index
     * @return the tab position index, or {@code -1} if the model index is not found
     */
    protected int getTabIndex(int modelIndex) {
        ObjectTabPaneModel<VisitEditorTab> model = getModel();
        for (int i = 0; i < model.size(); ++i) {
            VisitEditorTab tab = model.getObject(i);
            if (tab != null && tab.getId() == modelIndex) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Creates a new visit browser CRUD window.
     *
     * @param context the context
     * @return a new visit browser CRUD window
     */
    protected VisitHistoryBrowserCRUDWindow createHistoryBrowserCRUDWindow(Context context) {
        VisitHistoryBrowserCRUDWindow result = new VisitHistoryBrowserCRUDWindow(
                query, context, help.subtopic("summary"));
        result.setId(HISTORY_TAB);
        return result;
    }

    /**
     * Determines if the problems tab is displayed.
     *
     * @param context the context
     * @return {@code true} if the problems tab should be displayed
     */
    protected boolean showProblems(Context context) {
        IMObjectBean bean = new IMObjectBean(context.getPractice());
        return bean.getBoolean("showProblemsInVisit");
    }

    /**
     * Creates a new problem browser CRUD window.
     *
     * @param context the context
     */
    protected BrowserCRUDWindow<Act> createProblemBrowserCRUDWindow(Context context) {
        ProblemQuery query = new ProblemQuery(context.getPatient());
        ProblemBrowser browser = new ProblemBrowser(query, new DefaultLayoutContext(context, help));
        BrowserCRUDWindow<Act> result = new BrowserCRUDWindow<Act>(browser, new ProblemRecordCRUDWindow(context, help));
        result.setId(PROBLEM_TAB);
        return result;
    }

    /**
     * Creates a new visit charge CRUD window.
     *
     * @param event   the event
     * @param context the context
     * @return a new visit charge CRUD window
     */
    protected VisitChargeCRUDWindow createVisitChargeCRUDWindow(Act event, Context context) {
        VisitChargeCRUDWindow result = new VisitChargeCRUDWindow(event, context, help.subtopic("invoice"));
        result.setId(INVOICE_TAB);
        return result;
    }

    /**
     * Creates a window to view reminders.
     *
     * @param context the context
     * @return a new window
     */
    protected ReminderBrowserCRUDWindow createReminderCRUDWindow(Context context) {
        ReminderBrowserCRUDWindow result = new ReminderBrowserCRUDWindow(patient, context, help.subtopic("reminder"));
        result.setId(REMINDER_TAB);
        return result;
    }

    /**
     * Creates a window to view patient documents.
     *
     * @param context the context
     * @return a new window
     */
    protected BrowserCRUDWindow<DocumentAct> createDocumentBrowserCRUDWindow(Context context) {
        Query<DocumentAct> query = new PatientDocumentQuery<DocumentAct>(patient);
        Browser<DocumentAct> browser = BrowserFactory.create(query, new DefaultLayoutContext(context, help));
        VisitDocumentCRUDWindow window = new VisitDocumentCRUDWindow(context, help.subtopic("document"));
        BrowserCRUDWindow<DocumentAct> result = new BrowserCRUDWindow<DocumentAct>(browser, window);
        result.setId(DOCUMENT_TAB);
        return result;
    }

    /**
     * Creates a window to view prescriptions.
     *
     * @param context the context
     * @return a new window
     */
    protected PrescriptionBrowserCRUDWindow createPrescriptionCRUDWindow(Context context) {
        PrescriptionBrowserCRUDWindow result = new PrescriptionBrowserCRUDWindow(patient, context,
                                                                                 help.subtopic("prescription"));
        result.setVisitEditor(this);
        result.setId(PRESCRIPTION_TAB);
        return result;
    }

    /**
     * Creates a new window to view estimates.
     *
     * @param customer the customer
     * @param patient  the patient
     * @param context  the context
     * @param help     the help context
     * @return a new window
     */
    protected EstimateBrowserCRUDWindow createEstimateBrowserCRUDWindow(Party customer, Party patient, Context context,
                                                                        HelpContext help) {
        EstimateBrowserCRUDWindow result = new EstimateBrowserCRUDWindow(customer, patient, this, context,
                                                                         help.subtopic("estimate"));
        result.setId(ESTIMATE_TAB);
        return result;
    }

    /**
     * Adds the visit editor tabs to the tab pane model.
     * <p/>
     * This implementation adds tabs for history, invoice, reminder, document, prescription and estimates.
     *
     * @param model the model to add to
     */
    protected void addTabs(ObjectTabPaneModel<VisitEditorTab> model) {
        addPatientHistoryTab(model);
        if (problemWindow != null) {
            addProblemTab(model);
        }
        addInvoiceTab(model);
        addRemindersAlertsTab(model);
        addDocumentsTab(model);
        addPrescriptionsTab(model);
        addEstimatesTab(model);
    }

    /**
     * Helper to add a tab to the tab pane.
     *
     * @param button the button key
     * @param model  the tab model
     * @param tab    the component
     * @param id     the tab identifier. This remains unchanged if the tab moves
     */
    protected void addTab(String button, ObjectTabPaneModel<VisitEditorTab> model, VisitEditorTab tab, int id) {
        int index = model.size();
        int shortcut = index + 1;
        String text = "&" + shortcut + " " + Messages.get(button);
        model.addTab(tab, text, tab.getComponent());
        tab.setId(id);
    }

    /**
     * Invoked when a tab is selected.
     *
     * @param selected the selected tab index
     */
    protected void onTabSelected(int selected) {
        ObjectTabPaneModel<VisitEditorTab> model = getModel();
        VisitEditorTab tab = model.getObject(selected);
        if (tab != null) {
            tab.show();
            notifyListener(tab.getId());
        }
    }

    /**
     * Returns the tab pane model.
     *
     * @return the model
     */
    @SuppressWarnings("unchecked")
    protected ObjectTabPaneModel<VisitEditorTab> getModel() {
        return (ObjectTabPaneModel<VisitEditorTab>) tabbedPane.getModel();
    }

    /**
     * Notify the listener of a tab selection.
     *
     * @param index the tab index
     */
    protected void notifyListener(int index) {
        if (listener != null) {
            listener.selected(index);
        }
    }

    /**
     * Adds a tab to display/edit the patient history.
     *
     * @param model the tab pane model to add to
     */
    protected void addPatientHistoryTab(ObjectTabPaneModel<VisitEditorTab> model) {
        addTab("button.summary", model, historyWindow, HISTORY_TAB);
    }

    /**
     * Adds a tab to display/edit the problems.
     *
     * @param model the tab pane model to add to
     */
    protected void addProblemTab(ObjectTabPaneModel<VisitEditorTab> model) {
        addTab("button.problem", model, problemWindow, PROBLEM_TAB);
    }

    /**
     * Adds a tab to display/edit the invoice.
     *
     * @param model the tab pane model to add to
     */
    protected void addInvoiceTab(ObjectTabPaneModel<VisitEditorTab> model) {
        addTab("button.invoice", model, chargeWindow, INVOICE_TAB);
    }

    /**
     * Adds a tab to display reminders and alerts.
     *
     * @param model the tab pane model to add to
     */
    protected void addRemindersAlertsTab(ObjectTabPaneModel<VisitEditorTab> model) {
        addTab("button.reminder", model, reminderWindow, REMINDER_TAB);
    }

    /**
     * Adds a tab to display documents.
     *
     * @param model the tab pane model to add to
     */
    protected void addDocumentsTab(ObjectTabPaneModel<VisitEditorTab> model) {
        addTab("button.document", model, documentWindow, DOCUMENT_TAB);
    }

    /**
     * Adds a tab to display prescriptions.
     *
     * @param model the tab pane model to add to
     */
    protected void addPrescriptionsTab(ObjectTabPaneModel<VisitEditorTab> model) {
        addTab("button.prescriptions", model, prescriptionWindow, PRESCRIPTION_TAB);
    }

    /**
     * Adds a tab to display estimates.
     *
     * @param model the tab pane model to add to
     */
    protected void addEstimatesTab(ObjectTabPaneModel<VisitEditorTab> model) {
        addTab("button.estimates", model, estimateWindow, ESTIMATE_TAB);
    }

    /**
     * Returns the estimate window.
     *
     * @return the estimate window
     */
    protected EstimateBrowserCRUDWindow getEstimateWindow() {
        return estimateWindow;
    }


    protected boolean switchTabs(int oldIndex, int newIndex) {
        return true;
    }

    /**
     * Updates the visit status based on the charge status.
     */
    private void updateVisitStatus() {
        Retryer.run(new VisitStatusUpdater());
    }

    private class VisitStatusUpdater extends AbstractRetryable {

        /**
         * Runs the action.
         *
         * @return {@code true} if the action completed successfully, {@code false} if it failed, and should not be
         *         retried
         * @throws RuntimeException if the action fails and may be retried
         */
        @Override
        protected boolean runAction() {
            Act act = IMObjectHelper.reload(event);
            return (act != null) && updateStatus(act);
        }

        /**
         * Updates the visit status based on that of the charge.
         *
         * @param event the visit
         * @return {@code true}
         */
        private boolean updateStatus(Act event) {
            String status = chargeWindow.getObject().getStatus();
            String newStatus = null;
            if (FinancialActStatus.ON_HOLD.equals(status) || ActStatus.IN_PROGRESS.equals(status)) {
                newStatus = ActStatus.IN_PROGRESS;
            } else if (ActStatus.POSTED.equals(status) || ActStatus.COMPLETED.equals(status)) {
                newStatus = ActStatus.COMPLETED;
            }
            if (newStatus != null && !status.equals(event.getStatus())) {
                event.setStatus(newStatus);
                ServiceHelper.getArchetypeService().save(event);
            }
            return true;
        }
    }
}
