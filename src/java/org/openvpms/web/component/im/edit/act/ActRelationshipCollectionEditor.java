package org.openvpms.web.component.im.edit.act;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.ActRelationship;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.edit.Saveable;
import org.openvpms.web.component.im.edit.CollectionEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditorFactory;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.act.ActItemTableModel;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.component.im.util.IMObjectCopier;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.ReadOnlyComponentFactory;
import org.openvpms.web.component.im.view.TableComponentFactory;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Editor for collections of {@link ActRelationship}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate:2006-02-21 03:48:29Z $
 */
public class ActRelationshipCollectionEditor extends CollectionEditor
        implements Saveable {

    /**
     * The set of acts being edited, and their associated relationships.
     */
    private Map<Act, ActRelationship> _acts;

    /**
     * The act item archetype.
     */
    private final ArchetypeDescriptor _actItemDescriptor;


    /**
     * Construct a new <code>ActRelationshipCollectionEditor</code>.
     *
     * @param act        the parent act
     * @param descriptor the node descriptor
     * @param context    the layout context
     */
    public ActRelationshipCollectionEditor(Act act, NodeDescriptor descriptor,
                                           LayoutContext context) {
        super(act, descriptor, context);
        String relationshipType = descriptor.getArchetypeRange()[0];
        ArchetypeDescriptor relationship
                = DescriptorHelper.getArchetypeDescriptor(relationshipType);
        NodeDescriptor target = relationship.getNodeDescriptor("target");
        String itemType = target.getArchetypeRange()[0];
        _actItemDescriptor = DescriptorHelper.getArchetypeDescriptor(itemType);
    }

    /**
     * Returns the set of acts being edited.
     *
     * @return the set of acts being edited.
     */
    public Set<Act> getActs() {
        return _acts.keySet();
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
        IMObjectReference product = ((ActItemEditor) editor).getProduct();
        if (IMObjectHelper.isA(product, "product.template")) {
            saved = expandTemplate(act, product);
        } else if (super.save(editor)) {
            ActRelationship relationship = _acts.get(act);
            if (relationship.isNew()) {
                saved = saveRelationship(relationship, act);
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
     * Create a new table model.
     *
     * @param context the layout context
     * @return a new table model
     */
    @Override
    protected IMObjectTableModel createTableModel(LayoutContext context) {
        DefaultLayoutContext readOnly = new DefaultLayoutContext(context);
        readOnly.setComponentFactory(new TableComponentFactory(context));
        return new ActItemTableModel(_actItemDescriptor, readOnly);
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
            Act act = (Act) service.create(_actItemDescriptor.getShortName());
            _acts.put(act, relationship);
            super.edit(act);
        } else {
            super.edit(object);
        }
    }

    /**
     * Remove an object from the collection.
     *
     * @param object the object to remove
     */
    @Override
    protected void removeFromCollection(IMObject object) {
        NodeDescriptor descriptor = getDescriptor();
        IMObject parent = getObject();
        Act act = (Act) object;
        ActRelationship relationship = _acts.remove(act);
        descriptor.removeChildFromCollection(parent, relationship);
    }

    /**
     * Creates a new editor.
     *
     * @param object  the object to edit
     * @param context the layout context
     * @return an editor to edit <code>object</code>
     */
    @Override
    protected IMObjectEditor createEditor(IMObject object,
                                          LayoutContext context) {
        return IMObjectEditorFactory.create(object, context);
    }

    /**
     * Determines if an act contains a product template.
     *
     * @param act the act
     * @return <code>true</code> if the act contains a product template,
     *         otherwise <code>false</code>
     */
    protected IMObjectReference getProductTemplate(Act act) {
        Participation participant = IMObjectHelper.getObject(
                "participation.product", act.getParticipations());
        if (participant != null) {
            IMObjectReference ref = participant.getEntity();
            if (IMObjectHelper.isA(ref, "product.template")) {
                return ref;
            }
        }
        return null;
    }

    /**
     * Copies an act item for each product referred to in its template.
     *
     * @param act         the act
     * @param templateRef a reference to the template
     */
    protected boolean expandTemplate(Act act, IMObjectReference templateRef) {
        boolean saved = false;
        IArchetypeService service = ServiceHelper.getArchetypeService();
        IMObject template = IMObjectHelper.getObject(templateRef);
        if (template != null) {
            // need to remove the relationship as a new relationship is
            // created for each child act
            ActRelationship actRelationship = _acts.remove(act);
            String shortName = actRelationship.getArchetypeId().getShortName();

            IMObjectCopier copier = new IMObjectCopier(new ActItemCopyHandler());
            Collection values = IMObjectHelper.getValues(template, "includes");
            for (Object value : values) {
                EntityRelationship relationship = (EntityRelationship) value;
                IMObjectReference product = relationship.getTarget();

                // copy the act, and associate the product
                Act copy = (Act) copier.copy(act);
                LayoutContext context = new DefaultLayoutContext();
                context.setComponentFactory(new ReadOnlyComponentFactory(context));
                ActItemEditor editor = (ActItemEditor) createEditor(
                        copy, context);
                editor.setProduct(product);

                Object quantity = IMObjectHelper.getValue(relationship,
                                                          "includeQty");
                if (quantity != null) {
                    editor.setQuantity((BigDecimal) quantity);
                }

                // create a new act relationship linking the copied act with
                // the parent act
                ActRelationship relationshipCopy
                        = (ActRelationship) service.create(shortName);
                if (saveRelationship(relationshipCopy, copy)) {
                    // register and save the act
                    _acts.put(copy, relationshipCopy);
                    saved = editor.save();
                }
            }
        }
        return saved;
    }

    /**
     * Save an act relationsip.
     *
     * @param act the act relationship
     * @param act the child act
     * @return <code>true</code> if the save was successful
     */
    private boolean saveRelationship(ActRelationship relationship, Act act) {
        Act parent = (Act) getObject();
        relationship.setSource(parent.getObjectReference());
        relationship.setTarget(act.getObjectReference());
        NodeDescriptor descriptor = getDescriptor();
        return SaveHelper.save(relationship, parent, descriptor);
    }

    private class ActItemCopyHandler extends ActCopyHandler {

        /**
         * Determines how {@link IMObjectCopier} should treat an object.
         *
         * @param object  the source object
         * @param service the archetype service
         * @return <code>object</code> if the object shouldn't be copied,
         *         <code>null</code> if it should be replaced with
         *         <code>null</code>, or a new instance if the object should be
         *         copied
         */
        public IMObject getObject(IMObject object, IArchetypeService service) {
            IMObject result;
            if (object instanceof Participation) {
                Participation participant = (Participation) object;
                if (IMObjectHelper.isA(participant.getEntity(),
                                       "product.template")) {
                    result = null;
                } else {
                    result = super.getObject(object, service);
                }
            } else {
                result = super.getObject(object, service);
            }
            return result;
        }
    }
}
