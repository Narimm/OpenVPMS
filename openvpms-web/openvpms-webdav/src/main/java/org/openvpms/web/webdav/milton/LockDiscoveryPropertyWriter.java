package org.openvpms.web.webdav.milton;

import io.milton.http.LockToken;
import io.milton.http.XmlWriter;
import io.milton.http.values.ValueWriter;

import java.util.Map;

/**
 * Used to serialise {@code LockToken} in PROPFIND requests.
 *
 * @author Tim Anderson
 */
class LockDiscoveryPropertyWriter implements ValueWriter {

    /**
     * Determines if this supports a property.
     *
     * @param namespaceURI the namespace URI
     * @param localName    the property name
     * @param valueClass   the value class
     * @return {@code true} if {@code valueClass} is a
     */
    @Override
    public boolean supports(String namespaceURI, String localName, Class valueClass) {
        return "lockdiscovery".equals(localName) && LockToken.class.isAssignableFrom(valueClass);
    }

    /**
     * Writes the value out to XML using the given XmlWriter.
     *
     * @param writer       the writer to serialise to
     * @param namespaceURI the namespace URI
     * @param prefix       the namespace prefix
     * @param localName    the property name
     * @param value        the value to write
     * @param href         the URL of of the resource
     * @param nsPrefixes   namespace prefixes
     */
    @Override
    public void writeValue(XmlWriter writer, String namespaceURI, String prefix, String localName, Object value, String href,
                           Map<String, String> nsPrefixes) {
        LockHelper.writeLockDiscovery((LockToken) value, prefix, href, writer);
    }

    /**
     * Parse the given textual representation, probably from a PROPPATCH request.
     *
     * @param namespaceURI the namespace URI
     * @param localName    the property name
     * @param value        the value to parse
     * @return the parsed value
     */
    @Override
    public Object parse(String namespaceURI, String localName, String value) {
        throw new UnsupportedOperationException();
    }
}
