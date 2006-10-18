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
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.app;

import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.ResourceImageReference;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.layout.RowLayoutData;
import org.acegisecurity.Authentication;
import org.acegisecurity.context.SecurityContextHolder;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.resource.util.Messages;


/**
 * Title pane.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class TitlePane extends ContentPane {

    /**
     * The project logo.
     */
    private final String PATH = "/org/openvpms/web/resource/image/openvpms.gif";

    /**
     * The style name.
     */
    private static final String STYLE = "TitlePane";


    /**
     * Construct a new <code>TitlePane</code>.
     */
    public TitlePane() {
        doLayout();
    }

    /**
     * Lay out the component.
     */
    protected void doLayout() {
        setStyleName(STYLE);

        Label logo = LabelFactory.create(new ResourceImageReference(PATH));
        Authentication auth
                = SecurityContextHolder.getContext().getAuthentication();
        RowLayoutData centre = new RowLayoutData();
        centre.setAlignment(new Alignment(Alignment.DEFAULT, Alignment.CENTER));
        logo.setLayoutData(centre);

        Label label = LabelFactory.create(null, "small");
        String name = (auth != null) ? auth.getName() : "";
        label.setText(Messages.get("label.user", name));

        Row labelRow = RowFactory.create("InsetX", label);
        RowLayoutData right = new RowLayoutData();
        right.setAlignment(new Alignment(Alignment.RIGHT, Alignment.BOTTOM));
        right.setWidth(new Extent(100, Extent.PERCENT));
        labelRow.setLayoutData(right);

        Row row = RowFactory.create(logo, labelRow);
        add(row);
    }
}
