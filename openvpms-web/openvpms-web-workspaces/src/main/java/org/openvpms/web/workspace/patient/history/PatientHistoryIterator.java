/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2014 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.history;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.act.ActHierarchyIterator;

/**
 * An iterator over patient history acts.
 * <p/>
 * This includes 3 levels of act hierarchy.
 *
 * @author Tim Anderson
 */
public class PatientHistoryIterator extends ActHierarchyIterator<Act> {

    /**
     * Constructs an {@link PatientHistoryIterator}.
     *
     * @param acts          the top-level acts to iterate
     * @param shortNames    the history item short names to include
     * @param sortAscending if {@code true} sort items on ascending timestamp; otherwise sort on descending timestamp
     */
    public PatientHistoryIterator(Iterable<Act> acts, String[] shortNames, boolean sortAscending) {
        this(acts, new PatientHistoryFilter(shortNames, sortAscending));
    }

    /**
     * Constructs an {@link PatientHistoryIterator}.
     *
     * @param acts   the top-level acts to iterate
     * @param filter the history item short names to include
     */
    public PatientHistoryIterator(Iterable<Act> acts, PatientHistoryFilter filter) {
        this(acts, filter, 3);
    }

    /**
     * Constructs an {@link PatientHistoryIterator}.
     *
     * @param acts     the top-level acts to iterate
     * @param filter   the history item short names to include
     * @param maxDepth the maximum depth to iterate to, or {@code -1} to have unlimited depth
     */
    public PatientHistoryIterator(Iterable<Act> acts, PatientHistoryFilter filter, int maxDepth) {
        super(acts, filter, maxDepth);
    }

}
