package com.wormtrader.history.indicators;
/********************************************************************
* @(#)WMA.java 1.00 20120721
* Copyright © 2007 - 2012 by Richard T. Salamone, Jr. All rights reserved.
*
* WMA: Extends MovingAverage to produce the Weighted Moving Average
*
* @author Rick Salamone
* @version 1.00
* 20120721 rts created
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.shanebow.util.SBMisc;

public final class WMA
	extends MovingAverage
	{
	public static final String STUDY_NAME="WMA";

	private int[] m_wma;

	public WMA( BarList bars, String params )
		{
		super( bars );
		int length = bars.size();
		if ( length < _capacityIncrement )
			length = _capacityIncrement;
		m_wma = new int[length];
		setParams( params );
		}

	@Override public String getName() { return STUDY_NAME; }

	@Override public void dataChanged(int index)
		{
		int numBars = m_bars.size();
		if ( m_wma.length <= numBars )
			m_wma = SBMisc.arrayExpand ( m_wma, _capacityIncrement );

		for ( int i = index; i < numBars; i++ )
			m_wma[i] = compute(i);
		}
	private int compute(int index)
		{
		m_index = index;
		if ( index == 0 ) // prime the pump
			return m_minValue = m_maxValue = m_bars.get(index).getPrice(m_price);

		int sum = 0; // sum of the prices time their weight
		int num = 0; // number of weight
		int weight = Math.min(m_period,m_bars.size());
		while ( weight > 0 )
			{
			int price = m_bars.get(index).getPrice(m_price);
			sum += price * weight;
			num += weight;
			if (--index < 0) break;
			--weight;
			}
		return sum/num;
		}
	
	@Override public int getNoOffsetValue(int index) { return m_wma[index]; }
	}
