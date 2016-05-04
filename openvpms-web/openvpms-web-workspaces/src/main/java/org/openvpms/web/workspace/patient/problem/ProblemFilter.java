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

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.act.ActHierarchyFilter;

import java.util.Comparator;
import java.util.Map;
import java.util.Set;

/**
 * Filters patient problems.
 * <p/>
 * This enables specific problems items to by included by archetype.
 *
 * @author Tim Anderson
 */
public class ProblemFilter extends ActHierarchyFilter<Act> {

    /**
     * Constructs an {@link ProblemFilter}.
     *
     * @param shortNames the act short names
     * @param ascending  if {@code true} sort items on ascending timestamp; otherwise sort on descending timestamp
     */
    public ProblemFilter(String[] shortNames, boolean ascending) {
        super(shortNames, true);
        setSortItemsAscending(ascending);
    }

    /**
     * Returns a comparator to sort the children of an act.
     *
     * @param act the parent act
     * @return the comparator to sort the act's children
     */
    @Override
    public Comparator<Act> getComparator(Act act) {
        if (TypeHelper.isA(act, PatientArchetypes.PATIENT_MEDICATION, PatientArchetypes.CLINICAL_NOTE)) {
            return super.getComparator(true);
        }
        return super.getComparator(act);
    }

    /**
     * Returns the immediate children of an act.
     *
     * @param act  the parent act
     * @param acts a cache of the visited acts, keyed on reference
     * @return the immediate children of {@code act}
     */
    @Override
    protected Set<Act> getChildren(Act act, Map<IMObjectReference, Act> acts) {
        Set<Act> children = super.getChildren(act, acts);
        if (TypeHelper.isA(act, PatientArchetypes.CLINICAL_PROBLEM)) {
            ActBean bean = new ActBean(act);
            Set<Act> events = getActs(bean.getNodeSourceObjectRefs("events"), acts);
            children.addAll(events);
        }
        return children;
    }

}
