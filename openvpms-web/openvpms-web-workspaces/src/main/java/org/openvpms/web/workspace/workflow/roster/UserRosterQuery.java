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

import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.archetype.rules.user.UserQueryFactory;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.lookup.ArchetypeLookupQuery;
import org.openvpms.web.component.im.lookup.LookupField;
import org.openvpms.web.component.im.lookup.LookupFieldFactory;
import org.openvpms.web.component.im.lookup.LookupQuery;
import org.openvpms.web.component.im.query.EntityResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.focus.FocusHelper;


/**
 * Queries rosters for users.
 *
 * @author Tim Anderson
 */
public class UserRosterQuery extends RosterQuery<User> {

    /**
     * Classification selector.
     */
    private LookupField classifications;

    /**
     * Constructs a {@link UserRosterQuery}.
     *
     * @param context the context
     */
    public UserRosterQuery(Context context) {
        super(UserArchetypes.USER_ARCHETYPES, false, User.class, context);
    }

    /**
     * Lays out the component in a container, and sets focus on the instance
     * name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        super.doLayout(container);
    }


    @Override
    protected void doQueryLayout(Component container) {
        addDate(container);
        addSearchField(container);
        LookupQuery lookups = new ArchetypeLookupQuery(UserArchetypes.USER_TYPE);
        classifications = LookupFieldFactory.create(lookups, true);
        classifications.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onQuery();
            }
        });

        container.add(LabelFactory.text(DescriptorHelper.getDisplayName(UserArchetypes.USER, "classifications")));
        container.add(classifications);
        getFocusGroup().add(classifications);
        addActive(container);
        FocusHelper.setFocus(getSearchField());
    }

    /**
     * Creates the result set.
     *
     * @param sort the sort criteria. May be {@code null}
     * @return a new result set
     */
    @Override
    protected ResultSet<User> createResultSet(SortConstraint[] sort) {
        return new EntityResultSet<User>(getArchetypeConstraint(), getValue(), false, null, sort,
                                         getMaxResults(), isDistinct()) {

            /**
             * Creates a new archetype query.
             *
             * @return a new archetype query
             */
            @Override
            protected ArchetypeQuery createQuery() {
                ArchetypeQuery query = super.createQuery();
                Party location = getContext().getLocation();
                if (location != null) {
                    UserQueryFactory.addLocationConstraint(location, query);
                }
                String classification = classifications.getSelectedCode();
                if (classification != null) {
                    query.add(Constraints.join("classifications").add(Constraints.eq("code", classification)));
                }
                return query;
            }
        };
    }

}
