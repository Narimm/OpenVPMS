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
 */

package org.openvpms.web.app.workflow.investigation;

import echopointng.GroupBox;
import nextapp.echo2.app.Component;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.web.app.reporting.AbstractReportingWorkspace;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.DefaultContextSwitchListener;
import org.openvpms.web.component.focus.FocusGroup;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.Browser;
import org.openvpms.web.component.im.query.DefaultIMObjectTableBrowser;
import org.openvpms.web.component.im.query.Query;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.component.util.GroupBoxFactory;


/**
 * Workspace to display list of investigation results.
 *
 * @author Tim Anderson
 */
public class InvestigationsWorkspace extends AbstractReportingWorkspace<Act> {

    /**
     * Constructs an {@code InvestigationsWorkspace}.
     *
     * @param context the context
     */
    public InvestigationsWorkspace(Context context) {
        super("workflow", "investigation", Act.class, context);
    }

    /**
     * Lays out the components.
     *
     * @param container the container
     * @param group     the focus group
     */
    @Override
    protected void doLayout(Component container, FocusGroup group) {
        Query<Act> query = new InvestigationsQuery();

        // create a layout context, with hyperlinks enabled
        LayoutContext context = new DefaultLayoutContext(getContext(), getHelpContext());
        TableComponentFactory factory = new TableComponentFactory(context);
        context.setComponentFactory(factory);
        context.setContextSwitchListener(DefaultContextSwitchListener.INSTANCE);

        InvestigationsTableModel model = new InvestigationsTableModel(context);
        Browser<Act> browser = new DefaultIMObjectTableBrowser<Act>(query, model, context);
        GroupBox box = GroupBoxFactory.create(browser.getComponent());
        container.add(box);
        group.add(browser.getFocusGroup());
    }

    /**
     * Determines if the workspace should be refreshed.
     *
     * @return {@code true}
     */
    @Override
    protected boolean refreshWorkspace() {
        return true;
    }

}
