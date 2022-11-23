package com.wormtrader.history.indicators;
/********************************************************************
* @(#)Fractals.java 1.00 20120723
* Copyright © 2012-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* Fractals: As defined by Bill Williams in Trading Chaos (both editions).
*
* @author Rick Salamone
* @version 1.00
* 20120723 rts created
* 20120803 rts fixed to handle "ties" (matching lows or highs)
* 20120808 rts plots carots
* 20120905 rts added nextUp and nextDown methods
* 20121014 rts added calculation on second to last bar for possible fractal
* 20121015 rts added priorUp and priorDown methods
* 20130302 rts uses BarList
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import java.awt.Color;

final public class Fractals
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="Fractals";
	public String getName() { return STUDY_NAME; }

	public static final byte IS_UP = (byte)0x01;
	public static final byte IS_DOWN = (byte)0x02;
	private static int  _capacityIncrement = 80;

	private final BarList m_bars;
	private int         m_minValue = Integer.MAX_VALUE;
	private int         m_maxValue = 0;
	private Color[]     m_color = { Color.WHITE, Color.WHITE }; // up, down
	private byte[]      m_flags;
	private int         m_index = -1; // index of last valid data

	public Fractals( BarList bars ) { this( bars, "" ); }
	public Fractals( BarList bars, String params )
		{
		m_bars = bars;
		int length = bars.size();
		if ( length < _capacityIncrement )
			length = _capacityIncrement;
		m_flags = new byte[length];
		setParams(params);
		}

	public final BarList bars() { return m_bars; }
	public final Bar bar(int x) { return m_bars.get(x); }

	private void enlargeArrays()
		{
		m_flags = arrayExpand ( m_flags, _capacityIncrement );
		}

	public static final byte[] arrayExpand ( byte[] copyFrom, int capacityIncrement )
		{
		int newSize = copyFrom.length + capacityIncrement;
		byte[] copyTo = new byte[newSize];
		System.arraycopy(copyFrom, 0, copyTo, 0, copyFrom.length);
		return copyTo;
		}

	public String getParams() { return ""; }
	public void setParams( String params )
		{
		dataChanged(0);
		if ((params == null) || params.isEmpty())
			return;
		System.err.println( getClass().getName() + ".setParams: NOT IMPLEMENTED");
		}

	public String getRangeString(int value) { return ""; }
	public String getClipLabel() { return STUDY_NAME; }
	public String getToolTipText(int x) { return ""; }
	public void setColor(Color c) { m_color[0] = c; }
	public int getRangeMaximum() { return m_maxValue; }
	public int getRangeMinimum() { return m_minValue; }
	public void dataChanged(int index)
		{
		int numBars = m_bars.size();
		while ( m_flags.length < numBars )
			enlargeArrays();
		for ( int i = index - 2; i < numBars-2; i++ )
			compute(i);
		if ( numBars > 2 )
			{
			m_flags[numBars-1] = 0;
			if (numBars >= 4)
				checkSecondToLast();
			else
			m_flags[numBars-2] = 0;
			}
		}

	private void checkSecondToLast()
		{
		int index = m_bars.size() - 1;
		Bar bar = m_bars.get(index);
		int lastBarHi = bar.getHigh();
		int lastBarLo = bar.getLow();

		bar = m_bars.get(--index);
		int secondToLastHi = bar.getHigh();
		int secondToLastLo = bar.getLow();

		byte flags = 0;
		if ( secondToLastHi >= lastBarHi
		&&   hasTwoLowerHighs(secondToLastHi, index, -1))
			{
			flags |= IS_UP;
			if (secondToLastHi > m_maxValue) m_maxValue = secondToLastHi;
			}

		if ( secondToLastLo <= lastBarLo
		&&   hasTwoHigherLows(secondToLastLo, index, -1))
			{
			flags |= IS_DOWN;
			if (secondToLastLo < m_minValue) m_minValue = secondToLastLo;
			}
		m_flags[index] = flags;
		}

	private void compute(int index)
		{
		m_index = index;

		if ( index < 2 )
			{
			if (index < 0) return;
			m_flags[index] = 0;
			if ( index == 0 ) // prime the pump
				m_maxValue = m_minValue = m_bars.get(0).midpoint();
			return;
			}

		Bar bar = m_bars.get(index);
		byte flags = 0;
		int hi = bar.getHigh();
		int lo = bar.getLow();

		if ( hasTwoLowerHighs(hi, index, 1)
		&&   hasTwoLowerHighs(hi, index, -1))
			{
			flags |= IS_UP;
			if (hi > m_maxValue) m_maxValue = hi;
			}

		if ( hasTwoHigherLows(lo, index, 1)
		&&   hasTwoHigherLows(lo, index, -1))
			{
			flags |= IS_DOWN;
			if (lo < m_minValue) m_minValue = lo;
			}
		m_flags[index] = flags;
		}

	public boolean hasTwoLowerHighs(int aHigh, int aIndex, int aIncrement)
		{
		try
			{
			int hi;
			do
				{
				do
					{
					aIndex += aIncrement;
					hi = m_bars.get(aIndex).getHigh();
					if (hi > aHigh) return false;
					if (hi == aHigh && aIncrement == 1) return false;
					}
				while (hi == aHigh);
				aIndex += aIncrement;
				hi = m_bars.get(aIndex).getHigh();
				if (hi > aHigh) return false;
				if (hi == aHigh && aIncrement == 1) return false;
				}
			while (hi == aHigh);
			return true;
			}
		catch (Exception e) { return false; } // array out of bounds
		}

	public boolean hasTwoHigherLows(int aLow, int aIndex, int aIncrement)
		{
		try
			{
			int lo;
			do
				{
				do
					{
					aIndex += aIncrement;
					lo = m_bars.get(aIndex).getLow();
					if (lo < aLow) return false;
					if (lo == aLow && aIncrement == 1) return false;
					}
				while (lo == aLow);
				aIndex += aIncrement;
				lo = m_bars.get(aIndex).getLow();
				if (lo < aLow) return false;
				if (lo == aLow && aIncrement == 1) return false;
				}
			while (lo == aLow);
			return true;
			}
		catch (Exception e) { return false; } // array out of bounds
		}

	public void plot ( TGGraph graph, byte clip, int index )
		{
		if ( index < 2 ) return;
		byte flags = m_flags[index];
		if ( flags == 0 ) return;
		Bar bar = m_bars.get(index);
		if ((flags & IS_UP) != 0 )
			{
			graph.setColor( m_color[0] );
			graph.carotAbove (clip, bar.getHigh());
			}
		if ((flags & IS_DOWN) != 0 )
			{
			graph.setColor( m_color[1] );
			graph.carotBelow (clip, bar.getLow());
			}
		}

	public String getMetrics(int i) { return String.format("%d: 0x%02X", i, m_flags[i]); }
	public byte getValue ( int x ) { return m_flags[x]; }

	/**
	* @return the index of the bar in the BarList which is the next up fractal
	*         after the specified index or -1 if no up fractal was found.
	*/
	public int nextUp(int startIndex)
		{
		int lastIndex = m_bars.size()-2;
		for (int x = startIndex + 1; x < lastIndex; x++)
			if ((m_flags[x] & IS_UP) != 0) return x;
		return lastIndex + 1;
		}

	/**
	* @return the index of the bar in the BarList which is the next down fractal
	*         after the specified index or -1 if no down fractal was found.
	*/
	public int nextDown(int startIndex)
		{
		int lastIndex = m_bars.size()-2;
		for (int x = startIndex + 1; x < lastIndex; x++)
			if ((m_flags[x] & IS_DOWN) != 0) return x;
		return lastIndex + 1;
		}

	/**
	* @return the index of the most recent bar before the specified index
	*         that is an up fractal, or -1 if no up fractal was found.
	*/
	public int priorUp(int startIndex)
		{
		for (int x = startIndex - 1; x >= 0; --x)
			if ((m_flags[x] & IS_UP) != 0) return x;
		return -1;
		}

	/**
	* @return the index of the most recent bar before the specified index
	*         that is an up fractal, or -1 if no up fractal was found.
	*/
	public int priorDown(int startIndex)
		{
		for (int x = startIndex - 1; x >= 0; --x)
			if ((m_flags[x] & IS_DOWN) != 0) return x;
		return -1;
		}
	}
