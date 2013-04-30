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

/**
 * Represents the state of an {@link Query}.
 * <p/>
 * This is used by {@link Query} implementations to return lightweight representations of their state.
 * This may be used restore a query to a prior state or populate a compatible query.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface QueryState {

    /**
     * Returns the type that the query returns.
     *
     * @return the type
     */
    public Class getType();

    /**
     * The archetype short names being queried.
     * <p/>
     * Any wildcards are expanded.
     *
     * @return the archetype short names
     */
    public String[] getShortNames();

    /**
     * Determines if the query supports the specified archetypes.
     *
     * @param shortNames the archetype short names
     * @return <tt>true</tt> if the archetypes are supported, otherwise <tt>false</tt>
     */
    public boolean supports(String[] shortNames);

    /**
     * Determines if the query supports the specified type and archetypes.
     *
     * @param type       the type
     * @param shortNames the archetype short names
     * @return <tt>true</tt> if the archetypes are supported, otherwise <tt>false</tt>
     */
    boolean supports(Class type, String[] shortNames);

}
