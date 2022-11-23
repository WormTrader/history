package com.wormtrader.history.ui.graph;
/********************************************************************
* @(#)Annotations.java 1.00 20120627
* Copyright © 2011-2014 by Richard T. Salamone, Jr. All rights reserved.
*
* Annotations: Handles saving and restoring a graph's Annotation objects.
* There are two public static methods save() and restore() which take a
* graph and a tape as parameters.
*
* Programmer Notes:
* The main issue is that the graph (which the user annotates) consists
* of a subset of the bars for a symbol, defined by the graph's date,
* bar size, and backfill. Furthermore, the graph accesses the domain (bars)
* data using indices. Therefore, we need to save and restore annotations
* by refering to the actual time of the bars where each annotation begins
* and ends, then translate these to indices for the graph. Furthermore we
* must filter out any annotations not visible with the current settings.
*
* @author Rick Salamone
* @version 1.00
* 20120627 rts created
* 20130218 rts checks for remote user dir and if so skips with stdout message
* 20140209 rts storing under usr.dir still no work network
*******************************************************/
import com.wormtrader.bars.BarList;
import com.wormtrader.history.Tape;
import com.shanebow.ui.draw.Annotation;
import com.shanebow.ui.draw.Coordinates;
import com.shanebow.ui.graph.TGGraph;
import com.shanebow.util.SBLog;
import com.shanebow.util.SBProperties;
import java.awt.Color;
import java.io.*;
import java.util.Vector;

public final class Annotations
	{
	public static void save(TGGraph aGraph, Tape aTape)
		{
if (SBProperties.get("usr.dir").startsWith("http:"))
	{
	System.out.println("ONLINE VERSION CANNOT SAVE/RESTORE ANNOTATIONS");
	return;
	}
		String symbol = aTape.getSymbol();
		if (symbol.isEmpty()) return;
		File file = file(symbol, aTape.getBarSize().paramValue());
		BarList barList = aTape.getBars();
		Vector<String> offGraph = linesOutOfRange(file, barList);
		if ((offGraph == null) && !aGraph.isAnnotated())
			{
			file.delete();
			return;
			}
		PrintWriter pw = null;
		try
			{
			pw = new PrintWriter (file);
			if (offGraph != null)
				for (String line : offGraph)
					pw.println(line);

			if (aGraph.isAnnotated())
				freeze(pw, aGraph, barList);
			}
		catch (Exception e) { SBLog.error("Save " + file, e.getMessage()); }
		finally { if (pw != null) pw.close(); }
		}

	public static final void restore(TGGraph aGraph, Tape aTape)
		{
if (SBProperties.get("usr.dir").startsWith("http:"))
	{
	System.out.println("ONLINE VERSION CANNOT SAVE/RESTORE ANNOTATIONS");
	return;
	}
		String symbol = aTape.getSymbol();
		if (symbol.isEmpty()) return;
		File file = file(symbol, aTape.getBarSize().paramValue());
		if (!file.exists())
			return;
		BufferedReader stream = null;
		try
			{
			stream = new BufferedReader(new FileReader(file));
			thaw(stream, aGraph, aTape.getBars());
			}
		catch (Exception e) { SBLog.error("Restore " + file, e.getMessage()); }
		finally { try { stream.close(); } catch (Exception e) {}}
		}

	// PRIVATE
	/** File extension for annotation files */
	private static final String EXT = ".txt";

	/** Field separator within annotation records */
	private static final String SEPARATOR = ",";

	/**
	* Fields comprising the csv record for each annotation
	*/
	private static final byte TYPE = 0;   // line, fibo, etc
	private static final byte CLIP = 1;   // clip on graph - always 0 for now
	private static final byte TIME0 = 2;  // time of start of annotation
	private static final byte VALUE0 = 3; // value of start of annotation
	private static final byte TIME1 = 4;  // time of end of annotation
	private static final byte VALUE1 = 5; // value of start of annotation
	private static final byte COLOR = 6;

	private static File file(String aSymbol, int aBarSizeParam)
		{
		String path = SBProperties.get("usr.dir") + "/annotations/";
		return new File(path + aSymbol + aBarSizeParam + EXT);
		}

	private static void freeze(PrintWriter pw, TGGraph aGraph, BarList aBarList)
		throws IOException
		{
byte clip = (byte)0; // @TODO: indicator annotations unreliable
		for ( Annotation ann : aGraph.getAnnotations())
			{
			Coordinates start = ann.getStart();
			Coordinates end = ann.getEnd();
			long time0 = aBarList.get(start.getXApp()).getTime();
			long time1 = aBarList.get(end.getXApp()).getTime();
			String line = "" + ann.getType() + SEPARATOR + clip
			             + SEPARATOR + time0 + SEPARATOR + start.getYApp()
			             + SEPARATOR + time1 + SEPARATOR + end.getYApp()
			             + SEPARATOR + ann.getColor().getRGB();
			pw.println(line);
			}
		}

	/**
	* Reads the lines and if they are in the date range of aBarList,
	* adds them to aGraph.
	*/
	private static void thaw(BufferedReader stream, TGGraph aGraph, BarList aBarList)
		throws Exception
		{
		String csv;
		long minTime = aBarList.firstTime();
		long maxTime = aBarList.lastTime();
		while ((csv = stream.readLine()) != null )
			{
			csv = csv.trim();
			if ( csv.isEmpty())
				continue;
			String[] split = csv.split(SEPARATOR);

			long t0 = Long.parseLong(split[TIME0].trim());
			if ( !inRange(t0, minTime, maxTime)) continue;
			long t1 = Long.parseLong(split[TIME1].trim());
			if ( !inRange(t1, minTime, maxTime)) continue;

			aGraph.addAnnotation(parseInt(split, TYPE),
			                     new Color(parseInt(split, COLOR)),
			                     (byte)parseInt(split, CLIP),
			                     aBarList.binarySearch(t0),
			                     parseInt(split, VALUE0),
			                     aBarList.binarySearch(t1),
			                     parseInt(split, VALUE1));
			}
		}

	/**
	* Returns a list of all lines in the annotation file that are outside
	* the range of the bars contained in the graph. In other words these
	* are the annotations that aren't displayed on the graph.
	*/
	private static Vector<String> linesOutOfRange(File file, BarList aBarList)
		{
		if (!file.exists())
			return null;
		Vector<String> it = new Vector<String>();
		String csv;
		long minTime = aBarList.firstTime();
		long maxTime = aBarList.lastTime();
		BufferedReader stream = null;
		try
			{
			stream = new BufferedReader(new FileReader(file));
			while ((csv = stream.readLine()) != null )
				{
				csv = csv.trim();
				if ( csv.isEmpty())
					continue;
				String[] split = csv.split(SEPARATOR);

				if ( !inRange(Long.parseLong(split[TIME0].trim()), minTime, maxTime)
				||   !inRange(Long.parseLong(split[TIME1].trim()), minTime, maxTime))
					it.add(csv);
				}
			}
		catch (Exception e) { SBLog.error("Get OOR " + file, e.getMessage()); }
		finally { try { stream.close(); } catch (Exception e) {}}
		return it.isEmpty()? null : it;
		}

	private static final boolean inRange(long value, long min, long max)
		{
		return (value >= min) && (value <= max);
		}

	private static int parseInt(String[] split, int aIndex)
		{
		return Integer.parseInt(split[aIndex].trim());
		}
	}
