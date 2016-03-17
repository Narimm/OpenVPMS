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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.doc;

import org.openvpms.archetype.rules.doc.DefaultDocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentArchetypes;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.archetype.rules.doc.DocumentHandler;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.rule.IArchetypeRuleService;
import org.openvpms.component.system.common.exception.OpenVPMSException;
import org.openvpms.report.DocFormats;
import org.openvpms.report.jasper.JRXMLDocumentHandler;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.property.Modifiable;
import org.openvpms.web.component.property.ModifiableListener;
import org.openvpms.web.system.ServiceHelper;

import java.io.InputStream;


/**
 * Editor for <em>entity.documentTemplateEmail</em> entities.
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

    private static class EmailDocumentHandler implements DocumentHandler {

        private static final String[] SUPPORTED_MIME_TYPES = {DocFormats.ODT_TYPE, DocFormats.DOC_TYPE,
                                                              DocFormats.HTML_TYPE, DocFormats.RTF_TYPE};

        private static final String[] SUPPORTED_EXTENSIONS = {DocFormats.ODT_EXT, DocFormats.DOC_EXT,
                                                              DocFormats.HTML_EXT, DocFormats.JRXML_EXT,
                                                              DocFormats.RTF_EXT};

        private final DefaultDocumentHandler handler;
        private final JRXMLDocumentHandler jrxmlHandler;

        public EmailDocumentHandler() {
            IArchetypeRuleService service = ServiceHelper.getArchetypeService();
            handler = new DefaultDocumentHandler(DocumentArchetypes.DEFAULT_DOCUMENT, service);
            jrxmlHandler = new JRXMLDocumentHandler(service);
        }

        /**
         * Determines if this handler supports a document.
         *
         * @param name     the document name
         * @param mimeType the mime type of the document. May be {@code null}
         * @return {@code true} if this handler supports the document
         */
        @Override
        public boolean canHandle(String name, String mimeType) {
            name = name.toLowerCase();
            return isSupportedExtension(name) || (mimeType != null && isSupportedMimeType(mimeType));
        }

        /**
         * Determines if this handler supports a document.
         *
         * @param name      the document name
         * @param shortName the document archetype short name
         * @param mimeType  the mime type of the document. May be {@code null}
         * @return {@code true} if this handler supports the document
         */
        @Override
        public boolean canHandle(String name, String shortName, String mimeType) {
            return DocumentArchetypes.DEFAULT_DOCUMENT.equals(shortName) && canHandle(name, mimeType);
        }

        /**
         * Determines if this handler supports a document.
         *
         * @param document the document
         * @return {@code true} if this handler supports the document
         */
        @Override
        public boolean canHandle(Document document) {
            return DocumentArchetypes.DEFAULT_DOCUMENT.equals(document.getArchetypeId().getShortName());
        }

        /**
         * Creates a new {@link Document} from a stream.
         *
         * @param name     the document name. Any path information is removed.
         * @param stream   a stream representing the document content
         * @param mimeType the mime type of the document. May be {@code null}
         * @param size     the size of stream, or {@code -1} if the size is not known
         * @return a new document
         * @throws DocumentException         if the document can't be created
         * @throws ArchetypeServiceException for any archetype service error
         * @throws OpenVPMSException         for any other error
         */
        @Override
        public Document create(String name, InputStream stream, String mimeType, int size) {
            return getHandler(name).create(name, stream, mimeType, size);
        }

        /**
         * Updates a {@link Document} from a stream.
         *
         * @param document the document to update
         * @param stream   a stream representing the new document content
         * @param mimeType the mime type of the document. May be {@code null}
         * @param size     the size of stream, or {@code -1} if the size is not known
         */
        @Override
        public void update(Document document, InputStream stream, String mimeType, int size) {
            getHandler(document.getName()).update(document, stream, mimeType, size);
        }

        /**
         * Returns the document content as a stream.
         *
         * @param document the document
         * @return the document content
         * @throws DocumentException for any error
         */
        @Override
        public InputStream getContent(Document document) {
            return getHandler(document.getName()).getContent(document);
        }

        private DocumentHandler getHandler(String name) {
            return (isJRXML(name)) ? jrxmlHandler : handler;
        }

        private boolean isJRXML(String name) {
            return name.toLowerCase().endsWith(DocFormats.JRXML_EXT);
        }

        private boolean isSupportedExtension(String name) {
            for (String ext : SUPPORTED_EXTENSIONS) {
                if (name.endsWith(ext)) {
                    return true;
                }
            }
            return false;
        }

        private boolean isSupportedMimeType(String mimeType) {
            for (String type : SUPPORTED_MIME_TYPES) {
                if (type.equalsIgnoreCase(mimeType)) {
                    return true;
                }
            }
            return false;
        }
    }
}