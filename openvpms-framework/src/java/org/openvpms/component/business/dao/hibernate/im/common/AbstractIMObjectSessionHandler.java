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

package org.openvpms.component.business.dao.hibernate.im.common;

import org.hibernate.Session;
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
public abstract class AbstractIMObjectSessionHandler
        implements IMObjectSessionHandler {

    /**
     * The DAO.
     */
    private final IMObjectDAO dao;

    /**
     * The assembler.
     */
    private final Assembler assembler;


    /**
     * Creates a new <tt>AbstractIMObjectSessionHandler<tt>.
     *
     * @param dao       the DAO
     * @param assembler the assembler
     */
    public AbstractIMObjectSessionHandler(IMObjectDAO dao,
                                          Assembler assembler) {
        this.dao = dao;
        this.assembler = assembler;
    }

    /**
     * Saves an object.
     *
     * @param object  the object to save
     * @param session the session to use
     * @param context the assembler context
     */
    public void save(IMObject object, Session session, Context context) {
        DOState state = assembler.assemble(object, context);
        if (state.isComplete()) {
            IMObjectDO target = state.getObject();
            session.saveOrUpdate(target);
            object.setId(target.getId());
            object.setVersion(target.getVersion());
        }
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
     * @param context
     */
    public void delete(IMObject object, Session session, Context context) {
        if (!object.isNew()) {
            DOState state = assembler.assemble(object, context);
            delete(state.getObject(), session);
            session.flush();
        }
    }

    protected void delete(IMObjectDO object, Session session) {
        session.delete(object);
    }

    /**
     * Retrieves an object given its reference.
     *
     * @param reference the object reference
     * @return the corresponding object, or <tt>null</tt> if none is found
     */
    protected IMObject get(IMObjectReference reference) {
        return dao.get(reference);
    }

    /**
     * Saves unsaved objects in a collection.
     * <p/>
     * This calls {@link Session#save} for each non-persistent object in the
     * collection, and adds them to <tt>newObjects</tt>.
     *
     * @param parent     the parent object
     * @param objects    the objects to check
     * @param session    the session
     * @param newObjects used to collect new objects encountered during save
     */
    protected <T extends IMObject> void saveNew(IMObject parent,
                                                Collection<T> objects,
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
     * Saves an object.
     * <p/>
     * If the object is not persistent, it will be saved using
     * {@link Session#save(Object)}, and added to <tt>newObjects</tt>.
     * <p/>
     * If it is persistent, it will be saved using
     * {@link Session#merge(Object)}.
     *
     * @param object  the object to save
     * @param session the session
     * @param context
     */
    protected void saveMerge(IMObject object, Session session,
                             Context context) {
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
        if (target.getId() != source.getId()) {
            target.setId(source.getId());
        }
        if (target.getVersion() < source.getVersion()) {
            target.setVersion(source.getVersion());
        }
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
