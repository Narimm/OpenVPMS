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

package org.openvpms.archetype.rules.prefs;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.EntityBean;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.util.PropertySet;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * Default implementation of the {@link Preferences} interface.
 *
 * @author Tim Anderson
 */
class PreferencesImpl implements Preferences {

    /**
     * The user the preferences belong to.
     */
    private final IMObjectReference user;

    /**
     * The preferences.
     */
    private EntityBean bean;

    /**
     * The preference groups.
     */
    private final List<PreferenceGroup> groups = new ArrayList<>();

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The transaction manager, or {@code null} if changes aren't persistent.
     */
    private final PlatformTransactionManager transactionManager;

    /**
     * Constructs a  {@link PreferencesImpl}. Changes aren't persistent.
     *
     * @param user        the user the preferences belong to
     * @param preferences the preferences entity
     * @param service     the archetype service
     */
    public PreferencesImpl(IMObjectReference user, Entity preferences, IArchetypeService service) {
        this(user, preferences, service, null);
    }

    /**
     * Constructs a {@link PreferencesImpl}.
     *
     * @param user               the user the preferences belong to
     * @param preferences        the preferences entity
     * @param service            the archetype service
     * @param transactionManager the transaction manager, or {@code null} if changes aren't persistent
     */
    public PreferencesImpl(IMObjectReference user, Entity preferences, IArchetypeService service,
                           PlatformTransactionManager transactionManager) {
        this.user = user;
        bean = new EntityBean(preferences, service);
        this.service = service;
        this.transactionManager = transactionManager;
    }

    /**
     * Returns a user preference, given its preference group name and name.
     *
     * @param groupName    the preference group name
     * @param name         the preference name
     * @param defaultValue the default value, if the preference is unset. May be {@code null}
     * @return the preference. May be {@code null}
     */
    @Override
    public Object getPreference(String groupName, String name, Object defaultValue) {
        Object value = getGroup(groupName).get(name);
        return value != null ? value : defaultValue;
    }

    /**
     * Sets a preference.
     *
     * @param groupName the preference group name
     * @param name      the preference name
     * @param value     the preference value. May be {@code null}
     */
    @Override
    public void setPreference(String groupName, String name, Object value) {
        boolean save = transactionManager != null;
        setPreference(groupName, name, value, save);
    }

    /**
     * Returns a preference, given its preference group name and name.
     *
     * @param groupName    the preference group name
     * @param name         the preference name
     * @param defaultValue the default value, if the preference is unset. May be {@code null}
     * @return the preference. May be {@code null}
     */
    @Override
    public boolean getBoolean(String groupName, String name, boolean defaultValue) {
        return getGroup(groupName).getBoolean(name, defaultValue);
    }

    /**
     * Returns a preference, given its preference group name and name.
     *
     * @param groupName    the preference group name
     * @param name         the preference name
     * @param defaultValue the default value, if the preference is unset. May be {@code null}
     * @return the preference. May be {@code null}
     */
    @Override
    public int getInt(String groupName, String name, int defaultValue) {
        return getGroup(groupName).getInt(name, defaultValue);
    }

    /**
     * Returns a preference, given its preference group name and name.
     *
     * @param groupName    the preference group name
     * @param name         the preference name
     * @param defaultValue the default value, if the preference is unset. May be {@code null}
     * @return the preference. May be {@code null}
     */
    @Override
    public long getLong(String groupName, String name, long defaultValue) {
        return getGroup(groupName).getLong(name, defaultValue);
    }

    /**
     * Returns a preference, given its preference group name and name.
     *
     * @param groupName    the preference group name
     * @param name         the preference name
     * @param defaultValue the default value, if the preference is unset. May be {@code null}
     * @return the preference. May be {@code null}
     */
    @Override
    public String getString(String groupName, String name, String defaultValue) {
        return getGroup(groupName).getString(name, defaultValue);
    }

    /**
     * Returns the reference value of a property.
     *
     * @param groupName    the preference group name
     * @param name         the preference name
     * @param defaultValue the default value, if the preference is unset. May be {@code null}
     * @return the preference. May be {@code null}
     */
    @Override
    public IMObjectReference getReference(String groupName, String name, IMObjectReference defaultValue) {
        IMObjectReference result = getGroup(groupName).getReference(name);
        return (result != null) ? result : defaultValue;
    }

    /**
     * Returns the available preference group names.
     *
     * @return the group name
     */
    @Override
    public Set<String> getGroupNames() {
        String[] relationshipTypes = getRelationshipTypes();
        return getGroupNames(relationshipTypes);
    }

    /**
     * Returns the available preferences in a group.
     *
     * @param groupName the group name.
     * @return the preference names
     */
    @Override
    public Set<String> getNames(String groupName) {
        PreferenceGroup group = getGroup(groupName);
        return group.getNames();
    }

    /**
     * Returns preferences for a user.
     *
     * @param user    the user
     * @param service the archetype service
     * @return the preferences
     */
    public static Preferences getPreferences(IMObjectReference user, final IArchetypeService service) {
        Entity prefs = getEntity(user, service);
        return new PreferencesImpl(user, prefs, service);
    }

