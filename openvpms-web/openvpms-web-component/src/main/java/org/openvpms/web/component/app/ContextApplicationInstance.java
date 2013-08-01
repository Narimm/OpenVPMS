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
 * Copyright 2013 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.app;

import nextapp.echo2.app.ApplicationInstance;
import nextapp.echo2.webcontainer.ContainerContext;
import nextapp.echo2.webrender.ClientProperties;
import org.openvpms.archetype.rules.practice.LocationRules;
import org.openvpms.archetype.rules.practice.PracticeRules;
import org.openvpms.archetype.rules.user.UserRules;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.component.business.domain.im.security.User;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.NodeSelectConstraint;
import org.openvpms.component.system.common.query.ObjectRefConstraint;
import org.openvpms.component.system.common.query.ObjectSet;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.web.component.style.Style;
import org.openvpms.web.component.style.UserStyleSheets;
import org.openvpms.web.component.util.ErrorHelper;
import org.openvpms.web.echo.spring.SpringApplicationInstance;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import javax.annotation.Resource;
import java.awt.Dimension;
import java.util.Map;


/**
 * An {@code ApplicationInstance} associated with a {@link GlobalContext}.
 *
 * @author Tim Anderson
 */
public abstract class ContextApplicationInstance
        extends SpringApplicationInstance {

    /**
     * The context.
     */
    private final GlobalContext context;

    /**
     * The client screen resolution.
     */
    private Dimension resolution;

    /**
     * The style sheets.
     */
    private UserStyleSheets styleSheets;

    /**
     * The practice rules.
     */
    private final PracticeRules rules;


    /**
     * Constructs a {@code ContextApplicationInstance}.
     *
     * @param context the context
     * @param rules   the practice rules
     */
    public ContextApplicationInstance(GlobalContext context, PracticeRules rules) {
        this.context = context;
        this.rules = rules;
        initUser();
        initPractice();
        initLocation();
        context.addListener(new ContextListener() {
            public void changed(String key, IMObject value) {
                if (Context.LOCATION_SHORTNAME.equals(key)) {
                    updateLocation((Party) value);
                }
            }
        });
    }

    /**
     * Returns the instance associated with the current thread.
     *
     * @return the current instance, or {@code null}
     */
    public static ContextApplicationInstance getInstance() {
        return (ContextApplicationInstance) ApplicationInstance.getActive();
    }

    /**
     * Returns the current context.
     *
     * @return the current context
     */
    public GlobalContext getContext() {
        return context;
    }

    /**
     * Switches the current workspace to display an object.
     *
     * @param object the object to view
     */
    public abstract void switchTo(IMObject object);

    /**
     * Switches the current workspace to one that supports a particular archetype.
     *
     * @param shortName the archetype short name
     */
    public abstract void switchTo(String shortName);

    /**
     * Clears the current context.
     */
    protected void clearContext() {
        for (IMObject object : context.getObjects()) {
            context.removeObject(object);
        }
    }

    /**
     * Returns the client's screen resolution.
     *
     * @return the client's screen resolution, or <em>1024x768</em> if it cannot be determined
     */
    public Dimension getResolution() {
        if (resolution == null) {
            ContainerContext context = (ContainerContext) getContextProperty(ContainerContext.CONTEXT_PROPERTY_NAME);
            int width = getProperty(context, "width", ClientProperties.SCREEN_WIDTH, 1024);
            int height = getProperty(context, "height", ClientProperties.SCREEN_HEIGHT, 768);
            resolution = new Dimension(width, height);
        }
        return resolution;
    }

    /**
     * Overrides the client's screen resolution.
     *
     * @param resolution the new resolution
     */
    public void setResolution(Dimension resolution) {
        this.resolution = resolution;
    }

    /**
     * Sets the style sheets.
     *
     * @param styleSheets the style sheets
     */
    @Resource
    public void setStyleSheets(UserStyleSheets styleSheets) {
        this.styleSheets = styleSheets;
    }

    /**
     * Returns the style sheets.
     *
     * @return the style sheets
     */
    public UserStyleSheets getStyleSheets() {
        return styleSheets;
    }

    /**
     * Sets the style sheet based on the client's screen resolution.
     */
    public void setStyleSheet() {
        Dimension size = getResolution();
        setStyle(size.width, size.height);
    }

    /**
     * Sets the style based on the specified screen resolution
     *
     * @param width  the screen width
     * @param height the screen height
     */
    public void setStyle(int width, int height) {
        try {
            Style style = styleSheets.getStyle(width, height);
            styleSheets.setStyle(this, style);
            setStyleSheet(style.getStylesheet());
            setResolution(style.getSize());
        } catch (Throwable exception) {
            ErrorHelper.show(exception, true);
        }
    }

    /**
     * Returns an integer property value from one of the query parameters, container context, or a default value.
     *
     * @param context         the container context
     * @param queryStringName the query string name of the property
     * @param propertyName    the container context name of the property
     * @param defaultValue    the default, if no other value was found
     * @return the property value, or the default, if no other value was found
     */
    private int getProperty(ContainerContext context, String queryStringName, String propertyName, int defaultValue) {
        int result = -1;
        if (context != null) {
            Map map = context.getInitialRequestParameterMap();
            if (map != null) {
                String[] values = (String[]) map.get(queryStringName);
                if (values != null && values.length == 1) {
                    try {
                        result = Integer.valueOf(values[0]);
                    } catch (NumberFormatException ignore) {
                        // do nothing
                    }
                }
            }
            if (result == -1) {
                ClientProperties properties = context.getClientProperties();
                if (properties != null) {
                    result = properties.getInt(propertyName, defaultValue);
                }
            }
        }
        if (result <= 0) {
            result = defaultValue;
        }
        return result;
    }

    /**
     * Initialises the user.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void initUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            UserRules rules = new UserRules();
            User user = rules.getUser(auth.getName());
            if (user != null) {
                context.setUser(user);
                if (rules.isClinician(user)) {
                    context.setClinician(user);
                }
            }
        }
    }

    /**
     * Initialises the practice.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void initPractice() {
        context.setPractice(rules.getPractice());
    }

    /**
     * Initialises the location specific information.
     *
     * @throws ArchetypeServiceException for any archetype service error
     */
    private void initLocation() {
        // Get the current Practice
        Party practice = context.getPractice();

        // Get the current user.
        User user = context.getUser();

        // If pactice and/or user not set then exit.
        if (practice == null || user == null) {
            return;
        }

        UserRules userRules = new UserRules();
        // Now get the default location for the user or the first location if
        // no default.
        Party location = userRules.getDefaultLocation(user);

        // If no locations defined for user find default location for Practice
        // or the first location if no default.
        if (location == null) {
            location = rules.getDefaultLocation(practice);
        }

        context.setLocation(location);
        updateLocation(location);
    }

    /**
     * Updates the context when the location changes.
     *
     * @param location the location. May be {@code null}
     */
    private void updateLocation(Party location) {
        Party deposit = null;
        Party till = null;
        Entity scheduleView = null;
        Entity workListView = null;
        Party stockLocation = null;

        if (location != null) {
            // initialise the defaults for the location
            LocationRules rules = new LocationRules();
            deposit = rules.getDefaultDepositAccount(location);
            till = rules.getDefaultTill(location);
            scheduleView = rules.getDefaultScheduleView(location);
            workListView = rules.getDefaultWorkListView(location);
            stockLocation = getStockLocation(rules, location);
        }
        context.setDeposit(deposit);
        context.setTill(till);
        context.setScheduleView(scheduleView);
        context.setSchedule(null);
        context.setWorkListView(workListView);
        context.setWorkList(null);
        context.setStockLocation(stockLocation);
    }

    /**
     * Helper to return the stock location for a location.
     * <p/>
     * NOTE: this implementation returns a partially populated object, as
     * stock locations may have a large no. of product relationships.
     * <p/>
     * The version of the object is set to {@code -1} so that it cannot
     * be used to overwrite the actual stock location.
     *
     * @param rules    the location rules
     * @param location the location
     * @return the stock location, or {@code null} if none is found
     */
    private Party getStockLocation(LocationRules rules, Party location) {
        Party result = null;
        IMObjectReference ref = rules.getDefaultStockLocationRef(location);
        if (ref != null) {
            ObjectRefConstraint constraint
                    = new ObjectRefConstraint("loc", ref);
            ArchetypeQuery query = new ArchetypeQuery(constraint);
            query.add(new NodeSelectConstraint("loc.name"));
            query.add(new NodeSelectConstraint("loc.description"));
            ObjectSetQueryIterator iter = new ObjectSetQueryIterator(query);
            if (iter.hasNext()) {
                ObjectSet set = iter.next();
                result = new Party();
                result.setId(ref.getId());
                result.setArchetypeId(ref.getArchetypeId());
                result.setLinkId(ref.getLinkId());
                result.setName(set.getString("loc.name"));
                result.setDescription(set.getString("loc.description"));
                result.setVersion(-1);
            }
        }
        return result;
    }

}
