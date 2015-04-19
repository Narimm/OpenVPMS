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

package org.openvpms.web.component.im.doc;

import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.web.component.im.edit.SaveHelper;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.AbstractAppTest;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;


/**
 * Base class for tests for the {@link DocumentActEditor}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: $
 */
public abstract class AbstractDocumentActEditorTest extends AbstractAppTest {
    /**
     * Creates a new act.
     *
     * @return a new act
     */
    protected abstract DocumentAct createAct();

    /**
     * Creates a new editor.
     *
     * @param act the act to edit
     * @return a new editor
     */
    protected abstract DocumentActEditor createEditor(DocumentAct act);

    /**
     * Helper to invoke save on an editor in a transaction.
     *
     * @param editor the editor
     * @return <tt>true</tt> if the save was successful, otherwise <tt>false</tt>
     */
    protected boolean save(DocumentActEditor editor) {
        return SaveHelper.save(editor);
    }

    /**
     * Helper to invoke delete on an editor in a transaction.
     *
     * @param editor the editor
     * @return <tt>true</tt> if the delete was successful, otherwise <tt>false</tt>
     */
    protected boolean delete(final DocumentActEditor editor) {
        TransactionTemplate txn = new TransactionTemplate(
            ServiceHelper.getTransactionManager());
        return txn.execute(new TransactionCallback<Boolean>() {
            public Boolean doInTransaction(TransactionStatus status) {
                return editor.delete();
            }
        });
    }

    /**
     * Helper to create an image document.
     *
     * @return a new image document
     */
    protected Document createImage() {
        String name = "/org/openvpms/web/resource/image/openvpms.gif";
        return createDocument(name);
    }

    /**
     * Creates a document from a resource.
     *
     * @param path the resource path
     * @return a new document
     */
    protected Document createDocument(String path) {
        return DocumentTestHelper.createDocument(path);
    }

    /**
     * Creates a new <em>entity.documentTemplate</em>, associated with an <em>act.documentTemplate</em>.
     *
     * @param archetype the archetype the template is associated with
     * @return a new template
     */
    protected Entity createDocumentTemplate(String archetype) {
        return DocumentTestHelper.createDocumentTemplate(archetype);
    }

}
