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
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.ValidationException;

import java.util.HashMap;
import java.util.Map;


/**
 * Validating handler for the {@link LookupLoader}.
 * This doesn't make any lookup persistent.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ValidatingLookupLoaderHandler
        extends AbstractLookupLoaderHandler {

    /**
     * Cache of loaded lookups, keyed on the concatentation of archetype
     * short name and code.
     */
    private Map<String, Lookup> loaded = new HashMap<String, Lookup>();

    /**
     * The processed object count.
     */
    private int count;

    /**
     * The logger.
     */
    private static final Log log
            = LogFactory.getLog(ValidatingLookupLoaderHandler.class);


    /**
     * Constructs a new <tt>ValidatingLookupLoaderListener</tt>.
     *
     * @param service the archetype service
     */
    public ValidatingLookupLoaderHandler(IArchetypeService service) {
        super(service);
    }

    /**
     * Resolves a lookup given its archetype short name and code.
     *
     * @param shortName the lookup short name
     * @param code      the lookup code
     * @return the corresponding lookup or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any error
     */
    @Override
    public Lookup getLookup(String shortName, String code) {
        Lookup result = loaded.get(getKey(shortName, code));
        if (result == null) {
            result = super.getLookup(shortName, code);
        }
        return result;
    }

    /**
     * Indicates start of a load.
     */
    @Override
    public void start() {
        super.start();
        loaded.clear();
        count = 0;
    }

    /**
     * Indicates a load error.
     * Logs an error and returns <tt>true</tt> to continue the load.
     *
     * @param object    the object. May be <tt>null</tt>
     * @param exception the exception
     * @param context   the object's context. May be <tt>null</tt>
     * @return <tt>true</tt>
     */
    @Override
    public boolean error(IMObject object, Throwable exception,
                         Context context) {
        super.error(object, exception, context);
        return true;
    }

    /**
     * Indicates end of a load.
     * Delegates to {@link #flush()}.
     */
    @Override
    public void end() {
        super.end();
        loaded.clear();
    }

    /**
     * Returns the no. of processed objects.
     *
     * @return the no. of processed objects
     */
    public int getCount() {
        return count;
    }

    /**
     * Adds an object.
     *
     * @param object the object
     */
    protected void add(IMObject object) {
        ++count;
        if (object instanceof Lookup) {
            Lookup lookup = (Lookup) object;
            String shortName = lookup.getArchetypeId().getShortName();
            String code = lookup.getCode();
            String key = getKey(shortName, code);
            loaded.put(key, lookup);

            try {
                getService().validateObject(object);
            } catch (ValidationException exception) {
                log.error("Failed to validate lookup=" + shortName
                        + ", code=" + code + ": " + exception.toString());
            }
        } else {
            try {
                getService().validateObject(object);
            } catch (ValidationException exception) {
                log.error(exception.toString());
            }
        }
    }

    /**
     * Generates a key for a lookup based on its short name and code.
     *
     * @param shortName the lookup short name
     * @param code      the lookup code
     * @return the key
     */
    private String getKey(String shortName, String code) {
        return shortName + "-" + code;
    }

}
