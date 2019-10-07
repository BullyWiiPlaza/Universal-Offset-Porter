package com.bullywiihacks.address.porter.wiiu;

import lombok.Getter;
import lombok.val;

import java.util.List;

import static java.lang.Long.toHexString;
import static java.lang.Math.min;
import static java.lang.String.*;
import static java.lang.System.lineSeparator;

public class OffsetPortingReport
{
	public static final int FAILED_ADDRESS = -1;

	@Getter
	private boolean isAssembly;

	private byte[] searchTemplate;
	private int shiftBytes;

	@Getter
	private int address;

	private final MemoryRange memoryRange;
	private double secondsElapsed;

	private static final int MAXIMUM_SEARCH_TEMPLATE_LENGTH_CHARACTERS = 30;
	private static final double MAXIMUM_SEARCH_TEMPLATE_LENGTH_FORMATTED = MAXIMUM_SEARCH_TEMPLATE_LENGTH_CHARACTERS
			+ (MAXIMUM_SEARCH_TEMPLATE_LENGTH_CHARACTERS * 0.5);

	OffsetPortingReport(int offset,
	                    boolean isAssembly,
	                    MemoryRange memoryRange,
	                    List<Byte> searchTemplate,
	                    int shiftBytes,
	                    double secondsElapsed)
	{
		this.isAssembly = isAssembly;
		this.searchTemplate = toByteArray(searchTemplate);
		this.shiftBytes = shiftBytes;
		this.address = offset;
		this.memoryRange = memoryRange;
		this.secondsElapsed = secondsElapsed;
	}

	private byte[] toByteArray(List<Byte> bytesList)
	{
		byte[] byteArray = new byte[bytesList.size()];
		for (int index = 0; index < bytesList.size(); index++)
		{
			byteArray[index] = bytesList.get(index);
		}

		return byteArray;
	}

	private static String byteArrayToHex(byte[] byteArray)
	{
		val capacity = byteArray.length * 2;
		val stringBuilder = new StringBuilder(capacity);
		for (val singleByte : byteArray)
		{
			val format = format("%02x ", singleByte);
			stringBuilder.append(format);
		}

		return stringBuilder.toString().trim();
	}

	@Override
	public String toString()
	{
		val searchTemplateString = byteArrayToHex(searchTemplate).toUpperCase();
		val portedOffsetString = address == FAILED_ADDRESS ? "FAILED"
				: "0x" + toHexString(address).toUpperCase();

		return "Seconds taken: " + secondsElapsed + lineSeparator()
				+ "Is assembly offset: " + isAssembly + lineSeparator()
				+ "Search template: " + searchTemplateString.substring(0,
				(int) min(MAXIMUM_SEARCH_TEMPLATE_LENGTH_FORMATTED, searchTemplateString.length()))
				+ (searchTemplateString.length() > MAXIMUM_SEARCH_TEMPLATE_LENGTH_FORMATTED
				? " [...]" : "") + lineSeparator()
				+ "Offset: 0x" + toHexString(shiftBytes).toUpperCase() + lineSeparator()
				+ "Memory Range: " + memoryRange.toString() + lineSeparator()
				+ "Ported offset: " + portedOffsetString;
	}
}