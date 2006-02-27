package org.openvpms.web.component.im.edit.act;

import java.util.ArrayList;
import java.util.List;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;
import nextapp.echo2.app.Row;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.create.IMObjectCreator;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.filter.BasicNodeFilter;
import org.openvpms.web.component.im.filter.ChainedNodeFilter;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.layout.ExpandableLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.util.RowFactory;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.estimationItem</em>.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class ActItemEditor extends AbstractIMObjectEditor {

    /**
     * Participants.
     */
    private List<IMObject> _participants = new ArrayList<IMObject>();


    /**
     * Construct a new <code>ActEditor</code>.
     *
     * @param act        the act to edit
     * @param parent     the parent object. May be <code>null</code>
     * @param descriptor the parent descriptor. May be <code>null</cocde>
     * @param showAll    if <code>true</code> show optional and required fields;
     *                   otherwise show required fields.
     */
    protected ActItemEditor(Act act, IMObject parent,
                            NodeDescriptor descriptor, boolean showAll) {
        super(act, parent, descriptor, showAll);
        ArchetypeDescriptor archetype = getArchetypeDescriptor();
        NodeDescriptor participants = archetype.getNodeDescriptor("participants");

        for (String shortName : participants.getArchetypeRange()) {
            if (!shortName.equals("participation.author")) {
                IMObject participant = getParticipant(shortName,
                        participants.getChildren(act));
                if (participant == null) {
                    participant = IMObjectCreator.create(shortName);
                }
                if (participant != null) {
                    _participants.add(participant);
                }
            }
        }
    }

    /**
     * Create a new editor for an object, if it can be edited by this class.
     *
     * @param object     the object to edit
     * @param parent     the parent object. May be <code>null</code>
     * @param descriptor the parent descriptor. May be <code>null</cocde>
     * @param showAll    if <code>true</code> show optional and required fields;
     *                   otherwise show required fields.
     * @return a new editor for <code>object</code>, or <code>null</code> if it
     *         cannot be edited by this
     */
    public static IMObjectEditor create(IMObject object, IMObject parent,
                                        NodeDescriptor descriptor,
                                        boolean showAll) {
        IMObjectEditor result = null;
        if (object instanceof Act) {
            ArchetypeDescriptor archetype
                    = DescriptorHelper.getArchetypeDescriptor(object);
            if (archetype != null
                    && archetype.getShortName().equals("act.estimationItem")) {
                NodeDescriptor participants = archetype.getNodeDescriptor("participants");
                if (participants != null) {
                    result = new ActItemEditor((Act) object, parent, descriptor, showAll);
                }
            }
        }
        return result;
    }

    /**
     * Save any edits.
     *
     * @return <code>true</code> if the save was successful
     */
    @Override
    protected boolean doSave() {
        boolean saved = false;
        if (saveObject()) {
            saved = saveChildren();
        }
        return saved;
    }

    /**
     * Creates the layout strategy.
     *
     * @param showAll if <code>true</code> show required and optional fields;
     *                otherwise show required fields.
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy(boolean showAll) {
        return new LayoutStrategy(showAll);
    }

    /**
     * Returns the first object from list with matching short name.
     *
     * @param shortName the short name
     * @param objects   the objects to search
     * @return the first object from list with matching short name, or
     *         <code>null</code> if none exists.
     */
    private IMObject getParticipant(String shortName, List<IMObject> objects) {
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
     * Act item layout strategy.
     */
    private class LayoutStrategy extends ExpandableLayoutStrategy {

        /**
         * Construct a new <code>LayoutStrategy</code>.
         *
         * @param showOptional if <code>true</code> show optional fields as well
         *                     as mandatory ones.
         */
        public LayoutStrategy(boolean showOptional) {
            super(showOptional);
            ChainedNodeFilter filter = new ChainedNodeFilter();
            filter.add(new BasicNodeFilter(showOptional, false));
            filter.add(new NamedNodeFilter("participants"));
            setNodeFilter(filter);
        }

        /**
         * Lays out child components in a 2x2 grid.
         *
         * @param object      the parent object
         * @param descriptors the child descriptors
         * @param container   the container to use
         * @param factory     the component factory
         */
        @Override
        protected void doSimpleLayout(IMObject object,
                                      List<NodeDescriptor> descriptors,
                                      Component container,
                                      IMObjectComponentFactory factory) {
            NodeDescriptor participants
                    = getArchetypeDescriptor().getNodeDescriptor("participants");
            Grid grid = GridFactory.create(4);
            for (IMObject participant : _participants) {
                String displayName = DescriptorHelper.getDisplayName(participant);
                Component component = factory.create(participant, getObject(), participants);
                add(grid, displayName, component);
            }
            for (NodeDescriptor descriptor : descriptors) {
                Component child = factory.create(object, descriptor);
                add(grid, descriptor.getDisplayName(), child);
            }
            if (showButton()) {
                Row group = RowFactory.create(grid, getButtonRow());
                container.add(group);
            } else {
                container.add(grid);
            }
        }

    }

}
