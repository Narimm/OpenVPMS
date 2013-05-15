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
 *  Copyright 2006-2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.list;

import org.openvpms.component.business.domain.im.common.IMObject;

import java.util.List;


/**
 * List model for {@link IMObject}s.
 *
 * @author Tim Anderson
 * @see IMObjectListCellRenderer
 */
public class IMObjectListModel extends AbstractIMObjectListModel<IMObject> {

    /**
     * Constructs a {@code IMObjectListModel}.
     *
     * @param objects the objects to populate the list with.
     * @param all     if {@code true}, add a localised "All"
     * @param none    if {@code true}, add a localised "None"
     */
    public IMObjectListModel(List<? extends IMObject> objects, boolean all, boolean none) {
        super(objects, all, none);
    }

    /**
     * Returns the value at the specified index in the list.
     *
     * @param index the index
     * @return the value
     */
    public Object get(int index) {
        return getObject(index);
    }

}
