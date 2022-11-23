package com.wormtrader.history.indicators;
/********************************************************************
* @(#)TLOC.java 1.00 20130113
* Copyright © 2013 by Richard T. Salamone, Jr. All rights reserved.
*
* TLOC: Short for "The Law of Charts" as put forth by Joe Ross in
* various books including "Electronic Trading TNT I - Gorilla Trading
* Stuff" beginning on page 45.
*
* TNT I, page 65 - ledge definition
* TNT I, page 69 - trading range discussion
*
* @author Rick Salamone
* @version 1.00
* 20130113 rts initial: finds local hi/lo by checking full corrections
* 20130113 rts identifies 123 highs and lows
* 20130302 rts uses BarList
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import java.awt.Color;

final public class TLOC
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="TLOC";
	public String getName() { return STUDY_NAME; }

	public static final byte IS_ONE = (byte)0x01;
	public static final byte IS_TWO = (byte)0x02;
	public static final byte IS_THREE = (byte)0x04;
	public static final byte IS_HI = (byte)0x08;
	public static final byte IS_LO = (byte)0x10;

	private static int  _capacityIncrement = 80;

	private final BarList m_bars;
	private int         m_minValue = Integer.MAX_VALUE;
	private int         m_maxValue = 0;
	private Color[]     m_color = { Color.YELLOW, Color.YELLOW }; // up, down
	private byte[]      m_flags;
	private int         m_index = -1; // index of last valid data
	private int         m_max_correction = 3;

	public TLOC( BarList bars ) { this( bars, "" ); }
	public TLOC( BarList bars, String params )
		{
		m_bars = bars;
		int length = bars.size();
		if ( length < _capacityIncrement )
			length = _capacityIncrement;
		m_flags = new byte[length];
		setParams(params);
		}

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
		for ( int i = index; i < numBars; i++ )
			compute(i);
		}

	private boolean isSet(int index, byte flag)
		{ return (m_flags[index] & flag) != 0; }
	private void set(int index, byte flag) { m_flags[index] |= flag; }

	private void compute(int index)
		{
		m_index = index;
		m_flags[index] = 0;
		if ( index == 0 ) // prime the pump
			{
			m_maxValue = m_minValue = m_bars.get(0).midpoint();
			return;
			}

		int localHighIndex = prevLocalHigh(index, m_max_correction);
		if (localHighIndex >= 0)
			{
			processLocalHighAt(localHighIndex);
			}
		int localLowIndex = prevLocalLow(index, m_max_correction);
		if (localLowIndex >= 0)
			{
//			set(localLowIndex, IS_LO);
			processLocalLowAt(localLowIndex);
			}
		}

	private void processLocalLowAt(int index)
		{
		set(index, IS_LO);
		// since we're processing a low, there should have been a prior hi
		int ioph = prior((byte)(IS_HI|IS_LO), index); // ioph === index of previous high
		if (ioph == -1 || !isSet(ioph, IS_HI))
			return; // there was no previous hi - nothing to do

		// Now find the prior low which could be on the same bar as the prior hi
		int iopl = isSet(ioph, IS_LO)? ioph // iopl === index of prev low
		         : prior(IS_LO, ioph);
		if (iopl == -1 || !isSet(iopl, IS_LO) // prev not a low
		||  isSet(iopl, IS_THREE)) // if prev lo is a #3 this is a hook
			return; // there was no previous lo - nothing to do
		int vopl = m_bars.get(iopl).getLow(); // value of prev lo
		if ( vopl < m_bars.get(index).getLow())
			{
			set(index, IS_THREE);
			set(ioph, IS_TWO);
			// see if there's a lower low prior to the previous low
			// without an intervening hi - if so use it as the #1 low
			int iopll = (prior((byte)(IS_HI|IS_LO), iopl-1));
			if (iopll != -1 && !isSet(iopll, IS_HI)
			&& m_bars.get(iopll).getLow() < vopl)
				iopl = iopll;
			set(iopl, IS_ONE);
			}
		}

	private void processLocalHighAt(int index)
		{
		set(index, IS_HI);
		// since we're processing a high, there should have been a prior lo
		int iopl = prior((byte)(IS_HI|IS_LO), index); // iopl === index of previous low
		if (iopl == -1 || !isSet(iopl, IS_LO))
			return; // there was no previous lo - nothing to do

		// Now find the prior high which could be on the same bar as the prior lo
		int ioph = isSet(iopl, IS_HI)? iopl // ioph === index of prev high
		         : prior(IS_HI, iopl);
		if (ioph == -1 || !isSet(ioph, IS_HI)
		||  isSet(ioph, IS_THREE)) // if prev hi is a #3 this is a hook
			return;
		int voph = m_bars.get(ioph).getHigh(); // value of prev hi
		if ( voph > m_bars.get(index).getHigh())
			{
//System.out.format("local high @ %d; pl[%d]: 0x%04X  ph[%d]: 0x%04X\n",
// index, iopl, m_flags[iopl], ioph, m_flags[ioph]);
			set(index, IS_THREE);
			set(iopl, IS_TWO);
			// see if there's a higher high prior to the previous hi
			// without an intervening lo - if so use it as the #1 high
			int iophh = (prior((byte)(IS_HI|IS_LO), ioph-1));
			if (iophh != -1 && !isSet(iophh, IS_LO)
			&& m_bars.get(iophh).getHigh() > voph)
				ioph = iophh;
			set(ioph, IS_ONE);
			}
		}

	/**
	* Checks if the bar at aIndex makes a full downward correction
	* of the previous bar(s). If so it returns the index of the
	* local high otherwise it returns -1; It will only check up to
	* aMaxBars before returning failure.
	*/
	private int prevLocalHigh(int aIndex, int aMaxBars)
		{
		int index = aIndex;
		Bar bar = m_bars.get(aIndex);
		int lo = bar.getLow();
		int hi = bar.getHigh();

		for (int i = 0; i < aMaxBars; i++)
			{
			if ( --index <= 0 ) return -1;
			bar = m_bars.get(index);
			int priorLo = bar.getLow();
			int priorHi = bar.getHigh();
			if ( lo < priorLo && hi < priorHi )// have lower low & lower high
				{
				// Check whether m_bars[index] is a local high
				for (int j = index-1; ; --j)
					{
					if (j < 0) return 0;
					int priorPriorHi = m_bars.get(j).getHigh();
					if (priorHi > priorPriorHi) // it's a local high
						{
						if (priorHi > m_maxValue) m_maxValue = priorHi;
						return index;
						}
					else if (priorHi < priorPriorHi) // it's not a local high
						break;
					// else if (priorHi == priorPriorHi) it might be a local high
					}
				}
			// Combine the prior bar for the next iteration
			if (priorLo < lo) lo = priorLo;
			if (priorHi < hi) hi = priorHi;
			}
		return -1;
		}

	/**
	* Checks if the bar at aIndex makes a full upward correction
	* of the previous bar(s). If so it returns the index of the
	* local low otherwise it returns -1; It will only check up to
	* aMaxBars before returning failure.
	*/
	private int prevLocalLow(int aIndex, int aMaxBars)
		{
		int index = aIndex;
		Bar bar = m_bars.get(aIndex);
		int lo = bar.getLow();
		int hi = bar.getHigh();

		for (int i = 0; i < aMaxBars; i++)
			{
			if ( --index <= 0 ) return -1;
			bar = m_bars.get(index);
			int priorLo = bar.getLow();
			int priorHi = bar.getHigh();
			if ( lo > priorLo && hi > priorHi )// have higher low & higher high
				{
				// Check whether m_bars[index] is a local high
				for (int j = index-1; ; --j)
					{
					if (j < 0) return 0;
					int priorPriorLo = m_bars.get(j).getLow();
					if (priorLo < priorPriorLo) // it's a local low
						{
						if (priorLo < m_minValue) m_minValue = priorLo;
						return index;
						}
					else if (priorLo > priorPriorLo) // it's not a local low
						break;
					// else if (priorLo == priorPriorLo) it might be a local low
					}
				}
			// Combine the prior bar for the next iteration
			if (priorLo > lo) lo = priorLo;
			if (priorHi > hi) hi = priorHi;
			}
		return -1;
		}

	public void plot ( TGGraph graph, byte clip, int index )
		{
		if ( index < 2 ) return;
		byte flags = m_flags[index];
		if ( flags == 0 ) return;
		Bar bar = m_bars.get(index);
		// if flags is hi & lo need to do some heavy lifting to figure out numbers
		String text = numbers(flags);
		if ((flags & IS_HI) != 0 )
			{
			graph.setColor( m_color[0] );
			if ( text.isEmpty())
				graph.carotAbove (clip, bar.getHigh());
			else
				graph.textAbove (clip, bar.getHigh(), text);
			}
		if ((flags & IS_LO) != 0 )
			{
			graph.setColor( m_color[1] );
			if ( text.isEmpty())
				graph.carotBelow (clip, bar.getLow());
			else
				graph.textBelow (clip, bar.getLow(), text);
			}
		}

	private String numbers(byte aFlags)
		{
		String it = "";
		if ((aFlags & IS_ONE) != 0) it += "1";
		if ((aFlags & IS_TWO) != 0) it += "2";
		if ((aFlags & IS_THREE) != 0) it += "3";
		return it;
		}

	public String getMetrics(int i) { return String.format("%d: 0x%02X", i, m_flags[i]); }
	public byte getValue ( int x ) { return m_flags[x]; }

	/**
	* @return the index of the most recent bar before the specified index
	*         that is an exact match of the specified flags, or -1 if not found.
	*/
	public int priorExact(byte flags, int startIndex)
		{
		for (int x = startIndex - 1; x >= 0; --x)
			if ((m_flags[x] & flags) == flags) return x;
		return -1;
		}

	/**
	* @return the index of the most recent bar before the specified index
	*         that contains any of the specified flags, or -1 if not found.
	*/
	public int prior(byte flags, int startIndex)
		{
		for (int x = startIndex - 1; x >= 0; --x)
			if ((m_flags[x] & flags) != 0) return x;
		return -1;
		}

	/**
	* @return the index of the most recent bar before the specified index
	*         that is an up fractal, or -1 if no up fractal was found.
	*/
	public int priorUp(int startIndex)
		{
		for (int x = startIndex - 1; x >= 0; --x)
			if ((m_flags[x] & IS_HI) != 0) return x;
		return -1;
		}

	/**
	* @return the index of the most recent bar before the specified index
	*         that is an up fractal, or -1 if no up fractal was found.
	*/
	public int priorDown(int startIndex)
		{
		for (int x = startIndex - 1; x >= 0; --x)
			if ((m_flags[x] & IS_LO) != 0) return x;
		return -1;
		}
	}
