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

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.system.common.query.ArchetypeQueryException;

import java.util.List;


/**
 * Default query component for {@link Act} instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class DefaultActQuery<T extends Act> extends DateRangeActQuery<T> {

    /**
     * Constructs a new <tt>DefaultActQuery</tt>.
     *
     * @param entity        the entity to search for
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param entityName    the act entity name
     * @param conceptName   the act concept name
     * @param statusLookups the act status lookups
     */
    public DefaultActQuery(Entity entity, String participant,
                           String participation,
                           String entityName, String conceptName,
                           List<Lookup> statusLookups) {
        this(entity, participant, participation, entityName, conceptName,
             statusLookups, null);
    }

    /**
     * Constructs a new <tt>DefaultActQuery</tt>.
     *
     * @param entity        the entity to search for
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param entityName    the act entity name
     * @param conceptName   the act concept name
     * @param statusLookups the act status lookups
     * @param excludeStatus to exclude. May be <tt>null</tt>
     */
    public DefaultActQuery(Entity entity, String participant,
                           String participation, String entityName,
                           String conceptName, List<Lookup> statusLookups,
                           String excludeStatus) {
        super(entity, participant, participation, entityName, conceptName,
              statusLookups, excludeStatus);
    }

    /**
     * Constructs a new <tt>DefaultActQuery</tt>.
     *
     * @param entity        the entity to search for
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param shortNames    the act short names
     * @param statusLookups the act status lookups
     * @param excludeStatus to exclude. May be <tt>null</tt>
     * @throws ArchetypeQueryException if the short names don't match any
     *                                 archetypes
     */
    public DefaultActQuery(Entity entity, String participant,
                           String participation, String[] shortNames,
                           List<Lookup> statusLookups, String excludeStatus) {
        super(entity, participant, participation, shortNames, statusLookups,
              excludeStatus);
    }

    /**
     * Constructs a new <tt>DefaultActQuery</tt> to query acts for a specific
     * status.
     *
     * @param entity        the entity to search for
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param entityName    the act entity name
     * @param conceptName   the act concept name
     * @param status        the act status
     */
    public DefaultActQuery(Entity entity, String participant,
                           String participation, String entityName,
                           String conceptName, String status) {
        super(entity, participant, participation, entityName, conceptName,
              status);
    }

    /**
     * Constructs a new <tt>DefaultActQuery</tt>.
     *
     * @param entity        the entity to search for
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param shortNames    the act short names
     * @param statuses      the act statuses to search on. May be
     *                      <tt>empty</tt>
     */
    public DefaultActQuery(Entity entity, String participant,
                           String participation, String[] shortNames,
                           String[] statuses) {
        super(entity, participant, participation, shortNames, statuses);
    }

}
