package io.mosip.registration.processor.core.queue.impl;

import org.apache.activemq.ActiveMQConnectionFactory;
import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;

import io.mosip.registration.processor.core.queue.factory.MosipActiveMq;
import io.mosip.registration.processor.core.queue.factory.MosipQueue;
import io.mosip.registration.processor.core.spi.queue.MosipQueueManager;

/**
 * This class is ActiveMQ implementation for Mosip Queue
 * 
 * @author Mukul Puspam
 * @since 0.0.1
 */
public class MosipActiveMqImpl implements MosipQueueManager<MosipQueue, byte[]> {

	private Connection connection;
	private Session session;
	private Destination destination;

	/**
	 * The method to set up session and destination
	 * 
	 * @param mosipActiveMq The Mosip ActiveMq instance
	 * @param address The address to set up
	 */
	private void setup(MosipActiveMq mosipActiveMq, String address) {
		if (connection == null) {
			try {
				this.connection = mosipActiveMq.getActiveMQConnectionFactory().createConnection();
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (session == null) {
			try {
				connection.start();
				this.session = this.connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			} catch (JMSException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		if (this.destination == null) {
			try {
				this.destination = this.session.createQueue(address);
			} catch (JMSException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/* (non-Javadoc)
	 * @see io.mosip.registration.processor.core.spi.queue.MosipQueueManager#send(java.lang.Object, java.lang.Object, java.lang.String)
	 */
	@Override
	public Boolean send(MosipQueue mosipQueue, byte[] message, String address) {
		boolean flag = false;
		MosipActiveMq mosipActiveMq = (MosipActiveMq) mosipQueue;
		ActiveMQConnectionFactory activeMQConnectionFactory = mosipActiveMq.getActiveMQConnectionFactory();
		if (activeMQConnectionFactory == null) {
			System.out.println("Problem");
		}
		if (destination == null) {
			setup(mosipActiveMq, address);
		}
		try {
			MessageProducer messageProducer = session.createProducer(destination);
			BytesMessage byteMessage = session.createBytesMessage();
			byteMessage.writeObject(message);
			messageProducer.send(byteMessage);
			flag = true;
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}

	/* (non-Javadoc)
	 * @see io.mosip.registration.processor.core.spi.queue.MosipQueueManager#consume(java.lang.Object, java.lang.String)
	 */
	@Override
	public byte[] consume(MosipQueue mosipQueue, String address) {
		MosipActiveMq mosipActiveMq = (MosipActiveMq) mosipQueue;
		if (destination == null) {
			setup(mosipActiveMq, address);
		}
		MessageConsumer consumer;
		try {
			consumer = session.createConsumer(destination);
			BytesMessage message = (BytesMessage) consumer.receive(5000);
			if (message != null) {
				byte[] data = new byte[(int) message.getBodyLength()];
				message.readBytes(data);
				return data;
			} else {
				System.out.println("no file");
			}
		} catch (JMSException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

}
