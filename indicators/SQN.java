package com.wormtrader.history.indicators;
/********************************************************************
* @(#)SQN.java 1.00 20120626
* Copyright © 2012 by Richard T. Salamone, Jr. All rights reserved.
*
* SQN: Van Tharp's System Quality Number calculation, but applied
* to the change in closing prices rather than for trade results as
* originally designed.
* SQN = Squareroot(N) * Average (Profit&Loss) / Stdev (Profit&Loss)
*
* Tharp applies these to determine his market-type directional component criteria:
*  Strong Bull > 1.47
*  Bull >= 0.7
*  Neutral < .7 and >= 0
*  Bear < 0
*  Strong Bear < -0.7
*
* @author Rick Salamone
* 20120626 rts created
* 20120710 rts implemented get/setParams
* 20120716 rts fixed bug with range doesn't include "0" & reduced array size
*******************************************************/
import java.awt.Color;
import java.util.List;
import com.wormtrader.bars.Close;
import com.wormtrader.history.TapeStudy;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.ui.graph.GraphClipRenderer;
import com.shanebow.util.SBFormat;
import com.shanebow.util.SBMath;
import com.shanebow.util.SBMisc;

public final class SQN
	implements TapeStudy, GraphClipRenderer
	{
	public static final String STUDY_NAME="SQN";
	public String getName() { return STUDY_NAME; }

	private static final int DEFAULT_PERIOD=100;
	private static final int SCALING_FACTOR=100; // avoid floating point math

	private List<? extends Close>   m_closes;
	private int        m_minValue = -16; // these may grow, but want to have zero
	private int        m_maxValue = 16;  // visible as a reference point
	private Color[]    m_color = {Color.WHITE, Color.ORANGE};
	private int        m_period;
	private double     m_sqrtPeriod;

	private int[]      m_sqn; // actually SCALING_FACTOR * SQN, since format with 2 decimals
	private int[]      m_change; // working array of daily price change in cents
	private int        m_index = -1; // index of last valid data
	private static int _capacityIncrement = 40;

	public SQN( List<? extends Close> closes ) { this(closes, ""); }
	public SQN( List<? extends Close> prices, String params )
		{
		m_closes = prices;
		setParams(params);
		}

	public String getParams() { return "" + m_period; }

	public void setParams( String params )
		{
		int period = DEFAULT_PERIOD;
		try { period = Integer.parseInt(params.trim()); }
		catch (Exception e) {System.out.println(STUDY_NAME + " params: '" + params + "' " + e);}
		if (m_period == period) return; // unchanged
		m_period = period;
		m_sqrtPeriod = Math.sqrt(m_period);
		m_change = new int[m_period];
		int length = m_closes.size() - m_period;
		if ( length < 0 )
			length = 0;
		m_sqn = new int[length];
		dataChanged(0);
		}

	private void enlargeArrays()
		{
		m_sqn = SBMisc.arrayExpand ( m_sqn, _capacityIncrement );
		}

	private int getScaled(int index)
		{
		int slot = index - m_period;
		return (slot < 0) ? 0 : m_sqn[slot];
		}
	private void setScaled(int index, int scaledSQN)
		{
		int slot = index - m_period;
		if (slot < 0)
			return;
		if ( m_sqn.length <= index )
			enlargeArrays();
		m_sqn[slot] = scaledSQN;
		if ( scaledSQN > m_maxValue ) m_maxValue = scaledSQN;
		if ( scaledSQN < m_minValue ) m_minValue = scaledSQN;
		}

	public String getRangeString(int value)
		{
		return "SQN: " + SBFormat.toDollarString(value);
		}
	public String getClipLabel() { return getName() + "(" + m_period + ")"; }
	public String getToolTipText(int x)
		{
		if ( x < m_period ) return "";
		return getName() + ": " + SBFormat.toDollarString(getSQN(x));
		}

	public void setColor(Color c) { m_color[0] = c; }
	public int getRangeMaximum() { return m_maxValue+4; }
	public int getRangeMinimum() { return m_minValue-4; }
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
			m_minValue = -16;
			m_maxValue = 16;
			setScaled(0, 0);
			}
		else // do the actual calculations
			{
			int first = index - m_period;
			if (first < 0)
				return;
			double sum = 0;
			for ( int i = 0; i < m_period; i++ )
				{
			int prev = m_closes.get(index-i-1).getPrice();
				m_change[i] = (m_closes.get(index-i).getPrice()
				            - prev)*100/prev;
				sum += m_change[i];
				}
			double mean = sum / m_period;
			sum = 0;
			for ( int change : m_change )
				{
				double diff = change - mean;
				sum += diff * diff;
				}
			int sqn = (int)(SCALING_FACTOR * m_sqrtPeriod * mean / Math.sqrt(sum/m_period));
			setScaled(index, sqn);
			}
		}

	public void plot(TGGraph graph, byte clip, int index)
		{
		graph.setColor( m_color[1] );
		graph.connect ( clip, 0, 0);
		if ( index <= m_period ) return;
		graph.setColor( m_color[0] );
		graph.connect ( clip, getScaled(index-1), getScaled(index));
		}

	public String getMetrics(int index) { return "" + getSQN(index); }
	public double getSQN(int index) { return (double)m_sqn[index]/(double)SCALING_FACTOR; }
	public int getValue(int index) { return m_sqn[index]; }
	}
