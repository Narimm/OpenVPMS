/*
 *  Version: 1.0
 *
 *  The contents of this file are subject to the OpenVPMS License Version
 *  1.0 (the 'License'); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *  http://www.openvpms.org/license/
 *
 *  Software distributed under the License is distributed on an 'AS IS' basis,
 *  WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 *  for the specific language governing rights and limitations under the
 *  License.
 *
 *  Copyright 2006 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.web.component.app;

/**
 * Local context.
 * <p/>
 * This can inherit context information from a parent context, but any changes are kept lcoally.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LocalContext extends DelegatingContext {

    /**
     * Constructs a <tt>LocalContext</tt>, with no inheritance.
     */
    public LocalContext() {
        this(null);
    }

    /**
     * Constructs a new <tt>LocalContext</tt> that inherits values
     * from the specified context. If the specified context is <tt>null</tt>
     * then no inheritance occurs.
     *
     * @param parent the parent context. May be <tt>null</tt>
     */
    public LocalContext(Context parent) {
        super(new DefaultContext(), parent);
    }

    /**
     * Returns the parent context.
     *
     * @return the parent context. May be <tt>null</tt>
     */
    public Context getParent() {
        return getFallback();
    }

    private static final class DefaultContext extends AbstractContext {

    }

}
