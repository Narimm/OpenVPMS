package org.openvpms.web.component.edit;


/**
 * Interface to track the saved status of an object.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface Saveable extends Modifiable {

    /**
     * Save any edits.
     *
     * @return <code>true</code> if the save was successful
     */
    boolean save();

    /**
     * Determines if any edits have been saved.
     *
     * @return <code>true</code> if edits have been saved.
     */
    boolean isSaved();

}
