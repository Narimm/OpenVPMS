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

/**
 * Simple query implementation that indicates that the query should be run
 * automatically.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class AutoQuery extends AbstractQuery {

    /**
     * Construct a new <code>Browser</code> that queries IMObjects with the
     * specified short names.
     *
     * @param shortNames the short names
     */
    public AutoQuery(String[] shortNames) {
        super(shortNames);
    }

    /**
     * Construct a new <code>Browser</code> that queries IMObjects with the
     * specified criteria.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public AutoQuery(String refModelName, String entityName,
                     String conceptName) {
        super(refModelName, entityName, conceptName);
    }

    /**
     * Determines if the query should be run automatically.
     *
     * @return <code>true</code> if the query should be run automaticaly;
     *         otherwie <code>false</code>
     */
    @Override
    public boolean isAuto() {
        return true;
    }

}
