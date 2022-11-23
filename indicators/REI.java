package com.wormtrader.history.indicators;
/********************************************************************
* @(#)REI.java 1.00 2007
* Copyright © 2007-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* REI - Range Expansion Index - Developed by Thomas R. DeMark & Jr.
*
* @author Rick Salamone
* @version 1.00
* 2007     rts created
* 20130302 rts uses BarList
*******************************************************/

import java.awt.Color;
import com.wormtrader.bars.BarList;

import com.wormtrader.bars.Bar;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import com.shanebow.util.SBFormat;
import com.shanebow.util.SBMath;
import com.shanebow.util.SBMisc;

final public class REI
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="REI";
	public String getName() { return STUDY_NAME; }
	public static final int OVERBOUGHT = 45;
	public static final int OVERSOLD = -45;

	private static final int DEFAULT_PERIOD = 5;
	private BarList m_bars;
	private Color       m_color[] = { Color.CYAN, Color.GRAY }; // rei, ref lines
	private int         m_period = DEFAULT_PERIOD;
	private int[]       m_rei; // actual REI for each bar; length >= m_bars.size()
	private int[]       m_num; // circular buffers for step3 look back
	private int[]       m_den; //   calculations; length == m_period
	private int         m_index = -1; // index of last valid data
	private static int  _capacityIncrement = 80;

	public REI( BarList bars )
		{
		this( bars, "" );
		}
	public REI( BarList bars, String params )
		{
		m_bars = bars;
		int length = bars.size();
		if ( length < _capacityIncrement )
			length = _capacityIncrement;
		m_rei = new int[length];
		setParams(params);
		}

	public String getParams() { return ""; }
	public void setParams( String params )
		{
		setPeriod(DEFAULT_PERIOD);
		if ((params == null) || params.isEmpty())
			return;
		System.err.println( getClass().getName() + ".setParams: NOT IMPLEMENTED");
		}

	public void setPeriod(int p)
		{
		m_period = p;
		m_num = new int[p];
		m_den = new int[p];
		dataChanged(0);
		}

	public String getRangeString(int value)
		{
		return "REI(" + m_period + "): " + value;
		}
	public String getClipLabel() { return "REI(" + m_period + ")"; }
	public String getToolTipText(int x)
		{
		return "" + m_rei[x];
		}

	public void setColor(Color c) { m_color[0] = c; }
	public int getRangeMaximum() { return 110; }
	public int getRangeMinimum() { return -110; }

	public void dataChanged(int index)
		{
		if ( index != m_index + 1 )
			{
			index = 0;  // TODO: complete recalc - should only
			m_index = -1; // have go back m_periods
			}
		int numBars = m_bars.size();
		for ( int i = index; i < numBars; i++ )
			compute(i);
		}

	private void compute(int index)
		{
		if ( ++m_index != index ) // not in order !
			SBDialog.fatalError("REI.compute(" + index + ") expected index " + m_index );
		Bar bar = m_bars.get(index);
		int hi = bar.getHigh();
		int lo = bar.getLow();
		if ( index < 2 )
			{
			m_den[index] = m_num[index] = m_rei[index] = 0;
			return;
			}
		int slot = index % m_period; // index in circular buffers for this rei

		bar = m_bars.get(index - 2);
		int hi2 = bar.getHigh();
		int lo2 = bar.getLow();
		step1( slot, hi, lo, hi2, lo2);

		if ( index <= 8 ) // prime the pump
			{
			if ( index >= m_period ) step3(index);
			else m_rei[index] = 0;
			return;
			}
		if ( m_rei.length <= index ) // enough space?
			m_rei = SBMisc.arrayExpand ( m_rei, _capacityIncrement );

		bar = m_bars.get(index-5); int hi5 = bar.getHigh(); int lo5 = bar.getLow();
		bar = m_bars.get(index-6); int hi6 = bar.getHigh(); int lo6 = bar.getLow();
		if ((hi >= lo5 || hi >= lo6) && (lo <= hi5 || lo <= hi6))
			{
			step3(index); // met 1st step 2 condition, step 1 qualified
			return;
			}

		int cl7 = m_bars.get(index-7).getClose();
		int cl8 = m_bars.get(index-8).getClose();
		if ((hi2 < cl7 && hi2 < cl8) || (lo2 > cl7 && lo2 > cl8))
			m_num[slot] = 0; // failed both step 2 conditons, step 1 DQ'd
		// else met 2nd step 2 condition, step 1 qualified
		step3( index );
		}

	private int step1(int slot, int hi, int lo, int hi2, int lo2)
		{
		int x = (hi - hi2) + (lo - lo2);
		m_num[slot] = x;
//		m_den[slot] = (x < 0) ? -x : x; // abs
		m_den[slot] = Math.abs(hi - hi2) + Math.abs(lo - lo2);
		return x;
		}

	private void step3( int index )
		{
		int numerator = 0;
		int denominator = 0;
		for ( int slot = 0; slot < m_period; slot++ ) // tally previous m_period's
			{
			numerator += m_num[slot]; // +, -, or 0
			denominator += m_den[slot]; // + (or 0 rarely)
			}
		if ( denominator == 0) m_rei[index] = 0;
		else m_rei[index] = 100 * numerator / denominator;
		}

	public void plot ( TGGraph graph, byte clip, int index )
		{
		if ( index < m_period ) return;
		graph.setColor( m_color[1] );
		graph.connect ( clip, OVERSOLD, OVERSOLD );
		graph.connect ( clip, OVERBOUGHT, OVERBOUGHT );
		graph.setColor( m_color[0] );
		graph.connect ( clip, m_rei[index-1], m_rei[index] );
		}

	public String getMetrics ( int index ) { return "" + m_rei[index]; }
	public int getValue ( int index ) { return m_rei[index]; }
	public int reading ( int index ) { return m_rei[index]; }
	public int duration ( int index ) // how long OB(+) or OS(-)?
		{
		int count = 0;
		do
			{
			if ( m_rei[index] > OVERBOUGHT )
				{
				if ( count < 0 ) // we've been counting oversolds!
					break;	// get out
				++count;
				}
			else if ( m_rei[index] < OVERSOLD )
				{
				if ( count > 0 ) // we've been counting overboughts!
					break;	// get out
				--count;
				}
			else break; // niether OB or OS
			}
		while ( index-- > 0 );
		return count;
		}
	}
