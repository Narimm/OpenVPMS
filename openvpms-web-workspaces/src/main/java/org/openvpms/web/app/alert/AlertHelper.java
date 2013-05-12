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

package org.openvpms.web.app.alert;

import nextapp.echo2.app.Color;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.util.LookupNameHelper;
import org.openvpms.web.echo.colour.ColourHelper;
import org.openvpms.web.system.ServiceHelper;

/**
 * Alert helper.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class AlertHelper {

    /**
     * Returns the alert type associated with an act.
     *
     * @param act the act
     * @return the alert type, or <tt>null</tt> if none is found
     */
    public static Lookup getAlertType(Act act) {
        return ServiceHelper.getLookupService().getLookup(act, "alertType");
    }

    /**
     * Returns the priority name associated with an alert type.
     *
     * @param alertType the alert type
     * @return the alert type's priority name
     */
    public static String getPriorityName(Lookup alertType) {
        return LookupNameHelper.getName(alertType, "priority");
    }

    /**
     * Returns the colour associated with an alert type.
     *
     * @param alertType the alert type
     * @return the alert type's colour
     */
    public static Color getColour(Lookup alertType) {
        IMObjectBean bean = new IMObjectBean(alertType);
        return ColourHelper.getColor(bean.getString("colour"));
    }

}
