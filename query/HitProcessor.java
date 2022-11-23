package com.wormtrader.history.query;
/********************************************************************
* @(#)HitProcessor.java 1.00 20130404
* Copyright © 2013 by Richard T. Salamone, Jr. All rights reserved.
*
* HitProcessor: Interface for objects that will process the hits of
* an arbitrary Query.
*
* @author Rick Salamone
* @version 1.00
* 20130404 rts created
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.bars.BarList;

public interface HitProcessor
	{
	/**
	* @return the names of all fields calculated by this hit processor
	*/
	public String[] headers();

	/**
	* @return the classes of the fields calculated by this hit processor
	*/
	public Class[] classes();

	/**
	* Called by the worker after data structures are initialized, but
	* before running the query on any symbols
	* @param BarList aM5Bars - this list will be refilled every day with
	* with the bars the query is going to feed to the Tape for that day
	*/
	public void startRun(BarList aM5Bars);

	/**
	* @param BarList aM5Bars - this list will be refilled every day with
	* with the bars the query is going to feed to the Tape for that day
	*/
	public void startDay(String symbol, String yyyymmdd);

	/**
	* @return the new QueryHit object
	*/
	public QueryHit createHit(String aSymbol, Bar aBar, int indexM5);

	/**
	* @return a HTML formatted String summarizing all the hits
	*/
	public String summary(int aNumHits, int aNumDays);
	}
