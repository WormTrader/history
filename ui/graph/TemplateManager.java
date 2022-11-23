package com.wormtrader.history.ui.graph;
/********************************************************************
* @(#)TemplateManager.java 1.00 20120727
* Copyright © 2012-2013 by Richard T. Salamone, Jr. All rights reserved.
*
* TemplateManager: Panel with an SBFilePicker for choosing the graph
* template along with buttons for new, open, save, save as.
* 
* @author Rick Salamone
* @version 1.00
* 20120727 rts created
* 20130217 rts uses WebFilePicker if usr.dir is starts with 'http:'
*******************************************************/
import com.wormtrader.bars.BarSize;
import com.shanebow.ui.SBAction;
import com.shanebow.ui.SBFilePicker;
import com.shanebow.util.SBProperties;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public final class TemplateManager
	extends JPanel
	{
	private static DlgCandleGraphConfg _dlgConfig;
	private final TemplateListener fListener;
	private final SBFilePicker     fFilePicker;
	private final GraphTemplate    fTemplate = new GraphTemplate();

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

	public TemplateManager(TemplateListener aListener, String mru_key)
		{
		super ();
		fListener = aListener;
		fFilePicker = (SBProperties.getInstance().getProperty("usr.dir", "").startsWith("http:"))?
			new WebFilePicker("Chart configuration", "usr.dir", "app.gt", mru_key)
		 :	 new SBFilePicker("Chart configuration", "usr.dir", "app.gt", mru_key);
		fFilePicker.addActionListener(new ActionListener()
			{
			@Override public void actionPerformed(ActionEvent e) { load(); }
			});
		add(fFilePicker);
		add(fActSave.makeButton());
		add(fActSaveAs.makeButton());
		fFilePicker.selectMRU();
		}

	public final GraphTemplate getTemplate() { return fTemplate; }

	final void launch(String aTitle)
		{
		if ( _dlgConfig == null )
			_dlgConfig = new DlgCandleGraphConfg();
		_dlgConfig.show(fTemplate, aTitle, this);
		}

	private void load()
		{
		if ( fFilePicker.isUnnamed())
			{
			fTemplate.clear();
			fActSave.setEnabled(false);
			}
		else
			{
			fActSave.setEnabled(true);
			fTemplate.thaw(fFilePicker.filespec());
			}
		if ( _dlgConfig != null )
			_dlgConfig.populateFrom(fTemplate);
		fListener.applySettings(fTemplate);
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
		_dlgConfig.applyInputsTo(fTemplate);
		fTemplate.freeze(fFilePicker.filespec());
		fListener.applySettings(fTemplate);
		}
	}
