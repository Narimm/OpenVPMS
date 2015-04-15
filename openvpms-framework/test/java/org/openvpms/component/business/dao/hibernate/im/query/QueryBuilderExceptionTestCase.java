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
 *  Copyright 2010 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.component.business.dao.hibernate.im.query;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.openvpms.component.business.service.archetype.helper.PropertyResolverException;

/**
 * {@link org.openvpms.component.business.dao.hibernate.im.query.QueryBuilderException} test case.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class QueryBuilderExceptionTestCase {

    /**
     * Verifies that the messages are generated correctly.
     */
    @Test
    public void testMessages() {
        assertEquals("Need to update tests to incorporate new messages",
                     19, QueryBuilderException.ErrorCode.values().length);
        checkException(QueryBuilderException.ErrorCode.NullQuery, "You must specify a non-null query");
        checkException(QueryBuilderException.ErrorCode.NoShortNamesSpecified,
                       "You must specify at least one short name");
        checkException(QueryBuilderException.ErrorCode.MustSpecifyNodeName, "A node name must be specified");
        checkException(QueryBuilderException.ErrorCode.InvalidQualifiedName, "Invalid qualified name: foo", "foo");
        checkException(QueryBuilderException.ErrorCode.NoNodeDescriptorForName,
                       "There is no node descriptor with name foo", "foo");
        checkException(QueryBuilderException.ErrorCode.OperatorNotSupported,
                       "The operator foo is not supported", "foo");
        checkException(QueryBuilderException.ErrorCode.CanOnlySortOnTopLevelNodes,
                       "Can only sort on top level nodes");
        checkException(QueryBuilderException.ErrorCode.CannotQueryAcrossTypes,
                       "Query failed because foo and bar are of different types", "foo", "bar");
        checkException(QueryBuilderException.ErrorCode.NoNodeDescWithName,
                       "The archetype foo does not have a node descriptor of name bar", "foo", "bar");
        checkException(QueryBuilderException.ErrorCode.NodeDescriptorsDoNotMatch,
                       "Not all archetypes have a node descriptor with name foo", "foo");
        checkException(QueryBuilderException.ErrorCode.ConstraintTypeNotSupported,
                       "The constraint foo is not supported", "foo");
        checkException(QueryBuilderException.ErrorCode.NoArchetypesForId, "There is no archetype with id foo", "foo");
        checkException(QueryBuilderException.ErrorCode.InvalidObjectReferenceConstraint,
                       "Invalid object reference constraint: foo", "foo");
        checkException(QueryBuilderException.ErrorCode.NoMatchingArchetypesForId,
                       "Could not find any matching archetypes for archetype id foo", "foo");
        checkException(QueryBuilderException.ErrorCode.NoMatchingArchetypesForShortName,
                       "Could not find any matching archetypes for short names foo", "foo");
        checkException(QueryBuilderException.ErrorCode.NoMatchingArchetypesForLongName,
                       "Could not find any matching archetypes for rmName: foo entityName: bar and conceptName goo",
                       "foo", "bar", "goo");
        checkException(QueryBuilderException.ErrorCode.NoArchetypeRangeAssertion,
                       "No archetypeRange assertion defined for archetype foo and node bar", "foo", "bar");
        checkException(QueryBuilderException.ErrorCode.DuplicateAlias, "Duplicate alias foo", "foo");
        checkException(QueryBuilderException.ErrorCode.CannotJoinDuplicateAlias,
                       "Cannot join on property foo with alias bar. Alias already exists", "foo", "bar");
    }

    /**
     * Creates an {@link PropertyResolverException} with the supplied code and
     * arguments and verifies that the generated message matches that expected.
     *
     * @param code     the error code
     * @param expected the expected message
     * @param args     exception arguments
     */
    private void checkException(QueryBuilderException.ErrorCode code,
                                String expected, Object... args) {
        QueryBuilderException exception = new QueryBuilderException(code, args);
        assertEquals(code, exception.getErrorCode());
        assertEquals(expected, exception.getMessage());
    }
}
