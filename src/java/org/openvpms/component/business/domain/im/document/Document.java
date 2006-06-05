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


package org.openvpms.component.business.domain.im.document;

// openvpms-common
import org.apache.commons.lang.builder.ToStringBuilder;

// openvpms-framework
import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.datatypes.basic.DynamicAttributeMap;

/**
 * Models any type of resource, which has a mime type, a size and holds the
 * contents 
 *
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class Document extends IMObject {

    /**
     * Default SUID
     */
    private static final long serialVersionUID = 1L;

    /**
     * The mime type
     */
    private String mimeType;
    
    /**
     * The size of the document
     */
    private long docSize;
    
    /**
     * The checksum of the contents
     */
    private long checksum;
    
    /**
     * The contents
     */
    private byte[] contents;
    
    /**
     * Details holds dynamic attributes for a lookup
     */
    private DynamicAttributeMap details = new DynamicAttributeMap();

    /**
     * default constructor
     */
    public Document() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @param archetypeId
     */
    public Document(ArchetypeId archetypeId) {
        super(archetypeId);
        // TODO Auto-generated constructor stub
    }

    /**
     * @return Returns the checksum.
     */
    public long getChecksum() {
        return checksum;
    }

    /**
     * @param checksum The checksum to set.
     */
    public void setChecksum(long checksum) {
        this.checksum = checksum;
    }

    /**
     * @return Returns the contents.
     */
    public byte[] getContents() {
        return contents;
    }

    /**
     * @param contents The contents to set.
     */
    public void setContents(byte[] contents) {
        this.contents = contents;
    }

    /**
     * @return Returns the docSize.
     */
    public long getDocSize() {
        return docSize;
    }

    /**
     * @param docSize The docSize to set.
     */
    public void setDocSize(long docSize) {
        this.docSize = docSize;
    }

    /**
     * @return Returns the mimeType.
     */
    public String getMimeType() {
        return mimeType;
    }

    /**
     * @param mimeType The mimeType to set.
     */
    public void setMimeType(String mimeType) {
        this.mimeType = mimeType;
    }

    /**
     * @return Returns the details.
     */
    public DynamicAttributeMap getDetails() {
        return details;
    }

    /**
     * @param details The details to set.
     */
    public void setDetails(DynamicAttributeMap details) {
        this.details = details;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#clone()
     */
    @Override
    public Object clone() throws CloneNotSupportedException {
        Document copy = (Document)super.clone();
        
        copy.mimeType = this.mimeType;
        copy.docSize = this.docSize;
        copy.checksum = this.checksum;
        
        // copy the contents
        copy.contents = new byte[this.contents.length];
        System.arraycopy(this.contents, 0, copy.contents, 0, copy.contents.length);
        
        // details
        copy.details = (DynamicAttributeMap)(this.details == null ?
                null : this.details.clone());

        return copy;
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.domain.im.common.IMObject#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .appendSuper(null)
            .append("mimeType", mimeType)
            .append("docSize", docSize)
            .append("checksum", checksum)
            .toString();
    }
}
