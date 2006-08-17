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

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
package org.openvpms.web.app.subsystem;

import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;


/**
 * Default implementation of the {@link ShortNames} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ShortNameList implements ShortNames {

    /**
     * The archetype short names.
     */
    private final String[] _shortNames;

    /**
     * Creates a new <code>ShortNameList</code>.
     *
     * @param refModelName the archetype reference model name
     * @param entityName   the archetype entity name
     * @param conceptName  the archetype concept name
     */
    public ShortNameList(String refModelName, String entityName,
                         String conceptName) {
        _shortNames = DescriptorHelper.getShortNames(refModelName, entityName,
                                                     conceptName);
    }

    /**
     * Creates a new <code>ShortNameList</code>.
     *
     * @param shortNames the short names
     */
    public ShortNameList(String[] shortNames) {
        _shortNames = shortNames;
    }

    /**
     * Creates a new <code>ShortNameList</code>.
     *
     * @param shortName the short name
     */
    public ShortNameList(String shortName) {
        _shortNames = new String[]{shortName};
    }

    /**
     * Returns the archetype short names.
     *
     * @return the archetype short names
     */
    public String[] getShortNames() {
        return _shortNames;
    }

}
