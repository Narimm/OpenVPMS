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

package org.openvpms.web.app.customer.note;

import org.apache.commons.collections.Transformer;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.LookupServiceHelper;
import org.openvpms.web.app.alert.Alert;


/**
 * Transformer that returns the priority of an <em>lookup.customerAlertType</em> associated with an act.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
class AlertPriorityTransformer implements Transformer {

    /**
     * The singleton instance.
     */
    public static final Transformer INSTANCE = new AlertPriorityTransformer();

    /**
     * Transforms the input object (leaving it unchanged) into some output object.
     *
     * @param input the object to be transformed, should be left unchanged
     * @return a transformed object
     * @throws ClassCastException       (runtime) if the input is the wrong class
     * @throws IllegalArgumentException (runtime) if the input is invalid
     * @throws org.apache.commons.collections.FunctorException
     *                                  (runtime) if the transform cannot be completed
     */
    public Object transform(Object input) {
        Act act = (Act) input;
        Lookup lookup = LookupServiceHelper.getLookupService().getLookup(act, "alertType");
        if (lookup != null) {
            IMObjectBean bean = new IMObjectBean(lookup);
            String priority = bean.getString("priority");
            return priority != null ? Alert.Priority.valueOf(priority) : null;
        }
        return null;
    }
}
