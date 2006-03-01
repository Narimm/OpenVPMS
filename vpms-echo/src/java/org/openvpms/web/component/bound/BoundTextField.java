package org.openvpms.web.component.bound;

import nextapp.echo2.app.Extent;
import nextapp.echo2.app.TextField;

import org.openvpms.web.component.edit.Property;


/**
 * Binds a {@link Property} to a <code>TextField</code>.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class BoundTextField extends TextField {

    /**
     * Construct a new <code>BoundTextField</code>.
     *
     * @param property the property to bind
     * @param columns  the no. of columns to display
     */
    public BoundTextField(Property property, int columns) {
        setWidth(new Extent(columns, Extent.EX));

        Binder binder = new TextComponentBinder(this, property);
        binder.setField();
    }

}
