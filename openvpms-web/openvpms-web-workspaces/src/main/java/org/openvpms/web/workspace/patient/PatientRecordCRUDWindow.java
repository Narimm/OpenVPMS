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
 *  $Id: PatientRecordCRUDWindow.java 942 2006-05-30 07:52:45Z tanderson $
 */

package org.openvpms.web.workspace.patient;

import org.openvpms.component.business.domain.im.act.Act;


/**
 * CRUD Window for patient record acts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-30 07:52:45Z $
 */
public interface PatientRecordCRUDWindow {

    /**
     * Sets the current patient clinical event.
     *
     * @param event the current event
     */
    void setEvent(Act event);

    /**
     * Returns the current patient clinical event.
     *
     * @return the current event. May be <tt>null</tt>
     */
    Act getEvent();

}
