package com.wormtrader.history.event;

import java.util.Vector;
import com.wormtrader.bars.Bar;
import com.wormtrader.history.Tape;

public final class TapeListenerSupport
	{
	private final Vector<TapeListener> m_listeners
													= new Vector<TapeListener>();
	public final synchronized void add( TapeListener el )
		{
		if ( !m_listeners.contains( el ))
			m_listeners.add( el );
		}

	public final synchronized void remove( TapeListener el )
		{
		m_listeners.remove( el );
		}

  @SuppressWarnings("unchecked")
	private final void fireTapeEvent(Tape source, int actionID, int index, Bar bar)
		{
		Vector<TapeListener> listeners;
		synchronized (this)
			{
			listeners = (Vector<TapeListener>)m_listeners.clone();
			}
		TapeEvent evt = new TapeEvent(source, actionID, index, bar );
		for (TapeListener listener : listeners )
			listener.tapeChanged(evt);
		}
	}
