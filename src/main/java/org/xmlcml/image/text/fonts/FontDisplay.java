package org.xmlcml.image.text.fonts;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;

import javax.swing.Box;
import javax.swing.ButtonGroup;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

// This program shows the displayed values for some Unicode symbols.
//Note the command line doesn't generally support Unicode fonts.
//
//Written 3/2005 by Wayne Pollock, Tampa Florida USA.

public class FontDisplay extends JApplet implements ActionListener {
	private JTextArea ta;
	private JLabel title, currentFontLbl;
	private Font font; // Need a font that can display Unicode!
	private ArrayList<Font> fontList;
	private JMenuBar menuBar;
	private JMenu fontMenu;

	public void init() {
		ta = new JTextArea(11, 20); // TextArea to display symbols
		ta.setEditable(false);

		// Find all Fonts that can display these symbols:
		fontList = new ArrayList<Font>();
		Collections.addAll(fontList, GraphicsEnvironment
				.getLocalGraphicsEnvironment().getAllFonts());
		for (Iterator<Font> i = fontList.iterator(); i.hasNext();) {
			Font f = i.next();
			if (!f.canDisplay('\u25B6')) {
				i.remove();
				System.out.println("bad: " + f);
			} else {
				System.out.println("ok: " + f);
			}
		}

		// Create the menu bar:
		menuBar = new JMenuBar();

		// Build the font menu:
		fontMenu = new JMenu("Unicode Fonts");
		fontMenu.setMnemonic(KeyEvent.VK_U);
		fontMenu.getAccessibleContext().setAccessibleDescription(
				"Menu of available Unicode fonts");
		menuBar.add(fontMenu);

		// Populate the font menu with radio button menu items for each font:
		ButtonGroup fontGroup = new ButtonGroup();
		JRadioButtonMenuItem item;

		// If no Unicode fonts found, add something anyway (disabled):
		if (fontList.size() == 0) {
			fontList.add(new Font("Lucida", Font.PLAIN, 18));
			item = new JRadioButtonMenuItem("Non-Unicode Font");
			item.setEnabled(false);
			fontGroup.add(item);
			fontMenu.add(item);
		} else {
			for (Font f : fontList) {
				item = new JRadioButtonMenuItem(f.getName());
				item.addActionListener(this); // Hook up event handling.
				fontGroup.add(item);
				fontMenu.add(item);
			}
		}

		setJMenuBar(menuBar);

		// Select the first font as default:
		fontMenu.getItem(0).setSelected(true);
		font = fontList.get(0);
		ta.setFont(font.deriveFont(Font.PLAIN, 18.0F));

		title = new JLabel("Some Useful Unicode Symbols", JLabel.CENTER);
		title.setFont(new Font("SansSeriff", Font.BOLD, 18));
		title.setForeground(Color.BLUE);

		currentFontLbl = new JLabel("(Font used: \"" + font.getName() + "\")",
				JLabel.CENTER);
		currentFontLbl.setFont(new Font("SansSeriff", Font.BOLD, 14));
		currentFontLbl.setForeground(Color.BLUE);

		// Layout and add the components:
		setContentPane(Box.createVerticalBox());
		add(title);
		add(Box.createVerticalGlue());
		add(currentFontLbl);
		add(Box.createVerticalGlue());
		add(Box.createVerticalGlue());
		add(new JScrollPane(ta));
		addContent(ta);
	}

	private void addContent(JTextArea ta) {
		ta.append("\\u2103: \u2103 (Celsius degrees)\n"
				+ "\\u2109: \u2109 (Fahrenheit degrees)\n"
				+ "\\u00B0: \u00B0 (Generic degree symbol)\n\n" +

				"\\u2713: \u2713 (Checkmark symbol)\n"
				+ "\\u2714: \u2714 (Heavy checkmark symbol)\n" +

				"\\u00A9: \u00A9 (Copyright symbol)\n\n"
				+ "\\u2192: \u2192 (Right arrow symbol)\n"
				+ "\\u25B6: \u25B6 (Hierarchical menu (arrow) symbol)\n");
	}

	public void actionPerformed(ActionEvent event) {
//		System.out.println("event: "+event);
		// Update Font selection:
		String fontName = event.getActionCommand();
		for (Font f : fontList)
			if (f.getName().equals(fontName))
				font = f;
		currentFontLbl.setText("(Font used: \"" + font.getName() + "\")");
		ta.setFont(font.deriveFont(Font.PLAIN, 18.0F));
	}

	public static void main(String[] args) {
		JFrame frame = new JFrame("Some Useful Unicode Symbols");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JApplet me = new FontDisplay();
		frame.add(me, BorderLayout.CENTER);
		me.init();
		me.start();
		frame.pack();
		frame.setVisible(true);
	}
}
