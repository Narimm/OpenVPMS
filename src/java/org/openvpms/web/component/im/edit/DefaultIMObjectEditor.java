package org.openvpms.web.component.im.edit;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;


/**
 * Default editor for {@link IMObject}s.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class DefaultIMObjectEditor extends AbstractIMObjectEditor {

    /**
     * Construct a new <code>DefaultIMObjectEditor</code>.
     *
     * @param object  the object to edit
     * @param showAll if <code>true</code> show optional and required fields;
     *                otherwise show required fields.
     */
    public DefaultIMObjectEditor(IMObject object, boolean showAll) {
        this(object, null, null, showAll);
    }

    /**
     * Construct a new <code>DefaultIMObjectEditor</code> for an object that
     * belongs to a collection.
     *
     * @param object     the object to edit
     * @param parent     the parent object.
     * @param descriptor the parent descriptor
     */
    public DefaultIMObjectEditor(IMObject object, IMObject parent,
                                 NodeDescriptor descriptor) {
        this(object, parent, descriptor, false);
    }

    /**
     * Construct a new <code>DefaultIMObjectEditor</code> for an object that
     * belongs to a collection.
     *
     * @param object     the object to edit
     * @param parent     the parent object.
     * @param descriptor the parent descriptor
     * @param showAll    if <code>true</code> show optional and required fields;
     *                   otherwise show required fields.
     */
    public DefaultIMObjectEditor(IMObject object, IMObject parent,
                                 NodeDescriptor descriptor, boolean showAll) {
        super(object, parent, descriptor, showAll);
    }

}
