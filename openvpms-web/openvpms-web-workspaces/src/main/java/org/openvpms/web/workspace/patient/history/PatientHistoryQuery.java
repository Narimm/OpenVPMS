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

import nextapp.echo2.app.Button;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.prefs.PreferenceArchetypes;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.web.component.im.list.ShortNameListModel;
import org.openvpms.web.component.im.query.PageLocator;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.im.relationship.RelationshipHelper;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.focus.FocusGroup;


/**
 * Patient medical record history query.
 * <p>
 * This returns <em>act.patientClinicalEvent</em> acts within a date range.
 * <br/>
 * It provides a selector to filter acts items; filtering must be performed by the caller.
 *
 * @author Tim Anderson
 */
public class PatientHistoryQuery extends AbstractPatientHistoryQuery {

    /**
     * Determines if charges are included.
     */
    private CheckBox includeCharges;

    /**
     * The short names to query.
     */
    private static final String[] SHORT_NAMES = new String[]{PatientArchetypes.CLINICAL_EVENT};

    /**
     * Document act version short names.
     */
    private static final String[] DOC_VERSION_SHORT_NAMES = new String[]{
            InvestigationArchetypes.PATIENT_INVESTIGATION_VERSION,
            PatientArchetypes.DOCUMENT_ATTACHMENT_VERSION,
            PatientArchetypes.DOCUMENT_IMAGE_VERSION,
            PatientArchetypes.DOCUMENT_LETTER_VERSION};


    /**
     * Constructs a {@link PatientHistoryQuery}.
     *
     * @param patient     the patient to query
     * @param preferences user preferences
     */
    public PatientHistoryQuery(Party patient, Preferences preferences) {
        super(patient, SHORT_NAMES, preferences);

        boolean charges = preferences.getBoolean(PreferenceArchetypes.HISTORY, "showCharges", true);
        init(charges);
    }

    /**
     * Constructs a {@link PatientHistoryQuery}.
     *
     * @param patient the patient to query
     * @param charges if {@code true}, include charges
     */
    public PatientHistoryQuery(Party patient, boolean charges) {
        super(patient, SHORT_NAMES, null);
        init(charges);
    }

    /**
     * Determines if charges should be included.
     *
     * @param include if {@code true}, include charges, else exclude them
     */
    public void setIncludeCharges(boolean include) {
        includeCharges.setSelected(include);
        onIncludeChargesChanged();
    }

    /**
     * Determines the page that an event falls on, excluding any date range constraints.
     *
     * @param object the event
     * @return the page that the event would fall on, if present
     */
    public int getPage(Act object) {
        int page = 0;
        ActBean bean = new ActBean(object);
        IMObjectReference patient = bean.getNodeParticipantRef("patient");
        if (patient != null && ObjectUtils.equals(patient, getEntityId())) {
            ArchetypeQuery query = new ArchetypeQuery(PatientArchetypes.CLINICAL_EVENT);
            query.add(new ParticipantConstraint("patient", PatientArchetypes.PATIENT_PARTICIPATION, patient));
            page = QueryHelper.getPage(object, query, getMaxResults(), "startTime", false, PageLocator.DATE_COMPARATOR);
        }
        return page;
    }

    /**
     * Initialises the query.
     *
     * @param charges if {@code true}, display charges
     */
    protected void init(boolean charges) {
        String[] actItemShortNames = RelationshipHelper.getTargetShortNames(PatientArchetypes.CLINICAL_EVENT_ITEM);
        String[] shortNames = (String[]) ArrayUtils.addAll(actItemShortNames, DOC_VERSION_SHORT_NAMES);
        setItemShortNames(shortNames);
        if (charges) {
            setSelectedItemShortNames((String[]) ArrayUtils.add(shortNames, CustomerAccountArchetypes.INVOICE_ITEM));
        } else {
            setSelectedItemShortNames(shortNames);
        }
        includeCharges = CheckBoxFactory.create("patient.record.query.includeCharges", charges);

        includeCharges.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onIncludeChargesChanged();
                onQuery();
            }
        });
    }

    /**
     * Lays out the component in a container, and sets focus on the instance name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        Label typeLabel = LabelFactory.create("query.type");
        container.add(typeLabel);
        SelectField shortNameSelector = getShortNameSelector();
        container.add(shortNameSelector);
        container.add(includeCharges);
        Button sort = getSort();
        container.add(sort);
        FocusGroup focusGroup = getFocusGroup();
        focusGroup.add(shortNameSelector);
        focusGroup.add(includeCharges);
        focusGroup.add(sort);
        addSearchField(container);
        super.doLayout(container);
    }

    /**
     * Updates the short names to query.
     *
     * @param model    the model
     * @param selected the selected index
     */
    protected void updateSelectedShortNames(ShortNameListModel model, int selected) {
        String[] shortNames;
        if (model.isAll(selected)) {
            shortNames = model.getShortNames();
        } else {
            String shortName = model.getShortName(selected);
            shortNames = getSelectedShortNames(shortName);
        }
        if (includeCharges.isSelected()) {
            shortNames = (String[]) ArrayUtils.add(shortNames, CustomerAccountArchetypes.INVOICE_ITEM);
        }
        setSelectedItemShortNames(shortNames);
    }

    /**
     * Returns the selected short names.
     *
     * @param shortName the short name
     * @return the corresponding short names
     */
    private String[] getSelectedShortNames(String shortName) {
        if (InvestigationArchetypes.PATIENT_INVESTIGATION.equals(shortName)) {
            return new String[]{shortName, InvestigationArchetypes.PATIENT_INVESTIGATION_VERSION};
        } else if (PatientArchetypes.DOCUMENT_ATTACHMENT.equals(shortName)) {
            return new String[]{shortName, PatientArchetypes.DOCUMENT_ATTACHMENT_VERSION};
        } else if (PatientArchetypes.DOCUMENT_IMAGE.equals(shortName)) {
            return new String[]{shortName, PatientArchetypes.DOCUMENT_IMAGE_VERSION};
        } else if (PatientArchetypes.DOCUMENT_LETTER.equals(shortName)) {
            return new String[]{shortName, PatientArchetypes.DOCUMENT_LETTER_VERSION};
        }
        return new String[]{shortName};
    }

    /**
     * Invoked when the include charges flag is changed.
     * <p>
     * This updates the selected short names, and the session preferences.
     */
    private void onIncludeChargesChanged() {
        updateSelectedShortNames();

        // update session preferences
        Preferences preferences = getPreferences();
        if (preferences != null) {
            preferences.setPreference(PreferenceArchetypes.HISTORY, "showCharges", includeCharges.isSelected());
        }
    }
}
