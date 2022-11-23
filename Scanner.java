package com.wormtrader.history;
/********************************************************************
* @(#)Scanner.java 1.00 20090719
* Copyright © 2009-2012 by Richard T. Salamone, Jr. All rights reserved.
*
* Scanner is the super class for all stock scanners. A scan is passed a
* tape via its toHit method which decides whether the last bar is a hit.
*
* To write a specific scaner subclass, extend Scanner and place class in:
*
*      package com.wormtrader.custom.scans.
*
* Override the following methods:
*
*   1) toString()
*        - return the user friendly name of this scan
* 
*   2) initialize(Tape tape):
*        - called before any processing for each run. Use this method
*          to register any needed studies on the tape.
*        - Ex: MovingAverage m_ma = (MovingAverage)tape.addStudy ( "SMA", "5,C" );
*        - If no studies are used, then it is not necessary to override this method.
*
*   3) doIt(Tape tape):
*        - called once per symbol per run. In general, examine the last bar
*          in the tape to determine if this symbol is a hit. If so, call
*          add(tape.getSymbol()) to record the hit.
*
* 20090715 rts created
* 20121115 rts repackaged to allow AT and CT to use as well as HM
*******************************************************/
import com.wormtrader.history.Tape;
import com.shanebow.util.SBMisc;
import com.shanebow.util.SBProperties;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class Scanner
	implements Comparable<Scanner>
	{
	public static final List<Scanner> list()
		{
		Vector<Scanner> it = new Vector<Scanner>();
		Scanner scanner;
		String path = SBProperties.get("scans.path");
		for ( String className : SBMisc.jarClasses("_sbcustom.jar", path))
			if ((scanner = (Scanner)SBMisc.newInstance(path, className)) != null )
				it.add(scanner);
		it.trimToSize();
		Collections.sort(it);
		return it;
		}

	public Scanner() {}

	public void initialize( Tape tape ) {}
	public boolean isHit(Tape tape) { return false; }

	@Override public final int compareTo(Scanner aOther)
		{
		return this.toString().compareTo(aOther.toString());
		}
	}