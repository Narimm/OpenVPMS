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
 * Represents the state of an {@link Browser}.
 * <p/>
 * This is used by {@link Browser} implementations to return lightweight representations of their state.
 * This may be used restore a browser to a prior state, or populate a compatible browser.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public interface BrowserState {

    /**
     * Determines if this state is supported by the specified browser.
     *
     * @param browser the browser
     * @return <tt>true</tt> if the state is supported by the browser; otherwise <tt>false</tt>
     */
    boolean supports(Browser browser);

    /**
     * Determines if this state is supports the specified archetypes and type.
     * <p/>
     * Note that this is not as precise as the {@link #supports(Browser)} method. Two browser states may report that
     * they support the same archetypes and type but have been created by two incompatible browsers.
     *
     * @param shortNames the archetype short names
     * @param type       the type returned by the underlying query
     * @return <tt>true</tt> if the state supports the specified archetypes and type
     */
    boolean supports(String[] shortNames, Class type);
}
