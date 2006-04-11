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

package org.openvpms.web.component.im.layout;

import nextapp.echo2.app.Component;

import org.openvpms.web.component.im.filter.BasicNodeFilter;
import org.openvpms.web.component.im.filter.ChainedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.filter.ValueNodeFilter;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.util.TabIndexer;

/**
 * Default implmentation of the {@link LayoutContext} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class DefaultLayoutContext implements LayoutContext {

    /**
     * Determines if this is an edit context.
     */
    private boolean _edit;

    /**
     * The component factory.
     */
    private IMObjectComponentFactory _factory;

    /**
     * The default node filter.
     */
    private NodeFilter _filter;

    /**
     * The tab indexer.
     */
    private TabIndexer _indexer;


    /**
     * Construct a new <code>DefaultLayoutContext</code>.
     */
    public DefaultLayoutContext() {
        this((IMObjectComponentFactory) null);
    }

    /**
     * Construct a new <code>DefaultLayoutContext</code>.
     *
     * @param edit if <code>true</code> this is an edit context; if
     *             <code>false</code> it is a view context.
     */
    public DefaultLayoutContext(boolean edit) {
        this((IMObjectComponentFactory) null);
        _edit = edit;
    }
    /**
     * Construct a new <code>DefaultLayoutContext</code>.
     *
     * @param factory the component factory. May  be <code>null</code>
     */
    public DefaultLayoutContext(IMObjectComponentFactory factory) {
        _factory = factory;
        NodeFilter id = new ValueNodeFilter("uid", new Long(-1));
        NodeFilter showOptional = new BasicNodeFilter(true);
        _filter = new ChainedNodeFilter(id, showOptional);
        _indexer = new TabIndexer();
    }

    /**
     * Construct a new  <code>DefaultLayoutContext</code> from an existing
     * layout context.
     *
     * @param context the context
     */
    public DefaultLayoutContext(LayoutContext context) {
        _factory = context.getComponentFactory();
        _filter = context.getDefaultNodeFilter();
        _indexer = context.getTabIndexer();
        _edit = context.isEdit();
    }

    /**
     * Determines if this is an edit context.
     *
     * @return <code>true</code> if this is an edit context; <code>false</code>
     *         if it is a view context. Defaults to <code>false</code>
     */
    public boolean isEdit() {
        return _edit;
    }

    /**
     * Sets if this is an edit context.
     *
     * @param edit if <code>true</code> this is an edit context; if
     *             <code>false</code> it is a view context.
     */
    public void setEdit(boolean edit) {
        _edit = edit;
    }

    /**
     * Returns the component factory.
     *
     * @return the component factory
     */
    public IMObjectComponentFactory getComponentFactory() {
        return _factory;
    }

    /**
     * Sets the component factory.
     *
     * @param factory the component factory
     */
    public void setComponentFactory(IMObjectComponentFactory factory) {
        _factory = factory;
    }

    /**
     * Returns the tab indexer.
     *
     * @return the tab indexer
     */
    public TabIndexer getTabIndexer() {
        return _indexer;
    }

    /**
     * Sets the tab index of a component.
     *
     * @param component the component
     */
    public void setTabIndex(Component component) {
        _indexer.setTabIndex(component);
    }

    /**
     * Returns the default filter.
     *
     * @return the default filter. May be <code>null</code>
     */
    public NodeFilter getDefaultNodeFilter() {
        return _filter;
    }

    /**
     * Sets the default filter.
     *
     * @param filter the default filter. May be <code>null</code>
     */
    public void setNodeFilter(NodeFilter filter) {
        _filter = filter;
    }
}
