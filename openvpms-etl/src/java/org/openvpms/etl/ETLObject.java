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
 *  Copyright 2007 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */

package org.openvpms.etl;

import java.util.ArrayList;
import java.util.List;

/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ETLObject {

    private long objectId;
    private String legacyId;
    private String archetype;
    private List<ETLNode> nodes;

    public ETLObject() {
    }

    public ETLObject(String archetype) {
        setArchetype(archetype);
    }

    public long getObjectId() {
        return objectId;
    }

    public void setObjectId(long objectId) {
        this.objectId = objectId;
    }

    public String getLegacyId() {
        return legacyId;
    }

    public void setLegacyId(String legacyId) {
        this.legacyId = legacyId;
    }

    public String getArchetype() {
        return archetype;
    }

    public void setArchetype(String archetype) {
        this.archetype = archetype;
    }

    public List<ETLNode> getNodes() {
        return nodes;
    }

    public ETLNode getNode(String name) {
        for (ETLNode node : nodes) {
            if (name.equals(node.getName())) {
                return node;
            }
        }
        return null;
    }

    public void addNode(ETLNode node) {
        if (nodes == null) {
            nodes = new ArrayList<ETLNode>();
        }
        nodes.add(node);
        node.setObject(this);
    }

    protected void setNodes(List<ETLNode> nodes) {
        this.nodes = nodes;
    }
}
