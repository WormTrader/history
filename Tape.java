package com.wormtrader.history;
/********************************************************************
* @(#)Tape.java	1.00 2007
* Copyright © 2007-2014 by Richard T. Salamone, Jr. All rights reserved.
*
* Tape: A tape is a collection of market bars for a particular barsize.
* A tape also maintains a list of studies as well as a list of listeners.
* When a bar comes in, the studies are updated first, then the bar is
* sent to the listeners. Exit methods (stops) that fire off the tape
* should register themselves using addPriorityTapeListener which will
* force them to see the tape before other listeners like graphs, worms.
*
* @author Rick Salamone
* @version 1.00 2007
* 2007???? rts Code evolved between 2007 - 2009 along with
*              private versions of Shane's Brain
* 20100816 rts added indexOf() to support worms that need to
*              synchronize tapes with different start times
* 20120316 rts added addPriorityTapeListener()
* 20120605 rts added ctor that only requires a BarSize
* 20120607 rts added adjGoBack() and made BarSize final
* 20120608 rts goBack is now saved and restored via usr.tape.fill.XX property
* 20120615 rts using BarList to replace vector of Bar
* 20120627 rts now fires events when symbol or backfill changes
* 20121108 rts rewrite bar builder code & added currentBar
* 20130218 rts no longer commits to disk on barModified
* 20130225 rts modified backfill's goBack to support weekly bar size
* 20130304 rts added purgeOldestDay() to support simulation
* 20130313 rts added fireBarComplete to support simulation
* 20140326 rts added reset()
*******************************************************/
import com.wormtrader.bars.BarList;
import com.wormtrader.bars.BarsListener;
import com.wormtrader.bars.BarSize;
import com.wormtrader.bars.Bar;
import com.wormtrader.history.TapeStudy;
import com.wormtrader.history.event.*;
import com.wormtrader.history.indicators.*;
import com.shanebow.util.SBArray;
import com.shanebow.util.SBProperties;
import com.shanebow.util.SBDate;
import com.shanebow.util.SBLog;
import com.shanebow.util.SBFormat; // only for dump()

