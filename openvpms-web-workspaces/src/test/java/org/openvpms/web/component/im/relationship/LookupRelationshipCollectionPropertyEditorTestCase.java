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

package org.openvpms.web.component.im.relationship;

import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.web.component.im.edit.AbstractCollectionPropertyEditorTest;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.property.CollectionProperty;


/**
 * Tests the {@link LookupRelationshipCollectionPropertyEditor} class.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class LookupRelationshipCollectionPropertyEditorTestCase
    extends AbstractCollectionPropertyEditorTest {

    private Lookup state;

    /**
     * Sets up the test case.
     */
    @Override
    public void setUp() {
        super.setUp();
        state = TestHelper.getLookup("lookup.state", "VIC");
        if (!state.getLookupRelationships().isEmpty()) {
            // remove existing relationships
            state.getSourceLookupRelationships().clear();
            save(state);
        }
    }

    /**
     * Returns the parent of the collection.
     *
     * @return the parent object
     */
    protected IMObject createParent() {
        return state;
    }

    /**
     * Returns the name of the collection node.
     *
     * @return the node name
     */
    protected String getCollectionNode() {
        return "source";
    }

    /**
     * Returns an editor for a collection property.
     *
     * @param property the collection property
     * @param parent   the parent of the collection
     * @return a new editor for the property
     */
    protected CollectionPropertyEditor createEditor(CollectionProperty property,
                                                    IMObject parent) {
        return new LookupRelationshipCollectionPropertyEditor(property,
                                                              (Lookup) parent);
    }

    /**
     * Returns an object to add to the collection.
     *
     * @param parent the parent of the collection
     * @return a new object to add to the collection
     */
    protected IMObject createObject(IMObject parent) {
        LookupRelationship result = (LookupRelationship) TestHelper.create("lookupRelationship.stateSuburb");
        result.setSource(parent.getObjectReference());
        String code = "ASUBURB" + System.currentTimeMillis() + System.nanoTime();
        Lookup target = TestHelper.getLookup("lookup.suburb", code);
        result.setTarget(target.getObjectReference());
        return result;
    }
}
