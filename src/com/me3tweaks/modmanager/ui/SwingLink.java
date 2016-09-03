package com.me3tweaks.modmanager.ui;

import java.awt.Cursor;
import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;

import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

public class SwingLink extends JLabel {
	  private static final long serialVersionUID = 8273875024682878518L;
	  private String text;
	  private URI uri;
	  private Action action;

	  public SwingLink(String text, URI uri){
	    super();
	    setup(text,uri);
	    setCursor(new Cursor(Cursor.HAND_CURSOR));
	  }
	  
	  public SwingLink(String text, String tooltip, Action action) {
		  super();
		  setup(text,tooltip, action);
		  setCursor(new Cursor(Cursor.HAND_CURSOR));
	  }

	  public SwingLink(String text, String uri){
	    super();
	    setup(text,URI.create(uri));
	    setCursor(new Cursor(Cursor.HAND_CURSOR));
	  }

	  public void setup(String t, URI u){
	    text = t;
	    uri = u;
	    setText(text);
	    setToolTipText(uri.toString());
	    addMouseListener(new MouseAdapter() {
	      public void mouseClicked(MouseEvent e) {
	        open(uri);
	      }
	      public void mouseEntered(MouseEvent e) {
	        setText(text,false);
	      }
	      public void mouseExited(MouseEvent e) {
	        setText(text,true);
	      }
	    });
	  }
	  
	  public void setup(String t, String tooltip, Action u){
		    text = t;
		    action = u;
		    setText(text);
		    setToolTipText(tooltip);
		    addMouseListener(new MouseAdapter() {
		      public void mouseClicked(MouseEvent e) {
		        u.actionPerformed(new ActionEvent(this,ActionEvent.ACTION_PERFORMED,null));
		      }
		      public void mouseEntered(MouseEvent e) {
		        setText(text,false);
		      }
		      public void mouseExited(MouseEvent e) {
		        setText(text,true);
		      }
		    });
		  }

	  @Override
	  public void setText(String text){
	    setText(text,true);
	  }

	  public void setText(String text, boolean ul){
	    String link = ul ? "<u>"+text+"</u>" : text;
	    super.setText("<html><span style=\"color: #000099;\">"+
	    link+"</span></html>");
	    this.text = text;
	  }

	  public String getRawText(){
	    return text;
	  }

	  private static void open(URI uri) {
	    if (Desktop.isDesktopSupported()) {
	      Desktop desktop = Desktop.getDesktop();
	      try {
	        desktop.browse(uri);
	      } catch (IOException e) {
	        JOptionPane.showMessageDialog(null,
	            "Failed to launch the link, your computer is likely misconfigured.",
	            "Cannot Launch Link",JOptionPane.WARNING_MESSAGE);
	      }
	    } else {
	      JOptionPane.showMessageDialog(null,
	          "Java is not able to launch links on your computer.",
	          "Cannot Launch Link", JOptionPane.WARNING_MESSAGE);
	    }
	  }
	}