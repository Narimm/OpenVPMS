package org.openvpms.web.component.im.edit.act;

import java.util.ArrayList;
import java.util.List;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.web.component.im.create.IMObjectCreator;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.filter.BasicNodeFilter;
import org.openvpms.web.component.im.filter.ChainedNodeFilter;
import org.openvpms.web.component.im.filter.NamedNodeFilter;
import org.openvpms.web.component.im.layout.AbstractLayoutStrategy;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.IMObjectComponentFactory;
import org.openvpms.web.component.util.GridFactory;
import org.openvpms.web.component.edit.ModifiableListener;
import org.openvpms.web.component.edit.Modifiable;


/**
 * An editor for {@link Act}s which have an archetype of
 * <em>act.estimationItem</em> or <em>act.customerInvoiceItem</em>.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public abstract class ActItemEditor extends AbstractIMObjectEditor {

    /**
     * Participant editors.
     */
    private List<IMObjectEditor> _participants
            = new ArrayList<IMObjectEditor>();


    /**
     * Construct a new <code>ActItemEditor</code>.
     *
     * @param act        the act to edit
     * @param parent     the parent object. May be <code>null</code>
     * @param descriptor the parent descriptor. May be <code>null</cocde>
     * @param showAll    if <code>true</code> show optional and required fields;
     *                   otherwise show required fields.
     */
    public ActItemEditor(Act act, IMObject parent, NodeDescriptor descriptor,
                         boolean showAll) {
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
                    _participants.add(editor);

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

                }
            }
        }
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
     * Invoked when the participation product is changed, to update prices.
     *
     * @param participation the product participation instance
     */
    protected abstract void productModified(Participation participation);

    /**
     * Helper to return a product price from a product.
     *
     * @param shortName the price short name
     * @param product   the product
     * @return the price corresponding to  <code>shortName</code> or
     *         <code>null</code> if none exists
     */
    protected ProductPrice getPrice(String shortName, Product product) {
        return IMObjectHelper.getObject(shortName, product.getProductPrices());
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
     * Act item layout strategy.
     */
    private class LayoutStrategy extends AbstractLayoutStrategy {

        /**
         * Construct a new <code>LayoutStrategy</code>.
         *
         * @param showOptional if <code>true</code> show optional fields as well
         *                     as mandatory ones.
         */
        public LayoutStrategy(boolean showOptional) {
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
            container.add(grid);
        }

    }
}
