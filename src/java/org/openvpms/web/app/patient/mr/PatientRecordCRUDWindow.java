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
 *  $Id: PatientRecordCRUDWindow.java 942 2006-05-30 07:52:45Z tanderson $
 */

package org.openvpms.web.app.patient.mr;

import static org.openvpms.web.app.patient.mr.PatientRecordTypes.RELATIONSHIP_CLINICAL_EVENT_ITEM;
import org.openvpms.web.app.subsystem.ActCRUDWindow;
import org.openvpms.web.app.subsystem.ShortNames;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.dialog.ErrorDialog;
import org.openvpms.web.component.im.create.IMObjectCreator;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.edit.act.ActHelper;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.util.ErrorHelper;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.spring.ServiceHelper;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;

import nextapp.echo2.app.Row;


/**
 * CRUD Window for patient record acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-30 07:52:45Z $
 */
public abstract class PatientRecordCRUDWindow extends ActCRUDWindow {

    /**
     * Clinical event item short names.
     */
    private final String[] _clinicalEventItems;

    /**
     * The short names that this may create.
     */
    private ShortNameResolver _shortNames;

    /**
     * The act used to determine the short names of the archetypes that
     * may be created.
     */
    private Act _act;


    /**
     * Create a new <code>PatientRecordCRUDWindow</code>.
     *
     * @param shortNames the short names of archetypes that this may create
     */
    public PatientRecordCRUDWindow(RecordShortNames shortNames) {
        super(Messages.get("patient.record.createtype"), null);
        _clinicalEventItems = ActHelper.getTargetShortNames(
                RELATIONSHIP_CLINICAL_EVENT_ITEM);
        _shortNames = new ShortNameResolver(shortNames);
    }

    /**
     * Sets the act used to determine the short names of the archetypes that
     * may be created.
     *
     * @param act the act. May be <code>null</code>
     */
    public void setAct(Act act) {
        _act = act;
    }

    /**
     * Sets the object.
     *
     * @param object the object. May be <code>null</code>
     */
    @Override
    public void setObject(IMObject object) {
        super.setObject(object);
        _act = (Act) object;
    }

    /**
     * Returns the short names of the archetypes that this may create.
     *
     * @return the short names
     */
    @Override
    protected ShortNames getShortNames() {
        return _shortNames;
    }

    /**
     * Invoked when the 'new' button is pressed.
     *
     * @param type       localised type display name
     * @param shortNames the short names
     */
    @Override
    protected void onCreate(String type, ShortNames shortNames) {
        String[] names = shortNames.getShortNames();
        if (names.length == 0) {
            // haven't got a current event for the view
            ErrorDialog.show(Messages.get("patient.record.create.noevent"));
        } else {
            super.onCreate(type, shortNames);
        }
    }

    /**
     * Invoked when a new object has been created.
     *
     * @param object the new object
     */
    @Override
    protected void onCreated(IMObject object) {
        Act act = (Act) object;
        Party patient = Context.getInstance().getPatient();
        if (patient != null) {
            try {
                IArchetypeService service
                        = ServiceHelper.getArchetypeService();
                Participation participation
                        = (Participation) service.create(
                        "participation.patient");
                participation.setEntity(new IMObjectReference(patient));
                participation.setAct(new IMObjectReference(act));
                act.addParticipation(participation);
            } catch (OpenVPMSException exception) {
                ErrorHelper.show(exception);
            }
        }
        super.onCreated(object);
    }

    /**
     * Determines if an act can be edited.
     *
     * @param act the act
     * @return <code>true</code> if the act can be edited, otherwise
     *         <code>false</code>
     */
    @Override
    protected boolean canEdit(Act act) {
        // @todo fix when statuses are sorted out
        return true;
    }

    /**
     * Lays out the buttons.
     *
     * @param buttons the button row
     */
    @Override
    protected void layoutButtons(Row buttons) {
        buttons.add(getEditButton());
        buttons.add(getCreateButton());
        buttons.add(getDeleteButton());
        buttons.add(getPrintButton());
    }

    /**
     * Enables/disables the buttons that require an object to be selected.
     *
     * @param enable determines if buttons should be enabled
     */
    @Override
    protected void enableButtons(boolean enable) {
        Row buttons = getButtons();
        buttons.removeAll();
        if (enable) {
            buttons.add(getEditButton());
            buttons.add(getCreateButton());
            buttons.add(getDeleteButton());
            buttons.add(getPrintButton());
        } else {
            buttons.add(getCreateButton());
        }
    }

    /**
     * Helper to return the short names of acts that may be added to
     * <em>actRelationship.patientClinicalEventItem</em>.
     *
     * @return the short names
     */
    protected String[] getClinicalEventItemShortNames() {
        return _clinicalEventItems;
    }

    /**
     * Adds a relationship between two acts.
     *
     * @param act              the act
     * @param parentType       the type of the parent act
     * @param relationshipType the type of the relationship to add
     */
    protected void addActRelationship(Act act, String parentType,
                                      String relationshipType) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        Act parent = ActHelper.getActOrParent(_act, parentType);
        if (parent == null) {
            parent = ActHelper.getActOrChild(_act, parentType);
        }
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
            String message = Messages.get("patient.record.create.noparent",
                                          name);
            ErrorHelper.show(message);
        }
    }

    private class ShortNameResolver implements ShortNames {

        /**
         * The short names to delegate to.
         */
        private RecordShortNames _shortNames;

        /**
         * Construct a new <code>ShortNameResolver</code>.
         *
         * @param shortNames the short names to delegate to
         */
        public ShortNameResolver(RecordShortNames shortNames) {
            _shortNames = shortNames;
        }

        /**
         * Returns the archetype short names.
         *
         * @return the archetype short names
         */
        public String[] getShortNames() {
            _shortNames.setAct(_act);
            return _shortNames.getShortNames();
        }
    }

}
