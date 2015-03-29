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

package org.openvpms.web.component.im.list;

import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * List cell renderer that display's an {@link IMObject}'s name or description.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class IMObjectListCellRenderer
    extends AllNoneListCellRenderer<IMObject> {

    /**
     * A renderer that renders the object's name.
     */
    public static final IMObjectListCellRenderer NAME = new IMObjectListCellRenderer(Node.NAME);

    /**
     * A renderer that renders the object's description.
     */
    public static final IMObjectListCellRenderer DESCRIPTION = new IMObjectListCellRenderer(Node.DESCRIPTION);

    /**
     * The nodes that may be rendered.
     */
    private enum Node {

        NAME, DESCRIPTION
    }

    /**
     * The node to render.
     */
    private final Node node;


    /**
     * Constructs a new <tt>IMObjectListCellRenderer</tt>.
     *
     * @param node the node to render
     */
    protected IMObjectListCellRenderer(Node node) {
        super(IMObject.class);
        this.node = node;
    }

    /**
     * Renders an object.
     *
     * @param list   the list component
     * @param object the object to render. May be <tt>null</tt>
     * @param index  the object index
     * @return the rendered object
     */
    protected Object getComponent(Component list, IMObject object, int index) {
        if (object != null) {
            return (node == Node.NAME) ? object.getName() : object.getDescription();
        }
        return null;
    }

}
