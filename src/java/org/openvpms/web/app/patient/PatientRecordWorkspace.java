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

package org.openvpms.web.app.patient;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.app.subsystem.ActWorkspace;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.ShortNames;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.create.IMObjectCreator;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.query.ActQuery;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.spring.ServiceHelper;

import java.util.ArrayList;
import java.util.List;


/**
 * Patient medical reocrd workspace.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PatientRecordWorkspace extends ActWorkspace {

    /**
     * The selected act.
     */
    private Act _selected;

    /**
     * Clinical episode act short name.
     */
    private static final String CLINICAL_EPISODE = "act.patientClinicalEpisode";

    /**
     * Clinical event act short name.
     */
    private static final String CLINICAL_EVENT = "act.patientClinicalEvent";

    /**
     * Clinical problem act short name.
     */
    private static final String CLINICAL_PROBLEM = "act.patientClinicalProblem";

    /**
     * Clinical episode event act relationship short name.
     */
    private static final String RELATIONSHIP_CLINICAL_EPISODE_EVENT
            = "actRelationship.patientClinicalEpisodeEvent";

    /**
     * Clinical event item  act relationship short name,
     */
    private static final String RELATIONSHIP_CLINICAL_EVENT_ITEM
            = "actRelationship.patientClinicalEventItem";


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
        Context.getInstance().setPatient(party);
        layoutWorkspace(party, getRootComponent());
        initQuery(party);
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
                "PatientRecordWorkspace.Layout", browser.getComponent(),
                window.getComponent());
    }

    /**
     * Creates a new CRUD window for viewing and editing acts.
     *
     * @return a new CRUD window
     */
    protected CRUDWindow createCRUDWindow() {
        String type = Messages.get("patient.record.createtype");
        return new PatientRecordCRUDWindow(type, new ShortNameResolver());
    }

    /**
     * Invoked when an act is selected.
     *
     * @param act the act
     */
    @Override
    protected void actSelected(Act act) {
        super.actSelected(act);
        _selected = act;
    }

    /**
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(IMObject object, boolean isNew) {
        if (isNew) {
            if (IMObjectHelper.isA(object, CLINICAL_EVENT)) {
                addActRelationship((Act) object, CLINICAL_EPISODE,
                        RELATIONSHIP_CLINICAL_EPISODE_EVENT);
            } else if (IMObjectHelper.isA(object, CLINICAL_PROBLEM)) {
                addActRelationship((Act) object, CLINICAL_EVENT,
                        RELATIONSHIP_CLINICAL_EVENT_ITEM);
            }
        }
        super.onSaved(object, isNew);
    }

    /**
     * Invoked when the object has been deleted.
     *
     * @param object the object
     */
    @Override
    protected void onDeleted(IMObject object) {
        super.onDeleted(object);
        _selected = null;
    }


    /**
     * Creates a new query for visits view.
     *
     * @param party the party to query acts for
     * @return a new query
     */
    protected ActQuery createQuery(Party party) {
        String[] shortNames = {};
        ArchetypeDescriptor archetype = DescriptorHelper.getArchetypeDescriptor(
                RELATIONSHIP_CLINICAL_EVENT_ITEM);
        if (archetype != null) {
            NodeDescriptor target = archetype.getNodeDescriptor("target");
            if (target != null) {
                shortNames = DescriptorHelper.getShortNames(target);
            }
        }
        return createQuery(party, shortNames);
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
        return new RecordBrowser(query, createProblemsQuery(), sort);
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

    /**
     * Returns the act with the specified short name, recursively navigating to
     * the parent until either one is found, or there are no more parent acts.
     *
     * @param act       the act
     * @param shortName the archetype shortname
     * @return the act or a parent, or <code>null</code> if none was found
     */
    private Act getAct(Act act, String shortName) {
        Act result = null;
        if (act != null) {
            if (IMObjectHelper.isA(act, shortName)) {
                result = act;
            } else {
                RecordBrowser browser = (RecordBrowser) getBrowser();
                Act parent = browser.getParent(act);
                result = getAct(parent, shortName);
            }
        }
        return result;
    }

    /**
     * Adds a relationship between two acts.
     *
     * @param act              the act
     * @param parentType       the type of the parent act
     * @param relationshipType the type of the relationship to addd
     */
    private void addActRelationship(Act act, String parentType,
                                    String relationshipType) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        Act parent = getAct(_selected, parentType);
        if (parent != null) {
            try {
                ActRelationship relationship
                        = (ActRelationship) IMObjectCreator.create(
                        relationshipType);
                if (relationship != null) {
                    relationship.setSource(parent.getObjectReference());
                    relationship.setTarget(act.getObjectReference());
                    parent.addActRelationship(relationship);
                    SaveHelper.save(parent, service);
                }
            } catch (OpenVPMSException exception) {
                ErrorHelper.show(exception);
            }
        } else {
            String name = DescriptorHelper.getDisplayName(act);
            String message = Messages.get("patient.record.create.noparent");
            ErrorHelper.show(message, name);
        }
    }

    class ShortNameResolver implements ShortNames {

        /**
         * Returns the archetype short names.
         *
         * @return the archetype short names
         */
        public String[] getShortNames() {
            RecordBrowser browser = (RecordBrowser) getBrowser();
            Act act = browser.getSelected();
            boolean isProblem = false;
            boolean isEvent = false;
            boolean isEpisode = false;
            List<String> names = new ArrayList<String>();
            if (IMObjectHelper.isA(act, CLINICAL_PROBLEM)) {
                isProblem = true;
            } else if (IMObjectHelper.isA(act, CLINICAL_EVENT)) {
                isEvent = true;
            } else if (IMObjectHelper.isA(act, CLINICAL_EPISODE)) {
                isEpisode = true;
            }
            if (isProblem || isEvent || isEpisode) {
                names.add(CLINICAL_EVENT);
                names.add(CLINICAL_EPISODE);
            }
            if (isProblem || isEvent) {
                names.add(0, CLINICAL_PROBLEM);
            }
            return names.toArray(new String[0]);
        }
    }
}
