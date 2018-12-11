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
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Row;
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
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.model.entity.Entity;
import org.openvpms.component.model.object.Reference;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.web.component.im.list.ShortNameListModel;
import org.openvpms.web.component.im.query.PageLocator;
import org.openvpms.web.component.im.query.ParticipantConstraint;
import org.openvpms.web.component.im.query.QueryHelper;
import org.openvpms.web.component.im.relationship.RelationshipHelper;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.GridFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.RowFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.style.Styles;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


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
     * Determines if the history is sorted on ascending or descending start time.
     */
    private final boolean sortHistoryAscending;

    /**
     * Determines if charges are included.
     */
    private CheckBox includeCharges;

    /**
     * The product type selector.
     */
    private ProductTypeSelector productType;

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
        sortHistoryAscending = getSortHistoryAscending(preferences);
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
        sortHistoryAscending = false;
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
        IMObjectBean bean = new IMObjectBean(object);
        Reference patient = bean.getTargetRef("patient");
        if (patient != null && ObjectUtils.equals(patient, getEntityId())) {
            ArchetypeQuery query = new ArchetypeQuery(PatientArchetypes.CLINICAL_EVENT);
            query.add(new ParticipantConstraint("patient", PatientArchetypes.PATIENT_PARTICIPATION, patient));
            page = QueryHelper.getPage(object, query, getMaxResults(), "startTime", sortHistoryAscending,
                                       PageLocator.DATE_COMPARATOR);
        }
        return page;
    }

    /**
     * Returns the product types to select.
     *
     * @return the product type references, or an empty list if all product types are being selected
     */
    public Set<Reference> getProductTypes() {
        Set<Reference> result;
        ProductTypeSelector selector = getProductTypeSelector();
        if (selector.isAll()) {
            result = Collections.emptySet();
        } else {
            result = new HashSet<>();
            for (Entity entity : selector.getSelected()) {
                result.add(entity.getObjectReference());
            }
        }
        return result;
    }

    /**
     * Returns the preferred height of the query when rendered.
     *
     * @return the preferred height, or {@code null} if it has no preferred height
     */
    @Override
    public Extent getHeight() {
        return super.getHeight(2);
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
        includeCharges = CheckBoxFactory.create(charges);

        includeCharges.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onIncludeChargesChanged();
                onQuery();
            }
        });

        if (sortHistoryAscending) {
            setDefaultSortConstraint(ASCENDING_START_TIME);
        } else {
            setDefaultSortConstraint(DESCENDING_START_TIME);
        }
    }

    /**
     * Lays out the component in a container, and sets focus on the instance name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        FocusGroup focusGroup = getFocusGroup();
        SelectField shortNameSelector = getShortNameSelector();
        Button sort = getSort();
        ProductTypeSelector selector = getProductTypeSelector();

        container.add(LabelFactory.create("query.type"));
        container.add(shortNameSelector);
        focusGroup.add(shortNameSelector);
        Row subrow = RowFactory.create(Styles.CELL_SPACING, LabelFactory.create("patient.record.query.includeCharges"),
                                       includeCharges, sort);
        container.add(subrow);
        focusGroup.add(includeCharges);
        focusGroup.add(sort);
        addSearchField(subrow);
        container.add(LabelFactory.create("patient.record.query.productType"));
        container.add(selector);
        focusGroup.add(selector.getTarget());
        addDateRange(container);
    }

    /**
     * Creates a container component to lay out the query component in.
     *
     * @return a new container
     * @see #doLayout(Component)
     */
    @Override
    protected Component createContainer() {
        return GridFactory.create(3);
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
     * Determines if visits are sorted on ascending or descending start time.
     *
     * @param preferences user preferences
     * @return the {@code true} if visits are sorted on ascending start time, {@code false} if they are sorted
     * descending
     */
    protected boolean getSortHistoryAscending(Preferences preferences) {
        String sort = preferences.getString(PreferenceArchetypes.HISTORY, "historySort", "DESC");
        return "ASC".equals(sort);
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

    /**
     * Returns the product type selector.
     *
     * @return the product type selector
     */
    private ProductTypeSelector getProductTypeSelector() {
        if (productType == null) {
            productType = new ProductTypeSelector();
            productType.setListener(this::onQuery);
        }
        return productType;
    }
}
