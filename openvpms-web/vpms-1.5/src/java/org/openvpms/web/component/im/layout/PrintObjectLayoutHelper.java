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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.web.component.im.layout;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.layout.ColumnLayoutData;
import nextapp.echo2.app.layout.RowLayoutData;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.RowFactory;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class PrintObjectLayoutHelper {

    /**
     * The button label.
     */
    private final String label;

    /**
     * Determines if the button should be enabled.
     */
    private boolean enableButton = true;


    /**
     * Constructs a <tt>PrintObjectLayoutHelper</tt>.
     *
     * @param label the button label
     */
    public PrintObjectLayoutHelper(String label) {
        this.label = label;
    }

    /**
     * Determines if the button should be enabled.
     *
     * @param enable if <tt>true</tt>, enable the button
     */
    public void setEnableButton(boolean enable) {
        this.enableButton = enable;
    }

    public Button doLayout(Component container) {
        Button button = ButtonFactory.create(label);
        button.setEnabled(enableButton);
        RowLayoutData rowLayout = new RowLayoutData();
        Alignment topRight = new Alignment(Alignment.RIGHT, Alignment.TOP);
        rowLayout.setAlignment(topRight);
        rowLayout.setWidth(new Extent(100, Extent.PERCENT));
        button.setLayoutData(rowLayout);
        Row row = RowFactory.create("InsetX", button);
        ColumnLayoutData columnLayout = new ColumnLayoutData();
        columnLayout.setAlignment(topRight);
        row.setLayoutData(columnLayout);
        container.add(row);
        return button;
    }

}
