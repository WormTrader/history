package com.wormtrader.history.query;
/********************************************************************
* @(#)DlgQBuilder.java 1.00 20120806
* Copyright © 2012-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* DlgQBuilder: Allows a user to build a Query interactively.
*
* @author Rick Salamone
* @version 1.00
* 20130405 rts created
*******************************************************/
import com.shanebow.ui.LAF;
import com.shanebow.ui.SBDialog;
import com.shanebow.ui.ToggleOnTop;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.border.*;

public final class DlgQBuilder
	extends JDialog
	{
	private static final String CMD_CANCEL="Cancel";

	private final JButton       fButton = new JButton(CMD_CANCEL);

	public DlgQBuilder(CodeEditor taCode)
		{
		super((Frame)null, LAF.getDialogTitle("Query Builder"), false);

		JPanel content = new JPanel(new BorderLayout());
		content.add(new QueryBuilder(taCode), BorderLayout.CENTER);
//		content.add(controlPanel(),BorderLayout.SOUTH);
		content.setBorder(LAF.bevel(5,5));
		setContentPane(content);

/***********
		setPreferredSize(new Dimension(475,400));
***********/
		pack();
		setLocationByPlatform(true);
		try { setAlwaysOnTop(true); }
		catch (Exception e) {} // guess we can't always be on top!
		LAF.addUISwitchListener(this);
		}

	private JPanel controlPanel()
		{
		JPanel cp = new JPanel(new FlowLayout(FlowLayout.TRAILING));
	/********
		fButton.addActionListener( new ActionListener()
			{
			public void actionPerformed(ActionEvent e)
				{
				JButton src = (JButton)e.getSource();
				String cmd = src.getText();
				if ( cmd.equals(CMD_CANCEL))
					{
					if (fQueryWorker != null)
						fQueryWorker.cancel(true);
					fButton.setVisible(false);
					}
				}
			});
	********/
		cp.add( fButton );
		ToggleOnTop tot = new ToggleOnTop();
		cp.add( tot );
		tot.setSelected(true);
		return cp;
		}
	}
