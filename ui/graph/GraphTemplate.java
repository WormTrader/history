package com.wormtrader.history.ui.graph;
/********************************************************************
* GraphTemplate.java 1.00 20080518
* Copyright © 2007 - 2012 by Richard T. Salamone, Jr. All rights reserved.
*
* GraphTemplate: Saves and restores the graph settings to/from a disk file.
*
* @author Rick Salamone
* @version 1.00
* 20080518 rts created
* 20120719 rts moved MA parse/toCSV to MASettings
* 20130219 rts use HostFile to support remote files
*******************************************************/
import com.wormtrader.bars.Bar;
import com.shanebow.web.host.HostFile;
import com.shanebow.ui.SBDialog;
import java.awt.Color;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.io.IOException;
import java.io.EOFException;

public final class GraphTemplate
	{
	public static final String MA_SECTION_TITLE="Moving Averages";
	public static final String CLIPS_SECTION_TITLE="Clips";
	public static final String OLAYS_SECTION_TITLE="Overlays";
	public static final int MAX_AVERAGES=5;
	public static final String BAD_FILE_CHARS = "\\/:*?\"<>.,|";

	private String[]     m_olayName;
	private String[]     m_olayParams;
	private String[]     m_clipName;
	private String[]     m_clipParams;
	private MASettings[] m_maSettings =
		{
		new MASettings ( MASettings.USE_SMA,   5, Bar.PRICE_CLOSE, Color.YELLOW ),
		new MASettings ( MASettings.USE_EMA,   8, Bar.PRICE_CLOSE, new Color(80,80,0)),
		new MASettings ( MASettings.USE_EMA,  21, Bar.PRICE_CLOSE, new Color(127,127,0)),
		new MASettings ( MASettings.USE_EMA,  55, Bar.PRICE_CLOSE, Color.ORANGE ),
		new MASettings ( MASettings.USE_EMA, 100, Bar.PRICE_CLOSE, Color.GRAY),
		};
	private int m_lineNumber = 0;
	private String m_name;

	public final boolean freeze( String filespec )
		{
		PrintWriter file = null;
		try
			{
			file = HostFile.printWriter(filespec);

			int n = m_maSettings.length;
			file.println( MA_SECTION_TITLE + ": " + n );
			for ( int i = 0; i < n; i++ )
				file.println(m_maSettings[i].toCSV());

			n = m_olayName.length;
			file.println( OLAYS_SECTION_TITLE + ": " + n );
			for ( int i = 0; i < n; i++ )
				file.println( m_olayName[i] );

			n = m_clipName.length;
			file.println( CLIPS_SECTION_TITLE + ": " + n );
			for ( int i = 0; i < n; i++ )
				file.println( m_clipName[i] );
			}
		catch (Exception e)
			{
			return SBDialog.error( "Error Saving: " + filespec, e.getMessage());
			}
		finally
			{
			try { if (file != null) file.close();}
			catch (Exception ignore) {}
			}
		m_name = filespec;
		return true;
		}

	public final void clear()
		{
		for ( MASettings ma : m_maSettings)
			ma.setActive(false);
		m_olayName = null;
		m_olayParams = null;
		m_clipName = null;
		m_clipParams = null;
		}

	public final boolean thaw ( String name )
		{
		m_name = name;
		m_lineNumber = 0;
		String caller = "thaw(" + name + ")";
		BufferedReader stream = null;
		try
			{
			stream = HostFile.bufferedReader(name);
			if (stream == null )
				return logError ( null, caller, "File open error" );

			// Load the Moving Averages
			String[] pieces = splitNextLine( stream );
			if ( !pieces[0].startsWith(MA_SECTION_TITLE))
				return logError ( stream, caller, "Corrupt file: Expected Averages" );
			int n = Integer.parseInt(pieces[1]);
			if ( n != m_maSettings.length )
				return logError ( stream, caller, "Corrupt file: Wrong # of Averages" );
			for ( int i = 0; i < n; i++ )
				{
				pieces = splitNextLine(stream);
				if (!m_maSettings[i].set(pieces))
					return logError ( stream, caller, "Corrupt file: Malformed Average" );
				}

			// Load the Overlays
			pieces = splitNextLine( stream );
			if ( !pieces[0].startsWith(OLAYS_SECTION_TITLE))
				return logError ( stream, caller, "Corrupt file: Expected Overlays" );
			n = Integer.parseInt(pieces[1]);
			m_olayName = new String[n];
			m_olayParams = new String[n];
			for ( int i = 0; i < n; i++ )
				{
				pieces = splitNextLine(stream);
				m_olayName[i] = pieces[0];
				m_olayParams[i] = (pieces.length > 1)? pieces[1] : "";
				}

			// Load the Clips
			pieces = splitNextLine( stream );
			if ( !pieces[0].startsWith(CLIPS_SECTION_TITLE))
				return logError ( stream, caller, "Corrupt file: Expected Clips" );
			n = Integer.parseInt(pieces[1]);
			m_clipName = new String[n];
			m_clipParams = new String[n];
			for ( int i = 0; i < n; i++ )
				{
				pieces = splitNextLine(stream);
				m_clipName[i] = pieces[0];
				m_clipParams[i] = (pieces.length > 1)? pieces[1] : "";
				}
			stream.close();
			}
		catch (IOException e)
			{
			return logError ( stream, caller, e.getMessage());
			}
		catch (Exception e)
			{
			return logError ( stream, caller, e.getMessage());
			}
		return true;
		}

	private String readLine( BufferedReader stream ) throws Exception
		{
		String text;
		do
			{
			text = stream.readLine();
			if ( text == null )
				throw new EOFException( "Corrupt file" );
			++m_lineNumber;
			text = text.trim();
			}
		while ( text.isEmpty());
		return text;
		}

	private String[] splitNextLine( BufferedReader stream ) throws Exception
		{
		String text = readLine(stream);
		String[] pieces = text.split(":");
		for ( int i = 0; i < pieces.length; i++ )
			pieces[i] = pieces[i].trim();
		return pieces;
		}

	private boolean logError( BufferedReader stream, String caller, String msg )
		{
		System.err.println( "Error: " + caller + " " + msg );
		if ( stream != null )
			{
			msg += "\nLine: " + m_lineNumber;
			try { stream.close(); }
			catch(IOException e) {}
			}
		SBDialog.error( msg, "Error: " + caller );
		return false;
		}

	public String[] getOverlayNames() { return m_olayName; }
	public String[] getOverlayParams(){ return m_olayParams; }
	public String[] getClipNames() { return m_clipName; }
	public String[] getClipParams(){ return m_clipParams; }
	public MASettings[] getMASettings(){ return m_maSettings; }

	public void setClipNames(Object[] clipNames)
		{
		m_clipName = new String[clipNames.length];
m_clipParams = new String[clipNames.length];
		for ( int i = 0; i < clipNames.length; i++ ) {
			m_clipName[i] = clipNames[i].toString();
m_clipParams[i] = "";
			}
		}

	// @TODO: change to setOlays/setClips & change params to colors
	public void setOverlayNames(Object[] olayNames)
		{
		m_olayName = new String[olayNames.length];
m_olayParams = new String[olayNames.length];
		for ( int i = 0; i < olayNames.length; i++ ) {
			m_olayName[i] = olayNames[i].toString();
m_olayParams[i] = "";
			}
		}
	public String toString() { return m_name; }
	}
