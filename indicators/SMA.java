package com.wormtrader.history.indicators;
/********************************************************************
* @(#)SMA.java 1.00 2007
* Copyright © 2007 - 2012 by Richard T. Salamone, Jr. All rights reserved.
*
* SMA: Extends MovingAverage to produce the Simple Moving Average
*
* @author Rick Salamone
* @version 1.00
* 2007???? rts created
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.shanebow.util.SBMisc;

public final class SMA
	extends MovingAverage
	{
	public static final String STUDY_NAME="SMA";

	private int[] m_runningSum;

	public SMA( BarList bars, String params )
		{
		super( bars );
		int length = bars.size();
		if ( length < _capacityIncrement )
			length = _capacityIncrement;
		m_runningSum = new int[length];
		setParams( params );
		}

	@Override public String getName() { return STUDY_NAME; }

	@Override public void dataChanged(int index)
		{
		int numBars = m_bars.size();
		for ( int i = index; i < numBars; i++ )
			compute(i);
		}
	private void compute(int index)
		{
		m_index = index;
		int price = m_bars.get(index).getPrice(m_price);
		if ( index == 0 ) // prime the pump
			{
			m_runningSum[0] = m_minValue = m_maxValue = price;
			}
		else // do the actual calculations
			{
			if ( m_runningSum.length <= index )
				m_runningSum = SBMisc.arrayExpand ( m_runningSum, _capacityIncrement );
			m_runningSum[index] = price + m_runningSum[index-1];
			if ( index >= m_period )
				{
				int drop = m_bars.get(index - m_period).getPrice(m_price);
				m_runningSum[index] -= drop;
				}
			if ( price > m_maxValue ) m_maxValue = price;
			else if ( price < m_minValue ) m_minValue = price;
			}
		}

	@Override public int getNoOffsetValue( int index )
		{
		if ( index >= m_period )
			return m_runningSum[index] / m_period;
		return m_runningSum[index] / (index + 1);
		}
	}
