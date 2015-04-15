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

package org.openvpms.component.system.common.query;


/**
 * Used to create a query constraint on participation fields duplicated
 * from the associated act.
 * <p/>
 * These can be used to improve query performance.
 * <p/>
 * NOTE: this class will be removed in 2.0, when primary participation entities
 * are moved to the act.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ParticipationConstraint implements IConstraint {

    /**
     * The supported constraint fields.
     */
    public enum Field {
        ActShortName,
        StartTime,
        EndTime
    }

    /**
     * The constraint alias.
     */
    private final String alias;

    /**
     * The operator.
     */
    private final RelationalOp operator;

    /**
     * The field.
     */
    private final Field field;

    /**
     * The value.
     */
    private final Object value;


    /**
     * Creates a new <tt>ParticipationConstraint</tt>.
     *
     * @param field the field
     * @param value the field value
     */
    public ParticipationConstraint(Field field, Object value) {
        this(field, RelationalOp.EQ, value);
    }

    /**
     * Creates a new <tt>ParticipationConstraint</tt>.
     *
     * @param field    the field
     * @param operator the operator
     * @param value    the field value
     */
    public ParticipationConstraint(Field field, RelationalOp operator,
                                   Object value) {
        this(null, field, operator, value);
    }

    /**
     * Creates a new <tt>ParticipationConstraint</tt>.
     *
     * @param alias    the constraint alias
     * @param field    the field
     * @param operator the operator
     * @param value    the field value
     */
    public ParticipationConstraint(String alias, Field field,
                                   RelationalOp operator, Object value) {
        this.alias = alias;
        this.field = field;
        this.operator = operator;
        this.value = value;
    }

    /**
     * Returns the type alias.
     *
     * @return the type alias. May be <tt>null</tt>
     */
    public String getAlias() {
        return alias;
    }

    /**
     * Returns the field.
     *
     * @return the field
     */
    public Field getField() {
        return field;
    }

    /**
     * Returns the operator.
     *
     * @return the operator
     */
    public RelationalOp getOperator() {
        return operator;
    }

    /**
     * Returns the field value.
     *
     * @return the value
     */
    public Object getValue() {
        return value;
    }

}


