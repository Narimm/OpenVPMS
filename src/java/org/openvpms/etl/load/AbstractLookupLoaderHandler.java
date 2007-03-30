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
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.LookupHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeConstraint;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefNodeConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ShortNameConstraint;

import java.util.List;


/**
 * Abstract implementation of the {@link LookupLoaderHandler} interface.
 * Filters duplicate lookups.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class AbstractLookupLoaderHandler
        implements LookupLoaderHandler {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The logger.
     */
    private final Log log
            = LogFactory.getLog(AbstractLookupLoaderHandler.class);


    /**
     * Constructs a new <tt>AbstractLookupLoaderListener</tt>.
     */
    public AbstractLookupLoaderHandler(IArchetypeService service) {
        this.service = service;
    }

    /**
     * Resolves a lookup given its archetype short name and code.
     *
     * @param shortName the lookup short name
     * @param code      the lookup code
     * @return the corresponding lookup or <tt>null</tt> if none is found
     * @throws ArchetypeServiceException for any error
     */
    public Lookup getLookup(String shortName, String code) {
        return LookupHelper.getLookup(getService(), shortName, code);
    }

    /**
     * Indicates start of a load.
     */
    public void start() {
        // no-op
    }

    /**
     * Adds a new object.
     * This implementation filters duplicates.
     *
     * @param object  the object
     * @param context the object's context. May be <tt>null</tt>
     */
    public void add(IMObject object, Context context) {
        if (object instanceof Lookup) {
            if (!isDuplicate((Lookup) object)) {
                add(object);
            }
        } else if (object instanceof LookupRelationship) {
            if (!isDuplicate((LookupRelationship) object)) {
                add(object);
            }
        } else {
            add(object);
        }
    }

    /**
     * Indicates a load error.
     * Logs an error and returns <tt>false</tt> to terminate the load.
     *
     * @param object    the object. May be <tt>null</tt>
     * @param exception the exception
     * @param context   the object's context. May be <tt>null</tt>
     * @return <tt>false</tt>
     */
    public boolean error(IMObject object, Throwable exception,
                         Context context) {
        log.error(exception);
        return false;
    }

    /**
     * Flush any unsaved objects.
     */
    public void flush() {
    }

    /**
     * Indicates end of a load.
     * Delegates to {@link #flush()}.
     */
    public void end() {
        flush();
    }

    /**
     * Adds an object.
     *
     * @param object the object
     */
    protected abstract void add(IMObject object);

    /**
     * Returns the archetype service.
     *
     * @return the archetype service
     */
    protected IArchetypeService getService() {
        return service;
    }

    /**
     * Determines if a lookup is a duplicate.
     *
     * @param lookup the lookup
     * @return <tt>true</tt> if it is a duplicate, otherwise <tt>false</tt>
     */
    private boolean isDuplicate(Lookup lookup) {
        String shortName = lookup.getArchetypeId().getShortName();
        ShortNameConstraint constraint
                = new ShortNameConstraint("l", shortName);
        ArchetypeQuery query = new ArchetypeQuery(constraint);
        query.add(new NodeSelectConstraint("l.code"));
        query.add(new NodeConstraint("code", lookup.getCode()));
        List<ObjectSet> sets = service.getObjects(query).getResults();
        return !sets.isEmpty();
    }

    /**
     * Determines if a lookup relationship is a duplicate.
     *
     * @param relationship the relationship
     * @return <tt>true</tt> if it is a duplicate, otherwise <tt>false</tt>
     */
    private boolean isDuplicate(LookupRelationship relationship) {
        String shortName = relationship.getArchetypeId().getShortName();
        ArchetypeQuery query = new ArchetypeQuery(shortName, true, true);
        query.add(new ObjectRefNodeConstraint("source",
                                              relationship.getSource()));
        query.add(new ObjectRefNodeConstraint("target",
                                              relationship.getTarget()));
        List<IMObject> results = service.get(query).getResults();
        return !results.isEmpty();
    }
}
