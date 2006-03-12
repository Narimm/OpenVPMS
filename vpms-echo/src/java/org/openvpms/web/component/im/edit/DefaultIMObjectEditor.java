package org.openvpms.web.component.im.edit;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.layout.LayoutContext;


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
     * @param context the layout context
     */
    public DefaultIMObjectEditor(IMObject object, LayoutContext context) {
        this(object, null, null, context);
    }

    /**
     * Construct a new <code>DefaultIMObjectEditor</code> for an object that
     * belongs to a collection.
     *
     * @param object     the object to edit
     * @param parent     the parent object.
     * @param descriptor the parent descriptor
     * @param context    the layout context
     */
    public DefaultIMObjectEditor(IMObject object, IMObject parent,
                                 NodeDescriptor descriptor,
                                 LayoutContext context) {
        super(object, parent, descriptor, context);
    }

}
