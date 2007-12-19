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

package org.openvpms.web.app.customer;

import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.act.AbstractActTableModel;


/**
 * Table model for <em>act.customerNote</em> instances.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class CustomerNoteTableModel extends AbstractActTableModel {

    /**
     * Creates a new <tt>AlertTableModel</tt>.
     */
    public CustomerNoteTableModel() {
        this(null);
    }

    /**
     * Creates a new <tt>AlertTableModel</tt>.
     */
    public CustomerNoteTableModel(LayoutContext context) {
        super(new String[]{"act.customerNote"}, context);
    }

    /**
     * Returns a list of descriptor names to include in the table.
     *
     * @return the list of descriptor names to include in the table
     */
    @Override
    protected String[] getDescriptorNames() {
        return new String[]{"startTime", "category", "author", "note"};
    }
}
