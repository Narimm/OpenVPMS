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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.workspace;

import echopointng.TabbedPane;
import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.SplitPane;
import nextapp.echo2.app.event.ChangeEvent;
import nextapp.echo2.app.layout.SplitPaneLayoutData;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.event.ChangeListener;
import org.openvpms.web.echo.factory.ColumnFactory;
import org.openvpms.web.echo.factory.SplitPaneFactory;
import org.openvpms.web.echo.factory.TabbedPaneFactory;
import org.openvpms.web.echo.help.HelpContext;
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
        TabComponent tab = getSelected();
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
            TabComponent tab = getSelected();
            if (tab != null) {
                result = tab.getHelpContext();
            }
        }
        return (result == null) ? super.getHelpContext() : result;
    }

    /**
     * Returns the selected tab.
     *
     * @return the selected tab, or {@code null} if none is selected
     */
    protected TabComponent getSelected() {
        return model.getObject(pane.getSelectedIndex());
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
        tabContainer = ColumnFactory.create();
        SplitPaneLayoutData layoutData = new SplitPaneLayoutData();
        layoutData.setOverflow(SplitPaneLayoutData.OVERFLOW_HIDDEN); // to avoid scrollbars in tab section
        tabContainer.setLayoutData(layoutData);
        container = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL_TOP_BOTTOM, "TabbedBrowser", tabContainer);
        model = createTabModel(tabContainer);

        Component heading = super.doLayout();
        SplitPane root = SplitPaneFactory.create(SplitPane.ORIENTATION_VERTICAL, STYLE, heading, container);
        pane = TabbedPaneFactory.create(model);
        pane.setStyleName("VisitEditor.TabbedPane");
        pane.getSelectionModel().addChangeListener(new ChangeListener() {
            @Override
            public void onChange(ChangeEvent event) {
                onTabSelected(model.getObject(pane.getSelectedIndex()));
            }
        });
        tabContainer.add(pane);
        onTabSelected(model.getObject(0));
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

    /**
     * Invoked when a tab is selected.
     *
     * @param tab the tab
     */
    private void onTabSelected(TabComponent tab) {
        if (tab != null) {
            container.removeAll();
            container.add(tabContainer);
            container.add(tab.getComponent());
            tab.show();
        }
    }

}
