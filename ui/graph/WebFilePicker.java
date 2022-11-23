package com.wormtrader.history.ui.graph;
/********************************************************************
* @(#)WebFilePicker.java 1.00 20130217
* Copyright © 2013 by Richard T. Salamone, Jr. All rights reserved.
*
* WebFilePicker: Extends SBFilePicker for selecting files stored on
* the server. Caches a copy of all the users file names as a static
* variable when the class first loads.
*
* @author Rick Salamone
* 20130217 rts created
* 20130218 rts moved actual dir access code to HostFile
*******************************************************/
import com.shanebow.web.host.HostFile;
import com.shanebow.ui.SBFilePicker;
import com.shanebow.util.SBProperties;
//import java.io.*;
//import java.net.*;

public final class WebFilePicker
	extends SBFilePicker
	{
	public WebFilePicker(String aFileDesc, String aDirKey, String aExtKey, String aMruKey)
		{
		super(aFileDesc, aDirKey, aExtKey, aMruKey);
		}

/********
	public void selectMRU()
		{
		String mru = SBProperties.get(fMruProperty);
 		setSelectedItem((mru == null)? SBDialog.UNNAMED : mru);
		}
********/
	/**
	* @returns null if the user quit the dialog, or the user specified
	*          file root name sans directory and extension.
	*          Use getFilespec(name) to get the full filespec.
	public final String promptSaveName(Component aParent)
		{
		SBProperties props = SBProperties.getInstance();
		String ext = props.getProperty(fExtProperty);
		File dir = new File(props.getProperty(fDirProperty, "."));
		String name = SBDialog.promptSaveName(aParent, fFileDesc, dir, ext );
		return ( name != null )? select(name) : null;
		}
	*/

	private static String[] _dirlistCache;
	static void fillCache()
		{
		if (_dirlistCache != null) return;
System.out.println("WebFilePicker HARD CODED USER ID - fillCache: " + "u=7");
		_dirlistCache = HostFile.dir("u=7");
		}

	private static int count(String ext)
		{
		int count = 0;
		for (String f : _dirlistCache)
			if (f.endsWith(ext)) ++count;
		return count;
		}

	protected boolean exists(String aName)
		{
System.out.println("WebFilePicker exists: " + aName);
		if (aName==null || aName.isEmpty())
			return false;
		for (String f : _dirlistCache)
			if (f.equals(aName+fExt))
				return true;
		return false;
		}

	protected String[] dirList()
		{
		if (_dirlistCache == null) fillCache();
		int i = 0;
System.out.println("WebFilePicker fExt: " + fExt);
		int extLen = fExt.length();
		String[] it = new String[count(fExt)];
		for (String f : _dirlistCache)
			if (f.endsWith(fExt))
				it[i++] = f.substring(0,f.length()-extLen);
		return it;
		}
	}
