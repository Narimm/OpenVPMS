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

package org.openvpms.web.app.patient.mr;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.web.component.event.ActionListener;
import org.apache.commons.lang.ArrayUtils;
import org.openvpms.archetype.rules.patient.InvestigationArchetypes;
import org.openvpms.archetype.rules.patient.PatientArchetypes;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.web.component.im.act.ActHelper;
import org.openvpms.web.component.im.list.ShortNameListCellRenderer;
import org.openvpms.web.component.im.list.ShortNameListModel;
import org.openvpms.web.component.im.query.DateRangeActQuery;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.SelectFieldFactory;


/**
 * Patient medical record summary query. Enables child acts to be filtered.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PatientSummaryQuery extends DateRangeActQuery<Act> {

    /**
     * The act item short names that can be filtered on.
     */
    private String[] actItemShortNames;

    /**
     * The set of possible act item short names.
     */
    private String[] allShortNames;

    /**
     * The act items to display.
     */
    private String[] selectedShortNames;

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
    public PatientSummaryQuery(Party patient) {
        super(patient, "patient", PatientArchetypes.PATIENT_PARTICIPATION,
              new String[]{PatientRecordTypes.CLINICAL_EVENT}, Act.class);
        actItemShortNames = ActHelper.getTargetShortNames(PatientRecordTypes.RELATIONSHIP_CLINICAL_EVENT_ITEM);
        allShortNames = (String[]) ArrayUtils.addAll(actItemShortNames, DOC_VERSION_SHORT_NAMES);
        selectedShortNames = allShortNames;
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
     * Lays out the component in a container, and sets focus on the instance
     * name.
     *
     * @param container the container
     */
    @Override
    protected void doLayout(Component container) {
        final ShortNameListModel model
                = new ShortNameListModel(actItemShortNames, true, false);
        final SelectField shortNameSelector = SelectFieldFactory.create(
                model);
        shortNameSelector.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                int index = shortNameSelector.getSelectedIndex();
                if (model.isAll(index)) {
                    selectedShortNames = allShortNames;
                } else {
                    String shortName = model.getShortName(index);
                    selectedShortNames = getSelectedShortNames(shortName);
                }
                onQuery();
            }
        });
        shortNameSelector.setCellRenderer(new ShortNameListCellRenderer());

        Label typeLabel = LabelFactory.create("type");
        container.add(typeLabel);
        container.add(shortNameSelector);
        getFocusGroup().add(shortNameSelector);
        super.doLayout(container);
    }

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
}
