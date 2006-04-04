package org.openvpms.web.component.im.edit.act;

import java.math.BigDecimal;
import java.util.Collection;

import org.openvpms.component.business.domain.im.archetype.descriptor.ArchetypeDescriptor;
import org.openvpms.component.business.domain.im.archetype.descriptor.NodeDescriptor;
import org.openvpms.component.business.domain.im.common.Act;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.web.component.im.util.DescriptorHelper;
import org.openvpms.web.spring.ServiceHelper;


/**
 * Act helper.
 *
 * @author <a href="mailto:tma@netspace.net.au">Tim Anderson</a>
 * @version $LastChangedDate$
 */
public class ActHelper {

    /**
     * Sums a node in a list of acts.
     *
     * @param acts the acts
     * @param node the node to sum
     * @return the summed total
     */
    public static BigDecimal sum(Collection<Act> acts, String node) {
        return sum(new BigDecimal("0.0"), acts, node);
    }

    /**
     * Sums a node in a list of acts.
     *
     * @param initial the initial value
     * @param acts    the acts
     * @param node    the node to sum
     * @return the summed total
     */
    public static BigDecimal sum(BigDecimal initial, Collection<Act> acts,
                                 String node) {
        IArchetypeService service = ServiceHelper.getArchetypeService();

        BigDecimal result = initial;
        for (Act act : acts) {
            ArchetypeDescriptor archetype
                    = DescriptorHelper.getArchetypeDescriptor(act, service);
            NodeDescriptor decscriptor = archetype.getNodeDescriptor(node);
            if (decscriptor != null) {
                BigDecimal value = (BigDecimal) decscriptor.getValue(act);
                if (value != null) {
                    result = result.add(value);
                }
            }
        }
        return result;
    }
}
