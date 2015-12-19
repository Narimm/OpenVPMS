/*
 * Version: 1.0
 *
 * The contents of this file are subject to the OpenVPMS License Version
 * 1.0 (the 'License'); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 * http://www.openvpms.org/license/
 *
 * Software distributed under the License is distributed on an 'AS IS' basis,
 * WITHOUT WARRANTY OF ANY KIND, either express or implied. See the License
 * for the specific language governing rights and limitations under the
 * License.
 *
 * Copyright 2015 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.webdav.resource;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
import org.openvpms.component.system.common.query.ArchetypeQuery;
import org.openvpms.component.system.common.query.Constraints;
import org.openvpms.component.system.common.query.IMObjectQueryIterator;
import org.openvpms.report.DocFormats;

import java.util.HashSet;
import java.util.Set;

/**
 * Determines the document archetypes that may be edited via WebDAV.
 *
 * @author Tim Anderson
 */
public class EditableDocuments {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The document archetypes that may be edited.
     */
    private final String[] shortNames;

    /**
     * The logger.
     */
    private static final Log log = LogFactory.getLog(EditableDocuments.class);

    /**
     * Constructs a {@link EditableDocuments}.
     *
     * @param service    the archetype service
     * @param archetypes the archetype short names
     */
    public EditableDocuments(IArchetypeService service, String[] archetypes) {
        this.service = service;
        this.shortNames = getShortNames(archetypes, service);
    }

    /**
     * Returns the document archetypes that may be edited.
     *
     * @return the document archetype short names
     */
    public String[] getArchetypes() {
        return shortNames;
    }

    /**
     * Determines if a document can be edited.
     *
     * @param act the document act
     * @return {@code true} if the document can be edited
     */
    public boolean canEdit(DocumentAct act) {
        if (act.getDocument() != null && TypeHelper.isA(act, shortNames)) {
            String ext = FilenameUtils.getExtension(act.getFileName());
            return DocFormats.ODT_EXT.equalsIgnoreCase(ext)
                   || DocFormats.DOC_EXT.equalsIgnoreCase(ext)
                   || DocFormats.DOCX_EXT.equalsIgnoreCase(ext)
                   || DocFormats.ODT_TYPE.equals(act.getMimeType())
                   || DocFormats.DOC_TYPE.equals(act.getMimeType())
                   || DocFormats.DOCX_TYPE.equals(act.getMimeType());
        }
        return false;
    }

    /**
     * Returns an editable document act.
     *
     * @param id the act identifier
     * @return the corresponding act, or {@code null} if it does not exist or is not editable
     */
    public DocumentAct getDocumentAct(long id) {
        ArchetypeQuery query = new ArchetypeQuery(shortNames, false, true);
        query.add(Constraints.eq("id", id));
        IMObjectQueryIterator<DocumentAct> iterator = new IMObjectQueryIterator<>(service, query);
        return (iterator.hasNext()) ? iterator.next() : null;
    }

    /**
     * Returns the supported document act archetype short names.
     *
     * @return the document act archetype short names
     */
    private synchronized String[] getShortNames(String[] archetypes, IArchetypeService service) {
        Set<String> result = new HashSet<>();
        for (String archetype : archetypes) {
            String[] shortNames = DescriptorHelper.getShortNames(archetype);
            if (shortNames.length == 0) {
                log.error("'" + archetype + "' is not a valid archetype");
            } else {
                for (String shortName : shortNames) {
                    ArchetypeDescriptor descriptor = service.getArchetypeDescriptor(shortName);
                    if (descriptor != null) {
                        Class clazz = descriptor.getClazz();
                        if (clazz != null && DocumentAct.class.isAssignableFrom(clazz)) {
                            if (descriptor.getNodeDescriptor("document") != null) {
                                result.add(descriptor.getType().getShortName());
                            } else {
                                log.error("'" + shortName + "' is not a valid archetype");
                            }
                        }
                    }
                }
            }
        }
        return result.toArray(new String[result.size()]);
    }

}
