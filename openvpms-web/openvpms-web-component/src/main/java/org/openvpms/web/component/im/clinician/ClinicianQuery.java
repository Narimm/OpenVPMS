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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.clinician;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.user.ClinicianQueryFactory;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.query.EntityObjectSetQuery;
import org.openvpms.web.component.im.query.EntityObjectSetResultSet;
import org.openvpms.web.component.im.query.EntityQuery;
import org.openvpms.web.component.im.query.QueryFactory;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.echo.button.CheckBox;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.LabelFactory;

/**
 * Queries clinicians.
 * <p>
 * In multi-site practices, only those clinicians available at the current location will be selected by default.
 *
 * @author Tim Anderson
 */
public class ClinicianQuery extends EntityQuery<User> {

    /**
     * Constructs a {@link ClinicianQuery}.
     *
     * @param context the context
     */
    public ClinicianQuery(Context context) {
        this(new String[]{UserArchetypes.USER}, context);
    }

    /**
     * Constructs an {@link ClinicianQuery} that queries entities with the specified short names.
     *
     * @param shortNames the short names
     * @param context    the context
     * @throws ArchetypeQueryException if the short names don't match any archetypes
     */
    public ClinicianQuery(String[] shortNames, Context context) {
        super(new ClinicianObjectSetQuery(shortNames, context.getLocation(), context.getPractice()), context);
        QueryFactory.initialise(this);
    }

    /**
     * Determines if clinicians for all locations should be displayed.
     *
     * @param allLocations if {@code true}, display clinicians for all locations
     */
    public void setAllLocations(boolean allLocations) {
        ClinicianObjectSetQuery query = (ClinicianObjectSetQuery) getQuery();
        if (query.allLocations != null) {
            query.allLocations.setSelected(allLocations);
        }
    }

    private static class ClinicianObjectSetQuery extends EntityObjectSetQuery {

        /**
         * The practice location to filter clinicians by, or {@code null} if this is a single location practice.
         */
        private final Party location;

        /**
         * Determines if clinicians from all locations should be displayed.
         */
        private final CheckBox allLocations;

        /**
         * Constructs a {@link ClinicianObjectSetQuery}.
         *
         * @param shortNames the short names
         * @param location   the practice location. May be {@code null}
         * @param practice   the practice, May be {@code null}
         * @throws ArchetypeQueryException if the short names don't match any archetypes
         */
        public ClinicianObjectSetQuery(String[] shortNames, Party location, Party practice) {
            super(shortNames);
            if (location != null && hasMultipleLocations(practice)) {
                // if the practice has more than one location, display the All Locations checkbox
                this.location = location;
                allLocations = CheckBoxFactory.create();
                allLocations.addActionListener(new ActionListener() {
                    @Override
                    public void onAction(ActionEvent event) {
                        onQuery();
                    }
                });
            } else {
                this.location = null;
                allLocations = null;
            }
        }

        /**
         * Determines if the practice has multiple locations.
         *
         * @param practice the practice. May be {@code null}
         * @return {@code true} if the practice has multiple locations
         */
        protected boolean hasMultipleLocations(Party practice) {
            if (practice != null) {
                IMObjectBean bean = new IMObjectBean(practice);
                return bean.getNodeTargetObjectRefs("locations").size() > 1;
            }
            return false;
        }

        /**
         * Lays out the component in a container, and sets focus on the instance name.
         *
         * @param container the container
         */
        @Override
        protected void doLayout(Component container) {
            super.doLayout(container);
            if (allLocations != null) {
                container.add(LabelFactory.create("location.all"));
                container.add(allLocations);
            }
        }

        /**
         * Creates the result set.
         *
         * @param sort the sort criteria. May be {@code null}
         * @return a new result set
         */
        @Override
        protected ResultSet<ObjectSet> createResultSet(SortConstraint[] sort) {
            return new ClinicianResultSet(getArchetypeConstraint(), getValue(), isIdentitySearch(), sort,
                                          getMaxResults(), isDistinct(), getLocation());
        }

        /**
         * Returns the practice location to filter clinicians by.
         *
         * @return the practice location, or {@code null} if locations aren't being filtered
         */
        private Party getLocation() {
            Party result = null;
            if (allLocations != null && !allLocations.isSelected()) {
                result = location;
            }
            return result;
        }
    }

    private static class ClinicianResultSet extends EntityObjectSetResultSet {

        /**
         * The practice location to filter clinicians by.
         */
        private final Party location;

        /**
         * Constructs an {@link ClinicianResultSet}.
         *
         * @param archetypes       the archetypes to query
         * @param value            the value to query on. May be {@code null}
         * @param searchIdentities if {@code true} search on identity name
         * @param sort             the sort criteria. May be {@code null}
         * @param rows             the maximum no. of rows per page
         * @param distinct         if {@code true} filter duplicate rows
         * @param location         the practice location to filter clinicians by. May be {@code null}
         */
        public ClinicianResultSet(ShortNameConstraint archetypes, String value, boolean searchIdentities,
                                  SortConstraint[] sort, int rows, boolean distinct, Party location) {
            super(archetypes, value, searchIdentities, sort, rows, distinct);
            this.location = location;
        }

        /**
         * Creates a new archetype query.
         *
         * @return a new archetype query
         */
        @Override
        protected ArchetypeQuery createQuery() {
            ArchetypeQuery query = super.createQuery();
            ClinicianQueryFactory.addClinicianConstraint(query);
            if (location != null) {
                ClinicianQueryFactory.addLocationConstraint(location, query);
            }
            return query;
        }
    }

}
