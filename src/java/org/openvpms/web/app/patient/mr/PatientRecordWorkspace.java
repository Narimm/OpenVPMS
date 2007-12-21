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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.app.patient.CustomerPatientSummary;
import static org.openvpms.web.app.patient.mr.PatientRecordTypes.CLINICAL_EVENT;
import static org.openvpms.web.app.patient.mr.PatientRecordTypes.CLINICAL_PROBLEM;
import org.openvpms.web.app.subsystem.ActWorkspace;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.ShortNameList;
import org.openvpms.web.component.app.ContextHelper;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.doc.DocumentCRUDWindow;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.DefaultActQuery;
import org.openvpms.web.component.im.query.PatientQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.util.FastLookupHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.resource.util.Messages;

import java.util.List;


/**
 * Patient medical record workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PatientRecordWorkspace extends ActWorkspace<Party, Act> {

    /**
     * Patient Document shortnames supported by the workspace.
     */
    private static final String[] DOCUMENT_SHORT_NAMES = {
            "act.patientDocumentForm",
            "act.patientDocumentLetter",
            "act.patientDocumentAttachment",
            "act.patientDocumentImage",
            "act.patientInvestigationBiochemistry",
            "act.patientInvestigationCytology",
            "act.patientInvestigationHaemotology",
            "act.patientInvestigationRadiology"
    };

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
     * Constructs a new <tt>PatientRecordWorkspace</tt>.
     */
    public PatientRecordWorkspace() {
        super("patient", "record", new ShortNameList("party.patient*"));
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
        layoutWorkspace(object);
        initQuery(object);
        firePropertyChange(SUMMARY_PROPERTY, null, null);
    }

    /**
     * Sets the current object.
     * This is analagous to  {@link #setObject} but performs a safe cast
     * to the required type.
     *
     * @param object the current object. May be <code>null</code>
     */
    public void setIMObject(IMObject object) {
        if (object == null || object instanceof Party) {
            setObject((Party) object);
        } else {
            throw new IllegalArgumentException(
                    "Argument 'object' must be an instance of "
                            + Party.class.getName());
        }
    }

    /**
     * Renders the workspace summary.
     *
     * @return the component representing the workspace summary, or
     *         <code>null</code> if there is no summary
     */
    @Override
    public Component getSummary() {
        return CustomerPatientSummary.getSummary(getObject());
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
     * Lays out the component.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        Party patient = GlobalContext.getInstance().getPatient();
        patient = IMObjectHelper.reload(patient);
        if (!IMObjectHelper.isSame(getObject(), patient)) {
            setObject(patient);
        }
    }

    /**
     * Create a new query.
     *
     * @return a new query
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    @Override
    protected Query<Party> createQuery() {
        Query<Party> query = super.createQuery();
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
        SummaryCRUDWindow window = new SummaryCRUDWindow();
        window.setQuery((PatientSummaryQuery) getQuery());
        return window;
    }

    /**
     * Creates a new query for the visits view.
     *
     * @param party the party to query acts for
     * @return a new query
     */
    protected ActQuery<Act> createQuery(Party party) {
        return new PatientSummaryQuery(party);
    }

    /**
     * Creates a new browser to query and display acts.
     *
     * @param query the query
     * @return a new browser
     */
    @Override
    protected Browser<Act> createBrowser(ActQuery<Act> query) {
        RecordBrowser browser = new RecordBrowser((PatientSummaryQuery) query,
                                                  createProblemsQuery(),
                                                  createReminderAlertQuery(),
                                                  createDocumentQuery(),
                                                  createChargesQuery());
        browser.setListener(new RecordBrowserListener() {
            public void onViewChanged() {
                changeCRUDWindow();
            }
        });
        return browser;
    }

    /**
     * Invoked when an act is selected.
     *
     * @param act the act
     */
    @Override
    protected void actSelected(Act act) {
        super.actSelected(act);
        CRUDWindow<Act> window = getCRUDWindow();
        if (window instanceof PatientRecordCRUDWindow) {
            Act event = getEvent(act);
            ((PatientRecordCRUDWindow) window).setEvent(event);
        }
    }

    /**
     * Changes the CRUD window depending on the current browser view.
     */
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
            String type = Messages.get("patient.document.createtype");
            window = new DocumentCRUDWindow(type, DOCUMENT_SHORT_NAMES);
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
        String[] shortNames = {CLINICAL_PROBLEM};
        String[] statuses = {};
        DefaultActQuery<Act> query = new DefaultActQuery<Act>(
                getObject(), "patient", "participation.patient", shortNames,
                statuses);
        query.setDefaultSortConstraint(DEFAULT_SORT);
        return query;
    }

    /**
     * Creates a new query, for the reminder/alert view.
     *
     * @return a new query
     */
    private Query<Act> createReminderAlertQuery() {
        String[] shortNames = {"act.patientReminder", "act.patientAlert"};
        List<Lookup> lookups = FastLookupHelper.getLookups(
                "act.patientReminder", "status");
        DefaultActQuery<Act> query = new DefaultActQuery<Act>(
                getObject(), "patient", "participation.patient", shortNames,
                lookups);
        query.setStatus(ActStatus.IN_PROGRESS);
        query.setDefaultSortConstraint(DEFAULT_SORT);
        return query;
    }

    /**
     * Creates a new query, for the document view.
     *
     * @return a new query
     */
    private Query<Act> createDocumentQuery() {
        List<Lookup> lookups = FastLookupHelper.getLookups(
                "act.patientDocumentLetter", "status");
        DefaultActQuery<Act> query = new DefaultActQuery<Act>(
                getObject(), "patient", "participation.patient",
                DOCUMENT_SHORT_NAMES, lookups);
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
            while (!(found = TypeHelper.isA(act, CLINICAL_EVENT))
                    && index > 0) {
                act = acts.get(--index);
            }
        }
        return (found) ? act : null;
    }

}
