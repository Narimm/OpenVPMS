package org.openvpms.web.component.im.view.act;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.table.IMObjectTable;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.act.ActItemTableModel;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.table.TableNavigator;
import org.openvpms.web.component.util.ColumnFactory;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Layout strategy that displays a collection of {@link ActRelationship}s in a
 * table.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ActRelationshipTableLayoutStrategy implements IMObjectLayoutStrategy {

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a <code>Component</code>, using a factory to
     * create the child components.
     *
     * @param object  the object to apply
     * @param factory the component factory
     * @return the component containing the rendered <code>object</code>
     */
    public Component apply(IMObject object, IMObjectComponentFactory factory) {
        IMObjectTableModel model
                = ActItemTableModel.create(factory, false, true);
        IMObjectTable table = new IMObjectTable(model);

        Act act = (Act) object;
        List<IMObject> acts = getActs(act);
        table.setObjects(acts);

        Column container = ColumnFactory.create();

        int size = acts.size();
        if (size != 0) {
            int rowsPerPage = table.getRowsPerPage();
            if (size > rowsPerPage) {
                TableNavigator navigator = new TableNavigator(table);
                container.add(navigator);
            }
        }
        container.add(table);
        return container;
    }

    protected List<IMObject> getActs(Act act) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        Set<ActRelationship> relationships = act.getSourceActRelationships();
        List<IMObject> result = new ArrayList<IMObject>(relationships.size());
        for (ActRelationship relationship : relationships) {
            Act item = (Act) service.get(relationship.getTarget());
            result.add(item);
        }
        return result;
    }

}
