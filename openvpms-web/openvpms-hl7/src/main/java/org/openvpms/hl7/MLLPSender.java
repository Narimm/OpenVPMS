package org.openvpms.hl7;

import org.openvpms.component.business.domain.im.common.Entity;
import org.openvpms.component.business.service.archetype.IArchetypeService;
import org.openvpms.component.business.service.archetype.helper.IMObjectBean;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class MLLPSender extends Connector {

    private String host;

    private int port;

    public MLLPSender() {
        super();
    }

    public MLLPSender(String host, int port, String sendingApplication, String sendingFacility,
                      String receivingApplication, String receivingFacility) {
        setHost(host);
        setPort(port);
        setSendingApplication(sendingApplication);
        setSendingFacility(sendingFacility);
        setReceivingApplication(receivingApplication);
        setReceivingFacility(receivingFacility);
    }

    public MLLPSender(Entity object, IArchetypeService service) {
        IMObjectBean bean = new IMObjectBean(object, service);
        setHost(bean.getString("host"));
        setPort(bean.getInt("port"));
        setSendingApplication(bean.getString("sendingApplication"));
        setReceivingApplication(bean.getString("receivingApplication"));
        setReceivingFacility(bean.getString("receivingFacility"));
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }
}
