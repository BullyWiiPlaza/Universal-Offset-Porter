package com.bullywiihacks.address.porter.wiiu.graphical_interface;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.io.File;
import java.nio.file.Files;

public class SingleFileChooser extends JFileChooser
{
	private JTextComponent pathTextComponent;

	public SingleFileChooser(JTextComponent pathTextComponent)
	{
		this.pathTextComponent = pathTextComponent;

		String currentFilePath = pathTextComponent.getText();
		File currentFile = new File(currentFilePath);

		if (Files.isRegularFile(currentFile.toPath()))
		{
			setCurrentDirectory(currentFile);
		} else
		{
			String workingDirectory = System.getProperty("user.dir");
			File workingDirectoryFile = new File(workingDirectory);
			setCurrentDirectory(workingDirectoryFile);
		}

		setFileSelectionMode(JFileChooser.FILES_ONLY);
	}

	public void allowFileSelection(JRootPane rootPane)
	{
		int selectedAnswer = showOpenDialog(rootPane);

		if (selectedAnswer == JFileChooser.APPROVE_OPTION)
		{
			String selectedFilePath = getSelectedFile().getAbsolutePath();
			String workingDirectory = System.getProperty("user.dir");
			selectedFilePath = selectedFilePath.replace(workingDirectory + File.separator, "");
			pathTextComponent.setText(selectedFilePath);
		}
	}
}