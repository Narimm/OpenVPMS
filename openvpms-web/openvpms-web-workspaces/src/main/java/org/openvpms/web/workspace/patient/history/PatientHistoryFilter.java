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

package org.openvpms.web.workspace.patient.history;

import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.web.component.im.act.ActHelper;
import org.openvpms.web.component.im.act.ActHierarchyFilter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;


/**
 * Filters patient history.
 * <p>
 * This:
 * <ul>
 * <li>enables specific event items to by included by archetype</li>
 * <li>excludes charge items if they are linked to by an included medication</li>
 * </ul>
 *
 * @author Tim Anderson
 */
public class PatientHistoryFilter extends ActHierarchyFilter<Act> {

    /**
     * The short names of the child acts to return.
     */
    private final List<String> shortNames;

    /**
     * Determines if invoice items should be included. If {@code true}, this excludes those invoice items linked to
     * <em>act.patientMedication</em>
     */
    private final boolean invoice;

    /**
     * The search criteria.
     */
    private final Predicate<Act> search;

    /**
     * Constructs a {@link PatientHistoryFilter}.
     * <p>
     * Items are sorted on ascending timestamp.
     *
     * @param shortNames the history item short names to include
     */
    public PatientHistoryFilter(String[] shortNames) {
        this(shortNames, true);
    }

    /**
     * Constructs a {@link PatientHistoryFilter}.
     *
     * @param shortNames    the history item short names to include
     * @param sortAscending if {@code true} sort items on ascending timestamp; otherwise sort on descending timestamp
     */
    public PatientHistoryFilter(String[] shortNames, boolean sortAscending) {
        this(shortNames, null, sortAscending);
    }

    /**
     * Constructs a {@link PatientHistoryFilter}.
     *
     * @param shortNames    the history item short names to include
     * @param search        the search criteria. May be {@code null}
     * @param sortAscending if {@code true} sort items on ascending timestamp; otherwise sort on descending timestamp
     */
    public PatientHistoryFilter(String[] shortNames, Predicate<Act> search, boolean sortAscending) {
        super();
        this.shortNames = new ArrayList<>(Arrays.asList(shortNames));
        invoice = this.shortNames.remove(CustomerAccountArchetypes.INVOICE_ITEM);
        this.search = search;
        setSortItemsAscending(sortAscending);
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
        if (invoice && TypeHelper.isA(parent, PatientArchetypes.CLINICAL_EVENT)) {
            children = filterInvoiceItems(parent, children);
        }
        if (search == null) {
            result = children;
        } else {
            result = new ArrayList<>();
            for (Act act : children) {
                if (search.test(act)) {
                    result.add(act);
                } else if (supportsVersions(act)) {
                    // need to look at the version acts before deciding to exclude the parent act
                    boolean add = false;
                    for (Act child : getChildren(act, acts)) {
                        if (search.test(child)) {
                            add = true;
                            break;
                        }
                    }
                    if (add) {
                        result.add(act);
                    }
                }
            }
        }
        return result;
    }

    /**
     * Determines if a child act should be included.
     * <p>
     * This implementation excludes children of <em>act.patientClinicalProblem</em> acts that are linked to an event
     * different to the root.
     *
     * @param child  the child act
     * @param parent the parent act
     * @param root   the root act
     * @return {@code true} if the child act should be included
     */
    @Override
    protected boolean include(Act child, Act parent, Act root) {
        if (TypeHelper.isA(parent, PatientArchetypes.CLINICAL_PROBLEM)) {
            ActBean bean = new ActBean(child);
            IMObjectReference event = bean.getNodeSourceObjectRef("event");
            if (event != null && event.getId() != root.getId()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Filters relationships.
     *
     * @param act the act
     * @return the filtered relationships
     */
    @Override
    protected Collection<org.openvpms.component.model.act.ActRelationship> getRelationships(Act act) {
        String[] acts = shortNames.toArray(new String[shortNames.size()]);
        return getRelationships(act.getSourceActRelationships(), createIsA(acts, true));
    }

    /**
     * Determines if an act is a document act that supports versioned documents.
     *
     * @param act the act
     * @return {@code true} if the act supports versioned document
     */
    private boolean supportsVersions(Act act) {
        return act instanceof DocumentAct && TypeHelper.isA(act, InvestigationArchetypes.PATIENT_INVESTIGATION,
                                                            PatientArchetypes.DOCUMENT_ATTACHMENT,
                                                            PatientArchetypes.DOCUMENT_LETTER,
                                                            PatientArchetypes.DOCUMENT_IMAGE);
    }

    /**
     * Excludes invoice items if there is a medication act that links to it.
     *
     * @param event    the <em>act.patientClinicalEvent</em>
     * @param children the included child acts
     * @return the child acts with invoice items added where there is no corresponding medication linking to it
     */
    private List<Act> filterInvoiceItems(Act event, List<Act> children) {
        List<Act> result;
        result = new ArrayList<>(children);
        ActBean bean = new ActBean(event);
        List<IMObjectReference> chargeItemRefs = bean.getNodeTargetObjectRefs("chargeItems");
        if (!chargeItemRefs.isEmpty()) {
            for (int i = 0; i < children.size() && !chargeItemRefs.isEmpty(); ++i) {
                Act act = children.get(i);
                if (TypeHelper.isA(act, PatientArchetypes.PATIENT_MEDICATION)) {
                    ActBean medication = new ActBean(act);
                    List<IMObjectReference> chargeItem = medication.getNodeSourceObjectRefs("invoiceItem");
                    if (!chargeItem.isEmpty()) {
                        chargeItemRefs.remove(chargeItem.get(0));
                    }
                }
            }
            List<Act> chargeItems = ActHelper.getActs(chargeItemRefs);
            result.addAll(chargeItems);
        }
        return result;
    }

}
