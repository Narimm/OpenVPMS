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
 * Copyright 2016 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.function.user;

import org.apache.commons.jxpath.Function;
import org.apache.commons.jxpath.Functions;
import org.apache.commons.jxpath.JXPathContext;
import org.apache.commons.jxpath.Variables;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.archetype.rules.practice.PracticeService;
import org.openvpms.archetype.rules.user.UserArchetypes;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.component.system.common.jxpath.AbstractObjectFunctions;
import org.openvpms.component.system.common.jxpath.JXPathHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.ObjectRefSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;

/**
 * User reporting functions.
 *
 * @author Tim Anderson
 */
public class UserFunctions extends AbstractObjectFunctions {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The practice service.
     */
    private final PracticeService practiceService;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * Functions that may be invoked by the expressions.
     */
    private final Functions functions;

    /**
     * The user functions.
     */
    private static final Log log = LogFactory.getLog(UserFunctions.class);

    /**
     * Username node.
     */
    private static final String USERNAME = "username";

    /**
     * Title node.
     */
    private static final String TITLE = "title";

    /**
     * First name node.
     */
    private static final String FIRST_NAME = "firstName";

    /**
     * Last name node.
     */
    private static final String LAST_NAME = "lastName";

    /**
     * Qualifications node.
     */
    private static final String QUALIFICATIONS = "qualifications";

    /**
     * Name node.
     */
    private static final String NAME = "name";

    /**
     * Description node.
     */
    private static final String DESCRIPTION = "description";

    /**
     * Constructs an {@link UserFunctions}.
     *
     * @param service         the archetype service
     * @param practiceService the practice service
     * @param lookups         the lookup service
     * @param functions       functions that may be invoked by the expressions
     */
    public UserFunctions(IArchetypeService service, PracticeService practiceService, ILookupService lookups,
                         Functions functions) {
        super("user");
        setObject(this);
        this.service = service;
        this.practiceService = practiceService;
        this.lookups = lookups;
        this.functions = functions;
    }

    /**
     * Formats the name of a user, according to the specified style.
     *
     * @param user  the user. May be {@code null}
     * @param style the style. One of 'short', 'medium' or 'long'
     * @return the formatted name, or {@code null} if no user is specified
     */
    public String format(User user, String style) {
        String result = null;
        if (user != null) {
            if ("short".equalsIgnoreCase(style)) {
                result = formatName(user, "shortUserNameFormat");
            } else if ("medium".equalsIgnoreCase(style)) {
                result = formatName(user, "mediumUserNameFormat");
            } else {
                result = formatName(user, "longUserNameFormat");
            }
        }
        return result;
    }

    /**
     * Formats the name of a user, according to the specified style.
     *
     * @param id    the user id
     * @param style the style. One of 'short', 'medium' or 'long'
     * @return the formatted name, or {@code null} if no user can be found with the id
     */
    public String formatById(long id, String style) {
        User user = getUser(id);
        return user != null ? format(user, style) : null;
    }

    /**
     * Returns a Function, if any, for the specified namespace, name and parameter types.
     * <p/>
     * This version changes format -> formatById if the first parameter is numeric.
     * <br/>
     * This is required as JXPath thinks the methods are ambiguous if they have the same name.
     *
     * @param namespace  if it is not the namespace specified in the constructor, the method returns null
     * @param name       is a function name.
     * @param parameters the function parameters
     * @return a MethodFunction, or null if there is no such function.
     */
    @Override
    public Function getFunction(String namespace, String name, Object[] parameters) {
        if ("format".equals(name) && parameters.length >= 1 && parameters[0] instanceof Number) {
            name = "formatById";
        }
        return super.getFunction(namespace, name, parameters);
    }

    /**
     * Formats a user name using the lookup.userNameFormat associated with the specified practice node.
     *
     * @param user the user to format
     * @param node the node
     * @return the formatted name
     */
    private String formatName(final User user, String node) {
        String result = null;
        Party practice = practiceService.getPractice();
        if (practice != null) {
            Lookup lookup = lookups.getLookup(practice, node);
            if (lookup != null) {
                IMObjectBean bean = new IMObjectBean(lookup, service);
                String expression = bean.getString("expression");
                if (expression != null) {
                    try {
                        IMObjectBean userBean = new IMObjectBean(user, service);
                        JXPathContext context = JXPathHelper.newContext(user, functions);
                        Variables variables = context.getVariables();
                        variables.declareVariable(USERNAME, userBean.getValue(USERNAME));
                        variables.declareVariable(TITLE, lookups.getName(user, TITLE));
                        variables.declareVariable(FIRST_NAME, userBean.getValue(FIRST_NAME));
                        variables.declareVariable(LAST_NAME, userBean.getValue(LAST_NAME));
                        variables.declareVariable(QUALIFICATIONS, userBean.getValue(QUALIFICATIONS));
                        variables.declareVariable(NAME, userBean.getValue(NAME));
                        variables.declareVariable(DESCRIPTION, userBean.getValue(DESCRIPTION));
                        Object value = context.getValue(expression);
                        if (value != null) {
                            result = value.toString();
                        }
                    } catch (Throwable exception) {
                        log.warn(exception.getMessage(), exception);
                    }
                }
            }
        }
        if (result == null) {
            result = user.getName();
        }
        return result;
    }

    /**
     * Returns a user given its identifier.
     *
     * @param id the user identifier
     * @return the corresponding user, or {@code null} if none is found
     */
    private User getUser(long id) {
        // use a 2 stage select to get the user, so that caches can be utilised
        ArchetypeQuery query = new ArchetypeQuery(UserArchetypes.USER, false);
        query.getArchetypeConstraint().setAlias("user");
        query.add(Constraints.eq("id", id));
        query.add(new ObjectRefSelectConstraint("user"));
        query.setMaxResults(1);
        ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(service, query);
        if (iterator.hasNext()) {
            IMObjectReference reference = iterator.next().getReference("user.reference");
            return (User) service.get(reference);
        }
        return null;
    }
}
