package org.openvpms.web.component.im.list;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.list.AbstractListComponent;
import nextapp.echo2.app.list.ListCellRenderer;

import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.web.resource.util.Messages;


/**
 * <code>ListCellRenderer</code> for a {@link LookupListModel}.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class LookupListCellRenderer implements ListCellRenderer {

    /**
     * Localised display name for "all".
     */
    private final String ALL = Messages.get("list.all");

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
        Object result = value;
        if (value == null) {
            // dummy lookup being rendered.
            AbstractListComponent box = (AbstractListComponent) list;
            LookupListModel model = (LookupListModel) box.getModel();
            Lookup lookup = model.getLookup(index);
            if (lookup.getArchetypeId() == null && lookup.getValue() == null) {
                // dummy lookup
                String code = lookup.getCode();
                if (LookupListModel.ALL.equals(code)) {
                    result = ALL;
                } else if (LookupListModel.NONE.equals(code)) {
                    result = NONE;
                }
            }
        }
        return result;
    }
}
