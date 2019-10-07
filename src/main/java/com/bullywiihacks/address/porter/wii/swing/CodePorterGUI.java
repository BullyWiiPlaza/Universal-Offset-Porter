package com.bullywiihacks.address.porter.wii.swing;

import com.bullywiihacks.address.porter.wii.CodeAddress;
import com.bullywiihacks.address.porter.wii.CodeAddressPorter;
import com.bullywiihacks.address.porter.wii.GeckoCodeParser;
import com.bullywiihacks.address.porter.wii.utilities.contextmenu.DefaultContextMenu;
import com.bullywiihacks.address.porter.wii.utilities.contextmenu.WriteProtectedContextMenu;
import com.bullywiihacks.address.porter.wii.utilities.loaders.FontLoader;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

public class CodePorterGUI
{
	private static String programVersion = "4.02";
	private static String windowTitle = "Universal Code Offset Porter v" + programVersion;

	private JFrame codePorterGUI;
	private JTextField sourceDumpPath;
	private JTextField destinationDumpPath;
	private JTextArea inputCodeArea;
	private Preferences guiEntries = Preferences.userRoot().node(
			this.getClass().getName());
	private DumpChooser chooser;
	private CodeAddressPorter codePorter = new CodeAddressPorter();
	private JTextArea portedCodeArea;
	private JButton portButton;

