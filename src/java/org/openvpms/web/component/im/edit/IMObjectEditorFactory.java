package org.openvpms.web.component.im.edit;

import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.web.component.im.edit.act.ActEditor;
import org.openvpms.web.component.im.edit.act.ActItemEditor;
import org.openvpms.web.component.im.edit.act.ParticipationEditor;
import org.openvpms.web.component.im.edit.DefaultIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;


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
     * @param object  the object to edit
     * @param showAll if <code>true</code> show optional and required fields;
     *                otherwise show required fields.
     */
    public static IMObjectEditor create(IMObject object, boolean showAll) {
        return create(object, null, null, showAll);
    }

    /**
     * Create a new editor.
     *
     * @param object     the object to edit
     * @param context    the parent object. May be <code>null</code>
     * @param descriptor the parent object's descriptor. May be
     *                   <code>null</code>
     * @param showAll    if <code>true</code> show optional and required fields;
     *                   otherwise show required fields.
     * @return a new editor to edit <code>object</code>
     */
    public static IMObjectEditor create(IMObject object, IMObject context,
                                        NodeDescriptor descriptor, boolean showAll) {
        IMObjectEditor result;
        result = RelationshipEditor.create(object, context, descriptor, showAll);
        if (result == null) {
            result = ActEditor.create(object, context, descriptor, showAll);
        }
        if (result == null) {
            result = ActItemEditor.create(object, context, descriptor, showAll);
        }
        if (result == null) {
            result = ParticipationEditor.create(object, context, descriptor, showAll);
        }
        if (result == null) {
            result = new DefaultIMObjectEditor(object, context, descriptor, showAll);
        }
        return result;
    }
}
