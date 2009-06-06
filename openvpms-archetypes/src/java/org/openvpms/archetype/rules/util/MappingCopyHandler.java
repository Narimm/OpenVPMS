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
 *  Copyright 2008 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.archetype.rules.util;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.ArchetypeServiceException;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.AbstractIMObjectCopyHandler;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopier;
import org.openvpms.component.business.service.archetype.helper.IMObjectCopyHandler;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * An {@link IMObjectCopyHandler IMObjectCopyHandler} that provides support for:
 * <ul>
 * <li>mapping one archetype to another</li>
 * <li>referencing, copying or excluding objects based on their archetype or
 * class type</li>.
 * </ul>
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public abstract class MappingCopyHandler extends AbstractIMObjectCopyHandler {

    /**
     * Determines how objects should be treated.
     */
    public enum Treatment {
        REFERENCE,  // indicates that the object should be returned unchanged
        COPY,       // indicates to return a new instance of the object
        EXCLUDE     // indicates to return <tt>null</tt>
    }

    /**
     * A list of short name pairs, indicating the short name to map from and to.
     * If the 'to' short name is null then any instance of the 'from' is
     * ignored.
     */
    private String[][] shortNameMap;

    /**
     * If <tt>true</tt>, the 'from' short name in the short name map is the
     * second element and the 'to' short name the first.
     */
    private boolean reverse;

    /**
     * The default treatment.
     */
    private Treatment defaultTreatment = Treatment.COPY;

    /**
     * The types to reference.
     */
    private Types refTypes = new Types();

    /**
     * The types to copy.
     */
    private Types copyTypes = new Types();

    /**
     * The types to exclude.
     */
    private Types excludeTypes = new Types();

    /**
     * Returned by {@link #mapTo} to indicate that an object is to be
     * replaced with <tt>null</tt>.
     */
    protected static final String MAP_TO_NULL = "__MAP_TO_NULL";


    /**
     * Creates a new <tt>MappingCopyHandler</tt>.
     */
    public MappingCopyHandler() {
    }

    /**
     * Creates a new <tt>MappingCopyHandler</tt>.
     *
     * @param shortNameMap a list of short name pairs, indicating the short name
     *                     to map from and to. If the 'to' short name is null,
     *                     then any instance of the 'from' is ignored
     */
    public MappingCopyHandler(String[][] shortNameMap) {
        setShortNameMap(shortNameMap);
    }

    /**
     * Creates a new <tt>MappingCopyHandler</tt>.
     *
     * @param shortNameMap a list of short name pairs, indicating the short name
     *                     to map from and to. If the 'to' short name is null,
     *                     then any instance of the 'from' is ignored
     * @param reverse      if <tt>true</tt>, the 'from' short name is the second
     *                     element and the 'to' short name the first
     */
    public MappingCopyHandler(String[][] shortNameMap, boolean reverse) {
        setShortNameMap(shortNameMap);
        setReverse(reverse);
    }

    /**
     * Sets the short name map.
     *
     * @param shortNameMap a list of short name pairs, indicating the short name
     *                     to map from and to. If the 'to' short name is null,
     *                     then any instance of the 'from' is ignored
     */
    public void setShortNameMap(String[][] shortNameMap) {
        this.shortNameMap = shortNameMap;
    }

    /**
     * @param reverse if <tt>true</tt>, the 'from' short name is the element,
     *                and the 'to' short name the first
     */
    public void setReverse(boolean reverse) {
        this.reverse = reverse;
    }

    /**
     * Sets a list of types to reference.
     * <p/>
     * All instances of the specified types will be returned as is by
     * the {@link #getObject} method.
     *
     * @param types the types to reference
     */
    public void setReference(Class ... types) {
        refTypes.setTypes(types);
    }

    /**
     * Sets the archetype short names of the objects to reference.
     * <p/>
     * All instances with the specified short names will be returned as is by
     * the {@link #getObject} method.
     *
     * @param shortNames the short names of the objects to reference
     */
    public void setReference(String ... shortNames) {
        refTypes.setShortNames(shortNames);
    }

    /**
     * Sets the types to copy.
     * <p/>
     * All instances of the specified types will be copied.
     *
     * @param types the types to copy
     */
    public void setCopy(Class ... types) {
        copyTypes.setTypes(types);
    }

    /**
     * Sets the archetype short names of the objects to copy.
     * <p/>
     * All instances with the specified short names will be copied.
     *
     * @param shortNames the short names of the objects to copy
     */
    public void setCopy(String ... shortNames) {
        copyTypes.setShortNames(shortNames);
    }

    /**
     * Sets the types to exclude.
     * <p/>
     * All instance of the specified types will be replaced with <tt>null</tt>.
     *
     * @param types the types to exclude
     */
    public void setExclude(Class ... types) {
        excludeTypes.setTypes(types);
    }

    /**
     * Sets the archetype short names of the objects to exclude.
     * <p/>
     * All instances with the specified short names will be replaced with
     * <tt>null</tt>.
     *
     * @param shortNames the short names of the objects to exclude
     */
    public void setExclude(String ... shortNames) {
        excludeTypes.setShortNames(shortNames);
    }

    /**
     * Determines the default treatment of objects.
     * <p/>
     * Defaults to {@link Treatment#COPY}.
     *
     * @param treatment the default treatment
     */
    public void setDefaultTreatment(Treatment treatment) {
        defaultTreatment = treatment;
    }

    /**
     * Returns the default treatment.
     *
     * @return the default treatment
     */
    public Treatment getDefaultTreatment() {
        return defaultTreatment;
    }

    /**
     * Determines how {@link IMObjectCopier IMObjectCopier} should treat an
     * object.
     * <p/>The behaviour is determined as follows:
     * <ol>
     * <li>if the the object is mapped to a different archetype via
     * {@link #setShortNameMap} , a new instance of the map-to type is
     * returned; else</li>
     * <li>if the object is mapped to null via the above, then <tt>null</null>
     * is returned; else</li>
     * <li>the treatment is determined via {@link #getTreatment}</li>
     *
     * @param object  the source object
     * @param service the archetype service
     * @return <tt>object</tt> if the object shouldn't be copied,
     *         <tt>null</tt> if it should be replaced with <tt>null</tt>,
     *         or a new instance if the object should be copied
     */
    @Override
    public IMObject getObject(IMObject object, IArchetypeService service) {
        IMObject result;
        String from = object.getArchetypeId().getShortName();
        String to = mapTo(from);
        if (MAP_TO_NULL.equals(to)) {
            result = null;
        } else if (to != null) {
            result = newInstance(to, service);
        } else switch (getTreatment(object)) {
            case REFERENCE:
                result = object;
                break;
            case COPY:
                result = newInstance(object, service);
                break;
            case EXCLUDE:
                result = null;
                break;
            default:
                throw new IllegalStateException("Object not handled");
        }
        return result;
    }

    /**
     * Returns the short name to map the specified short name to.
     *
     * @param shortName the short name to map from
     * @return the short name to map to or {@link #MAP_TO_NULL} if the object
     *         should be replaced with <tt>null</tt>, or <tt>null</tt> if there
     *         is no mapping for the short name
     */
    protected String mapTo(String shortName) {
        String result = null;
        if (shortNameMap != null) {
            for (String[] map : shortNameMap) {
                String from;
                String to;
                if (!reverse) {
                    from = map[0];
                    to = map[1];
                } else {
                    from = map[1];
                    to = map[0];
                }
                if (shortName.equals(from)) {
                    result = (to == null) ? MAP_TO_NULL : to;
                    break;
                }
            }
        }
        return result;
    }

    /**
     * Determines how an object should be handled.
     * <p/>
     * This returns:
     * <ul>
     * <li>{@link Treatment#REFERENCE REFERENCE} if the object should be
     * referenced</li>
     * <li>{@link Treatment#COPY COPY} if a new instance of the object should be
     * returned so it may be copied</li>
     * <li>{@link Treatment#EXCLUDE EXCLUDE} if the object should be replaced
     * with <tt>null</tt></li>
     * </ul>
     *
     * @param object the object
     * @return the type of behaviour to apply to the object
     */
    protected Treatment getTreatment(IMObject object) {
        if (reference(object)) {
            return Treatment.REFERENCE;
        } else if (copy(object)) {
            return Treatment.COPY;
        } else if (exclude(object)) {
            return Treatment.EXCLUDE;
        }
        return defaultTreatment;
    }

    /**
     * Determines if an object should be referenced.
     * <p/>
     * This implementation returns <tt>true</tt> if the object is an instance
     * of a type specified by {@link #setReference(String[])}
     * or {@link #setReference(Class[])}.
     *
     * @param object the object to check
     * @return <tt>true</tt> if it should be referenced
     */
    protected boolean reference(IMObject object) {
        return refTypes.matches(object);
    }

    /**
     * Determines if an object should be copied.
     * <p/>
     * This implementation returns <tt>true</tt> if the object is an instance
     * of a type specified by {@link #setCopy(String[])}
     * or {@link #setCopy(Class[])}.
     *
     * @param object the object to check
     * @return <tt>true</tt> if it should be referenced
     */
    protected boolean copy(IMObject object) {
        return copyTypes.matches(object);
    }

    /**
     * Determines if an object should by excluded.
     * <p/>
     * Excluded objects are replaced with <tt>null</tt>.
     * <p/>
     * This implementation returns <tt>true</tt> if the object is an instance
     * of a type specified by {@link #setExclude(String[])} or
     * {@link #setExclude(Class[])}.
     *
     * @param object the object to check
     * @return <tt>true</tt> if it should be replaced with <tt>null</tt>
     */
    protected boolean exclude(IMObject object) {
        return excludeTypes.matches(object);
    }

    /**
     * Creates a new instance of the specified object.
     *
     * @param object  the object
     * @param service the archetype service
     * @return a new instance
     * @throws ArchetypeServiceException for any archetype service error
     */
    private IMObject newInstance(IMObject object, IArchetypeService service) {
        ArchetypeId id = object.getArchetypeId();
        String shortName = id.getShortName();
        return newInstance(shortName, service);
    }

    /**
     * Creates a new instance of the specified type.
     *
     * @param shortName the short name of the object to create
     * @param service   the archetype service
     * @return a new instance
     * @throws ArchetypeServiceException for any archetype service error
     */
    private IMObject newInstance(String shortName, IArchetypeService service) {
        IMObject result = service.create(shortName);
        if (result == null) {
            throw new ArchetypeServiceException(
                    ArchetypeServiceException.ErrorCode.FailedToCreateObject,
                    shortName);
        }
        return result;
    }

    private static class Types {

        /**
         * The class types to match on.
         */
        private Set<Class> types = Collections.emptySet();

        /**
         * The archetype short names to match on
         */
        private Set<String> shortNames = Collections.emptySet();


        /**
         * Sets the class types to match on.
         *
         * @param types the class types
         */
        public void setTypes(Class ... types) {
            if (types.length == 0) {
                this.types = Collections.emptySet();
            } else {
                this.types = new HashSet<Class>(Arrays.asList(types));
            }
        }

        public Class matchesType(IMObject object) {
            return getMatchingType(object);
        }

        /**
         * Sets the archetype short names to match on.
         *
         * @param shortNames the archetype short names
         */
        public void setShortNames(String ... shortNames) {
            if (shortNames.length == 0) {
                this.shortNames = Collections.emptySet();
            } else {
                this.shortNames = new HashSet<String>(
                        Arrays.asList(shortNames));
            }
        }

        /**
         * Determines if an object matches the types.
         *
         * @param object the object
         * @return <tt>true</tt> if the object matches, otherwise <tt>false</tt>
         */
        public boolean matches(IMObject object) {
            String shortName = object.getArchetypeId().getShortName();
            return shortNames.contains(shortName) || isInstance(object);
        }

        /**
         * Determines if an object is an instance of one of the specified types.
         *
         * @param object the object to check
         * @return <tt>true</tt> if the object is an instance, otherwise
         *         <tt>false</tt>
         */
        private boolean isInstance(IMObject object) {
            for (Class type : types) {
                if (type.isAssignableFrom(object.getClass())) {
                    return true;
                }
            }
            return false;
        }

        private Class getMatchingType(IMObject object) {
            return getMatchingType(object.getClass());
        }

        private Class getMatchingType(Class clazz) {
            Class result = null;
            for (Class type : types) {
                if (type.isAssignableFrom(clazz)) {
                    if (result != null) {
                        if (result.isAssignableFrom(type)) {
                            result = type;
                        }
                    } else {
                        result = type;
                    }
                }
            }
            return result;
        }
    }
}
