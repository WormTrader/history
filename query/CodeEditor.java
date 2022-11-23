package com.wormtrader.history.query;
/********************************************************************
* @(#)CodeEditor.java 1.00 20120507
* Copyright © 2012-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* TextFileArea: Extends JTextArea to allow editing text file(s)
*
* @author Rick Salamone
* @version 1.00
* 20120507 rts feasibility - compiles & runs importing SBDialog using Janino!
*******************************************************/
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.TextFileArea;
import java.awt.*;
import java.lang.reflect.*;
import java.io.*;
import org.codehaus.janino.*;
// import org.codehaus.janino.util.*;

import javax.swing.JTextArea;

public class CodeEditor
	extends TextFileArea
	{
	public CodeEditor()
		{
		super();
		setTabSize(3);
		}

	public Object compile()
		{
		System.out.println("Compiling...");
		try
			{
			return _compile(getText());
			}
		catch (Exception e)
			{
			SBDialog.error("Janio", e.toString());
			return null;
			}
		}

	private static final String CLASS_NAME = "Foo";
	private StringBuffer fProgram;

	private Object _compile(String aProgram)
		throws Exception
		{
		fProgram = new StringBuffer()
		.append("import static com.shanebow.util.SBDate.SUN;")
		.append("import static com.shanebow.util.SBDate.MON;")
		.append("import static com.shanebow.util.SBDate.TUE;")
		.append("import static com.shanebow.util.SBDate.WED;")
		.append("import static com.shanebow.util.SBDate.THR;")
		.append("import static com.shanebow.util.SBDate.FRI;")
		.append("import static com.shanebow.util.SBDate.SAT;")
		.append("import static com.wormtrader.bars.Bar.SQUAT;")
		.append("import com.shanebow.ui.SBDialog;")
		.append("import com.shanebow.util.SBDate;")
		.append("import com.wormtrader.bars.Bar;")
		.append("import com.wormtrader.history.Tape;")
		.append("import com.wormtrader.history.indicators.*;")
		.append("public class ")
		.append(CLASS_NAME)
		.append(" extends com.wormtrader.history.query.Query")
		.append(" {")
		.append("public boolean isHit(Bar bM5, int iM5){")
		.append(aProgram)
		.append("}}");

		SimpleCompiler compiler = new SimpleCompiler();
		compiler.cook( new StringReader(fProgram.toString()) );

		// get the loaded class
		Class<?> cl = compiler.getClassLoader().loadClass(CLASS_NAME);

return cl.newInstance();
/************
		// Invoke the "public static main(String[])" method
//		Method meth = cl.getMethod("main", new Class[] { String[].class });
//		String[] methArgs = new String[] { aProgram };  // contains one input argument
//		meth.invoke(null, new Object[] { methArgs });
		Method meth = cl.getMethod("someMethod", new Class[] { String.class });
		meth.invoke(null, new Object[] { "an arg to someMethod" });
************/
		}
	}
