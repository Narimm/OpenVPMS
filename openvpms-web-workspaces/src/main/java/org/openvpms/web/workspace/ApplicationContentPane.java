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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace;

import nextapp.echo2.app.ContentPane;
import nextapp.echo2.app.SplitPane;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.workspace.WorkspacesFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.workspace.workflow.messaging.MessageMonitor;


/**
 * Content pane that displays the {@link TitlePane} and {@link MainPane}.
 *
 * @author Tim Anderson
 */
public class ApplicationContentPane extends ContentPane {

    /**
     * The context.
     */
    private final GlobalContext context;

    /**
     * The workspaces factory.
     */
    private final WorkspacesFactory factory;

    /**
     * The layout pane style name.
     */
    private static final String LAYOUT_STYLE = "ApplicationContentPane.Layout";


    /**
     * Constructs an {@code ApplicationContentPane}.
     *
     * @param context the context
     * @param factory the workspaces factory
     */
    public ApplicationContentPane(GlobalContext context, WorkspacesFactory factory) {
        this.context = context;
        this.factory = factory;
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
        PracticeRules practiceRules = ServiceHelper.getBean(PracticeRules.class);
        MessageMonitor messageMonitor = ServiceHelper.getBean(MessageMonitor.class);
        layout.add(new TitlePane(practiceRules, context));
        layout.add(new MainPane(messageMonitor, context, factory));
        add(layout);
    }

}
