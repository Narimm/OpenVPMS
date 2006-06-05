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


package org.openvpms.component.business.domain.im.act;

// commons-lang
import org.apache.commons.lang.builder.ToStringBuilder;

//openvpms-framework
import org.openvpms.component.business.domain.im.common.IMObjectReference;

/**
 * Document specific-act
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class DocumentAct extends Act {
    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The version of the document
     */
    private String docVersion;
    
    /**
     * A reference to the actual {@link Document}
     */
    private IMObjectReference docReference;

    /**
     * Default constructor
     */
    public DocumentAct() {
        super();
    }

    /**
     * @return Returns the document.
     */
    public IMObjectReference getDocReference() {
        return docReference;
    }

    /**
     * @param document The document to set.
     */
    public void setDocReference(IMObjectReference reference) {
        this.docReference = reference;
    }

    /**
     * @return Returns the version.
     */
    public String getDocVersion() {
        return docVersion;
    }

    /**
     * @param version The version to set.
     */
    public void setDocVersion(String version) {
        this.docVersion = version;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.act.Act#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        DocumentAct copy = (DocumentAct)super.clone();
        
        copy.docVersion = this.docVersion;
        copy.docReference = (IMObjectReference)this.docReference.clone();
        
        return copy;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.act.Act#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .appendSuper(null)
            .append("docVersion", docVersion)
            .append("docReference", docReference)
            .toString();
    }

    
}
