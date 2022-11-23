package com.wormtrader.history.ui.graph;
/********************************************************************
* DlgCandleGraphConfg.java 1.00 20080518
* Copyright © 2007 - 2014 by Richard T. Salamone, Jr. All rights reserved.
*
* DlgCandleGraphConfg:
*
* @author Rick Salamone
* @version 2.00
* 20080518 rts created
* 20120727 rts major update to use SBFilePicker
* 20140612 rts added constant MAX_OLAYS & increased it to 8
*******************************************************/
import com.wormtrader.history.Tape;
import com.wormtrader.history.TapeStudies;
import com.shanebow.ui.ColorButton;
import com.shanebow.ui.LAF;
import com.shanebow.ui.SublistChooser;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import com.shanebow.tools.sortedvector.*;

public class DlgCandleGraphConfg
	extends JDialog
	{
	public static int MAX_OLAYS = 8;
	private SublistChooser    m_clipSelector;
	private SublistChooser    m_olaySelector;
	private MovingAvgEditor[] m_maEditor;
	private final JPanel fControls = new JPanel();
	private boolean fPopulating;

	static final SortedVector<StudyConfig> _studyConfigs = new SortedVector<StudyConfig>();
	static {
		_studyConfigs.add(new StudyConfig("River", "River",
			"Period,int,20^Band 1 Sigmas,F2,1.50^Band 2 Sigmas,F2,2.00"));
		}

	DlgCandleGraphConfg()
		{
		JPanel main = new JPanel();
		main.setLayout( new BoxLayout( main, BoxLayout.Y_AXIS ));
		main.add( movingAvgPanel());

		m_olaySelector = chooser(TapeStudies.OVERLAYS, MAX_OLAYS);
		m_olaySelector.setCellRenderers(null, new IndicatorCellRenderer());
		main.add(LAF.titled(GraphTemplate.OLAYS_SECTION_TITLE, m_olaySelector));

		main.add( clipPanel());
		main.add(fControls);
		setContentPane(main);
		Dimension size = m_maEditor[0].getPreferredSize();
		fControls.setMaximumSize(size);
		fControls.setPreferredSize(size);
		main.setBorder(LAF.getStandardBorder());
		LAF.addUISwitchListener(this);
		pack();
		}

	public void show( GraphTemplate aTemplate, String desc, JComponent aFilePicker )
		{
		setTitle("Configure Graph " + desc );
		fControls.removeAll();
		fControls.add(aFilePicker);
		populateFrom(aTemplate);
		setVisible( true );
		}

	private JPanel movingAvgPanel()
		{
		JPanel panel = new JPanel(new GridLayout( 0, 1 ));
		panel.setBorder(
			BorderFactory.createTitledBorder( GraphTemplate.MA_SECTION_TITLE ));
		m_maEditor = new MovingAvgEditor[GraphTemplate.MAX_AVERAGES];
		for ( int i = 0; i < m_maEditor.length; i++ )
			panel.add( m_maEditor[i] = new MovingAvgEditor());
		return panel;
		}

	private SublistChooser chooser(Object[] options, int maxAllowed)
		{
		SublistChooser it = new SublistChooser( options, maxAllowed ) {
			@Override public Object preChoose(Object item) { return configStudy(item); }
			@Override public Object removed(Object item) {
				return ((String)item).split("\\(")[0]; // strip off any params
				}
			};
		return it;
		}

	private Object configStudy(Object item) {
		if (fPopulating) return item;
		String studyName = (String)item;
		StudyConfig cfg = _studyConfigs.get(studyName);
		if (cfg == null) return item; // not configurable
		DynaForm form = new DynaForm(cfg.getFormDef());
 		String[] opts = { "OK", "Cancel" };
		if ( 0 != JOptionPane.showOptionDialog(this, form, "Configure " + studyName,
			 JOptionPane.DEFAULT_OPTION,	JOptionPane.PLAIN_MESSAGE, null, opts, opts[0] ))
			return item;
		return studyName + "(" + form.grab() + ")";
		}

	private JPanel clipPanel()
		{
		m_clipSelector = new SublistChooser( TapeStudies.CLIP_INDICATORS, 4 );
		m_clipSelector.setBorder(
			BorderFactory.createTitledBorder( GraphTemplate.CLIPS_SECTION_TITLE ));
		return m_clipSelector;
		}

	public void populateFrom(GraphTemplate aTemplate)
		{
		fPopulating = true;
		MASettings[] maSettings = aTemplate.getMASettings();
		for ( int i = 0; i < m_maEditor.length; i++ )
			m_maEditor[i].reset(maSettings[i]);

		m_clipSelector.removeAll();
		m_clipSelector.chooseItems( aTemplate.getClipNames());

		m_olaySelector.removeAll();
		m_olaySelector.chooseItems( aTemplate.getOverlayNames());
		fPopulating = false;
		}

	public void applyInputsTo(GraphTemplate aTemplate)
		{
		aTemplate.setClipNames( m_clipSelector.getChosen());
		aTemplate.setOverlayNames( m_olaySelector.getChosen());
		MASettings[] maSettings = aTemplate.getMASettings();
		for ( int i = 0; i < m_maEditor.length; i++ )
			m_maEditor[i].applyTo(maSettings[i]);
		}
	}

