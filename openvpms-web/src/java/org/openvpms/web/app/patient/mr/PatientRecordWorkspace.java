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
import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import static org.openvpms.web.app.patient.mr.PatientRecordTypes.CLINICAL_EVENT;
import static org.openvpms.web.app.patient.mr.PatientRecordTypes.CLINICAL_PROBLEM;
import org.openvpms.web.app.patient.summary.PatientSummary;
import org.openvpms.web.app.subsystem.ActWorkspace;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.component.app.ContextHelper;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.doc.DocumentCRUDWindow;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.DefaultActQuery;
import org.openvpms.web.component.im.query.PatientQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.util.FastLookupHelper;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


/**
 * Patient medical record workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PatientRecordWorkspace extends ActWorkspace {

    /**
     * Patient Document shortnames supported by the workspace.
     */
    private static final String[] DOCUMENT_SHORT_NAMES = {"act.patientDocumentForm",
                                                          "act.patientDocumentLetter",
                                                          "act.patientDocumentAttachment",
                                                          "act.patientDocumentImage"};

    /**
     * Patient Investigation shortnames supported by the workspace.
     */
    private static final String[] INVESTIGATION_SHORT_NAMES = {"act.patientInvestigationRadiology",
                                                               "act.patientInvestigationBiochemistry",
                                                               "act.patientInvestigationHaematology",
                                                               "act.patientInvestigationCytology"};

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
        ContextHelper.setPatient(party);
        layoutWorkspace(party);
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
     * Create a new query.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    @Override
    @SuppressWarnings("unchecked")
    protected Query<IMObject> createQuery(String refModelName,
                                          String entityName,
                                          String conceptName) {
        Query query = super.createQuery(refModelName, entityName, conceptName);
        if (query instanceof PatientQuery) {
            ((PatientQuery) query).setShowAllPatients(true);
        }
        return query;
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
     * Creates the workspace.
     *
     * @return a new workspace
     */
    @Override
    protected Component createWorkspace() {
        Component result;
        CRUDWindow window = getCRUDWindow();
        if (window instanceof SummaryCRUDWindow) {
            // todo SummaryCRUDWindow is a bit of a hack as it is never rendered
            result = getBrowser().getComponent();
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
    protected CRUDWindow createCRUDWindow() {
        return new SummaryCRUDWindow();
    }

    /**
     * Creates a new query for the visits view.
     *
     * @param party the party to query acts for
     * @return a new query
     */
    protected ActQuery<Act> createQuery(Party party) {
        return createQuery(party, new String[]{CLINICAL_EVENT});
    }

    /**
     * Creates a new browser to query and display acts.
     *
     * @param query the query
     * @return a new browser
     */
    @Override
    protected Browser<Act> createBrowser(ActQuery<Act> query) {
        SortConstraint[] sort = {new NodeSortConstraint("startTime", false)};
        Party patient = (Party) getObject();
        RecordBrowser browser = new RecordBrowser(query, createQuery(patient),
                                                  createProblemsQuery(),
                                                  createMedicationQuery(),
                                                  createReminderAlertQuery(),
                                                  createDocumentQuery(),
                                                  createInvestigationQuery(),
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
        } else if (view == RecordBrowser.View.DOCUMENTS) {
            String type = Messages.get("patient.document.createtype");
            window = new DocumentCRUDWindow(type, DOCUMENT_SHORT_NAMES);
        } else if (view == RecordBrowser.View.INVESTIGATIONS) {
            String type = Messages.get("patient.investigation.createtype");
            window = new DocumentCRUDWindow(type, INVESTIGATION_SHORT_NAMES);
        } else {
            window = new ReminderCRUDWindow();
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
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor(
                "act.patientReminder");
        NodeDescriptor statuses = archetype.getNodeDescriptor("status");
        List<Lookup> lookups = FastLookupHelper.getLookups(statuses);
        DefaultActQuery query = new DefaultActQuery(patient, "patient",
                                                    "participation.patient",
                                                    shortNames, lookups, null);
        query.setStatus(ActStatus.IN_PROGRESS);
        return query;
    }

    /**
     * Creates a new query, for the document view.
     *
     * @return a new query
     */
    private Query<Act> createDocumentQuery() {
        Party patient = (Party) getObject();
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor(
                "act.patientDocumentLetter");
        NodeDescriptor statuses = archetype.getNodeDescriptor("status");
        List<Lookup> lookups = FastLookupHelper.getLookups(statuses);
        return new DefaultActQuery(patient, "patient", "participation.patient",
                                   DOCUMENT_SHORT_NAMES, lookups, null);
    }

    /**
     * Creates a new query, for the investigations view.
     *
     * @return a new query
     */
    private Query<Act> createInvestigationQuery() {
        Party patient = (Party) getObject();
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor(
                "act.patientInvestigationRadiology");
        NodeDescriptor statuses = archetype.getNodeDescriptor("status");
        List<Lookup> lookups = FastLookupHelper.getLookups(statuses);
        return new DefaultActQuery(patient, "patient", "participation.patient",
                                   INVESTIGATION_SHORT_NAMES, lookups, null);
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
