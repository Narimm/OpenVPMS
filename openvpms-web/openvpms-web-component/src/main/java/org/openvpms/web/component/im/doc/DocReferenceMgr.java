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
 */

package org.openvpms.web.component.im.doc;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.ArchetypeServiceHelper;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.component.im.util.IMObjectHelper;

import java.util.LinkedList;


/**
 * Helper to manage a set of document references.
 *
 * @author Tim Anderson
 */
class DocReferenceMgr {

    /**
     * The original reference.
     */
    private final IMObjectReference original;

    /**
     * The context.
     */
    private final Context context;

    /**
     * The set of references to manage.
     */
    private LinkedList<IMObjectReference> references = new LinkedList<IMObjectReference>();

    /**
     * Constructs a {@code DocReferenceMgr}.
     *
     * @param original the original reference. May be {@code null}
     * @param context  the context
     */
    public DocReferenceMgr(IMObjectReference original, Context context) {
        if (original != null) {
            references.add(original);
        }
        this.original = original;
        this.context = context;
    }

    /**
     * Adds a new document reference.
     *
     * @param reference the reference to add
     */
    public void add(IMObjectReference reference) {
        references.add(reference);
    }

    /**
     * Removes a document reference.
     *
     * @param reference the reference to remove
     */
    public void remove(IMObjectReference reference) {
        references.remove(reference);
    }

    /**
     * Commits the changes. Every document bar the most recent will be removed.
     *
     * @throws ArchetypeServiceException for any error
     */
    public void commit() {
        while (references.size() > 1) {
            delete(references.removeFirst());
        }
    }

    /**
     * Rolls back the changes. Every document bar the original will be removed.
     *
     * @throws ArchetypeServiceException for any error
     */
    public void rollback() {
        while (references.size() > 1) {
            IMObjectReference reference = references.removeLast();
            delete(reference);
        }
        if (references.size() == 1 && original != null && !references.get(0).equals(original)) {
            delete(references.removeLast());
        }
    }

    /**
     * Deletes all documents.
     *
     * @throws ArchetypeServiceException for any error
     */
    public void delete() {
        while (!references.isEmpty()) {
            delete(references.removeLast());
        }
    }

    /**
     * Removes the document associated with a document reference.
     *
     * @param reference the reference of the document to remove
     * @throws ArchetypeServiceException for any error
     */
    private void delete(IMObjectReference reference) {
        IMObject object = IMObjectHelper.getObject(reference, context);
        if (object != null) {
            ArchetypeServiceHelper.getArchetypeService().remove(object);
        }
    }

}
