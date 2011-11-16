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

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextSwitchListener;
import org.openvpms.web.component.app.GlobalContext;
import org.openvpms.web.component.im.filter.BasicNodeFilter;
import org.openvpms.web.component.im.filter.ChainedNodeFilter;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.filter.ValueNodeFilter;
import org.openvpms.web.component.im.util.DefaultIMObjectCache;
import org.openvpms.web.component.im.util.DefaultIMObjectDeletionListener;
import org.openvpms.web.component.im.util.IMObjectCache;
import org.openvpms.web.component.im.util.IMObjectDeletionListener;
import org.openvpms.web.component.im.util.SoftRefIMObjectCache;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.im.view.layout.ViewLayoutStrategyFactory;

import java.util.HashSet;
import java.util.Set;


/**
 * Abstract implementation of the {@link LayoutContext} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractLayoutContext implements LayoutContext {

    /**
     * The parent layout context.
     */
    private LayoutContext parent;

    /**
     * The context.
     */
    private Context context;

    /**
     * The cache.
     */
    private IMObjectCache cache;

    /**
     * Determines if this is an edit context.
     */
    private boolean edit;

    /**
     * The component factory.
     */
    private IMObjectComponentFactory factory;

    /**
     * The default node filter.
     */
    private NodeFilter filter;

    /**
     * The layout strategy factory.
     */
    private IMObjectLayoutStrategyFactory layoutFactory = DEFAULT_LAYOUT_FACTORY;

    /**
     * The layout depth.
     */
    private int depth;

    /**
     * The deletion listener.
     */
    private IMObjectDeletionListener<IMObject> deletionListener = DEFAULT_DELETION_LISTENER;

    /**
     * The context switch listener.
     */
    private ContextSwitchListener contextSwitchListener;

    /**
     * The set of rendered objects.
     */
    private Set<IMObjectReference> rendered = new HashSet<IMObjectReference>();

    /**
     * The default layout strategy factory.
     */
    private static final IMObjectLayoutStrategyFactory DEFAULT_LAYOUT_FACTORY
            = new ViewLayoutStrategyFactory();

    /**
     * The default deletion listener.
     */
    private static final IMObjectDeletionListener<IMObject> DEFAULT_DELETION_LISTENER
            = new DefaultIMObjectDeletionListener();


    /**
     * Construct a new <tt>AbstractLayoutContext</tt>.
     */
    public AbstractLayoutContext() {
        this((IMObjectComponentFactory) null);
    }

    /**
     * Construct a new <tt>AbstractLayoutContext</tt>.
     *
     * @param edit if <tt>true</tt> this is an edit context; if
     *             <tt>false</tt> it is a view context.
     */
    public AbstractLayoutContext(boolean edit) {
        this((IMObjectComponentFactory) null);
        this.edit = edit;
    }

    /**
     * Construct a new <tt>AbstractLayoutContext</tt>.
     *
     * @param factory the component factory. May  be <tt>null</tt>
     */
    public AbstractLayoutContext(IMObjectComponentFactory factory) {
        this.factory = factory;
        NodeFilter id = new ValueNodeFilter("id", -1);
        NodeFilter showOptional = new BasicNodeFilter(true);
        filter = new ChainedNodeFilter(id, showOptional);
    }

    /**
     * Construct a new  <tt>AbstractLayoutContext</tt> from an existing
     * layout context. Increases the layout depth by 1.
     *
     * @param context the context
     */
    public AbstractLayoutContext(LayoutContext context) {
        this.parent = context;
        this.context = context.getContext();
        this.cache = context.getCache();
        factory = context.getComponentFactory();
        filter = context.getDefaultNodeFilter();
        edit = context.isEdit();
        layoutFactory = context.getLayoutStrategyFactory();
        depth = context.getLayoutDepth() + 1;
        deletionListener = context.getDeletionListener();
        contextSwitchListener = context.getContextSwitchListener();
    }

    /**
     * Returns the context.
     *
     * @return the context
     */
    public Context getContext() {
        if (context == null) {
            context = GlobalContext.getInstance();
        }
        return context;
    }

    /**
     * Sets the context.
     *
     * @param context the context
     */
    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * Returns the object cache.
     * <p/>
     * If no cache exists, a {@link DefaultIMObjectCache} will be created.
     *
     * @return the object cache
     */
    public IMObjectCache getCache() {
        if (cache == null) {
            cache = new SoftRefIMObjectCache();
        }
        return cache;
    }

    /**
     * Sets the object cache.
     *
     * @param cache the cache
     */
    public void setCache(IMObjectCache cache) {
        this.cache = cache;
    }

    /**
     * Determines if this is an edit context.
     *
     * @return <tt>true</tt> if this is an edit context; <tt>false</tt>
     *         if it is a view context. Defaults to <tt>false</tt>
     */
    public boolean isEdit() {
        return edit;
    }

    /**
     * Sets if this is an edit context.
     *
     * @param edit if <tt>true</tt> this is an edit context; if
     *             <tt>false</tt> it is a view context.
     */
    public void setEdit(boolean edit) {
        this.edit = edit;
    }

    /**
     * Returns the component factory.
     *
     * @return the component factory
     */
    public IMObjectComponentFactory getComponentFactory() {
        return factory;
    }

    /**
     * Sets the component factory.
     *
     * @param factory the component factory
     */
    public void setComponentFactory(IMObjectComponentFactory factory) {
        this.factory = factory;
    }

    /**
     * Returns the default filter.
     *
     * @return the default filter. May be <tt>null</tt>
     */
    public NodeFilter getDefaultNodeFilter() {
        return filter;
    }

    /**
     * Sets the default filter.
     *
     * @param filter the default filter. May be <tt>null</tt>
     */
    public void setNodeFilter(NodeFilter filter) {
        this.filter = filter;
    }

    /**
     * Returns the layout strategy factory.
     *
     * @return the layout strategy factory
     */
    public IMObjectLayoutStrategyFactory getLayoutStrategyFactory() {
        return layoutFactory;
    }

    /**
     * Sets the layout strategy factory.
     *
     * @param factory the layout strategy factory
     */
    public void setLayoutStrategyFactory(
            IMObjectLayoutStrategyFactory factory) {
        layoutFactory = factory;
    }

    /**
     * Returns the layout depth.
     *
     * @return the layout depth. If unset, defaults to <tt>0</tt>
     */
    public int getLayoutDepth() {
        return depth;
    }

    /**
     * Sets the layout depth.
     *
     * @param depth the depth
     */
    public void setLayoutDepth(int depth) {
        this.depth = depth;
    }

    /**
     * Marks an object as being rendered.
     *
     * @param object the rendered object
     */
    public void setRendered(IMObject object) {
        rendered.add(object.getObjectReference());
    }

    /**
     * Determines if a component has been created to display an object.
     *
     * @param object the object
     */
    public boolean isRendered(IMObject object) {
        return isRendered(object.getObjectReference());
    }

    /**
     * Determines if a component has been created to display an object.
     *
     * @param object the object
     */
    public boolean isRendered(IMObjectReference object) {
        boolean result = rendered.contains(object);
        if (!result && parent != null) {
            result = parent.isRendered(object);
        }
        return result;
    }

    /**
     * Returns an archetype descriptor for an object.
     *
     * @param object the object
     * @return an archetype descriptor for the object, or <tt>null</tt> if none can be found
     */
    public ArchetypeDescriptor getArchetypeDescriptor(IMObject object) {
        ArchetypeDescriptor result = null;
        if (parent != null) {
            result = parent.getArchetypeDescriptor(object);
        }
        return (result == null) ? DescriptorHelper.getArchetypeDescriptor(object) : result;
    }

    /**
     * Registers a listener for deletion events.
     *
     * @param listener the listener
     */
    public void setDeletionListener(IMObjectDeletionListener<IMObject> listener) {
        deletionListener = (listener != null) ? listener : DEFAULT_DELETION_LISTENER;
    }

    /**
     * Returns the deletion listener.
     *
     * @return the listener, or a default listener if none is registered
     */
    public IMObjectDeletionListener<IMObject> getDeletionListener() {
        return deletionListener;
    }

    /**
     * Registers a listener for context switch events.
     *
     * @param listener the listener. May be <tt>null</tt>
     */
    public void setContextSwitchListener(ContextSwitchListener listener) {
        contextSwitchListener = listener;
    }

    /**
     * Returns the context switch listener.
     *
     * @return the context switch listener, or <tt>null</tt> if none is registered
     */
    public ContextSwitchListener getContextSwitchListener() {
        return contextSwitchListener;
    }
}
