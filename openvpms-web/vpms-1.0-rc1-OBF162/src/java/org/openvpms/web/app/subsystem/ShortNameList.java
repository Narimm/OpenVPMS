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
import org.openvpms.component.business.service.archetype.helper.TypeHelper;


/**
 * Default implementation of the {@link ShortNames} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ShortNameList implements ShortNames {

    /**
     * The archetype short names. May contain wildcards.
     */
    private final String[] shortNames;

    /**
     * The short names with wildcards expanded.
     */
    private String[] expanded;


    /**
     * Creates a new <tt>ShortNameList</tt>.
     *
     * @param shortNames the short names
     */
    public ShortNameList(String[] shortNames) {
        this.shortNames = shortNames;
    }

    /**
     * Creates a new <code>ShortNameList</code>.
     *
     * @param shortName the short name
     */
    public ShortNameList(String shortName) {
        shortNames = new String[]{shortName};
    }

    /**
     * Returns the archetype short names.
     *
     * @return the archetype short names
     */
    public String[] getShortNames() {
        return shortNames;
    }

    /**
     * Returns <tt>true</tt> if the collection contains a short name.
     *
     * @param shortName the short name. May contain wildcards
     * @return <tt>true</tt> if this contains <tt>shortName</tt>
     */
    public boolean contains(String shortName) {
        if (expanded == null) {
            expanded = DescriptorHelper.getShortNames(shortNames);
        }
        for (String s : expanded) {
            if (TypeHelper.matches(s, shortName)) {
                return true;
            }
        }
        return false;
    }

}
