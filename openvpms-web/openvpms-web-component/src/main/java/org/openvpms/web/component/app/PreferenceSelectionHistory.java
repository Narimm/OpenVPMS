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

package org.openvpms.web.component.app;

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.ObjectRefSelectConstraint;
import org.openvpms.component.system.common.query.ObjectSetQueryIterator;
import org.openvpms.web.component.prefs.UserPreferences;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Selection history backed by preferences.
 *
 * @author Tim Anderson
 */
public class PreferenceSelectionHistory extends SelectionHistory {

    /**
     * The short name that the history refers to. May contain wildcards.
     */
    private final String shortName;

    /**
     * The user preferences.
     */
    private final UserPreferences preferences;

    /**
     * The preference group.
     */
    private final String group;

    /**
     * The preference name.
     */
    private final String name;

    /**
     * Construct a {@code SelectionHistory} with the specified capacity.
     *
     * @param context     the context
     * @param preferences the preferences
     */
    public PreferenceSelectionHistory(Context context, String shortName, UserPreferences preferences, String group, String name) {
        super(context);
        this.shortName = shortName;
        this.preferences = preferences;
        this.group = group;
        this.name = name;
        parseHistory(context);
    }

    /**
     * Returns the short name that the history refers to. May contain wildcards.
     *
     * @return the short name
     */
    public String getShortName() {
        return shortName;
    }

    /**
     * Adds an object to the history.
     * <p/>
     * If it is already present, the existing selection will be removed, and the object added to the front of
     * the history, indicating that it is the most recent selection.
     * <p/>
     * If the no. of selections exceeds the history capacity, the oldest selection will be removed
     *
     * @param object the object to add
     */
    @Override
    public void add(IMObject object) {
        super.add(object);
        saveHistory();
    }

    /**
     * Parses the selection history from preferences.
     * <p/>
     * The history is stored as a comma separated list of {@code <id>,<timestamp>,<id>,<timestamp>...}
     *
     * @param context the context
     */
    protected void parseHistory(Context context) {
        String value = preferences.getString(group, name, null);
        String[] values = (value != null) ? value.split(",") : new String[0];
        Map<Long, Date> map = new HashMap<>();
        for (int i = 0; i < values.length - 1; i += 2) {
            long id;
            Date time;
            try {
                id = Long.valueOf(values[i]);
            } catch (NumberFormatException exception) {
                break;
            }
            try {
                time = new Date(Long.valueOf(values[i + 1]));
            } catch (NumberFormatException exception) {
                break;
            }
            map.put(id, time);
        }
        if (!map.isEmpty()) {
            ArchetypeQuery query = new ArchetypeQuery(shortName, true, false);
            query.getArchetypeConstraint().setAlias("p");
            query.add(new ObjectRefSelectConstraint("p"));
            query.add(Constraints.in("id", map.keySet().toArray()));
            ObjectSetQueryIterator iterator = new ObjectSetQueryIterator(query);
            while (iterator.hasNext()) {
                IMObjectReference ref = iterator.next().getReference("p.reference");
                Date time = map.get(ref.getId());
                getSelections().add(new Selection(ref, time, context));
            }
        }
    }

    /**
     * Writes the current history to preferences.
     */
    protected void saveHistory() {
        StringBuilder builder = new StringBuilder();
        for (Selection selection : getSelections()) {
            String id = Long.toString(selection.getReference().getId());
            String time = Long.toString(selection.getTime().getTime());
            int length = id.length() + time.length() + 1; // +1 for separator
            if (builder.length() > 0) {
                length++;
            }
            if (builder.length() + length > 5000) {
                break;
            }
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(id).append(',').append(time);
        }
        preferences.setPreference(group, name, builder.toString(), true);
    }
}
