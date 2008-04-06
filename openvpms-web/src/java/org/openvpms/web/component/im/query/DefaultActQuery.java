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
     * @param shortNames the act short names to query
     * @param statuses   the act status lookups
     */
    public DefaultActQuery(String[] shortNames, ActStatuses statuses) {
        this(null, null, null, shortNames, statuses);
    }

    /**
     * Constructs a new <tt>DefaultActQuery</tt>.
     *
     * @param entity        the entity to search for
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param act           the act short name
     * @param statuses      the act status lookups
     */
    public DefaultActQuery(Entity entity, String participant,
                           String participation, String act,
                           ActStatuses statuses) {
        this(entity, participant, participation, new String[]{act},
             statuses);
    }

    /**
     * Constructs a new <tt>DefaultActQuery</tt>.
     *
     * @param entity        the entity to search for
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param acts          the act short names
     * @param statuses      the act status lookups
     */
    public DefaultActQuery(Entity entity, String participant,
                           String participation, String[] acts,
                           ActStatuses statuses) {
        super(entity, participant, participation, acts, statuses, Act.class);
    }

    /**
     * Constructs a new <tt>DefaultActQuery</tt> to query acts for a specific
     * status.
     *
     * @param entity        the entity to search for
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param acts          the act short names
     * @param status        the act status
     */
    public DefaultActQuery(Entity entity, String participant,
                           String participation, String[] acts, String status) {
        this(entity, participant, participation, acts, new String[]{status});
    }

    /**
     * Constructs a new <tt>DefaultActQuery</tt>.
     *
     * @param entity        the entity to search for
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param acts          the act short names
     */
    public DefaultActQuery(Entity entity, String participant,
                           String participation, String[] acts) {
        this(entity, participant, participation, acts, true, new String[0]);
    }

    /**
     * Constructs a new <tt>DefaultActQuery</tt>.
     *
     * @param entity        the entity to search for
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param acts          the act short names
     * @param statuses      the act statuses to search on. May be
     *                      <tt>empty</tt>
     */
    public DefaultActQuery(Entity entity, String participant,
                           String participation, String[] acts,
                           String[] statuses) {
        this(entity, participant, participation, acts, true, statuses);
    }

    /**
     * Constructs a new <tt>DefaultActQuery</tt>.
     *
     * @param entity        the entity to search for
     * @param participant   the partcipant node name
     * @param participation the entity participation short name
     * @param acts          the act short names
     * @param primaryOnly   if <tt>true</tt> only primary archetypes will be
     *                      queried
     * @param statuses      the act statuses to search on. May be
     *                      <tt>empty</tt>
     */
    public DefaultActQuery(Entity entity, String participant,
                           String participation, String[] acts,
                           boolean primaryOnly, String[] statuses) {
        super(entity, participant, participation, acts, primaryOnly, statuses,
              Act.class);
    }

}
