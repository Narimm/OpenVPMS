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

package org.openvpms.etl.load;

import org.openvpms.component.business.domain.archetype.ArchetypeId;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;


/**
 * Represents a mapping between a legacy database row and its corresponding
 * {@link IMObject}.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
public class ETLLog {

    /**
     * The log identifier.
     */
    private long logId;

    /**
     * The version.
     */
    private long version;

    /**
     * The loader name. This can be used to help uniquely identify rows
     * in those cases where the row identifier, and the target
     * archetypes and index is not enough.
     */
    private String loader;

    /**
     * The legacy row identifier.
     */
    private String rowId;

    /**
     * The target archetype short name.
     */
    private String archetype;

    /**
     * The target object's identifier.
     */
    private long id = -1;

    /**
     * The target object's link identifier.
     */
    private String linkId;

    /**
     * The collection index, or <tt>-1</tt> if the target object is not part of
     * a collection.
     */
    private int index;

    /**
     * Mapping errors.
     */
    private String errors;


    /**
     * Constructs a new <tt>ETLLog</tt>.
     */
    public ETLLog() {
        this(null, null, null);
    }

    /**
     * Constructs a new <tt>ETLLog</tt>.
     *
     * @param loader    the loader name
     * @param rowId     the legacy row identifier
     * @param archetype the target archetype short name
     */
    public ETLLog(String loader, String rowId, String archetype) {
        this(loader, rowId, archetype, -1);
    }

    /**
     * Constructs a new <tt>ETLLog</tt>.
     *
     * @param loader    the loader name
     * @param rowId     the legacy row identifier
     * @param archetype the target archetype short name
     * @param index     the target collection index. May be <tt>-1</tt> to
     *                  indicate that the object is not part of a collection
     */
    public ETLLog(String loader, String rowId, String archetype,
                  int index) {
        this.rowId = rowId;
        this.archetype = archetype;
        this.loader = loader;
        this.index = index;
    }

    /**
     * Returns the log identifier.
     * This is unique across all {@link ETLLog} instances.
     *
     * @return the log identifier
     */
    public long getLogId() {
        return logId;
    }

    /**
     * Sets the log identifier.
     *
     * @param logId the log identifier
     */
    public void setLogId(long logId) {
        this.logId = logId;
    }

    /**
     * Returns the version.
     *
     * @return the version
     */
    public long getVersion() {
        return version;
    }

    /**
     * Sets the version.
     *
     * @param version the version
     */
    public void setVersion(long version) {
        this.version = version;
    }

    /**
     * Returns the loader name.
     *
     * @return the loader name
     */
    public String getLoader() {
        return loader;
    }

    /**
     * Sets the loader name.
     *
     * @param loader the loader name
     */
    public void setLoader(String loader) {
        this.loader = loader;
    }

    /**
     * Returns the legacy row identifier.
     *
     * @return the legacy row identifier
     */
    public String getRowId() {
        return rowId;
    }

    /**
     * Sets the legacy row identifier.
     *
     * @param rowId the legacy row identifier
     */
    public void setRowId(String rowId) {
        this.rowId = rowId;
    }

    /**
     * Returns the target archetype short name.
     *
     * @return the target archetype short name
     */
    public String getArchetype() {
        return archetype;
    }

    /**
     * Sets the target archetype short name.
     *
     * @param archetype the target archetype short name
     */
    public void setArchetype(String archetype) {
        this.archetype = archetype;
    }

    /**
     * Returns the target object's collection index.
     *
     * @return the collection index, or <tt>-1</tt> if the object is not part
     *         of a collection
     */
    public int getIndex() {
        return index;
    }

    /**
     * Sets the target object's collection index.
     *
     * @param index the collection index. May be <tt>-1</tt> to indicate that
     *              the object is not part of a collection
     */
    public void setIndex(int index) {
        this.index = index;
    }

    /**
     * Sets the target object's reference.
     *
     * @param reference the reference. May be <tt>null</tt>
     */
    public void setReference(IMObjectReference reference) {
        if (reference != null) {
            id = reference.getId();
            linkId = reference.getLinkId();
        } else {
            id = -1;
            linkId = null;
        }
    }

    /**
     * Returns the target object's reference.
     *
     * @return the reference, or <tt>null</tt> if it is unknown
     */
    public IMObjectReference getReference() {
        if (id == -1 && linkId == null) {
            return null;
        }
        return new IMObjectReference(new ArchetypeId(archetype), id, linkId);
    }

    /**
     * Returns a formatted error string produced during the mapping.
     *
     * @return the errors. May be <tt>null</tt>
     */
    public String getErrors() {
        return errors;
    }

    /**
     * Sets the errors.
     *
     * @param errors the errors. May be <tt>null</tt>
     */
    public void setErrors(String errors) {
        this.errors = errors;
    }

    /**
     * Returns the target object's identifier.
     *
     * @return the target object's identifier
     */
    protected long getId() {
        return id;
    }

    /**
     * Sets the target object's identifier.
     *
     * @param id the target object's identifier
     */
    protected void setId(long id) {
        this.id = id;
    }

    /**
     * Returns the target object's link identifier.
     *
     * @return the target object's link identifier
     */
    protected String getLinkId() {
        return linkId;
    }

    /**
     * Sets the target object's link identifier.
     *
     * @param linkId the target object's link identifier
     *
     */
    protected void setLinkId(String linkId) {
        this.linkId = linkId;
    }

}
