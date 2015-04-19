package org.openvpms.component.business.service.security;

import org.openvpms.component.business.domain.im.security.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.concurrent.Callable;

/**
 * Helper methods to invoke functions within a spring {@code SecurityContext} for a particular user.
 *
 * @author Tim Anderson
 */
public class RunAs {

    /**
     * Executes a {@code Runnable} with the spring {@code SecurityContext} set to that of the supplied user.
     *
     * @param user     the user
     * @param runnable the runnable to execute
     */
    public static void run(User user, Runnable runnable) {
        SecurityContext existing = initContext(user);
        try {
            runnable.run();
        } finally {
            SecurityContextHolder.setContext(existing);
        }
    }

    /**
     * Executes a {@code Callable} with the spring {@code SecurityContext} set to that of the supplied user.
     *
     * @param user     the user
     * @param callable the callable to execute
     */
    public static <T> T run(User user, Callable<T> callable) throws Exception {
        SecurityContext existing = initContext(user);
        try {
            return callable.call();
        } finally {
            SecurityContextHolder.setContext(existing);
        }
    }

    /**
     * Initialise the security context.
     *
     * @param user the user
     * @return the existing user
     */
    private static SecurityContext initContext(User user) {
        SecurityContext existing = SecurityContextHolder.getContext();
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        UsernamePasswordAuthenticationToken authentication
                = new UsernamePasswordAuthenticationToken(user, user.getPassword(), user.getAuthorities());
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        return existing;
    }
}
