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
 *  Copyright 2009 (C) OpenVPMS Ltd. All Rights Reserved.
 *
 *  $Id$
 */
package org.openvpms.archetype.rules.doc;

import org.openvpms.archetype.rules.act.ActCopyHandler;
import org.openvpms.component.business.domain.im.act.Act;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.IMObject;
import org.openvpms.component.business.domain.im.common.IMObjectReference;
import org.openvpms.component.business.domain.im.common.Participation;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.ActBean;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;

/**
 * An {@link ActCopyHandler} used in document act versioning.
 * <p/>
 * This performs a shallow copy of the act excluding act relationships, copying all
 * {@link Participation Participations} and references all other objects.
 *
 * @author <a href="mailto:support@openvpms.org">OpenVPMS Team</a>
 * @version $LastChangedDate: 2006-05-02 05:16:31Z $
 */
class VersioningCopyHandler extends ActCopyHandler {

    /**
     * The document versions node name.
     */
    private static final String VERSIONS = "versions";

    /**
     * The document reference node name.
     */
    private static final String DOCUMENT = "document";


    /**
     * Creates a new <tt>VersioningCopyHandler</tt>.
     *
     * @param template a template act, used to determine the type of the document version act.
     * @param service  the archetype service
     */
    public VersioningCopyHandler(Act template, IArchetypeService service) {
        setCopy(Participation.class);
        ActBean bean = new ActBean(template, service);
        if (bean.hasNode(VERSIONS)) {
            String[] shortNames = bean.getArchetypeRange(VERSIONS);
            for (String shortName : shortNames) {
                String[] targetShortNames = DescriptorHelper.getNodeShortNames(shortName, "target", service);
                if (targetShortNames.length >= 1) {
                    String[][] map = new String[1][2];
                    map[0][0] = template.getArchetypeId().getShortName();
                    map[0][1] = targetShortNames[0];
                    setShortNameMap(map);
                    break;
                }
            }
        }
    }

    /**
     * Determines how IMObjectCopier should treat an object.
     *
     * @param object  the source object
     * @param service the archetype service
     * @return <tt>object</tt> if the object shouldn't be copied,
     *         <tt>null</tt> if it should be replaced with <tt>null</tt>,
     *         or a new instance if the object should be copied
     */
    @Override
    public IMObject getObject(IMObject object, IArchetypeService service) {
        IMObject result = super.getObject(object, service);
        if (result instanceof DocumentAct && result != object && object instanceof DocumentAct) {
            // copy the document reference manually, to avoid IMObjectCopier loading the associated document simply to
            // copy the reference. Also need to exclude the document nodes in checkCopyable() to avoid this
            IMObjectReference document = ((DocumentAct) object).getDocument();
            ((DocumentAct) result).setDocument(document);
        }
        return result;
    }

    /**
     * Determines if a node is copyable.
     * <p/>
     * This implementation excludes the <em>document</em> node.
     *
     * @param archetype the node's archetype descriptor
     * @param node      the node
     * @return <tt>true</tt> if the node is copyable, otherwise <tt>false</tt>
     */
    @Override
    protected boolean checkCopyable(ArchetypeDescriptor archetype, NodeDescriptor node) {
        boolean result = true;
        if (DocumentAct.class.isAssignableFrom(archetype.getClazz())) {
            String name = node.getName();
            result = !DOCUMENT.equals(name);
        }
        return result;
    }
}
