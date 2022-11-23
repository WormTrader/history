package com.wormtrader.history.indicators;
/********************************************************************
* @(#)MACD.java 1.00 2007
* Copyright © 2007-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* MACD - Moving Average Convergence Divergence
*
* @author Rick Salamone
* @version 1.00
* 2007     rts created
* 20110531 rts implemented get/setParams
* 20130302 rts uses List instead of Vector
*******************************************************/
import com.wormtrader.bars.Close;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import com.shanebow.util.SBFormat;
import com.shanebow.util.SBMath;
import com.shanebow.util.SBMisc;
import java.awt.Color;
import java.util.List;

public final class MACD
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="MACD";
	public String getName() { return STUDY_NAME; }

	private static final String DEFAULT_PARAMS="12,26,9"; //"21,55,8";
	private List<? extends Close> m_closes;
	private int        m_minValue = Integer.MAX_VALUE;
	private int        m_maxValue = Integer.MIN_VALUE;
	private Color[]    m_color =
										{ 	Color.DARK_GRAY,          // histogram
											new Color(255,153,153),   // signal
											new Color(  0,255,255) }; // macd
	private int        m_fastPeriod;
	private int        m_slowPeriod;
	private int        m_macdPeriod;

	private int[]      m_fastEMA;
	private int[]      m_slowEMA;
	private int[]      m_macdEMA; // i.e. the signal line
	private int        m_index = -1; // index of last valid data
	private static int _capacityIncrement = 80;

	public MACD( List<? extends Close> closes ) { this ( closes, "" ); }
	public MACD( List<? extends Close> prices, String params )
		{
		m_closes = prices;
		int length = prices.size();
		if ( length < _capacityIncrement )
			length = _capacityIncrement;
		m_fastEMA = new int[length];
		m_slowEMA = new int[length];
		m_macdEMA = new int[length];
		setParams(params);
		}

	private void enlargeArrays()
		{
		m_fastEMA = SBMisc.arrayExpand ( m_fastEMA, _capacityIncrement );
		m_slowEMA = SBMisc.arrayExpand ( m_slowEMA, _capacityIncrement );
		m_macdEMA = SBMisc.arrayExpand ( m_macdEMA, _capacityIncrement );
		}

	public String getParams()
		{
		String params = m_fastPeriod + "," + m_slowPeriod + "," + m_macdPeriod;
		return params.equals(DEFAULT_PARAMS)? "" : params;
		}

	public void setParams( String params )
		{
		if ((params == null) || params.isEmpty())
			params = DEFAULT_PARAMS;
		String[] pieces = params.split(",");
		m_fastPeriod = Integer.parseInt(pieces[0]);
		m_slowPeriod = Integer.parseInt(pieces[1]);
		m_macdPeriod = Integer.parseInt(pieces[2]);

		dataChanged(0);
		}

	public String getRangeString(int value) { return "" + value; }
	public String getClipLabel() { return getName(); }
	public String getToolTipText(int x)
		{
		if ( x < 1 ) return "";
		int macd = m_fastEMA[x] - m_slowEMA[x];
		int signal = m_macdEMA[x];
		int histogram = macd - signal;
		return "macd: " + macd + " sig: " + signal + " histo: " + histogram;
		}

	public void setColor(Color c) { m_color[0] = c; }
	public int getRangeMaximum() { return m_maxValue; }
	public int getRangeMinimum() { return m_minValue; }
	public void dataChanged(int index)
		{
		int numCloses = m_closes.size();
		for ( int i = index; i < numCloses; i++ )
			compute(i);
		}
	private void compute(int index)
		{
		m_index = index;
		Close close = m_closes.get(index);
		int price = close.getPrice();
		if ( index == 0 ) // prime the pump
			{
			m_minValue = m_maxValue = 0;
			m_fastEMA[0] = price;
			m_slowEMA[0] = price;
			m_macdEMA[0] = 0;
			}
		else // do the actual calculations
			{
			if ( m_fastEMA.length <= index )
				enlargeArrays();
			m_fastEMA[index] = SBMath.ema( price, m_fastEMA[index-1], m_fastPeriod );
			m_slowEMA[index] = SBMath.ema( price, m_slowEMA[index-1], m_slowPeriod );
			int macd = m_fastEMA[index] - m_slowEMA[index];
			m_macdEMA[index] = SBMath.ema( macd, m_macdEMA[index-1], m_macdPeriod );
		int histo = macd - m_macdEMA[index];
			if ( macd > m_maxValue ) m_maxValue = macd;
			if ( macd < m_minValue ) m_minValue = macd;
			if ( histo > m_maxValue ) m_maxValue = histo;
			if ( histo < m_minValue ) m_minValue = histo;
			}
		}
	public void plot ( TGGraph graph, byte clip, int index )
		{
		if ( index < 1 ) return;
		int macd0 = m_fastEMA[index-1] - m_slowEMA[index-1];
		int macd1 = m_fastEMA[index] - m_slowEMA[index];
		int signal0 = m_macdEMA[index-1];
		int signal1 = m_macdEMA[index];
		graph.setColor( m_color[0] ); // plot histogram = macd - signal
		graph.mountain( clip, macd0 - signal0, macd1 - signal1 );
		graph.setColor( m_color[1] );   // plot signal line
		graph.connect ( clip, signal0, signal1 );
//		graph.setColor( m_color[2] ); // plot the macd
//		graph.connect ( clip, macd0, macd1 );
		}

	public String getMetrics( int index )
		{
		return "" + getMACD(index) + ", " + getSignal(index)
		     + ", " + getHistogram(index);
		}
	public int getMACD ( int index ) { return m_fastEMA[index] - m_slowEMA[index]; }
	public int getSignal ( int index ) { return m_macdEMA[index]; }
	public int getHistogram ( int index )
		{
		return m_fastEMA[index] - m_slowEMA[index] // MACD minus Signal
					- m_macdEMA[index];
		}
	}
