package com.wormtrader.history.ui;
/********************************************************************
* @(#)MiniChartsView.java 1.00 20090722
* Copyright © 2012 by Richard T. Salamone, Jr. All rights reserved.
*
* MiniChartsView: A grid of small charts that gives a quick look at
* various symbols and time frames.
*
* @author Rick Salamone
* @version 1.00
* 20120731 rts created
* 20120925 rts added double click to change grid size
*******************************************************/
import com.wormtrader.bars.BarSize;
import com.wormtrader.dao.SBExecution;
import com.wormtrader.history.ui.graph.*;
import com.wormtrader.history.Tape;
import com.wormtrader.history.TripleTape;
import com.wormtrader.positions.*;
import com.shanebow.ui.LAF;
import com.shanebow.ui.SBRadioPanel;
import com.shanebow.ui.graph.SBGraph;
import com.shanebow.ui.graph.GraphPopupAction;
import com.shanebow.util.SBLog;
import com.shanebow.util.SBProperties;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public class MiniChartsView
	extends JPanel
	{
	private MiniChartModel[] fModels;
	private final String fKeyPrefix; // for save/restore properties
	private final TemplateManager fTemplateManager;
	private int fCols;

	private final GraphPopupAction fActSymbol = new GraphPopupAction(
		"Symbol", 'Y', "Set Symbol and Time Frame")
		{
		@Override public void actionPerformed(ActionEvent e)
			{
			promptTape((MiniChartModel)getGraphFromPopupEvent(e).getModel());
			}
		};

	private final MouseAdapter fMouseListener = new MouseAdapter()
		{
		@Override public void mouseClicked(MouseEvent e)
			{
if (e.getClickCount() > 1 && fKeyPrefix.startsWith("usr.")) {promptGrid(); return;}
/*
if (e.getClickCount() < 2)
((SBGraph)e.getSource()).viewController().toggleYAxis();
			if (e.getClickCount() < 2) return;
*/
			String symbol = ((MiniChartModel)((SBGraph)e.getSource()).viewController()
			              .getModel()).getTape().getSymbol();
			if (symbol != null && !symbol.isEmpty())
				LegsList.setSelected(LegsList.find(symbol,""));
			}
		};

	public MiniChartsView(String aPropKeyPrefix)
		{
		super(null);
		fKeyPrefix = aPropKeyPrefix;
		String mru_key = fKeyPrefix + "gt.";
		TemplateListener templateListener = new TemplateListener()
			{
			@Override public void applySettings(GraphTemplate aTemplate)
				{
				if (fModels == null) return;
				for (MiniChartModel model : fModels)
					model.applySettings(aTemplate);
				}
			};
		fTemplateManager = new TemplateManager(templateListener, mru_key);
		createCharts();
		setPreferredSize(new Dimension(500,600));
		setBorder(new CompoundBorder(BorderFactory.createEmptyBorder(10,10,10,10),
		                             new BevelBorder(BevelBorder.LOWERED)));
		}

	private void createCharts()
		{
		for (java.awt.Component c : getComponents())
		if (c instanceof SBGraph)
			((SBGraph)c).removeMouseListener(fMouseListener);
		removeAll();

		SBProperties props = SBProperties.getInstance();
		boolean showY = props.getBoolean(fKeyPrefix+"showY", false);
		int rows = props.getInt(fKeyPrefix+"rows", 3);
		fCols = props.getInt(fKeyPrefix+"cols", 2);
		setLayout(new GridLayout(rows,fCols));
		int numCharts = rows * fCols;
		fModels = expand(fModels, numCharts);
		boolean usrModifiable = fKeyPrefix.startsWith("usr.");
		for (int i = 0; i < numCharts; i++ )
			{
			SBGraph chart = new SBGraph(fModels[i], "", false, false);
			if (usrModifiable)
				chart.addActions(fActSymbol);
			chart.viewController().showYAxis(showY);
			chart.viewController().setShowGrid(false);
			add(chart);
			chart.addMouseListener(fMouseListener);
			}
		validate();
		}

	private MiniChartModel[] expand( MiniChartModel[] copyFrom, int newSize )
		{
		int oldSize;
		if (copyFrom == null)
			oldSize = 0;
		else if ( newSize <= (oldSize = copyFrom.length))
			return copyFrom;
		MiniChartModel[] copyTo = new MiniChartModel[newSize];
		if (oldSize > 0) System.arraycopy(copyFrom, 0, copyTo, 0, oldSize);
		Tape dummyTape = new Tape(com.wormtrader.bars.BarSize.FIVE_MIN);
		for (int i = oldSize; i < newSize; i++ )
			copyTo[i] = new MiniChartModel(dummyTape, fTemplateManager);
		return copyTo;
		}

	/**
	* Load the charts up with the symbols/time frames stored in the properties
	* Note this must be called after the LegsList is populated
	*/ 
	public void restoreTapes()
		{
		String key = fKeyPrefix + "tapes";
		String tapesProp = SBProperties.get(key);
		System.out.println("Mini restore key: " + tapesProp);
		if (tapesProp != null)
			restoreTapes(tapesProp);
		}

	private void restoreTapes(String aSymSizePairs)
		{
		System.out.println("RESTORE  " + aSymSizePairs);
		String[] pieces = aSymSizePairs.split(",");
		int numTapes = pieces.length / 2;
		fModels = expand(fModels, numTapes);
		for (int i=0, j=0; j < pieces.length; j += 2)
			{
			try
				{
				if (pieces[j].isEmpty()) continue;
				PositionLeg leg = LegsList.find(pieces[j], "");
				BarSize barSize = BarSize.find(Integer.parseInt(pieces[j+1]));
				fModels[i].setTape(leg.getTape(barSize));
				++i;
				}
			catch(Exception e) { SBLog.write("MiniCharts unable to load: "+ pieces[j] + ": " + e); }
			}
		saveTapes();
		}

	/**
	* Write the current symbols/time frames out to the properties
	*/ 
	private void saveTapes()
		{
		StringBuffer prop = new StringBuffer();
		for (MiniChartModel model : fModels )
			{
			Tape tape = model.getTape();
			String symbol = tape.getSymbol();
			int barSize = tape.getBarSize().paramValue();
			prop.append(symbol).append(',').append(barSize).append(',');
			}
		prop.deleteCharAt(prop.length()-1);
		SBProperties.set(fKeyPrefix+"tapes", prop.toString());
		}

	public void dump()
		{
		System.out.println("MiniChartModels:");
		for (MiniChartModel model : fModels )
			System.out.println("  " + model.toString());
		}

	public void setTape(int row, int col, Tape tape )
		{
		int index = row * fCols + col;
		fModels[index].setTape(tape);
		}

/*******
	public void setExecs( Iterable<SBExecution> execs )
		{
		fModels.setExecs( execs );
		}
*******/

	private void promptTape(MiniChartModel aModel)
		{
		Tape tape = aModel.getTape();
 		String[] options = { "OK", "Cancel" };
		JLabel label = new JLabel("<html>Enter a symobl and time frame for this chart");
		JPanel panel = new JPanel(new GridLayout(0,1));
		JTextField tfSymbol = new JTextField(tape.getSymbol());
		SBRadioPanel<BarSize> rbBarSizes = new SBRadioPanel<BarSize>(1, 0, TripleTape.BAR_SIZES );
		rbBarSizes.select(tape.getBarSize());
		panel.add(label);
		panel.add(LAF.titled("Symbol", tfSymbol));
		panel.add(LAF.titled("Time Frame", rbBarSizes));
		while ( true ) // until valid response or bail
			{
			if ( 0 != JOptionPane.showOptionDialog(aModel.getGraph(), panel,
				LAF.getDialogTitle("Set Mini Chart"), JOptionPane.DEFAULT_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0] ))
				return;

			String symbol = tfSymbol.getText().trim().toUpperCase();
			BarSize barSize = rbBarSizes.getSelected();
			PositionLeg leg = LegsList.find(symbol, "");
			if (leg != null)
				{
				aModel.setTape(leg.getTape(barSize));
				saveTapes();
				return;
				}
			if (symbol.isEmpty())
				label.setText( "<html><font color=RED>Symbol required");
			else
				label.setText( "<html><font color=RED>Symbol " + symbol + " not in system");
			}
		}

	private void promptGrid()
		{
		SBProperties props = SBProperties.getInstance();
		boolean showY = props.getBoolean(fKeyPrefix+"showY", false);
		int rows = props.getInt(fKeyPrefix+"rows", 3);
		int cols = props.getInt(fKeyPrefix+"cols", 2);
		JTextField tfMiniRows = new JTextField("" + rows);
		JTextField tfMiniCols = new JTextField("" + cols);
		JLabel label = new JLabel("<html>Enter the grid dimensions");
 		String[] options = { "OK", "Cancel" };
		JPanel panel = new JPanel(new GridLayout(0,1));
		panel.add(label);
		panel.add(LAF.titled("Rows", tfMiniRows));
		panel.add(LAF.titled("Cols", tfMiniCols));
		while ( true ) // until valid response or bail
			{
			if ( 0 != JOptionPane.showOptionDialog(this, panel,
				LAF.getDialogTitle("Set Mini Chart"), JOptionPane.DEFAULT_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0] ))
				return;

			try
				{
				rows = Integer.parseInt(tfMiniRows.getText().trim());
				cols = Integer.parseInt(tfMiniCols.getText().trim());
				if (rows < 0 || cols < 0)
					throw new Exception();
				props.setProperty(fKeyPrefix+"rows", ""+rows);
				props.setProperty(fKeyPrefix+"cols", ""+cols);
				createCharts();
				restoreTapes();
				return;
				}
			catch (Exception e) { label.setText( "<html><font color=RED>Positive values required"); }
			}
		}
	}
