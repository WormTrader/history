package com.wormtrader.history.indicators;
/********************************************************************
* @(#)MovingAverage.java 1.00 2007
* Copyright © 2007 - 2014 by Richard T. Salamone, Jr. All rights reserved.
*
* MovingAverage - Abstract class for moving averages like EMA, SMA
*
* Subclasses must implement three methods:
* 1. getName() - just return the study name, eg SMA
* 2. dataChanged(index) - update the calculations for new or changed value at index
* 3. getNoOffsetValue(index) - return the value of the MA for this index
*
* Subclasses should completely ignore the offset - everything related to offseting
* the MA is handled here in the base class.
*
* Offsets and value of the MA demonstrated in diagram:
*
*    |  _/ 20  Assume bar #5 is the last bar and the offset is 2, then
*  |   /   19   getValue(4) = 17: the MA at bar #4
*   __/    18   getValue(5) = 17.5: the MA at bar #5
*  /       17   getValue(7) = 20: the MA at bar #7 which doesn't exist yet
*  4 5 6 7      getNoOffsetValue(5) = 20 == getValue(5+offset)
*               getValue() = 20: the most recent MA regardless of offset
*
* @author Rick Salamone
* @version 1.00
* 2007???? rts created
* 20120719 rts added an offset setting
* 20120905 rts added an getValue() no param version
* 20121014 rts added slope100()
* 20140321 rts set offest to 0 if param is missing from string
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import com.shanebow.util.SBFormat;
import java.awt.Color;

public abstract class MovingAverage
	implements TapeStudy, GraphClipRenderer
	{
	public static final int DEFAULT_PERIOD = 12;
	public static final char DEFAULT_PRICE = Bar.PRICE_CLOSE;
	protected static int   _capacityIncrement = 80;

	protected BarList m_bars;
	protected int     m_minValue = Integer.MAX_VALUE;
	protected int     m_maxValue = Integer.MIN_VALUE;
	protected int     m_period = DEFAULT_PERIOD;
	protected int     m_offset = 0;
	protected char    m_price = DEFAULT_PRICE;
	protected int     m_index = -1; // index of last valid data
	private Color     m_color = Color.ORANGE;

	public MovingAverage( BarList bars )
		{
		m_bars = bars;
		}

	public final String getParams()
		{
		return "" + m_period + "," + m_price + "," + m_offset;
		}

	public void setParams( String params )
		{
		String[] pieces = params.split(",");
		m_period = Integer.parseInt(pieces[0].trim());
		m_price = pieces[1].trim().charAt(0);
		m_offset = (pieces.length > 2)? Integer.parseInt(pieces[2].trim()) : 0;
		reset();
		}
	private void reset() { dataChanged(0); }
	public final void setPeriod(int p)  { m_period = p; reset(); }
	public final void setPrice(char p)  { m_price = p; reset(); }
	public final void setColor(Color c) { m_color = c; }
	public final int getRangeMaximum()  { return m_maxValue; }
	public final int getRangeMinimum()  { return m_minValue; }
	public final String getRangeString(int value)
		{
		return toString() + ": $" + SBFormat.toDollarString(value);
		}
	public final String getClipLabel() { return toString(); }
	@Override public final String toString()
		{
		String it = getName() + "(" + m_period;
		if (m_offset != 0) it += "," + m_offset;
		return it + ")";
		}

	public final String getToolTipText(int x)
		{
		return "$" + SBFormat.toDollarString(getValue(x));
		}
	public final void plot ( TGGraph graph, byte clip, int index )
		{
		int slot = index - m_offset;
		if ( slot < 1 || slot >= m_bars.size()) return;
		int value0 = getNoOffsetValue(slot-1);
		if (value0 <= 0) return;
		graph.setColor( m_color );
		graph.connect ( clip, value0, getNoOffsetValue(slot));
		}
	public final String getMetrics ( int index )
		{
		return SBFormat.toDollarString(getValue(index));
		}

	public final boolean isAbove(int value) { return getValue(m_index) > value; }
	public final boolean isAbove(MovingAverage other)
		{ return getValue(m_index) > other.getValue(m_index); }
	public final boolean crossedAbove(int value)
		{
		return (m_index > 0)
				&& (getValue(m_index) > value)
				&& (getValue(m_index-1) <= value);
		}
	public final boolean crossedAbove(MovingAverage other)
		{
		return (m_index > 0)
				&& (getValue(m_index) > other.getValue(m_index))
				&& (getValue(m_index-1) <= other.getValue(m_index-1));
		}
	public final boolean isBelow(int value) { return getValue(m_index) < value; }
	public final boolean isBelow(MovingAverage other)
		{ return getValue(m_index) < other.getValue(m_index); }
	public final boolean crossedBelow(int value)
		{
		return (m_index > 0)
				&& (getValue(m_index)   <  value)
				&& (getValue(m_index-1) >= value);
		}
	public final boolean crossedBelow(MovingAverage other)
		{
		return (m_index > 0)
				&& (getValue(m_index)   <  other.getValue(m_index))
				&& (getValue(m_index-1) >= other.getValue(m_index-1));
		}

	public final int getPeriod() { return m_period; }

	abstract public String getName();
	abstract public void   dataChanged(int index);
	abstract public int    getNoOffsetValue ( int index );
	final public int getValue ( int index )
		{
		int slot = index - m_offset;
		if ( slot < 1 ) slot = 0;
		else if ( slot >= m_bars.size()) slot = m_bars.size() - 1;
		return getNoOffsetValue(slot);
		}

	/**
	* @return the last calculated value of this MA - in the case
	* of a positive offset, this is the value that is plotted to
	* the right of the last bar.
	*/
	final int getValue() { return getNoOffsetValue(m_index); }

	/**
	* @return one hundred times the slope of this moving average at point x.
	* The "one hundered" is a scaling factor so we can avoid floating point math.
	* The slope is simply the rise of the MA over a run of the specified number
	* of bars.
	*/
	public final int slope100(int x, int numBars)
		{
		if (x < numBars) return 0;
		int y0 = getNoOffsetValue(x-numBars);
		int y1 = getNoOffsetValue(x);
		return 100 * (y1-y0) / numBars;
		}

	public final BarList getBars() { return m_bars; }
	}
