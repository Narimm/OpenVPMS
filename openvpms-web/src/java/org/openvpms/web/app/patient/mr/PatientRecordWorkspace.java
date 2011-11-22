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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app.patient.mr;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.patient.reminder.ReminderArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.app.patient.CustomerPatientSummary;
import org.openvpms.web.app.subsystem.BrowserCRUDWorkspace;
import org.openvpms.web.component.subsystem.CRUDWindow;
import org.openvpms.web.component.app.ContextHelper;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.ActStatuses;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.DefaultActQuery;
import org.openvpms.web.component.im.query.PatientQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.TabbedBrowserListener;
import org.openvpms.web.component.im.util.Archetypes;
import org.openvpms.web.component.util.DoubleClickMonitor;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


/**
 * Patient medical record workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PatientRecordWorkspace extends BrowserCRUDWorkspace<Party, Act> {


    /**
     * Patient charges shortnames supported by teh workspace
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
     * The document archetypes.
     */
    private Archetypes<DocumentAct> docArchetypes;

    /**
     * The double click monitor.
     */
    private DoubleClickMonitor click = new DoubleClickMonitor();

    /**
     * The reminder statuses to query.
     */
    private static final ActStatuses STATUSES
            = new ActStatuses(ReminderArchetypes.REMINDER);


    /**
     * Constructs a new <tt>PatientRecordWorkspace</tt>.
     */
    public PatientRecordWorkspace() {
        super("patient", "record");
        setArchetypes(Party.class, "party.patient*");
        setChildArchetypes(Act.class, "act.patientClinicalEvent");

        docArchetypes = Archetypes.create(PatientDocumentQuery.DOCUMENT_SHORT_NAMES, DocumentAct.class,
                                          Messages.get("patient.document.createtype"));
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <tt>null</tt>
     */
    @Override
    public void setObject(Party object) {
        super.setObject(object);
        ContextHelper.setPatient(object);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Renders the workspace summary.
     *
     * @return the component representing the workspace summary, or
     *         <code>null</code> if there is no summary
     */
    @Override
    public Component getSummary() {
        return new CustomerPatientSummary(GlobalContext.getInstance()).getSummary(getObject());
    }

    /**
     * Returns the latest version of the current context object.
     *
     * @return the latest version of the context object, or {@link #getObject()}
     *         if they are the same
     */
    @Override
    protected Party getLatest() {
        return super.getLatest(GlobalContext.getInstance().getPatient());
    }

    /**
     * Create a new query.
     *
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    @Override
    protected Query<Party> createSelectQuery() {
        Query<Party> query = super.createSelectQuery();
        if (query instanceof PatientQuery) {
            ((PatientQuery) query).setShowAllPatients(true);
        }
        return query;
    }

    /**
     * Creates the workspace.
     *
     * @return a new workspace
     */
    @Override
    protected Component createWorkspace() {
        Component result;
        CRUDWindow window = getCRUDWindow();
        if (window instanceof SummaryCRUDWindow) {
            result = SplitPaneFactory.create(
                    SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
                    "PatientRecordWorkspace.SummaryLayout",
                    window.getComponent(), getBrowser().getComponent());
        } else {
            result = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL,
                                             "PatientRecordWorkspace.Layout",
                                             getBrowser().getComponent(),
                                             window.getComponent());
        }
        return result;
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<Act> createCRUDWindow() {
        SummaryCRUDWindow window = new SummaryCRUDWindow(getChildArchetypes());
        window.setQuery((PatientSummaryQuery) getQuery());
        return window;
    }

    /**
     * Creates a new query for the visits view.
     *
     * @return a new query
     */
    protected ActQuery<Act> createQuery() {
        return new PatientSummaryQuery(getObject());
    }

    /**
     * Creates a new browser to query and display acts.
     *
     * @param query the query
     * @return a new browser
     */
    @Override
    protected Browser<Act> createBrowser(Query<Act> query) {
        RecordBrowser browser = new RecordBrowser((PatientSummaryQuery) query,
                                                  createProblemsQuery(),
                                                  createReminderAlertQuery(),
                                                  new PatientDocumentQuery(getObject()),
                                                  createChargesQuery());
        browser.setListener(new TabbedBrowserListener() {
            public void onBrowserChanged() {
                changeCRUDWindow();
            }
        });
        return browser;
    }

    /**
     * Invoked when an act is selected.
     * <p/>
     * This implementation edits the selected act, if the current view is summary view and it has been double
     * clicked on.
     *
     * @param act the act
     */
    @Override
    protected void onBrowserSelected(Act act) {
        CRUDWindow<Act> window = getCRUDWindow();
        super.onBrowserSelected(act);
        if (window instanceof PatientRecordCRUDWindow) {
            Act event = getEvent(act);
            ((PatientRecordCRUDWindow) window).setEvent(event);
            if (window instanceof SummaryCRUDWindow) {
                long id = (act != null) ? act.getId() : 0;
                if (click.isDoubleClick(id)) { // avoid holding onto the act
                    window.edit();
                }
            }
        }
    }

    /**
     * Invoked when the object has been deleted.
     * <p/>
     * If the current window is the summary view, this implementation attempts to select the next object in the browser,
     * or the prior object if there is no next object. This is so that when the browser is refreshed, the selection will
     * be retained.
     *
     * @param object the object
     */
    @Override
    protected void onDeleted(Act object) {
        CRUDWindow<Act> window = getCRUDWindow();
        if (window instanceof SummaryCRUDWindow) {
            List<Act> list = getBrowser().getObjects();
            int index = list.indexOf(object);
            if (index != -1 && list.size() > 1) {
                if (TypeHelper.isA(object, PatientArchetypes.CLINICAL_EVENT)) {
                    // select the next event, if any
                    int newIndex = -1;
                    for (int i = index + 1; i < list.size(); ++i) {
                        if (TypeHelper.isA(list.get(i), PatientArchetypes.CLINICAL_EVENT)) {
                            newIndex = i;
                            break;
                        }
                    }
                    if (newIndex == -1) {
                        // select the previous event, if any
                        for (int i = index - 1; i >= 0; --i) {
                            if (TypeHelper.isA(list.get(i), PatientArchetypes.CLINICAL_EVENT)) {
                                newIndex = i;
                                break;
                            }
                        }
                    }
                    index = newIndex;
                } else {
                    // select another object. If there is one after the object being deleted, select that, else select
                    // the one before it
                    if (index + 1 < list.size()) {
                        ++index;
                    } else {
                        --index;
                    }
                }
                if (index != -1) {
                    Act select = list.get(index);
                    ((SummaryCRUDWindow) window).setEvent(getEvent(select));
                    getBrowser().setSelected(select);
                }
            }
        }
        super.onDeleted(object);
    }

    /**
     * Invoked when the browser is queried.
     * <p/>
     * This implementation selects the first available object and determines the associated event, if any.
     */
    @Override
    protected void onBrowserQuery() {
        super.onBrowserQuery();
        CRUDWindow<Act> window = getCRUDWindow();
        if (window instanceof PatientRecordCRUDWindow) {
            Act event = getEvent(window.getObject());
            ((PatientRecordCRUDWindow) window).setEvent(event);
        }
    }

    /**
     * Changes the CRUD window depending on the current browser view.
     */
    @SuppressWarnings("unchecked")
    private void changeCRUDWindow() {
        RecordBrowser browser = (RecordBrowser) getBrowser();
        CRUDWindow<Act> window;
        RecordBrowser.View view = browser.getView();
        if (view == RecordBrowser.View.SUMMARY
            || view == RecordBrowser.View.PROBLEMS) {
            PatientRecordCRUDWindow w;
            if (view == RecordBrowser.View.SUMMARY) {
                w = (PatientRecordCRUDWindow) createCRUDWindow();
            } else {
                w = new ProblemRecordCRUDWindow();
            }
            Act event = getEvent(null);
            w.setEvent(event);
            window = (CRUDWindow<Act>) w;
        } else if (view == RecordBrowser.View.DOCUMENTS) {
            CRUDWindow w = new PatientDocumentCRUDWindow(docArchetypes);
            window = (CRUDWindow<Act>) w;  // todo
        } else if (view == RecordBrowser.View.REMINDER_ALERT) {
            window = new ReminderCRUDWindow();
        } else {
            window = new ChargesCRUDWindow();
        }

        Act selected = browser.getSelected();
        if (selected != null) {
            window.setObject(selected);
        }
        setCRUDWindow(window);
        setWorkspace(createWorkspace());
    }

    /**
     * Creates a new query, for the problems view.
     *
     * @return a new query
     */
    private DefaultActQuery<Act> createProblemsQuery() {
        String[] shortNames = {PatientArchetypes.CLINICAL_PROBLEM};
        DefaultActQuery<Act> query = new DefaultActQuery<Act>(
                getObject(), "patient", "participation.patient", shortNames);
        query.setDefaultSortConstraint(DEFAULT_SORT);
        return query;
    }

    /**
     * Creates a new query, for the reminder/alert view.
     *
     * @return a new query
     */
    private Query<Act> createReminderAlertQuery() {
        String[] shortNames = {ReminderArchetypes.REMINDER, "act.patientAlert"};
        DefaultActQuery<Act> query = new DefaultActQuery<Act>(
                getObject(), "patient", "participation.patient", shortNames,
                STATUSES);
        query.setDefaultSortConstraint(DEFAULT_SORT);
        return query;
    }

    /**
     * Creates a new query, for the charges view.
     *
     * @return a new query
     */
    private Query<Act> createChargesQuery() {
        String[] statuses = {};
        DefaultActQuery<Act> query = new DefaultActQuery<Act>(
                getObject(), "patient", "participation.patient",
                CHARGES_SHORT_NAMES, false, statuses);
        query.setDefaultSortConstraint(DEFAULT_SORT);
        query.setMaxResults(10);
        return query;
    }

    /**
     * Returns the event associated with the current selected act.
     *
     * @param act the current selected act. May be <tt>null</tt>
     * @return the event associated with the current selected act, or
     *         <tt>null</tt> if none is found
     */
    private Act getEvent(Act act) {
        RecordBrowser browser = ((RecordBrowser) getBrowser());
        Browser<Act> summary = browser.getBrowser(
                RecordBrowser.View.SUMMARY);
        if (act == null) {
            act = summary.getSelected();
        }
        boolean found = false;
        if (act != null) {
            List<Act> acts = summary.getObjects();
            int index = acts.indexOf(act);
            while (!(found = TypeHelper.isA(act, PatientArchetypes.CLINICAL_EVENT)) && index > 0) {
                act = acts.get(--index);
            }
        }
        return (found) ? act : null;
    }

}
