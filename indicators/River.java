package com.wormtrader.history.indicators;
/*********************************************************************
* River - Developed by Dr. John Bollinger - Version of Bollinger Bands
* that supports fractional sigma values.
*
* 20140319 rts started adding parameter support
* 20140412 rts displays up to two bands
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

public final class River
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="River";
	public String getName() { return STUDY_NAME; }
	public static final String DEFAULTS = "20,1.50,2.00";
/**********
Period,20,int
Price,C,OHLC
Sigmas,2,F2
Show: MA, Bands, Fill
label,type,default,[range]
**********/

	private boolean       m_showMA = false;
	private boolean       m_showBands = false;
	private boolean       m_fill = true;
	private Color[]       m_color = { Color.PINK, new Color(153,153,153),
	                                  new Color(50,255,255, 40), new Color(255,255,255, 80) }; // new Color(220,234,237, 50)
	private int           m_period;
	private int           m_numBands = 1;
	private int           m_widestBand = 0;

	private BarList       m_bars;
	private int           m_minValue = Integer.MAX_VALUE;
	private int           m_maxValue = Integer.MIN_VALUE;
	private int[]         m_100bandSigmas; // length == m_numBands
	private int[]         m_100sigma;    // 100 * one std deviation of last m_period prices
	private int           m_index = -1;  // index of last valid data
	private static int    _capacityIncrement = 80;
	private MovingAverage m_ma;

	public River( BarList bars )
		{
		this ( bars, DEFAULTS );
		}
	public River( BarList bars, String params )
		{
		m_bars = bars;
		int length = bars.size();
		if ( length < _capacityIncrement )
			length = _capacityIncrement;
		m_100sigma = new int[length];
System.out.println("River ctor: " + params);
		setParams(params);
		}

	private void enlargeArrays()
		{
		m_100sigma = SBMisc.arrayExpand ( m_100sigma, _capacityIncrement );
		}

	public String getParams()
		{
		String it = "" + m_period;
		for (int i = 0; i < m_100bandSigmas.length; i++)
			it += "," + SBFormat.toDollarString(m_100bandSigmas[i]);
		return it;
		}

	public void setParams( String params )
		{
		if ((params == null) || params.isEmpty())
			params = DEFAULTS;
System.out.println( getClass().getSimpleName() + ".params: " + params);
		String[] pieces = params.split(",");
		if (pieces.length < 2)
			throw new IllegalArgumentException(STUDY_NAME + " requires at least 2 params");
		m_period = Integer.parseInt(pieces[0]);
		m_numBands = pieces.length - 1;
System.out.println( getClass().getSimpleName() + ".numBands: " + m_numBands);
		m_100bandSigmas = new int[m_numBands];
		int maxSigmas = 0;
		for (int i = 1; i < pieces.length; i++)
			{
			m_100bandSigmas[i-1] = SBFormat.parseDollarString(pieces[i].trim());
			if ( m_100bandSigmas[i-1] > maxSigmas )
				{
				maxSigmas = m_100bandSigmas[i-1];
				m_widestBand = i - 1;
				}
			}

if (m_100bandSigmas[0] == 50) m_color[2] = new Color(255,102,102, 70);

		m_ma = new SMA( m_bars, "" + m_period + ",C" );
		dataChanged(0);
System.out.println( getClass().getSimpleName() + ".setParams: " + getParams());
		}

	public String getRangeString(int value)
		{
		return "River: " + value;
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
		if ( m_100sigma.length <= index )
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
		m_100sigma[m_index] = (int)(Math.sqrt((double)sum / count) * 100.0);
		int maxWidth = bandWidth(m_index, m_100bandSigmas[m_widestBand]);

		int bb = ma + maxWidth;
		if ( bb > m_maxValue ) m_maxValue = bb;
		bb = ma - maxWidth;
		if ( bb < m_minValue ) m_minValue = bb;
		}

	/**
	* @return distance from moving average to the band for the given index
	* and number of standard deviations (x100)
	*/
	private int bandWidth(int index, int aNumSigmasx100)
		{
		return aNumSigmasx100 * m_100sigma[index] / 10000;
		}

	public void plot ( TGGraph graph, byte clip, int index )
		{
		if ( index < 1 ) return;
		int ma = m_ma.getValue( index );
		int maPrev = m_ma.getValue( index - 1 );
		for (int b = m_numBands - 1; b >= 0; --b)
			{
			int widthPrev = bandWidth(index-1, m_100bandSigmas[b]);
			int topPrev = maPrev + widthPrev;
			int botPrev = maPrev - widthPrev;

			int widthNow = bandWidth(index, m_100bandSigmas[b]);
			int topNow = ma + widthNow;
			int botNow = ma - widthNow;

			if (m_fill)
				{
				graph.setColor( m_color[2+b] );
				graph.fill(clip, topPrev, topNow, botPrev, botNow);
				}
			if (m_showBands)
				{
				graph.setColor( m_color[1] ); // plot BB's
				graph.connect ( clip, topPrev, topNow );
				graph.connect ( clip, botPrev, botNow );
				}
			}
		if ( m_showMA )
			{
			graph.setColor( m_color[0] ); // plot MA
			graph.connect ( clip, maPrev, ma );
			}
		}

	public String getMetrics ( int index )
		{
		if ( index < 1 ) return "";
		int ma = m_ma.getValue( index );
		String it = "ma: " + SBFormat.toDollarString(ma);
		for (int b = 0; b < m_numBands; b++)
			it += "band[" + b + "]"
			   + SBFormat.toDollarString(bandWidth(index, m_100bandSigmas[b]));
		return it;
		}

	public int getValue ( int index ) { return bandWidth(index, m_100bandSigmas[m_widestBand]); }
	public int getLowerBand ( int index )
		{ return m_ma.getValue(index) - bandWidth(index, m_100bandSigmas[m_widestBand]); }
	public int getUpperBand ( int index )
		{ return m_ma.getValue(index) + bandWidth(index, m_100bandSigmas[m_widestBand]); }
	}