public final class Tape
	implements BarsListener, TapeListener
	{
	private final BarSize     m_barSize;
	private final BarList     m_bars = new BarList();
	private final TapeStudies m_studies = new TapeStudies();
	private final SBArray<TapeListener> m_listeners	= new SBArray<TapeListener>(2);

	// Mutable instance data
	private boolean TRACE=false;
	private String            m_symbol = "";
	private String            m_goBack = null;
	private long              m_time = 0; // backfill time
	private int               m_lowestLo = Integer.MAX_VALUE;
	private int               m_highestHi = Integer.MIN_VALUE;
	private long              m_lastBarTime = 0;

	public Tape ( BarSize bs ) { this("", bs, SBDate.today); }

	public Tape ( String symbol, BarSize bs, long time )
		{
		this( symbol, bs );
		m_time = time;
		backfill();
		}

	public Tape ( String symbol, BarSize bs )
		{
		m_symbol = symbol;
		m_barSize = bs;
		m_goBack = SBProperties.get(fillPropertyKey());
		if ( m_goBack==null )           // if no backfill property,
			m_goBack = bs.backfills()[0]; // default to shortest for bs
		}

	private String fillPropertyKey()
		{
		return "usr.tape.fill." + m_barSize.paramValue();
		}

	private void _saveGoBack(String goBack)
		{
		m_goBack = goBack;
		if ( goBack != null )
			SBProperties.set(fillPropertyKey(), goBack);
		}

	public void setTrace(boolean on) { TRACE = on; }
	private final void trace( String format, Object... args )
		{
		if (TRACE) SBLog.write(toString() + "." + String.format(format, args));
		}

	public void dump() // called by BarsTable - right click
		{
		StringBuffer b = new StringBuffer("<html>");
		int nbars = m_bars.size();
		b.append(m_bars.toString())
		 .append("<br>m_lastBarTime: ").append(SBDate.yyyymmdd__hhmmss(m_lastBarTime))
		    .append(" Look Back: ").append(m_goBack)
		 .append("<br>Price Range: ").append(SBFormat.toDollarString(m_lowestLo))
		    .append(" - ").append(SBFormat.toDollarString(m_highestHi));

		m_studies.dump(b);
		b.append("<br>Listeners(").append(m_listeners.size()).append("):<ul>");
		for ( TapeListener tl : m_listeners )
			b.append("<li>").append(tl.toString()).append("</li>");
		b.append("</ul>");
		com.shanebow.ui.SBDialog.inform(toString() + " Info", b.toString());
		}

	/**
	* Use this method to add listeners that need to "see" the tape
	* before "normal" listeners - specifically, exit methods should
	* use this method to gain priority over the worms.
	*/
	public final synchronized void addPriorityTapeListener( TapeListener el )
		{
		if ( m_listeners.contains( el )) return;
			m_listeners.add( 0, el );
		}
	public final synchronized void addTapeListener( TapeListener el )
		{
		if ( !m_listeners.contains( el ))
			m_listeners.add( el );
		}
	public final synchronized void removeTapeListener( TapeListener el )
		{
		m_listeners.remove( el );
		}
	private final void fireTapeEvent(int actionID, int index, Bar bar)
		{
		synchronized (this)
			{
			TapeEvent evt = new TapeEvent(this, actionID, index, bar );
			for ( TapeListener listener : m_listeners )
				listener.tapeChanged(evt);
			}
		}

	public void clear()
		{
		if ( m_bars.size() <= 0 )
			return;
		_clear();
		fireTapeEvent( TapeEvent.BARS_CLEARED, 0, null );
		}

	private void _clear()
		{
		m_lowestLo = Integer.MAX_VALUE;
		m_highestHi = Integer.MIN_VALUE;
		m_lastBarTime = 0;
		m_bars.clear();
		m_studies.update( 0 );
		}

	public void setSymbol ( String symbol ) // HistoryManager.StockDetailPanel
		{
		if (( symbol == null ) || !symbol.equals(m_symbol))
			{
			_setSymbol(symbol);
			backfill();
			}
		}

	private void _setSymbol(String symbol)
		{
		fireTapeEvent( TapeEvent.SYMBOL_CHANGING, 0, null);
		_clear();
		m_symbol = symbol;
		}

	public final void reset() // reload all bars
		{
		_clear();
		backfill();
		}

	public final int reset( String symbol, long time ) // TripleTape
		{
		trace( "reset(%s,%s) - back %s", symbol, SBDate.mmddyy_hhmm(time), m_goBack );
		_setSymbol(symbol);
		m_time = time;
		backfill();
		return m_bars.size();
		}

	public final int reset( String symbol, long[] dateRange ) // HistoryManager.ScreenWorker
		{
		_setSymbol(symbol);
		m_bars.fetch( symbol, getBarSize(), dateRange );
		barsModified();
		fireTapeEvent( TapeEvent.BACKFILLED, 0, null );
		return m_bars.size();
		}

	public void setTime ( long time )
		{
		trace( "setTime(%s) %s", SBDate.mmddyy_hhmm(time), SBDate.mmddyy_hhmm(m_time));
		m_time = time;
		backfill();
		}

	public String getGoBack() { return m_goBack; }
	public void setGoBack ( String goBack )
		{
		trace( "setGoBack(%s) from %s", goBack, SBDate.mmddyy_hhmm(m_time));
		_saveGoBack(goBack);
		backfill();
		}

	public void adjGoBack ( String goBack )
		{
		if (goBack.equals(m_goBack) && size() > 0) return; // nothin to do
		if (!m_barSize.isIntraday() || size() <= 0
		|| (m_time == 0) || !isSet(m_symbol) || !isSet(m_goBack))
			{
			setGoBack(goBack); // just clear & reload the bars
			return;
			}
		// adjust the bars
		trace( "adjGoBack(%s) from %s was %s", goBack, SBDate.mmddyy_hhmm(m_time), m_goBack);
		long newFirstTime = SBDate.goBack(m_time, goBack);
		long oldFirstTime = get(0).getTime();
		if ( newFirstTime > oldFirstTime ) // decreasing the goBack
			{
			_removePriorTo(newFirstTime);
			}
		else
			{
			long [] range = { newFirstTime, oldFirstTime-1 };
			trace( "adjGoBack to %s insert range: %s - %s", goBack,
			        SBDate.mmddyy_hhmm(range[0]), SBDate.mmddyy_hhmm(range[1]));
			BarList newBars = new BarList();
			newBars.fetch(m_symbol, m_barSize, range);
			m_bars.addAll(0, newBars);
			}
		_saveGoBack(goBack);
		barsModified();
		fireTapeEvent( TapeEvent.BACKFILLED, 0, null );
		}

	/**
	* Removes the oldest day's bars from the tape
	*/
	public void purgeOldestDay()
		{
		long time = SBDate.toTime(m_bars.firstBar().yyyymmdd() + "  23:59:59");
		_removePriorTo(time);
		barsModified();
		}

	/**
	* Deletes bars from front of tape that have time before earliestTime
	* PRIVATE because does not update studies etc
	*/
	private void _removePriorTo(long earliestTime)
		{
		while (size() > 0 && earliestTime > get(0).getTime())
			m_bars.remove(0);
		}

	private boolean isSet(String x) { return (x != null) && !x.isEmpty(); }
	private void backfill()
		{
		_clear();
		if ((m_time != 0) && isSet(m_symbol) && isSet(m_goBack))
			{
			long first = m_goBack.startsWith("All") ? 0
				: SBDate.goBack(m_time, m_goBack, m_barSize.isIntraday()); // intraday must land on weekday
			long [] range = { first, m_time };
			trace( "backfill %s range: %s - %s", m_goBack, SBDate.mmddyy_hhmm(first),
			             SBDate.mmddyy_hhmm(m_time));
			// m_bars.clear(); Note for intraday this would blow away any "live bars"
			// but for analyzer, etc want to clear so do in simulator code
			m_bars.fetch(m_symbol, m_barSize, range);
			if (m_barSize.compareTo(BarSize.ONE_DAY) > 0) // weekly or greater
				{
				fCurrentBar = (SBDate.dayOfWeek(m_time)== 2  // if not monday open, continue last bar
				           &&  SBDate.hhmmss(m_time).compareTo("09:30:00") <= 0)? null : lastBar();
				}
			barsModified();
			trace( "backfilled %s from %s: %s", m_goBack,
			             SBDate.mmddyy_hhmm(m_time), m_bars.toString());
			fireTapeEvent( TapeEvent.BACKFILLED, 0, null );
			}
		}

	// implement BarsListener
	public final void realtimeBar( Bar bar ){ addBar(bar, TapeEvent.REALTIME_BAR); }
	public final void historyBar( Bar bar ) { addBar(bar, TapeEvent.BAR_ADDED); }
	public final void historyDone()
		{
		trace("history done: %d bars", m_bars.size());
		fireTapeEvent( TapeEvent.HISTORY_DONE, m_bars.size() - 1, null );
		}
	public final void barError ( int errorCode, String errorMsg )
		{
		fireTapeEvent( TapeEvent.BAR_ERROR, errorCode, null );
		SBLog.error( toString(), "barError(" + errorCode + ": " + errorMsg + ")");
		}

	private final void updateHiLo ( Bar b )
		{
		int lo = b.getLow();
		int hi = b.getHigh();
		if ( lo < m_lowestLo ) m_lowestLo = lo;
		if ( hi > m_highestHi ) m_highestHi = hi;
		}

	private final void addBar ( Bar b, int actionID ) // adds bar in order
		{
		int index = m_bars.size();
		updateHiLo(b);
		long thisBarTime = b.getTime();
long elapsed = thisBarTime - m_lastBarTime;
		if ( thisBarTime <= m_lastBarTime )  // got an out of order history bar
			{
			for ( int i = 0; i < index; i++ )
				{
				Bar bar = m_bars.get(i);
				long barTime = bar.getTime();
				if ( thisBarTime < barTime  )
					{
					m_bars.insertElementAt( b, i );
					m_studies.update( i );
					fireTapeEvent( TapeEvent.BAR_INSERTED, i, b );
					return;
					}
				else if ( barTime == thisBarTime )	// both hist & realtime bars for
					{														// this time, use the history bar.
					if ( b.getWAP() != 0 ) 					// Note: realtime bars have WAP == 0
						{
						m_bars.setElementAt( b, i );
						m_studies.update( i );
						fireTapeEvent( TapeEvent.BAR_INSERTED, i, b );
						}
					return;
					}
				}
			}
		else if (elapsed != m_barSize.duration()
		     &&  !b.hhmm().equals("09:30"))
			{
			SBLog.write(toString() + ".addBar(" + b.hhmm() + ") SHORT! prev: "
			            + SBDate.hhmm(m_lastBarTime) + " elapsed: "
			            + elapsed + " dur: " + m_barSize.duration());
			}
		m_bars.add ( b );
		m_lastBarTime = thisBarTime;
		m_studies.update( index );
		fireTapeEvent( actionID, index, b );
		}

	public void tapeChanged( TapeEvent e ) // implements TapeListener
		{
		if ( e.getActionID() == TapeEvent.REALTIME_BAR
		||   e.getActionID() == TapeEvent.BAR_ADDED )
			buildBar( e );
		}

	/**
	* receive a lesser time frame bar from the data feed and use it to build
	* a bar of this tape's time frame.
	*/
	private Bar fCurrentBar;
	private void buildBar( TapeEvent e )
		{
		Bar barSmall = e.getBar();
		long time = barSmall.getTime();

		// If there is no current bar in progress, create and add a new (partial) bar
		if (fCurrentBar == null) // time boundary - start a new bar
			{
			time = (m_barSize.compareTo(BarSize.ONE_DAY) >= 0)?
			       SBDate.toTime(barSmall.yyyymmdd() + "  09:30")
			     : time - (time % m_barSize.duration()); // time to bar start
			fCurrentBar = new Bar(time, barSmall);  // clone the smaller bar
			addBar(fCurrentBar, TapeEvent.BARS_MODIFIED);
			return; // assume the bar isn't finished yet (though the very 1st bar might be)
			}

		// continue building the current bar in progress
		int lastBarIndex = m_bars.size() - 1;
		long nextBarTime = time + e.getTape().m_barSize.duration();
		fCurrentBar.adjust(barSmall);
		updateHiLo(barSmall);
		m_studies.update( lastBarIndex );

		// If the bar in progress is finished, send a tape event
		// this only works for intraday bars, see note on fireBarComplete
		if ((nextBarTime % m_barSize.duration()) == 0 ) // the bar is finished
			fireBarComplete(e.getActionID());
		else fireTapeEvent(TapeEvent.BARS_MODIFIED, lastBarIndex, fCurrentBar);
		}

	/**
	* Sends message to the listeners that the last bar is a completed realtime
	* or historical bar
	* NOTE: In a perfect world this would be a private method. But, the simulated
	* data feed is in a better position to know when non-intraday bars are
	* complete, especially given half-days and holidays so this method is public
	* just for the simulator, and we assume the live trader is restarted every day.
	*/
	public final void fireBarComplete(int actionID)
		{
		Bar lastCompleteBar = fCurrentBar;
		fCurrentBar = null;
		fireTapeEvent(actionID, m_bars.size() - 1, lastCompleteBar);
		}

	private boolean isBarBoundary(long time)
		{
		if (m_barSize.isIntraday() && (time % m_barSize.duration()) == 0 ) // the bar is finished
			return true;
		String hhmm = SBDate.hhmm(time);
		if (!hhmm.equals("09:30"))
			return false;
		return m_barSize.equals(BarSize.ONE_DAY)? true
		     : m_barSize.equals(BarSize.ONE_WEEK)? SBDate.startOfWeek(time) > lastBar().getTime()
		     : false;
		}

	public final Bar currentBar() { return fCurrentBar; }
	public final Bar lastBar() { return (m_bars.size() > 0) ? m_bars.lastElement() : null; }
/**********
	public final Bar lastBar()
		{
		try { return (fCurrentBar == null)? m_bars.lastElement()
		                                  : m_bars.get(m_bars.size()-2); }
		catch (Exception e) { return null; }
		}
**********/

	public final void barModified(int index)
		{                             // forces recalc of low & high
		if ( m_bars.size() == 0 ) return;
 		m_lowestLo = Integer.MAX_VALUE;
		m_highestHi = Integer.MIN_VALUE;
		for ( Bar bar : m_bars )
			updateHiLo(bar);
		m_lastBarTime = m_bars.lastElement().getTime();
		m_studies.update( index );
		Bar bar = m_bars.get(index);
		fireTapeEvent( TapeEvent.BARS_MODIFIED, index, bar );
		}

	public final void barsModified() // forces recalc of low & high
		{
		barModified(0);
		}

	public final String  getSymbol() { return m_symbol; }
	public final String  toString() {return "Tape(" + m_symbol +"," + m_barSize + ")";}
	public final String  getTitle() {return m_symbol + " " + m_barSize + " bars"; }
	public final BarList getBars() { return m_bars; }
	public final BarSize getBarSize() { return m_barSize; }
	public final Bar     get(int index) { return m_bars.get(index); }
	public final int     size()    { return m_bars.size(); }
	public final int     getLowestLo()	{ return m_lowestLo; }
	public final int     getHighestHi()	{ return m_highestHi; }
//	public final long    getLastBarTime() { return m_lastBarTime; }

	public final int indexOf(long time)
		{
		int x = m_bars.binarySearch(time);
		return (x < 0)? -1 : x;
		}
	public final TapeStudy addStudy(String name)
		{
		return m_studies.add(this, name, null);
		}
	public final TapeStudy addStudy(String name, String params )
		{
		return m_studies.add( this, name, params );
		}
	}
