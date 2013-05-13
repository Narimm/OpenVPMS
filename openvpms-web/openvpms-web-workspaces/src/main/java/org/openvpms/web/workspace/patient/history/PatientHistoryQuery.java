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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.workspace.patient.history;

import nextapp.echo2.app.Button;
import nextapp.echo2.app.CheckBox;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import org.apache.commons.lang.ArrayUtils;
import org.openvpms.archetype.rules.finance.account.CustomerAccountArchetypes;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.list.ShortNameListCellRenderer;
import org.openvpms.web.component.im.list.ShortNameListModel;
import org.openvpms.web.component.im.query.DateRangeActQuery;
import org.openvpms.web.component.im.relationship.RelationshipHelper;
import org.openvpms.web.echo.event.ActionListener;
import org.openvpms.web.echo.factory.ButtonFactory;
import org.openvpms.web.echo.factory.CheckBoxFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SelectFieldFactory;
import org.openvpms.web.echo.focus.FocusGroup;
import org.openvpms.web.echo.focus.FocusHelper;
import org.openvpms.web.resource.i18n.Messages;


/**
 * Patient medical record history query.
 * <p/>
 * This returns <em>act.patientClinicalEvent</em> acts within a date range.
 * <br/>
 * It provides a selector to filter acts items; filtering must be performed by the caller.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientHistoryQuery extends DateRangeActQuery<Act> {

    /**
     * The act item short names that can be filtered on.
     */
    private String[] actItemShortNames;

    /**
     * The set of possible act item short names, excluding the <em>act.customerAccountInvoiceItem</em>.
     */
    private String[] shortNames;

    /**
     * The act items to display.
     */
    private String[] selectedShortNames;

    /**
     * Determines if charges are included.
     */
    private CheckBox includeCharges;

    /**
     * Determines if the visit items are being sorted ascending or descending.
     */
    private boolean sortAscending = true;

    /**
     * Button to change the visit items sort order.
     */
    private Button sort;

    /**
     * Document act version short names.
     */
    private static final String[] DOC_VERSION_SHORT_NAMES = new String[]{
        InvestigationArchetypes.PATIENT_INVESTIGATION_VERSION,
        PatientArchetypes.DOCUMENT_ATTACHMENT_VERSION,
        PatientArchetypes.DOCUMENT_IMAGE_VERSION,
        PatientArchetypes.DOCUMENT_LETTER_VERSION};


    /**
     * Constructs a new <tt>PatientSummaryQuery</tt>.
     *
     * @param patient the patient to query
     */
    public PatientHistoryQuery(Party patient) {
        super(patient, "patient", PatientArchetypes.PATIENT_PARTICIPATION,
              new String[]{PatientArchetypes.CLINICAL_EVENT}, Act.class);
        actItemShortNames = RelationshipHelper.getTargetShortNames(PatientArchetypes.CLINICAL_EVENT_ITEM);
        shortNames = (String[]) ArrayUtils.addAll(actItemShortNames, DOC_VERSION_SHORT_NAMES);
        selectedShortNames = (String[]) ArrayUtils.add(shortNames, CustomerAccountArchetypes.INVOICE_ITEM);
        setAuto(true);
    }

    /**
     * Returns the short names of the selected act items.
     *
     * @return the act item short names
     */
    public String[] getActItemShortNames() {
        return selectedShortNames;
    }

    /**
     * Sets the state of the <em>allDates</em> checkbox, if present.
     *
     * @param selected the state of the <em>allDates</em> checkbox
     */
    public void setAllDates(boolean selected) {
        getDateRange().setAllDates(selected);
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
     * Lays out the component in a container, and sets focus on the instance name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        final ShortNameListModel model = new ShortNameListModel(actItemShortNames, true, false);
        final SelectField shortNameSelector = SelectFieldFactory.create(model);
        ActionListener listener = new ActionListener() {
            public void onAction(ActionEvent event) {
                int index = shortNameSelector.getSelectedIndex();
                if (model.isAll(index)) {
                    selectedShortNames = shortNames;
                } else {
                    String shortName = model.getShortName(index);
                    selectedShortNames = getSelectedShortNames(shortName);
                }
                if (includeCharges.isSelected()) {
                    selectedShortNames = (String[]) ArrayUtils.add(selectedShortNames,
                                                                   CustomerAccountArchetypes.INVOICE_ITEM);
                }
                onQuery();
            }
        };
        shortNameSelector.addActionListener(listener);
        shortNameSelector.setCellRenderer(new ShortNameListCellRenderer());

        Label typeLabel = LabelFactory.create("query.type");
        container.add(typeLabel);
        container.add(shortNameSelector);
        includeCharges = CheckBoxFactory.create("patient.record.query.includeCharges", true);
        includeCharges.addActionListener(listener);

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

        container.add(includeCharges);
        container.add(sort);
        FocusGroup focusGroup = getFocusGroup();
        focusGroup.add(shortNameSelector);
        focusGroup.add(includeCharges);
        focusGroup.add(sort);
        super.doLayout(container);
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
     * Sets the sort button icon.
     */
    private void setSortIcon() {
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
}
