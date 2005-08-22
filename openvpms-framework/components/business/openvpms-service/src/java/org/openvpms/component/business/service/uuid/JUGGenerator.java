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


package org.openvpms.component.business.service.uuid;

// commons-lang
import org.apache.commons.lang.StringUtils;

//jug
import org.safehaus.uuid.UUIDGenerator;
import org.safehaus.uuid.EthernetAddress;

/**
 * This is an implementation of the {@link UUIDGenerator} interface based on the 
 * Java Uuid Generator (JUG) library.
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class JUGGenerator implements IUUIDGenerator {
    /**
     * A reference to the UUID generator
     */
    public UUIDGenerator generator;

    /**
     * The Ethernet addressthat is used for generation. If one is not
     * specified the one will be automatically generated.
     */
    private EthernetAddress ethernetAddress;
    
    /**
     * Instantiate an instance of the UUID generator using the specified 
     * ethernet address. The specified ethernet address must be 6-byte
     * MAC address that complies with the IEEE 802.1 standard.
     */
    public JUGGenerator(String ethernetAddress) 
    throws UUIDServiceException {
        try {
            generator = UUIDGenerator.getInstance();
            if (StringUtils.isEmpty(ethernetAddress)) {
                this.ethernetAddress = generator.getDummyAddress();
            } else {
                this.ethernetAddress = new EthernetAddress(ethernetAddress);
            }
        } catch (Exception exception) {
            throw new UUIDServiceException(
                    UUIDServiceException.ErrorCode.FailedToInitializeService,
                    new Object[] {this.getClass().getName()}, exception);
        }
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.uuid.IUUIDGenerator#nextId()
     */
    public String nextId() {
        return generator.generateTimeBasedUUID(this.ethernetAddress).toString();
    }

    /* (non-Javadoc)
     * @see org.openvpms.component.business.service.uuid.IUUIDGenerator#nextId(java.lang.String)
     */
    public String nextId(String prefix) {
        return new StringBuffer(prefix)
            .append(generator.generateTimeBasedUUID(this.
                    ethernetAddress).toString())
            .toString();
    }

}
