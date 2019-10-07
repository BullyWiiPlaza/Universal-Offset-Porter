package com.bullywiihacks.address.porter.wiiu.graphical_interface;

import com.bullywiihacks.address.porter.wiiu.AssemblyChecker;
import com.bullywiihacks.address.porter.wiiu.OffsetPorter;
import com.bullywiihacks.address.porter.wiiu.OffsetPortingReport;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.io.File;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;

public class UniversalOffsetPorterGUI extends JFrame
{
	private JPanel rootPanel;

	private JTextField sourceMemoryDumpField;
	private JTextField destinationMemoryDumpField;
	private JButton browseSourceMemoryDumpButton;
	private JButton browseDestinationMemoryDumpButton;
	private JTextField maximumSearchTemplateTriesField;
	private JTextField dataBytesUnequalThresholdField;
	private JTextField offsetVarianceField;
	private JTextField assemblyNullBytesThresholdField;
	private JFormattedTextField sourceOffsetField;
	private JTextArea resultsArea;
	private JButton portButton;
	private JButton informationButton;
	private JButton cancelPortingButton;
	private boolean porting;
	private PersistentSettings persistentSettings;
	private boolean portingCanceled;

	private static UniversalOffsetPorterGUI instance;

	public static UniversalOffsetPorterGUI getInstance()
	{
		if (instance == null)
		{
			instance = new UniversalOffsetPorterGUI();
		}

		return instance;
	}

	private UniversalOffsetPorterGUI()
	{
		persistentSettings = new PersistentSettings();
		setFrameProperties();
		runInputCheckerThread();
		addPortOffsetButtonListener();
		addBrowseSourceMemoryDumpListener();
		addBrowseDestinationMemoryDumpListener();
		addSourceOffsetDocumentListener();
		DefaultContextMenu.addDefaultContextMenu(resultsArea);
		cancelPortingButton.addActionListener(actionEvent -> portingCanceled = true);
		maximumSearchTemplateTriesField.setText("" + OffsetPorter.MAXIMUM_SEARCH_TEMPLATE_TRIES);
		offsetVarianceField.setText("" + OffsetPorter.DEFAULT_OFFSET_VARIANCE);
		dataBytesUnequalThresholdField.setText("" + OffsetPorter.DATA_BYTES_UNEQUAL_THRESHOLD);
		assemblyNullBytesThresholdField.setText("" + AssemblyChecker.ASSEMBLY_NULL_BYTES_RATIO_THRESHOLD);

		restorePersistentSettings();
		addPersistentSettingsBackupShutdownHook();
		addInformationButtonListener();
	}

	private void addInformationButtonListener()
	{
		informationButton.addActionListener(actionEvent ->
		{
			try
			{
				Desktop.getDesktop().browse(new URI("https://github.com/BullyWiiPlaza/Universal-Offset-Porter"));
			} catch (Exception exception)
			{
				exception.printStackTrace();
			}
		});
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
		String prefix = "0x";

		if (value.startsWith(prefix))
		{
			value = value.substring(prefix.length());
		}

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
				try
				{
					setBackgroundColor(sourceMemoryDumpField);
					setBackgroundColor(destinationMemoryDumpField);
					boolean validMemoryDumps = isValidFile(sourceMemoryDumpField)
							&& isValidFile(destinationMemoryDumpField);
					validateSourceOffset();
					boolean isSourceOffsetValid = isSourceOffsetValid();
					portButton.setEnabled(validMemoryDumps && isSourceOffsetValid && !porting);
					cancelPortingButton.setEnabled(porting);

					Thread.sleep(10);
				} catch (Exception exception)
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
			resultsArea.setText("");
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
						OffsetPortingReport offsetPortingReport = offsetPorter.port();

						if (offsetPortingReport == null)
						{
							portingCanceled = false;
						} else
						{
							if (offsetPortingReport.getAddress() == OffsetPortingReport.FAILED_ADDRESS)
							{
								JOptionPane.showMessageDialog(rootPane,
										offsetPortingReport.toString(),
										"Porting Failed",
										JOptionPane.ERROR_MESSAGE);
							} else
							{
								resultsArea.setText(offsetPortingReport.toString());
							}
						}
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
		setSize(700, 500);
		setTitle("Universal Offset Porter by BullyWiiPlaza");
	}

	public boolean isPortingCanceled()
	{
		return portingCanceled;
	}
}