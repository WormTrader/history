package com.wormtrader.history;
/********************************************************************
* @(#)TapeStudy.java 1.00 20090204
* Copyright © 2009-2012 by Richard T. Salamone, Jr. All rights reserved.
*
* TapeStudy:
*
* @author Rick Salamone
* @version 1.00
* 20090204 rts created
* 20100110 rts added getMetrics() method to allow dynamic display in BarTable
*******************************************************/

public interface TapeStudy
	{
	public String getName();
	public void   dataChanged(int index);
	public String getParams();
	public void   setParams( String params );
	/**
	* Metrics are displayed in the BarsTableModel if this study is
	* selected for viewing/tracking
	*/
	public String getMetrics(int index);
	public String getClipLabel();
	}
