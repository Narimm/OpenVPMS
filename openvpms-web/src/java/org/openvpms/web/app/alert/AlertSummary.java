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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.app.alert;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Color;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.ColumnLayoutData;
import nextapp.echo2.app.layout.RowLayoutData;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ColourHelper;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;

import java.util.Collections;
import java.util.List;


/**
 * Provides a summary of customer/patient alerts.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class AlertSummary {

    /**
     * The alerts.
     */
    private final List<Alert> alerts;

    /**
     * Constructs an <tt>AlertSummary</tt>.
     *
     * @param alerts the alerts
     */
    public AlertSummary(List<Alert> alerts) {
        this.alerts = alerts;
    }

    /**
     * Returns the component.
     *
     * @return the component
     */
    public Component getComponent() {
        Column result = ColumnFactory.create();
        Collections.sort(alerts);
        for (int i = 0; i < alerts.size() && i < 4; ++i) {
            Alert element = alerts.get(i);
            result.add(getButton(element));
        }
        Button viewAll = ButtonFactory.create("alerts.viewall", "small", new ActionListener() {
            public void onAction(ActionEvent event) {
                onShowAll();
            }
        });
        Row right = RowFactory.create(viewAll);

        RowLayoutData rightLayout = new RowLayoutData();
        rightLayout.setAlignment(Alignment.ALIGN_RIGHT);
        rightLayout.setWidth(new Extent(100, Extent.PERCENT));
        right.setLayoutData(rightLayout);

        Row row;
        if (alerts.size() > 4) {
            Label more = LabelFactory.create("alerts.more", "small.bold");
            row = RowFactory.create(more, right);
        } else {
            row = RowFactory.create(right);
            ColumnLayoutData col = new ColumnLayoutData();
            col.setAlignment(Alignment.ALIGN_RIGHT);
            row.setLayoutData(col);
        }
        result.add(row);
        return result;
    }

    /**
     * Displays a dialog with all alerts.
     */
    protected void onShowAll() {
        AlertsViewer viewer = new AlertsViewer(alerts);
        viewer.show();
    }

    /**
     * Returns a button to render the alerts.
     *
     * @param alert the alerts
     * @return a new button
     */
    protected Button getButton(final Alert alert) {
        Lookup lookup = alert.getAlertType();
        Button result = ButtonFactory.create(null, "small");
        result.setText(lookup.getName());
        IMObjectBean bean = new IMObjectBean(lookup);
        Color value = ColourHelper.getColor(bean.getString("colour"));
        if (value != null) {
            result.setBackground(value);
            result.setForeground(ColourHelper.getTextColour(value));
        }

        result.addActionListener(new ActionListener() {
            public void onAction(ActionEvent event) {
                AlertsViewer viewer = new AlertsViewer(alert);
                viewer.show();
            }
        });
        return result;
    }

}
