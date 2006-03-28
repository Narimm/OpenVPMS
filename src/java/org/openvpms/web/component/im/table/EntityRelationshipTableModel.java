package org.openvpms.web.component.im.table;

import java.util.List;

import nextapp.echo2.app.Component;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.util.IMObjectHelper;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;


/**
 * Table model for {@link EntityRelationship}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class EntityRelationshipTableModel extends DefaultIMObjectTableModel {

    /**
     * Construct a new <code>EntityRelationshipTableModel</code>.
     */
    public EntityRelationshipTableModel() {
        super(createTableColumnModel());
    }

    /**
     * Determines if this model can display a set of archetypes.
     *
     * @param archetypes the archetype descriptors
     * @return <code>true</ocde> if this model can display instances of
     *         <code>archetypes</code>; otherwise <code>false</code>
     */
    public static boolean canHandle(List<ArchetypeDescriptor> archetypes) {
        boolean result = false;
        String className = EntityRelationship.class.getName();
        for (ArchetypeDescriptor archetype : archetypes) {
            if (className.equals(archetype.getClassName())) {
                result = true;
            } else {
                result = false;
                break;
            }
        }
        return result;
    }

    /**
     * Determines if selection should be enabled.
     *
     * @return <code>true</code> if selection should be enabled; otherwise
     *         <code>false</code>
     */
    @Override
    public boolean getEnableSelection() {
        return false;
    }

    /**
     * Returns the value found at the given coordinate within the table.
     *
     * @param object the object
     * @param column
     * @param row    the table row
     */
    @Override
    protected Object getValue(IMObject object, int column, int row) {
        Object result;
        if (column == NAME_INDEX) {
            result = getEntity((EntityRelationship) object);
        } else {
            result = super.getValue(object, column, row);
        }
        return result;
    }

    /**
     * Returns the name of the entity in a relationship. This returns the
     * "non-current" or target side of the relationship. "Non-current" refers
     * the object that is NOT currently being viewed/edited. If the source and
     * target entities don't refer to the current object being viewed/edited,
     * then the target entity of the relationship is used.
     *
     * @param relationship the relationship
     * @return a viewer of the "non-current" entity of the relationship
     */
    protected Component getEntity(EntityRelationship relationship) {
        IMObjectReference entity = null;
        IMObject current = Context.getInstance().getCurrent();
        if (current == null) {
            entity = relationship.getTarget();
        } else {
            IMObjectReference ref = new IMObjectReference(current);
            if (ref.equals(relationship.getSource())) {
                entity = relationship.getTarget();
            } else if (ref.equals(relationship.getTarget())) {
                entity = relationship.getSource();
            }
        }
        return new IMObjectReferenceViewer(entity).getComponent();
    }

    /**
     * Returns an object given its reference.
     *
     * @param reference the object reference
     * @return the object corresponding to <code>reference</code> or
     *         <code>null</code> if none exists
     */
    protected IMObject getObject(IMObjectReference reference) {
        return IMObjectHelper.getObject(reference);
    }
}
