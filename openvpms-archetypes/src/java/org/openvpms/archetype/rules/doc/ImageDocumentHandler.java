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
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.doc;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
import javax.imageio.stream.ImageInputStream;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

/**
 * A handler for image documents.
 * <p/>
 * This supports .png, .jpg, .gif, and .bmp images.
 *
 * @author Tim Anderson
 */
public class ImageDocumentHandler extends AbstractDocumentHandler {

    /**
     * Supported mime types.
     */
    private static final String MIME_TYPES[] = {"image/png", "image/x-png", "image/jpeg", "image/gif", "image/bmp"};

    /**
     * Supported suffixes.
     */
    private static final String SUFFIXES[] = {"png", "jpg", "jpeg", "gif", "bmp"};

    /**
     * Constructs an {@link ImageDocumentHandler}.
     *
     * @param service the archetype service
     */
    public ImageDocumentHandler(IArchetypeService service) {
        super(DocumentArchetypes.IMAGE_DOCUMENT, service);
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
        if (name == null) {
            throw new IllegalArgumentException("Argument 'name' may not be null");
        }
        String ext = FilenameUtils.getExtension(name);
        if (ext.length() > 0) {
            for (String suffix : SUFFIXES) {
                if (suffix.equalsIgnoreCase(ext)) {
                    return true;
                }
            }
        }
        if (!StringUtils.isEmpty(mimeType)) {
            for (String mime : MIME_TYPES) {
                if (mime.equalsIgnoreCase(mimeType)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Creates a new {@link Document} from a stream.
     *
     * @param name     the document name. Any path information is removed.
     * @param stream   a stream representing the document content
     * @param mimeType the mime type of the content. May be {@code null}
     * @param size     the size of stream, or {@code -1} if the size is not known
     * @return a new document
     * @throws DocumentException         if the document can't be created
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public Document create(String name, InputStream stream, String mimeType, int size) {
        int width = -1;
        int height = -1;
        Document document;
        InputStream buffered = new ReusableInputStream(stream);
        try (ImageInputStream in = ImageIO.createImageInputStream(buffered)) {
            final Iterator<ImageReader> readers = ImageIO.getImageReaders(in);
            if (readers.hasNext()) {
                ImageReader reader = readers.next();
                try {
                    reader.setInput(in);
                    width = reader.getWidth(0);
                    height = reader.getHeight(0);
                } finally {
                    reader.dispose();
                }
            }
            buffered.reset();
            buffered.mark(0); // don't want to buffer any longer
            document = super.create(name, buffered, mimeType, size);
        } catch (IOException exception) {
            throw new DocumentException(DocumentException.ErrorCode.ReadError, exception);
        }
        if (width != -1 && height != -1) {
            IMObjectBean bean = new IMObjectBean(document, getService());
            bean.setValue("width", width);
            bean.setValue("height", height);
        }
        return document;
    }

    private static class ReusableInputStream extends BufferedInputStream {

        public ReusableInputStream(InputStream in) {
            super(in);
            mark(Integer.MAX_VALUE);
        }

        @Override
        public void close() throws IOException {
            super.reset();
        }
    }
}
