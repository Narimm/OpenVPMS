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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.app;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class SelectionHistory {

    public static final int DEFAULT_CAPACITY = 25;

    private List<Selection> selections = new ArrayList<Selection>();

    private final int capacity;

    public SelectionHistory() {
        this(DEFAULT_CAPACITY);
    }

    public SelectionHistory(int capacity) {
        this.capacity = capacity;
    }


    public void add(IMObject object) {
        IMObjectReference ref = object.getObjectReference();
        Selection selection = new Selection(ref);
        selections.remove(selection);
        selections.add(0, selection);

        if (selections.size() > capacity) {
            selections.remove(selections.size() - 1);
        }
    }

    public List<Selection> getSelections() {
        return selections;
    }

    public Date getSelected(IMObject object) {
        return getSelected(object.getObjectReference());
    }

    public Date getSelected(IMObjectReference reference) {
        int index = selections.indexOf(new Selection(reference));
        return (index != -1) ? selections.get(index).getTime() : null;
    }

    public static class Selection {

        private final IMObjectReference reference;

        private final Date time;

        public IMObjectReference getReference() {
            return reference;
        }

        public IMObject getObject() {
            return IMObjectHelper.getObject(reference);
        }

        public Date getTime() {
            return time;
        }

        public Selection(IMObjectReference reference) {
            this.reference = reference;
            this.time = new Date();
        }

        /**
         * Indicates whether some other object is "equal to" this one.
         * <p/>
         * Two selections are considerered equal if they have the same reference.
         *
         * @param obj the reference object with which to compare.
         * @return <tt>true</tt> if this object is the same as the obj argument; <tt>false</tt> otherwise.
         */
        @Override
        public boolean equals(Object obj) {
            return obj == this || (obj instanceof Selection && ((Selection) obj).reference.equals(reference));
        }

        /**
         * Returns a hash code value for the object.
         *
         * @return a hash code value for this object.
         */
        @Override
        public int hashCode() {
            return reference.hashCode();
        }
    }

}
