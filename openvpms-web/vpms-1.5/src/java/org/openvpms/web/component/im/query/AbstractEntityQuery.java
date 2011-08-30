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

import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityIdentity;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.ArchetypeQueryException;
import org.openvpms.component.system.common.query.NodeSortConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.web.component.focus.FocusHelper;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Query component for {@link Entity} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractEntityQuery<T> extends AbstractArchetypeQuery<T> {

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
     * The default sort constraint.
     */
    private static final SortConstraint[] DEFAULT_SORT = {
            new NodeSortConstraint("name")
    };


    /**
     * Construct a new <tt>AbstractEntityQuery</tt> that queries Entity
     * instances with the specified short names.
     *
     * @param shortNames the short names
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public AbstractEntityQuery(String[] shortNames) {
        super(shortNames, true);
        setDefaultSortConstraint(DEFAULT_SORT);
        QueryFactory.initialise(this);
    }

    /**
     * Construct a new <tt>AbstractEntityQuery</tt> that queries Entity
     * instances with the specified short names.
     *
     * @param shortNames the short names
     * @param type       the type that this query returns
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public AbstractEntityQuery(String[] shortNames, Class type) {
        super(shortNames, type);
        setDefaultSortConstraint(DEFAULT_SORT);
        QueryFactory.initialise(this);
    }

    /**
     * Performs the query.
     *
     * @param sort the sort constraint. May be <tt>null</tt>
     * @return the query result set. May be <tt>null</tt>
     * @throws ArchetypeServiceException if the query fails
     */
    @Override
    public ResultSet<T> query(SortConstraint[] sort) {
        ResultSet<T> result = null;

        if (canQueryOnName()) {
            result = createResultSet(sort);
        } else {
            ErrorHelper.show(Messages.get("entityquery.error.nameLength",
                                          getValueMinLength()));
        }
        return result;
    }

    /**
     * Sets the value to query on.
     *
     * @param value the value. May contain wildcards, or be <tt>null</tt>
     */
    @Override
    public void setValue(String value) {
        super.setValue(value);
        checkIdentityName(value);
    }

    /**
     * Determines if the query should be an identity search or name search.
     * If an identity search, the name is used to search for entities
     * with a matching {@link EntityIdentity}.
     *
     * @return <tt>true</tt> if the query should be an identity search
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
        addSearchField(container);
        addIdentitySearch(container);
        addInactive(container);
        FocusHelper.setFocus(getSearchField());
    }

    /**
     * Invoked when the search field changes.
     * <p/>
     * This sets the identity search checkbox if the field contains a number.
     */
    @Override
    protected void onSearchFieldChanged() {
        String value = getValue();
        checkIdentityName(value);
        super.onSearchFieldChanged();
    }

    /**
     * Determines if a value may be an identity (i.e contains numerics).
     * If so, selects the 'identity search' box.
     *
     * @param value the name. May be <tt>null</tt>
     */
    private void checkIdentityName(String value) {
        if (value != null) {
            value = value.replaceAll("\\*", "");
            if (value.matches("\\d+")) {
                getIdentitySearch().setSelected(true);
            }
        }
    }

}