    /**
     * Returns the root preference entity for a user, creating and saving one if none exists.
     *
     * @param user               the user
     * @param service            the archetype service
     * @param transactionManager the transaction manager
     * @return the root preference entity
     */
    public static Entity getPreferences(final IMObjectReference user, final IArchetypeService service,
                                        PlatformTransactionManager transactionManager) {
        Entity prefs;
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        prefs = template.execute(new TransactionCallback<Entity>() {
            @Override
            public Entity doInTransaction(TransactionStatus status) {
                Entity prefs = getEntity(user, service);
                if (prefs.isNew()) {
                    IMObjectBean bean = new IMObjectBean(prefs, service);
                    bean.addNodeTarget("user", user);
                    bean.save();
                }
                return prefs;
            }
        });
        return prefs;
    }

    /**
     * Returns the root preference entity for a user, creating it if it doesn't exist.
     *
     * @param user    the user
     * @param service the archetype service
     * @return the
     */
    protected static Entity getEntity(IMObjectReference user, IArchetypeService service) {
        Entity prefs;
        final ArchetypeQuery query = new ArchetypeQuery(PreferenceArchetypes.PREFERENCES);
        query.add(Constraints.join("user").add(Constraints.eq("target", user)));
        query.add(Constraints.sort("id"));
        query.setMaxResults(1);
        IMObjectQueryIterator<Entity> iterator = new IMObjectQueryIterator<>(service, query);
        if (iterator.hasNext()) {
            prefs = iterator.next();
        } else {
            prefs = (Entity) service.create(PreferenceArchetypes.PREFERENCES);
        }
        return prefs;
    }

    /**
     * Returns the available group names. These correspond to the archetype short names of the target node of
     * the relationships.
     *
     * @param relationshipTypes the relationship types
     * @return the available group names
     */
    protected Set<String> getGroupNames(String[] relationshipTypes) {
        return new LinkedHashSet<>(Arrays.asList(DescriptorHelper.getNodeShortNames(relationshipTypes, "target")));
    }

    /**
     * Returns the available preference relationship types.
     *
     * @return the releationship types
     */
    protected String[] getRelationshipTypes() {
        return DescriptorHelper.getShortNames("entityLink.preferenceGroup*");
    }

    /**
     * Returns a group, given its name.
     * <p/>
     * If the group doesn't exist, it will be created.
     *
     * @param name the group name
     * @return the group
     * @throws IllegalArgumentException if the name doesn't correspond to a valid group
     */
    private PreferenceGroup getGroup(String name) {
        PreferenceGroup result = null;
        for (PropertySet set : groups) {
            PreferenceGroup group = (PreferenceGroup) set;
            if (TypeHelper.isA(group.getEntity(), name)) {
                result = group;
                break;
            }
        }
        if (result == null) {
            for (EntityLink link : bean.getValues("groups", EntityLink.class)) {
                if (TypeHelper.isA(link.getTarget(), name)) {
                    Entity entity = (Entity) service.get(link.getTarget());
                    if (entity != null) {
                        result = new PreferenceGroup(entity, service);
                        groups.add(result);
                        break;
                    }
                }
            }
        }
        if (result == null) {
            boolean save = transactionManager != null;
            String[] relationshipTypes = getRelationshipTypes();
            for (String relationshipType : relationshipTypes) {
                String[] shortNames = DescriptorHelper.getNodeShortNames(relationshipType, "target");
                if (ArrayUtils.contains(shortNames, name)) {
                    result = addGroup(name, relationshipType, save);
                    break;
                }
            }
            if (result == null) {
                throw new IllegalArgumentException("Argument 'name' is not a valid preference group name");
            }
        }
        return result;
    }

    /**
     * Adds a preference group.
     *
     * @param name             the group archetype short name
     * @param relationshipType the relationship type
     * @param save             if {@code true} make the group persistent
     * @return the preference group
     */
    private PreferenceGroup addGroup(String name, String relationshipType, boolean save) {
        PreferenceGroup result;
        Entity entity = (Entity) service.create(name);
        bean.addNodeTarget("groups", relationshipType, entity);
        if (save) {
            try {
                service.save(Arrays.asList(bean.getEntity(), entity));
            } catch (Throwable exception) {
                reload();
                addGroup(name, relationshipType, false);
            }
        }
        result = new PreferenceGroup(entity, service);
        groups.add(result);
        return result;
    }


    private void setPreference(String groupName, String name, Object value, boolean save) {
        PreferenceGroup group = getGroup(groupName);
        Object current = group.get(name);
        if (!ObjectUtils.equals(current, value)) {
            group.set(name, value);
            if (save) {
                try {
                    group.save();
                } catch (Throwable exception) {
                    reload();
                    setPreference(groupName, name, value, false);
                }
            }
        }
    }

    private void reload() {
        Entity prefs = getPreferences(user, service, transactionManager);
        bean = new EntityBean(prefs, service);
        groups.clear();
    }
}
