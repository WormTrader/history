package com.wormtrader.history.indicators;
/*********************************************************************
* Dragon - Developed by Dr. John Bollinger - Version of Bollinger Bands
* that supports fractional sigma values.
*
* 20140319 rts started adding parameter support
*********************************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import com.shanebow.util.SBFormat;
import com.shanebow.util.SBMath;
import com.shanebow.util.SBMisc;
import java.awt.Color;

public final class Dragon
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="Dragon";
	public String getName() { return STUDY_NAME; }

	private static final int DEFAULT_PERIOD = 10;
	private static final int DEFAULT_SIGMASx100 = 200; // multiplier for band widths

	private boolean       m_showMA = false;
	private boolean       m_showBands = false;
	private boolean       m_fill = true;
	private Color[]       m_color = { Color.PINK, new Color(153,153,153),
	                                  new Color(255,53,53, 100) };
	private int           m_period = 10; // DEFAULT_PERIOD;
	private int           m_numSigmasx100 = 50; // DEFAULT_SIGMASx100;
/**********
Period: 20, int
Price: C, OHLC select
Sigmas: 2, float 2
Show: MA, Bands, Fill
**********/

	private BarList   m_bars;
	private int           m_minValue = Integer.MAX_VALUE;
	private int           m_maxValue = Integer.MIN_VALUE;
	private int[]         m_bandOffset;   // std deviation of last m_period prices
	private int           m_index = -1; // index of last valid data
	private static int    _capacityIncrement = 80;
	private MovingAverage m_ma;

	public Dragon( BarList bars )
		{
		this ( bars, "" );
		}
	public Dragon( BarList bars, String params )
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
		m_ma = new SMA( m_bars, "" + m_period + ",C" );
		dataChanged(0);
		if ((params == null) || params.isEmpty())
			return;
		System.err.println( getClass().getSimpleName() + ".setParams: NOT IMPLEMENTED");
		}

	public String getRangeString(int value)
		{
		return "Dragon: " + value;
		}
	public String getClipLabel() { return "BB(" + m_period + ")"; }
	public String getToolTipText(int x)
		{
		return "BB";
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
		int sum = 0;
		int count = Math.min(++index, m_period);
		for ( int i = 0; i < count; i++ )
			{
			int work = m_bars.get( --index ).getClose() - ma;
			work *= work;
			sum += work;
			}
		m_bandOffset[m_index] = (int)(m_numSigmasx100 * Math.sqrt((double)sum / count) /100.0);
		int bb = ma + m_bandOffset[m_index];
		if ( bb > m_maxValue ) m_maxValue = bb;
		bb = ma - m_bandOffset[m_index];
		if ( bb < m_minValue ) m_minValue = bb;
		}
	public void plot ( TGGraph graph, byte clip, int index )
		{
		if ( index < 1 ) return;
		int ma = m_ma.getValue( index );
		int maPrev = m_ma.getValue( index - 1 );
		int topPrev = maPrev + m_bandOffset[index-1];
		int topNow = ma + m_bandOffset[index];
		int botPrev = maPrev - m_bandOffset[index-1];
		int botNow = ma - m_bandOffset[index];

		if (m_fill)
			{
			graph.setColor( m_color[2] );
			graph.fill(clip, topPrev, topNow, botPrev, botNow);
			}
		if ( m_showMA )
			{
			graph.setColor( m_color[0] ); // plot MA
			graph.connect ( clip, maPrev, ma );
			}
		if (m_showBands)
			{
			graph.setColor( m_color[1] ); // plot BB's
			graph.connect ( clip, topPrev, topNow );
			graph.connect ( clip, botPrev, botNow );
			}
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
