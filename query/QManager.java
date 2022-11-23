package com.wormtrader.history.query;
/********************************************************************
* @(#)QManager.java 1.00 20120807
* Copyright © 2012 by Richard T. Salamone, Jr. All rights reserved.
*
* QManager: Panel with an SBFilePicker for choosing a query code file
* along with buttons for new, open, save, save as.
* 
* @author Rick Salamone
* @version 1.00
* 20120807 rts created
*******************************************************/
import com.shanebow.ui.SBAction;
import com.shanebow.ui.SBFilePicker;
import com.shanebow.ui.TextFileArea;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class QManager
	extends JPanel
	{
	private final SBFilePicker     fFilePicker;
	private final TextFileArea     fTextEditor;

	private final SBAction fActSave = new SBAction("Save", 'S',
		"Save changes to file", null )
		{
		public void actionPerformed(ActionEvent e) { fileSave(); }
		};

	private final SBAction fActSaveAs = new SBAction("Save As...", 'A',
		"Save changes to a specified file", null )
		{
		public void actionPerformed(ActionEvent e) { fileSaveAs(); }
		};

	public QManager(TextFileArea aTextEditor)
		{
		super ();
		fTextEditor = aTextEditor;
		fFilePicker = new SBFilePicker("Query code",
			"usr.query.dir", "app.query.ext", "usr.query.mru");
		fFilePicker.addActionListener(new ActionListener()
			{
			@Override public void actionPerformed(ActionEvent e) { load(); }
			});
		fFilePicker.setPrototypeDisplayValue("This is a good size");
		add(fFilePicker);
		add(fActSave.makeButton());
		add (fActSaveAs.makeButton());
		fFilePicker.selectMRU();
		}

	public final TextFileArea getTemplate() { return fTextEditor; }

	private void load()
		{
		if ( fFilePicker.isUnnamed())
			{
			fTextEditor.clear();
			fActSave.setEnabled(false);
			}
		else
			{
			fActSave.setEnabled(true);
			if ( fFilePicker.exists())
				fTextEditor.open(fFilePicker.filespec());
			}
		}

	final void fileSave()
		{
		if (fFilePicker.isUnnamed())
			fileSaveAs();
		else
			freeze();
		}

	final void fileSaveAs()
		{
		String name = fFilePicker.promptSaveName(this);
		if ( name != null )
			freeze();
		}

	private void freeze()
		{
		fTextEditor.saveAs(fFilePicker.filespec());
		}
	}
