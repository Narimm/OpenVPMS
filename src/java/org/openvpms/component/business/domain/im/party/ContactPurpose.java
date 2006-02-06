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
 *  $Id$
 */

package org.openvpms.component.business.domain.im.party;

// java core
import java.util.Date;

// openvpms-framework
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;

/**
 * A contact purpose scoped to the {@link Contact} object and is archetyped. It
 * identified the purpose for a specific contact. A {@link Contact} has one or 
 * more contact purpose instances.
 * 
 * @author <a href="mailto:support@openvpms.org>OpenVPMS Team</a>
 * @version $LastChangedDate$
 */
public class ContactPurpose extends IMObject {

    /**
     * Generated SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The date that this purpose become active
     */
    private Date fromDate;
    
    /**
     * The contact purpose is inactive after this date.
     */
    private Date thruDate;

    /**
     * Specific details for the contact purpose.
     */
    private DynamicAttributeMap details;

    /**
     * A reference to the owning {@link Contact}
     */
    private Contact contact;
    

    /**
     * Define a protected default constructor
     */
    public ContactPurpose() {
    }
    
    /**
     * @return Returns the details.
     */
    public DynamicAttributeMap getDetails() {
        return details;
    }

    /**
     * @param details
     *            The details to set.
     */
    public void setDetails(DynamicAttributeMap details) {
        this.details = details;
    }
    
    /**
     * @return Returns the contact.
     */
    public Contact getContact() {
        return contact;
    }

    /**
     * @param contact The contact to set.
     */
    public void setContact(Contact contact) {
        this.contact = contact;
    }

    /**
     * @return Returns the fromDate.
     */
    public Date getFromDate() {
        return fromDate;
    }

    /**
     * @param fromDate The fromDate to set.
     */
    public void setFromDate(Date fromDate) {
        this.fromDate = fromDate;
    }

    /**
     * @return Returns the thruDate.
     */
    public Date getThruDate() {
        return thruDate;
    }

    /**
     * @param thruDate The thruDate to set.
     */
    public void setThruDate(Date thruDate) {
        this.thruDate = thruDate;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        ContactPurpose copy = (ContactPurpose)super.clone();
        copy.contact = this.contact;
        copy.details = (DynamicAttributeMap)(this.details == null ?
                null : this.details.clone());
        copy.fromDate = (Date)(this.fromDate == null ?
                null : this.fromDate.clone());
        copy.thruDate = (Date)(this.thruDate == null ?
                null : this.thruDate.clone());
        
        return copy;
    }
}
