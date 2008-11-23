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

import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.web.component.im.edit.AbstractCollectionPropertyEditorTest;
import org.openvpms.web.component.im.edit.CollectionPropertyEditor;
import org.openvpms.web.component.property.CollectionProperty;
import org.openvpms.web.system.ServiceHelper;
import org.openvpms.web.test.TestHelper;


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
     *
     * @throws Exception for any error
     */
    @Override
    protected void onSetUp() throws Exception {
        super.onSetUp();
        ILookupService service = ServiceHelper.getLookupService();
        Lookup lookup = service.getLookup("lookup.state", "VIC");
        if (lookup != null) {
            lookup = (Lookup) get(lookup);
            if (lookup != null) {
                // remove existing relationships
                lookup.getSourceLookupRelationships().clear();
                TestHelper.save(lookup);
            }
        }
        if (lookup == null) {
            lookup = (Lookup) TestHelper.create("lookup.state");
            lookup.setCode("VIC");
            lookup.setName("Victoria");
            TestHelper.save(lookup);
        }
        state = lookup;
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
        LookupRelationship result = (LookupRelationship) TestHelper.create(
                "lookupRelationship.stateSuburb");
        result.setSource(parent.getObjectReference());
        Lookup target = (Lookup) TestHelper.create("lookup.suburb");
        target.setCode("ASUBURB" + System.currentTimeMillis());
        target.setName("A Suburb");
        TestHelper.save(target);
        result.setTarget(target.getObjectReference());
        return result;
    }
}
