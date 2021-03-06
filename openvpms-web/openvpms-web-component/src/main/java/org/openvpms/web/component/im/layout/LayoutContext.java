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

package org.openvpms.web.component.im.layout;

import org.openvpms.archetype.rules.prefs.Preferences;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.cache.IMObjectCache;
import org.openvpms.component.system.common.util.Variables;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.app.ContextSwitchListener;
import org.openvpms.web.component.im.delete.IMObjectDeletionListener;
import org.openvpms.web.component.im.filter.NodeFilter;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.mail.MailContext;
import org.openvpms.web.echo.help.HelpContext;


/**
 * Layout context.
 *
 * @author Tim Anderson
 */
public interface LayoutContext {

    /**
     * Returns the context.
     *
     * @return the context
     */
    Context getContext();

    /**
     * Returns the object cache.
     *
     * @return the object cache
     */
    IMObjectCache getCache();

    /**
     * Sets the object cache.
     *
     * @param cache the cache
     */
    void setCache(IMObjectCache cache);

    /**
     * Determines if this is an edit context.
     *
     * @return {@code true} if this is an edit context; {@code false} if it is a view context. Defaults to {@code false}
     */
    boolean isEdit();

    /**
     * Sets if this is an edit context.
     *
     * @param edit if {@code true} this is an edit context; if {@code false} it is a view context.
     */
    void setEdit(boolean edit);

    /**
     * Returns the component factory.
     *
     * @return the component factory
     */
    IMObjectComponentFactory getComponentFactory();

    /**
     * Sets the component factory.
     *
     * @param factory the component factory
     */
    void setComponentFactory(IMObjectComponentFactory factory);

    /**
     * Returns the default filter.
     *
     * @return the default filter. May be {@code null}
     */
    NodeFilter getDefaultNodeFilter();

    /**
     * Sets the default filter.
     *
     * @param filter the default filter. May be {@code null}
     */
    void setNodeFilter(NodeFilter filter);

    /**
     * Returns the layout strategy factory.
     *
     * @return the layout strategy factory
     */
    IMObjectLayoutStrategyFactory getLayoutStrategyFactory();

    /**
     * Sets the layout strategy factory.
     *
     * @param factory the layout strategy factory
     */
    void setLayoutStrategyFactory(IMObjectLayoutStrategyFactory factory);

    /**
     * Returns the layout depth.
     *
     * @return the layout depth. If unset, defaults to {@code 0}
     */
    int getLayoutDepth();

    /**
     * Sets the layout depth.
     *
     * @param depth the depth
     */
    void setLayoutDepth(int depth);

    /**
     * Marks an object as being rendered.
     *
     * @param object the rendered object
     */
    void setRendered(IMObject object);

    /**
     * Determines if a component has been created to display an object.
     *
     * @param object the object
     * @return <tt>true</tt> if a component has been rendered
     */
    boolean isRendered(IMObject object);

    /**
     * Determines if a component has been created to display an object.
     *
     * @param object the object
     * @return <tt>true</tt> if a component has been rendered
     */
    boolean isRendered(IMObjectReference object);

    /**
     * Returns an archetype descriptor for an object.
     *
     * @param object the object
     * @return an archetype descriptor for the object
     * @throws IllegalStateException if there is no archetype descriptor for the object
     */
    ArchetypeDescriptor getArchetypeDescriptor(IMObject object);

    /**
     * Registers a listener for deletion events.
     *
     * @param listener the listener
     */
    void setDeletionListener(IMObjectDeletionListener<IMObject> listener);

    /**
     * Returns the deletion listener.
     *
     * @return the listener, or a default listener if none is registered
     */
    IMObjectDeletionListener<IMObject> getDeletionListener();

    /**
     * Registers a mail context.
     *
     * @param context the mail context. May be <tt>null</tt>
     */
    void setMailContext(MailContext context);

    /**
     * Returns the mail context.
     *
     * @return the mail context. May be <tt>null</tt>
     */
    MailContext getMailContext();

    /**
     * Registers a listener for context switch events.
     *
     * @param listener the listener. May be <tt>null</tt>
     */
    void setContextSwitchListener(ContextSwitchListener listener);

    /**
     * Returns the context switch listener.
     *
     * @return the context switch listener, or <tt>null</tt> if none is registered
     */
    ContextSwitchListener getContextSwitchListener();

    /**
     * Returns the help context.
     *
     * @return the help context
     */
    HelpContext getHelpContext();

    /**
     * Returns variables for use in macro expansion.
     *
     * @return the variables
     */
    Variables getVariables();

    /**
     * Returns the user preferences.
     *
     * @return the user preferences
     */
    Preferences getPreferences();
}
