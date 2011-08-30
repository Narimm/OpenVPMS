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
 * Paged, sortable table of {@link IMObject}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class PagedIMObjectTableModel<T extends IMObject>
        extends PagedIMTableModel<T> implements IMObjectTableModel<T> {


    /**
     * Construct a new <code>PagedIMObjectTableModel</code>.
     *
     * @param model the underlying table model.
     */
    public PagedIMObjectTableModel(IMObjectTableModel<T> model) {
        super(model);
    }

}
