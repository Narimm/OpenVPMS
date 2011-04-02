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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.esci.adapter.map.invoice;

/**
 * Package information.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
class Package {

    /**
     * The package size.
     */
    private final int size;

    /**
     * The package units.
     */
    private final String units;

    /**
     * Constructs a <tt>Package</tt>.
     *
     * @param size  the package size
     * @param units the package units
     */
    public Package(int size, String units) {
        this.size = size;
        this.units = units;
    }

    /**
     * Returns the package size.
     *
     * @return the package size
     */
    public int getPackageSize() {
        return size;
    }

    /**
     * Returns the package units.
     *
     * @return the package units
     */
    public String getPackageUnits() {
        return units;
    }

}
