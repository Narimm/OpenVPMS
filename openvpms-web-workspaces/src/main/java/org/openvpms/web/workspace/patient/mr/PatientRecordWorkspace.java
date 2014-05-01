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

package org.openvpms.web.workspace.patient.mr;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextHelper;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.PatientQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.TabbedBrowserListener;
import org.openvpms.web.component.workspace.BrowserCRUDWorkspace;
import org.openvpms.web.component.workspace.CRUDWindow;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.util.DoubleClickMonitor;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.customer.CustomerMailContext;
import org.openvpms.web.workspace.patient.CustomerPatientSummary;
import org.openvpms.web.workspace.patient.PatientRecordCRUDWindow;
import org.openvpms.web.workspace.patient.history.AbstractPatientHistoryBrowser;
import org.openvpms.web.workspace.patient.history.AbstractPatientHistoryCRUDWindow;
import org.openvpms.web.workspace.patient.history.PatientHistoryQuery;
import org.openvpms.web.workspace.patient.history.PatientHistoryQueryFactory;
import org.openvpms.web.workspace.patient.summary.CustomerPatientSummaryFactory;


/**
 * Patient medical record workspace.
 *
 * @author Tim Anderson
 */
public class PatientRecordWorkspace extends BrowserCRUDWorkspace<Party, Act> {

    /**
     * The double click monitor.
     */
    private final DoubleClickMonitor click = new DoubleClickMonitor();


    /**
     * Constructs a {@link PatientRecordWorkspace}.
     *
     * @param context the context
     */
    public PatientRecordWorkspace(Context context) {
        super("patient", "record", context);
        setArchetypes(Party.class, "party.patient*");
        setChildArchetypes(Act.class, "act.patientClinicalEvent");

        setMailContext(new CustomerMailContext(context, getHelpContext()));
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be {@code null}
     */
    @Override
    public void setObject(Party object) {
        super.setObject(object);
        ContextHelper.setPatient(getContext(), object);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Renders the workspace summary.
     *
     * @return the component representing the workspace summary, or {@code null} if there is no summary
     */
    @Override
    public Component getSummary() {
        CustomerPatientSummaryFactory factory = ServiceHelper.getBean(CustomerPatientSummaryFactory.class);
        CustomerPatientSummary summary = factory.createCustomerPatientSummary(getContext(), getHelpContext());
        return summary.getSummary(getObject());
    }

    /**
     * Returns the latest version of the current context object.
     *
     * @return the latest version of the context object, or {@link #getObject()} if they are the same
     */
    @Override
    protected Party getLatest() {
        return super.getLatest(getContext().getPatient());
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
        if (window instanceof AbstractPatientHistoryCRUDWindow) {
            result = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
                                             "PatientRecordWorkspace.SummaryLayout", window.getComponent(),
                                             getBrowser().getComponent());
        } else {
            result = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL, "PatientRecordWorkspace.Layout",
                                             getBrowser().getComponent(), window.getComponent());
        }
        return result;
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow<Act> createCRUDWindow() {
        return getBrowser().createCRUDWindow(getContext(), getHelpContext());
    }

    /**
     * Creates a new query for the visits view.
     *
     * @return a new query
     */
    protected ActQuery<Act> createQuery() {
        return PatientHistoryQueryFactory.create(getObject(), getContext().getPractice());
    }

    /**
     * Creates a new browser to query and display acts.
     *
     * @param query the query
     * @return a new browser
     */
    @Override
    protected Browser<Act> createBrowser(Query<Act> query) {
        RecordBrowser browser = createRecordBrowser(getObject(), (PatientHistoryQuery) query, getContext(),
                                                    getHelpContext());
        browser.setListener(new TabbedBrowserListener() {
            public void onBrowserChanged() {
                changeCRUDWindow();
            }
        });
        return browser;
    }

    /**
     * Creates a new patient record browser.
     *
     * @param patient the patient
     * @param query   the patient history query
     * @param context the context
     * @param help    the help context
     * @return a new record browser
     */
    protected RecordBrowser createRecordBrowser(Party patient, PatientHistoryQuery query, Context context,
                                                HelpContext help) {
        return new RecordBrowser(patient, query, context, help);
    }

    /**
     * Returns the browser.
     *
     * @return the browser, or {@code null} if none has been registered
     */
    @Override
    protected RecordBrowser getBrowser() {
        return (RecordBrowser) super.getBrowser();
    }

    /**
     * Invoked when an act is selected.
     * <p/>
     * This implementation edits the selected act, if the current view is a history view and it has been double
     * clicked on.
     *
     * @param act the act
     */
    @Override
    protected void onBrowserSelected(Act act) {
        CRUDWindow<Act> window = getCRUDWindow();
        super.onBrowserSelected(act);
        if (window instanceof PatientRecordCRUDWindow) {
            Act event = getBrowser().getEvent(act);
            ((PatientRecordCRUDWindow) window).setEvent(event);
            if (window instanceof AbstractPatientHistoryCRUDWindow) {
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
     * If the current window is a history view, this implementation attempts to select the next object in the browser,
     * or the prior object if there is no next object. This is so that when the browser is refreshed, the selection will
     * be retained.
     *
     * @param object the object
     */
    @Override
    protected void onDeleted(Act object) {
        Browser<Act> browser = getBrowser().getSelectedBrowser();
        if (browser instanceof AbstractPatientHistoryBrowser) {
            AbstractPatientHistoryBrowser history = (AbstractPatientHistoryBrowser) browser;
            Act select = history.selectNext(object);
            if (select != null) {
                Act event = getBrowser().getEvent(select);
                ((AbstractPatientHistoryCRUDWindow) getCRUDWindow()).setEvent(event);
                getBrowser().setSelected(select);
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
            Act event = getBrowser().getEvent(window.getObject());
            ((PatientRecordCRUDWindow) window).setEvent(event);
        }
    }

    /**
     * Changes the CRUD window depending on the current browser view.
     */
    @SuppressWarnings("unchecked")
    private void changeCRUDWindow() {
        RecordBrowser browser = getBrowser();
        CRUDWindow<Act> window = browser.createCRUDWindow(getContext(), getHelpContext());

        Act selected = browser.getSelected();
        if (selected != null) {
            window.setObject(selected);
        }
        setCRUDWindow(window);
        setWorkspace(createWorkspace());
    }

}
