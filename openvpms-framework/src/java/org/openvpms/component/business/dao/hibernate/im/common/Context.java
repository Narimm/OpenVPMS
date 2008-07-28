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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.component.business.dao.hibernate.im.common;

import org.hibernate.Session;
import org.hibernate.proxy.HibernateProxy;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class Context {

    private final Assembler assembler;

    private final Session session;

    private final ResourceKey key;

    private Map<IMObject, DOState> objectToDOMap
            = new HashMap<IMObject, DOState>();

    private Map<IMObjectDO, IMObject> doToObjectMap
            = new HashMap<IMObjectDO, IMObject>();

    private Map<IMObjectReference, DOState> refToDOMap
            = new HashMap<IMObjectReference, DOState>();

    private Set<DOState> saved = new HashSet<DOState>();

    private Set<DOState> saveDeferred = new LinkedHashSet<DOState>();

    private Set<Object> assembling = new HashSet<Object>();

    private final boolean syncActive;

    private ContextHandler handler;

    private Context(Assembler assembler, Session session, boolean syncActive) {
        this.assembler = assembler;
        this.session = session;
        this.syncActive = syncActive;
        key = new ResourceKey(session);
    }

    public static Context getContext(Assembler assembler, Session session) {
        Context context;
        ResourceKey key = new ResourceKey(session);
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            if (!TransactionSynchronizationManager.hasResource(key)) {
                context = new Context(assembler, session, true);
                TransactionSynchronizationManager.bindResource(
                        context.getResourceKey(), context);
                TransactionSynchronizationManager.registerSynchronization(
                        new ContextSynchronization(context));
            } else {
                context = (Context) TransactionSynchronizationManager.getResource(
                        key);
            }
        } else {
            context = new Context(assembler, session, false);
        }
        return context;
    }

    public void setContextHandler(ContextHandler handler) {
        this.handler = handler;
    }

    public Assembler getAssembler() {
        return assembler;
    }

    public void addAssembling(DOState object) {
        assembling.add(object);
    }

    public void removeAssembling(DOState object) {
        assembling.remove(object);
    }

    public boolean isAssembling(DOState object) {
        return assembling.contains(object);
    }

    public void addAssembling(IMObject object) {
        assembling.add(object);
    }

    public void removeAssembling(IMObject object) {
        assembling.remove(object);
    }
    public boolean isAssembling(IMObject object) {
        return assembling.contains(object);
    }

    public Session getSession() {
        return session;
    }

    public boolean isSynchronizationActive() {
        return syncActive;
    }

    public void add(IMObject source, DOState target) {
        objectToDOMap.put(source, target);
        doToObjectMap.put(target.getObject(), source);
        refToDOMap.put(source.getObjectReference(), target);
    }

    public void add(IMObjectDO source, IMObject target) {
        doToObjectMap.put(source, target);
    }

    public void remove(IMObjectDO target) {
        IMObject source = doToObjectMap.get(target);
        session.delete(target);
        doToObjectMap.remove(target);
        if (source != null) {
            DOState state = objectToDOMap.get(source);
            objectToDOMap.remove(source);
            refToDOMap.remove(source.getObjectReference());
            if (state != null) {
                saveDeferred.remove(state);
            }
        }
    }

    public DOState getCached(IMObject source) {
        return objectToDOMap.get(source);
    }

    public IMObject getCached(IMObjectDO source) {
        return doToObjectMap.get(source);
    }

    public DOState getCached(IMObjectReference reference) {
        return refToDOMap.get(reference);
    }

    public <T extends IMObjectDO, Impl extends IMObjectDOImpl> T
            get(IMObjectReference reference, Class<T> type, Class<Impl> impl) {
        Object result = session.get(impl, reference.getId());
        if (result instanceof HibernateProxy) {
            HibernateProxy proxy = ((HibernateProxy) result);
            result = proxy.getHibernateLazyInitializer().getImplementation();
        }

        return type.cast(result);
    }

    public void destroy() {
        for (DOState state : objectToDOMap.values()) {
            state.destroy();
        }
        objectToDOMap.clear();
        doToObjectMap.clear();
        refToDOMap.clear();
        saved.clear();
        saveDeferred.clear();
    }

    private Object getResourceKey() {
        return key;
    }

    public void addSaveDeferred(DOState state) {
        saveDeferred.add(state);
    }

    public Set<DOState> getSaveDeferred() {
        return saveDeferred;
    }

    public void removeSaveDeferred(DOState state) {
        saveDeferred.remove(state);
    }

    public void addSaved(DOState state) {
        saved.add(state);
    }

    public Set<DOState> getSaved() {
        return saved;
    }

    private static class ContextSynchronization
            extends TransactionSynchronizationAdapter {

        private final Context context;

        public ContextSynchronization(Context context) {
            this.context = context;
        }

        @Override
        public void suspend() {
            TransactionSynchronizationManager.unbindResource(
                    context.getResourceKey());
        }

        @Override
        public void resume() {
            TransactionSynchronizationManager.bindResource(
                    context.getResourceKey(), context);
        }

        @Override
        public void beforeCommit(boolean readOnly) {
            ContextHandler handler = context.handler;
            if (handler != null) {
                handler.preCommit(context);
            }
        }

        @Override
        public void afterCompletion(int status) {
            TransactionSynchronizationManager.unbindResource(
                    context.getResourceKey());
            ContextHandler handler = context.handler;
            if (handler != null) {
                if (status == STATUS_COMMITTED) {
                    handler.commit(context);
                } else {
                    handler.rollback(context);
                }
            }

            context.destroy();
        }

    }

    private static class ResourceKey {

        private final Session session;

        public ResourceKey(Session session) {
            this.session = session;
        }

        /**
         * Returns a hash code value for the object.
         *
         * @return a hash code value for this object.
         */
        @Override
        public int hashCode() {
            return session.hashCode();
        }

        /**
         * Indicates whether some other object is "equal to" this one.
         *
         * @param obj the reference object with which to compare.
         * @return <tt>true</tt> if this object is the same as the obj
         *         argument; <tt>false</tt> otherwise.
         */
        @Override
        public boolean equals(Object obj) {
            if (obj == this) {
                return true;
            }
            if (obj instanceof ResourceKey) {
                return session.equals(((ResourceKey) obj).session);
            }
            return false;
        }

    }
}
