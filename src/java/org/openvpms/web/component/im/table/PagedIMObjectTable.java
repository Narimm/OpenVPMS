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
import org.openvpms.web.component.im.query.ResultSet;


/**
 * Paged IMObject table.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PagedIMObjectTable<T extends IMObject> extends PagedIMTable<T> {

    /**
     * Construct a new <code>PagedIMObjectTable</code>.
     *
     * @param model the model to render results
     */
    public PagedIMObjectTable(IMObjectTableModel<T> model) {
        super(model);
    }

    /**
     * Construct a new <code>PagedIMObjectTable</code>.
     *
     * @param model the model to render results
     * @param set   the result set
     */
    public PagedIMObjectTable(IMObjectTableModel<T> model, ResultSet<T> set) {
        this(model);
        setResultSet(set);
    }


}
