package com.wormtrader.history.ui;
/********************************************************************
* @(#)TripleScreen.java 1.00 2009????
* Copyright © 2009-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* TripleScreen: A tabbed pane where each tab shows a view of the
* tape for a different time frame. The name TripleScreen derives
* from the three time frames presented ala Alexander Elder. Each
* tab is a TapeView object which consists of a graph and a table
* of the bars in a split screen.
*
* @author Rick Salamone
* @version 1.00
* 2009???? rts created
* 20100310 rts added showLines to graph hi/low over past three days
* 20120503 rts using CardPane rather than JTabbedPane: now supports controls
* 20120508 rts moved tabs/controls to bottom for easier access
* 20121119 rts moved fixed add lines to handle the last daily bar is in progress
* 20121119 rts setExecs on all time frames <= 5 minutes
* 20130216 rts eliminated icon on tabs
* 20130227 rts added selectedTape to return the currently visible tape
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList; // only for addLines
import com.wormtrader.bars.BarSize;
import com.wormtrader.dao.SBExecution;
import com.wormtrader.history.Tape;
import com.wormtrader.history.TripleTape;
import com.wormtrader.history.ui.TapeView;
import com.shanebow.ui.SBAction;
import javax.swing.ImageIcon;

public class TripleScreen
	extends CardPane
	{
	private static final int TIME_FRAMES = TripleTape.numTimeFrames();

	private TripleTape      m_sharedTapes = new TripleTape("");
	protected TripleTape    m_tapes = m_sharedTapes;
	private TapeView[]      m_tapeView = new TapeView[TIME_FRAMES];

	public TripleScreen()
		{
		super(CardPane.BOTTOM);
		for ( byte i = 0; i < TIME_FRAMES; i++ )
			{
			BarSize bs = TripleTape.BAR_SIZES[i];
			m_tapeView[i] = new TapeView( m_tapes.getTape(i));
			addTab( bs.toString(), null, m_tapeView[i] );
			}
		}

	public void reset ( String symbol, long time )
		{
		for ( Tape tape : m_sharedTapes.getTapes())
			tape.reset ( symbol, time );
		reset( m_sharedTapes );
		}

	public void reset ( TripleTape t3 )
		{
		if ( t3 != null && m_tapes != t3 )
			{
			m_tapes = t3;
			for ( byte i = 0; i < TIME_FRAMES; i++ )
				m_tapeView[i].setTape( m_tapes.getTape(i));
			addLines();
			}
		}

	public final void addPopupActions(SBAction... aActions)
//	public final void addPopupActions(GraphPopupAction... aActions)
		{
		for ( byte i = 0; i < TIME_FRAMES; i++ )
			m_tapeView[i].graph().addActions(aActions);
		}

	private void addLines()
		{
System.out.println("Calling addLines");
		for ( byte i = 0; i < TIME_FRAMES; i++ )
			m_tapeView[i].graph().clearLines();
		BarList bars;
		try { bars = m_tapes.getTape(BarSize.ONE_DAY).getBars(); }
		catch (Exception nullDailyTape) { return; }
		int barIndex = bars.size() - 1;
		if ( barIndex < 0 ) return; // nothing to do
		Bar bar = bars.get(barIndex--); // most recent daily bar - want yesterday
		try // if this is a day in progress "bar" is today instead of yesterday
			{
// @TODO: Should be able to do this with any intraday size being used
			Bar last5 = m_tapes.getTape(BarSize.FIVE_MIN).lastBar();
			if (!last5.hhmm().equals("15:55") // we're trading this day now...
			&&  last5.yyyymmdd().equals(bar.yyyymmdd()))
				bar = bars.get(barIndex--);
			}
		catch (Exception e) {}
		int high = bar.getHigh();
		int low = bar.getLow();
		// draw yesterday's high & low on all three graphs
		for ( byte i = 0; i < TIME_FRAMES; i++ )
			{
			m_tapeView[i].graph().addLine(high, java.awt.Color.BLUE);
			m_tapeView[i].graph().addLine(low,  java.awt.Color.BLUE);
			}
		// find highest high & lowest low for prev "goBack" number of days
		int goBack = 3;
		while ( --goBack > 0 )
			if ( barIndex < 0 ) break;
			else
				{
				bar = bars.get(barIndex--);
				if ( bar.getHigh() > high ) high = bar.getHigh();
				if ( bar.getLow()  < low  ) low = bar.getLow();
				}
		// draw highest high & lowest low for prev "goBack" number of days
		for ( byte i = 0; i < TIME_FRAMES; i++ )
			{
			m_tapeView[i].graph().addLine(high, java.awt.Color.YELLOW);
			m_tapeView[i].graph().addLine(low,  java.awt.Color.YELLOW);
			}
		}

	public void setExecs( Iterable<SBExecution> execs )
		{
		for ( byte i = 0; i < TIME_FRAMES; i++ )
			if ( m_tapes.getTape(i).getBarSize().compareTo(BarSize.THIRTY_MIN) <= 0)
				m_tapeView[i].setExecs( execs );
		}

	public final TripleTape getTapeSet() { return m_tapes; }
	public final Tape selectedTape() { return m_tapes.getTape((byte)getSelectedIndex()); }
	}
