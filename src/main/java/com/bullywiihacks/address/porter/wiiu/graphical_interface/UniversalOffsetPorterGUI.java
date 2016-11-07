package com.bullywiihacks.address.porter.wiiu.graphical_interface;

import com.bullywiihacks.address.porter.wiiu.AssemblyChecker;
import com.bullywiihacks.address.porter.wiiu.OffsetPorter;
import com.bullywiihacks.address.porter.wiiu.PortedOffset;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UniversalOffsetPorterGUI extends JFrame
{
	private JPanel rootPanel;

	private JTextField sourceMemoryDumpField;
	private JTextField destinationMemoryDumpField;
	private JButton browseSourceMemoryDumpButton;
	private JButton browseDestinationMemoryDumpButton;
	private JFormattedTextField maximumSearchTemplateTriesField;
	private JFormattedTextField dataBytesUnequalThresholdField;
	private JFormattedTextField offsetVarianceField;
	private JFormattedTextField assemblyNullBytesThresholdField;
	private JFormattedTextField sourceOffsetField;
	private JTextArea resultsArea;
	private JButton portButton;
	private boolean porting;

	private PersistentSettings persistentSettings;

	public UniversalOffsetPorterGUI()
	{
		persistentSettings = new PersistentSettings();
		setFrameProperties();
		runInputCheckerThread();
		addPortOffsetButtonListener();
		addBrowseSourceMemoryDumpListener();
		addBrowseDestinationMemoryDumpListener();
		addSourceOffsetDocumentListener();
		DefaultContextMenu.addDefaultContextMenu(resultsArea);

		PlainDocument doc = new PlainDocument();
		doc.setDocumentFilter(new DocumentFilter()
		{
			@Override
			public void insertString(FilterBypass fb, int off, String str, AttributeSet attr)
					throws BadLocationException
			{
				fb.insertString(off, str.replaceAll("\\D++", ""), attr);  // remove non-digits
			}

			@Override
			public void replace(FilterBypass fb, int off, int len, String str, AttributeSet attr)
					throws BadLocationException
			{
				fb.replace(off, len, str.replaceAll("\\D++", ""), attr);  // remove non-digits
			}
		});

		offsetVarianceField.setDocument(doc);

		maximumSearchTemplateTriesField.setValue(OffsetPorter.MAXIMUM_SEARCH_TEMPLATE_TRIES);
		offsetVarianceField.setValue(OffsetPorter.DEFAULT_OFFSET_VARIANCE);
		dataBytesUnequalThresholdField.setValue(OffsetPorter.DATA_BYTES_UNEQUAL_THRESHOLD);
		assemblyNullBytesThresholdField.setValue(AssemblyChecker.ASSEMBLY_NULL_BYTES_RATIO_THRESHOLD);

		restorePersistentSettings();
		addPersistentSettingsBackupShutdownHook();
	}

	private void restorePersistentSettings()
	{
		String sourceMemoryDump = persistentSettings.get("SOURCE_MEMORY_DUMP");
		if (sourceMemoryDump != null)
		{
			sourceMemoryDumpField.setText(sourceMemoryDump);
		}

		String destinationMemoryDump = persistentSettings.get("DESTINATION_MEMORY_DUMP");
		if (destinationMemoryDump != null)
		{
			destinationMemoryDumpField.setText(destinationMemoryDump);
		}

		String sourceOffset = persistentSettings.get("SOURCE_OFFSET");
		if (sourceOffset != null)
		{
			sourceOffsetField.setText(sourceOffset);
		}

		String maximumTries = persistentSettings.get("MAXIMUM_TRIES");
		if (maximumTries != null)
		{
			maximumSearchTemplateTriesField.setText(maximumTries);
		}

		String dataBytesTemplateAccuracy = persistentSettings.get("DATA_BYTES_TEMPLATE_ACCURACY");
		if (dataBytesTemplateAccuracy != null)
		{
			dataBytesUnequalThresholdField.setText(dataBytesTemplateAccuracy);
		}

		String offsetVariance = persistentSettings.get("OFFSET_VARIANCE");
		if (offsetVariance != null)
		{
			offsetVarianceField.setText(offsetVariance);
		}

		String assemblyNullBytesThreshold = persistentSettings.get("ASSEMBLY_NULL_BYTES_THRESHOLD");
		if (assemblyNullBytesThreshold != null)
		{
			assemblyNullBytesThresholdField.setText(assemblyNullBytesThreshold);
		}
	}

	private void addPersistentSettingsBackupShutdownHook()
	{
		Runtime.getRuntime().addShutdownHook(new Thread(() ->
		{
			persistentSettings.put("SOURCE_MEMORY_DUMP", sourceMemoryDumpField.getText());
			persistentSettings.put("DESTINATION_MEMORY_DUMP", destinationMemoryDumpField.getText());
			persistentSettings.put("SOURCE_OFFSET", sourceOffsetField.getText());
			persistentSettings.put("MAXIMUM_TRIES", maximumSearchTemplateTriesField.getText());
			persistentSettings.put("DATA_BYTES_TEMPLATE_ACCURACY", dataBytesUnequalThresholdField.getText());
			persistentSettings.put("OFFSET_VARIANCE", offsetVarianceField.getText());
			persistentSettings.put("ASSEMBLY_NULL_BYTES_THRESHOLD", assemblyNullBytesThresholdField.getText());
			persistentSettings.writeToFile();
		}));
	}

	private void addSourceOffsetDocumentListener()
	{

	}

	private void validateSourceOffset()
	{
		boolean validSourceOffset = isSourceOffsetValid();
		sourceOffsetField.setBackground(validSourceOffset ? Color.GREEN : Color.RED);
	}

	private boolean isSourceOffsetValid()
	{
		long sourceOffset = getIntValue(sourceOffsetField);
		long sourceMemoryDumpLength = new File(sourceMemoryDumpField.getText()).length();
		return sourceOffset < sourceMemoryDumpLength;
	}

	private long getIntValue(JTextComponent textComponent)
	{
		String value = textComponent.getText().replace(",", "");

		if (value.equals(""))
		{
			return 0;
		}

		return Long.parseUnsignedLong(value, 16);
	}

	private double getDoubleValue(JTextComponent textComponent)
	{
		String value = textComponent.getText();

		if (value.equals(""))
		{
			return 0;
		}

		return Double.parseDouble(value);
	}

	private void addBrowseSourceMemoryDumpListener()
	{
		browseSourceMemoryDumpButton.addActionListener(actionEvent ->
		{
			SingleFileChooser singleFileChooser = new SingleFileChooser(sourceMemoryDumpField);
			singleFileChooser.allowFileSelection(rootPane);
		});
	}

	private void addBrowseDestinationMemoryDumpListener()
	{
		browseDestinationMemoryDumpButton.addActionListener(actionEvent ->
		{
			SingleFileChooser singleFileChooser = new SingleFileChooser(destinationMemoryDumpField);
			singleFileChooser.allowFileSelection(rootPane);
		});
	}

	private void runInputCheckerThread()
	{
		Thread filesChecker = new Thread(() ->
		{
			while (true)
			{
				setBackgroundColor(sourceMemoryDumpField);
				setBackgroundColor(destinationMemoryDumpField);
				boolean validMemoryDumps = isValidFile(sourceMemoryDumpField)
						&& isValidFile(destinationMemoryDumpField);
				validateSourceOffset();
				boolean isSourceOffsetValid = isSourceOffsetValid();
				portButton.setEnabled(validMemoryDumps && isSourceOffsetValid && !porting);

				try
				{
					Thread.sleep(10);
				} catch (InterruptedException exception)
				{
					exception.printStackTrace();
				}
			}
		});

		filesChecker.start();
	}

	private void setBackgroundColor(JTextField filePathField)
	{
		boolean isFile = isValidFile(filePathField);
		filePathField.setBackground(isFile ? Color.GREEN : Color.RED);
	}

	private boolean isValidFile(JTextField filePathField)
	{
		String filePath = filePathField.getText();

		return Files.isRegularFile(Paths.get(filePath));
	}

	private void addPortOffsetButtonListener()
	{
		portButton.addActionListener(actionEvent ->
		{
			String sourceMemoryDumpFilePath = sourceMemoryDumpField.getText();
			String destinationMemoryDumpFilePath = destinationMemoryDumpField.getText();
			int sourceOffset = Integer.parseUnsignedInt(sourceOffsetField.getText(), 16);
			String portButtonText = portButton.getText();
			portButton.setText("Porting...");
			porting = true;

			new SwingWorker<String, String>()
			{
				@Override
				protected String doInBackground() throws Exception
				{
					try
					{
						OffsetPorter.DATA_BYTES_UNEQUAL_THRESHOLD = getDoubleValue(dataBytesUnequalThresholdField);
						OffsetPorter.DEFAULT_OFFSET_VARIANCE = getDoubleValue(offsetVarianceField);
						OffsetPorter.MAXIMUM_SEARCH_TEMPLATE_TRIES = (int) getIntValue(maximumSearchTemplateTriesField);
						AssemblyChecker.ASSEMBLY_NULL_BYTES_RATIO_THRESHOLD = getDoubleValue(assemblyNullBytesThresholdField);

						OffsetPorter offsetPorter = new OffsetPorter(sourceMemoryDumpFilePath, destinationMemoryDumpFilePath, sourceOffset);
						PortedOffset portedOffset = offsetPorter.port();
						resultsArea.setText(portedOffset.toString());
					} catch (Exception exception)
					{
						exception.printStackTrace();
					}

					return null;
				}

				@Override
				protected void done()
				{
					portButton.setText(portButtonText);
					porting = false;
				}
			}.execute();
		});
	}

	private void setFrameProperties()
	{
		add(rootPanel);
		WindowUtilities.setIconImage(this);
		setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
		setLocationRelativeTo(null);
		setSize(500, 500);
		setTitle("Universal Offset Porter");
	}
}