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
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.archetype.rules.prefs.PreferenceArchetypes;
import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.list.ShortNameListCellRenderer;
import org.openvpms.web.component.im.list.ShortNameListModel;
import org.openvpms.web.component.im.query.DateRangeActQuery;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.echo.focus.FocusHelper;
import org.openvpms.web.resource.i18n.Messages;

/**
 * Patient history query.
 *
 * @author Tim Anderson
 */
public abstract class AbstractPatientHistoryQuery extends DateRangeActQuery<Act> {

    /**
     * User preferences.
     */
    private final Preferences preferences;

    /**
     * The act items to query.
     */
    private String[] selectedShortNames;

    /***
     * Determines if the items are being sorted ascending or descending.
     */
    private boolean sortAscending = true;

    /**
     * The item short name model.
     */
    private ShortNameListModel model;

    /**
     * The item short name selector.
     */
    private SelectField shortNameSelector;

    /**
     * Button to change the items sort order.
     */
    private Button sort;


    /**
     * Constructs a {@link AbstractPatientHistoryQuery}.
     *
     * @param patient     the patient to query
     * @param shortNames  the act short names
     * @param preferences the user preferences. May be {@code null}
     */
    public AbstractPatientHistoryQuery(Party patient, String[] shortNames, Preferences preferences) {
        super(patient, "patient", PatientArchetypes.PATIENT_PARTICIPATION, shortNames, Act.class);
        this.preferences = preferences;
        setAuto(true);
        boolean ascending = (preferences == null) || isSortAscending(preferences);
        setSortAscending(ascending);
    }

    /**
     * Sets the available item short names to query.
     *
     * @param shortNames the short names
     */
    public void setItemShortNames(String[] shortNames) {
        if (model == null) {
            model = new ShortNameListModel(shortNames, true, false);
            shortNameSelector = SelectFieldFactory.create(model);
            ActionListener listener = new ActionListener() {
                public void onAction(ActionEvent event) {
                    updateSelectedShortNames(model, shortNameSelector.getSelectedIndex());
                    onQuery();
                }
            };
            shortNameSelector.addActionListener(listener);
            shortNameSelector.setCellRenderer(new ShortNameListCellRenderer());
        }
    }

    /**
     * Returns the available item short names to query.
     *
     * @return the short names
     */
    public String[] getItemShortNames() {
        return model != null ? model.getShortNames() : new String[0];
    }

    /**
     * Sets the item short names to query.
     *
     * @param shortNames the short names
     */
    public void setSelectedItemShortNames(String[] shortNames) {
        selectedShortNames = shortNames;
    }

    /**
     * Returns the item short names to query.
     *
     * @return the short names
     */
    public String[] getSelectedItemShortNames() {
        return selectedShortNames != null ? selectedShortNames : new String[0];
    }

    /**
     * Determines if the visit items are being sorted ascending or descending.
     *
     * @param ascending if {@code true} visit items are to be sorted ascending; {@code false} if descending
     */
    public void setSortAscending(boolean ascending) {
        sortAscending = ascending;
        if (sort != null) {
            setSortIcon();
        }
        // update session preferences
        if (preferences != null) {
            preferences.setPreference(PreferenceArchetypes.HISTORY, "sort", ascending ? "ASC" : "DESC");
        }
    }

    /**
     * Determines if the visit items are being sorted ascending or descending.
     *
     * @return {@code true} if visit items are being sorted ascending; {@code false} if descending
     */
    public boolean isSortAscending() {
        return sortAscending;
    }

    /**
     * Returns the value being queried on.
     * <p/>
     * This implementation does not append wildcards; all searches are 'contains'.
     *
     * @return the value. May be {@code null}
     */
    @Override
    public String getValue() {
        return StringUtils.trimToNull(getSearchField().getText());
    }

    /**
     * Invoked when the search field changes. Invokes {@link #onQuery} and resets the focus back to the search field.
     */
    @Override
    protected void onSearchFieldChanged() {
        super.onSearchFieldChanged();
        FocusHelper.setFocus(getSearchField());
    }

    /**
     * Returns the user preferences.
     *
     * @return the preferences. May be {@code null}
     */
    protected Preferences getPreferences() {
        return preferences;
    }

    /**
     * Returns the item short name selector.
     *
     * @return the item short name selector
     */
    protected SelectField getShortNameSelector() {
        return shortNameSelector;
    }

    /**
     * Returns the sort button.
     *
     * @return the sort button
     */
    protected Button getSort() {
        if (sort == null) {
            sort = ButtonFactory.create(new ActionListener() {
                @Override
                public void onAction(ActionEvent event) {
                    sortAscending = !sortAscending;
                    setSortIcon();
                    onQuery();
                    FocusHelper.setFocus(sort);
                }
            });
            setSortIcon();
        }
        return sort;
    }

    /**
     * Updates the item short names to query.
     * <p/>
     * This delegates to {@link #updateSelectedShortNames(ShortNameListModel, int)}
     */
    protected void updateSelectedShortNames() {
        updateSelectedShortNames(model, shortNameSelector.getSelectedIndex());
    }

    /**
     * Updates the item short names to query.
     *
     * @param model    the model
     * @param selected the selected index
     */
    protected abstract void updateSelectedShortNames(ShortNameListModel model, int selected);

    /**
     * Sets the sort button icon.
     */
    protected void setSortIcon() {
        String style;
        String toolTip;
        if (sortAscending) {
            style = "sort.ascending";
            toolTip = Messages.get("patient.record.query.sortAscending");
        } else {
            style = "sort.descending";
            toolTip = Messages.get("patient.record.query.sortDescending");
        }
        sort.setStyleName(style);
        sort.setToolTipText(toolTip);
    }

    /**
     * Determines if medical records are sorted ascending or descending.
     *
     * @param preferences user preferences
     * @return the sort order
     */
    protected boolean isSortAscending(Preferences preferences) {
        String sort = preferences.getString(PreferenceArchetypes.HISTORY, "sort", "ASC");
        return "ASC".equals(sort);
    }

}
