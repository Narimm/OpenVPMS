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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.problem;

import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.component.im.act.ActHierarchyIterator;

/**
 * An iterator over patient problem acts.
 * <p/>
 * This includes 2 levels of act hierarchy, and the visits that items are from.
 *
 * @author Tim Anderson
 */
public class ProblemHierarchyIterator extends ActHierarchyIterator<Act> {

    /**
     * Constructs an {@link ProblemHierarchyIterator}.
     *
     * @param acts   the collection of acts
     * @param filter the hierarchy filter
     */
    public ProblemHierarchyIterator(Iterable<Act> acts, ProblemFilter filter) {
        super(acts, filter, 4);
    }

}
