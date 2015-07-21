package com.me3tweaks.modmanager.modmaker;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

public class TLKFragment {
	private Document owningDocument;
	private NodeList stringsList;
	private int index;
	private String filepath;
	public Document getOwningDocument() {
		return owningDocument;
	}
	public void setOwningDocument(Document owningDocument) {
		this.owningDocument = owningDocument;
	}
	public NodeList getStringsList() {
		return stringsList;
	}
	public void setStringsList(NodeList stringsList) {
		this.stringsList = stringsList;
	}
	public int getIndex() {
		return index;
	}
	public void setIndex(int index) {
		this.index = index;
	}
	public TLKFragment(String filepath, Document owningDocument, NodeList stringsList, int index) {
		super();
		this.filepath = filepath;
		this.owningDocument = owningDocument;
		this.stringsList = stringsList;
		this.index = index;
	}
	public String getFilepath() {
		return filepath;
	}
	public void setFilepath(String filepath) {
		this.filepath = filepath;
	}
	
	
}
