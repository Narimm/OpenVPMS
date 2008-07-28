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

package org.openvpms.component.business.dao.hibernate.im.document;

import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.common.DOState;
import org.openvpms.component.business.dao.hibernate.im.common.IMObjectAssembler;
import org.openvpms.component.business.domain.im.document.Document;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentAssembler extends IMObjectAssembler<Document, DocumentDO> {

    public DocumentAssembler() {
        super(Document.class, DocumentDO.class, DocumentDOImpl.class);
    }

    @Override
    protected void assembleDO(DocumentDO target, Document source,
                              DOState state, Context context) {
        super.assembleDO(target, source, state, context);
        target.setChecksum(source.getChecksum());
        target.setContents(source.getContents());
        target.setDocSize(source.getDocSize());
        target.setMimeType(source.getMimeType());
    }

    @Override
    protected void assembleObject(Document target, DocumentDO source,
                                  Context context) {
        super.assembleObject(target, source, context);
        target.setChecksum(source.getChecksum());
        target.setContents(source.getContents());
        target.setDocSize(source.getDocSize());
        target.setMimeType(source.getMimeType());
    }

    protected Document create(DocumentDO object) {
        return new Document();
    }

    protected DocumentDO create(Document object) {
        return new DocumentDOImpl();
    }
}
