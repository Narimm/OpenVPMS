package org.openvpms.web.component.edit;

import nextapp.echo2.app.Component;

import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Enter description here.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate: 2005-12-05 22:57:22 +1100 (Mon, 05 Dec 2005) $
 */
public interface Editor {

    /**
     * Returns a title for the editor.
     *
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
    public IMObject getObject();

    /**
     * Create a new object.
     */
    void create();

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

}
