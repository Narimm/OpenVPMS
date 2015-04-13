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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.im.query;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


/**
 * Abstract implementation of the {@link QueryState} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractQueryState implements QueryState {

    /**
     * The type that the query returns.
     */
    private final Class type;

    /**
     * The archetype short names being queried.
     */
    private final Set<String> shortNames;

    /**
     * Constructs an <tt>AbstractQueryState</tt>.
     *
     * @param query the query
     */

    public AbstractQueryState(Query query) {
        type = query.getType();
        shortNames = new HashSet<String>(Arrays.asList(query.getShortNames()));
    }

    /**
     * Returns the type that the query returns.
     *
     * @return the type
     */
    public Class getType() {
        return type;
    }

    /**
     * The archetype short names being queried.
     * <p/>
     * Any wildcards are expanded.
     *
     * @return the archetype short names
     */
    public String[] getShortNames() {
        return shortNames.toArray(new String[shortNames.size()]);
    }

    /**
     * Determines if the query supports the specified archetypes.
     *
     * @param shortNames the archetype short names
     * @return <tt>true</tt> if the archetypes are supported, otherwise <tt>false</tt>
     */
    public boolean supports(String[] shortNames) {
        return this.shortNames.size() == shortNames.length && this.shortNames.containsAll(Arrays.asList(shortNames));
    }

    /**
     * Determines if the query supports the specified type and archetypes.
     *
     * @param type       the type
     * @param shortNames the archetype short names
     * @return <tt>true</tt> if the archetypes are supported, otherwise <tt>false</tt>
     */
    public boolean supports(Class type, String[] shortNames) {
        return this.type.equals(type) && supports(shortNames);
    }

}