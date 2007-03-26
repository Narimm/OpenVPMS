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
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationException;
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.etl.ETLValue;
import org.openvpms.etl.ETLValueDAO;

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
     * @param service the archetype service
     */
    public ValidatingLoader(ETLValueDAO dao, IArchetypeService service) {
        super(dao, service);
    }

    /**
     * Loads an object.
     *
     * @param objectId  the object's identifier
     * @param archetype the object's archetype
     * @param values    the values to construct the object from
     * @return the object
     * @throws LoaderException for any error
     */
    @Override
    protected IMObject load(String objectId, String archetype,
                            List<ETLValue> values) {
        IMObject target;
        LoadState state = mapped.get(objectId);
        boolean validate = true;
        if (state == null) {
            target = service.create(archetype);
            if (target == null) {
                log.error("Error processing objectId=" + objectId +
                        ": Archetype not found: " + archetype);
            } else {
                state = new LoadState(target, service);
                mapped.put(objectId, state);
                IMObjectBean bean = state.getBean();
                for (ETLValue value : values) {
                    try {
                        setValue(value, bean, objectId);
                    } catch (LoaderException exception) {
                        log.error(exception.getMessage());
                        validate = false; // no point validating
                    }
                }
                if (validate) {
                    try {
                        service.validateObject(target);
                    } catch (ValidationException exception) {
                        log.error("Error processing objectId=" + objectId
                                + ": Failed to validate: "
                                + exception.toString());
                    }
                }
                state.setNull();
            }
        } else {
            target = getObject(state);
        }
        return target;
    }

    @Override
    protected IMObject getObject(LoadState state) {
        IMObject target;
        target = state.getObject();
        if (target == null) {
            target = ArchetypeQueryHelper.getByObjectReference(
                    service, state.getRef());
        }
        return target;
    }
}
