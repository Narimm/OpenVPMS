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
 */
package org.openvpms.web.app.subsystem;

import org.openvpms.archetype.rules.act.ActStatus;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.web.component.im.edit.ActActions;


/**
 * Determines the operations that may be performed on document acts.
 *
 * @author Tim Anderson
 */
public class DocumentActActions extends ActActions<DocumentAct> {

    /**
     * Determines if a document act can be refreshed.
     *
     * @param act the act to check
     * @return {@code true} if the act isn't posted, and has <em>documentTemplate</em> and <em>document</em> nodes.
     */
    public boolean canRefresh(DocumentAct act) {
        boolean refresh = false;
        if (!ActStatus.POSTED.equals(act.getStatus())) {
            ActBean bean = new ActBean(act);
            if (bean.hasNode("documentTemplate") && bean.hasNode("document")) {
                refresh = true;
            }
        }
        return refresh;
    }
}
