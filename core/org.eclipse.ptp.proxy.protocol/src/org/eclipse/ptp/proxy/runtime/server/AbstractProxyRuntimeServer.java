/*******************************************************************************
 * Copyright (c) 2007, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - Initial API and implementation
 *     Dieter Krachtus, University of Heidelberg
 *     Roland Schulz, University of Tennessee
 *******************************************************************************/

package org.eclipse.ptp.proxy.runtime.server;

import java.io.IOException;
import java.util.concurrent.LinkedBlockingQueue;

import org.eclipse.ptp.proxy.command.IProxyCommand;
import org.eclipse.ptp.proxy.command.IProxyCommandListener;
import org.eclipse.ptp.proxy.command.IProxyQuitCommand;
import org.eclipse.ptp.proxy.event.IProxyEvent;
import org.eclipse.ptp.proxy.packet.ProxyPacket;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeInitCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeModelDefCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeStartEventsCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeSubmitJobCommand;
import org.eclipse.ptp.proxy.runtime.command.IProxyRuntimeTerminateJobCommand;
import org.eclipse.ptp.proxy.runtime.command.ProxyRuntimeCommandFactory;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory;
import org.eclipse.ptp.proxy.server.AbstractProxyServer;

public abstract class AbstractProxyRuntimeServer extends AbstractProxyServer implements IProxyCommandListener {

	/*
	 * Event queue for incoming events.
	 */
	private final LinkedBlockingQueue<IProxyCommand> fCommands = new LinkedBlockingQueue<IProxyCommand>();

	/**
	 * @since 4.0
	 */
	protected final IProxyRuntimeEventFactory fEventFactory;

	/**
	 * @since 4.0
	 */
	public int fEventLoopTransID;

	/**
	 * @since 4.0
	 */
	protected Thread fEventThread;

	/**
	 * @since 4.0
	 */
	protected Thread eventThread;

	/**
	 * @param host
	 * @param port
	 * @since 4.0
	 */
	public AbstractProxyRuntimeServer(String host, int port, IProxyRuntimeEventFactory eventFactory) {
		super(host, port, new ProxyRuntimeCommandFactory());
		fEventFactory = eventFactory;
		addListener(this);
	}

	/**
	 * @return
	 * @since 4.0
	 */
	protected IProxyRuntimeEventFactory getEventFactory() {
		return fEventFactory;
	}

	/**
	 * @return
	 * @since 4.0
	 */
	protected Thread getEventThread() {
		return fEventThread;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.proxy.command.IProxyCommandListener#handleCommand(org
	 * .eclipse.ptp.proxy.command.IProxyCommand)
	 */
	/**
	 * @since 4.0
	 */
	public void handleCommand(IProxyCommand c) {
		fCommands.add(c);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.proxy.server.AbstractProxyServer#runStateMachine()
	 */
	@Override
	protected void runStateMachine() throws InterruptedException, IOException {
		while (state != ServerState.SHUTDOWN) {
			IProxyCommand command;
			IProxyEvent event;
			int transID;
			System.out.println("runStateMachine: state: " + state); //$NON-NLS-1$
			switch (state) {
			case INIT:
				command = fCommands.take();

				// instead of getting the base_ID the hard way, rather implement
				// a getBase_ID method in IProxyRuntimeInitCommand.
				int base_ID = Integer.parseInt(command.getArguments()[1].split("=")[1]); //$NON-NLS-1$
				new ElementIDGenerator(base_ID);

				transID = command.getTransactionID();
				System.out.println("runStateMachine: command: " + command.getCommandID() + " (" + transID + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if (command instanceof IProxyRuntimeInitCommand) {
					event = fEventFactory.newOKEvent(transID);
					sendEvent(event);
					state = ServerState.DISCOVERY;
				} else {
					System.err.println("unexpected command (INIT): " + command); //$NON-NLS-1$
				}

				break;
			case DISCOVERY:
				command = fCommands.take();
				transID = command.getTransactionID();
				System.out.println("runStateMachine: command: " + command.getCommandID() + " (" + transID + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if (command instanceof IProxyRuntimeModelDefCommand) {
					event = fEventFactory.newOKEvent(transID);
					sendEvent(event);
					state = ServerState.NORMAL;
				} else {
					System.err.println("unexpected command (DISC): " + command); //$NON-NLS-1$
				}

				break;
			case NORMAL:
				command = fCommands.take();
				transID = command.getTransactionID();
				System.out.println("runStateMachine: command: " + command.getCommandID() + " (" + transID + ")"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				if (command instanceof IProxyRuntimeStartEventsCommand) {
					// TODO start event loop
					// event = eventFactory.newOKEvent(transID); //TODO: send OK
					// here?
					// sendEvent(event);

					if (fEventThread == null) {
						fEventLoopTransID = transID;
						this.startEventThread(transID);
					}
				} else if (command instanceof IProxyQuitCommand) {
					event = fEventFactory.newShutdownEvent(transID);
					sendEvent(event);
					state = ServerState.SHUTDOWN;
				} else if (command instanceof IProxyRuntimeTerminateJobCommand) {
					terminateJob(transID, command.getArguments());
				} else if (command instanceof IProxyRuntimeSubmitJobCommand) {
					submitJob(transID, command.getArguments());
				} else {
					System.err.println("unexpected command (NORM): " + command); //$NON-NLS-1$
				}
				// state = ServerState.NORMAL;
				break;
			}
		}
	}

	/**
	 * @param event
	 * @since 4.0
	 */
	public void sendEvent(IProxyEvent event) throws IOException {
		// if (!isReady()) {
		// throw new IOException(Messages.AbstractProxyClient_0);
		// }
		// if (event.getTransactionID()==-1) { /* event without known TransID
		// gets eventLoopTransID */
		// if (state == ServerState.NORMAL) {
		// event.setTransactionID(eventLoopTransID);
		// } else {
		// System.err.println("Not allowed to send events without TransID outside of EventLoop (state: normal)");
		// return;
		// }
		// }
		ProxyPacket packet = new ProxyPacket(event);

		// if (getDebugOptions().PROTOCOL_TRACING) {
		// packet.setDebug(true);
		// }
		packet.send(sessOutput);
	}

	/**
	 * @param thread
	 * @since 4.0
	 */
	protected void setEventThread(Thread thread) {
		fEventThread = thread;
	}

	/**
	 * @param transID
	 * @since 4.0
	 */
	protected abstract void startEventThread(int transID);

	/**
	 * @since 4.0
	 */
	protected abstract void submitJob(int transID, String[] arguments);

	/**
	 * @since 4.0
	 */
	protected abstract void terminateJob(int transID, String[] arguments);
}
