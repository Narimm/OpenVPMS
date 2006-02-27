package org.openvpms.web.component.im.edit;

import java.beans.PropertyChangeListener;

import nextapp.echo2.app.Component;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.Saveable;


/**
 * Editor for {@link IMObject}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface IMObjectEditor extends Saveable {

    /**
     * Property name for event indicating that the component has changed.
     */
    final String COMPONENT_CHANGED_PROPERTY = "component";

    /**
     * Returns a title for the editor.
     *
     * @return a title for the editor
     */
    String getTitle();

    /**
     * Returns a display name for the object being edited.
     *
     * @return a display name for the object
     */
    String getDisplayName();

    /**
     * Returns the object being edited.
     *
     * @return the object being edited
     */
    IMObject getObject();

    /**
     * Delete the current object.
     *
     * @return <code>true</code> if the object was deleted successfully
     */
    boolean delete();

    /**
     * Determines if the object has been deleted.
     *
     * @return <code>true</code> if the object has been deleted
     */
    boolean isDeleted();

    /**
     * Cancel any edits. Once complete, query methods may be invoked, but the
     * behaviour of other methods is undefined..
     */
    void cancel();

    /**
     * Determines if editing was cancelled.
     *
     * @return <code>true</code> if editing was cancelled
     */
    boolean isCancelled();

    /**
     * Returns the rendered object.
     *
     * @return the rendered object
     */
    Component getComponent();


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
