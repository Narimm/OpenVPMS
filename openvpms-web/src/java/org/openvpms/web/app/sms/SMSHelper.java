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
 *  Copyright 2012 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.app.sms;

import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.List;

/**
 * SMS helper methods.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public class SMSHelper {

    /**
     * Determines if SMS is configured for the practice.
     * <p/>
     * TODO - this should be moved into a practice service
     *
     * @return <tt>true</tt> if SMS is configured, otherwise <tt>false</tt>
     */
    public static boolean isSMSEnabled() {
        boolean enabled = false;
        Party practice = GlobalContext.getInstance().getPractice();
        if (practice != null) {
            EntityBean bean = new EntityBean(practice);
            List<IMObjectReference> refs = bean.getNodeTargetEntityRefs("SMS");
            for (IMObjectReference ref : refs) {
                if (IMObjectHelper.isActive(ref)) {
                    enabled = true;
                    break;
                }
            }
        }
        return enabled;
    }
}
