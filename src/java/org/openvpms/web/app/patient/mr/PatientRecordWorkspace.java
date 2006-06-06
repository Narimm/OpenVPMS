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
 *  $Id: PatientRecordWorkspace.java 931 2006-05-29 03:43:35Z tanderson $
 */

package org.openvpms.web.app.patient.mr;

import static org.openvpms.web.app.patient.mr.PatientRecordTypes.*;
import org.openvpms.web.app.subsystem.ActWorkspace;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.act.ActHelper;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.util.SplitPaneFactory;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;


/**
 * Patient medical record workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-29 03:43:35Z $
 */
public class PatientRecordWorkspace extends ActWorkspace {

    /**
     * Clinical event item short names.
     */
    private final String[] _clinicalEventItems;


    /**
     * Construct a new <code>PatientRecordWorkspace</code>.
     */
    public PatientRecordWorkspace() {
        super("patient", "record", "party", "party", "patient*");
        _clinicalEventItems = ActHelper.getTargetShortNames(
                RELATIONSHIP_CLINICAL_EVENT_ITEM);
    }

    /**
     * Sets the current object.
     *
     * @param object the object. May be <code>null</code>
     */
    @Override
    public void setObject(IMObject object) {
        super.setObject(object);
        Party party = (Party) object;
        Context.getInstance().setPatient(party);
        layoutWorkspace(party, getRootComponent());
        initQuery(party);
    }

    /**
     * Determines if the workspace should be refreshed. This implementation
     * returns true if the current patient has changed.
     *
     * @return <code>true</code> if the workspace should be refreshed, otherwise
     *         <code>false</code>
     */
    @Override
    protected boolean refreshWorkspace() {
        Party patient = Context.getInstance().getPatient();
        return (patient != getObject());
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        Party patient = Context.getInstance().getPatient();
        if (patient != getObject()) {
            setObject(patient);
        }
    }

    /**
     * Returns a component representing the acts.
     *
     * @param acts the act browser
     * @return a component representing the acts
     */
    @Override
    protected Component getActs(Browser acts) {
        return acts.getComponent();
    }

    /**
     * Creates the workspace split pane.
     *
     * @param browser the act browser
     * @param window  the CRUD window
     * @return a new workspace split pane
     */
    @Override
    protected SplitPane createWorkspace(Browser browser, CRUDWindow window) {
        return SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL,
                                       "PatientRecordWorkspace.Layout",
                                       browser.getComponent(),
                                       window.getComponent());
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow createCRUDWindow() {
        return new VisitRecordCRUDWindow();
    }

    /**
     * Creates a new query for the visits view.
     *
     * @param party the party to query acts for
     * @return a new query
     */
    protected ActQuery createQuery(Party party) {
        ActQuery query = createQuery(party, _clinicalEventItems);
        String[] required = {CLINICAL_EPISODE, CLINICAL_EVENT};
        query.setRequiredShortNames(required);
        return query;
    }

    /**
     * Creates a new browser to query and display acts.
     *
     * @param query the query
     * @return a new browser
     */
    @Override
    protected Browser<Act> createBrowser(Query<Act> query) {
        SortConstraint[] sort = {new NodeSortConstraint("startTime", false)};
        RecordBrowser browser = new RecordBrowser(query, createProblemsQuery(),
                                                  sort);
        browser.setListener(new RecordBrowserListener() {
            public void onViewChanged() {
                changeCRUDWindow();
            }
        });
        return browser;
    }

    /**
     * Changes the CRUD window depending on the current browser view.
     */
    private void changeCRUDWindow() {
        RecordBrowser browser = (RecordBrowser) getBrowser();
        PatientRecordCRUDWindow current
                = (PatientRecordCRUDWindow) getCRUDWindow();
        Act selected = (Act) current.getObject();
        PatientRecordCRUDWindow window;
        if (browser.isVisit()) {
            window = new VisitRecordCRUDWindow();
        } else {
            window = new ProblemRecordCRUDWindow();
        }
        // use the current window's act to filter act short names.
        // If there is a selection in the browser view, this will be overriden
        window.setAct(selected);
        selected = browser.getSelected();
        if (selected != null) {
            window.setObject(selected);
        }
        setCRUDWindow(window);
    }

    /**
     * Creates a new query, for the problems view.
     *
     * @return a new query
     */
    private ActQuery createProblemsQuery() {
        Party patient = (Party) getObject();
        String[] shortNames = {CLINICAL_PROBLEM};
        return createQuery(patient, shortNames);
    }

    /**
     * Creates a new query.
     *
     * @param patient    the patient to query acts for
     * @param shortNames the act short names
     * @return a new query
     */
    private ActQuery createQuery(Party patient, String[] shortNames) {
        String[] statuses = {};
        return new ActQuery(patient, "patient", "participation.patient",
                            shortNames, statuses);
    }

}
