package org.openvpms.web.component.subsystem;

import java.beans.PropertyChangeListener;

import nextapp.echo2.app.Component;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Manages the user interface for a set of related actions. Rach action
 * typically corresponds to a use case.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface Workspace {

    /**
     * The summary property name, used in event notification.
     */
    final String SUMMARY_PROPERTY = "summary";

    /**
     * Returns the localised title of this workspace.
     *
     * @return the localised title of this workspace
     */
    String getTitle();

    /**
     * Renders the workspace.
     *
     * @return the component representing the workspace
     */
    Component getComponent();

    /**
     * Renders the workspace summary.
     *
     * @return the component representing the workspace summary, or
     *         <code>null</code> if there is no summary
     */
    Component getSummary();

    /**
     * Determines if the workspace supports an archetype.
     *
     * @param shortName the archetype's short name
     * @return <code>true</code> if the workspace can handle the archetype;
     *         otherwise <code>false</code>
     */
    boolean canHandle(String shortName);

    /**
     * Sets the object to be viewed/edited by the workspace.
     *
     * @param object the object. May be <code>null</code>
     */
    void setObject(IMObject object);

    /**
     * Returns the object to to be viewed/edited by the workspace.
     *
     * @return the the object. May be <oode>null</code>
     */
    IMObject getObject();

    /**
     * Add a property change listener.
     *
     * @param name     the property name to listen on
     * @param listener the listener
     */
    void addPropertyChangeListener(String name,
                                   PropertyChangeListener listener);

    /**
     * Remove a property change listener.
     *
     * @param name     the property name to remove the listener for
     * @param listener the listener to remove
     */
    void removePropertyChangeListener(String name,
                                      PropertyChangeListener listener);

}
