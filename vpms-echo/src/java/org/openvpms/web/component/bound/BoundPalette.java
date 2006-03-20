package org.openvpms.web.component.bound;

import java.util.ArrayList;
import java.util.List;

import org.openvpms.web.component.edit.CollectionProperty;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.palette.Palette;


/**
 * Binds a {@link Property} to a <code>Palette</code>.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class BoundPalette extends Palette {

    /**
     * The bound property.
     */
    private CollectionProperty _property;


    /**
     * Construct a new <code>BoundPalette</coce>.
     *
     * @param items    all items that may be selected
     * @param property the property to bind
     */
    public BoundPalette(List items, CollectionProperty property) {
        super(items, new ArrayList(property.getValues()));
        _property = property;
    }

    /**
     * Add items to the 'selected' list.
     *
     * @param values the values to add.
     */
    @Override
    protected void add(Object[] values) {
        for (Object value : values) {
            _property.add(value);
        }
    }

    /**
     * Remove items from the 'selected' list.
     *
     * @param values the values to remove
     */
    @Override
    protected void remove(Object[] values) {
        for (Object value : values) {
            _property.remove(value);
        }
    }
}
