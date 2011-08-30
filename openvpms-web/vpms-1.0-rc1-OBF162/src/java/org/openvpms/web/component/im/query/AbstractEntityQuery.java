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

package org.openvpms.web.component.im.query;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.im.list.ShortNameListModel;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Query component for {@link Entity} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractEntityQuery<T extends Entity>
        extends AbstractIMObjectQuery<T> {

    /**
     * The identity search check box. If selected, name searches will be
     * performed against the entities {@link EntityIdentity} instances.
     */
    private CheckBox identity;

    /**
     * Identity search label id.
     */
    private static final String IDENTITY_SEARCH_ID = "entityquery.identity";


    /**
     * Construct a new <code>AbstractEntityQuery</code> that queries Entity
     * instances with the specified short names.
     *
     * @param shortNames the short names
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public AbstractEntityQuery(String[] shortNames) {
        super(shortNames);
        QueryFactory.initialise(this);
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <code>null</code>
     * @return the query result set
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public ResultSet<T> query(SortConstraint[] sort) {
        String type = getShortName();
        String name = getName();
        boolean activeOnly = !includeInactive();
        ResultSet<T> result;

        if (canQueryOnName()) {
            ShortNameConstraint archetypes;
            if (type == null || type.equals(ShortNameListModel.ALL)) {
                archetypes = getArchetypes();
                archetypes.setActiveOnly(activeOnly);
            } else {
                archetypes = new ShortNameConstraint(type, true, activeOnly);
            }
            result = new EntityResultSet<T>(archetypes, name,
                                            isIdentitySearch(),
                                            getConstraints(),
                                            sort, getMaxResults(),
                                            isDistinct());
        } else {
            ErrorHelper.show(Messages.get("entityquery.error.nameLength",
                                          getNameMinLength()));
            result = new EmptyResultSet<T>(getMaxResults());
        }
        return result;
    }

    /**
     * Determines if the query should be an identity search or name search.
     * If an identity search, the name is used to search for entities
     * with a matching {@link EntityIdentity}.
     *
     * @return <code>true</code> if the query should be an identity search
     */
    protected boolean isIdentitySearch() {
        return getIdentitySearch().isSelected();
    }

    /**
     * Returns the identity search checkbox.
     *
     * @return the identity search chechbox
     */
    protected CheckBox getIdentitySearch() {
        if (identity == null) {
            identity = new CheckBox();
            identity.setSelected(false);
        }
        return identity;
    }

    /**
     * Adds the identity search checkbox to a container.
     *
     * @param container the container
     */
    protected void addIdentitySearch(Component container) {
        Label label = LabelFactory.create(IDENTITY_SEARCH_ID);
        container.add(label);
        container.add(getIdentitySearch());
        getFocusGroup().add(identity);
    }

    /**
     * Lays out the component in a container, and sets focus on the instance
     * name.
     *
     * @param container the container
     */
    protected void doLayout(Component container) {
        addShortNameSelector(container);
        addInstanceName(container);
        addIdentitySearch(container);
        addInactive(container);
        ApplicationInstance.getActive().setFocusedComponent(getInstanceName());
    }

    /**
     * Invoked when the instance name changes.
     * This sets the identity search checkbox if the name contains a number.
     */
    @Override
    protected void onInstanceNameChanged() {
        String name = getName();
        if (name != null && name.matches(".*\\d+.*")) {
            getIdentitySearch().setSelected(true);
        }
        super.onInstanceNameChanged();
    }

}
