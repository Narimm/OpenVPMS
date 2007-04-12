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
import org.openvpms.component.business.service.archetype.helper.ArchetypeQueryHelper;
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
     * IMObject.
     */
    private Map<IMObject, ETLLog> logs = new HashMap<IMObject, ETLLog>();

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
     * The batch size.
     */
    private final int batchSize = 100;


    /**
     * Constructs a new <tt>DefaultObjectHandler</tt>.
     *
     * @param loaderName the loader name
     * @param dao        the DAO
     * @param service    the archetype service
     */
    public DefaultObjectHandler(String loaderName, ETLLogDAO dao,
                                IArchetypeService service) {
        this.loaderName = loaderName;
        this.dao = dao;
        this.service = service;
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
            ETLLog log = logs.remove(object);
            String reference = Reference.create(log.getArchetype(),
                                                log.getRowId());
            references.remove(reference);
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
        logs.put(object, createLog(rowId, object, index));
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
        log.setErrors(exception.getMessage());
        errorLogs.add(log);
        notifyListener(rowId, exception);
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
                        Map<IMObject, ETLLog> logs,
                        Collection<ETLLog> errorLogs) {
        try {
            service.save(objects);
            dao.save(logs.values());
        } catch (OpenVPMSException exception) {
            // can't process as a batch. Process individual objects.
            for (IMObject object : objects) {
                save(object, logs.get(object));
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
     * @param log    the object's log
     */
    protected void save(IMObject object, ETLLog log) {
        try {
            service.save(object);
        } catch (OpenVPMSException exception) {
            log.setErrors(exception.getMessage());
            log.setLinkId(null);
            notifyListener(log.getRowId(), exception);
        }
        dao.save(log);
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
        List<ETLLog> logs = dao.get(loaderName, ref.getRowId(),
                                    ref.getArchetype());
        if (logs.isEmpty()) {
            logs = dao.get(null, ref.getRowId(), ref.getArchetype());
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
        if (log.getLinkId() == null) {
            throw new LoaderException(ReferencedObjectNotMapped,
                                      Reference.create(log.getArchetype(),
                                                       log.getRowId()),
                                      log.getErrors());
        }
        return new IMObjectReference(archetype.getType(), log.getLinkId());
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
                result = ArchetypeQueryHelper.getByObjectReference(service,
                                                                   objectRef);
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
        log.setLinkId(object.getLinkId());
        return log;
    }

    /**
     * Notifies any registered listener of an error.
     *
     * @param rowId     the identifier of the row that triggered the error
     * @param exception the exception
     */
    private void notifyListener(String rowId, Throwable exception) {
        if (listener != null) {
            listener.error(rowId, exception);
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
