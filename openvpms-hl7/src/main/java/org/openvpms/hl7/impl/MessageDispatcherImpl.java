package org.openvpms.hl7.impl;

import ca.uhn.hl7v2.DefaultHapiContext;
import ca.uhn.hl7v2.HL7Exception;
import ca.uhn.hl7v2.HapiContext;
import ca.uhn.hl7v2.app.Connection;
import ca.uhn.hl7v2.llp.LLPException;
import ca.uhn.hl7v2.model.Message;
import ca.uhn.hl7v2.util.idgenerator.IDGenerator;
import org.openvpms.hl7.Connector;
import org.openvpms.hl7.MLLPSender;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Default implementation of the {@link MessageDispatcher} interface.
 *
 * @author Tim Anderson
 */
public class MessageDispatcherImpl implements MessageDispatcher {

    private final HeaderPopulator populator;

    private HapiContext messageContext;

    private AtomicLong sequence = new AtomicLong(1);

    private List<ConnectorManagerListener> listeners
            = Collections.synchronizedList(new ArrayList<ConnectorManagerListener>());

    /**
     * Constructs a {@link MessageDispatcherImpl}.
     */
    public MessageDispatcherImpl() {
        populator = new HeaderPopulator();
        messageContext = new DefaultHapiContext();

        // override the default sequence handling
        messageContext.getParserConfiguration().setIdGenerator(new IDGenerator() {
            @Override
            public String getID() {
                return "";
            }
        });
    }

    /**
     * Adds a listener to be notified when a message is sent.
     *
     * @param listener the listener
     */
    @Override
    public void addListener(ConnectorManagerListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener the listener to remove
     */
    @Override
    public void removeListener(ConnectorManagerListener listener) {
        listeners.remove(listener);
    }


    public HapiContext getMessageContext() {
        return messageContext;
    }

    /**
     * Queues a message to be sent via multiple connectors.
     *
     * @param message    the message to queue
     * @param connectors the connectors
     */
    @Override
    public void queue(Message message, List<Connector> connectors) {
        try {
            for (Connector connector : connectors) {
                if (connector instanceof MLLPSender) {
                    populate(message, (MLLPSender) connector);
                    Message response = sendAndReceive(message, (MLLPSender) connector);
                    for (ConnectorManagerListener listener : listeners) {
                        listener.sent(message, response);
                    }
                }
            }
        } catch (Throwable exception) {
            throw new IllegalStateException(exception);
        }
    }

    protected Date getTimestamp() {
        return new Date();
    }

    protected long getSequence(Connector connector) {
        return sequence.incrementAndGet();
    }

    private void populate(Message message, MLLPSender sender) throws HL7Exception {
        populator.populate(message, sender, getTimestamp(), getSequence(sender));
    }


    protected Message sendAndReceive(Message message, MLLPSender sender)
            throws HL7Exception, LLPException, IOException {
        Connection connection = messageContext.newClient(sender.getHost(), sender.getPort(), false);
        connection.getInitiator().setTimeout(30, TimeUnit.SECONDS);
        return connection.getInitiator().sendAndReceive(message);
    }
}