	public CodePorterGUI() throws Exception
	{
		portButton = new JButton("Port");

		codePorterGUI = new JFrame();
		codePorterGUI.setIconImage(Toolkit.getDefaultToolkit().getImage(
				CodePorterGUI.class.getResource("/com/bullywiihacks/address/porter/wii/resources/Binary.jpg")));
		codePorterGUI.setTitle(windowTitle);
		codePorterGUI.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		codePorterGUI.setSize(581, 695);
		codePorterGUI.setLocationRelativeTo(null);
		codePorterGUI.getContentPane().setLayout(
				new FormLayout(new ColumnSpec[]{
						FormFactory.RELATED_GAP_COLSPEC,
						ColumnSpec.decode("default:grow"),}, new RowSpec[]{
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,
						FormFactory.RELATED_GAP_ROWSPEC,
						FormFactory.DEFAULT_ROWSPEC,}));

		JLabel sourceDumpLabel = new JLabel("Source RAM Dump:");
		codePorterGUI.getContentPane().add(sourceDumpLabel,
				"1, 2, 2, 1, center, default");

		JButton sourceBrowseButton = new JButton("Browse...");
		sourceBrowseButton.addActionListener(actionEvent ->
		{
			chooser = new DumpChooser(sourceDumpPath);

			chooser.setCurrentDirectory();

			chooser.selectDump(codePorterGUI.getRootPane());
		});
		codePorterGUI.getContentPane().add(sourceBrowseButton, "1, 4, 2, 1");

		sourceDumpPath = new JTextField();
		sourceDumpPath.getDocument().addDocumentListener(new DocumentListener()
		{
			public void changedUpdate(DocumentEvent e)
			{
				readDestinationRAMDump();
			}

			public void removeUpdate(DocumentEvent e)
			{
				readDestinationRAMDump();
			}

			public void insertUpdate(DocumentEvent e)
			{
				readDestinationRAMDump();
			}

			void readDestinationRAMDump()
			{
				new SwingWorker<String, String>()
				{
					@Override
					protected String doInBackground()
					{
						managePortButtonAvailability();

						try
						{
							codePorter.setSourceMemoryDumpBytes(null);

							codePorterGUI
									.setTitle("Reading source dump...");

							codePorter.setSourceMemoryDumpBytes(Files.readAllBytes(Paths.get(sourceDumpPath.getText())));
						} catch (IOException exception)
						{
							exception.printStackTrace();
						}

						return null;
					}

					@Override
					protected void done()
					{
						codePorterGUI.setTitle(windowTitle);
						managePortButtonAvailability();
					}
				}.execute();
			}
		});
		sourceDumpPath.setToolTipText("Path to the source RAM Dump");
		codePorterGUI.getContentPane().add(sourceDumpPath,
				"1, 6, 2, 1, fill, default");
		sourceDumpPath.setColumns(10);
		sourceDumpPath.setText(guiEntries.get("SRC_PATH",
				new File("").getAbsolutePath()));

		JLabel destinationDumpLabel = new JLabel("Destination RAM Dump:");
		codePorterGUI.getContentPane().add(destinationDumpLabel,
				"1, 8, 2, 1, center, bottom");

		JButton destinationBrowseButton = new JButton("Browse...");
		destinationBrowseButton.addActionListener(actionEvent ->
		{
			chooser = new DumpChooser(destinationDumpPath);

			chooser.setCurrentDirectory();

			chooser.selectDump(codePorterGUI.getRootPane());
		});
		codePorterGUI.getContentPane().add(destinationBrowseButton,
				"1, 10, 2, 1");

		destinationDumpPath = new JTextField();
		destinationDumpPath.getDocument().addDocumentListener(
				new DocumentListener()
				{
					public void changedUpdate(DocumentEvent e)
					{
						readDestinationRAMDump();
					}

					public void removeUpdate(DocumentEvent e)
					{
						readDestinationRAMDump();
					}

					public void insertUpdate(DocumentEvent e)
					{
						readDestinationRAMDump();
					}

					void readDestinationRAMDump()
					{
						new SwingWorker<String, String>()
						{
							@Override
							protected String doInBackground()
							{
								managePortButtonAvailability();

								try
								{
									codePorter.setDestinationDumpBytes(null);

									codePorterGUI
											.setTitle("Reading destination dump...");

									codePorter.setDestinationDumpBytes(Files.readAllBytes(Paths.get(destinationDumpPath.getText())));
								} catch (IOException exception)
								{
									exception.printStackTrace();
								}

								return null;
							}

							@Override
							protected void done()
							{
								codePorterGUI.setTitle(windowTitle);
								managePortButtonAvailability();
							}
						}.execute();
					}
				});
		destinationDumpPath.setToolTipText("Path to the destination RAM dump");
		codePorterGUI.getContentPane().add(destinationDumpPath,
				"1, 12, 2, 1, fill, default");
		destinationDumpPath.setColumns(10);
		destinationDumpPath.setText(guiEntries.get("DEST_PATH",
				new File("").getAbsolutePath()));

		JLabel codeAddressLabel = new JLabel("Input Code:");
		codePorterGUI.getContentPane().add(codeAddressLabel,
				"2, 18, center, default");

		inputCodeArea = new JTextArea(12, 20);
		DefaultContextMenu inputAreaContextMenu = new DefaultContextMenu();
		inputAreaContextMenu.add(inputCodeArea);
		JScrollPane inputScrollPane = new JScrollPane(inputCodeArea);
		inputCodeArea.setToolTipText("The code that should be ported");
		codePorterGUI.getContentPane().add(inputScrollPane,
				"1, 20, 2, 1, fill, default");
		inputCodeArea.setColumns(10);
		inputCodeArea.setText(guiEntries.get("ADDRESS", ""));

		portButton.setToolTipText("Start porting the code");
		portButton.addActionListener(actionEvent -> new SwingWorker<String, String>()
		{
			@Override
			protected String doInBackground()
			{
				portButton.setEnabled(false);
				portedCodeArea.setText("");

				String codeToPort = inputCodeArea.getText();

				GeckoCodeParser codeParser = new GeckoCodeParser(
						codeToPort);

				List<CodeAddress> codeAddresses = codeParser
						.getCodeAddresses();
				List<String> portedAddresses = new ArrayList<>();

				for (CodeAddress codeAddress : codeAddresses)
				{
					codePorter.setCodeAddress(codeAddress);

					codePorterGUI.setTitle("Porting...");

//							double startingTime = System.currentTimeMillis();

					int portedAddress = codePorter.portAddress();

//							System.out.println(((System.currentTimeMillis() - startingTime) / 1000 + " seconds"));

					if (portedAddress == -1)
					{
						codePorterGUI.setTitle(windowTitle);

						portedAddresses.add("??????");
					} else
					{
						String newPortedAddress = Integer.toHexString(
								portedAddress).toUpperCase()
								+ "";

						if (newPortedAddress.length() == 7)
						{
							newPortedAddress = newPortedAddress
									.substring(1);
						}

						portedAddresses.add(newPortedAddress);
					}
				}

				codeParser.insertPortedAddresses(portedAddresses);

				String portedCode = codeParser.getGeckoCode();

				portedCodeArea.setText(portedCode);

				codePorterGUI.setTitle(windowTitle);

				return null;
			}

			@Override
			protected void done()
			{
				portButton.setEnabled(true);
			}
		}.execute());

		JLabel portedAddressLabel = new JLabel("Ported Code:");
		codePorterGUI.getContentPane().add(portedAddressLabel,
				"1, 22, 2, 1, center, default");

		portedCodeArea = new JTextArea();
		WriteProtectedContextMenu portedCodeAreaContextMenu = new WriteProtectedContextMenu();
		portedCodeAreaContextMenu.add(portedCodeArea);
		JScrollPane outputScrollPane = new JScrollPane(portedCodeArea);
		portedCodeArea.setRows(8);
		portedCodeArea.setToolTipText("The ported code");
		portedCodeArea.setEditable(false);
		codePorterGUI.getContentPane().add(outputScrollPane,
				"1, 24, 2, 1, fill, default");
		portedCodeArea.setColumns(10);
		codePorterGUI.getContentPane().add(portButton, "1, 26, 2, 1");

		JLabel copyrightLabel = new JLabel(
				"\u00A9 2014 - 2015 Bully@WiiPlaza Productions");
		copyrightLabel
				.setToolTipText("Click here to visit the official program release topic on BullyWiiHacks");
		copyrightLabel.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent clickEvent)
			{
				visit("http://www.bullywiihacks.com/t3423-");
			}
		});

		FontLoader fontLoader = new FontLoader("com/bullywiihacks/address/porter/wii/resources/TektonPro-BoldExt.otf");
		Font copyrightFont = fontLoader.getFont(Font.BOLD, 22);

		copyrightLabel.setFont(copyrightFont);
		codePorterGUI.getContentPane().add(copyrightLabel,
				"2, 28, center, default");
		codePorterGUI.setVisible(true);

		Runtime.getRuntime().addShutdownHook(new Thread(() ->
		{
			guiEntries.put("SRC_PATH", sourceDumpPath.getText());
			guiEntries.put("DEST_PATH", destinationDumpPath.getText());
			guiEntries.put("ADDRESS", inputCodeArea.getText());
		}));
	}

	@SuppressWarnings("SameParameterValue")
	private static void visit(String url)
	{
		Desktop desktop = Desktop.getDesktop();

		try
		{
			desktop.browse(new URI(url));
		} catch (Exception exception)
		{
			exception.printStackTrace();
		}
	}

	private void managePortButtonAvailability()
	{
		if (codePorter.getSourceDumpBytes() == null || codePorter.getDestinationDumpBytes() == null)
		{
			portButton.setEnabled(false);
		} else
		{
			portButton.setEnabled(true);
		}
	}
}