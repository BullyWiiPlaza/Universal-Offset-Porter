package com.bullywiihacks.address.porter.wii.utilities.general;

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class ByteUtilities
{
	public static String toHexadecimal(byte[] sourceArray)
	{
		StringBuilder hexadecimalRepresentation = new StringBuilder();

		for (byte singleByte : sourceArray)
		{
			String hexadecimalByte = String.format("%02X", singleByte);

			hexadecimalRepresentation.append(hexadecimalByte);
		}

		return hexadecimalRepresentation.toString();
	}

	public static byte[] getIntegerValue(int fileOffset, byte[] sourceArray)
	{
		byte[] integerValue = new byte[4];

		System.arraycopy(sourceArray, fileOffset, integerValue, 0, integerValue.length);

		return integerValue;
	}

	public static int getFirstMatchIndex(byte[] sourceArray,
			byte[] searchTemplate)
	{
		for (int targetArrayIndex = 0; targetArrayIndex < sourceArray.length; targetArrayIndex++)
		{
			boolean matchingValueFound = true;

			for (int searchTemplateIndex = 0; searchTemplateIndex < searchTemplate.length; searchTemplateIndex++)
			{
				if (sourceArray[targetArrayIndex + searchTemplateIndex] != searchTemplate[searchTemplateIndex])
				{
					matchingValueFound = false;
				}
			}

			if (matchingValueFound)
			{
				return targetArrayIndex;
			} else if (targetArrayIndex > sourceArray.length
					- searchTemplate.length - 1)
			{
				break;
			}
		}

		return -1;
	}

	public static boolean containsPossibleCodeAddress(byte[] byteArray)
	{
		ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);

		try
		{
			while (true)
			{
				int currentValue = byteBuffer.getInt();

				if (GeckoCode.isPossibleCodeAddress(currentValue))
				{
					return true;
				}
			}
		} catch (BufferUnderflowException bufferUnderflow)
		{
			return false;
		}
	}

	public static boolean containsHighAssemblyOffsets(byte[] byteArray)
	{
		ByteBuffer byteBuffer = ByteBuffer.wrap(byteArray);

		byteBuffer.getShort();

		try
		{
			while (true)
			{
				if(!ShortUtilities.isLowAssemblyOffset(byteBuffer.getShort()))
				{
					return true;
				}

				byteBuffer.getShort();
			}
		} catch (BufferUnderflowException bufferUnderflow)
		{
			return false;
		}
	}

	public static boolean isAllZeros(byte[] byteArray)
	{
		for (byte b : byteArray)
		{
			if (b != (byte) 0)
			{
				return false;
			}
		}

		return true;
	}
}