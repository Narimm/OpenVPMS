package org.openvpms.web.component.im.edit.act;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import nextapp.echo2.app.Label;
import nextapp.echo2.app.table.TableColumn;
import nextapp.echo2.app.table.TableColumnModel;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.util.LabelFactory;
import org.openvpms.web.component.im.edit.CollectionEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.edit.Saveable;
import org.openvpms.web.component.im.filter.BasicNodeFilter;
import org.openvpms.web.component.im.filter.ChainedNodeFilter;
import org.openvpms.web.component.im.filter.FilterHelper;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.table.DescriptorTableColumn;
import org.openvpms.web.component.im.table.DescriptorTableModel;
import org.openvpms.web.component.im.table.IMObjectTable;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.view.ReadOnlyComponentFactory;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Editor for collections of {@link ActRelationship}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ActRelationshipCollectionEditor extends CollectionEditor
        implements Saveable {

    /**
     * If <code>true</code> show optional and required fields; otherwise show
     * required fields.
     */
    private final boolean _showAll;

    /**
     * The set of acts being edited, and their associated relationships.
     */
    private Map<Act, ActRelationship> _acts;


    /**
     * Construct a new <code>CollectionEditor</code>.
     *
     * @param act        the parent act
     * @param descriptor the node descriptor
     * @param showAll    if <code>true</code> show optional and required fields;
     *                   otherwise show required fields.
     */
    public ActRelationshipCollectionEditor(Act act, NodeDescriptor descriptor,
                                           boolean showAll) {
        super(act, descriptor);
        _showAll = showAll;
    }

    /**
     * Save any edits.
     *
     * @param editor the editor managing the object to save
     * @return <code>true</code> if the save was successful
     */
    @Override
    protected boolean save(IMObjectEditor editor) {
        Act act = (Act) editor.getObject();
        boolean saved = false;
        if (super.save(editor)) {
            ActRelationship relationship = _acts.get(act);
            if (relationship.isNew()) {
                Act parent = (Act) getObject();
                IMObjectReference source = new IMObjectReference(parent);
                IMObjectReference target = new IMObjectReference(act);
                relationship.setSource(source);
                relationship.setTarget(target);
                NodeDescriptor descriptor = getDescriptor();
                if (SaveHelper.save(relationship, parent, descriptor)) {
                    saved = true;
                }
            } else {
                saved = true;
            }
        }
        return saved;
    }

    /**
     * Returns the list of objects to display in the table.
     *
     * @return the list objects to display.
     */
    @Override
    protected List<IMObject> getObjects() {
        IArchetypeService service = ServiceHelper.getArchetypeService();
        List<IMObject> relationships = super.getObjects();
        _acts = new HashMap<Act, ActRelationship>(relationships.size());
        for (IMObject object : relationships) {
            ActRelationship relationship = (ActRelationship) object;
            Act item = (Act) service.get(relationship.getTarget());
            _acts.put(item, relationship);
        }
        return new ArrayList<IMObject>(_acts.keySet());
    }

    /**
     * Create a new table.
     *
     * @param deletable determines if entries should be deleteable.
     * @return a new table
     */
    @Override
    protected IMObjectTable createTable(boolean deletable) {
        ArchetypeDescriptor descriptor = getDescriptorFromRange(getDescriptor());
        NodeDescriptor target = descriptor.getNodeDescriptor("target");
        ArchetypeDescriptor act = getDescriptorFromRange(target);
        NodeDescriptor participants = act.getNodeDescriptor("participants");

        ChainedNodeFilter filter = new ChainedNodeFilter();
        filter.add(new BasicNodeFilter(_showAll));
        filter.add(new NamedNodeFilter("participants"));
        List<NodeDescriptor> nodes = FilterHelper.filter(filter, act);
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
        return new IMObjectTable(model);
    }

    /**
     * Edit an object.
     *
     * @param object the object to edit
     */
    @Override
    protected void edit(final IMObject object) {
        if (object.isNew()) {
            ActRelationship relationship = (ActRelationship) object;
            IArchetypeService service = ServiceHelper.getArchetypeService();
            Act act = (Act) service.create("act.estimationItem");
            _acts.put(act, relationship);
            super.edit(act);
        } else {
            super.edit(object);
        }
    }

    /**
     * Creates a new editor.
     *
     * @param object the object to edit
     * @return an editor to edit <code>object</code>
     */
    @Override
    protected IMObjectEditor createEditor(IMObject object) {
        Act act = (Act) object;
        boolean showAll = !object.isNew();
        return IMObjectEditorFactory.create(act, showAll);
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

    /**
     * Helper to return the archetype descriptor from a node descriptor's
     * archetype range.
     *
     * @param descriptor the node descriptor
     * @return the archetype descriptor corresponding to the node descriptor's
     *         archetype range
     */
    private ArchetypeDescriptor getDescriptorFromRange(NodeDescriptor descriptor) {
        String[] range = descriptor.getArchetypeRange();
        if (range == null || range.length != 1) {
            throw new IllegalArgumentException("Unsuspported descriptor: " + descriptor);
        }
        return DescriptorHelper.getArchetypeDescriptor(range[0]);
    }

    private class ActRelationshipTableModel extends DescriptorTableModel {

        /**
         * Construct a <code>DescriptorTableModel</code>.
         */
        protected ActRelationshipTableModel(TableColumnModel model) {
            super(model, new ReadOnlyComponentFactory());
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
