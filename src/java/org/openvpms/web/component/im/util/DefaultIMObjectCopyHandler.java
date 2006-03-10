package org.openvpms.web.component.im.util;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.IArchetypeService;


/**
 * Default implementation of the {@link IMObjectCopyHandler} interface.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class DefaultIMObjectCopyHandler implements IMObjectCopyHandler {

    /**
     * Determines how {@link IMObjectCopier} should treat an object. This
     * implementation always returns a new instance, of the same archetype as
     * <code>object</code>
     *
     * @param object  the source object
     * @param service the archetype service
     * @return <code>object</code> if the object shouldn't be copied,
     *         <code>null</code> if it should be replaced with
     *         <code>null</code>, or a new instance if the object should be
     *         copied
     */
    public IMObject getObject(IMObject object, IArchetypeService service) {
        return service.create(object.getArchetypeId());
    }

}
