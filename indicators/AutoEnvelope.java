package com.wormtrader.history.indicators;
/********************************************************************
* @(#)AutoEnvelope.java.java 1.00 20110528
* Copyright (c) 2011 by Richard T. Salamone, Jr. All rights reserved.
*
* AutoEnvelope.java: Similar to Elder's AutoEnvelope, except
* that values change every day. Elder's version is described
* in "Come into My Trading Room," as follows:
*
* Envelope channels are set parallel to the moving average (the
* slow MA if you use two MAs). The two channel lines must contain
* approximately 90-95% of all prices for the past two or three
* months between them, with only the extremes protruding outside.
* Envelope channels provide attractive profit targets – sell longs
* near the upper channel line and cover shorts near the lower channel.
*
* The AutoEnvelope is a custom indicator - an original tool that
* automatically sizes channels by calculating a standard deviation
* for the last 100 bars. It is designed to change value at most once
* a week, making it suitable even for intra-day data.
*
* Base EMA (22) – This is the number of bars for the Exponential
* Moving Average plotted in the center of the channel. 
* Factor (27) – This is the number of standard deviations (expressed
* in tenths) for creating the channel. Statistically, 2.7 is the proper
* size to use, but that assumes a standard distribution, which is not
* always present in the markets. We find that 27 works for most stocks
* using daily data, but you may want to adjust this Factor to fit your
* trading vehicle and style.
*
* @author Rick Salamone
* @version 0.00 20110519 rts pseudocode
* @version 1.00 20110528 rts first release
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import com.shanebow.util.SBFormat;
import com.shanebow.util.SBMath;
import com.shanebow.util.SBMisc;
import java.awt.Color;

public final class AutoEnvelope
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="AutoEnvelope";
	public String getName() { return STUDY_NAME; }

	private static final int DEFAULT_PERIOD = 22;
	private static final int DEFAULT_FACTOR = 27; // multiplier for band widths
	private static final int DEFAULT_LOOKBACK = 100;

	private BarList   m_bars;
	private boolean       m_showMA = false;
	private int           m_minValue = Integer.MAX_VALUE;
	private int           m_maxValue = Integer.MIN_VALUE;
	private Color[]       m_color = { Color.PINK, new Color(143,126,98) };
	private int           m_lookback = DEFAULT_LOOKBACK;
	private int           fFactor = DEFAULT_FACTOR;
	private int[]         m_bandOffset; // std deviation of last m_lookback prices
	private int[]         m_work; // holds the range for m_lookback periods
	private int           m_index = -1; // index of last valid data
	private static int    _capacityIncrement = 80;
	private MovingAverage m_ma;

	public AutoEnvelope( BarList bars )
		{
		this ( bars, "" );
		}
	public AutoEnvelope( BarList bars, String params )
		{
		m_bars = bars;
		int length = bars.size();
		if ( length < _capacityIncrement )
			length = _capacityIncrement;
		m_bandOffset = new int[length];
		setParams(params);
		}

	private void enlargeArrays()
		{
		m_bandOffset = SBMisc.arrayExpand ( m_bandOffset, _capacityIncrement );
		}

	public String getParams() { return ""; }
	public void setParams( String params )
		{
		m_ma = new EMA( m_bars, "22,C" );
		m_work = new int[m_lookback];
		dataChanged(0);
		if ((params == null) || params.isEmpty())
			return;
		System.err.println( getClass().getSimpleName() + ".setParams: NOT IMPLEMENTED");
		}

	public String getRangeString(int value)
		{
		return "AutoEnvelope: " + value;
		}
	public String getClipLabel() { return "Env(" + m_lookback + ")"; }
	public String getToolTipText(int x)
		{
		return "Autoenvelope";
		}

	public void setColor(Color c) { m_color[0] = c; }
	public int getRangeMaximum() { return m_maxValue; }
	public int getRangeMinimum() { return m_minValue; }

	public void dataChanged(int index)
		{
		if ( index == 0 )
			{
			m_minValue = Integer.MAX_VALUE;
			m_maxValue = Integer.MIN_VALUE;
			}
		m_ma.dataChanged(index);
		int numBars = m_bars.size();
		for ( int i = index; i < numBars; i++ )
			compute(i);
		}
	private void compute(int index)
		{
		if ( m_bandOffset.length <= index )
			enlargeArrays();
		int ma = m_ma.getValue( index );
		m_index = index;
		int count = Math.min(++index, m_lookback);
		for ( int i = 0; i < count; i++ )
			m_work[i] =  m_bars.get(--index ).range();

		double x = 0;
		for ( int i = 0; i < count; i++ )
			x += Math.pow(m_work[i],2) - (m_work[i] * (m_work[i]/count));
		m_bandOffset[m_index] = (int)((Math.sqrt(x/(count-1))) * fFactor / 10.0);
		int bb = ma + m_bandOffset[m_index];
		if ( bb > m_maxValue ) m_maxValue = bb;
		bb = ma - m_bandOffset[m_index];
		if ( bb < m_minValue ) m_minValue = bb;
		}
	public void plot ( TGGraph graph, byte clip, int index )
		{
		if ( index < 1 ) return;
	if ( index < m_lookback/4 ) return;
		int ma = m_ma.getValue( index );
		int maPrev = m_ma.getValue( index - 1 );

		if ( m_showMA )
			{
			graph.setColor( m_color[0] ); // plot MA
			graph.connect ( clip, maPrev, ma );
			}
		graph.setColor( m_color[1] ); // plot BB's
		graph.connect ( clip, maPrev + m_bandOffset[index-1], ma + m_bandOffset[index] );
		graph.connect ( clip, maPrev - m_bandOffset[index-1], ma - m_bandOffset[index] );
		}

	public String getMetrics ( int index )
		{
		if ( index < 1 ) return "";
		int ma = m_ma.getValue( index );
		return SBFormat.toDollarString(ma - m_bandOffset[index])
		       + ", " + SBFormat.toDollarString(ma + m_bandOffset[index]);
		}

	public int getValue ( int index ) { return m_bandOffset[index]; }
	public int getLowerBand ( int index )
		{ return m_ma.getValue(index) - m_bandOffset[index]; }
	public int getUpperBand ( int index )
		{ return m_ma.getValue(index) + m_bandOffset[index]; }
	}
