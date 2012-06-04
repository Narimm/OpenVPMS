package org.openvpms.web.app.patient.visit;

import echopointng.TabbedPane;
import echopointng.tabbedpane.DefaultTabModel;
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
import org.openvpms.web.app.patient.charge.VisitChargeEditor;
import org.openvpms.web.app.patient.history.PatientHistoryQuery;
import org.openvpms.web.app.patient.mr.PatientDocumentCRUDWindow;
import org.openvpms.web.app.patient.mr.PatientDocumentQuery;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.event.ChangeListener;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.retry.AbstractRetryable;
import org.openvpms.web.component.retry.Retryer;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.TabPaneModel;
import org.openvpms.web.component.util.TabbedPaneFactory;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.ServiceHelper;


/**
 * The visit editor.
 *
 * @author Tim Anderson
 */
public class VisitEditor {

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
     * The index of the patient history tab.
     */
    private static final int HISTORY_INDEX = 0;

    /**
     * The index of the invoice tab.
     */
    private static final int INVOICE_INDEX = 1;

    /**
     * The index of the reminders/alerts tab.
     */
    private static final int REMINDERS_INDEX = 2;

    /**
     * The index of the document tab.
     */
    private static final int DOCUMENT_INDEX = 3;

    /**
     * Constructs a <tt>VisitBrowser</tt>.
     *
     * @param patient the patient
     * @param event   the <em>act.patientClinicalEvent</em>
     * @param invoice the invoice
     */
    public VisitEditor(Party patient, Act event, FinancialAct invoice, Context context) {
        this.patient = patient;
        this.event = event;

        query = new PatientHistoryQuery(patient);
        query.setAllDates(true);
        query.setFrom(event.getActivityStartTime());
        query.setTo(DateRules.getDate(event.getActivityStartTime(), 1, DateUnits.DAYS));

        visitWindow = new VisitBrowserCRUDWindow(query, context);

        chargeWindow = createVisitChargeCRUDWindow(event, context);
        chargeWindow.setObject(invoice);

        reminderWindow = new ReminderBrowserCRUDWindow(patient);

        documentWindow = new VisitDocumentCRUDWindow();
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
        switch (tab.getSelectedIndex()) {
            case HISTORY_INDEX:
                visitWindow.setButtons(buttons);
                break;
            case INVOICE_INDEX:
                chargeWindow.setButtons(buttons);
                break;
            case REMINDERS_INDEX:
                reminderWindow.setButtons(buttons);
                break;
            case DOCUMENT_INDEX:
                documentWindow.setButtons(buttons);
                break;
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
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        if (container == null) {
            container = ColumnFactory.create("InsetY");
            TabPaneModel model = new TabPaneModel(container);
            addPatientHistoryTab(model);
            addInvoiceTab(model);
            addRemindersAlertsTab(model);
            addDocumentsTab(model);
            tab = TabbedPaneFactory.create(model);
            tab.setStyleName("VisitEditor.TabbedPane");
            tab.getSelectionModel().addChangeListener(new ChangeListener() {
                @Override
                public void onChange(ChangeEvent event) {
                    onTabSelected();
                }
            });
            focusGroup.add(tab);
            container.add(tab);
            tab.setSelectedIndex(0);
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
     * Creates a new visit charge CRUD window.
     *
     * @param event   the event
     * @param context the context
     * @return a new visit charge CRUD window
     */
    protected VisitChargeCRUDWindow createVisitChargeCRUDWindow(Act event, Context context) {
        return new VisitChargeCRUDWindow(event, context);
    }

    /**
     * Adds a tab to display/edit the patient history.
     *
     * @param model the tab pane model to add to
     */
    private void addPatientHistoryTab(TabPaneModel model) {
        addTab(1, "button.summary", model, visitWindow.getComponent());
    }

    /**
     * Adds a tab to display/edit the invoice.
     *
     * @param model the tab pane model to add to
     */
    private void addInvoiceTab(TabPaneModel model) {
        addTab(2, "button.invoice", model, chargeWindow.getComponent());
    }

    /**
     * Adds a tab to display reminders and alerts.
     *
     * @param model the tab pane model to add to
     */
    private void addRemindersAlertsTab(TabPaneModel model) {
        addTab(3, "button.reminder", model, reminderWindow.getComponent());
    }

    /**
     * Adds a tab to display documents.
     *
     * @param model the tab pane model to add to
     */
    private void addDocumentsTab(TabPaneModel model) {
        Query<DocumentAct> query = new PatientDocumentQuery<DocumentAct>(patient);
        Browser<DocumentAct> browser = BrowserFactory.create(query);
        BrowserCRUDWindow<DocumentAct> window = new BrowserCRUDWindow<DocumentAct>(browser, documentWindow);
        addTab(4, "button.document", model, window.getComponent());
    }

    /**
     * Helper to add a browser to the tab pane.
     *
     * @param shortcut  the tab button shortcut no.
     * @param button    the button key
     * @param model     the tab model
     * @param component the component
     */
    private void addTab(int shortcut, String button, DefaultTabModel model, Component component) {
        String text = "&" + shortcut + " " + Messages.get(button);
        model.addTab(text, component);
    }

    /**
     * Invoked when a tab is selected.
     */
    private void onTabSelected() {
        switch (tab.getSelectedIndex()) {
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
        }
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
        if (listener != null) {
            listener.historySelected();
        }
    }

    /**
     * Invoked when the invoice tab is selected.
     */
    private void onInvoiceSelected() {
        if (listener != null) {
            listener.invoiceSelected();
        }
    }

    /**
     * Invoked when the reminders tab is selected.
     */
    private void onRemindersSelected() {
        if (listener != null) {
            listener.remindersSelected();
        }
    }

    /**
     * Invoked when the documents tab is selected.
     */
    private void onDocumentsSelected() {
        if (listener != null) {
            listener.documentsSelected();
        }
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
            return updateStatus(event);
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
