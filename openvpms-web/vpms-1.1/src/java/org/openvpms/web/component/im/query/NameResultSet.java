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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IConstraint;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.ShortNameConstraint;
import org.openvpms.component.system.common.query.SortConstraint;

import java.util.ArrayList;
import java.util.List;


/**
 * Result set that queries on archetype and instance name.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class NameResultSet<T extends IMObject>
        extends AbstractArchetypeServiceResultSet<T> {

    /**
     * The archetypes to query.
     */
    private final ShortNameConstraint archetypes;

    /**
     * The instance name.
     */
    private final String instanceName;

    /**
     * The logger.
     */
    private final Log log = LogFactory.getLog(NameResultSet.class);


    /**
     * Construct a new <code>NameResultSet</code>.
     *
     * @param archetypes   the archetypes to query
     * @param instanceName the instance name
     * @param constraints  additional query constraints. May be
     *                     <code<null</code>
     * @param sort         the sort criteria. May be <code>null</code>
     * @param rows         the maximum no. of rows per page
     * @param distinct     if <code>true</code> filter duplicate rows
     */
    public NameResultSet(ShortNameConstraint archetypes,
                         String instanceName, IConstraint constraints,
                         SortConstraint[] sort, int rows, boolean distinct) {
        super(constraints, rows, sort);
        this.archetypes = archetypes;
        this.instanceName = instanceName;
        setDistinct(distinct);
    }

    /**
     * Returns the archetypes.
     *
     * @return the archetypes
     */
    protected ShortNameConstraint getArchetypes() {
        return archetypes;
    }

    /**
     * Returns the instance name.
     *
     * @return the instance name. May be <tt>null</tt>
     */
    protected String getInstanceName() {
        return instanceName;
    }

    /**
     * Creates a new archetype query.
     *
     * @return a new archetype query
     */
    protected ArchetypeQuery createQuery() {
        ArchetypeQuery query = new ArchetypeQuery(archetypes);
        if (!StringUtils.isEmpty(instanceName)) {
            query.add(new NodeConstraint("name", instanceName));
        }
        return query;
    }

    /**
     * Returns all archetypes matching supplied constraint.
     *
     * @param archetypes the archetype constraint
     * @return the archetypes matching the constraint
     * @throws OpenVPMSException for any error
     */
    protected List<ArchetypeDescriptor> getArchetypes(
            ShortNameConstraint archetypes) {
        IArchetypeService service
                = ArchetypeServiceHelper.getArchetypeService();
        String[] shortNames = DescriptorHelper.getShortNames(
                archetypes.getShortNames());
        List<ArchetypeDescriptor> result
                = new ArrayList<ArchetypeDescriptor>(shortNames.length);
        for (String shortName : shortNames) {
            ArchetypeDescriptor archetype = service.getArchetypeDescriptor(
                    shortName);
            if (archetype != null) {
                result.add(archetype);
            }
        }

        return result;
    }

    /**
     * Determines if a node can be sorted on.
     *
     * @param node the node
     * @return <code>true</code> if the node can be sorted on, otherwise
     *         <code>false</code>
     */
    private boolean isValidSortNode(String node) {
        List<ArchetypeDescriptor> archetypes = getArchetypes(this.archetypes);
        NodeDescriptor descriptor;

        for (ArchetypeDescriptor archetype : archetypes) {
            descriptor = archetype.getNodeDescriptor(node);
            if (descriptor == null) {
                log.warn("Can't sort results on node=" + node
                        + ". Node not supported by archetype="
                        + archetype.getName());
                return false;
            }
        }
        return true;
    }

}
