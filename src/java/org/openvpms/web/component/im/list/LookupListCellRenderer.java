package org.openvpms.web.component.im.list;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.list.ListCellRenderer;

import org.openvpms.web.resource.util.Messages;


/**
 * <code>ListCellRenderer</code> for a {@link LookupListModel}.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class LookupListCellRenderer implements ListCellRenderer {

    /**
     * Localised display name for "none".
     */
    private final String NONE = Messages.get("list.none");


    /**
     * Renders an item in a list.
     *
     * @param list  the list component
     * @param value the item value
     * @param index the item index
     * @return the rendered form of the list cell
     */
    public Object getListCellRendererComponent(Component list, Object value,
                                               int index) {
        String result;
        if (value instanceof String) {
            result = (String) value;
        } else {
            result = NONE;
        }
        return result;
    }
}
