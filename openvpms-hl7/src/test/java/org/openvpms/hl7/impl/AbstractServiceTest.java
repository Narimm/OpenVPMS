package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.model.Message;
import org.openvpms.archetype.test.TestHelper;
import org.openvpms.component.business.domain.im.party.Party;
import org.openvpms.hl7.Connector;
import org.openvpms.hl7.MLLPSender;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public abstract class AbstractServiceTest extends AbstractMessageTest {

    /**
     * The connectors.
     */
    private Connectors connectors;

    /**
     * The connector manager.
     */
    private TestMessageDispatcher manager;

    /**
     * Sets up the test case.
     */
    @Override
    public void setUp() {
        super.setUp();
        connectors = new Connectors() {
            @Override
            public List<Connector> getSenders(Party location) {
                return Arrays.<Connector>asList(new MLLPSender("dummy", 2026, "VPMS", "Main Clinic", "Cubex", "Cubex"));
            }
        };
        manager = new TestMessageDispatcher(connectors);
        manager.setTimestamp(TestHelper.getDatetime("2014-08-25 08:59:00"));
        manager.setSequence(1200022);
        manager.addListener(new ConnectorManagerListener() {
            @Override
            public void sent(Message message, Message response) {
                log("sent: ", message);
                log("received: ", response);
            }
        });
    }

    protected Connectors getConnectors() {
        return connectors;
    }

    protected TestMessageDispatcher getManager() {
        return manager;
    }

    protected void checkMessage(String expected) throws HL7Exception {
        List<Message> messages = manager.getMessages();
        assertEquals(1, messages.size());

        String encode = messages.get(0).encode();
        assertEquals(expected, encode);
    }


}
