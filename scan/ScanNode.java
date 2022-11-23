package com.wormtrader.history.scan;
/********************************************************************
* @(#)ScanNode.java 1.00 20090719
* Copyright © 2009-2012 by Richard T. Salamone, Jr. All rights reserved.
*
* ScanNode: A tree node that encapsulates a Scanner and the current hit
* list.
*
* This class maintains a vector of hits in a fashion amenable to display
* by JTree. So each Scanner is a tree node, and each hit is a leaf.
*
* 20090715 rts created
* 20121116 rts modified for decoupling Scanner
*******************************************************/
import com.wormtrader.bars.Bar;
import com.wormtrader.history.Tape;
import com.wormtrader.history.Scanner;
import java.util.Enumeration;
import java.util.Vector;
import javax.swing.tree.TreeNode;

public final class ScanNode
	implements TreeNode, Comparable<ScanNode>
	{
	private static TreeNode _parent;
	final public static void setParent(TreeNode parent) { _parent = parent; }

	private static long _time = 0;
	public  static void setTime(long time) { _time = time; }
	public  static long getTime() { return _time; }

	private final Scanner fScanner;
	private final Vector<ScanHit> m_hits = new Vector<ScanHit>();

	public ScanNode(Scanner aScanner)
		{
		fScanner = aScanner;
		}

	public void initialize( Tape tape ) { fScanner.initialize(tape); }

	public boolean isHit(Tape tape) { return fScanner.isHit(tape); }
	@Override public final int compareTo(ScanNode aOther)
		{
		return fScanner.compareTo(aOther.fScanner);
		}

	@Override public final String toString()
		{
		return fScanner.toString();
		}

	final public boolean add( ScanHit child ) { return m_hits.add(child); }
	final public boolean add( String symbol, String desc )
		{ return add(new ScanHit( this, symbol, desc)); }
	final public void clear() { m_hits.clear(); }
	final public ScanHit get(int hitIndex) { return m_hits.get(hitIndex); }
	final public int numHits() { return m_hits.size(); }

	// implement TreeNode interface
/************
	final public Enumeration children() {return m_hits.elements();}
	final public boolean getAllowsChildren() { return true; }
	final public TreeNode getChildAt(int childIndex) { return m_hits.get(childIndex); }
	final public int getChildCount() { return m_hits.size(); }
	final public int getIndex(TreeNode child) { return m_hits.indexOf(child); }
	final public TreeNode getParent() { return _parent; }
	final public boolean isLeaf() { return false; }
*********/
	final public Enumeration children() {return null;}
	final public boolean getAllowsChildren() { return false; }
	final public TreeNode getChildAt(int childIndex) { return (TreeNode)null; }
	final public int getChildCount() { return 0; }
	final public int getIndex(TreeNode child) { return -1; }
	final public TreeNode getParent() { return _parent; }
	final public boolean isLeaf() { return false; }
	}