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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.table;

import org.openvpms.web.component.event.ActionListener;


/**
 * Listener for {@link KeyTable} page events.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class PageListener extends ActionListener {

    /**
     * Action command to indicate to move to the previous page.
     */
    public static final String PAGE_PREVIOUS = "previous";

    /**
     * Action command to indicate to move to the next page.
     */
    public static final String PAGE_NEXT = "next";

    /**
     * Action command to indicate to move to the first page.
     */
    public static final String PAGE_FIRST = "first";

    /**
     * Action command to indicate to move to the last page.
     */
    public static final String PAGE_LAST = "last";

}
