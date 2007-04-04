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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.util.ArrayList;
import java.util.List;


/**
 * Default handler for {@link LookupLoader} events. Makes generated lookups
 * persistent, filtering any duplicates.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DefaultLookupLoaderHandler extends AbstractLookupLoaderHandler {

    /**
     * The no. of processed objects.
     */
    private int count;

    /**
     * The batch size.
     */
    private final int batchSize = 100;

    /**
     * The batch of unsaved lookups.
     */
    private final List<IMObject> batch = new ArrayList<IMObject>();

    /**
     * Determines if validation should occur.
     */
    private final boolean validate;


    /**
     * Constructs a new <tt>LookupLoaderListener</tt>.
     *
     * @param service  the archetype service
     * @param validate if <tt>true</tt> validate objects prior to saving them
     */
    public DefaultLookupLoaderHandler(IArchetypeService service,
                                      boolean validate) {
        super(service);
        this.validate = validate;
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
        Lookup result = super.getLookup(shortName, code);
        if (result == null && !batch.isEmpty()) {
            flush();
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
        batch.clear();
        count = 0;
    }

    /**
     * Flush any unsaved objects.
     */
    @Override
    public void flush() {
        if (!batch.isEmpty()) {
            save(batch);
            batch.clear();
        }
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
     * Queues an object for saving.
     *
     * @param object the object
     */
    protected void add(IMObject object) {
        batch.add(object);
        ++count;
        if (batch.size() >= batchSize) {
            flush();
        }
    }

    /**
     * Saves a set of objects.
     *
     * @param objects the objects to save
     */
    protected void save(List<IMObject> objects) {
        if (!validate) {
            // validation normally does derivation of values, so when not
            // validating, need to do it explicitly
            for (IMObject object : objects) {
                getService().deriveValues(object);
            }
        }
        getService().save(objects, validate);
    }

}
