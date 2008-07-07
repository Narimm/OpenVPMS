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

import org.openvpms.component.business.dao.hibernate.im.common.Context;
import org.openvpms.component.business.dao.hibernate.im.document.DocumentDO;
import org.openvpms.component.business.domain.im.act.DocumentAct;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class DocumentActAssembler
        extends AbstractActAssembler<DocumentAct, DocumentActDO> {


    public DocumentActAssembler() {
        super(DocumentAct.class, DocumentActDO.class);
    }

    @Override
    protected void assembleDO(DocumentActDO result, DocumentAct source,
                              Context context) {
        super.assembleDO(result, source, context);
        result.setDocVersion(source.getDocVersion());
        result.setFileName(source.getFileName());
        result.setMimeType(source.getMimeType());
        result.setPrinted(source.isPrinted());
        result.setDocument(get(source.getDocReference(), DocumentDO.class,
                               context));
    }

    protected DocumentAct create(DocumentActDO object) {
        return new DocumentAct();
    }

    protected DocumentActDO create(DocumentAct object) {
        return new DocumentActDO();
    }
}
