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
 */
package org.openvpms.web.component.app;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;


/**
 * Maintains a history of object selections.
 *
 * @author Tim Anderson
 */
public class SelectionHistory {

    /**
     * The default selection history capacity.
     */
    public static final int DEFAULT_CAPACITY = 25;

    /**
     * The context.
     */
    private Context context;

    /**
     * The selection history.
     */
    private List<Selection> selections = new ArrayList<Selection>();

    /**
     * The history capacity. On exceeding this limit, the oldest selection is removed.
     */
    private final int capacity;

    /**
     * Construct a {@code SelectionHistory} with default capacity.
     *
     * @param context the context
     */
    public SelectionHistory(Context context) {
        this(DEFAULT_CAPACITY, context);
    }

    /**
     * Construct a {@code SelectionHistory} with the specified capacity.
     *
     * @param capacity the capacity
     * @param context  the context
     */
    public SelectionHistory(int capacity, Context context) {
        this.capacity = capacity;
        this.context = context;
    }


    /**
     * Adds an object to the history.
     * <p/>
     * If it is already present, the existing selection will be removed, and the object added to the front of
     * the history, indicating that it is the most recent selection.
     * <p/>
     * If the no. of selections exceeds the history capacity, the oldest selection will be removed
     *
     * @param object the object to add
     */
    public void add(IMObject object) {
        IMObjectReference ref = object.getObjectReference();
        Selection selection = new Selection(ref, context);
        selections.remove(selection);
        selections.add(0, selection);

        if (selections.size() > capacity) {
            selections.remove(selections.size() - 1);
        }
    }

    /**
     * Returns the selection history, ordered on time.
     *
     * @return the selections
     */
    public List<Selection> getSelections() {
        return selections;
    }

    /**
     * Returns the time when an object was selected.
     *
     * @param object the object
     * @return the time when the object was selected, or {@code null} if it can't be found in the history
     */
    public Date getSelected(IMObject object) {
        return getSelected(object.getObjectReference());
    }

    /**
     * Returns the time when an object was selected.
     *
     * @param reference the object reference
     * @return the time when the object was selected, or {@code null} if it can't be found in the history
     */
    public Date getSelected(IMObjectReference reference) {
        int index = selections.indexOf(new Selection(reference, context));
        return (index != -1) ? selections.get(index).getTime() : null;
    }

    /**
     * Tracks the selection of an object.
     */
    public static class Selection {

        /**
         * The object reference.
         */
        private final IMObjectReference reference;

        /**
         * The selection time.
         */
        private final Date time;

        /**
         * The context.
         */
        private final Context context;


        /**
         * Constructs a {@code Selection}.
         *
         * @param reference the object reference
         * @param context   the context
         */
        public Selection(IMObjectReference reference, Context context) {
            this.reference = reference;
            this.time = new Date();
            this.context = context;
        }

        /**
         * Returns the object reference.
         *
         * @return the object reference
         */
        public IMObjectReference getReference() {
            return reference;
        }

        /**
         * Returns the object.
         *
         * @return the object, or {@code null} if it no longer exists
         */
        public IMObject getObject() {
            return IMObjectHelper.getObject(reference, context);
        }

        /**
         * Returns the selection time.
         *
         * @return the selection time
         */
        public Date getTime() {
            return time;
        }

        /**
         * Indicates whether some other object is "equal to" this one.
         * <p/>
         * Two selections are considerered equal if they have the same reference.
         *
         * @param obj the reference object with which to compare.
         * @return {@code true} if this object is the same as the obj argument; {@code false} otherwise.
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
