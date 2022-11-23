package com.wormtrader.history.indicators;
/********************************************************************
* @(#)EMA.java 1.00 2007
* Copyright © 2007 - 2012 by Richard T. Salamone, Jr. All rights reserved.
*
* EMA: Extends MovingAverage to produce the Exponential Moving Average
*
* @author Rick Salamone
* @version 1.00
* 2007???? rts created
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.shanebow.util.SBMath;
import com.shanebow.util.SBMisc;
import java.awt.Color;

public final class EMA
	extends MovingAverage
	{
	public static final String STUDY_NAME="EMA";
	public static final int PRECISION = 10000;

	private int[]        m_ema;

	public EMA( BarList bars, String params )
		{
		super( bars );
		int length = bars.size();
		if ( length < _capacityIncrement )
			length = _capacityIncrement;
		m_ema = new int[length];
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
			m_minValue = m_maxValue = price;
			m_ema[0] = price * PRECISION;
			}
		else // do the actual calculations
			{
			if ( m_ema.length <= index )
				m_ema = SBMisc.arrayExpand ( m_ema, _capacityIncrement );
			m_ema[index] = SBMath.ema( price * PRECISION, m_ema[index-1], m_period );
			if ( price > m_maxValue ) m_maxValue = price;
			else if ( price < m_minValue ) m_minValue = price;
			}
		}

	@Override public int getNoOffsetValue(int index)
		{
		return m_ema[index]/PRECISION;
		}
	}
