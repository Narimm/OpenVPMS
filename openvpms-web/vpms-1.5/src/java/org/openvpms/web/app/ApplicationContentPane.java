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

import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.SplitPane;
import org.openvpms.web.component.util.SplitPaneFactory;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.app.workflow.messaging.MessageMonitor;


/**
 * Content pane that displays the {@link TitlePane} and {@link MainPane}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ApplicationContentPane extends ContentPane {

    /**
     * The layout pane style name.
     */
    private static final String LAYOUT_STYLE = "ApplicationContentPane.Layout";


    /**
     * Constructs a <tt>ApplicationContentPane</tt>.
     */
    public ApplicationContentPane() {
    }

    /**
     * @see nextapp.echo2.app.Component#init()
     */
    public void init() {
        super.init();
        doLayout();
    }

    /**
     * Lays out the component.
     */
    protected void doLayout() {
        SplitPane layout = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL, LAYOUT_STYLE);
        MessageMonitor messageMonitor = (MessageMonitor) ServiceHelper.getContext().getBean("messageMonitor");
        layout.add(new TitlePane(messageMonitor));
        layout.add(new MainPane());
        add(layout);
    }

}
