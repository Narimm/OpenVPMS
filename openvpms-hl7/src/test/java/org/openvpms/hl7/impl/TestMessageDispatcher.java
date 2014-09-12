package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;
import org.openvpms.hl7.Connector;
import org.openvpms.hl7.MLLPSender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Enter description.
 *
 * @author Tim Anderson
 */
public class TestMessageDispatcher extends MessageDispatcherImpl {

    /**
     * The queued messages.
     */
    private List<Message> messages = new ArrayList<Message>();

    private Date timestamp;

    private long sequence = -1;


    /**
     * Constructs an {@link TestMessageDispatcher}.
     *
     * @param connectors the connectors
     */
    public TestMessageDispatcher(Connectors connectors) {
        super();
    }

    @Override
    public void queue(Message message, List<Connector> connectors) {
        super.queue(message, connectors);
        messages.add(message);
    }

    @Override
    protected Message sendAndReceive(Message message, MLLPSender sender)
            throws HL7Exception, LLPException, IOException {
        return message.generateACK();
    }

    public List<Message> getMessages() {
        return messages;
    }

    public void setTimestamp(Date timestamp) {
        this.timestamp = timestamp;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }

    @Override
    protected Date getTimestamp() {
        return timestamp != null ? timestamp : super.getTimestamp();
    }

    @Override
    protected long getSequence(Connector connector) {
        return sequence != -1 ? sequence : super.getSequence(connector);
    }
}
