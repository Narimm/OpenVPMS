package org.openvpms.web.component.edit;


/**
 * Interface to track the modified status of an object.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface Modifiable {

    /**
     * Determines if the object has been modified.
     *
     * @return <code>true</code> if the object has been modified
     */
    boolean isModified();

    /**
     * Clears the modified status of the object.
     */
    void clearModified();

}
