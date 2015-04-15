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
 * Abstract implementation of the {@link BrowserState} inteface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractBrowserState implements BrowserState {

    /**
     * The query state. May be <tt>null</tt>.
     */
    private final QueryState state;

    /**
     * Constructs an <tt>AbstractBrowserState</tt>.
     *
     * @param query the query
     */
    public AbstractBrowserState(Query query) {
        state = query.getQueryState();
    }

    /**
     * Returns the query state.
     *
     * @return the query state, or <tt>null</tt> if the query doesn't support it
     */
    public QueryState getQueryState() {
        return state;
    }

    /**
     * Determines if this state is supports the specified archetypes and type.
     *
     * @param shortNames the archetype short names
     * @param type       the type returned by the underlying query
     * @return <tt>true</tt> if the state supports the specified archetypes and type
     */
    public boolean supports(String[] shortNames, Class type) {
        return state != null && state.supports(type, shortNames);
    }
}
