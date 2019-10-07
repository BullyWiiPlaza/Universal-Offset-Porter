package com.bullywiihacks.address.porter.wiiu;

import lombok.Getter;
import lombok.val;

import javax.xml.bind.DatatypeConverter;
import java.util.List;

import static java.lang.Long.toHexString;
import static java.lang.Math.min;
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

	@Override
	public String toString()
	{
		val searchTemplateString = DatatypeConverter.printHexBinary(searchTemplate);
		val portedOffsetString = address == FAILED_ADDRESS ? "FAILED" : toHexString(address).toUpperCase();

		return "Seconds Taken: " + secondsElapsed + lineSeparator()
				+ "Assembly Offset: " + isAssembly + lineSeparator()
				+ "Last Search Template: " + searchTemplateString.substring(0,
				min(30, searchTemplateString.length()))
				+ (searchTemplateString.length() > 30 ? " [...]" : "") + lineSeparator()
				+ "Source Offset Shift Bytes: " + toHexString(shiftBytes).toUpperCase() + lineSeparator()
				+ "Scanned Memory Range: " + memoryRange.toString() + lineSeparator()
				+ "Ported Offset: " + portedOffsetString;
	}
}