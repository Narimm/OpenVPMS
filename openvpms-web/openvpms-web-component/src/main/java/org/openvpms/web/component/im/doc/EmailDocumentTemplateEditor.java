/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.doc;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.system.ServiceHelper;


/**
 * Editor for <em>entity.documentTemplateEmail*</em> entities.
 *
 * @author Tim Anderson
 */
public class EmailDocumentTemplateEditor extends AbstractDocumentTemplateEditor {

    /**
     * Document content type.
     */
    protected static final String DOCUMENT_CONTENT = "DOCUMENT";

    /**
     * The content type node name.
     */
    private static final String CONTENT_TYPE = "contentType";

    /**
     * Constructs a {@link EmailDocumentTemplateEditor}.
     *
     * @param template the object to edit
     * @param parent   the parent object. May be {@code null}
     * @param context  the layout context. May be {@code null}
     * @throws ArchetypeServiceException for any archetype service error
     */
    public EmailDocumentTemplateEditor(Entity template, IMObject parent, LayoutContext context) {
        super(template, parent, false, new EmailDocumentHandler(), context);
        updateDocumentState();
        disableMacroExpansion("subject");
        disableMacroExpansion("subjectSource");
        disableMacroExpansion("content");
        disableMacroExpansion("contentSource");
        getProperty(CONTENT_TYPE).addModifiableListener(new ModifiableListener() {
            @Override
            public void modified(Modifiable modifiable) {
                onContentTypeChanged();
            }
        });

    }

    /**
     * Returns the content type.
     *
     * @return the content type. May be {@code null}
     */
    protected String getContentType() {
        return getProperty(CONTENT_TYPE).getString();
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        return new EmailDocumentTemplateLayoutStrategy(getSelector());
    }

    /**
     * Save any edits.
     * <p/>
     * This uses {@link #saveChildren()} to save the children prior to invoking {@link #saveObject()}.
     *
     * @throws OpenVPMSException if the save fails
     */
    @Override
    protected void doSave() {
        super.doSave();
    }

    /**
     * Invoked when the content type changes.
     * <p/>
     * This changes the layout to switch between document and text content.
     */
    private void onContentTypeChanged() {
        String type = getContentType();
        if (type != null) {
            // NOTE: select fields deselect the current value before selecting the new one, so need to check to
            // see if a value is present before changing layout
            updateDocumentState();
            onLayout();
        }
    }

    /**
     * Invoked to update how the document is handled.
     * <p/>
     * If the content type is DOCUMENT, then the document is required, otherwise it will be deleted on save.
     */
    private void updateDocumentState() {
        boolean isDocument = DOCUMENT_CONTENT.equals(getContentType());
        setDocumentRequired(isDocument);
        setDeleteDocument(!isDocument);
    }

    private static class EmailDocumentHandler extends SupportedContentDocumentHandler {

        private static final String[] SUPPORTED_EXTENSIONS = {DocFormats.ODT_EXT, DocFormats.DOC_EXT,
                                                              DocFormats.HTML_EXT, DocFormats.JRXML_EXT,
                                                              DocFormats.RTF_EXT};

        private static final String[] SUPPORTED_MIME_TYPES = {DocFormats.ODT_TYPE, DocFormats.DOC_TYPE,
                                                              DocFormats.HTML_TYPE, DocFormats.RTF_TYPE};


        public EmailDocumentHandler() {
            super(SUPPORTED_EXTENSIONS, SUPPORTED_MIME_TYPES, ServiceHelper.getArchetypeService());
        }
    }
}