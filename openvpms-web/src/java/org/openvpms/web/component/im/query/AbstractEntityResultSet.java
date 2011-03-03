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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.im.query;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;
import org.openvpms.component.system.common.query.JoinConstraint;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;


/**
 * Result set for {@link Entity} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-08-22 05:39:25Z $
 */
public abstract class AbstractEntityResultSet<T> extends AbstractIMObjectResultSet<T> {

    /**
     * Identity short names. Non-null if an instance name has been set, and identities are being searched,
     * and the entities have an "identities" node
     */
    private String[] identityShortNames;


    /**
     * Creates a new <tt>AbstractEntityResultSet</tt>.
     *
     * @param archetypes       the archetypes to query
     * @param value            the value to query on. May be <tt>null</tt>
     * @param searchIdentities if <tt>true</tt> search on identity name
     * @param constraints      additional query constraints. May be <tt>null</tt>
     * @param sort             the sort criteria. May be <tt>null</tt>
     * @param rows             the maximum no. of rows per page
     * @param distinct         if <tt>true</tt> filter duplicate rows
     * @param executor         the query executor
     */
    public AbstractEntityResultSet(ShortNameConstraint archetypes, String value, boolean searchIdentities,
                                   IConstraint constraints, SortConstraint[] sort, int rows, boolean distinct,
                                   QueryExecutor<T> executor) {
        super(archetypes, value, constraints, sort, rows, distinct, executor);
        if (searchIdentities) {
            // determine if "identities" nodes exist for each archetype.
            Set<String> shortNames = new HashSet<String>();
            List<ArchetypeDescriptor> matches = getArchetypes(archetypes);
            boolean identities = true;
            for (ArchetypeDescriptor archetype : matches) {
                NodeDescriptor ident = archetype.getNodeDescriptor("identities");
                if (ident == null) {
                    identities = false;
                    break;
                } else {
                    shortNames.addAll(Arrays.asList(DescriptorHelper.getShortNames(ident)));
                }
            }
            if (identities) {
                identityShortNames = shortNames.toArray(new String[shortNames.size()]);
                setDistinct(true);
            }
        }
    }

    /**
     * Determines if the <em>identities</em> node is being searched.
     *
     * @return <tt>true</tt> if identities are being searched
     */
    public boolean isSearchingIdentities() {
        return identityShortNames != null;
    }

    /**
     * Creates a new archetype query.
     *
     * @return a new archetype query
     */
    @Override
    protected ArchetypeQuery createQuery() {
        ArchetypeQuery query;
        if (identityShortNames == null) {
            query = super.createQuery();
        } else {
            query = new ArchetypeQuery(getArchetypes());
            JoinConstraint identities = Constraints.leftJoin("identities", Constraints.shortName(
                    "identity", identityShortNames, true));
            query.add(identities);
            String value = getValue();
            if (!StringUtils.isEmpty(getValue())) {
                IConstraint identName = Constraints.eq("identity.name", value);
                Long id = getId(value);
                if (id != null) {
                    query.add(Constraints.or(Constraints.eq("id", id), identName));
                } else {
                    query.add(identName);
                }
            }
        }
        return query;
    }

}
