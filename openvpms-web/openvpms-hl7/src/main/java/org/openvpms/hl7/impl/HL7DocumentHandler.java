package org.openvpms.hl7.impl;

import org.openvpms.archetype.rules.doc.AbstractDocumentHandler;
import org.openvpms.archetype.rules.doc.DocumentException;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.zip.CRC32;

import static org.openvpms.archetype.rules.doc.DocumentException.ErrorCode.ReadError;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
class HL7DocumentHandler extends AbstractDocumentHandler {

    private static final Charset CHARSET = Charset.forName("UTF-8");


    public HL7DocumentHandler(IArchetypeService service) {
        super("document.HL7", service);
    }

    public Document create(String name, String encoded, String mimeType) {
        byte[] bytes = encoded.getBytes(CHARSET);
        return create(name, bytes, mimeType, bytes.length);
    }

    public String getStringContent(Document document) {
        byte[] bytes = document.getContents();
        return new String(bytes, CHARSET);
    }

    /**
     * Creates a new {@link Document} from a stream.
     *
     * @param name     the document name. Any path information is removed.
     * @param stream   a stream representing the document content
     * @param mimeType the mime type of the content. May be <code>null</code>
     * @param size     the size of stream, or <tt>-1</tt> if the size is not
     *                 known
     * @return a new document
     * @throws DocumentException         if the document can't be created
     * @throws ArchetypeServiceException for any archetype service error
     */
    @Override
    public Document create(String name, InputStream stream, String mimeType, int size) {
        byte[] buffer = new byte[1024];
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        int read = 0;
        int length;
        CRC32 checksum = new CRC32();
        try {
            while ((length = stream.read(buffer)) != -1) {
                checksum.update(buffer, 0, length);
                output.write(buffer, 0, length);
                read += length;
            }
            if (size != -1 && read != size) {
                throw new DocumentException(ReadError, name);
            }
            output.close();
        } catch (IOException exception) {
            throw new DocumentException(ReadError, exception, name);
        }
        byte[] data = output.toByteArray();
        return create(name, data, mimeType, size, checksum.getValue());
    }

    /**
     * Returns the document content as a stream.
     *
     * @param document the document
     * @return the document content
     */
    @Override
    public InputStream getContent(Document document) {
        return new ByteArrayInputStream(document.getContents());
    }
}
