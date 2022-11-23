package com.wormtrader.history.ui.graph;
/********************************************************************
* @(#)TemplateListener.java 1.00 20120802
* Copyright © 2012 by Richard T. Salamone, Jr. All rights reserved.
*
* TemplateListener: Implement this interface in order to listen to
* changes from a TemplateManager.
* 
* @author Rick Salamone
* @version 1.00
* 20120802 rts created
*******************************************************/

public interface TemplateListener
	{
	abstract public void applySettings(GraphTemplate aTemplate);
	}
