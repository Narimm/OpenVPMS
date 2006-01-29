package org.openvpms.web.component.edit;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.EntityRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;

/**
 * A factory for {@link IMObjectEditor} instances.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class IMObjectEditorFactory {

    /**
     * Create a new editor.
     *
     * @param object     the object to edit
     * @param parent     the parent object. May be <code>null</code>
     * @param descriptor the parent object's descriptor. May be
     *                   <code>null</code>
     * @return a new editor to edit <code>object</code>
     */
    public static IMObjectEditor create(IMObject object, IMObject parent,
                                        NodeDescriptor descriptor) {
        IMObjectEditor result;
        if (object instanceof EntityRelationship) {
            result = new RelationshipEditor((EntityRelationship) object, parent,
                    descriptor);
        } else {
            result = new DefaultIMObjectEditor(object, parent, descriptor);
        }
        return result;
    }
}
