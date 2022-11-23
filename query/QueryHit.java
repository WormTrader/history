package com.wormtrader.history.query;
/********************************************************************
* @(#)QueryHit.java 1.00 20120506
* Copyright © 2012 by Richard T. Salamone, Jr. All rights reserved.
*
* QueryHit:
*
* @author Rick Salamone
* @version 1.00
* 20120506 rts created
* 20130410 rts added hhmm() method
* 20130411 rts fixed for compiler bug when USD implemented Comparable
*******************************************************/
import com.wormtrader.dao.USD;
import com.shanebow.util.SBDate;

public class QueryHit
	{
	protected final String fSymbol;
	protected final long fTime;
	protected final USD fBasePrice;
	private int fRunLength;

	public QueryHit(String aSymbol, long aTime, int aBasePrice)
		{
		fSymbol = aSymbol;
		fTime = aTime;
		fBasePrice = new USD(aBasePrice);
		}

	public final USD basePrice() { return fBasePrice; }
	public final String symbol() { return fSymbol; }
	public final String yyyymmdd() { return SBDate.yyyymmdd(fTime); }
	public final String hhmm() { return SBDate.hhmm(fTime); }
	public final void setRunLength(int numBars) { fRunLength = numBars; }
	public final int getRunLength() { return fRunLength; }

	public Object field(int aIndex)
		{
		if (aIndex == 2) return fBasePrice; // KLUDGE for compiler
		return (aIndex == 0)? "xyz" //fSymbol
		     : (aIndex == 1)? "123" // SBDate.yyyymmdd__hhmmss(fTime)
//		     : (aIndex == 2)? fBasePrice
		     : (aIndex == 3)? 3 //fRunLength
		     : "?";
		}

	@Override public String toString()
		{
		return fSymbol + " " + SBDate.yyyymmdd__hhmmss(fTime) + fBasePrice;
		}
	}
