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

package org.openvpms.component.business.service.archetype;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * Notifies {@link IArchetypeServiceListener} of archetype service events.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class Notifier {

    /**
     * The archetype service.
     */
    private final ArchetypeService service;

    /**
     * The listeners to notify.
     */
    private final Map<String, List<IArchetypeServiceListener>> listeners;

    /**
     * The set of saved objects.
     */
    private Set<IMObject> saved = new LinkedHashSet<IMObject>();

    /**
     * The set of removed objects.
     */
    private Set<IMObject> removed = new LinkedHashSet<IMObject>();

    /**
     * Determines if transaction synchronization is active.
     */
    private final boolean syncActive;

    /**
     * The logger.
     */
    private static Log log = LogFactory.getLog(Notifier.class);


    /**
     * Creates a new <tt>Notifier</tt>.
     *
     * @param service    the archetype service
     * @param syncActive determines if transaction synchronization is active.
     *                   If so, notification is delayed until the transaction
     *                   commits
     */
    public Notifier(ArchetypeService service, boolean syncActive) {
        this.service = service;
        this.syncActive = syncActive;
        this.listeners = service.getListeners();
    }

    /**
     * Notifies listeners of an object about to be saved.
     *
     * @param object the object being saved
     * @param list   the listeners to notify
     */
    public void notifySaving(IMObject object,
                             List<IArchetypeServiceListener> list) {
        for (IArchetypeServiceListener listener : list) {
            if (syncActive) {
                // there is a transaction in progress, so register the object
                // in order to notify listeners on commit or rollback
                saved.add(object);
            }
            try {
                listener.save(object);
            } catch (Throwable exception) {
                log.warn("Caught unhandled exception from "
                        + "IArchetypeServiceListener.save() implementation ",
                         exception);
            }
        }
    }

    /**
     * Notifies listeners of an object about to be removed.
     *
     * @param object the object being removed
     * @param list   the listeners to notify
     */
    public void notifyRemoving(IMObject object,
                               List<IArchetypeServiceListener> list) {
        for (IArchetypeServiceListener listener : list) {
            if (syncActive) {
                // there is a transaction in progress, so register the object
                // in order to notify listeners on commit or rollback
                removed.add(object);
            }
            try {
                listener.remove(object);
            } catch (Throwable exception) {
                log.warn("Caught unhandled exception from "
                        + "IArchetypeServiceListener.remove() implementation ",
                         exception);
            }
        }
    }

    /**
     * Notifies listeners of an object being saved.
     * <p/>
     * If there is a transaction active, notification will be delayed and
     * only those listeners registered at time of transaction commit will be
     * notified.
     * <p/>
     * If there is no transaction active, the supplied listeners will be
     * notified immediately.
     *
     * @param object the saved object
     * @param list   the listeners to notify, if no transaction is active
     */
    public void notifySaved(IMObject object,
                            List<IArchetypeServiceListener> list) {
        if (syncActive) {
            saved.add(object);
        } else {
            doNotifySaved(object, list);
        }
    }

    /**
     * Notifies listeners of an object being removed.
     * <p/>
     * If there is a transaction active, notification will be delayed and
     * only those listeners registered at time of transaction commit will be
     * notified.
     * <p/>
     * If there is no transaction active, the supplied listeners will be
     * notified immediately.
     *
     * @param object the saved object
     * @param list   the listeners to notify, if no transaction is active
     */
    public void notifyRemoved(IMObject object,
                              List<IArchetypeServiceListener> list) {
        if (syncActive) {
            removed.add(object);
        } else {
            doNotifyRemoved(object, list);
        }
    }

    /**
     * Notifies listeners of any pending events on commit.
     */
    public void notifyCommit() {
        for (IMObject object : saved) {
            synchronized (listeners) {
                List<IArchetypeServiceListener> list
                        = listeners.get(object.getArchetypeId().getShortName());
                if (list != null) {
                    doNotifySaved(object, list);
                }
            }
        }
        for (IMObject object : removed) {
            synchronized (listeners) {
                List<IArchetypeServiceListener> list
                        = listeners.get(object.getArchetypeId().getShortName());
                if (list != null) {
                    doNotifyRemoved(object, list);
                }
            }
        }
        destroy();
    }

    /**
     * Notifies listeners of any pending events on rollback.
     */
    public void notifyRollback() {
        for (IMObject object : saved) {
            synchronized (listeners) {
                List<IArchetypeServiceListener> list
                        = listeners.get(object.getArchetypeId().getShortName());
                if (list != null) {
                    doNotifyRollback(object, list);
                }
            }
        }
        for (IMObject object : removed) {
            synchronized (listeners) {
                List<IArchetypeServiceListener> list
                        = listeners.get(object.getArchetypeId().getShortName());
                if (list != null) {
                    doNotifyRollback(object, list);
                }
            }
        }
        destroy();
    }

    /**
     * Returns the notifier for the given service and current thread.
     * <p/>
     * If one does not exist, it will be created.
     *
     * @param service the archetype service
     * @return the context
     */
    public static Notifier getNotifier(ArchetypeService service) {
        Notifier notifier;
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            if (!TransactionSynchronizationManager.hasResource(service)) {
                notifier = new Notifier(service, true);
                TransactionSynchronizationManager.bindResource(
                        service, notifier);
                TransactionSynchronizationManager.registerSynchronization(
                        new NotifierSynchronization(notifier));
            } else {
                notifier = (Notifier)
                        TransactionSynchronizationManager.getResource(service);
            }
        } else {
            notifier = new Notifier(service, false);
        }
        return notifier;
    }

    /**
     * Destroys any pending events.
     */
    private void destroy() {
        saved.clear();
        removed.clear();
    }

    /**
     * Notifies listeners of an object being saved.
     *
     * @param object    the saved object
     * @param listeners the listener to notify
     */
    private void doNotifySaved(IMObject object,
                               List<IArchetypeServiceListener> listeners) {
        for (IArchetypeServiceListener listener : listeners) {
            try {
                listener.saved(object);
            } catch (Throwable exception) {
                log.warn("Caught unhandled exception from "
                        + "IArchetypeServiceListener implementation ",
                         exception);
            }
        }
    }

    /**
     * Notifies listeners of an object being removed.
     *
     * @param object    the removed object
     * @param listeners the listeners to notify
     */
    private void doNotifyRemoved(IMObject object,
                                 List<IArchetypeServiceListener> listeners) {
        for (IArchetypeServiceListener listener : listeners) {
            try {
                listener.removed(object);
            } catch (Throwable exception) {
                log.warn("Caught unhandled exception from "
                        + "IArchetypeServiceListener.removed() implementation ",
                         exception);
            }
        }
    }

    /**
     * Notifies listeners of an object being rolled back.
     *
     * @param object    the removed object
     * @param listeners the listeners to notify
     */
    private void doNotifyRollback(IMObject object,
                                  List<IArchetypeServiceListener> listeners) {
        for (IArchetypeServiceListener listener : listeners) {
            try {
                listener.rollback(object);
            } catch (Throwable exception) {
                log.warn("Caught unhandled exception from "
                        + "IArchetypeServiceListener.rollback() "
                        + "implementation ", exception);
            }
        }
    }

    /**
     * Helper class to trigger events on transaction commit.
     */
    private static class NotifierSynchronization
            extends TransactionSynchronizationAdapter {

        private final Notifier notifier;

        public NotifierSynchronization(Notifier notifier) {
            this.notifier = notifier;
        }

        @Override
        public void suspend() {
            TransactionSynchronizationManager.unbindResource(notifier.service);
        }

        @Override
        public void resume() {
            TransactionSynchronizationManager.bindResource(notifier.service,
                                                           notifier);
        }

        @Override
        public void afterCompletion(int status) {
            TransactionSynchronizationManager.unbindResource(notifier.service);
            if (status == STATUS_COMMITTED) {
                notifier.notifyCommit();
            } else if (status == STATUS_ROLLED_BACK) {
                notifier.notifyRollback();
            }
            notifier.destroy();
        }

    }

}
