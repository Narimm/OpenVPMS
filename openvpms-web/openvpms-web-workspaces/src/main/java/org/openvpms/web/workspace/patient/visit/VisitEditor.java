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

package org.openvpms.web.workspace.patient.visit;

import echopointng.TabbedPane;
import echopointng.tabbedpane.DefaultTabModel;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.event.ChangeEvent;
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.archetype.rules.act.FinancialActStatus;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
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
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.TabbedPaneFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.tabpane.TabPaneModel;
import org.openvpms.web.resource.i18n.Messages;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.patient.charge.VisitChargeEditor;
import org.openvpms.web.workspace.patient.history.PatientHistoryBrowser;
import org.openvpms.web.workspace.patient.history.PatientHistoryCRUDWindow;
import org.openvpms.web.workspace.patient.history.PatientHistoryQuery;
import org.openvpms.web.workspace.patient.history.PatientHistoryQueryFactory;
import org.openvpms.web.workspace.patient.mr.PatientDocumentQuery;


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
     * The id of the invoice tab.
     */
    public static final int INVOICE_TAB = 1;

    /**
     * The id of the reminders/alerts tab.
     */
    public static final int REMINDER_TAB = 2;

    /**
     * The id of the document tab.
     */
    public static final int DOCUMENT_TAB = 3;

    /**
     * The id of the prescription tab.
     */
    public static final int PRESCRIPTION_TAB = 4;

    /**
     * The id of the estimates tab.
     */
    public static final int ESTIMATE_TAB = 5;

    /**
     * The CRUD window for editing events and their items.
     */
    private final VisitBrowserCRUDWindow visitWindow;

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
    private TabbedPane tab;

    /**
     * The focus group.
     */
    private FocusGroup focusGroup = new FocusGroup(getClass().getName());

    /**
     * Determines if the documents have been queried.
     */
    private boolean documentsQueried;

    /**
     * History tab index.
     */
    private int historyIndex;

    /**
     * Invoice tab index.
     */
    private int invoiceIndex;

    /**
     * Reminder tab index.
     */
    private int reminderIndex;

    /**
     * Document tab index.
     */
    private int documentIndex;

    /**
     * Prescription tab index.
     */
    private int prescriptionIndex;

    /**
     * Estimate tab index.
     */
    private int estimateIndex;


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

        visitWindow = createVisitBrowserCRUDWindow(context);
        visitWindow.setSelected(event);

        chargeWindow = createVisitChargeCRUDWindow(event, context);
        chargeWindow.setObject(invoice);

        reminderWindow = new ReminderBrowserCRUDWindow(patient, context, help.subtopic("reminder"));

        documentWindow = createDocumentBrowserCRUDWindow(context);

        prescriptionWindow = new PrescriptionBrowserCRUDWindow(patient, context, help.subtopic("prescription"));

        estimateWindow = new EstimateBrowserCRUDWindow(customer, patient, this, context, help.subtopic("estimate"));
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
        return visitWindow.getBrowser();
    }

    /**
     * Returns the patient history CRUD window.
     *
     * @return the history CRUD window
     */
    public PatientHistoryCRUDWindow getHistory() {
        return visitWindow.getWindow();
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
        int index = getModelIndex(tab.getSelectedIndex());
        CRUDWindow<? extends Act> window = getWindow(index);
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
        int index = getModelIndex(tab.getSelectedIndex());
        CRUDWindow<? extends Act> window = getWindow(index);
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
            TabPaneModel model = new TabPaneModel(container);
            addTabs(model);
            tab = TabbedPaneFactory.create(model);
            tab.setStyleName("VisitEditor.TabbedPane");
            tab.getSelectionModel().addChangeListener(new ChangeListener() {
                @Override
                public void onChange(ChangeEvent event) {
                    onTabSelected(getModelIndex(tab.getSelectedIndex()));
                }
            });
            focusGroup.add(tab);
            container.add(tab);
            tab.setSelectedIndex(0);
            visitWindow.getBrowser().setFocusOnResults();
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
        tab.setSelectedIndex(getTabIndex(index));
    }

    /**
     * Returns the {@link CRUDWindow} associated with the tab index.
     *
     * @param index the tab model index
     * @return the corresponding {@link CRUDWindow} or {@code null} if none is found
     */
    protected CRUDWindow<? extends Act> getWindow(int index) {
        switch (index) {
            case HISTORY_TAB:
                return visitWindow.getWindow();
            case INVOICE_TAB:
                return chargeWindow;
            case REMINDER_TAB:
                return reminderWindow.getWindow();
            case DOCUMENT_TAB:
                return documentWindow.getWindow();
            case PRESCRIPTION_TAB:
                return prescriptionWindow.getWindow();
            case ESTIMATE_TAB:
                return estimateWindow.getWindow();
        }
        return null;
    }

    /**
     * Returns the tab model index, given its position index.
     *
     * @param tabIndex the tab position index
     * @return the tab model index
     */
    protected int getModelIndex(int tabIndex) {
        if (tabIndex == historyIndex) {
            return HISTORY_TAB;
        } else if (tabIndex == invoiceIndex) {
            return INVOICE_TAB;
        } else if (tabIndex == reminderIndex) {
            return REMINDER_TAB;
        } else if (tabIndex == documentIndex) {
            return DOCUMENT_TAB;
        } else if (tabIndex == prescriptionIndex) {
            return PRESCRIPTION_TAB;
        } else if (tabIndex == estimateIndex) {
            return ESTIMATE_TAB;
        }
        return -1;
    }

    /**
     * Returns the tab position index, given its model index.
     *
     * @param modelIndex the tab model index
     * @return the tab position index
     */
    protected int getTabIndex(int modelIndex) {
        switch (modelIndex) {
            case HISTORY_TAB:
                return historyIndex;
            case INVOICE_TAB:
                return invoiceIndex;
            case REMINDER_TAB:
                return reminderIndex;
            case DOCUMENT_TAB:
                return documentIndex;
            case PRESCRIPTION_TAB:
                return prescriptionIndex;
            case ESTIMATE_TAB:
                return estimateIndex;
        }
        return -1;
    }

    /**
     * Creates a new visit browser CRUD window.
     *
     * @param context the context
     * @return a new visit browser CRUD window
     */
    protected VisitBrowserCRUDWindow createVisitBrowserCRUDWindow(Context context) {
        return new VisitBrowserCRUDWindow(query, context, help.subtopic("summary"));
    }

    /**
     * Creates a new visit charge CRUD window.
     *
     * @param event   the event
     * @param context the context
     * @return a new visit charge CRUD window
     */
    protected VisitChargeCRUDWindow createVisitChargeCRUDWindow(Act event, Context context) {
        return new VisitChargeCRUDWindow(event, context, help.subtopic("invoice"));
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
        return new BrowserCRUDWindow<DocumentAct>(browser, window);
    }

    /**
     * Adds the history, invoice, reminder, document, prescription and estimates tabs to the tab pane model.
     *
     * @param model the model to add to
     */
    protected void addTabs(TabPaneModel model) {
        addPatientHistoryTab(model);
        addInvoiceTab(model);
        addRemindersAlertsTab(model);
        addDocumentsTab(model);
        addPrescriptionsTab(model);
        addEstimatesTab(model);
    }

    /**
     * Helper to add a browser to the tab pane.
     *
     * @param button    the button key
     * @param model     the tab model
     * @param component the component
     * @return the tab index
     */
    protected int addTab(String button, DefaultTabModel model, Component component) {
        int index = model.size();
        int shortcut = index + 1;
        String text = "&" + shortcut + " " + Messages.get(button);
        model.addTab(text, component);
        return index;
    }

    /**
     * Invoked when a tab is selected.
     *
     * @param selected the selected tab model index
     */
    protected void onTabSelected(int selected) {
        switch (selected) {
            case HISTORY_TAB:
                onHistorySelected();
                break;
            case INVOICE_TAB:
                onInvoiceSelected();
                break;
            case REMINDER_TAB:
                onRemindersSelected();
                break;
            case DOCUMENT_TAB:
                onDocumentsSelected();
                break;
            case PRESCRIPTION_TAB:
                onPrescriptionsSelected();
                break;
            case ESTIMATE_TAB:
                onEstimatesSelected();
                break;
        }
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
    protected void addPatientHistoryTab(TabPaneModel model) {
        // need to add the browser to a ContentPane to get scrollbars
        ContentPane pane = new ContentPane();
        pane.add(visitWindow.getComponent());
        historyIndex = addTab("button.summary", model, pane);
    }

    /**
     * Adds a tab to display/edit the invoice.
     *
     * @param model the tab pane model to add to
     */
    protected void addInvoiceTab(TabPaneModel model) {
        // need to add the window to a ContentPane to get scrollbars
        ContentPane pane = new ContentPane();
        pane.add(chargeWindow.getComponent());
        invoiceIndex = addTab("button.invoice", model, pane);
    }

    /**
     * Adds a tab to display reminders and alerts.
     *
     * @param model the tab pane model to add to
     */
    protected void addRemindersAlertsTab(TabPaneModel model) {
        reminderIndex = addTab("button.reminder", model, reminderWindow.getComponent());
    }

    /**
     * Adds a tab to display documents.
     *
     * @param model the tab pane model to add to
     */
    protected void addDocumentsTab(TabPaneModel model) {
        documentIndex = addTab("button.document", model, documentWindow.getComponent());
    }

    /**
     * Adds a tab to display prescriptions.
     *
     * @param model the tab pane model to add to
     */
    protected void addPrescriptionsTab(TabPaneModel model) {
        prescriptionIndex = addTab("button.prescriptions", model, prescriptionWindow.getComponent());
    }

    /**
     * Adds a tab to display estimates.
     *
     * @param model the tab pane model to add to
     */
    protected void addEstimatesTab(TabPaneModel model) {
        estimateIndex = addTab("button.estimates", model, getEstimateWindow().getComponent());
    }

    /**
     * Returns the estimate window.
     *
     * @return the estimate window
     */
    protected EstimateBrowserCRUDWindow getEstimateWindow() {
        return estimateWindow;
    }

    /**
     * Invoked when the patient history tab is selected.
     * <p/>
     * This refreshes the history if the current event being displayed.
     */
    protected void onHistorySelected() {
        Browser<Act> browser = visitWindow.getBrowser();
        if (browser.getObjects().contains(event)) {
            Act selected = browser.getSelected();
            browser.query();
            browser.setSelected(selected);
        }
        browser.setFocusOnResults();
        notifyListener(HISTORY_TAB);
    }

    /**
     * Invoked when the invoice tab is selected.
     */
    protected void onInvoiceSelected() {
        VisitChargeEditor editor = chargeWindow.getEditor();
        if (editor != null) {
            editor.getFocusGroup().setFocus();
        }
        notifyListener(INVOICE_TAB);
    }

    /**
     * Invoked when the reminders tab is selected.
     */
    protected void onRemindersSelected() {
        reminderWindow.getBrowser().setFocusOnResults();
        notifyListener(REMINDER_TAB);
    }

    /**
     * Invoked when the documents tab is selected.
     */
    protected void onDocumentsSelected() {
        if (!documentsQueried) {
            documentWindow.getBrowser().query();
            documentsQueried = true;
        } else {
            documentWindow.getBrowser().setFocusOnResults();
        }
        notifyListener(DOCUMENT_TAB);
    }

    /**
     * Invoked when the prescriptions tab is selected.
     */
    protected void onPrescriptionsSelected() {
        prescriptionWindow.getBrowser().setFocusOnResults();
        prescriptionWindow.setChargeEditor(getChargeEditor());
        notifyListener(PRESCRIPTION_TAB);
    }

    /**
     * Invoked when the estimates tab is selected.
     */
    protected void onEstimatesSelected() {
        estimateWindow.getBrowser().setFocusOnResults();
        notifyListener(ESTIMATE_TAB);
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
