package org.openvpms.web.component.im.edit.act;

import java.util.ArrayList;
import java.util.List;

import nextapp.echo2.app.Component;
import nextapp.echo2.app.Grid;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.domain.im.product.Product;
import org.openvpms.component.business.domain.im.product.ProductPrice;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.edit.Modifiable;
import org.openvpms.web.component.edit.ModifiableListener;
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
     * Patient participation short name.
     */
    private static final String PATIENT_SHORTNAME = "participation.patient";

    /**
     * Product participation short name.
     */
    private static final String PRODUCT_SHORTNAME = "participation.product";

    /**
     * Author participation short name.
     */
    private static final String AUTHOR_SHORTNAME = "participation.author";

    /**
     * Participants node descriptor name.
     */
    private static final String PARTICIPANTS = "participants";


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

        NodeDescriptor participants = getDescriptor(PARTICIPANTS);

        for (String shortName : participants.getArchetypeRange()) {
            if (shortName.equals(PATIENT_SHORTNAME)) {
                addPatientEditor(act, participants);
            } else if (shortName.equals(PRODUCT_SHORTNAME)) {
                addProductEditor(act, participants);
            } else if (!shortName.equals(AUTHOR_SHORTNAME)) {
                Participation participant = IMObjectHelper.getObject(
                        shortName, act.getParticipations());
                if (participant == null) {
                    participant = (Participation) IMObjectCreator.create(
                            shortName);
                }
                addEditor(participant, act, participants);
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
     * Add a patient editor.
     *
     * @param act        the act
     * @param descriptor the participants node descriptor
     */
    private void addPatientEditor(Act act, NodeDescriptor descriptor) {
        Participation participant = getParticipation(PATIENT_SHORTNAME,
                                                     act);
        if (participant.isNew() && participant.getEntity() == null) {
            IMObject patient = Context.getInstance().getPatient();
            if (patient != null) {
                participant.setEntity(patient.getObjectReference());
            }
        }
        addEditor(participant, act, descriptor);
    }

    /**
     * Add a product editor.
     *
     * @param act        the act
     * @param descriptor the participants node descriptor
     */
    private void addProductEditor(Act act, NodeDescriptor descriptor) {
        final Participation participant
                = getParticipation(PRODUCT_SHORTNAME, act);
        if (participant.isNew()) {
            productModified(participant);
        }
        IMObjectEditor editor = addEditor(participant, act, descriptor);
        editor.addModifiableListener(new ModifiableListener() {
            public void modified(Modifiable modifiable) {
                productModified(participant);
            }
        });
    }

    /**
     * Add an editor for a participant.
     *
     * @param participant the participant
     * @param act         the parent act
     * @param descriptor  the participants node descriptor
     * @return the editor
     */
    private IMObjectEditor addEditor(Participation participant, Act act,
                                     NodeDescriptor descriptor) {
        final IMObjectEditor editor = ParticipationEditor.create(
                participant, act, descriptor, true);
        getModifiableSet().add(participant, editor);
        _participants.add(editor);
        return editor;
    }

    /**
     * Returns a participation instance for the supplied shortname, creating one
     * if needed.
     *
     * @param shortName the partcipant short name
     * @param act       the act
     * @return a participation from the act, or a new participation if none is
     *         present
     */
    private Participation getParticipation(String shortName, Act act) {
        Participation participant = IMObjectHelper.getObject(
                shortName, act.getParticipations());
        if (participant == null) {
            participant = (Participation) IMObjectCreator.create(
                    shortName);
        }
        return participant;
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
            filter.add(new NamedNodeFilter(PARTICIPANTS));
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
