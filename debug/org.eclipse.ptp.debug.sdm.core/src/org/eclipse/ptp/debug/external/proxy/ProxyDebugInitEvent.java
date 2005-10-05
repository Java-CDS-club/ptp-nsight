package org.eclipse.ptp.debug.external.proxy;

import org.eclipse.ptp.core.proxy.event.AbstractProxyEvent;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugInitEvent extends AbstractProxyEvent implements IProxyEvent {
	private int		num_servers;
	
	public ProxyDebugInitEvent(BitList set, int num) {
		super(EVENT_DBG_INIT, set);
		this.num_servers = num;
	}
	
	public int getNumServers() {
		return this.num_servers;
	}
	
	public String toString() {
		return "EVENT_DBG_INIT " + this.getBitSet().toString() + " " + this.num_servers;
	}
}
