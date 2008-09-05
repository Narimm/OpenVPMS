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
import nextapp.echo2.app.event.ActionListener;
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
     * The set of possible act item short names.
     */
    private String[] allShortNames;

    /**
     * The act items to display.
     */
    private String[] itemShortNames;


    /**
     * Constructs a new <tt>PatientSummaryQuery</tt>.
     *
     * @param patient the patient to query
     */
    public PatientSummaryQuery(Party patient) {
        super(patient, "patient", "participation.patient",
              new String[]{PatientRecordTypes.CLINICAL_EVENT}, Act.class);
        allShortNames = ActHelper.getTargetShortNames(
                PatientRecordTypes.RELATIONSHIP_CLINICAL_EVENT_ITEM);

        itemShortNames = allShortNames;
        setAuto(true);
    }

    /**
     * Returns the short names of the selected act items.
     *
     * @return the act item short names
     */
    public String[] getActItemShortNames() {
        return itemShortNames;
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
                = new ShortNameListModel(allShortNames, true, false);
        final SelectField shortNameSelector = SelectFieldFactory.create(
                model);
        shortNameSelector.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                int index = shortNameSelector.getSelectedIndex();
                if (model.isAll(index)) {
                    itemShortNames = allShortNames;
                } else {
                    String shortName = model.getShortName(index);
                    itemShortNames = new String[]{shortName};
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

}
