package org.openvpms.web.component.util;

import java.util.List;

import nextapp.echo2.app.SelectField;
import nextapp.echo2.app.list.DefaultListModel;
import nextapp.echo2.app.list.ListModel;
import org.apache.commons.jxpath.Pointer;

import org.openvpms.web.component.bound.BoundSelectField;
import org.openvpms.web.component.util.ComponentFactory;


/**
 * Factory for {@link SelectField}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public final class SelectFieldFactory extends ComponentFactory {

    /**
     * Creates a new select field with the provided model.
     *
     * @param model the model
     */
    public static SelectField create(ListModel model) {
        SelectField select = new SelectField(model);
        setDefaults(select);
        if (model.size() > 0) {
            // default to the first element
            select.setSelectedIndex(0);
        }
        return select;
    }

    /**
     * Creates a new bound select field.
     *
     * @param pointer a pointer to the field to update
     * @param model   the model
     */
    public static SelectField create(Pointer pointer, ListModel model) {
        SelectField select = new BoundSelectField(pointer, model);
        setDefaults(select);
        return select;
    }

    /**
     * Create a new select field that will initially contain the provided list
     * of items.
     *
     * @param items the items to add
     * @return a new select field
     */
    public static SelectField create(List items) {
        return create(items.toArray());
    }

    /**
     * Create a new select field that will initially contain the provided array
     * of items.
     *
     * @param items the items to add
     * @return a new select field
     */
    public static SelectField create(Object[] items) {
        return create(new DefaultListModel(items));
    }

}
