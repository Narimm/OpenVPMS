package org.openvpms.web.component.im.view.act;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import nextapp.echo2.app.Column;
import nextapp.echo2.app.Component;
import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.ColumnFactory;
import org.openvpms.web.component.LabelFactory;
import org.openvpms.web.component.TableNavigator;
import org.openvpms.web.component.im.IMObjectComponentFactory;
import org.openvpms.web.component.im.filter.BasicNodeFilter;
import org.openvpms.web.component.im.filter.ChainedNodeFilter;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.DescriptorTableModel;
import org.openvpms.web.component.im.table.IMObjectTable;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.query.NodeBrowserFactory;
import org.openvpms.web.spring.ServiceHelper;
import org.openvpms.web.util.DescriptorHelper;


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
        Act act = (Act) object;
        ArchetypeDescriptor archetype
                = DescriptorHelper.getArchetypeDescriptor("act.estimationItem");

        NodeDescriptor participants = archetype.getNodeDescriptor("participants");
        ChainedNodeFilter filter = new ChainedNodeFilter();
        filter.add(new BasicNodeFilter(true));
        filter.add(new NamedNodeFilter("participants"));
        List<NodeDescriptor> nodes = FilterHelper.filter(filter, archetype);
        TableColumnModel columns = DescriptorTableModel.create(nodes);
        int index = columns.getColumnCount();
        String[] range = participants.getArchetypeRange();
        for (int i = 0; i < range.length; ++i, ++index) {
            String shortName = range[i];
            if (!shortName.equals("participation.author")) {
                columns.addColumn(new ParticipantTableColumn(shortName, index,
                        participants));
            }
        }

        IMObjectTableModel model = new ActRelationshipTableModel(columns);
        IMObjectTable table = new IMObjectTable(model);

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

    private class ActRelationshipTableModel extends DescriptorTableModel {

        /**
         * Construct a <code>DescriptorTableModel</code>.
         */
        protected ActRelationshipTableModel(TableColumnModel model) {
            super(model, new NodeBrowserFactory());
        }

        /**
         * Returns the value for the specified column.
         *
         * @param object the object
         * @param column the column
         */
        @Override
        protected Object getValue(IMObject object, TableColumn column) {
            Object result = null;
            if (column instanceof ParticipantTableColumn) {
                ParticipantTableColumn col = (ParticipantTableColumn) column;
                NodeDescriptor descriptor = col.getDescriptor();
                IMObject child = getByShortName(col.getShortName(), descriptor.getChildren(object));
                if (child != null) {
                    ArchetypeDescriptor archetype = DescriptorHelper.getArchetypeDescriptor(child);
                    NodeDescriptor entity = archetype.getNodeDescriptor("entity");
                    result = getFactory().create(child, entity);
                } else {
                    Label label = LabelFactory.create();
                    label.setText("No " + col.getHeaderValue());
                    result = label;
                }
            } else {
                result = super.getValue(object, column);
            }
            return result;
        }

        /**
         * Returns the first object that has a matching short name.
         *
         * @param shortName the short name to match on
         * @param objects   the objects to search
         * @return the first object with a short name the same as
         *         <code>shortName</code> or <code>null</code> if none exists
         */
        private IMObject getByShortName(String shortName, List<IMObject> objects) {
            IMObject result = null;
            for (IMObject object : objects) {
                if (object.getArchetypeId().getShortName().equals(shortName)) {
                    result = object;
                    break;
                }
            }
            return result;
        }

    }

    private static class ParticipantTableColumn extends DescriptorTableColumn {

        private final String _shortName;
        private final String _displayName;

        /**
         * Creates a <code>TableColumn</code> with the specified model index,
         * undefined width, and undefined cell and header renderers.
         *
         * @param modelIndex the column index of model data visualized by this
         *                   column
         */
        public ParticipantTableColumn(String shortName, int modelIndex, NodeDescriptor descriptor) {
            super(modelIndex, descriptor);
            _shortName = shortName;
            _displayName = DescriptorHelper.getDisplayName(shortName);
        }

        /**
         * Returns the header value for this column.  The header value is the
         * object that will be provided to the header renderer to produce a
         * component that will be used as the table header for this column.
         *
         * @return the header value for this column
         */
        @Override
        public Object getHeaderValue() {
            return _displayName;
        }

        public String getShortName() {
            return _shortName;
        }
    }

}
