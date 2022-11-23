package com.wormtrader.history.query;
/********************************************************************
* @(#)QueryBuilder.java 1.00 20120615
* Copyright © 2012 by Richard T. Salamone, Jr. All rights reserved.
*
* QueryBuilder: Creates a Query class based on user inputs.
*
* @author Rick Salamone
* @version 1.00
* 20120914 rts created
*******************************************************/
import com.shanebow.ui.LAF;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class QueryBuilder
	extends JPanel
	{
	private static String AND="\n    && ";

	private static final CodeChoice[] BAR_MIDPOINT =
		{
		new CodeChoice(">",  "M5(#).midpoint() >  M5(#-1).midpoint()"),
		new CodeChoice("<",  "M5(#).midpoint() <  M5(#-1).midpoint()"),
		new CodeChoice("==", "M5(#).midpoint() == M5(#-1).midpoint()"),
		};

	private static final CodeChoice[] BAR_TRAVEL =
		{
		new CodeChoice("T\u2192H",   "candleM5.travel(#) == 34"),
		new CodeChoice("T\u2192T",   "candleM5.travel(#) == 33"),
		new CodeChoice("T\u2192M",   "candleM5.travel(#) == 32"),
		new CodeChoice("T\u2192B",   "candleM5.travel(#) == 31"),
		new CodeChoice("T\u2192L",   "candleM5.travel(#) == 30"),

		new CodeChoice("M\u2192H",   "candleM5.travel(#) == 24"),
		new CodeChoice("M\u2192T",   "candleM5.travel(#) == 23"),
		new CodeChoice("M\u2192M",   "candleM5.travel(#) == 22"),
		new CodeChoice("M\u2192B",   "candleM5.travel(#) == 21"),
		new CodeChoice("M\u2192L",   "candleM5.travel(#) == 20"),

		new CodeChoice("B\u2192H",   "candleM5.travel(#) == 14"),
		new CodeChoice("B\u2192T",   "candleM5.travel(#) == 13"),
		new CodeChoice("B\u2192M",   "candleM5.travel(#) == 12"),
		new CodeChoice("B\u2192B",   "candleM5.travel(#) == 11"),
		new CodeChoice("B\u2192L",   "candleM5.travel(#) == 10"),
		};

	private static final CodeChoice[] CANDLE_SHEIGHT =
		{
		new CodeChoice("\u2191Tall",   "candleM5.height(#) == Candle.UP_TALL"),
		new CodeChoice("\u2191Medium", "candleM5.height(#) == Candle.UP_MEDIUM"),
		new CodeChoice("\u2191Short",  "candleM5.height(#) == Candle.UP_SHORT"),
		new CodeChoice("Doji", "M5(#).isDoji()"),
		new CodeChoice("\u2193Short",  "candleM5.height(#) == Candle.DN_SHORT"),
		new CodeChoice("\u2193Medium", "candleM5.height(#) == Candle.DN_MEDIUM"),
		new CodeChoice("\u2193Tall",   "candleM5.height(#) == Candle.DN_TALL"),
		};

	private static final CodeChoice[] BAR_CHANGE =
		{
		new CodeChoice("Up",   "M5(#).isUp()"),
		new CodeChoice("Down", "M5(#).isDown()"),
		new CodeChoice("Doji", "M5(#).isDoji()"),
		};

	private static final CodeChoice[] MFI_TYPES =
		{
		new CodeChoice("Squat", "gator5.isSquat(#)"),
		new CodeChoice("Green", "gator5.isGreen(#)"),
		new CodeChoice("Fake",  "gator5.isFake(#)"),
		new CodeChoice("Fade",  "gator5.isFade(#)"),
		new CodeChoice("S or G", "(gator5.isSquat(#) || gator5.isGreen(#))"),
		new CodeChoice("F or F", "(gator5.isFake(#) || gator5.isFade(#))"),
		};

	private static final CodeChoice[] GATOR_BIAS =
		{
		new CodeChoice("Flat", "gator5.bias(#) == 0"),
		new CodeChoice("Open Up", "gator5.bias(#) > 0"),
		new CodeChoice("Open Down",  "gator5.bias(#) < 0"),
		};

	static final char UP_ARROW='\u2191';
	static final char DN_ARROW='\u2193';
	private static final CodeChoice[] GATOR5 =
		{
		new CodeChoice("above", "gator5.above(#)"),
		new CodeChoice("below", "gator5.below(#)"),
		new CodeChoice("\u2191\u2191 above", "gator5.upupAbove(#)"),
		new CodeChoice("\u2193\u2193 below", "gator5.dndnBelow(#)"),
		};

	private static final CodeChoice[] GATOR30 =
		{
		new CodeChoice("above", "gator30.above()"),
		new CodeChoice("below", "gator30.below()"),
		new CodeChoice("\u2191\u2191 above", "gator30.upupAbove()"),
		new CodeChoice("\u2193\u2193 below", "gator30.dndnBelow()"),
		};

	private static final String[] _labels =
		{
		"Bar Midpoint",
		"Bar Travel",
		"Candle Height",
		"Bar Change",
		"MFI Type",
		"Gator Bias",
		"M5 Gator",
		"M30 Gator",
		};

	private static final CodeChoice[][] _indicators =
		{
		BAR_MIDPOINT,
		BAR_TRAVEL,
		CANDLE_SHEIGHT,
		BAR_CHANGE,
		MFI_TYPES,
		GATOR_BIAS,
		GATOR5,
		GATOR30,
		};

	private final CodeEditor taCode;
	private final JComboBox[][] cb; // cb[col][row]

	public QueryBuilder(CodeEditor aCodeEditor)
		{
		super(new BorderLayout());
		taCode = aCodeEditor;
		int numBars = 3; // current & back 2
		JPanel cbPanel = new JPanel(new GridLayout(0,numBars +1));

		cb = new JComboBox[numBars][_labels.length];

		// Add the column headings		
		cbPanel.add(new JLabel(""));
		cbPanel.add(new JLabel("Current"));
		for (int c = 1; c < cb.length; c++ )
			cbPanel.add(new JLabel("Back " + c));

		// Add the rows
		for (int r = 0; r < _labels.length; r++)
			{
			cbPanel.add(new JLabel(_labels[r]));
			for (int c = 0; c < cb.length; c++)
				{
				JComboBox box = cb[c][r] = new JComboBox(_indicators[r]);
				box.insertItemAt("--", 0);
				box.setSelectedIndex(0);
				cbPanel.add(box);
				}
			}
		add(cbPanel, BorderLayout.NORTH);
//		add(LAF.titled(new JScrollPane(taCode), "Generated Code"), BorderLayout.CENTER);
		addListener();
		}

	private void addListener()
		{
		ActionListener actionListener = new ActionListener()
			{
			@Override public void actionPerformed(ActionEvent e) { generateCode(); }
			};
		for (JComboBox[] cbRow : cb)
			for (JComboBox box : cbRow)
				box.addActionListener(actionListener);
		}

	private void generateCode()
		{
		StringBuilder code = new StringBuilder();
		for (int c = 1; c < cb.length; c++)
			code.append("int back").append(c).append(" = iM5  - ").append(c).append(";\n");

		code.append("return ");
		for (int c = 0; c < cb.length; c++)
			{
			String indexStr = (c==0)? "iM5" : ("back" + c);
			for (int r = 0; r < _labels.length; r++)
				{
				JComboBox box = cb[c][r];
				try // throws class cast exception if "--" selected
					{
					code.append(((CodeChoice)box.getSelectedItem()).code(indexStr));
					code.append(AND);
					}
				catch (Exception ignore) {}
				}
			}
		code.setLength(code.length()-AND.length()); // chop off the last AND
		code.append(';');
		taCode.setText(code.toString());
		}
	}

final class CodeChoice
	{
	String fText; // user friendly description
	String fCode; // code generated when selected
	int index;

	public CodeChoice(String aText, String aCode)
		{
		fText = aText;
		fCode = aCode;
		}

	@Override public String toString() { return fText; }
	public String code(String indexStr)
		{
		return fCode.replaceAll("\\#", indexStr);
		}
	}
