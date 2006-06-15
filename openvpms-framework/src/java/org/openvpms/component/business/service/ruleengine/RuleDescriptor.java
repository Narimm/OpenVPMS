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


package org.openvpms.component.business.service.ruleengine;

import org.apache.commons.lang.builder.ToStringBuilder;

/**
 * A rule descriptor defines the meta data for a particular rule.
 * 
 * @author   <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version  $LastChangedDate$
 */
public class RuleDescriptor {
    public enum SourceType {
        CLASSPATH,
        SYSTEM;

        /* (non-Javadoc)
         * @see java.lang.Enum#toString()
         */
        @Override
        public String toString() {
            return super.toString().toLowerCase();
        }
    };
    
    /**
     * The source of the rule, which indicates if the path resolves to a classpath
     * or a normal system path. If it is not specified then it defaults to 
     * system
     */
    private SourceType source = SourceType.SYSTEM;
    
    /**
     * This indicates the path of the rule
     */
    private String path; 
        
    /**
     * Default constructor
     */
    public RuleDescriptor() {
        // do nothing
    }

    /**
     * @return Returns the path.
     */
    public String getPath() {
        return path;
    }

    /**
     * @param path The path to set.
     */
    public void setPath(String path) {
        this.path = path;
    }

    /**
     * @return Returns the source.
     */
    public String getSourceAsString() {
        return source.toString();
    }

    /**
     * @param source The source to set.
     */
    public void setSourceAsString(String source) {
        if (source.toLowerCase().equals(SourceType.SYSTEM.toString())) {
            this.source = SourceType.SYSTEM;
        } else if (source.toLowerCase().equals(SourceType.CLASSPATH.toString())) {
            this.source = SourceType.CLASSPATH;
        } else {
            // throw an exception
            throw new RuleEngineException(
                    RuleEngineException.ErrorCode.InvalidSourceSpecified,
                    new Object[] {source});
        }
    }

    /**
     * @return Returns the source.
     */
    public SourceType getSource() {
        return source;
    }

    /**
     * @param source The source to set.
     */
    public void setSource(SourceType source) {
        this.source = source;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return new ToStringBuilder(this)
            .append("source", source)
            .append("path", path)
            .toString();
    }
    
    
}
