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
 *  Copyright 2011 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id: $
 */

package org.openvpms.web.workspace.admin.lookup;

import org.apache.commons.lang.ObjectUtils;
import org.openvpms.archetype.rules.util.MappingCopyHandler;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopyHandler;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.web.component.im.util.DefaultIMObjectCache;
import org.openvpms.web.component.im.util.IMObjectCache;
import org.openvpms.web.system.ServiceHelper;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Helper to administer lookups.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
class LookupReplaceHelper {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;


    /**
     * Constructs a <tt>LookupReplaceHelper</tt>.
     */
    public LookupReplaceHelper() {
        service = ServiceHelper.getArchetypeService();
    }

    /**
     * Replaces references to the source lookup with the target lookup, optionally deleting the source lookup.
     *
     * @param source the source lookup
     * @param target the target lookup
     * @param delete if <tt>true</tt> delete the source lookup
     */
    public void replace(final Lookup source, final Lookup target, final boolean delete) {
        TransactionTemplate template = new TransactionTemplate(ServiceHelper.getTransactionManager());
        template.execute(new TransactionCallback<Object>() {
            public Object doInTransaction(TransactionStatus status) {
                doReplace(source, target, delete);
                return null;
            }
        });
    }

    /**
     * Replaces references to the source lookup with the target lookup, optionally deleting the source lookup.
     *
     * @param source the source lookup
     * @param target the target lookup
     * @param delete if <tt>true</tt> delete the source lookup
     */
    private void doReplace(Lookup source, Lookup target, boolean delete) {
        ILookupService lookupService = ServiceHelper.getLookupService();
        boolean move = !delete;
        if (mergeRelationships(source, target, move)) {
            if (move) {
                service.save(Arrays.asList(source, target));
            } else {
                service.save(target);
            }
        }
        lookupService.replace(source, target);
        if (delete) {
            service.remove(source);
        }
    }

    /**
     * Copies relationships of the source lookup to the target lookup, if the relationship doesn't exist.
     *
     * @param source                  the lookup to copy relationships from
     * @param target                  the lookup to copy relationships to
     * @param moveSourceRelationships if <tt>true</tt> delete all source relationships from the source
     * @return <tt>true</tt> if lookups were copied
     */
    private boolean mergeRelationships(Lookup source, Lookup target, boolean moveSourceRelationships) {
        IMObjectCache cache = new DefaultIMObjectCache();
        cache.add(source);
        cache.add(target);
        boolean result = false;
        if (!source.getLookupRelationships().isEmpty()) {
            IMObjectCopier copier = new IMObjectCopier(new LookupRelationshipCopyHandler());

            IMObjectReference targetRef = target.getObjectReference();
            Set<LookupRelationship> srcRels = source.getSourceLookupRelationships();
            for (LookupRelationship relationship : srcRels.toArray(new LookupRelationship[srcRels.size()])) {
                boolean found = false;
                for (LookupRelationship check : target.getSourceLookupRelationships()) {
                    if (ObjectUtils.equals(check.getTarget(), relationship.getTarget())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    LookupRelationship copy = copyRelationship(copier, relationship);
                    copy.setSource(targetRef);
                    target.addLookupRelationship(copy);
                }
                if (moveSourceRelationships) {
                    source.removeLookupRelationship(relationship);
                    Lookup other = (Lookup) cache.get(relationship.getTarget());
                    if (other != null) {
                        other.removeLookupRelationship(relationship);
                        service.save(other);
                    }
                }
            }

            Set<LookupRelationship> tgtRels = source.getTargetLookupRelationships();
            for (LookupRelationship relationship : tgtRels.toArray(new LookupRelationship[tgtRels.size()])) {
                boolean found = false;
                for (LookupRelationship check : target.getTargetLookupRelationships()) {
                    if (ObjectUtils.equals(check.getSource(), relationship.getSource())) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    LookupRelationship copy = copyRelationship(copier, relationship);
                    copy.setTarget(targetRef);
                    target.addLookupRelationship(copy);
                }
            }
            if (!moveSourceRelationships) {
                service.save(source);
            }
            result = true;
        }
        return result;
    }

    /**
     * Copies a relationship.
     *
     * @param copier       the copier
     * @param relationship the relationship to copy
     * @return a copy of the relationship
     */
    private LookupRelationship copyRelationship(IMObjectCopier copier, LookupRelationship relationship) {
        List<IMObject> list = copier.apply(relationship);
        if (list.size() != 1) {
            throw new IllegalStateException("Expected 1 object from LookupRelationshipCopyHandler, got: "
                                            + list.size());
        }
        if (!(list.get(0) instanceof LookupRelationship)) {
            throw new IllegalStateException("Got a " + list.get(0).getClass().getName()
                                            + " instead of a LookupRelationship");
        }
        return (LookupRelationship) list.get(0);
    }


    /**
     * An {@link IMObjectCopyHandler} for copying {@link LookupRelationship}s.
     * This copies the relationship, and references the existing source and target objects.
     */
    private static class LookupRelationshipCopyHandler extends MappingCopyHandler {

        /**
         * Constructs a <tt>LookupRelationshipCopyHandler</tt>.
         */
        public LookupRelationshipCopyHandler() {
            setCopy(LookupRelationship.class);
            setDefaultTreatment(Treatment.REFERENCE);
        }
    }


}
