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

import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.event.ActionListener;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.app.subsystem.ActWorkspace;
import org.openvpms.web.app.subsystem.CRUDWindow;
import org.openvpms.web.app.subsystem.ShortNames;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.query.*;
import org.openvpms.web.component.im.tree.ActTreeNodeFactory;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.RowFactory;
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
     * The current clinical episode.
     */
    private Act _clinicalEpisode;

    /**
     * The current clinical event;
     */
    private Act _clinicalEvent;

    /**
     * The current clinical problem;
     */
    private Act _clinicalProblem;

    /**
     * Flag to denote the current view. If <code>true</code>, in 'visit'
     * view, otherwise in 'problem' view.
     */
    private boolean _visitView = true;

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
     * This implementation returns the acts displayed in a group box.
     *
     * @param acts the act browser
     * @return a component representing the acts
     */
    @Override
    protected Component getActs(Browser acts) {
        Component box = super.getActs(acts);
        Button visits = ButtonFactory.create("visit", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onViewChanged(true);
            }
        });
        Button problems = ButtonFactory.create("problem", new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                onViewChanged(false);
            }
        });
        Row row = RowFactory.create("CellSpacing", visits, problems);
        return SplitPaneFactory.create(
                SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP,
                "SplitPane.Dialog", row, box);
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
     * Invoked when the object has been saved.
     *
     * @param object the object
     * @param isNew  determines if the object is a new instance
     */
    @Override
    protected void onSaved(IMObject object, boolean isNew) {
        if (isNew) {
            if (IMObjectHelper.isA(object, CLINICAL_EVENT)) {
                if (_clinicalEpisode != null) {
                    addActRelationship(RELATIONSHIP_CLINICAL_EPISODE_EVENT,
                            _clinicalEpisode, (Act) object);
                }
            } else if (IMObjectHelper.isA(object, CLINICAL_PROBLEM)) {
                if (_clinicalEvent != null) {
                    addActRelationship(RELATIONSHIP_CLINICAL_EVENT_ITEM,
                            _clinicalEvent, (Act) object);
                }
            }
        }
        super.onSaved(object, isNew);
    }

    /**
     * Creates a new query.
     *
     * @param party the party to query acts for
     * @return a new query
     */
    protected ActQuery createQuery(Party party) {
        String[] shortNames = {CLINICAL_EPISODE};
        String[] statuses = {};

        return new ActQuery(party, "patient", "participation.patient",
                shortNames, statuses);
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
     * Creates a new browser to query and display acts.
     *
     * @param query the query
     * @return a new browser
     */
    @Override
    protected Browser<Act> createBrowser(Query<Act> query) {
        if (_visitView) {
            return new TreeBrowser<Act>(query, null, new ActTreeNodeFactory());
        }
        return new ProblemTreeBrowser(query, null);
    }

    /**
     * Invoked when an act is selected.
     *
     * @param act the act
     */
    @Override
    protected void actSelected(Act act) {
        super.actSelected(act);
        if (IMObjectHelper.isA(act, CLINICAL_EPISODE)) {
            _clinicalEpisode = act;
            _clinicalEvent = null;
            _clinicalProblem = null;
        } else if (IMObjectHelper.isA(act, CLINICAL_EVENT)) {
            _clinicalEpisode = getParent(act, CLINICAL_EPISODE);
            _clinicalEvent = act;
            _clinicalProblem = null;
        } else if (IMObjectHelper.isA(act, CLINICAL_PROBLEM)) {
            _clinicalProblem = act;
            _clinicalEvent =
                    getParent(_clinicalProblem, CLINICAL_EVENT);
            _clinicalEpisode
                    = getParent(_clinicalEvent, CLINICAL_EPISODE);
        } else {
            _clinicalEpisode = null;
            _clinicalEvent = null;
            _clinicalProblem = null;
        }
    }

    /**
     * Invoked to change the current view.
     *
     * @param visit if <code>true</code> indicates to show the 'visit' view,
     *              otherwise indicates to show the 'problem' view
     */
    private void onViewChanged(boolean visit) {
        if (visit != _visitView) {
            _visitView = visit;
            setBrowser(createBrowser(getQuery()));
            initQuery((Party) getObject());
        }
    }

    /**
     * Returns the parent of act.
     *
     * @param act       the act. May be <code>null</code>
     * @param shortName the parent's archetype shortname
     * @return the act parent, or <code>null</code> if none was found
     */
    private Act getParent(Act act, String shortName) {
        if (act != null) {
            AbstractTreeBrowser<Act> browser
                    = (AbstractTreeBrowser<Act>) getBrowser();
            Act parent = browser.getParent(act);
            if (IMObjectHelper.isA(parent, shortName)) {
                return parent;
            }
        }
        return null;
    }

    /**
     * Adds a relationship between two acts.
     *
     * @param type   the relationship type
     * @param parent the parent act
     * @param child  the child act
     */
    private void addActRelationship(String type, Act parent, Act child) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        ActRelationship relationship = (ActRelationship) service.create(type);
        if (relationship != null) {
            relationship.setSource(parent.getObjectReference());
            relationship.setTarget(child.getObjectReference());
            if (SaveHelper.save(relationship, service)) {
                parent.addActRelationship(relationship);
                SaveHelper.save(relationship, service);
            }
        }
    }

    class ShortNameResolver implements ShortNames {

        /**
         * Returns the archetype short names.
         *
         * @return the archetype short names
         */
        public String[] getShortNames() {
            String[] shortNames;

            if (_visitView) {
                List<String> names = new ArrayList<String>();
                if (_clinicalProblem != null) {
                }
                if (_clinicalEvent != null) {
                    names.add(CLINICAL_PROBLEM);
                }
                if (_clinicalEpisode != null) {
                    names.add(CLINICAL_EVENT);
                }
                names.add(CLINICAL_EPISODE);
                shortNames = names.toArray(new String[0]);
            } else {
                if (_clinicalEvent != null) {
                    shortNames = new String[]{CLINICAL_PROBLEM};
                } else {
                    shortNames = new String[0];
                }
            }
            return shortNames;
        }
    }
}
