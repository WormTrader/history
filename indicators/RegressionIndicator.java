package com.wormtrader.history.indicators;
/********************************************************************
* @(#)RegressionIndicator.java 1.00 20140319
* Copyright © 2014 by Richard T. Salamone, Jr. All rights reserved.
*
* RegressionIndicator: Extends MovingAverage to produce the Regression
* indicator which is formed by connecting the end points of of successive
* Linear Regression Lines, each of which describes the best line thru the
* the values at the current index and going back m_period points.
*
* The calculations are optimized by always translating the x-axis so that
* the origin is at index - period + 1. In this way we only need to calculate
* the sum of the x's, the sum of the x-squared's, and the denominator of the
* equation when the params are set.
*
* @author Rick Salamone
* @version 1.00
* 20140319 rts created
* 20140607 rts finally fixed problem caused by using untranslated x in y = mx + b
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.shanebow.util.SBMisc;

public final class RegressionIndicator
	extends MovingAverage
	{
	public static final String STUDY_NAME="RI";

	private int[] m_values;
	private int Sx = 0;
	private int Sx2 = 0;
	private int m_denominator;

	public RegressionIndicator( BarList bars, String params )
		{
		super(bars);
		int length = bars.size();
		if ( length < _capacityIncrement )
			length = _capacityIncrement;
		m_values = new int[length];
System.out.format("RI.ctor(%s)\n", params);
		setParams( params );
		}

	@Override public final String getName() { return STUDY_NAME; }

	@Override public final void setParams( String params )
		{
		super.setParams(params);
		Sx = 0;
		Sx2 = 0;
		for (int x = 1; x < m_period; x++)
			{
			Sx += x;
			Sx2 += x * x;
			}
		m_denominator = (m_period * Sx2 - Sx*Sx);
System.out.format("RI.setParams(%d) den: %d\n", m_period, m_denominator);
		}

	@Override public void dataChanged(int index)
		{
		int numBars = m_bars.size();
		if (index == 0) {
			m_minValue = Integer.MAX_VALUE;
			m_maxValue = Integer.MIN_VALUE;
			}
		for ( int i = index; i < numBars; i++ )
			compute(i);
		}

	private void compute(int index)
		{
		m_index = index;
		if ( index < m_period ) // prime the pump
			{
			m_values[index] = 0;
			}
		else // do the actual calculations
			{
			if ( m_values.length <= index )
				m_values = SBMisc.arrayExpand ( m_values, _capacityIncrement );

			int Sy = 0;
			int Sxy = 0;
			int j = index - m_period + 1;
			for (int x = 0; x < m_period; x++)
				{
				int y = m_bars.get(j++).getPrice(m_price);
				Sxy += x * y;
				Sy += y;
				}

			long mnum = (m_period * Sxy - Sx*Sy);
			long bnum = (Sy*Sx2 - Sx*Sxy);
			double m = (double)mnum / m_denominator;
			double b = (double)bnum / m_denominator;

			/**
			* Since we've optimized by translating the x-axis so that the firstX
			* is 0 (calculated as j above), then the lastX (index) translates to
			* period-1 when plugging into y = mx + b
			*/
			int y = m_values[index] = (int)(m*(m_period-1) + b);
			if ( y > m_maxValue ) m_maxValue = y;
			else if ( y < m_minValue ) m_minValue = y;
if (index == 40) System.out.format("RI%d.compute(%d) = %d\n", m_period, index, y);

			}
		}

	@Override public int getNoOffsetValue( int index )
		{
		return (index >= m_period)? m_values[index] : 0;
		}
	}
