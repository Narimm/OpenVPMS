package org.openvpms.webdav.milton;

import io.milton.http.LockInfo;
import io.milton.http.LockTimeout;
import io.milton.http.LockToken;
import org.junit.Test;
import org.openvpms.web.webdav.milton.LockHelper;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link LockHelperTestCase}.
 *
 * @author Tim Anderson
 */
public class LockHelperTestCase {

    /**
     * Tests the {@link LockHelper#serialiseLockResponse(LockToken, String)} method.
     */
    @Test
    public void testSerialiseLockResponse() {
        String expected =
                "<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n"
                + "<D:prop xmlns:D=\"DAV:\">"
                + "<D:lockdiscovery>"
                + "<D:activelock>"
                + "<D:locktype><D:write/></D:locktype>"
                + "<D:lockscope><D:exclusive/></D:lockscope>"
                + "<D:depth>0</D:depth><D:owner>admin</D:owner>"
                + "<D:timeout>Second-60</D:timeout>"
                + "<D:locktoken><D:href>opaquelocktoken:1234</D:href></D:locktoken>"
                + "<D:lockroot><D:href>http://localhost:8080/openvpms/documents/foo.doc</D:href></D:lockroot>"
                + "</D:activelock>"
                + "</D:lockdiscovery>"
                + "</D:prop>";
        LockInfo info = new LockInfo(LockInfo.LockScope.EXCLUSIVE, LockInfo.LockType.WRITE, "admin",
                                     LockInfo.LockDepth.ZERO);
        LockToken token = new LockToken("1234", info, new LockTimeout(60L));
        String lock = LockHelper.serialiseLockResponse(token, "http://localhost:8080/openvpms/documents/foo.doc");
        assertEquals(expected, lock);
    }
}
