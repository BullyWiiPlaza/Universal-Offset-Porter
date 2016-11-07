package com.bullywiihacks.address.porter.wii;

import com.bullywiihacks.address.porter.wii.utilities.general.ByteUtilities;
import com.bullywiihacks.address.porter.wii.utilities.general.DebuggingPrints;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class CodeAddressPorter
{
	private byte[] sourceMemoryDumpBytes;
	private byte[] destinationMemoryDumpBytes;
	private CodeAddress codeAddress;
	private int shiftedBytes = 0;

	public CodeAddressPorter(String sourceMemoryDumpFilePath, String destinationMemoryDumpFilePath) throws IOException
	{
		sourceMemoryDumpBytes = readBytes(sourceMemoryDumpFilePath);
		destinationMemoryDumpBytes = readBytes(destinationMemoryDumpFilePath);
	}

	public CodeAddressPorter()
	{

	}

	private byte[] readBytes(String filePath) throws IOException
	{
		Path path = Paths.get(filePath);
		return Files.readAllBytes(path);
	}

	public void setSourceMemoryDumpBytes(byte[] sourceMemoryDumpBytes)
	{
		this.sourceMemoryDumpBytes = sourceMemoryDumpBytes;
	}

	public byte[] getSourceDumpBytes()
	{
		return sourceMemoryDumpBytes;
	}

	public void setDestinationDumpBytes(byte[] destinationDump)
	{
		this.destinationMemoryDumpBytes = destinationDump;
	}

	public byte[] getDestinationDumpBytes()
	{
		return destinationMemoryDumpBytes;
	}

	public void setCodeAddress(CodeAddress codeAddress)
	{
		this.codeAddress = codeAddress;
	}

	public int portAddress()
	{
		byte[] searchTemplate;
		byte[] defaultSourceValue;
		int tries = 1;

		while (true)
		{
			SearchTemplateSelector searchTemplateSelector = new SearchTemplateSelector();
			searchTemplate = searchTemplateSelector.getSearchTemplate();

			if (searchTemplate == null)
			{
				return -1;
			}

			defaultSourceValue = ByteUtilities.getIntegerValue(codeAddress.getAddress(), sourceMemoryDumpBytes);

			DebuggingPrints.printLn("Current try: " + tries);
			DebuggingPrints.printLn("Current search template: "
					+ ByteUtilities.toHexadecimal(searchTemplate));

			int sourceDumpMatch = ByteUtilities.getFirstMatchIndex(sourceMemoryDumpBytes,
					searchTemplate);

			DebuggingPrints.printLn("Search templates offset: "
					+ Integer.toHexString(shiftedBytes).toUpperCase()
					+ " bytes");

			DebuggingPrints
					.print("Is source file offset ("
							+ Integer.toHexString(codeAddress.getAddress()).toUpperCase()
							+ ") plus search template offset ("
							+ Integer.toHexString(shiftedBytes).toUpperCase()
							+ ") equal to first match of search template in source dump ("
							+ Integer.toHexString(sourceDumpMatch)
							.toUpperCase() + ")? ");

			if (codeAddress.getAddress() + shiftedBytes == sourceDumpMatch)
			{
				DebuggingPrints
						.printLn("Yes, applying template to destination dump...");

				int destinationDumpMatch = ByteUtilities.getFirstMatchIndex(
						destinationMemoryDumpBytes, searchTemplate);

				DebuggingPrints
						.print("Can template be found in destination dump? ");

				if (destinationDumpMatch != -1)
				{
					DebuggingPrints.printLn("Yes, comparing default values...");

					byte[] defaultDestinationValue = ByteUtilities
							.getIntegerValue(destinationDumpMatch
									- shiftedBytes, destinationMemoryDumpBytes);

					DebuggingPrints
							.print("Is default value from source dump ("
									+ ByteUtilities
									.toHexadecimal(defaultSourceValue)
									+ ") similar to default value from destination dump ("
									+ ByteUtilities
									.toHexadecimal(defaultDestinationValue)
									+ ")? ");

					// The default value instruction seems to be the same,
					// except for the offset
					if (ByteUtilities
							.toHexadecimal(defaultSourceValue)
							.substring(0, 4)
							.equals(ByteUtilities.toHexadecimal(
									defaultDestinationValue).substring(0, 4)))
					{
						int portedOffset = destinationDumpMatch - shiftedBytes;

						DebuggingPrints
								.printLn("Yes, file offset most likely successfully ported ("
										+ Integer.toHexString(portedOffset)
										.toUpperCase() + ")...");

						DebuggingPrints.printLn();

						return portedOffset;
					} else
					{
						DebuggingPrints.printLn("No");
					}
				} else
				{
					DebuggingPrints.printLn("No");
				}
			} else
			{
				DebuggingPrints.printLn("No");
			}

			tries++;

			shiftTemplatePointer();

			if (triesExpired())
			{
				return -1;
			}

			DebuggingPrints.printLn();
		}
	}

	private void shiftTemplatePointer()
	{
		if (shiftedBytes > getMaximumByteShift() - 4)
		{
			shiftedBytes = shiftedBytes * -1;
		}

		shiftedBytes = shiftedBytes + 4;
	}

	private int getMaximumByteShift()
	{
		int maximumTries = 1000;

		return maximumTries * 2;
	}

	private boolean triesExpired()
	{
		return shiftedBytes == 0;
	}

	public int portAddress(int address)
	{
		CodeAddress codeAddress = new CodeAddress(address, false);
		setCodeAddress(codeAddress);
		return portAddress();
	}

	private class SearchTemplateSelector
	{
		private byte[] getSearchTemplate()
		{
			int searchTemplateByteSize = 8;
			byte[] searchTemplateBuffer = new byte[searchTemplateByteSize];

			while (true)
			{
				for (int searchTemplateByteIndex = 0; searchTemplateByteIndex < searchTemplateBuffer.length; searchTemplateByteIndex++)
				{
					searchTemplateBuffer[searchTemplateByteIndex] = sourceMemoryDumpBytes[codeAddress.getAddress()
							+ searchTemplateByteIndex + shiftedBytes];
				}

				if (ByteUtilities.isAllZeros(searchTemplateBuffer))
				{
					shiftTemplatePointer();

					if (triesExpired())
					{
						return null;
					}

					continue;
				}

				if (ByteUtilities.containsPossibleCodeAddress(searchTemplateBuffer))
				{
					shiftTemplatePointer();

					if (triesExpired())
					{
						return null;
					}

					continue;
				}

				if (ByteUtilities.containsHighAssemblyOffsets(searchTemplateBuffer))
				{
					shiftTemplatePointer();

					if (triesExpired())
					{
						return null;
					}

					continue;
				}

				break;
			}

			return searchTemplateBuffer;
		}
	}
}