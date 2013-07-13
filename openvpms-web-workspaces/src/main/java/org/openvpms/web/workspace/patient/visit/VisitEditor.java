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
import org.openvpms.web.workspace.patient.history.PatientHistoryQuery;
import org.openvpms.web.workspace.patient.history.PatientHistoryQueryFactory;
import org.openvpms.web.workspace.patient.mr.PatientDocumentCRUDWindow;
import org.openvpms.web.workspace.patient.mr.PatientDocumentQuery;


/**
 * The visit editor.
 *
 * @author Tim Anderson
 */
public class VisitEditor {

    /**
     * The index of the patient history tab.
     */
    public static final int HISTORY_INDEX = 0;

    /**
     * The index of the invoice tab.
     */
    public static final int INVOICE_INDEX = 1;

    /**
     * The index of the reminders/alerts tab.
     */
    public static final int REMINDERS_INDEX = 2;

    /**
     * The index of the document tab.
     */
    public static final int DOCUMENT_INDEX = 3;

    /**
     * The index of the prescription tab.
     */
    public static final int PRESCRIPTION_INDEX = 4;

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
     * The patient CRUD window.
     */
    private PatientDocumentCRUDWindow documentWindow;

    /**
     * The prescription CRUD window.
     */
    private PrescriptionBrowserCRUDWindow prescriptionWindow;

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
     * The patient document browser.
     */
    private Browser<DocumentAct> documentBrowser;

    /**
     * Determines if the documents have been queried.
     */
    private boolean documentsQueried;


