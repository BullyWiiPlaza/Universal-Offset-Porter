package com.bullywiihacks.address.porter.wii.utilities.contextmenu;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.KeyStroke;
import javax.swing.text.JTextComponent;

@SuppressWarnings("serial")
public class WriteProtectedContextMenu extends JPopupMenu
{
	private JMenuItem copy;
	private JMenuItem selectAll;

	private JTextComponent jTextComponent;

	public WriteProtectedContextMenu()
	{
		copy = new JMenuItem("Copy");
		copy.setEnabled(false);
		copy.setAccelerator(KeyStroke.getKeyStroke("control C"));
		copy.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				jTextComponent.copy();
			}
		});

		add(copy);

		add(new JSeparator());

		selectAll = new JMenuItem("Select All");
		selectAll.setEnabled(false);
		selectAll.setAccelerator(KeyStroke.getKeyStroke("control A"));
		selectAll.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent event)
			{
				jTextComponent.selectAll();
			}
		});

		add(selectAll);
	}

	public void add(JTextComponent jTextComponent)
	{
		jTextComponent.addMouseListener(new MouseAdapter()
		{
			@Override
			public void mouseReleased(MouseEvent releasedEvent)
			{
				if (releasedEvent.getButton() == MouseEvent.BUTTON3)
				{
					processClick(releasedEvent);
				}
			}
		});
	}

	private void processClick(MouseEvent event)
	{
		jTextComponent = (JTextComponent) event.getSource();

		boolean enableCopy = false;
		boolean enableSelectAll = false;

		String selectedText = jTextComponent.getSelectedText();
		String text = jTextComponent.getText();

		if (text != null)
		{
			if (text.length() > 0)
			{
				enableSelectAll = true;
			}
		}

		if (selectedText != null)
		{
			if (selectedText.length() > 0)
			{
				enableCopy = true;
			}
		}

		copy.setEnabled(enableCopy);
		selectAll.setEnabled(enableSelectAll);

		show(jTextComponent, event.getX(), event.getY());
	}
}