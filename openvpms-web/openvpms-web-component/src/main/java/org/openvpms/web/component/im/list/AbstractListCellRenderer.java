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

package org.openvpms.web.component.im.list;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.list.ListCellRenderer;
import org.openvpms.web.resource.util.Messages;


/**
 * List cell renderer that renders special 'All' and 'None' objects in bold.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractListCellRenderer<T>
    implements ListCellRenderer {

    /**
     * The type being rendered.
     */
    private Class<T> type;

    /**
     * Localised display name for "all".
     */
    private final String ALL = Messages.get("list.all");

    /**
     * Localised display name for "none".
     */
    private final String NONE = Messages.get("list.none");


    /**
     * Constructs a new <tt>AbstractListCellRenderer</tt>.
     *
     * @param type the type that this can render
     */
    public AbstractListCellRenderer(Class<T> type) {
        this.type = type;
    }

    /**
     * Renders an item in a list.
     *
     * @param list  the list component
     * @param value the item value. May be <tt>null</tt>
     * @param index the item index
     * @return the rendered form of the list cell
     */
    public Object getListCellRendererComponent(Component list, Object value,
                                               int index) {
        Object result = null;
        if (value == null || type.isAssignableFrom(value.getClass())) {
            T object = type.cast(value);
            if (isAll(list, object, index)) {
                result = new BoldListCell(ALL);
            } else if (isNone(list, object, index)) {
                result = new BoldListCell(NONE);
            } else {
                result = getComponent(list, object, index);
            }
        }
        if (result == null) {
            result = "";
        }
        return result;
    }

    /**
     * Renders an object.
     *
     * @param list   the list component
     * @param object the object to render. May be <tt>null</tt>
     * @param index  the object index
     * @return the rendered object
     */
    protected abstract Object getComponent(Component list, T object, int index);

    /**
     * Determines if an object represents 'All'.
     *
     * @param list   the list component
     * @param object the object. May be <tt>null</tt>
     * @param index  the object index
     * @return <code>true</code> if the object represents 'All'.
     */
    protected abstract boolean isAll(Component list, T object, int index);

    /**
     * Determines if an object represents 'None'.
     *
     * @param list   the list component
     * @param object the object. May be <tt>null</tt>
     * @param index  the object index
     * @return <code>true</code> if the object represents 'None'.
     */
    protected abstract boolean isNone(Component list, T object, int index);

}
