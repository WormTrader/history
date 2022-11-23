package com.wormtrader.history.yahoo;
/********************************************************************
* @(#)ActYahooInfo.java 1.00 2009????
* Copyright © 2012 by Richard T. Salamone, Jr. All rights reserved.
*
* ActYahooInfo: Downloads summary information for a security from
* the Yahoo Finance web site.
*
* @author Rick Salamone
* @version 1.00
* 20121006 rts created
*******************************************************/
import com.shanebow.ui.LAF;
import com.shanebow.ui.SBAction;
import com.shanebow.ui.SBDialog;
import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import javax.swing.JPanel;
import javax.swing.JOptionPane;
import javax.swing.JTextField;

public final class ActYahooInfo
	extends SBAction
	{
	YahooInfoPanel fInfoPanel;
	String fSymbol;

	public ActYahooInfo()
		{
		super("Yahoo Info", 'I', "Request symbol details from Yahoo", "yahoo16");
		}

	public void setSymbol(String aSymbol) { fSymbol = aSymbol; }

	@Override public void actionPerformed(ActionEvent e)
		{
		if (fInfoPanel == null)
			fInfoPanel = new YahooInfoPanel();
//		fInfoPanel.reset(fSymbol);
 		String[] options = { "OK", "Cancel" };
		JTextField tfSymbol = new JTextField(fSymbol);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add( LAF.titled("Symbol", tfSymbol), BorderLayout.NORTH );
		panel.add( LAF.titled("Details", fInfoPanel), BorderLayout.CENTER );

		while ( true )
			{
			fInfoPanel.reset(fSymbol);
			if ( 0 != JOptionPane.showOptionDialog(null, panel,
				LAF.getDialogTitle(toString()), JOptionPane.DEFAULT_OPTION,
					JOptionPane.PLAIN_MESSAGE, null, options, options[0] ))
				return;
			fSymbol = tfSymbol.getText().trim().toUpperCase();
			if ( fSymbol.isEmpty())
				SBDialog.inputError("Symbol Required");
			}
		}
	}
