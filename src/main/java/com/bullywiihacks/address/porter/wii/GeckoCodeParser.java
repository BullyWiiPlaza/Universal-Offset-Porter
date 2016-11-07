package com.bullywiihacks.address.porter.wii;

import java.util.ArrayList;
import java.util.List;

import com.bullywiihacks.address.porter.wii.utilities.general.DebuggingPrints;
import com.bullywiihacks.address.porter.wii.utilities.general.GeckoCode;

public class GeckoCodeParser
{
	private String inputCode;

	private List<String> codeLines = new ArrayList<>();
	private List<CodeAddress> codeAddresses = new ArrayList<>();
	private List<Integer> codeAddressIndices = new ArrayList<>();
	private List<Boolean> validCodeLines = new ArrayList<>();
	private CodeTypes codeTypes = new CodeTypes();

	public GeckoCodeParser(String inputCode)
	{
		this.inputCode = inputCode;
		DebuggingPrints.printLn("Input code: " + System.lineSeparator()
				+ inputCode);

		DebuggingPrints.printLn();

		getAllInputLines();

		getAllCodeAddresses();
	}

	private void getAllCodeAddresses()
	{
		int codeLineIndex = getFirstValidCodeLine();
		String codeLine;
		String codeAddressString;
		String codeType;
		int codeAddressIndex;

		while (codeLineIndex < codeLines.size())
		{
			codeLine = codeLines.get(codeLineIndex);
			DebuggingPrints.printLn("Code line: " + codeLine);
			codeType = getCodeType(codeLine);
			DebuggingPrints.printLn("Code type: " + codeType);

			if (!codeType.equals("E0"))
			{
				if (codeTypes.getStringWriteCodeTypes().contains(codeType))
				{
					codeLineIndex = codeLineIndex + getLinesToSkip(codeLine);
				}

				codeAddressString = getAddress(codeLine);
				DebuggingPrints.printLn("Code address: " + codeAddressString);

				int numberOfContainedAddresses = 0;

				for (CodeAddress codeAddress : codeAddresses)
				{
					if (codeAddress.toString().equals(codeAddressString))
					{
						numberOfContainedAddresses++;
					}
				}

				DebuggingPrints
						.printLn("Instances of this address picked up so far: "
								+ numberOfContainedAddresses);

				codeAddressIndex = getCodeAddressIndex(codeAddressString,
						(numberOfContainedAddresses + 1));
				codeAddressIndices.add(codeAddressIndex);
				DebuggingPrints.printLn("Code address index: "
						+ codeAddressIndex);

				codeAddresses.add(new CodeAddress(Integer.parseInt(codeAddressString, 16), GeckoCode.isAssembly(codeType)));
				DebuggingPrints.printLn("Code address added!");
			}

			codeLineIndex++;

			if (codeLineIndex >= validCodeLines.size())
			{
				break;
			}

			if (!validCodeLines.get(codeLineIndex))
			{
				break;
			}
		}

		DebuggingPrints.printLn();
	}

	private void getAllInputLines()
	{
		String[] individualLines = inputCode.split("\n");
		boolean validCodeLine;
		String codeLine;

		for (int codeLinesIndex = 0; codeLinesIndex < individualLines.length; codeLinesIndex++)
		{
			// Remove line break
			individualLines[codeLinesIndex] = individualLines[codeLinesIndex]
					.replace("\r", "");

			codeLine = individualLines[codeLinesIndex];
			validCodeLine = isValidCodeLine(codeLine);

			codeLines.add(codeLine);
			validCodeLines.add(validCodeLine);

			DebuggingPrints.printLn((codeLinesIndex + 1) + ". line: "
					+ codeLine + System.lineSeparator() + "Valid code line: "
					+ validCodeLine);

			DebuggingPrints.printLn();
		}
	}

	private int getFirstValidCodeLine()
	{
		for (int codeLinesIndex = 0; codeLinesIndex < codeLines.size(); codeLinesIndex++)
		{
			if (isValidCodeLine(codeLines.get(codeLinesIndex)))
			{
				return codeLinesIndex;
			}
		}

		return -1;
	}

	private String getCodeType(String codeLine)
	{
		return codeLine.substring(0, 2);
	}

	private String getAddress(String codeLine)
	{
		String codeAddress;
		String codeType = getCodeType(codeLine);
		String codeTypeReplacement = "";

		if (codeTypes.getMem81CodeTypes().contains(codeType))
		{
			codeTypeReplacement = "1";
		}

		codeAddress = codeLine.replace(getCodeType(codeLine),
				codeTypeReplacement);

		int codeSpaceIndex = codeAddress.indexOf(" ");

		codeAddress = codeAddress.substring(0, codeSpaceIndex);

		return codeAddress;
	}

	private int getLinesToSkip(String codeLine)
	{
		String additionalLines = codeLine.substring(codeLine.length() - 2,
				codeLine.length());

		return Integer.parseInt(additionalLines, 16);
	}

	private boolean isValidCodeLine(String codeLine)
	{
		try
		{
			return codeLine.charAt(8) == ' ' && codeLine.length() == 17;
		} catch (StringIndexOutOfBoundsException s)
		{
			return false;
		}
	}

	private int getCodeAddressIndex(String codeAddress, int nthOccurrence)
	{
		if (codeAddress.length() > 6)
		{
			codeAddress = codeAddress.substring(1);
		}

		return ordinalIndexOf(inputCode, codeAddress, nthOccurrence);
	}

	private static int ordinalIndexOf(String str, String searchStr, int ordinal)
	{
		if (str == null || searchStr == null || ordinal <= 0)
		{
			return -1;
		}
		if (searchStr.length() == 0)
		{
			return 0;
		}
		int found = 0;
		int index = -1;
		do
		{
			index = str.indexOf(searchStr, index + 1);
			if (index < 0)
			{
				return index;
			}
			found++;
		} while (found < ordinal);

		return index;
	}

	private void insertAddress(String portedCodeAddress, int startingIndex)
	{
		String portedCode = inputCode.substring(0, startingIndex);

		while (portedCodeAddress.length() < 6)
		{
			portedCodeAddress = "0" + portedCodeAddress;
		}

		portedCode = portedCode + portedCodeAddress;

		portedCode = portedCode
				+ inputCode.substring(
				startingIndex + portedCodeAddress.length(),
				inputCode.length());

		DebuggingPrints.printLn("Ported code: " + System.lineSeparator()
				+ portedCode);

		inputCode = portedCode;
	}

	public List<CodeAddress> getCodeAddresses()
	{
		return codeAddresses;
	}

	public void insertPortedAddresses(List<String> portedCodeAddresses)
	{
		for (int portedCodeAddressesIndex = 0; portedCodeAddressesIndex < portedCodeAddresses
				.size(); portedCodeAddressesIndex++)
		{
			insertAddress(portedCodeAddresses.get(portedCodeAddressesIndex),
					codeAddressIndices.get(portedCodeAddressesIndex));
		}
	}

	public String getGeckoCode()
	{
		return inputCode;
	}
}