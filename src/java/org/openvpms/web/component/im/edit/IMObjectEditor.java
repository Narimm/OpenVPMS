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

package org.openvpms.web.component.im.edit;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.edit.Cancellable;
import org.openvpms.web.component.edit.Deletable;
import org.openvpms.web.component.edit.Editor;
import org.openvpms.web.component.edit.Saveable;

import java.beans.PropertyChangeListener;


/**
 * Editor for {@link IMObject}s.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public interface IMObjectEditor extends Editor, Saveable, Deletable,
                                        Cancellable {

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
     * Returns the archetype descriptor of the object.
     *
     * @return the object's archetype descriptor
     */
    ArchetypeDescriptor getArchetypeDescriptor();

    /**
     * Deletes the current object.
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
     * Marks the current object inactive.
     */
    void deactivate();

    /**
     * Cancel any edits. Once complete, query methods may be invoked, but the
     * behaviour of other methods is undefined.
     */
    void cancel();

    /**
     * Determines if editing was cancelled.
     *
     * @return <code>true</code> if editing was cancelled
     */
    boolean isCancelled();

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
