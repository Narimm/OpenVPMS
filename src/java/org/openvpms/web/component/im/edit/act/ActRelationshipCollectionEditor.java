package org.openvpms.web.component.im.edit.act;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.edit.Saveable;
import org.openvpms.web.component.im.edit.CollectionEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.component.im.layout.DefaultLayoutContext;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.table.IMObjectTable;
import org.openvpms.web.component.im.table.IMObjectTableModel;
import org.openvpms.web.component.im.table.act.ActItemTableModel;
import org.openvpms.web.component.im.util.DescriptorHelper;
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
     * @param context the layout context
     * @return a new table
     */
    @Override
    protected IMObjectTable createTable(LayoutContext context) {
        DefaultLayoutContext readOnly = new DefaultLayoutContext(context);
        readOnly.setComponentFactory(new TableComponentFactory());
        IMObjectTableModel model = new ActItemTableModel(
                _actItemDescriptor, readOnly);
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

}
