package com.wormtrader.history.indicators;
/********************************************************************
* @(#)Alligator.java 1.00 20120904
* Copyright © 2012 by Richard T. Salamone, Jr. All rights reserved.
*
* Alligator: A "compound indicator" composed of three smoothed and
* offset moving averages as defined by Bill Williams in Trading Chaos
* (2nd Edition).
*
* @author Rick Salamone
* @version 1.00
* 20120904 rts created
* 20130417 rts reworked diType() & angulation() to return dicrete values
* 20130524 rts added range()
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.bars.BarSize;
import com.wormtrader.history.Tape;
import com.wormtrader.history.indicators.*;
import com.shanebow.util.SBFormat;

public class Alligator
	{
	public static final byte DI_NONE=0x00;
	public static final byte DI_BULL=0x01;
	public static final byte DI_BEAR=0x02;

	public static final int RED_OFFSET=5;
	public static final int BLUE_OFFSET=8;
	public static final int BLUE_SLOPE_POINTS=5;
	public static final int RED_SLOPE_POINTS=4;
	public static final int GRN_SLOPE_POINTS=3;

	static final char ANGLE='\u2221';
	static final char DASH_UP_ARROW='\u21E1';
	static final char DASH_DN_ARROW='\u21E3';
	static final char UP_ARROW='\u2191';
	static final char DN_ARROW='\u2193';
	static final char NE_ARROW='\u2197';
	static final char SE_ARROW='\u2198';
	static final char SQUIGLE_ARROW='\u21DD';
	static final char WAVE_ARROW='\u21DD';

	static final char BACKSPACE='\u0008';
	static final char VLINE_ABOVE='\u02C8';
	static final char VLINE_BELOW='\u02CC';
	static final char RING_ABOVE='\u030A';
	static final char RING_BELOW='\u0325';
	static final char DOT_ABOVE='\u0308';
	static final char DOT_BELOW='\u0324';
	static final char DELTA='\u0394';

	public static final String[] DI_TYPE_DESC =
		{ "", "bull" + DASH_UP_ARROW, "bear" + DASH_DN_ARROW };

	static final char[] BIAS_INDICATOR = { SE_ARROW, SQUIGLE_ARROW, NE_ARROW };
//	static final char[] POS_INDICATOR = { RING_BELOW, '\u02D2', RING_ABOVE };
	static final char[] POS_INDICATOR = { RING_BELOW, '-', RING_ABOVE };
	public final SmMA green;  // green = 5,SmMA,C off 3
	public final SmMA red;    // red   = 8,SmMA,C off 5
	public final SmMA blue;   // blue  = 13,SmMA,C off BLUE_OFFSET=8
private final SmMA xMA; // moving average for crosses (red, blue, or green)

	public Alligator(Tape aTape)
		{
		green = (SmMA)aTape.addStudy(SmMA.STUDY_NAME, "5,C,3");
		red   = (SmMA)aTape.addStudy(SmMA.STUDY_NAME, "8,C,"+RED_OFFSET);
		blue  = (SmMA)aTape.addStudy(SmMA.STUDY_NAME, "13,C,"+BLUE_OFFSET);
xMA = blue;
		}

	public String getToolTipText(int x, Bar bar)
		{
		int bias;
try {
		return "\u2224" + SBFormat.toDollarString(bar(x).midpoint())
			+ " " + BIAS_INDICATOR[1+bias(x)]+ POS_INDICATOR[1+barPosition(x)]
			+ " " + DI_TYPE_DESC[diType(x)]
			+ " m: " + SBFormat.toDollarString(slope100(x))
			+ " \u2221 " + angulation(x)
			+ " #" + x + "\u02DF" + priorCrossIndex(x);
} catch (Exception e) { System.out.println("Allie @" + x + ": " + e); return "Oops @" + x; }
		}

	public final int redSlope100(int x) { return red.slope100(x, RED_SLOPE_POINTS); }
	public final int blueSlope100(int x) { return blue.slope100(x, BLUE_SLOPE_POINTS); }

	/**
	* @return one hundred times the average of slope of the blue and red lines at
	* bar index x.
	*/
	public final int slope100(int x)
		{
		return (blue.slope100(x, BLUE_SLOPE_POINTS)
		        + red.slope100(x, RED_SLOPE_POINTS))/2;
		}

	/**
	* @return one hundred times the slope of the price bars at point x, going
	* back to the prior cross.
	* The one hundered is a scaling factor so we can avoid floating point math.
	* This is a very simple naive slope calculation, we average the distance
	* from the red MA to the midPoint of the current bar, with that of the
	* current high (below gator) or current low (below gator) until we can do
	* some regression.
	*/
	public int priceSlope100(int x, int crossAt)
		{
		if (crossAt < 0)
			return 0;
		Bar currentBar = bar(x);
		Bar crossBar = bar(crossAt);
		int maAtCross = xMA.getValue(crossAt);
		int maNow = xMA.getValue(x);
		int high = currentBar.getHigh();
		int low = currentBar.getLow();
		int barChange2 = 0;
		if (high < maNow)
			barChange2 = (high - crossBar.getHigh())
			           + (currentBar.midpoint() - maAtCross);
		else if (low > maNow)
			barChange2 = (low - crossBar.getLow())
			           + (currentBar.midpoint() - maAtCross);

		return 50 * barChange2 / (x - crossAt);
		}

	public String angulation(int x)
		{
		int crossAt = priorCrossIndex(x);
		if (crossAt < 0)
			return "no x-over";
		if (x < 8)
			return "undefined";

		int blueSlope = blueSlope100(x);
		if (blueSlope == 0)
			return "flat allie";
		int priceSlope = priceSlope100(x,crossAt);
		double angulation = (double)priceSlope/blueSlope;
		return SBFormat.toDollarString((int)(100*angulation))
		+ "("+priceSlope+"/"+blueSlope+")";
		}

	/**
	* @return the price range of the gator at the specified index
	*/
	public int range(int x)
		{
		int r = red.getValue(x);
		int g = green.getValue(x);
		int b = blue.getValue(x);
		int it = Math.abs(b-g);
		int rb = Math.abs(r-b);
		int rg = Math.abs(r-g);
		if (rb > it) it = rb;
		if (rg > it) it = rg;
		return it;
		}

	public int angulation100(int x)
		{
		if (x < 8)
			return 0;
		int blueSlope = blueSlope100(x);
		if (blueSlope == 0)
			return 0;
		int crossAt = priorCrossIndex(x);
		if (crossAt < 0)
			return 0;
		int priceSlope = priceSlope100(x,crossAt);
		double angulation = (double)priceSlope/blueSlope;
		return (int)(100*angulation);
		}

	/**
	* @return DI_BULL, DI_NONE, DI_BEAR
	*/
	public byte diType(int x)
		{
		if (x < 2) return DI_NONE;
		Bar thisBar = bar(x);
		Bar prevBar = bar(x-1);
		int prevLo = prevBar.getLow();
		int prevHi = prevBar.getHigh();
		int prevCl = prevBar.getClose();
		int prevRed = red.getValue(x-1);

		int thisLo = thisBar.getLow();
		if (prevCl < prevRed
		&& ((thisLo < prevLo) || (thisLo == prevLo && prevLo < bar(x-2).getLow()))
		&&  thisBar.getClose() >= thisBar.midpoint())
			return DI_BULL;

		int thisHi = thisBar.getHigh();
		if (prevCl > prevRed
		&& (thisHi > prevHi
		    || (thisHi == prevHi && prevHi > bar(x-2).getHigh()))
		&&  thisBar.getClose() <= thisBar.midpoint())
			return DI_BEAR;

		return DI_NONE;
		}

	/**
	* Starting at the specified index, go back to find the index of the last bar
	* that straddles the alligator - use the xMA line.
	* @return index of cross or -1 if no prior cross occured
	*/
	public int priorCrossIndex(int aIndex)
		{
		int bias = bias(aIndex);
		if (bar(aIndex).getLow() > xMA.getValue(aIndex)) // bar above gator
			{
			while (aIndex-- > 0)
				if (bar(aIndex).getLow() <= xMA.getValue(aIndex))
					return aIndex;
			}
		else if (bar(aIndex).getHigh() < xMA.getValue(aIndex)) // bar below
			{
			while (aIndex-- > 0)
				if (bar(aIndex).getHigh() >= xMA.getValue(aIndex))
					return aIndex;
			}
		return -1;
		}

	public final Bar bar(int aIndex) { return red.getBars().get(aIndex); }
	protected int size() { return red.getBars().size(); }

	public final int redAtLastBar()
		{
		int x = red.getBars().size() - 1;
		if (x < RED_OFFSET)
			return bar(x).midpoint();
		return red.getNoOffsetValue(x-RED_OFFSET);
		}

	protected final int priorCrossAbove(int aIndex)
		{
		while (aIndex-- > 0)
			if (bar(aIndex).getLow() <= xMA.getValue(aIndex)) // was red
				return aIndex;
		return -1;
		}

	public final int bias(int aIndex)
		{
		if (aIndex < 5) return 0;
		// get the (offset) values at rhs where they all exist
		int g = green.getNoOffsetValue(aIndex);
		int b = blue.getNoOffsetValue(aIndex-5);
		int r = red.getNoOffsetValue(aIndex-2);
		if ( g > r && r > b ) return 1;
		if ( b > r && r > g ) return -1;
		return 0;
		}
	public final int bias() { return bias(size() - 1); }

	/**
	* @return true if the bar at the specified index is an up bar, above
	* the alligator, which has an upward bias, otherwise returns false
	*/
	public final boolean upupAbove(int x)
		{
		Bar bar = bar(x);
		return (bar.getClose() > bar.getOpen()) // up bar
		    && (bias(x) > 0) // upward bias
		    && (barPosition(x) > 0); // bar above gator
		}
	public final boolean upupAbove() { return upupAbove(size() - 1); }

	/**
	* @return true if the bar at the specified index is a down bar, below
	* the alligator, which has a downward bias, otherwise returns false
	*/
	public final boolean dndnBelow(int x)
		{
		Bar bar = bar(x);
		return (bar.getClose() < bar.getOpen()) // dn bar
		    && (bias(x) < 0) // downward bias
		    && (barPosition(x) < 0); // bar below gator
		}
	public final boolean dndnBelow() { return dndnBelow(size() - 1); }

	/*
	* @return true if the bar at the specified index closed above the gator,
	* which is flat or has an upward bias, otherwise returns false
	*/
	public final boolean above(int x)
		{
		Bar bar = bar(x);
		return (bar.getClose() > red.getValue(x)) // up bar
		    && (bias(x) >= 0); // flat or upward bias;
		}

	public final boolean above() { return above(size() - 1); }

	public final boolean below(int x)
		{
		Bar bar = bar(x);
		return (bar.getClose() < red.getValue(x)) // up bar
		    && (bias(x) <= 0); // flat or upward bias;
		}

	public final boolean below() { return below(size() - 1); }

	public final int barPosition(int aIndex)
		{
		return barPosition(red.getValue(aIndex), bar(aIndex));
		}

	public final int barPosition(int redValue, Bar bar)
		{
		return (bar.getLow()  > redValue)? 1
		     : (bar.getHigh() < redValue)? -1
		     : 0;
		}
	}
