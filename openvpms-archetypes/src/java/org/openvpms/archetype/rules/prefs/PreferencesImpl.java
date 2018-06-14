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
 * Copyright 2018 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.archetype.rules.prefs;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.ObjectUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.EntityLink;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.model.bean.IMObjectBean;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.component.system.common.util.PropertySet;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
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
     * The party the preferences belong to.
     */
    private final IMObjectReference party;

    /**
     * The party to use for default preferences. May be {@code null}
     */
    private final IMObjectReference source;

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
     * The preferences.
     */
    private IMObjectBean bean;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(PreferencesImpl.class);

    /**
     * Constructs a  {@link PreferencesImpl}. Changes aren't persistent.
     *
     * @param party       the party the preferences belong to
     * @param source      the party to use for default preferences. May be {@code null}
     * @param preferences the preferences entity
     * @param service     the archetype service
     */
    public PreferencesImpl(IMObjectReference party, IMObjectReference source, Entity preferences,
                           IArchetypeService service) {
        this(party, source, preferences, service, null);
    }

    /**
     * Constructs a {@link PreferencesImpl}.
     *
     * @param party              the user the preferences belong to
     * @param source             the party to use for default preferences. May be {@code null}
     * @param preferences        the preferences entity
     * @param service            the archetype service
     * @param transactionManager the transaction manager, or {@code null} if changes aren't persistent
     */
    public PreferencesImpl(IMObjectReference party, IMObjectReference source, Entity preferences,
                           IArchetypeService service, PlatformTransactionManager transactionManager) {
        this.party = party;
        this.source = source;
        bean = service.getBean(preferences);
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
     * Returns preferences for a party.
     * <p>
     * If the party has no preferences, and a {@code source} party is specified, the
     * <p>
     * Changes will not be made persistent.
     *
     * @param party   the party
     * @param source  the party to use for default preferences. May be {@code null}
     * @param service the archetype service
     * @return the preferences
     */
    public static Preferences getPreferences(IMObjectReference party, IMObjectReference source,
                                             final IArchetypeService service) {
        Entity prefs = getEntity(party, service);
        if (prefs == null && source != null) {
            prefs = getEntity(source, service);
        }
        if (prefs == null) {
            prefs = (Entity) service.create(PreferenceArchetypes.PREFERENCES);
        }
        return new PreferencesImpl(party, source, prefs, service);
    }

    /**
     * Returns the root preference entity for a user or practice, creating and saving one if none exists.
     *
     * @param party              the party
     * @param source             if non-null, specifies the source to copy preferences from if the party has none
     * @param service            the archetype service
     * @param transactionManager the transaction manager
     * @return the root preference entity
     */
    public static Entity getPreferences(final IMObjectReference party, final IMObjectReference source,
                                        final IArchetypeService service,
                                        PlatformTransactionManager transactionManager) {
        Entity result;
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        result = template.execute(status -> {
            Entity prefs = getEntity(party, service);
            if (prefs == null && source != null) {
                Entity sourcePrefs = getEntity(source, service);
                if (sourcePrefs != null) {
                    prefs = copy(party, sourcePrefs, service);
                }
            }
            if (prefs == null) {
                prefs = (Entity) service.create(PreferenceArchetypes.PREFERENCES);
                IMObjectBean bean = service.getBean(prefs);
                bean.addTarget("user", party);
                bean.save();
            }
            return prefs;
        });
        return result;
    }

    /**
     * Resets the preferences for a party.
     * <p>
     * If a source party is specified, it's preferences will be copied to the party if they exist.
     *
     * @param party              the party
     * @param source             the source party to copy preferences from. May be {@code null}
     * @param service            the archetype service
     * @param transactionManager the transaction manager
     */
    public static void reset(final IMObjectReference party, final IMObjectReference source,
                             final IArchetypeService service, PlatformTransactionManager transactionManager) {
        TransactionTemplate template = new TransactionTemplate(transactionManager);
        template.execute(new TransactionCallbackWithoutResult() {
            @Override
            public void doInTransactionWithoutResult(TransactionStatus status) {
                Entity prefs = getEntity(party, service);
                if (prefs != null) {
                    IMObjectBean bean = service.getBean(prefs);
                    List<IMObject> groups = bean.getTargets("groups", IMObject.class);
                    List<EntityLink> relationships = bean.getValues("groups", EntityLink.class);
                    for (EntityLink relationship : relationships) {
                        prefs.removeEntityLink(relationship);
                    }
                    bean.save();
                    for (IMObject group : groups) {
                        service.remove(group);
                    }
                    service.remove(prefs);
                }
                if (source != null && !ObjectUtils.equals(party, source)) {
                    Entity sourcePrefs = getEntity(source, service);
                    if (sourcePrefs != null) {
                        copy(party, sourcePrefs, service);
                    }
                }
            }
        });
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
     * @return the relationship types
     */
    protected String[] getRelationshipTypes() {
        return DescriptorHelper.getShortNames("entityLink.preferenceGroup*");
    }

    /**
     * Copies preferences.
     *
     * @param party   the party to link the copied preferences to
     * @param prefs   the preferences to copy
     * @param service the archetype service
     * @return the copied preferences entiy
     */
    protected static Entity copy(IMObjectReference party, Entity prefs, IArchetypeService service) {
        List<IMObject> objects = PreferencesCopier.copy(prefs, party, service);
        service.save(objects);
        return (Entity) objects.get(0);
    }

    /**
     * Returns the root preference entity for a party.
     *
     * @param party   the party
     * @param service the archetype service
     * @return the entity, or {@code null} if it doesn't exist
     */
    protected static Entity getEntity(IMObjectReference party, IArchetypeService service) {
        Entity prefs = null;
        final ArchetypeQuery query = new ArchetypeQuery(PreferenceArchetypes.PREFERENCES);
        query.add(Constraints.join("user").add(Constraints.eq("target", party)));
        query.add(Constraints.sort("id"));
        query.setMaxResults(1);
        IMObjectQueryIterator<Entity> iterator = new IMObjectQueryIterator<>(service, query);
        if (iterator.hasNext()) {
            prefs = iterator.next();
        }
        return prefs;
    }

    /**
     * Returns a group, given its name.
     * <p>
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
        bean.addTarget("groups", relationshipType, entity);
        if (save) {
            try {
                bean.save(entity);
            } catch (Throwable exception) {
                // This can occur if the root preference has been updated in a different browser session.
                // NOTE that this will cause any outer transaction to roll back. See OVPMS-2046
                log.error("Failed to add group=" + name + " to preference=" + bean.getReference(), exception);
                reload();
                addGroup(name, relationshipType, false);
            }
        }
        result = new PreferenceGroup(entity, service);
        groups.add(result);
        return result;
    }

    /**
     * Sets a preference.
     *
     * @param groupName the group name
     * @param name      the preference name
     * @param value     the value
     * @param save      if {@code true}, make it persistent
     */
    private void setPreference(String groupName, String name, Object value, boolean save) {
        PreferenceGroup group = getGroup(groupName);
        Object current = group.get(name);
        if (!ObjectUtils.equals(current, value)) {
            group.set(name, value);
            if (save) {
                try {
                    group.save();
                } catch (Throwable exception) {
                    // This can occur if the group has been updated in a different browser session.
                    // NOTE that this will cause any outer transaction to roll back. See OVPMS-2046
                    log.error("Failed to save preference=" + group.getEntity().getObjectReference()
                              + ", name=" + name + ", value=" + value, exception);
                    reload();
                    setPreference(groupName, name, value, false);
                }
            }
        }
    }

    /**
     * Reloads preferences.
     */
    private void reload() {
        Entity prefs = getPreferences(party, source, service, transactionManager);
        bean = service.getBean(prefs);
        groups.clear();
    }

}
