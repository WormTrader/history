package com.wormtrader.history.indicators;
/********************************************************************
* @(#)Temperature.java 1.00 20120710
* Copyright © 2012-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* Temperature: Calculates Market Temperature as developed by Dr. Alexander
* Elder.
*
* @author Rick Salamone
* @version 1.00
* 20120710 rts created
* 20130302 rts uses BarList
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import com.shanebow.util.SBMath;
import com.shanebow.util.SBMisc;
import java.awt.Color;

/*********************************************************************
*
*********************************************************************/
final public class Temperature
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="Market Temperature";
	public String getName() { return STUDY_NAME; }

	private static final int DEFAULT_PERIOD = 22;
	private BarList m_bars;
	private int         m_maxValue = 0;
	private Color[]     m_color = { Color.RED, Color.BLUE };
	private int         m_period = DEFAULT_PERIOD;
	private int[]       m_ema;   // smoothed force index
	private int         m_index = -1; // index of last valid data
	private static int  _capacityIncrement = 80;

	public Temperature( BarList bars )
		{
		this ( bars, "" );
		}
	public Temperature( BarList bars, String params )
		{
		m_bars = bars;
		int length = bars.size();
		if ( length < _capacityIncrement )
			length = _capacityIncrement;
		m_ema = new int[length];
		setParams(params);
		}

	private void enlargeArrays()
		{
		m_ema = SBMisc.arrayExpand ( m_ema, _capacityIncrement );
		}

	public String getParams() { return ""; }
	public void setParams( String params )
		{
		dataChanged(0);
		if ((params == null) || params.isEmpty())
			return;
		System.err.println( getClass().getName() + ".setParams: NOT IMPLEMENTED");
		}

	public String getRangeString(int value)
		{
		return "Temperature: " + value;
		}
	public String getClipLabel() { return "Temp"; }
	public String getToolTipText(int x)
		{
		return "Temp: " + getMktTemp(x) + " EMA: " + m_ema[x];
		}

	public void setColor(Color c) { m_color[0] = c; }
	public int getRangeMaximum()
		{
		return (int)(1.1 * m_maxValue);
		}
	public int getRangeMinimum()
		{
		return 0;
		}
	public void dataChanged(int index)
		{
		int numBars = m_bars.size();
		for ( int i = index; i < numBars; i++ )
			compute(i);
		}
	private void compute(int index)
		{
		m_index = index;
		if ( index == 0 ) // prime the pump
			{
			m_ema[0] = m_maxValue = 0;
			return;
			}
		if ( m_ema.length <= index )
			enlargeArrays();
		int mt = getMktTemp( index );
		m_ema[index] = SBMath.ema( mt, m_ema[index-1], m_period );
		if ( mt > m_maxValue ) m_maxValue = mt;
		}
	public void plot ( TGGraph graph, byte clip, int index )
		{
		if ( index < 1 ) return;
		graph.setColor( m_color[0] ); // plot temp
		graph.vLine ( clip, 0, getMktTemp(index));
		graph.setColor( m_color[1] ); // plot smoothed temp
		graph.connect ( clip, m_ema[index-1], m_ema[index] );
		}

	public String getMetrics( int index )
		{
		return "" + getMktTemp(index) + ", " + m_ema[index];
		}
	public int getValue ( int index ) { return m_ema[index]; }
	public int getMktTemp ( int index )
		{
		if ( index == 0 )
			return 0;
		Bar bar = m_bars.get(index-1);
		int hiPrev = bar.getHigh();
		int loPrev = bar.getLow();
		bar = m_bars.get(index);
		int hiDelta = bar.getHigh() - hiPrev;
		int loDelta = loPrev - bar.getLow();
		if (( hiDelta < 0 ) && ( loDelta < 0 )) return 0;
		return ( hiDelta > loDelta ) ? hiDelta : loDelta;
		}
	}
