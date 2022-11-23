package com.wormtrader.history.indicators;
/********************************************************************
* @(#)MFI.java 1.00 20130510
* Copyright © 2013 by Richard T. Salamone, Jr. All rights reserved.
*
* MFI: Money Flow Index was created by Gene Quong and Avrum Soudack.
* It is also known as volume-weighted RSI.
* Calculation
* 1. Typical Price = (High + Low + Close)/3
* 2. Raw Money Flow = Typical Price * Volume
* 3. Money Flow Ratio = (14-period Positive Money Flow)/(14-period Negative Money Flow)
* 4. Money Flow Index = 100 - 100/(1 + Money Flow Ratio) 
*
* @author Rick Salamone
* @version 1.00
* 20130510 rts created
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import com.shanebow.util.SBMisc;
import java.awt.Color;

public final class MFI
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="MFI";
	public String getName() { return STUDY_NAME; }

	public static final int OVERBOUGHT = 70;
	public static final int OVERSOLD = 30;
	public static final int DEFAULT_PERIOD = 14;

	private final BarList m_bars;
	private Color[]       m_color = { Color.ORANGE };
	private int           m_period = DEFAULT_PERIOD;
	private int[]         m_mfi;
	private int           m_index = -1; // index of last valid data
	private static int    _capacityIncrement = 80;

	public MFI( BarList bars ) { this ( bars, "" ); }
	public MFI( BarList bars, String params )
		{
		m_bars = bars;
		int length = bars.size();
		if ( length < _capacityIncrement )
			length = _capacityIncrement;
		m_mfi = new int[length];
		setParams(params);
		}

	private void enlargeArrays()
		{
		m_mfi = SBMisc.arrayExpand ( m_mfi, _capacityIncrement );
		}

	public String getParams() { return "" + m_period; }

	public void setParams( String params )
		{
		int period = DEFAULT_PERIOD;
		try { period = Integer.parseInt(params.trim()); }
		catch (Exception e) {}
		m_period = period;
		dataChanged(0);
		}

	public String getRangeString(int value)
		{
		return "MFI: " + value;
		}
	public String getClipLabel() { return "MFI(" + m_period + ")"; }
	public String getToolTipText(int x)
		{
		return "MFI(" + m_period + "): " + m_mfi[x];
		}

	public void setColor(Color c) { m_color[0] = c; }
	public int getRangeMaximum() { return 100; }
	public int getRangeMinimum() { return   0; }

	public void dataChanged(int index)
		{
		int numBars = m_bars.size();
		for ( int i = index; i < numBars; i++ )
			compute(i);
		}

	private void compute(int index)
		{
		if ( m_mfi.length <= index )
			enlargeArrays();
		m_index = index;
		long sumUps = 0;   // sum of changes for up days
		long sumDowns = 0; //  and down days over the period
		int count = (++index < m_period)? index : m_period;
		for ( int i = 0; i < count; i++ )
			{
			long change = getChange( --index );
			if ( change < 0 ) sumDowns -= change;
			else sumUps += change;
			}
		double flowRatio = (double)sumUps/(double)sumDowns;
		m_mfi[m_index] = (int)(100.0 - 100.0/(1.0 + flowRatio));
		}

	public void plot ( TGGraph graph, byte clip, int index )
		{
		if ( index < 1 ) return;
		graph.setColor( Color.LIGHT_GRAY ); // plot center line
		graph.connect ( clip, OVERSOLD, OVERSOLD );
		graph.connect ( clip, OVERBOUGHT, OVERBOUGHT );
		graph.setColor( m_color[0] ); // plot smoothed value
		graph.connect ( clip, m_mfi[index-1], m_mfi[index] );
		}

	public String getMetrics ( int index ) { return "" + m_mfi[index]; }
	public int getValue ( int index ) { return m_mfi[index]; }
	public long getChange ( int index )
		{
		Bar bar = m_bars.get(index);
	/*** "Official Implementation ****/
		int price = bar.typicalPrice();
		int prevPrice = (index == 0)? bar.getOpen()
		                            : m_bars.get(index-1).typicalPrice();
		int sign = (price > prevPrice)? 1 : -1;
	/*** My more efficient implementation ****
		int sign = bar.isUp()? 1 : -1;
	***************/
		return sign * bar.moneyFlow();
		}
	}
