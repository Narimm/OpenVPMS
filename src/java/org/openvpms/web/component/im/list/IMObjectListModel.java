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

import nextapp.echo2.app.list.AbstractListModel;
import org.openvpms.component.business.domain.im.common.IMObject;

import java.util.ArrayList;
import java.util.List;


/**
 * List model for {@link IMObject}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-07-05 22:12:49Z $
 * @see IMObjectListCellRenderer
 */
public class IMObjectListModel extends AbstractListModel {

    /**
     * The objects.
     */
    private List<IMObject> objects;

    /**
     * Dummy name indicating that all values apply.
     */
    public static final String ALL = "all";

    /**
     * Dummy name indicating that no value is required.
     */
    public static final String NONE = "none";

    /**
     * Determines if "all" should be included.
     */
    private final boolean all;

    /**
     * Determines if "none" should be included.
     */
    private final boolean none;


    /**
     * Construct a new <code>IMObjectListModel</code>.
     *
     * @param objects the objects to populate the list with.
     * @param all     if <code>true</code>, add a localised "All"
     * @param none    if <code>true</code>, add a localised "None"
     */
    public IMObjectListModel(List<IMObject> objects, boolean all,
                             boolean none) {
        this.all = all;
        this.none = none;
        this.objects = getObjects(objects);
    }

    /**
     * Returns the value at the specified index in the list.
     *
     * @param index the index
     * @return the value
     */
    public Object get(int index) {
        return objects.get(index);
    }

    /**
     * Returns the size of the list.
     *
     * @return the size
     */
    public int size() {
        return objects.size();
    }

    /**
     * Helper to prepend the objects "all" or "none" when required.
     * *
     *
     * @param objects the objects
     * @return a copy of <code>objects</code> preprended with "all" and/or
     *         "none" added when required
     */
    protected List<IMObject> getObjects(List<IMObject> objects) {
        if (all || none) {
            objects = new ArrayList<IMObject>(objects);

            if (all) {
                IMObject dummyAll = new IMObject();
                dummyAll.setName(ALL);
                objects.add(0, dummyAll);
            }
            if (none) {
                IMObject dummyNone = new IMObject();
                dummyNone.setName(NONE);
                objects.add(0, dummyNone);
            }
        }
        return objects;
    }

}
