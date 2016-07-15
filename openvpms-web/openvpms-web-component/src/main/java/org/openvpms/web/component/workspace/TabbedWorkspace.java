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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.workspace;

import echopointng.TabbedPane;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ChangeEvent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.event.ChangeListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.LabelFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.factory.TabbedPaneFactory;
import org.openvpms.web.echo.help.HelpContext;
import org.openvpms.web.echo.style.Styles;
import org.openvpms.web.echo.tabpane.ObjectTabPaneModel;
import org.openvpms.web.resource.i18n.Messages;

/**
 * An {@link Workspace} that supports multiple tabs.
 *
 * @author Tim Anderson
 */
public abstract class TabbedWorkspace<T extends IMObject> extends AbstractWorkspace<T> {

    /**
     * The tabbed pane.
     */
    private TabbedPane pane;

    /**
     * The tab pane model.
     */
    private ObjectTabPaneModel<TabComponent> model;

    /**
     * The container for the tabbed pane and the buttons.
     */
    private SplitPane container;

    /**
     * The tabbed pane container.
     */
    private Column tabContainer;

    /**
     * The split pane style.
     */
    private static final String STYLE = "SplitPaneWithButtonRow";

    /**
     * Constructs an {@link TabbedWorkspace}.
     *
     * @param id      the workspace id
     * @param context the context
     */
    public TabbedWorkspace(String id, Context context) {
        super(id, context);
    }

    /**
     * Invoked when the workspace is displayed.
     */
    @Override
    public void show() {
        TabComponent tab = model.getObject(pane.getSelectedIndex());
        if (tab != null) {
            tab.show();
        }
    }

    /**
     * Returns the help context.
     *
     * @return the help context
     */
    @Override
    public HelpContext getHelpContext() {
        HelpContext result = null;
        if (model != null && pane != null) {
            TabComponent tab = model.getObject(pane.getSelectedIndex());
            if (tab != null) {
                result = tab.getHelpContext();
            }
        }
        return (result == null) ? super.getHelpContext() : result;
    }

    /**
     * Lays out the component.
     * <p/>
     * This renders a heading using {@link #createHeading}.
     *
     * @return the component
     */
    @Override
    protected Component doLayout() {
        SplitPane root = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL, STYLE);
        container = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_BOTTOM_TOP, STYLE);
        Component heading = super.doLayout();
        tabContainer = ColumnFactory.create(Styles.INSET_Y);
        model = createTabModel(tabContainer);
        pane = TabbedPaneFactory.create(model);
        pane.getSelectionModel().addChangeListener(new ChangeListener() {
            @Override
            public void onChange(ChangeEvent event) {
                onTabSelected(model.getObject(pane.getSelectedIndex()));
            }
        });
        tabContainer.add(pane);
        onTabSelected(model.getObject(0));

        root.add(heading);
        root.add(this.container);
        return root;
    }

    /**
     * Creates a new tab model.
     *
     * @param container the container
     * @return the tab model
     */
    protected ObjectTabPaneModel<TabComponent> createTabModel(Component container) {
        ObjectTabPaneModel<TabComponent> result = new ObjectTabPaneModel<>(container);
        addTabs(result);
        return result;
    }

    /**
     * Adds tabs to the tabbed pane.
     *
     * @param model the tabbed pane model
     */
    protected abstract void addTabs(ObjectTabPaneModel<TabComponent> model);

    /**
     * Invoked when a tab is selected.
     *
     * @param tab the tab
     */
    private void onTabSelected(TabComponent tab) {
        if (tab != null) {
            container.removeAll();
            Component buttons = tab.getButtons();
            if (buttons != null) {
                container.add(buttons);
            } else {
                container.add(LabelFactory.create());
            }
            container.add(tabContainer);
            tab.show();
        }
    }

    /**
     * Helper to add a tab to the tab pane.
     *
     * @param name  the tab name resource bundle key
     * @param model the tab model
     * @param tab   the component
     */
    protected void addTab(String name, ObjectTabPaneModel<TabComponent> model, TabComponent tab) {
        int index = model.size();
        int shortcut = index + 1;
        String text = "&" + shortcut + " " + Messages.get(name);
        model.addTab(tab, text, tab.getComponent());
    }

    /**
     * Creates a help sub-topic.
     *
     * @param topic the sub-topic
     * @return a new help context
     */
    protected HelpContext subtopic(String topic) {
        return super.getHelpContext().subtopic(topic);
    }

}
