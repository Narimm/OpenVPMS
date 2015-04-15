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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.openvpms.etl.load.LoaderException.ErrorCode.ArchetypeNotFound;
import static org.openvpms.etl.load.LoaderException.ErrorCode.IMObjectNotFound;
import static org.openvpms.etl.load.LoaderException.ErrorCode.InvalidReference;
import static org.openvpms.etl.load.LoaderException.ErrorCode.RefResolvesMultipleObjects;
import static org.openvpms.etl.load.LoaderException.ErrorCode.ReferencedObjectNotMapped;


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
     * are added to {@link #batch} and {@link #batchGroups} on commit.
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
     * All objects in the batch, in the groups that they were committed in.
     */
    private List<List<IMObject>> batchGroups = new ArrayList<List<IMObject>>();

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
    private long batchSize = 1000;

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
        if (!incomplete.isEmpty()) {
            batch.putAll(incomplete);
            batchGroups.add(new ArrayList<IMObject>(incomplete.values()));
            incomplete.clear();
        }
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
     * @param logs      the logs for each object, keyed on link identifier
     * @param saveError if <tt>true</tt>, save any error in the supplied logs,
     *                  otherwise just notify if an error occurs
     * @return <tt>true</tt> if the save was successful, otherwise
     *         <tt>false</tt>
     */
    protected boolean save(Collection<IMObject> objects,
                           Map<String, List<ETLLog>> logs, boolean saveError) {
        boolean result = false;
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
            result = true;
        } catch (OpenVPMSException exception) {
            String message = messages.getMessage(exception);
            String rowId = null;
            boolean singleRowId = true;

            if (saveError) {
                for (Collection<ETLLog> list : logs.values()) {
                    for (ETLLog log : list) {
                        if (rowId == null) {
                            rowId = log.getRowId();
                        } else if (!rowId.equals(log.getRowId())) {
                            singleRowId = false;
                        }
                        log.setErrors(message);
                        log.setReference(null);
                    }
                    dao.save(list);
                }
            }
            if (rowId != null && singleRowId) {
                // only notify with rowId if all of the logs refer to the same
                // row.
                notifyListener(rowId, message, exception);
            } else {
                notifyListener(message, exception);
            }
        }
        return result;
    }

    /**
     * Saves the error logs.
     *
     * @param logs the error logs
     */
    protected void saveErrorLogs(Collection<ETLLog> logs) {
        if (!logs.isEmpty()) {
            for (ETLLog errorLog : logs) {
                dao.remove(errorLog.getLoader(), errorLog.getRowId());
            }
            dao.save(logs);
        }
    }

    /**
     * Saves mapped objects.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void save() {
        if (!batch.isEmpty()) {
            if (!save(batch.values(), logs, false)) {
                // can't process as a batch. Process each batch group instead
                for (List<IMObject> group : batchGroups) {
                    Map<String, List<ETLLog>> logMap
                            = new HashMap<String, List<ETLLog>>();
                    for (IMObject object : group) {
                        List<ETLLog> list = logs.get(object.getLinkId());
                        if (list == null) {
                            throw new IllegalArgumentException(
                                    "No logs corresponding to object: "
                                    + object.getLinkId());
                        }
                        logMap.put(object.getLinkId(), list);
                    }
                    save(group, logMap, true);
                }
            }
            saveErrorLogs(errorLogs);
            batch.clear();
            batchGroups.clear();
            logs.clear();
            errorLogs.clear();
        }
    }

    /**
     * Returns a object reference given an archetype/rowId string reference.
     *
     * @param ref the parsed reference
     * @return the corresponding object reference
     * @throws LoaderException for any loader exception
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
     * @param message   the error message
     * @param exception the exception
     */
    private void notifyListener(String message, Throwable exception) {
        if (listener != null) {
            listener.error(message, exception);
        }
    }

    /**
     * Notifies any registered listener of an error.
     *
     * @param rowId     the identifier of the row that triggered the error
     * @param message   the error message
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
        batchGroups.clear();
    }

}
