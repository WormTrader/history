package com.wormtrader.history.scan;

import java.util.Enumeration;
import javax.swing.tree.TreeNode;

class ScanHit
//	implements TreeNode
	{
	String m_symbol;
	String m_desc;

	public ScanHit( TreeNode parent, String symbol, String desc )
		{
		m_parent = parent;
		m_symbol = symbol;
		m_desc = desc;
		}

	public String toString() { return m_symbol; }

	// implement TreeNode interface
	private TreeNode m_parent;
	public Enumeration children() {return null;} // TODO: use EMPTY_ENUMERATION
	public boolean getAllowsChildren() { return false; }
	public TreeNode getChildAt(int childIndex) { return null; }
	public int getChildCount() { return 0; }
	public int getIndex(TreeNode child) { return -1; }
	public TreeNode getParent() { return m_parent; }
	public boolean isLeaf() { return true; }
	}
