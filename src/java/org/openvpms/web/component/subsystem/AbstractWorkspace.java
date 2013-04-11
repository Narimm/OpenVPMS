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
 */

package org.openvpms.web.component.subsystem;

import echopointng.KeyStrokeListener;
import echopointng.KeyStrokes;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.event.ActionEvent;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.dialog.HelpDialog;
import org.openvpms.web.component.event.ActionListener;
import org.openvpms.web.component.help.HelpContext;
import org.openvpms.web.component.help.HelpListener;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.mail.MailContext;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;


/**
 * Abstract implementation of the {@link Workspace} interface.
 *
 * @author Tim Anderson
 */
public abstract class AbstractWorkspace<T extends IMObject>
        implements Workspace<T> {

    /**
     * The current object. May be {@code null}.
     */
    private T object;

    /**
     * The workspace component.
     */
    private Component component;

    /**
     * The subsystem localistion id.
     */
    private final String subsystemId;

    /**
     * The workspace localisation id.
     */
    private final String workspaceId;

    /**
     * Property change listener notifier.
     */
    private PropertyChangeSupport propertyChangeNotifier;

    /**
     * The email context.
     */
    private MailContext context;

    /**
     * The help context.
     */
    private HelpContext help;


    /**
     * Constructs an {@code AbstractWorkspace}.
     *
     * @param subsystemId the subsystem localisation identifier
     * @param workspaceId the workspace localisation identifier
     */
    public AbstractWorkspace(String subsystemId, String workspaceId) {
        this.subsystemId = subsystemId;
        this.workspaceId = workspaceId;
    }

    /**
     * Returns the resource bundle key for the workspace title.
     * The corresponding title may contain keyboard shortcuts.
     *
     * @return the resource bundle key the workspace title
     */
    public String getTitleKey() {
        return "workspace." + subsystemId + "." + workspaceId;
    }

    /**
     * Renders the workspace.
     *
     * @return the component representing the workspace
     */
    public Component getComponent() {
        if (component == null || refreshWorkspace()) {
            component = doLayout();
        }
        return component;
    }

    /**
     * Invoked when the workspace is displayed.
     * <p/>
     * This implementation is a no-op.
     */
    public void show() {
    }

    /**
     * Invoked when the workspace is hidden.
     * <p/>
     * This implementation is a no-op
     */
    public void hide() {
    }

    /**
     * Renders the workspace summary.
     *
     * @return the component representing the workspace summary, or
     *         {@code null} if there is no summary
     */
    public Component getSummary() {
        return null;
    }

    /**
     * Sets the object to be viewed/edited by the workspace.
     *
     * @param object the object. May be {@code null}
     */
    public void setObject(T object) {
        this.object = object;
    }

    /**
     * Returns the object to to be viewed/edited by the workspace.
     *
     * @return the the object. May be <oode>null}
     */
    public T getObject() {
        return object;
    }

    /**
     * Determines if the workspace can be updated with instances of the specified archetype.
     *
     * @param shortName the archetype's short name
     * @return {@code false}
     */
    public boolean canUpdate(String shortName) {
        return false;
    }

    /**
     * Sets the current object.
     * <p/>
     * This is analagous to {@link #setObject} but performs a safe cast
     * to the required type.
     * <p/>
     * If the current object is the same instance as that supplied, no changes will be made.
     *
     * @param object the current object. May be {@code null}
     */
    public void setIMObject(IMObject object) {
        Class<T> type = getType();
        if (object == getObject()) {
            // no-op
        } else if (object == null || type.isAssignableFrom(object.getClass())) {
            setObject(type.cast(object));
        } else {
            throw new IllegalArgumentException("Argument 'object' must be an instance of " + type.getName());
        }
    }

    /**
     * Updates the workspace with the specified object.
     * <p/>
     * This implementation delegates to {@link #setIMObject}.
     *
     * @param object the object to update the workspace with
     */
    public void update(IMObject object) {
        setIMObject(object);
    }

    /**
     * Sets the mail context.
     *
     * @param context the mail context. May be {@code null}
     */
    public void setMailContext(MailContext context) {
        this.context = context;
    }

    /**
     * Returns the mail context.
     *
     * @return the mail context. May be {@code null}
     */
    public MailContext getMailContext() {
        return context;
    }

    /**
     * Returns the help context.
     *
     * @return the help context
     */
    public HelpContext getHelpContext() {
        if (help == null) {
            help = new HelpContext(getHelpTopic(), new HelpListener() {
                public void show(HelpContext context) {
                    HelpDialog.show(context);
                }
            });
        }
        return help;
    }

    /**
     * Add a property change listener.
     *
     * @param name     the property name to listen on
     * @param listener the listener
     */
    public void addPropertyChangeListener(String name,
                                          PropertyChangeListener listener) {
        if (propertyChangeNotifier == null) {
            propertyChangeNotifier = new PropertyChangeSupport(this);
        }
        propertyChangeNotifier.addPropertyChangeListener(name, listener);
    }

    /**
     * Remove a property change listener.
     *
     * @param name     the property name to remove the listener for
     * @param listener the listener to remove
     */
    public void removePropertyChangeListener(String name,
                                             PropertyChangeListener listener) {
        if (propertyChangeNotifier != null) {
            propertyChangeNotifier.removePropertyChangeListener(
                    name, listener);
        }
    }

    /**
     * Report a bound property update to any registered listeners. No event is
     * fired if old and new are equal and non-null.
     *
     * @param name     the name of the property that was changed
     * @param oldValue the old value of the property
     * @param newValue the new value of the property
     */
    protected void firePropertyChange(String name, Object oldValue,
                                      Object newValue) {
        if (propertyChangeNotifier != null) {
            propertyChangeNotifier.firePropertyChange(name, oldValue,
                                                      newValue);
        }
    }

    /**
     * Returns the class type that this operates on.
     *
     * @return the class type that this operates on
     */
    protected abstract Class<T> getType();

    /**
     * Lays out the component.
     *
     * @return the component
     */
    protected Component doLayout() {
        Component heading = Heading.getHeading(subsystemId, workspaceId);
        KeyStrokeListener listener = new KeyStrokeListener();
        listener.addKeyCombination(KeyStrokes.VK_F1);
        listener.addActionListener(new ActionListener() {
            @Override
            public void onAction(ActionEvent event) {
                onHelp();
            }
        });
        heading.add(listener);
        return heading;
    }

    /**
     * Returns the subsystem localisation identifier.
     *
     * @return the subsystem localisation id.
     */
    protected String getSubsystemId() {
        return subsystemId;
    }

    /**
     * Returns the workspace localisation identifier.
     *
     * @return the workspace localisation id
     */
    protected String getWorkspaceId() {
        return workspaceId;
    }

    /**
     * Determines if the workspace should be refreshed.
     *
     * @return {@code true} if a later version of {@link #getObject()}
     *         exists, or it has been deleted
     */
    protected boolean refreshWorkspace() {
        return getLatest() != getObject();
    }

    /**
     * Returns the latest version of the current context object.
     *
     * @return the latest version of the context object, or {@link #getObject()}
     *         if they are the same, or {@code null} if the context object is
     *         not supported by the workspace
     */
    protected T getLatest() {
        return getLatest(getObject());
    }

    /**
     * Helper to return the latest version of an object.
     *
     * @param context the current context object
     * @return the latest version of the context object, or {@link #getObject()}
     *         if they are the same, or {@code null} if the context object is
     *         not supported by the workspace
     */
    protected T getLatest(T context) {
        context = IMObjectHelper.reload(context);
        if (!IMObjectHelper.isSame(getObject(), context)) {
            if (context != null && !canUpdate(context.getArchetypeId().getShortName())) {
                return null;
            }
            return context;
        }
        return getObject();
    }

    /**
     * Launches help for the workspace.
     */
    protected void onHelp() {
        getHelpContext().show();
    }

    /**
     * Returns the help topic for this workspace.
     *
     * @return the help topic
     */
    protected String getHelpTopic() {
        return "workspace/" + subsystemId + "/" + workspaceId;
    }

}