class DynaForm
	extends JPanel
	{
	static Dimension ENTRY_FIELD_SIZE;

	DynaForm(String aFormDef)
		{
		super();
		String fFormDef = aFormDef;
		if (ENTRY_FIELD_SIZE == null)
			ENTRY_FIELD_SIZE = new JComboBox(com.wormtrader.bars.Bar.PRICE_FIELDS).getPreferredSize();
		setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
		String[] pieces = fFormDef.split("\\^");
	// if (pieces.length == 1) 
		int level = 0; // panel nesting level
		for (String piece : pieces)
			if ((piece = piece.trim()).startsWith("+"))
				add(new JLabel(piece.substring(1)));
			else if (piece.startsWith("}"))
				add(new JLabel("}"));
			else add(subPanel(piece));
		}

	private JPanel subPanel(String csv)
		{
		JPanel it = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		it.setBorder(LAF.bevel(5,10));
		String[] pieces = csv.split(",");
		it.add(new JLabel(pieces[0]));
		if (pieces[1].equalsIgnoreCase("int"))
			it.add(new JTextField(pieces[2]) {
				public Dimension getPreferredSize() { return ENTRY_FIELD_SIZE; }
				});
		else if (pieces[1].equalsIgnoreCase("OHLC"))
			it.add(new JComboBox(com.wormtrader.bars.Bar.PRICE_FIELDS));
		else if (pieces[1].equalsIgnoreCase("F2"))
			it.add(new JTextField(pieces[2]) {
				public Dimension getPreferredSize() { return ENTRY_FIELD_SIZE; }
				});
		it.add(new ColorButton());

		return it;
		}

	public String grab() { return walk(this,"").substring(1); }

	/**
	* Recursively walks the component tree starting at the specified root,
	* and calling setFontFace() on each ThaiContent component encountered.
	*/
	private String walk(Component aComponent, String it)
		{
		if (aComponent instanceof JComboBox)
			return it += "," + ((JComboBox)aComponent).getSelectedItem();
		if (aComponent instanceof JTextField)
			return it += "," + ((JTextField)aComponent).getText().trim();
		else if (aComponent instanceof Container)
			for ( Component child : ((Container)aComponent).getComponents())
				it = walk(child,it);
		return it;
		}
	}

class StudyConfig
	implements HasSortKey
	{
	String fName;
	String fClassName;
	String fFormDef;

	StudyConfig(String aName, String aClassName, String aFormDef) {
		fName = aName;
		fClassName = aClassName;
		fFormDef = aFormDef;
		}

	@Override public String getSortKey() { return fName; }
	String getFormDef() { return fFormDef; }
	}
