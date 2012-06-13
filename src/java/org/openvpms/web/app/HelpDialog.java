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

package org.openvpms.web.app;

import echopointng.LabelEx;
import echopointng.xhtml.XhtmlFragment;
import nextapp.echo2.app.Alignment;
import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.app.Button;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Extent;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.ResourceImageReference;
import nextapp.echo2.app.Row;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ActionEvent;
import nextapp.echo2.app.layout.RowLayoutData;
import nextapp.echo2.webcontainer.command.BrowserOpenWindowCommand;
import org.openvpms.web.app.admin.organisation.SubscriptionHelper;
import org.openvpms.web.component.dialog.PopupDialog;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.util.ButtonFactory;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.util.RowFactory;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.resource.util.Messages;
import org.openvpms.web.system.Version;


/**
 * Help dialog.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class HelpDialog extends PopupDialog {

    /**
     * The project logo.
     */
    private static final String PATH = "/org/openvpms/web/resource/image/openvpms.gif";

    /**
     * Constructs a new <code>HelpDialog</code>.
     */
    public HelpDialog() {
        super(Messages.get("helpdialog.title"), OK);
        Label logo = LabelFactory.create(new ResourceImageReference(PATH));
        RowLayoutData centre = new RowLayoutData();
        centre.setAlignment(new Alignment(Alignment.DEFAULT, Alignment.CENTER));
        logo.setLayoutData(centre);

        Label label = LabelFactory.create(null, "small");
        label.setText(Messages.get("helpdialog.version", Version.VERSION, Version.REVISION));

        Row labelRow = RowFactory.create("InsetX", label);
        RowLayoutData right = new RowLayoutData();
        right.setAlignment(new Alignment(Alignment.RIGHT, Alignment.BOTTOM));
        right.setWidth(new Extent(100, Extent.PERCENT));
        labelRow.setLayoutData(right);

        Row row = RowFactory.create(logo, labelRow);
        Column topics = ColumnFactory.create();
        Button helpLink = ButtonFactory.create("helpdialog.topics",
                                               "hyperlink");
        helpLink.setBackground(null); // want to inherit style of parent
        helpLink.addActionListener(new ActionListener() {
            public void onAction(ActionEvent e) {
                String link = Messages.get("helpdialog.topics.link");
                ApplicationInstance.getActive().enqueueCommand(
                        new BrowserOpenWindowCommand(link, null, null));
                close();
            }
        });
        topics.add(RowFactory.create(helpLink)); // force to minimum width
        String content = "<p xmlns='http://www.w3.org/1999/xhtml'>" + SubscriptionHelper.formatSubscription() + "</p>";
        LabelEx subscription = new LabelEx(new XhtmlFragment(content));
        subscription.setLineWrap(true);
        subscription.setTextAlignment(Alignment.ALIGN_CENTER);
        Column column = ColumnFactory.create("CellSpacing", row, topics);
        SplitPane panel = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP, "HelpDialog.content",
                                                  ColumnFactory.create("Inset", subscription),
                                                  ColumnFactory.create("Inset", column));
        getLayout().add(panel);
    }
}