    /**
     * Constructs a {@code VisitEditor}.
     *
     * @param patient the patient
     * @param event   the <em>act.patientClinicalEvent</em>
     * @param invoice the invoice
     * @param context the context
     * @param help    the help context
     */
    public VisitEditor(Party patient, Act event, FinancialAct invoice, Context context, HelpContext help) {
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

        documentWindow = new VisitDocumentCRUDWindow(context, help.subtopic("document"));

        prescriptionWindow = new PrescriptionBrowserCRUDWindow(patient, context, help.subtopic("prescription"));
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
    public VisitCRUDWindow getHistory() {
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
        tab.setSelectedIndex(INVOICE_INDEX);
    }

    /**
     * Sets the buttons for the current tab.
     *
     * @param buttons the buttons
     */
    public void setButtons(ButtonSet buttons) {
        CRUDWindow<? extends Act> window = getWindow(tab.getSelectedIndex());
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
        CRUDWindow<? extends Act> window = getWindow(tab.getSelectedIndex());
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
                    onTabSelected(tab.getSelectedIndex());
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
     * Returns the {@link CRUDWindow} associated with the tab index.
     *
     * @param index the tab index
     * @return the corresponding {@link CRUDWindow} or {@code null} if none is found
     */
    protected CRUDWindow<? extends Act> getWindow(int index) {
        switch (index) {
            case HISTORY_INDEX:
                return visitWindow.getWindow();
            case INVOICE_INDEX:
                return chargeWindow;
            case REMINDERS_INDEX:
                return reminderWindow.getWindow();
            case DOCUMENT_INDEX:
                return documentWindow;
            case PRESCRIPTION_INDEX:
                return prescriptionWindow.getWindow();
        }
        return null;
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
     * Adds the history, invoice, reminder and document tabs to the tab pane model.
     *
     * @param model the model to add to
     */
    protected void addTabs(TabPaneModel model) {
        addPatientHistoryTab(model);
        addInvoiceTab(model);
        addRemindersAlertsTab(model);
        addDocumentsTab(model);
        addPrescriptionsTab(model);
    }

    /**
     * Helper to add a browser to the tab pane.
     *
     * @param index     the tab index. Used to determine the shortcut key
     * @param button    the button key
     * @param model     the tab model
     * @param component the component
     */
    protected void addTab(int index, String button, DefaultTabModel model, Component component) {
        int shortcut = index + 1;
        String text = "&" + shortcut + " " + Messages.get(button);
        model.addTab(text, component);
    }

    /**
     * Invoked when a tab is selected.
     *
     * @param selected the selected tab
     */
    protected void onTabSelected(int selected) {
        switch (selected) {
            case HISTORY_INDEX:
                onHistorySelected();
                break;
            case INVOICE_INDEX:
                onInvoiceSelected();
                break;
            case REMINDERS_INDEX:
                onRemindersSelected();
                break;
            case DOCUMENT_INDEX:
                onDocumentsSelected();
                break;
            case PRESCRIPTION_INDEX:
                onPrescriptionsSelected();
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
    private void addPatientHistoryTab(TabPaneModel model) {
        // need to add the browser to a ContentPane to get scrollbars
        ContentPane pane = new ContentPane();
        pane.add(visitWindow.getComponent());
        addTab(HISTORY_INDEX, "button.summary", model, pane);
    }

    /**
     * Adds a tab to display/edit the invoice.
     *
     * @param model the tab pane model to add to
     */
    private void addInvoiceTab(TabPaneModel model) {
        // need to add the window to a ContentPane to get scrollbars
        ContentPane pane = new ContentPane();
        pane.add(chargeWindow.getComponent());
        addTab(INVOICE_INDEX, "button.invoice", model, pane);
    }

    /**
     * Adds a tab to display reminders and alerts.
     *
     * @param model the tab pane model to add to
     */
    private void addRemindersAlertsTab(TabPaneModel model) {
        addTab(REMINDERS_INDEX, "button.reminder", model, reminderWindow.getComponent());
    }

    /**
     * Adds a tab to display documents.
     *
     * @param model the tab pane model to add to
     */
    private void addDocumentsTab(TabPaneModel model) {
        Query<DocumentAct> query = new PatientDocumentQuery<DocumentAct>(patient);
        documentBrowser = BrowserFactory.create(query, new DefaultLayoutContext(context, help));
        BrowserCRUDWindow<DocumentAct> window = new BrowserCRUDWindow<DocumentAct>(documentBrowser, documentWindow);
        addTab(DOCUMENT_INDEX, "button.document", model, window.getComponent());
    }

    /**
     * Adds a tab to display prescriptions.
     *
     * @param model the tab pane model to add to
     */
    private void addPrescriptionsTab(TabPaneModel model) {
        addTab(PRESCRIPTION_INDEX, "button.prescriptions", model, prescriptionWindow.getComponent());
    }

    /**
     * Invoked when the patient history tab is selected.
     * <p/>
     * This refreshes the history if the current event being displayed.
     */
    private void onHistorySelected() {
        Browser<Act> browser = visitWindow.getBrowser();
        if (browser.getObjects().contains(event)) {
            Act selected = browser.getSelected();
            browser.query();
            browser.setSelected(selected);
        }
        browser.setFocusOnResults();
        notifyListener(HISTORY_INDEX);
    }

    /**
     * Invoked when the invoice tab is selected.
     */
    private void onInvoiceSelected() {
        VisitChargeEditor editor = chargeWindow.getEditor();
        if (editor != null) {
            editor.getFocusGroup().setFocus();
        }
        notifyListener(INVOICE_INDEX);
    }

    /**
     * Invoked when the reminders tab is selected.
     */
    private void onRemindersSelected() {
        reminderWindow.getBrowser().setFocusOnResults();
        notifyListener(REMINDERS_INDEX);
    }

    /**
     * Invoked when the documents tab is selected.
     */
    private void onDocumentsSelected() {
        if (!documentsQueried) {
            documentBrowser.query();
            documentsQueried = true;
        } else {
            documentBrowser.setFocusOnResults();
        }
        notifyListener(DOCUMENT_INDEX);
    }

    /**
     * Invoked when the prescriptions tab is selected.
     */
    private void onPrescriptionsSelected() {
        prescriptionWindow.getBrowser().setFocusOnResults();
        prescriptionWindow.setChargeEditor(getChargeEditor());
        notifyListener(PRESCRIPTION_INDEX);
    }

    /**
     * Updates the visit status based on the charge status.
     */
    private void updateVisitStatus() {
        Retryer.run(new VisitStatusUpdater());
    }

    private class VisitStatusUpdater extends AbstractRetryable {

        /**
         * Runs the action for the first time.
         *
         * @return {@code true} if the action completed successfully, {@code false} if it failed, and should not be
         *         retried
         * @throws RuntimeException if the action fails and may be retried
         */
        @Override
        public boolean runFirst() {
            return runSubsequent();
        }

        /**
         * Runs the action. This is invoked after the first attempt to run the action has failed.
         *
         * @return {@code true} if the action completed successfully, {@code false} if it failed, and should not be
         *         retried
         * @throws RuntimeException if the action fails and may be retried
         */
        @Override
        public boolean runSubsequent() {
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
