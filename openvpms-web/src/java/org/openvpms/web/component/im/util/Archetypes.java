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

package org.openvpms.web.component.im.util;

import org.apache.commons.lang.StringUtils;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;


/**
 * A collection of archetype short names.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class Archetypes<T extends IMObject> {

    /**
     * The short names with wildcards expanded.
     */
    private String[] shortNames;

    /**
     * The type that the short names represent.
     */
    private final Class<T> type;

    /**
     * The default short name.
     */
    private final String defaultShortName;

    /**
     * The display name.
     */
    private String displayName;


    /**
     * Creates a new <tt>Arcgetypes</tt>.
     *
     * @param shortName the archetype short name. May contain wildcards
     * @param type      the type that the short name represent
     */
    public Archetypes(String shortName, Class<T> type) {
        this(shortName, type, null);
    }

    /**
     * Creates a new <tt>Archetypes</tt>.
     *
     * @param shortName   the archetype short name. May contain wildcards
     * @param type        the type that the short name represent
     * @param displayName the collective noun for the archetype(s). If
     *                    <tt>null</tt>, one will be derived
     */
    public Archetypes(String shortName, Class<T> type, String displayName) {
        this(new String[]{shortName}, type, null, displayName);
    }

    /**
     * Creates a new <tt>Archetypes</tt>.
     *
     * @param shortNames       the archetype short names. May contain wildcards
     * @param type             the type that the short names represent
     * @param defaultShortName the default short name. May be <tt>null</tt>
     * @param displayName      the collective noun for the archetypes. If
     */
    public Archetypes(String[] shortNames, Class<T> type, String defaultShortName, String displayName) {
        this.shortNames = expandShortNames(shortNames);
        Class actual = IMObjectHelper.getType(this.shortNames);
        if (!type.isAssignableFrom(actual)) {
            throw new IllegalStateException("Invalid type. Expected "
                                            + type + ", but got " + actual + " for archetypes "
                                            + StringUtils.join(shortNames, ", "));
        }
        this.type = type;
        this.defaultShortName = defaultShortName;
        this.displayName = displayName;
    }

    /**
     * Creates a new <tt>ShortNames</tt>.
     *
     * @param shortNames the archetype short names. May contain wildcards
     * @param type       the type that the short names represent
     */
    public Archetypes(String[] shortNames, Class<T> type) {
        this(shortNames, type, null, null);
    }

    /**
     * Returns the archetype short names.
     * <p/>
     * Any wildcards are expanded.
     *
     * @return the archetype short names
     */
    public String[] getShortNames() {
        return shortNames;
    }

    /**
     * Returns the default archetype short name.
     *
     * @return the default short name. May be <tt>null</tt>
     */
    public String getDefaultShortName() {
        return defaultShortName;
    }

    /**
     * Returns <tt>true</tt> if the collection contains a short name.
     *
     * @param shortName the short name. May contain wildcards
     * @return <tt>true</tt> if this contains <tt>shortName</tt>
     */
    public boolean contains(String shortName) {
        for (String s : shortNames) {
            if (TypeHelper.matches(s, shortName)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns <tt>true</tt> if the collection contains the short name of the supplied object.
     *
     * @param object the object
     * @return <tt>true</tt> if this contains a short name matching the supplied object's
     */
    public boolean contains(IMObject object) {
        return contains(object.getArchetypeId().getShortName());
    }

    /**
     * Returns the display name.
     *
     * @return the display name
     */
    public String getDisplayName() {
        if (displayName == null) {
            displayName = createDisplayName();
        }
        return displayName;
    }

    /**
     * Helper to create a new instance.
     *
     * @param shortName the archetype short name. May contain wildcards.
     * @param type      the type that the short name represents
     * @return a new instance
     */
    public static <T extends IMObject> Archetypes<T> create(String shortName,
                                                            Class<T> type) {
        return new Archetypes<T>(shortName, type);
    }

    /**
     * Helper to create a new instance.
     *
     * @param shortName   the archetype short name. May contain wildcards.
     * @param type        the type that the short name represents
     * @param displayName the collective noun for the archetype(s). If <tt>null</tt>, one will be derived
     * @return a new instance
     */
    public static <T extends IMObject> Archetypes<T> create(
            String shortName, Class<T> type, String displayName) {
        return new Archetypes<T>(shortName, type, displayName);
    }

    /**
     * Helper to create a new instance.
     *
     * @param shortNames the archetype short names. May contain wildcards.
     * @param type       the type that the short names represent
     * @return a new instance
     */
    public static <T extends IMObject> Archetypes<T> create(String[] shortNames,
                                                            Class<T> type) {
        return new Archetypes<T>(shortNames, type);
    }

    /**
     * Helper to create a new instance.
     *
     * @param shortNames  the archetype short names. May contain wildcards.
     * @param type        the type that the short names represent
     * @param displayName the collective noun for the archetype(s). If <tt>null</tt>, one will be derived
     * @return a new instance
     */
    public static <T extends IMObject> Archetypes<T> create(
            String[] shortNames, Class<T> type, String displayName) {
        return create(shortNames, type, null, displayName);
    }

    /**
     * Helper to create a new instance.
     *
     * @param shortNames       the archetype short names. May contain wildcards.
     * @param type             the type that the short names represent
     * @param defaultShortName the default short name. May be <tt>null</tt>
     * @param displayName      the collective noun for the archetype(s). If <tt>null</tt>, one will be derived
     * @return a new instance
     */
    public static <T extends IMObject> Archetypes<T> create(
            String[] shortNames, Class<T> type, String defaultShortName, String displayName) {
        return new Archetypes<T>(shortNames, type, defaultShortName, displayName);
    }

    /**
     * Returns the type that the short names represent.
     *
     * @return the type
     */
    public Class<T> getType() {
        return type;
    }

    /**
     * Helper to derive a display name from the short names
     *
     * @return the display names for the short names
     */
    protected String createDisplayName() {
        StringBuffer type = new StringBuffer();
        for (int i = 0; i < shortNames.length && i < 2; ++i) {
            if (i != 0) {
                type.append("/");
            }
            type.append(DescriptorHelper.getDisplayName(shortNames[i]));
        }
        if (shortNames.length > 2) {
            type.append("/...");
        }
        return type.toString();
    }

    /**
     * Helper to expand wildcarded short names.
     *
     * @param shortNames the short names to expand
     * @return the expanded short names
     */
    private String[] expandShortNames(String[] shortNames) {
        String[] result = DescriptorHelper.getShortNames(shortNames);
        if (result.length == 0) {
            // try and find non-primary archetypes, but only if the strings
            // aren't wildcarded. In general, don't want wildcards to pick
            // up a mix of primary and non-primary archetypes, but also don't
            // want to have to specify to find non-primary archetypes when the
            // short names are fully specified
            for (String shortName : shortNames) {
                if (shortName.contains("*")) {
                    // a shortname contains wildcards, so abort
                    return result;
                }
            }
            result = DescriptorHelper.getShortNames(shortNames, false);
        }
        return result;
    }
}
