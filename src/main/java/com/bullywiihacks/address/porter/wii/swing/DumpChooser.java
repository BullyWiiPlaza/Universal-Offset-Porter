package com.bullywiihacks.address.porter.wii.swing;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JRootPane;
import javax.swing.text.JTextComponent;

public class DumpChooser
{
	private JFileChooser dumpChooser;
	private JTextComponent pathField;

	public DumpChooser(JTextComponent pathField)
	{
		dumpChooser = new JFileChooser();
		dumpChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

		this.pathField = pathField;
	}

	public void setCurrentDirectory()
	{
		if (!new File(pathField.getText()).exists())
		{
			dumpChooser.setCurrentDirectory(new File(new File("")
					.getAbsolutePath()));
		} else
		{
			dumpChooser.setCurrentDirectory(new File(pathField.getText()));
		}
	}

	public void selectDump(JRootPane rootPane)
	{
		int selection = dumpChooser.showOpenDialog(rootPane);

		if (selection == JFileChooser.APPROVE_OPTION)
		{
			pathField.setText(dumpChooser.getSelectedFile().getAbsolutePath());
		}
	}
}
