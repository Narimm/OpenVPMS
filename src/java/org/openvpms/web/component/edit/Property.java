package org.openvpms.web.component.edit;


/**
 * Property that provides notification on modification.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public interface Property extends Modifiable {

    /**
     * Set the value of the property.
     *
     * @param value the property value
     */
    void setValue(Object value);

    /**
     * Returns the value of the property.
     *
     * @return the property value
     */
    Object getValue();

}
