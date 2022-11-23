package com.wormtrader.history.ui.graph;
/********************************************************************
* @(#)MovingAvgEditor.java 1.00 20080518
* Copyright © 2007 - 2012 by Richard T. Salamone, Jr. All rights reserved.
*
* MovingAvgEditor: Panel to edit the values in a MASettings object.
*
* @author Rick Salamone
* @version 1.00
* 20080518 rts created
* 20120719 rts added field to edit offset
*******************************************************/
import java.awt.Color;
import javax.swing.*;

import com.wormtrader.bars.Bar;
import com.shanebow.ui.ColorButton;

class MovingAvgEditor
	extends JPanel
	{
	private MASettings m_indicator;
	private JComboBox cbAveType = new JComboBox(MASettings.AVG_METHODS);
	private JTextField tfPeriods = new JTextField(3);
	private JTextField tfOffset = new JTextField(3);
	private JComboBox cbWhichPrice = new JComboBox(Bar.PRICE_FIELDS);
	private JCheckBox chkActive = new JCheckBox();
	private ColorButton btnColor = new ColorButton();

	public MovingAvgEditor( MASettings indicator )
		{
		this();
		reset(indicator);
		}

	public MovingAvgEditor()
		{
		add( chkActive );
		add( btnColor );
		add( tfPeriods ); add(new JLabel("period"));
		add( cbAveType );
		add(new JLabel("of")); add( cbWhichPrice );
		add(new JLabel("offset")); add( tfOffset );
		}

	public void reset( MASettings indicator )
		{
		m_indicator = indicator;
		chkActive.setSelected( indicator.isActive());
		tfPeriods.setText("" + indicator.getPeriod());
		tfPeriods.setHorizontalAlignment(JTextField.RIGHT);
		tfOffset.setText("" + indicator.getOffset());
		tfOffset.setHorizontalAlignment(JTextField.RIGHT);
		char priceField = indicator.getWhichPrice();
		for ( int i = 0; i < Bar.PRICE_FIELDS.length; i++ )
			if ( Bar.PRICE_FIELDS[i].charAt(0) == priceField )
				{
				cbWhichPrice.setSelectedIndex(i);
				break;
				}
		cbAveType.setSelectedIndex(indicator.getAvgType());
		btnColor.setBackground(indicator.getColor());
		}

	public void applyTo(MASettings aMAsettings)
		{
		int value;
		aMAsettings.setActive(chkActive.isSelected());
		try
			{
			value = Integer.parseInt(tfPeriods.getText());
			aMAsettings.setPeriod(value);
			value = Integer.parseInt(tfOffset.getText());
			aMAsettings.setOffset(value);
			}
		catch( Exception e)
			{
			System.err.println( "Inconsistent input exception: " + e );
			return;
			}
		char priceField = ((String)cbWhichPrice.getSelectedItem()).charAt(0);
		aMAsettings.setWhichPrice(priceField);
		value = cbAveType.getSelectedIndex();
		aMAsettings.setAvgType(value);
		aMAsettings.setColor( btnColor.getBackground());
		}

	public void grabInput()
		{
		applyTo(m_indicator);
/*******
		int value;
		m_indicator.setActive(chkActive.isSelected());
		try
			{
			value = Integer.parseInt(tfPeriods.getText());
			m_indicator.setPeriod(value);
			value = Integer.parseInt(tfOffset.getText());
			m_indicator.setOffset(value);
			}
		catch( Exception e)
			{
			System.err.println( "Inconsistent input exception: " + e );
			return;
			}
		char priceField = ((String)cbWhichPrice.getSelectedItem()).charAt(0);
		m_indicator.setWhichPrice(priceField);
		value = cbAveType.getSelectedIndex();
		m_indicator.setAvgType(value);
		m_indicator.setColor( btnColor.getBackground());
*******/
		}
	}
