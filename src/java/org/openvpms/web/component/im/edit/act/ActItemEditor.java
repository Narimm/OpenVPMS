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
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.web.component.edit.Modifiable;
import org.openvpms.web.component.edit.ModifiableListener;
import org.openvpms.web.component.edit.Property;
import org.openvpms.web.component.im.create.IMObjectCreator;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.filter.BasicNodeFilter;
import org.openvpms.web.component.im.filter.ChainedNodeFilter;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.layout.ExpandableLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.util.IMObjectHelper;
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
     * Participant editors.
     */
    private List<IMObjectEditor> _participants = new ArrayList<IMObjectEditor>();


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
        NodeDescriptor participants = getDescriptor("participants");

        for (String shortName : participants.getArchetypeRange()) {
            if (!shortName.equals("participation.author")) {
                Participation participant = IMObjectHelper.getObject(
                        shortName, act.getParticipations());
                if (participant == null) {
                    participant = (Participation) IMObjectCreator.create(
                            shortName);
                }
                if (participant != null) {
                    final IMObjectEditor editor = ParticipationEditor.create(
                            participant, act, participants, showAll);
                    getModifiableSet().add(participant, editor);
                    if (shortName.equals("participation.product")) {
                        if (participant.isNew()) {
                            productModified(participant);
                        }
                        final Participation p = participant;
                        editor.addModifiableListener(new ModifiableListener() {
                            public void modified(Modifiable modifiable) {
                                productModified(p);
                            }
                        });
                    }
                    _participants.add(editor);
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


    private void productModified(Participation participation) {
        IMObjectReference entity = participation.getEntity();
        IMObject object = IMObjectHelper.getObject(entity);
        if (object != null && object instanceof Product) {
            Property fixedPrice = getProperty("fixedPrice");
            Property lowUnitPrice = getProperty("lowUnitPrice");
            Property highUnitPrice = getProperty("highUnitPrice");
            Product product = (Product) object;
            ProductPrice fixed = getPrice("productPrice.fixedPrice", product);
            ProductPrice unit = getPrice("productPrice.unitPrice", product);
            if (fixed != null) {
                fixedPrice.setValue(fixed.getPrice());
            }
            if (unit != null) {
                lowUnitPrice.setValue(unit.getPrice());
                highUnitPrice.setValue(unit.getPrice());
            }
        }
    }


    private ProductPrice getPrice(String shortName, Product product) {
        return IMObjectHelper.getObject(shortName, product.getProductPrices());
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
            Grid grid = GridFactory.create(4);
            for (IMObjectEditor editor : _participants) {
                add(grid, editor.getDisplayName(), editor.getComponent());
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
