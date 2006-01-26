package org.openvpms.web.component.bound;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.apache.commons.jxpath.Pointer;

import org.openvpms.web.component.palette.Palette;


/**
 * Binds a <code>Pointer</code> to a <code>Palette</code>.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class BoundPalette extends Palette {

    /**
     * The bound field.
     */
    private Pointer _pointer;

    /**
     * Construct a new <code>BoundPalette</coce>.
     *
     * @param items
     * @param pointer the bound field
     */
    public BoundPalette(List items, Pointer pointer) {
        super(items, new ArrayList((Collection) pointer.getValue()));
        _pointer = pointer;
    }

    @Override
    protected void add(Object[] values) {
        Collection collection = (Collection) _pointer.getValue();
        collection.addAll(Arrays.asList(values));
    }

    @Override
    protected void remove(Object[] values) {
        Collection collection = (Collection) _pointer.getValue();
        collection.removeAll(Arrays.asList(values));
    }
}
