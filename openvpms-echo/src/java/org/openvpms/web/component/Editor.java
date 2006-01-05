package org.openvpms.web.component;

import nextapp.echo2.app.Component;

/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $Revision: 1.4 $ $Date: 2002/02/21 09:49:41 $
 */
public interface Editor {

    /**
     * Returns a title for the editor.

     * @return a title for the editor
     */
    String getTitle();

    /**
     * Returns the editing component.
     *
     * @return the editing component
     */
    public Component getComponent();

    /**
     * Returns the object being edited.
     *
     * @return the object being edited
     */
    public Object getObject();

    /**
     * Save any edits.
     */
    void save();

    /**
     * Delete the current object.
     */
    void delete();

    /**
     * Cancel any edits.
     */
    void cancel();
}
