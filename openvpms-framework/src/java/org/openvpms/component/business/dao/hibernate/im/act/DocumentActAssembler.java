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

package org.openvpms.component.business.dao.hibernate.im.act;

import org.openvpms.component.business.dao.hibernate.im.common.Assembler;
import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.dao.hibernate.im.common.DeferredAssembler;
import org.openvpms.component.business.dao.hibernate.im.common.ReferenceUpdater;
import org.openvpms.component.business.dao.hibernate.im.document.DocumentDO;
import org.openvpms.component.business.dao.hibernate.im.document.DocumentDOImpl;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.IMObjectReference;


/**
 * An {@link Assembler} responsible for assembling {@link DocumentActDO}
 * instances from {@link DocumentAct}s and vice-versa.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentActAssembler
        extends AbstractActAssembler<DocumentAct, DocumentActDO> {

    /**
     * Creates a new <tt>DocumentActAssembler</tt>.
     */
    public DocumentActAssembler() {
        super(DocumentAct.class, DocumentActDO.class, DocumentActDOImpl.class);
    }

    /**
     * Assembles a data object from an object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param state   the data object state
     * @param context the assembly context
     */
    @Override
    protected void assembleDO(DocumentActDO target, DocumentAct source,
                              DOState state, Context context) {
        super.assembleDO(target, source, state, context);
        target.setDocVersion(source.getDocVersion());
        target.setFileName(source.getFileName());
        target.setMimeType(source.getMimeType());
        target.setPrinted(source.isPrinted());
        assembleDoc(target, source, state, context);
    }

    /**
     * Assembles an object from a data object.
     *
     * @param target  the object to assemble
     * @param source  the object to assemble from
     * @param context the assembly context
     */
    @Override
    protected void assembleObject(DocumentAct target, DocumentActDO source,
                                  Context context) {
        super.assembleObject(target, source, context);
        target.setDocVersion(source.getDocVersion());
        target.setFileName(source.getFileName());
        target.setMimeType(source.getMimeType());
        target.setPrinted(source.isPrinted());
        DocumentDO document = source.getDocument();
        IMObjectReference ref = (document != null)
                ? document.getObjectReference() : null;
        target.setDocument(ref);
    }

    /**
     * Creates a new object.
     *
     * @param object the source data object
     * @return a new object corresponding to the supplied data object
     */
    protected DocumentAct create(DocumentActDO object) {
        return new DocumentAct();
    }

    /**
     * Creates a new data object.
     *
     * @param object the source object
     * @return a new data object corresponding to the supplied object
     */
    protected DocumentActDO create(DocumentAct object) {
        return new DocumentActDOImpl();
    }

    /**
     * Assembles the document of a document act.
     *
     * @param target  the target act
     * @param source  the source act
     * @param state   the target state
     * @param context the assembly context
     */
    private void assembleDoc(final DocumentActDO target,
                             final DocumentAct source,
                             DOState state, Context context) {
        final IMObjectReference ref = source.getDocument();
        if (ref != null) {
            DOState docDO = get(ref, DocumentDO.class, DocumentDOImpl.class,
                                context);
            if (docDO != null) {
                target.setDocument((DocumentDO) docDO.getObject());
                state.addState(docDO);
            } else {
                new DeferredAssembler(state, ref) {
                    protected void doAssemble(Context context) {
                        target.setDocument(
                                load(ref, DocumentDO.class,
                                     DocumentDOImpl.class, context));
                    }
                };
            }
            if (ref.isNew()) {
                new ReferenceUpdater(state, ref) {
                    protected void doUpdate(IMObjectReference updated) {
                        source.setDocument(updated);
                    }
                };
            }
        } else {
            target.setDocument(null);
        }
    }
}
