package org.openvpms.web.component.im.view.act;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import nextapp.echo2.app.Component;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.query.PreloadedResultSet;
import org.openvpms.web.component.im.query.ResultSet;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.PagedIMObjectTable;
import org.openvpms.web.component.im.table.act.ActItemTableModel;
import org.openvpms.web.component.im.util.DescriptorHelper;
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
     * The act item archetype.
     */
    private final ArchetypeDescriptor _actDescriptor;

    /**
     * Construct a new <code>ActRelationshipTableLayoutStrategy</code>.
     *
     * @param descriptor the act relationship collection descriptor
     */
    public ActRelationshipTableLayoutStrategy(NodeDescriptor descriptor) {
        String relationshipType = descriptor.getArchetypeRange()[0];
        ArchetypeDescriptor relationship
                = DescriptorHelper.getArchetypeDescriptor(relationshipType);
        NodeDescriptor target = relationship.getNodeDescriptor("target");
        String itemType = target.getArchetypeRange()[0];
        _actDescriptor = DescriptorHelper.getArchetypeDescriptor(itemType);
    }

    /**
     * Apply the layout strategy.
     * <p/>
     * This renders an object in a <code>Component</code>, using a factory to
     * create the child components.
     *
     * @param object  the object to apply
     * @param context the layout context
     * @return the component containing the rendered <code>object</code>
     */
    public Component apply(IMObject object, LayoutContext context) {
        IMObjectTableModel model = new ActItemTableModel(_actDescriptor,
                                                         context);
        Act act = (Act) object;
        List<IMObject> acts = getActs(act);
        ResultSet set = new PreloadedResultSet<IMObject>(acts, 25);
        return new PagedIMObjectTable(model, set);
    }

    protected List<IMObject> getActs(Act act) {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        Set<ActRelationship> relationships = act.getSourceActRelationships();
        List<IMObject> result = new ArrayList<IMObject>();
        for (ActRelationship relationship : relationships) {
            if (relationship.getTarget() != null) {
                Act item = (Act) service.get(relationship.getTarget());
                if (item != null) {
                    result.add(item);
                }
            }
        }
        return result;
    }

}
