package org.openvpms.web.webdav.milton;

import io.milton.http.LockInfo;
import io.milton.http.LockToken;
import io.milton.http.XmlWriter;

import java.io.ByteArrayOutputStream;

/**
 * WebDAV lock helper methods.
 *
 * @author Tim Anderson
 */
public class LockHelper {

    /**
     * Serialises a lock token to XML.
     *
     * @param lock the lock token
     * @param uri  the URL that the lock applies to
     * @return the serialised token
     */
    public static String serialiseLockResponse(LockToken lock, String uri) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XmlWriter writer = new XmlWriter(out);
        writer.writeXMLHeader();
        String prefix = "D";
        writer.open(prefix, "prop xmlns:D=\"DAV:\"");
        writer.newLine();
        writeLockDiscovery(lock, prefix, uri, writer);
        writer.close(prefix, "prop");
        writer.flush();
        return out.toString();
    }

    /**
     * Writes a D:lockdiscovery element to a {@code XmlWriter}
     *
     * @param lock   the lock to write. May be {@code null}
     * @param uri    the URL that the lock applies to
     * @param writer the writer to write to
     */
    public static void writeLockDiscovery(LockToken lock, String prefix, String uri, XmlWriter writer) {
        writer.open(prefix, "lockdiscovery");
        if (lock != null) {
            writer.open(prefix, "activelock");
            writer.writeProperty(prefix, "locktype",
                                 "<" + prefix + ":" + lock.info.type.toString().toLowerCase() + "/>");
            writer.writeProperty(prefix, "lockscope",
                                 "<" + prefix + ":" + lock.info.scope.toString().toLowerCase() + "/>");
            writer.writeProperty(prefix, "depth", getDepth(lock.info.depth));
            writer.writeProperty(prefix, "owner", lock.info.lockedByUser);
            Long seconds = lock.timeout.getSeconds();
            if (seconds != null && seconds > 0) {
                writer.writeProperty(prefix, "timeout", "Second-" + seconds);
            }
            XmlWriter.Element lockToken = writer.begin(prefix, "locktoken").open();
            writer.writeProperty(prefix, "href", "opaquelocktoken:" + lock.tokenId);
            lockToken.close();

            XmlWriter.Element lockRoot = writer.begin(prefix, "lockroot").open();
            writer.writeProperty(prefix, "href", uri);
            lockRoot.close();

            writer.close(prefix, "activelock");
        }
        writer.close(prefix, "lockdiscovery");
    }

    /**
     * Parses a lock token from an if: header.
     *
     * @param header the if: header
     * @return the lock token
     */
    public static String parseToken(String header) {
        String result = header;
        int index = result.indexOf(":");
        if (index >= 0) {
            result = result.substring(index + 1);
            index = result.indexOf(">");
            if (index >= 0) {
                result = result.substring(0, index);
            }
        }
        return result;
    }

    /**
     * Converts a lock depth to a string.
     *
     * @param depth the depth. May be {@code null}
     * @return the converted value
     */
    private static String getDepth(LockInfo.LockDepth depth) {
        String result = "Infinity";
        if (depth != null && depth == LockInfo.LockDepth.ZERO) {
            result = "0";
        }
        return result;
    }
}
