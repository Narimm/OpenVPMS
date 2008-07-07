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
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

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

    private Map<IMObject, IMObjectDO> objectToDOMap
            = new HashMap<IMObject, IMObjectDO>();

    private Map<IMObjectReference, IMObjectDO> refToDOMap
            = new HashMap<IMObjectReference, IMObjectDO>();

    private Map<IMObjectDO, IMObject> assembled
            = new LinkedHashMap<IMObjectDO, IMObject>();

    private Context(Assembler assembler, Session session) {
        this.assembler = assembler;
        this.session = session;
        key = new ResourceKey(session);
    }

    public static Context getContext(Assembler assembler, Session session) {
        Context context;
        ResourceKey key = new ResourceKey(session);
        if (!TransactionSynchronizationManager.hasResource(key)) {
            context = new Context(assembler, session);
            TransactionSynchronizationManager.bindResource(
                    context.getResourceKey(), context);
            TransactionSynchronizationManager.registerSynchronization(
                    new ContextSynchronization(context));
        } else {
            context = (Context) TransactionSynchronizationManager.getResource(
                    key);
        }
        return context;
    }

    public Assembler getAssembler() {
        return assembler;
    }

    public void add(IMObject source, IMObjectDO target) {
        objectToDOMap.put(source, target);
        refToDOMap.put(source.getObjectReference(), target);
        assembled.put(target, source);
    }

    public Map<IMObjectDO, IMObject> getAssembled() {
        return assembled;
    }

    public void clearAssembled() {
        assembled.clear();
    }

    public IMObjectDO getCached(IMObject source) {
        return objectToDOMap.get(source);
    }

    public IMObjectDO getCached(IMObjectReference reference) {
        return refToDOMap.get(reference);
    }

    public <T extends IMObjectDO> T get(IMObjectReference reference,
                                        Class<T> type) {
        Object result = session.get(type, reference.getId());
        return type.cast(result);
    }

    private Object getResourceKey() {
        return key;
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
        public void afterCompletion(int status) {
            TransactionSynchronizationManager.unbindResource(
                    context.getResourceKey());
/*
            if (status == STATUS_COMMITTED) {
                for (Sync sync : list) {
                    sync.sync();
                }
            } else {
                // STATUS_ROLLBACK or STATUS_UNKOWN
                for (IMObjectDO object : newObjects) {
                    object.setId(-1);
                }
            }
*/

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
