package org.openvpms.web.component.edit;

import java.util.Collection;


/**
 * Collection property that provides notification on modification.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface CollectionProperty extends Property {

    /**
     * Add a value.
     *
     * @param value the value to add
     */
    void add(Object value);

    /**
     * Remove a value.
     *
     * @param value the value to remove
     */
    void remove(Object value);

    /**
     * Returns the collection.
     *
     * @return the collection
     */
    Collection getValues();
}
