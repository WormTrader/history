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
*******************************************************/
import static com.wormtrader.bars.Bar.FADE;
import static com.wormtrader.bars.Bar.FAKE;
import static com.wormtrader.bars.Bar.GREEN;
import static com.wormtrader.bars.Bar.SQUAT;
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;
import com.wormtrader.bars.BarSize;
import com.wormtrader.history.Tape;
import com.wormtrader.history.indicators.*;
import com.shanebow.util.SBFormat;

public final class BWChaos2nd
	extends Alligator
	{
	public final Alligator gator = this;
	public final AO ao;
	public final Fractals fractals;
//	public final MFI mfi;

	public BWChaos2nd(Tape aTape)
		{
		super(aTape);
		ao = (AO)aTape.addStudy(AO.STUDY_NAME, null);
		fractals = (Fractals)aTape.addStudy(Fractals.STUDY_NAME, null);
//		mfi = (MFI)aTape.addStudy(MFI.STUDY_NAME, null);
		}

	public final boolean isFade(int x)
		{
		return (x > 0) && (FADE == bar(x).mfiType(bar(x-1)));
		}

	public final boolean isFake(int x)
		{
		return (x > 0) && (FAKE == bar(x).mfiType(bar(x-1)));
		}

	public final boolean isSquat(int x)
		{
		return (x > 0) && (SQUAT == bar(x).mfiType(bar(x-1)));
		}

	public final boolean isGreen(int x)
		{
		return (x > 0) && (GREEN == bar(x).mfiType(bar(x-1)));
		}

	public final boolean isSquatOrGreen(int x)
		{
		return (x > 0) && bar(x).getVolume() > bar(x-1).getVolume();
		}

	public String barInfo(int x) { return DI_TYPE_DESC[diType(x)]; }

	public boolean divergentDown(int aIndex)
		{
		Bar bar = bar(aIndex);
		if (!bar.isDown()) return false;
		int open = bar.getOpen();
		int rNow = red.getValue(aIndex);
		if (open <= rNow) return false;
		int bias = bias(aIndex);
		if (bias != 1) return false;
		int crossIndex = priorCrossAbove(aIndex);
		int r = red.getValue(crossIndex);
		int blueDelta = blue.getNoOffsetValue(aIndex) - blue.getValue(crossIndex);
		if (blueDelta < 3) return false;
		int priceDelta = open - r;
System.out.format("%s blue delta blue: %d, price: %d diff: %d\n",
com.shanebow.util.SBDate.yyyymmdd__hhmmss(bar.getTime()),
blueDelta, priceDelta, priceDelta - blueDelta);
		return true;
		}

	public final boolean divergentDown() { return divergentDown(size() - 1); }
	}
