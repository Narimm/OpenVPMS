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

package org.openvpms.web.component.focus;

import java.util.ArrayList;
import java.util.List;

import echopointng.DateField;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.TextField;


/**
 * A group of components that may receive focus.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class FocusSet extends AbstractFocusGroup {

    /**
     * The components.
     */
    private List<Component> _components = new ArrayList<Component>();

    /**
     * The first focus traversal index.
     */
    private int _first = -1;

    /**
     * The next focus traversal index to assign.
     */
    private int _next = -1;



    /**
     * Construct a new <code>FocusSet</code>.
     *
     * @param name a synbolic name for the set
     */
    public FocusSet(String name) {
        super(name);
    }

    /**
     * Adds a component.
     *
     * @param component the component to add
     */
    public void add(Component component) {
        _components.add(component);
        setDirty(true);
        notifyParent();
    }

    /**
     * Returns the components.
     *
     * @return the components
     */
    public List<Component> getComponents() {
        return _components;
    }

    /**
     * Returns the first focus traversal index.
     *
     * @return the first focus traversal index, or <code>-1</code> if there are
     *         no indexes
     */
    public int getFirst() {
        return _first;
    }

    /**
     * Returns the last focus traversal index.
     *
     * @return the last focus traversal index, or <code>-1</code> if there are
     *         no indexes
     */
    public int getLast() {
        return (_next != _first) ? _next - 1 : _first;
    }

    /**
     * Resets the focus traversal indexes.
     */
    public void reindex() {
        int index = (_first == -1) ? 1 : _first;
        reindex(index);
    }

    /**
     * Resets the focus traversal indexes.
     *
     * @param index the first index
     * @return the last index
     */
    public int reindex(int index) {
        if (_first != index || isDirty()) {
            _first = index;
            _next = index;
            for (Component component : _components) {
                setIndex(component);
            }
            setDirty(false);
        }
        return _next;
    }

    /**
     * Sets the focus traversal index for a component.
     *
     * @param component the component
     */
    private void setIndex(Component component) {
        component.setFocusTraversalIndex(_next);
        if (component instanceof DateField) {
            // @todo workaround for dates.
            TextField text = ((DateField) component).getTextField();
            text.setFocusTraversalIndex(_next);
        }
        ++_next;
    }

}
