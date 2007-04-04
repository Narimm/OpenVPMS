/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl.load;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.etl.ETLValue;
import org.openvpms.etl.ETLValueDAOImpl;
import static org.openvpms.etl.load.LoaderException.ErrorCode.IMObjectNotFound;

import java.util.Collection;
import java.util.List;


/**
 * A {@link Loader} that validates, but does not store any data.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ValidatingLoader extends Loader {

    /**
     * The logger.
     */
    private final Log log = LogFactory.getLog(ValidatingLoader.class);


    /**
     * Constructs a new <tt>ValidatingLoader</tt>.
     *
     * @param dao              the DAO
     * @param service the archetype service
     * @param translateLookups if <tt>true</tt> translate values for lookup
     */
    public ValidatingLoader(ETLValueDAOImpl dao, IArchetypeService service,
                            boolean translateLookups) {
        super(dao, service, translateLookups, true);
    }

    /**
     * Loads an object.
     *
     * @param objectId  the object's identifier
     * @param archetype the object's archetype
     * @param values    the values to construct the object from
     * @return the object, or <tt>null</tt> if it cannot be loaded
     * @throws LoaderException           for any error
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    protected IMObject load(String objectId, String archetype,
                            List<ETLValue> values) {
        IMObject target = null;
        boolean validate = !isLoaded(objectId);
        try {
            target = super.load(objectId, archetype, values);
            if (validate) {
                try {
                    getService().validateObject(target);
                } catch (ValidationException exception) {
                    log.error("Error processing objectId=" + objectId
                            + ": Failed to validate: "
                            + exception.toString());
                }
            }
        } catch (LoaderException exception) {
            // ignore IMObjectNotFound errors as it probably indicates that a
            // reference to an object cannot be resolved as the ValidatingLoader
            // discards rather than saves objects
            if (!exception.getErrorCode().equals(IMObjectNotFound)) {
                log.error(exception.getMessage());
            }
        }
        return target;
    }

    /**
     * Saves a set of mapped objects.
     * This operation is a no-op as the <tt>ValidatingLoader</tt> does not save.
     *
     * @param objects the objects to save
     * @param validate if <tt>true</tt> validate objects prior to saving them
     * @throws ArchetypeServiceException for any error
     */
    @Override
    protected void save(Collection<IMObject> objects, boolean validate) {
    }

}
