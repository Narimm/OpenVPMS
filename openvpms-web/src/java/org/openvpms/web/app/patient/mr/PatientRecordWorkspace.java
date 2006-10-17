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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import static org.openvpms.web.app.patient.mr.PatientRecordTypes.CLINICAL_EVENT;
import static org.openvpms.web.app.patient.mr.PatientRecordTypes.CLINICAL_PROBLEM;
import org.openvpms.web.app.patient.summary.PatientSummary;
import org.openvpms.web.app.subsystem.ActWorkspace;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.DefaultActQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.util.SplitPaneFactory;

import java.util.List;


/**
 * Patient medical record workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-29 03:43:35Z $
 */
public class PatientRecordWorkspace extends ActWorkspace {

    /**
     * Construct a new <code>PatientRecordWorkspace</code>.
     */
    public PatientRecordWorkspace() {
        super("patient", "record", "party", "party", "patient*");
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
        GlobalContext.getInstance().setPatient(party);
        layoutWorkspace(party, getRootComponent());
        initQuery(party);
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
        return PatientSummary.getSummary((Party) getObject());
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
        Party patient = GlobalContext.getInstance().getPatient();
        return (patient != getObject());
    }

    /**
     * Lays out the component.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        Party patient = GlobalContext.getInstance().getPatient();
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
        return new SummaryCRUDWindow();
    }

    /**
     * Creates a new query for the visits view.
     *
     * @param party the party to query acts for
     * @return a new query
     */
    protected ActQuery createQuery(Party party) {
        return createQuery(party, new String[]{CLINICAL_EVENT});
    }

    /**
     * Creates a new browser to query and display acts.
     *
     * @param query the query
     * @return a new browser
     */
    @Override
    protected Browser<Act> createBrowser(ActQuery query) {
        SortConstraint[] sort = {new NodeSortConstraint("startTime", false)};
        Party patient = (Party) getObject();
        RecordBrowser browser = new RecordBrowser(query, createQuery(patient),
                                                  createProblemsQuery(),
                                                  createMedicationQuery(),
                                                  createReminderAlertQuery(),
                                                  sort);
        browser.setListener(new RecordBrowserListener() {
            public void onViewChanged() {
                changeCRUDWindow();
            }
        });
        return browser;
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    @Override
    protected void onDeleted(IMObject object) {
        super.onDeleted(object);
    }

    /**
     * Changes the CRUD window depending on the current browser view.
     */
    private void changeCRUDWindow() {
        RecordBrowser browser = (RecordBrowser) getBrowser();
        CRUDWindow window;
        RecordBrowser.View view = browser.getView();
        if (view == RecordBrowser.View.SUMMARY) {
            window = new SummaryCRUDWindow();
        } else if (view == RecordBrowser.View.VISITS) {
            window = new VisitRecordCRUDWindow();
        } else if (view == RecordBrowser.View.PROBLEMS) {
            Browser<Act> visit = browser.getBrowser(RecordBrowser.View.VISITS);
            ProblemRecordCRUDWindow problems = new ProblemRecordCRUDWindow();
            Act selected = visit.getSelected();
            if (TypeHelper.isA(selected, CLINICAL_EVENT)) {
                problems.setEvent(selected);
            }
            window = problems;
        } else if (view == RecordBrowser.View.MEDICATION) {
            window = new MedicationRecordCRUDWindow();
        } else {
            window = new ReminderCRUDWindow();
        }
        Act selected = browser.getSelected();
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
    private DefaultActQuery createProblemsQuery() {
        Party patient = (Party) getObject();
        String[] shortNames = {CLINICAL_PROBLEM};
        return createQuery(patient, shortNames);
    }

    /**
     * Creates a new query, for the medication view.
     *
     * @return a new query
     */
    private Query<Act> createMedicationQuery() {
        Party patient = (Party) getObject();
        String[] shortNames = {"act.patientMedication"};
        return createQuery(patient, shortNames);
    }

    /**
     * Creates a new query, for the reminder/alert view.
     *
     * @return a new query
     */
    private Query<Act> createReminderAlertQuery() {
        String[] shortNames = {"act.patientReminder", "act.patientAlert"};
        Party patient = (Party) getObject();
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor(
                "act.patientReminder");
        NodeDescriptor statuses = archetype.getNodeDescriptor("status");
        List<Lookup> lookups = LookupHelper.get(service, statuses);
        DefaultActQuery query = new DefaultActQuery(patient, "patient",
                                                    "participation.patient",
                                                    shortNames, lookups, null);
        query.setStatus("In Progress");
        return query;
    }

    /**
     * Creates a new query.
     *
     * @param patient    the patient to query acts for
     * @param shortNames the act short names
     * @return a new query
     */
    private DefaultActQuery createQuery(Party patient, String[] shortNames) {
        String[] statuses = {};
        return new DefaultActQuery(patient, "patient", "participation.patient",
                                   shortNames, statuses);
    }

}
