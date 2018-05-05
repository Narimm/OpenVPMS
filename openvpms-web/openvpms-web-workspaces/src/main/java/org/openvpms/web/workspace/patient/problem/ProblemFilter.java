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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.patient.problem;

import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.act.ActHierarchyFilter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Filters patient problems.
 * <p>
 * This enables specific problems items to by included by archetype.
 *
 * @author Tim Anderson
 */
public class ProblemFilter extends ActHierarchyFilter<Act> {

    /**
     * The search criteria.
     */
    private final Predicate<Act> search;

    /**
     * Constructs an {@link ProblemFilter}.
     *
     * @param shortNames the act short names
     * @param search     the search criteria. May be {@code null}
     * @param ascending  if {@code true} sort items on ascending timestamp; otherwise sort on descending timestamp
     */
    public ProblemFilter(String[] shortNames, Predicate<Act> search, boolean ascending) {
        super(shortNames, true);
        this.search = search;
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
     * Filters child acts.
     *
     * @param parent   the parent act
     * @param children the child acts
     * @param acts     the set of visited acts, keyed on reference
     * @return the filtered acts
     */
    @Override
    protected List<Act> filter(Act parent, List<Act> children, Map<IMObjectReference, Act> acts) {
        List<Act> result;
        if (search == null) {
            result = children;
        } else {
            result = new ArrayList<>();
            for (Act act : children) {
                // events always match, so that the child acts can be searched.
                if (TypeHelper.isA(act, PatientArchetypes.CLINICAL_EVENT) || search.test(act)) {
                    result.add(act);
                }
            }
        }
        return result;
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
