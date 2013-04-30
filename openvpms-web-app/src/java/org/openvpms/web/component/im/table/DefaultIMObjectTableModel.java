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

package org.openvpms.web.component.im.table;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Default {@link IMObjectTableModel}, displaying the <code>IMObject</code>'s
 * name and description.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DefaultIMObjectTableModel<T extends IMObject>
    extends BaseIMObjectTableModel<T> {

    /**
     * Constructs a <tt>DefaultIMObjectTableModel</tt>, displaying the <tt>IMObjects</tt> name and description.
     */
    public DefaultIMObjectTableModel() {
        this(true, true);
    }

    /**
     * Constructs a <tt>DefaultIMObjectTableModel</tt>.
     *
     * @param showName        if <tt>true</tt> show the object's name
     * @param showDescription if <tt>true</tt> show the object's description
     */
    public DefaultIMObjectTableModel(boolean showName, boolean showDescription) {
        super(null);
        setTableColumnModel(createTableColumnModel(false, false, showName, showDescription));
    }
}
