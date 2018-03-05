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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.workspace.admin.type;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.openvpms.archetype.rules.util.DateUnits;
import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.web.component.im.edit.AbstractIMObjectEditor;
import org.openvpms.web.component.im.edit.IMObjectEditor;
import org.openvpms.web.component.im.layout.IMObjectLayoutStrategy;
import org.openvpms.web.component.im.layout.LayoutContext;
import org.openvpms.web.component.im.relationship.EntityLinkCollectionTargetEditor;
import org.openvpms.web.component.im.view.ComponentState;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.component.property.Validator;
import org.openvpms.web.component.property.ValidatorError;
import org.openvpms.web.resource.i18n.Messages;

import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * An editor for <em>entity.reminderType</em>.
 *
 * @author Tim Anderson
 */
public class ReminderTypeEditor extends AbstractIMObjectEditor {

    /**
     * Constructs a {@link ReminderTypeEditor}.
     *
     * @param object        the object to edit
     * @param parent        the parent object. May be {@code null}
     * @param layoutContext the layout context
     */
    public ReminderTypeEditor(Entity object, IMObject parent, LayoutContext layoutContext) {
        super(object, parent, layoutContext);
        getEditors().add(new CountsEditor(getCollectionProperty("counts"), object, layoutContext));
    }

    /**
     * Creates the layout strategy.
     *
     * @return a new layout strategy
     */
    @Override
    protected IMObjectLayoutStrategy createLayoutStrategy() {
        IMObjectLayoutStrategy strategy = super.createLayoutStrategy();
        CountsEditor counts = (CountsEditor) getEditor("counts", false);
        strategy.addComponent(new ComponentState(counts));
        return strategy;
    }

    private static class CountsEditor extends EntityLinkCollectionTargetEditor {
        /**
         * Constructs a {@link CountsEditor}.
         *
         * @param property the collection property
         * @param entity   the parent entity
         * @param context  the layout context
         */
        public CountsEditor(CollectionProperty property, Entity entity, LayoutContext context) {
            super(property, entity, context);
        }

        /**
         * Creates a new object, subject to a short name being selected, and current collection cardinality.
         *
         * @param shortName the archetype short name. May be {@code null}
         * @return a new object, or {@code null} if the object can't be created
         */
        @Override
        public IMObject create(String shortName) {
            IMObject object = super.create(shortName);
            if (object != null) {
                int maxCount = 0;
                for (IMObject reminderCount : getCurrentObjects()) {
                    IMObjectBean bean = new IMObjectBean(reminderCount);
                    int count = bean.getInt("count");
                    if (count >= maxCount) {
                        maxCount = count + 1;
                    }
                }
                IMObjectBean bean = new IMObjectBean(object);
                bean.setValue("count", maxCount);
            }
            return object;
        }

        /**
         * Adds an object to the collection.
         *
         * @param object the object to add
         */
        @Override
        public boolean add(IMObject object) {
            boolean result = super.add(object);
            resequence();
            return result;
        }

        /**
         * Removes an object from the collection.
         *
         * @param object the object to remove
         */
        @Override
        public void remove(IMObject object) {
            super.remove(object);
            resequence();
        }

        /**
         * Invoked when two objects are swapped.
         * <p>
         *
         * @param object1 the first object
         * @param object2 the second object
         */
        @Override
        protected void swapped(IMObject object1, IMObject object2) {
            resequence();
        }

        /**
         * Validates the object.
         * <p>
         * This validates the current object being edited, and if valid, the collection.
         *
         * @param validator the validator
         * @return {@code true} if the object and its descendants are valid otherwise {@code false}
         */
        @Override
        protected boolean doValidation(Validator validator) {
            return super.doValidation(validator) && validateOverdueIntervals(validator);
        }

        /**
         * Ensures that overdue intervals are in increasing order by reminder count.
         * <p/>
         * This converts intervals to dates based on the current date, so may allow 30 days < 1 month in January, but
         * not June for example.
         *
         * @param validator the validator
         * @return {@code true} if the overdue intervals are valid, otherwise {@code false}
         */
        private boolean validateOverdueIntervals(Validator validator) {
            boolean valid = true;
            DateTime from = new DateTime();
            SortedMap<Integer, DateTime> overdue = new TreeMap<>();
            for (IMObject object : getCurrentObjects()) {
                IMObjectBean bean = new IMObjectBean(object);
                int count = bean.getInt("count");
                int interval = bean.getInt("interval");
                String units = bean.getString("units");
                if (units != null) {
                    Period period = DateUnits.fromString(units).toPeriod(interval);
                    overdue.put(count, from.withPeriodAdded(period, 1));
                }
            }
            Map.Entry<Integer, DateTime> last = null;
            for (Map.Entry<Integer, DateTime> entry : overdue.entrySet()) {
                if (last == null || entry.getValue().compareTo(last.getValue()) > 0) {
                    last = entry;
                } else {
                    int count1 = last.getKey();
                    int count2 = entry.getKey();
                    String message = Messages.format("reminder.type.overdueintervals", count1, count2);
                    validator.add(this, new ValidatorError(message));
                    valid = false;
                    break;
                }
            }
            return valid;
        }

        /**
         * Re-sequences reminder counts.
         */
        private void resequence() {
            int count = 0;
            for (IMObject reminderCount : getCurrentObjects()) {
                IMObjectEditor editor = getEditor(reminderCount);
                editor.getProperty("count").setValue(count++);
            }
        }
    }
}
