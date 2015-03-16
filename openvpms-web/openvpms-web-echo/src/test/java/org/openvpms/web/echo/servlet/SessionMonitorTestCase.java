package org.openvpms.web.echo.servlet;

import nextapp.echo2.app.Window;
import nextapp.echo2.webrender.Connection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.openvpms.archetype.test.ArchetypeServiceTest;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.web.echo.spring.SpringApplicationInstance;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

/**
 * Tests the {@link SessionMonitor}.
 *
 * @author Tim Anderson
 */
public class SessionMonitorTestCase extends ArchetypeServiceTest {

    /**
     * The session.
     */
    private HttpSession session;

    /**
     * The http request.
     */
    private HttpServletRequest request;

    /**
     * The logged in user.
     */
    private UsernamePasswordAuthenticationToken auth;

    /**
     * The session monitor.
     */
    private SessionMonitor monitor;


    /**
     * Sets up the test case.
     */
    @Before
    public void setUp() {
        session = mock(HttpSession.class);
        request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(session);
        final Connection connection = mock(Connection.class);
        when(connection.getRequest()).thenReturn(request);
        final User admin = TestHelper.createUser("admin", false);
        auth = new UsernamePasswordAuthenticationToken(admin, null);

        monitor = new SessionMonitor() {
            @Override
            protected Connection getConnection() {
                return connection;
            }

            @Override
            protected Authentication getAuthentication() {
                return auth;
            }
        };
    }

    /**
     * Tests session locking.
     *
     * @throws Exception for any error
     */
    @Test
    public void testLock() throws Exception {
        SpringApplicationInstance app = new SpringApplicationInstance() {
            @Override
            public void lock() {
            }

            @Override
            public void unlock() {
            }

            @Override
            public Window init() {
                return null;
            }
        };
        monitor.setAutoLockMS(500);
        monitor.addSession(session);
        monitor.active(request, auth);
        monitor.newApplication(app, session);
        assertFalse(monitor.isLocked(session));
        Thread.sleep(1000);
        assertTrue(monitor.isLocked(session));
        monitor.active(request, auth);
        monitor.unlock();
        assertFalse(monitor.isLocked(session));
    }

    /**
     * Tests session logout.
     *
     * @throws Exception for any error
     */
    @Test
    public void testAutoLogout() throws Exception {
        monitor.setAutoLockMS(250);
        monitor.setAutoLogoutMS(500);
        monitor.addSession(session);
        monitor.active(request, auth);
        assertFalse(monitor.isLocked(session));
        Mockito.verify(session, times(0)).invalidate();
        Thread.sleep(750);
        Mockito.verify(session, times(1)).invalidate();
    }
}
