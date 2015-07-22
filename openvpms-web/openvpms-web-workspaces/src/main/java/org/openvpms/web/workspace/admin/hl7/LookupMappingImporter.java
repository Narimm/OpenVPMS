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

package org.openvpms.web.workspace.admin.hl7;

import net.sf.jasperreports.engine.util.ObjectUtils;
import org.apache.commons.lang.StringUtils;
import org.openvpms.archetype.rules.doc.DocumentHandlers;
import org.openvpms.component.business.domain.im.common.IMObjectRelationship;
import org.openvpms.component.business.domain.im.document.Document;
import org.openvpms.component.business.domain.im.lookup.Lookup;
import org.openvpms.component.business.domain.im.lookup.LookupRelationship;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.web.component.error.ErrorFormatter;
import org.openvpms.web.resource.i18n.Messages;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Imports lookup mappings from a CSV document.
 *
 * @author Tim Anderson
 */
public class LookupMappingImporter {

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * The document handlers.
     */
    private DocumentHandlers handlers;

    /**
     * The field separator.
     */
    private final char separator;

    /**
     * Constructs a {@link LookupMappingImporter}.
     *
     * @param service   the archetype service
     * @param lookups   the lookups
     * @param handlers  the document handlers
     * @param separator the field separator
     */
    public LookupMappingImporter(IArchetypeService service, ILookupService lookups, DocumentHandlers handlers,
                                 char separator) {
        this.service = service;
        this.lookups = lookups;
        this.handlers = handlers;
        this.separator = separator;
    }

    /**
     * Imports lookup mappings.
     * <p/>
     * Any mapping already present will be ignored.
     *
     * @param document the CSV document to import
     * @return the loaded mappings, along with any errors encountered.
     */
    public LookupMappings load(Document document) {
        LookupMappingCSVReader reader = new LookupMappingCSVReader(handlers, separator);
        LookupMappings mappings = reader.read(document);
        if (mappings.getErrors().isEmpty()) {
            mappings = load(mappings);
        }
        return mappings;
    }

    /**
     * Loads mappings.
     *
     * @param mappings the mappings
     * @return the loaded mappings, along with any errors
     */
    private LookupMappings load(LookupMappings mappings) {
        List<LookupMapping> success = new ArrayList<>();
        List<LookupMapping> errors = new ArrayList<>();
        for (LookupMapping mapping : mappings.getMappings()) {
            Lookup from = getLookup(mapping.getFromType(), mapping.getFromCode(), mapping.getFromName(), mapping);
            Lookup to = getLookup(mapping.getToType(), mapping.getToCode(), mapping.getToName(), mapping);
            if (from == null || to == null) {
                errors.add(mapping);
                break;
            } else {
                try {
                    IMObjectBean bean = new IMObjectBean(from, service);
                    if (!bean.hasNodeTarget("mapping", to)) {
                        IMObjectRelationship relationship = bean.addNodeTarget("mapping", to);
                        to.addLookupRelationship((LookupRelationship) relationship);
                        service.save(Arrays.asList(from, to));
                        success.add(mapping);
                    }
                } catch (Throwable exception) {
                    mapping.setError(ErrorFormatter.format(exception));
                    errors.add(mapping);
                    break;
                }
            }
        }
        return new LookupMappings(success, errors);
    }

    /**
     * Returns a lookup given its archetype, code and name, creating it if required.
     *
     * @param shortName the lookup archetype
     * @param code      the lookup code
     * @param name      the lookup name
     * @param mapping   the mapping
     * @return the lookup, or {@code null} if it was not found or the arguments are invalid
     */
    private Lookup getLookup(String shortName, String code, String name, LookupMapping mapping) {
        Lookup result = null;
        String[] matches = DescriptorHelper.getShortNames(shortName);
        if (matches.length != 1 || !StringUtils.equals(matches[0], shortName)
            || !ObjectUtils.equals(DescriptorHelper.getArchetypeDescriptor(shortName).getClazz(), Lookup.class)) {
            mapping.setError(Messages.format("admin.hl7.mapping.import.invalidArch", shortName));
        } else {
            Lookup lookup = lookups.getLookup(shortName, code);
            if (lookup == null) {
                lookup = (Lookup) service.create(shortName);
                if (lookup != null) {
                    lookup.setCode(code);
                    lookup.setName(name);
                    result = lookup;
                }
            } else if (!StringUtils.equalsIgnoreCase(name, lookup.getName())) {
                mapping.setError(Messages.format("admin.hl7.mapping.import.invalidName", lookup.getName(),
                                                 lookup.getCode(), name));
            } else {
                result = lookup;
            }
        }
        return result;
    }

}