package org.openvpms.web.component.im.table;

import java.util.List;

import nextapp.echo2.app.Component;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.view.IMObjectReferenceViewer;


/**
 * Table model for {@link EntityRelationship}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class EntityRelationshipTableModel extends DefaultIMObjectTableModel {

    /**
     * Determines if hypelinks should be created for entities.
     */
    private final boolean _edit;

    /**
     * Construct a new <code>EntityRelationshipTableModel</code>.
     *
     * @param context layout context
     */
    public EntityRelationshipTableModel(LayoutContext context) {
        super(createTableColumnModel());
        _edit = context.isEdit();
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
     * Determines if selection should be enabled. This implementation returns
     * <code>true</code> if in edit mode.
     *
     * @return <code>true</code> if selection should be enabled; otherwise
     *         <code>false</code>
     */
    @Override
    public boolean getEnableSelection() {
        return _edit;
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

            if (relationship.getSource() != null
                && ref.equals(relationship.getSource())) {
                entity = relationship.getTarget();
            } else if (relationship.getTarget() != null
                       && ref.equals(relationship.getTarget())) {
                entity = relationship.getSource();
            } else {
                entity = relationship.getTarget();
            }
        }

        boolean hyperlink = !_edit; // disable hyperlinks when editing
        return new IMObjectReferenceViewer(entity, hyperlink).getComponent();
    }

}
