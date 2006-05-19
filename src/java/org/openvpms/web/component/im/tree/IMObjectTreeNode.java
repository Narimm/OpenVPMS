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

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
package org.openvpms.web.component.im.tree;

import echopointng.tree.DefaultMutableTreeNode;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.util.IMObjectHelper;


/**
 * Tree node associated with an {@link org.openvpms.component.business.domain.im.common.IMObject}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class IMObjectTreeNode extends DefaultMutableTreeNode {

    /**
     * The object associated with the node.
     */
    private final IMObject _object;


    /**
     * Construct a new <code>IMObjectTreeNode</code>.
     *
     * @param object the object to associate with the node
     */
    public IMObjectTreeNode(IMObject object) {
        _object = object;
        String description = IMObjectHelper.getString(object, "description");
        setUserObject(description);
    }

    /**
     * Returns the associated object.
     *
     * @return the object
     */
    public IMObject getObject() {
        return _object;
    }

}
