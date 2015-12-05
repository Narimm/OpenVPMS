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
     * @param token the lock token
     * @param uri   the URL that the lock applies to
     * @return the serialised token
     */
    public static String serialise(LockToken token, String uri) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XmlWriter writer = new XmlWriter(out);
        writer.writeXMLHeader();
        writer.open("D:prop  xmlns:D=\"DAV:\"");
        writer.newLine();
        writer.open("D:lockdiscovery");
        writer.newLine();
        writer.open("D:activelock");
        writer.newLine();
        writer.writeProperty(null, "D:locktype", "<D:" + token.info.type.toString().toLowerCase() + "/>");
        writer.writeProperty(null, "D:lockscope", "<D:" + token.info.scope.toString().toLowerCase() + "/>");
        writer.writeProperty(null, "D:depth", getDepth(token.info.depth));
        writer.writeProperty(null, "D:owner", token.info.lockedByUser);
        Long seconds = token.timeout.getSeconds();
        if (seconds != null && seconds > 0) {
            writer.writeProperty(null, "D:timeout", "Second-" + seconds);
        }
        XmlWriter.Element lockToken = writer.begin("D:locktoken").open();
        writer.writeProperty(null, "D:href", "opaquelocktoken:" + token.tokenId);
        lockToken.close();

        XmlWriter.Element lockRoot = writer.begin("D:lockroot").open();
        writer.writeProperty(null, "D:href", uri);
        lockRoot.close();

        writer.close("D:activelock");
        writer.close("D:lockdiscovery");
        writer.close("D:prop");
        writer.flush();
        return out.toString();
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
