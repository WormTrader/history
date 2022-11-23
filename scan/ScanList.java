package com.wormtrader.history.scan;
/********************************************************************
* @(#)ScanList.java 1.00 20090715
* Copyright © 2009-2012 by Richard T. Salamone, Jr. All rights reserved.
*
* ScanList: Instantiates and maintains the list of Scanner
* subclasses. The Vector of Scanner is wrapped in a TreeNode
* so that that it can serve as the root of the JTree which
* displays the results of running the scans.
*
* @version 1.0, 20090715 rts created
* @version 2.0, 20110424 rts now loads scans from _sbcustom.jar
*******************************************************/
import com.wormtrader.history.Scanner;
import com.shanebow.util.SBMisc;
import com.shanebow.util.SBProperties;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.tree.TreeNode;

public final class ScanList
	extends Vector<ScanNode>
	implements TreeNode
	{
	static ScanList _singleton = new ScanList();

	public static ScanList getInstance() { return _singleton; }
	private ScanList()
		{
		super();

		ScanNode.setParent( this );
		for (Scanner scanner : Scanner.list())
			add(new ScanNode(scanner));
		trimToSize();
		}

	public void clearHits()
		{
		for ( ScanNode node : this )
			node.clear();
		}

	public Enumeration children() {return elements();}
	public boolean getAllowsChildren() { return true; }
	public TreeNode getChildAt(int childIndex) { return get(childIndex); }
	public int getChildCount() { return size(); }
	public int getIndex(TreeNode child) { return indexOf(child); }
	public TreeNode getParent() { return (TreeNode)null; }
	public boolean isLeaf() { return false; }
	public String toString() { return "Scans"; }
	}
