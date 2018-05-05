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
 * Copyright 2017 (C) OpenVPMS Ltd. All Rights Reserved.
 */

package org.openvpms.web.component.im.doc;

import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.lookup.ILookupService;
import org.openvpms.web.component.app.Context;
import org.openvpms.web.echo.help.HelpContext;

/**
 * Factory for {@link DocumentGenerator}.
 *
 * @author Tim Anderson
 */
public class DocumentGeneratorFactory {

    /**
     * The file name formatter.
     */
    private final FileNameFormatter formatter;

    /**
     * The archetype service.
     */
    private final IArchetypeService service;

    /**
     * The lookup service.
     */
    private final ILookupService lookups;

    /**
     * Constructs a {@link DocumentGeneratorFactory}.
     *
     * @param formatter the formatter
     * @param service   the archetype service
     * @param lookups   the lookup service
     */
    public DocumentGeneratorFactory(FileNameFormatter formatter, IArchetypeService service, ILookupService lookups) {
        this.formatter = formatter;
        this.service = service;
        this.lookups = lookups;
    }

    /**
     * Constructs a {@link DocumentGenerator}.
     *
     * @param act      the document act
     * @param context  the context
     * @param help     the help context
     * @param listener the listener to notify
     */
    public DocumentGenerator create(DocumentAct act, Context context, HelpContext help,
                                    DocumentGenerator.Listener listener) {
        return new DocumentGenerator(act, context, help, formatter, service, lookups, listener);
    }

}