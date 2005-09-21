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
 *  Copyright 2005 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $$Id$$
 */

package org.openvpms.component.presentation.tapestry.callback;

import java.util.HashMap;

import ognl.Ognl;
import ognl.OgnlException;
import org.apache.tapestry.ApplicationRuntimeException;

/**
 * This guy is responsible for returning from an add or remove on a collection.
 * 
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class CollectionCallback extends EditCallback {

    private static final long serialVersionUID = 1L;

    private String addOgnlExpression;

    private String removeOgnlExpression;

    private boolean childRelationship;

    /**
     * @param pageName
     * @param model
     * @param addOgnl
     * @param removeOgnl
     */
    public CollectionCallback(String pageName, Object model, String addOgnl,
            String removeOgnl) {
        super(pageName, model);
        this.addOgnlExpression = addOgnl;
        this.removeOgnlExpression = removeOgnl;
    }

    public void add(Object newObject) {
        executeOgnlExpression(addOgnlExpression, newObject);
    }

    public void remove(Object object) {
        executeOgnlExpression(removeOgnlExpression, object);
    }

    /**
     * @param previousModel
     */
    @SuppressWarnings("unchecked")
    private void executeOgnlExpression(String ognlExpression, Object newObject) {
        HashMap context = new HashMap();
        context.put("member", newObject);

        try {
            Ognl.getValue(ognlExpression + "(#member)", context, model);
        } catch (OgnlException e) {
            throw new ApplicationRuntimeException(e);
        }
    }

    /**
     * @return Returns the addOgnlExpression.
     */
    public String getAddOgnlExpression() {
        return addOgnlExpression;
    }

    /**
     * @return Returns the removeOgnlExpression.
     */
    public String getRemoveOgnlExpression() {
        return removeOgnlExpression;
    }

    /**
     * @return
     */
    public boolean isChildRelationship() {
        return childRelationship;
    }

    /**
     * @param child
     */
    public void setChildRelationship(boolean child) {
        this.childRelationship = child;
    }

}
