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
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.model.object.IMObject;

import java.util.function.Predicate;

/**
 * A predicate that performs a text search on patient history acts.
 *
 * @author Tim Anderson
 */
public class TextSearch implements Predicate<Act> {

    /**
     * The search string.
     */
    private final String search;

    /**
     * Determines if clinician names should be checked.
     */
    private final boolean searchClinician;

    /**
     * Determines if batch numbers should be checked.
     */
    private final boolean searchBatch;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * Constructs a {@link TextSearch}.
     *
     * @param search          the search string
     * @param searchClinician if {@code true}, search clinician names
     * @param searchBatch     if {@code true}, search batch numbers
     * @param service         the archetype service
     */
    public TextSearch(String search, boolean searchClinician, boolean searchBatch, IArchetypeService service) {
        this.search = search.toLowerCase();
        this.searchClinician = searchClinician;
        this.searchBatch = searchBatch;
        this.service = service;
    }

    /**
     * Evaluates this predicate on the given argument.
     *
     * @param act the input argument
     * @return {@code true} if the input argument matches the predicate,
     * otherwise {@code false}
     */
    @Override
    public boolean test(Act act) {
        String description = act.getDescription();
        if (matchesSearch(description)) {
            return true;
        }
        IMObjectBean bean = service.getBean(act);
        if (matchesSearch(bean.getDisplayName())) {
            return true;
        }
        if (bean.hasNode("note")) {
            String note = bean.getString("note");
            if (matchesSearch(note)) {
                return true;
            }
        }
        if (searchClinician && bean.hasNode("clinician")) {
            if (matchesName(bean.getTarget("clinician"))) {
                return true;
            }
        }
        if (bean.isA(CustomerAccountArchetypes.INVOICE_ITEM)) {
            if (matchesName(bean.getTarget("product"))) {
                return true;
            }
        } else if (bean.isA(PatientArchetypes.PATIENT_MEDICATION)) {
            if (matchesSearch(bean.getString("label"))) {
                return true;
            }
            if (searchBatch && matchesName(bean.getTarget("batch"))) {
                return true;
            }
        } else if (bean.isA(InvestigationArchetypes.PATIENT_INVESTIGATION)) {
            if (matchesName(bean.getTarget("investigationType"))) {
                return true;
            }
            if (matchesSearch(Long.toString(act.getId()))) {
                return true;
            }
        } else if (bean.isA(PatientArchetypes.CLINICAL_PROBLEM)) {
            if (matchesName(bean.getLookup("presentingComplaint"))) {
                return true;
            }
        } else if (bean.isA(PatientArchetypes.DOCUMENT_FORM)) {
            if (matchesName(bean.getTarget("documentTemplate"))) {
                return true;
            }
        } else if (act instanceof DocumentAct) {
            if (matchesSearch(((DocumentAct) act).getFileName())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Determines if the search string is contained in the name of the supplied object.
     *
     * @param object the object. May be {@code null}
     * @return {@code true} if the search string is in the name, case-insensitive
     */
    private boolean matchesName(IMObject object) {
        return object != null && matchesSearch(object.getName());
    }

    /**
     * Determines if the search string is contained in the supplied text.
     *
     * @param text the text, Nay be {@code null}
     * @return {@code true} if the search string is in the text, case-insensitive
     */
    private boolean matchesSearch(String text) {
        return text != null && text.toLowerCase().contains(search);
    }

}
