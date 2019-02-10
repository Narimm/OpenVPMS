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
 * Copyright 2019 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.workflow.roster;

import org.openvpms.archetype.rules.workflow.ScheduleArchetypes;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.model.party.Party;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.edit.AbstractIMObjectReferenceEditor;
import org.openvpms.web.component.im.edit.IMObjectReferenceEditor;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.EmptyResultSet;
import org.openvpms.web.component.im.query.EntityObjectSetQuery;
import org.openvpms.web.component.im.query.EntityQuery;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.relationship.EntityLinkEditor;
import org.openvpms.web.component.property.Property;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;

/**
 * An editor for <em>entityLink.rosterAreaSchedule</em> relationships.
 * <p/>
 * This constrains schedules to those with the same location as the parent <em>entity.rosterArea</em>
 *
 * @author Tim Anderson
 */
public class RosterAreaScheduleEditor extends EntityLinkEditor {

    /**
     * The location.
     */
    private Party location;

    /**
     * Constructs an {@link RosterAreaScheduleEditor}.
     *
     * @param relationship the link
     * @param parent       the parent object
     * @param context      the layout context
     */
    public RosterAreaScheduleEditor(EntityLink relationship, Entity parent, LayoutContext context) {
        super(relationship, parent, context);
        location = (parent != null) ? (Party) getObject(getBean(parent).getTargetRef("location")) : null;
    }

    /**
     * Sets the practice location of the roster area.
     *
     * @param location the practice location. May be {@code null}
     */
    public void setLocation(Party location) {
        this.location = location;
        resetValid();
    }

    /**
     * Creates a new editor for the relationship target.
     *
     * @param property the target property
     * @param context  the layout context
     * @return a new reference editor
     */
    @Override
    protected IMObjectReferenceEditor<Entity> createTargetEditor(Property property, LayoutContext context) {
        return new AbstractIMObjectReferenceEditor<Entity>(property, getObject(), context) {

            @Override
            protected Query<Entity> createQuery(String name) {
                return new EntityQuery<>(new ScheduleObjectSetQuery(), context.getContext());
            }

            /**
             * Creates a validator error for an invalid reference.
             *
             * @param reference the reference. Never null
             * @return a new error
             */
            @Override
            protected ValidatorError createValidatorError(Reference reference) {
                Party schedule = (Party) RosterAreaScheduleEditor.this.getObject(reference);
                if (location != null && schedule != null) {
                    String message = Messages.format("workflow.rostering.invalidschedule", schedule.getName(),
                                                     location.getName());
                    return new ValidatorError(getProperty(), message);
                } else {
                    return super.createValidatorError(reference);
                }
            }
        };
    }

    /**
     * Restricts schedules to the roster area location.
     */
    private class ScheduleObjectSetQuery extends EntityObjectSetQuery {

        /**
         * Constructs a {@link ScheduleObjectSetQuery}.
         */
        ScheduleObjectSetQuery() {
            super(new String[]{ScheduleArchetypes.ORGANISATION_SCHEDULE});
            setAuto(true);
        }

        /**
         * Creates the result set.
         *
         * @param sort the sort criteria. May be {@code null}
         * @return a new result set
         */
        @Override
        protected ResultSet<ObjectSet> createResultSet(SortConstraint[] sort) {
            ResultSet<ObjectSet> set;
            if (location != null) {
                setConstraints(Constraints.join("location").add(Constraints.eq("target", location)));
                set = super.createResultSet(sort);
            } else {
                // no location set, so return an empty set
                set = new EmptyResultSet<>(getMaxResults());
            }
            return set;
        }
    }
}
