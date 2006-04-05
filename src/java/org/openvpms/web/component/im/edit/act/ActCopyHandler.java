package org.openvpms.web.component.im.edit.act;

import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.domain.im.common.ActRelationship;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.im.util.DefaultIMObjectCopyHandler;
import org.openvpms.web.component.im.util.IMObjectCopier;


/**
 * Copy handler for {@link Act}s. This copies acts, act relationships and
 * participations.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ActCopyHandler extends DefaultIMObjectCopyHandler {

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
        if (object instanceof Act || object instanceof ActRelationship
            || object instanceof Participation) {
            result = super.getObject(object, service);
        } else {
            result = object;
        }
        return result;
    }
}
