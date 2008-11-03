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

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.query.NodeConstraint;
import static org.openvpms.etl.load.LoaderException.ErrorCode.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Default implementation of the {@link ObjectHandler} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DefaultObjectHandler implements ObjectHandler {

    /**
     * The name of the loader.
     */
    private String loaderName;

    /**
     * The DAO.
     */
    private ETLLogDAO dao;

    /**
     * The archetype service.
     */
    private IArchetypeService service;

    /**
     * A map of string references to their corresponding object references.
     */
    private Map<String, IMObjectReference> references
            = new HashMap<String, IMObjectReference>();

    /**
     * ETLLog instances created via {@link #add}, keyed on their corresponding
     * IMObject linkId.
     */
    private Map<String, List<ETLLog>> logs
            = new HashMap<String, List<ETLLog>>();

    /**
     * ETLLog instances created via {@link #error}.
     */
    private List<ETLLog> errorLogs = new ArrayList<ETLLog>();

    /**
     * The set of incomplete objects, keyed on their references. These
     * are moved to {@link #batch} on commit.
     */
    private Map<IMObjectReference, IMObject> incomplete
            = new HashMap<IMObjectReference, IMObject>();

    /**
     * Cache of row identifiers for incomplete objects.
     */
    private Set<String> rowIds = new HashSet<String>();

    /**
     * The batch of unsaved objects.
     */
    private Map<IMObjectReference, IMObject> batch
            = new LinkedHashMap<IMObjectReference, IMObject>();

    /**
     * The error listener, to notify of processing errors. May be <tt>null</tt>
     */
    private ErrorListener listener;

    /**
     * The cache.
     */
    private final IMObjectCache cache;

    /**
     * The batch size.
     */
    private int batchSize = 1000;

    /**
     * Used for formatting exception messages.
     */
    private final ExceptionHelper messages;


    /**
     * Constructs a new <tt>DefaultObjectHandler</tt>.
     *
     * @param loaderName the loader name
     * @param mappings   the mappings
     * @param dao        the DAO
     * @param service    the archetype service
     */
    public DefaultObjectHandler(String loaderName, Mappings mappings,
                                ETLLogDAO dao, IArchetypeService service) {
        this.loaderName = loaderName;
        this.dao = dao;
        batchSize = mappings.getBatchSize();
        if (batchSize <= 0) {
            batchSize = 1;
        }
        this.service = service;
        this.cache = new IMObjectCache(service);
        messages = new ExceptionHelper(service);
    }

    /**
     * Indicates start of a load.
     */
    public void begin() {
        cleanup();
    }

    /**
     * Commits any unsaved objects.
     *
     * @throws LoaderException           for any loader exception
     * @throws ArchetypeServiceException for any archetyype service error
     */
    public void commit() {
        for (String rowId : rowIds) {
            dao.remove(loaderName, rowId);
        }
        rowIds.clear();
        batch.putAll(incomplete);
        incomplete.clear();
        if (batch.size() > batchSize) {
            save();
        }
    }

    /**
     * Rolls back any unsaved objects.
     */
    public void rollback() {
        for (IMObject object : incomplete.values()) {
            for (ETLLog log : logs.remove(object.getLinkId())) {
                String reference = Reference.create(log.getArchetype(),
                                                    log.getRowId());
                references.remove(reference);
            }
        }
        incomplete.clear();
    }

    /**
     * Indicates end of a load.
     *
     * @throws LoaderException           for any loader exception
     * @throws ArchetypeServiceException for any archetyype service error
     */
    public void end() {
        commit();
        save();
    }

    /**
     * Dispose of the handler.
     */
    public void close() {
        dao = null;
        service = null;
        cleanup();
    }

    /**
     * Adds a new object. The object is considered incomplete until
     * {@link #commit()} is invoked.
     *
     * @param rowId  the source row identifier
     * @param object the object
     * @param index  the object's  collection index, or <tt>-1</tt> if it
     *               doesn't belong to a collection
     */
    public void add(String rowId, IMObject object, int index) {
        String archetype = object.getArchetypeId().getShortName();
        String reference = Reference.create(archetype, rowId);
        references.put(reference, object.getObjectReference());
        List<ETLLog> logList = logs.get(object.getLinkId());
        if (logList == null) {
            logList = new ArrayList<ETLLog>();
            logs.put(object.getLinkId(), logList);
        }
        logList.add(createLog(rowId, object, index));
        incomplete.put(object.getObjectReference(), object);
        rowIds.add(rowId);
    }

    /**
     * Invoked when an error occurs.
     *
     * @param rowId     the identifier of the row that triggered the error
     * @param exception the exception
     */
    public void error(String rowId, Throwable exception) {
        ETLLog log = new ETLLog(loaderName, rowId, null);
        String message = messages.getMessage(exception);
        log.setErrors(message);
        errorLogs.add(log);
        notifyListener(rowId, message, exception);
    }

    /**
     * Gets an object, given a string reference.
     *
     * @param reference the reference
     * @return the object corresponding to <tt>reference</tt>
     * @throws LoaderException           for any loader exception
     * @throws ArchetypeServiceException for any archetyype service error
     */
    public IMObject getObject(String reference) {
        IMObject result;
        IMObjectReference objectRef = references.get(reference);
        if (objectRef != null) {
            result = getObject(objectRef, reference);
        } else {
            Reference ref = ReferenceParser.parse(reference);
            if (ref == null) {
                throw new LoaderException(InvalidReference, reference);
            }
            if (ref.getRowId() != null) {
                objectRef = getMappedReference(ref);
                result = getObject(objectRef, reference);
            } else {
                result = getObject(ref);
            }
            references.put(ref.toString(), result.getObjectReference());
        }
        return result;
    }

    /**
     * Gets an object reference, given a string reference.
     *
     * @param reference the reference
     * @return the object reference corresponding to <tt>reference</tt>
     * @throws LoaderException           for any loader exception
     * @throws ArchetypeServiceException for any archetyype service error
     */
    public IMObjectReference getReference(String reference) {
        IMObjectReference result = references.get(reference);
        if (result == null) {
            Reference ref = ReferenceParser.parse(reference);
            if (ref == null) {
                throw new LoaderException(InvalidReference, reference);
            }
            if (ref.getRowId() != null) {
                result = getMappedReference(ref);
            } else {
                result = getObject(ref).getObjectReference();
            }
            references.put(ref.toString(), result);
        }
        return result;
    }

    /**
     * Sets a listener to be notified of errors.
     *
     * @param listener the listener. May be <tt>null</tt>
     */
    public void setErrorListener(ErrorListener listener) {
        this.listener = listener;
    }

    /**
     * Saves a set of mapped objects.
     *
     * @param objects   the objects to save
     * @param logs      the object logs
     * @param errorLogs any error logs
     */
    protected void save(Collection<IMObject> objects,
                        Map<String, List<ETLLog>> logs,
                        Collection<ETLLog> errorLogs) {
        try {
            service.save(objects);
            for (IMObject object : objects) {
                List<ETLLog> objectLogs = logs.get(object.getLinkId());
                if (objectLogs == null) {
                    throw new IllegalArgumentException(
                            "No logs corresponding to object: "
                                    + object.getLinkId());
                }
                for (ETLLog log : objectLogs) {
                    log.setReference(object.getObjectReference());
                }
                dao.save(objectLogs);
            }
        } catch (OpenVPMSException exception) {
            // can't process as a batch. Process individual objects.
            for (IMObject object : objects) {
                List<ETLLog> objectLogs = logs.get(object.getLinkId());
                if (objectLogs == null) {
                    throw new IllegalArgumentException(
                            "No logs corresponding to object: "
                                    + object.getLinkId());
                }
                save(object, objectLogs);
            }
        }
        if (!errorLogs.isEmpty()) {
            for (ETLLog errorLog : errorLogs) {
                dao.remove(errorLog.getLoader(), errorLog.getRowId());
            }
            dao.save(errorLogs);
        }
    }

    /**
     * Save an object.
     *
     * @param object the object to save
     * @param logs   the logs associated with the object
     */
    protected void save(IMObject object, List<ETLLog> logs) {
        try {
            service.save(object);
            // update the reference for each log prior to save
            for (ETLLog log : logs) {
                log.setReference(object.getObjectReference());
            }
        } catch (OpenVPMSException exception) {
            ETLLog first = logs.get(0);
            String message = messages.getMessage(exception);
            for (ETLLog log : logs) {
                log.setErrors(message);
                log.setReference(null);
            }
            notifyListener(first.getRowId(), message, exception);
        }
        dao.save(logs);
    }

    /**
     * Saves mapped objects.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void save() {
        if (!batch.isEmpty()) {
            save(batch.values(), logs, errorLogs);
            batch.clear();
            logs.clear();
            errorLogs.clear();
        }
    }

    /**
     * Returns a object reference given an archetype/rowId string reference.
     *
     * @param ref the parsed reference
     * @return the corresponding object reference
     * @throws LoaderException           for any loader exception
     * @throws ArchetypeServiceException for any archetyype service error
     */
    private IMObjectReference getMappedReference(Reference ref) {
        // NOTE: this previously invoked 2 queries. The first queried on loader
        // name, row id & archetype, the second on row id & archetype.
        // In general however, the second form is the one most used, so for
        // performance reasons exclude the loader name from the query
        // and filter in memory.
        List<ETLLog> logs = dao.get(null, ref.getRowId(), ref.getArchetype());
        if (logs.size() != 1) {
            // try and filter logs on loader name, using these in preference
            // to logs generated by other loaders.
            List<ETLLog> filtered = new ArrayList<ETLLog>();
            for (ETLLog log : logs) {
                if (loaderName.equals(log.getLoader())) {
                    filtered.add(log);
                }
            }
            if (!filtered.isEmpty()) {
                logs = filtered;
            }
        }
        if (logs.isEmpty()) {
            throw new LoaderException(IMObjectNotFound, ref.toString());
        }
        if (logs.size() > 1) {
            throw new LoaderException(RefResolvesMultipleObjects,
                                      ref.toString());
        }
        ETLLog log = logs.get(0);
        ArchetypeDescriptor
                archetype = service.getArchetypeDescriptor(log.getArchetype());
        if (archetype == null) {
            throw new LoaderException(ArchetypeNotFound, log.getArchetype());
        }
        IMObjectReference reference = log.getReference();
        if (reference == null) {
            throw new LoaderException(ReferencedObjectNotMapped,
                                      Reference.create(log.getArchetype(),
                                                       log.getRowId()),
                                      log.getErrors());
        }
        return reference;
    }

    /**
     * Returns an object given its object reference.
     *
     * @param objectRef the object reference
     * @param reference the original reference string
     * @return the object corresponding to <tt>objectRef</tt>
     * @throws LoaderException           if the object cannot be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    private IMObject getObject(IMObjectReference objectRef, String reference) {
        IMObject result = incomplete.get(objectRef);
        if (result == null) {
            result = batch.get(objectRef);
            if (result == null) {
                result = cache.get(objectRef);
                if (result == null) {
                    throw new LoaderException(IMObjectNotFound, reference);
                }
            }
        }
        return result;
    }

    /**
     * Returns an object given an archetype/name/value reference.
     *
     * @param ref the reference
     * @return the object corresponding to <tt>ref</tt>
     * @throws LoaderException           if the object cannot be found
     * @throws ArchetypeServiceException for any archetype service error
     */
    private IMObject getObject(Reference ref) {
        IMObject result;
        ArchetypeQuery query = new ArchetypeQuery(ref.getArchetype(),
                                                  true, true);
        query.add(new NodeConstraint(ref.getName(), ref.getValue()));
        query.setMaxResults(2);
        Iterator<IMObject> iterator
                = new IMObjectQueryIterator<IMObject>(service, query);
        if (iterator.hasNext()) {
            result = iterator.next();
            if (iterator.hasNext()) {
                throw new LoaderException(
                        RefResolvesMultipleObjects, ref.toString());
            }
        } else {
            throw new LoaderException(IMObjectNotFound, ref.toString());
        }
        return result;
    }

    /**
     * Helper to create a new {@link ETLLog}.
     *
     * @param rowId  the row identifier
     * @param object the object
     * @param index  the object's collection index, or <tt>-1</tt> if it
     *               doesn't belong to a collection
     * @return a new log
     */
    private ETLLog createLog(String rowId, IMObject object, int index) {
        ETLLog log = new ETLLog(loaderName, rowId,
                                object.getArchetypeId().getShortName(), index);
        log.setReference(object.getObjectReference());
        return log;
    }

    /**
     * Notifies any registered listener of an error.
     *
     * @param rowId     the identifier of the row that triggered the error
     * @param message the error message
     * @param exception the exception
     */
    private void notifyListener(String rowId, String message,
                                Throwable exception) {
        if (listener != null) {
            listener.error(rowId, message, exception);
        }
    }

    /**
     * Helper to clean up collections.
     */
    private void cleanup() {
        references.clear();
        incomplete.clear();
        rowIds.clear();
        batch.clear();
    }

}
