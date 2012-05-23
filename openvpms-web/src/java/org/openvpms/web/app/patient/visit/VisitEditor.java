package org.openvpms.web.app.patient.visit;

import echopointng.TabbedPane;
import echopointng.tabbedpane.DefaultTabModel;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.event.ChangeEvent;
import org.openvpms.archetype.rules.util.DateRules;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.act.FinancialAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.app.patient.mr.PatientDocumentCRUDWindow;
import org.openvpms.web.app.patient.mr.PatientDocumentQuery;
import org.openvpms.web.app.patient.history.PatientHistoryQuery;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.button.ButtonSet;
import org.openvpms.web.component.event.ChangeListener;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.BrowserFactory;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.TabPaneModel;
import org.openvpms.web.component.util.TabbedPaneFactory;
import org.openvpms.web.resource.util.Messages;


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
     * The patient medical record summary query.
     */
    private final PatientHistoryQuery query;

    /**
     * The invoice CRUD window.
     */
    private final VisitChargeCRUDWindow invoiceWindow;

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
     * The index of the summary tab.
     */
    private static final int SUMMARY_INDEX = 0;

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

        query = new PatientHistoryQuery(patient);
        query.setAllDates(true);
        query.setFrom(event.getActivityStartTime());
        query.setTo(DateRules.getDate(event.getActivityStartTime(), 1, DateUnits.DAYS));

        visitWindow = new VisitBrowserCRUDWindow(query, context);

        invoiceWindow = new VisitChargeCRUDWindow(patient);
        invoiceWindow.setObject(invoice);

        reminderWindow = new ReminderBrowserCRUDWindow(patient);

        documentWindow = new VisitDocumentCRUDWindow();
    }

    public VisitChargeCRUDWindow getInvoice() {
        return invoiceWindow;

    }

    /**
     * Sets the buttons for the current tab.
     *
     * @param buttons the buttons
     */
    public void setButtons(ButtonSet buttons) {
        switch (tab.getSelectedIndex()) {
            case SUMMARY_INDEX:
                visitWindow.setButtons(buttons);
                break;
            case INVOICE_INDEX:
                invoiceWindow.setButtons(buttons);
                break;
            case REMINDERS_INDEX:
                reminderWindow.setButtons(buttons);
                break;
            case DOCUMENT_INDEX:
                documentWindow.setButtons(buttons);

        }
    }

    /**
     * Returns the medical record summary query.
     *
     * @return the medical record summary query
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
            addSummaryTab(model);
            addInvoiceTab(model);
            addRemindersAlertsTab(model);
            addDocumentsTab(model);
            tab = TabbedPaneFactory.create(model);
            tab.setHeight(new Extent(800, Extent.PX)); // TODO - need to calculate this in stylesheet
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
        return invoiceWindow.save();
    }

    /**
     * Adds a tab to display/edit the patient medical records
     *
     * @param model the tab pane model to add to
     */
    private void addSummaryTab(TabPaneModel model) {
        addTab(1, "button.summary", model, visitWindow.getComponent());
    }

    /**
     * Adds a tab to display/edit the invoice
     *
     * @param model the tab pane model to add to
     */
    private void addInvoiceTab(TabPaneModel model) {
        addTab(2, "button.invoice", model, invoiceWindow.getComponent());
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
     * Invoked when a tab is selected. Notifies the listener if registered.
     */
    private void onTabSelected() {
        if (listener != null) {
            switch (tab.getSelectedIndex()) {
                case SUMMARY_INDEX:
                    listener.summarySelected();
                    break;
                case INVOICE_INDEX:
                    listener.invoiceSelected();
                    break;
                case REMINDERS_INDEX:
                    listener.remindersSelected();
                    break;
                case DOCUMENT_INDEX:
                    listener.documentsSelected();
                    break;
            }
        }
    }

}
