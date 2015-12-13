package org.openvpms.web.webdav.resource;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openvpms.component.business.domain.im.act.DocumentAct;
import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.DescriptorHelper;
import org.openvpms.component.business.service.archetype.helper.TypeHelper;
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
     * @param service    the archetype short names
     * @param archetypes the archetype short names
     */
    public EditableDocuments(IArchetypeService service, String[] archetypes) {
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
