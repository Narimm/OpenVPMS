package org.openvpms.component.business.service.security;

import org.junit.Test;
import org.openvpms.component.business.domain.im.security.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.Callable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests the {@link RunAs} class.
 *
 * @author Tim Anderson
 */
public class RunAsTestCase {

    /**
     * Tests the {@link RunAs#run(User, Runnable)} method.
     */
    @Test
    public void testRunRunnable() {
        SecurityContextHolder.getContext().setAuthentication(null);
        final User user = new User();
        RunAs.run(user, new Runnable() {
            @Override
            public void run() {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                assertTrue(authentication instanceof UsernamePasswordAuthenticationToken);
                assertEquals(user, authentication.getPrincipal());
            }
        });

        assertNull(SecurityContextHolder.getContext().getAuthentication());

        // ensure the existing authentication is reverted to on exception
        UsernamePasswordAuthenticationToken existing
                = new UsernamePasswordAuthenticationToken(new Object(), new Object());
        SecurityContextHolder.getContext().setAuthentication(existing);

        try {
            RunAs.run(user, new Runnable() {
                @Override
                public void run() {
                    throw new RuntimeException();
                }
            });
            fail("expected RuntimeException");
        } catch (RuntimeException expected) {
            // do nothing
        }

        assertEquals(existing, SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Tests the {@link RunAs#run(User, Callable)} method.
     *
     * @throws Exception for any error
     */
    @Test
    public void testRunCallable() throws Exception {
        UsernamePasswordAuthenticationToken existing
                = new UsernamePasswordAuthenticationToken(new Object(), new Object());
        SecurityContextHolder.getContext().setAuthentication(existing);

        final User user = new User();
        Callable<String> callable = new Callable<String>() {
            @Override
            public String call() throws Exception {
                Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
                assertTrue(authentication instanceof UsernamePasswordAuthenticationToken);
                assertEquals(user, authentication.getPrincipal());
                return "foo";
            }
        };
        assertEquals("foo", RunAs.run(user, callable));

        assertEquals(existing, SecurityContextHolder.getContext().getAuthentication());

        // ensure the existing authentication is reverted to on exception

        try {
            RunAs.run(user, new Callable<String>() {
                @Override
                public String call() throws Exception {
                    throw new Exception();
                }
            });
            fail("expected Exception");
        } catch (Exception expected) {
            // do nothing
        }

        assertEquals(existing, SecurityContextHolder.getContext().getAuthentication());
    }
}
