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

package org.openvpms.component.business.dao.hibernate.im.entity;

import org.hibernate.HibernateException;
import org.hibernate.NonUniqueObjectException;
import org.hibernate.ObjectDeletedException;
import org.hibernate.Session;
import org.hibernate.StaleObjectStateException;
import org.openvpms.component.business.dao.im.common.IMObjectDAO;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;


/**
 * Abstract implementation of the {@link IMObjectSessionHandler} interface.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
abstract class AbstractIMObjectSessionHandler
        implements IMObjectSessionHandler {

    /**
     * The DAO.
     */
    private final IMObjectDAO dao;


    /**
     * Creates a new <tt>AbstractIMObjectSessionHandler<tt>.
     *
     * @param dao the DAO
     */
    public AbstractIMObjectSessionHandler(IMObjectDAO dao) {
        this.dao = dao;
    }

    /**
     * Saves an object.
     *
     * @param object     the object to save
     * @param session    the session to use
     * @param newObjects used to collect new objects encountered during save
     * @return the result of <tt>Session.merge(object)</tt>
     */
    public IMObject save(IMObject object, Session session,
                         Set<IMObject> newObjects) {
        if (object.isNew()) {
            session.saveOrUpdate(object);
            newObjects.add(object);
        } else {
            try {
                object = (IMObject) session.merge(object);
            } catch (ObjectDeletedException exception) {
                throw exception;
            }
        }
        return object;
    }

    /**
     * Updates the target object with the identifier and version of the source.
     *
     * @param target the object to update
     * @param source the object to update from
     */
    public void updateIds(IMObject target, IMObject source) {
        updateId(target, source);
    }

    /**
     * Deletes an object.
     *
     * @param object  the object to delete
     * @param session the session
     */
    public void delete(IMObject object, Session session) {
        deleteObject(object, session);
    }

    /**
     * Delete a collection of objects.
     *
     * @param objects the objects to delete
     * @param session the session to use
     */
    protected void delete(Collection<IMObject> objects, Session session) {
        for (IMObject object : objects) {
            deleteObject(object, session);
        }
    }

    /**
     * Retrieves an object given its reference.
     *
     * @param reference the object reference
     * @return the corresponding object, or <tt>null</tt> if none is found
     */
    protected IMObject get(IMObjectReference reference) {
        return dao.getByReference(reference);
    }

    /**
     * Helper to save any transient instances.
     *
     * @param objects    the objects to check
     * @param session    the session
     * @param newObjects used to collect new objects encountered during save
     */
    protected <T extends IMObject> void saveNew(Collection<T> objects,
                                                Session session,
                                                Set<IMObject> newObjects) {
        for (T object : objects) {
            if (object.isNew()) {
                session.save(object);
                newObjects.add(object);
            }
        }
    }

    /**
     * Updates the target objects with the identifier and version of the their
     * corresponding sources.
     *
     * @param targets the targets to update
     * @param sources the sources to update from
     * @param handler the handler to update the objects
     */
    protected <T extends IMObject> void update(Collection<T> targets,
                                               Collection<T> sources,
                                               IMObjectSessionHandler handler) {
        if (!targets.isEmpty()) {
            Map<IMObjectReference, T> map = getReferenceMap(sources);
            for (T target : targets) {
                T source = map.get(target.getObjectReference());
                if (source != null) {
                    handler.updateIds(target, source);
                }
            }
        }
    }

    /**
     * Updates the target object with the identifier and version of the source.
     *
     * @param target the object to update
     * @param source the object to update from
     */
    private void updateId(IMObject target, IMObject source) {
        if (target.getUid() != source.getUid()) {
            target.setUid(source.getUid());
        }
        if (target.getVersion() < source.getVersion()) {
            target.setVersion(source.getVersion());
        }
    }

    /**
     * Deletes an object. If the object is already persistent, it will be
     * reloaded into the current session, in order to avoid
     * {@link NonUniqueObjectException} should a detached instance be deleted
     * in a session already containing it.
     *
     * @param object  the object to delete
     * @param session the session
     * @throws StaleObjectStateException if the object has subsequently been
     *                                   deleted/changed
     * @throws HibernateException        for any other error
     */
    protected void deleteObject(IMObject object, Session session) {
        if (!object.isNew()) {
            object = reload(object); // could do a merge() instead?
        }
        if (object != null) {
            session.delete(object);
        }
    }

    /**
     * Reloads an object into a session.
     *
     * @param object the object to load
     * @return the reloaded object
     * @throws StaleObjectStateException if the object has subsequently been
     *                                   deleted/changed
     * @throws HibernateException        for any other error
     */
    private IMObject reload(IMObject object) {
        long version = object.getVersion();
        IMObject result = get(object.getObjectReference());
        if (result != null && result.getVersion() != version) {
            throw new StaleObjectStateException(object.getClass().getName(),
                                                object.getUid());
        }
        return result;
    }

    /**
     * Helper to return a map of {@link IMObject}s keyed by reference.
     *
     * @param objects the objects
     * @return the objects keyed by reference
     */
    private <T extends IMObject>Map<IMObjectReference, T> getReferenceMap(
            Collection<T> objects) {
        Map<IMObjectReference, T> map = new HashMap<IMObjectReference, T>();
        for (T source : objects) {
            map.put(source.getObjectReference(), source);
        }
        return map;
    }

}
