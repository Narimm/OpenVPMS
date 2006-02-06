package org.openvpms.web.component.edit;

import java.beans.PropertyChangeListener;

import nextapp.echo2.app.Component;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Editor for {@link IMObject}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface IMObjectEditor {

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
     * Returns the object being edited.
     *
     * @return the object being edited
     */
    IMObject getObject();

    /**
     * Save any edits.
     *
     * @return <code>true</code> if the save was successful
     */
    boolean save();

    /**
     * Delete the current object.
     *
     * @return <code>true</code> if the object was deleted successfully
     */
    boolean delete();

    /**
     * Cancel any edits.
     */
    void cancel();

    /**
     * Determines if the object has been changed.
     *
     * @return <code>true</code> if the object has been changed
     */
    boolean isModified();

    /**
     * Determines if the object has been deleted.
     *
     * @return <code>true</code> if the object has been deleted
     */
    boolean isDeleted();

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

}
