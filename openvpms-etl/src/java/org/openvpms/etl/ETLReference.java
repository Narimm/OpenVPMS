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


/**
 * Add description here.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ETLReference {

    private long referenceId;
    private ETLObject object;
    private String reference;
    private long version;

    public ETLReference() {
    }

    public ETLReference(ETLObject object) {
        this.object = object;
    }

    public ETLReference(String reference) {
        this.reference = reference;
    }

    public void setReferenceId(long referenceId) {
        this.referenceId = referenceId;
    }

    public long getReferenceId() {
        return referenceId;
    }

    public void setObject(ETLObject object) {
        this.object = object;
        reference = null;
    }

    public ETLObject getObject() {
        return object;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getReference() {
        return reference;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
