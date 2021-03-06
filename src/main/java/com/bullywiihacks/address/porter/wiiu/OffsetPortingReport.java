package com.bullywiihacks.address.porter.wiiu;

import javax.xml.bind.DatatypeConverter;
import java.util.List;

public class OffsetPortingReport
{
	private boolean isAssembly;
	private byte[] searchTemplate;
	private int shiftBytes;
	private int address;
	private final MemoryRange memoryRange;
	private double secondsElapsed;

	public OffsetPortingReport(int offset,
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

	public int getAddress()
	{
		return address;
	}

	public byte[] getSearchTemplate()
	{
		return searchTemplate;
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

	public boolean isAssembly()
	{
		return isAssembly;
	}

	public int getShiftBytes()
	{
		return shiftBytes;
	}

	public MemoryRange getMemoryRange()
	{
		return memoryRange;
	}

	@Override
	public String toString()
	{
		return "Seconds Taken: " + secondsElapsed + System.lineSeparator()
				+ "Assembly Offset: " + isAssembly + System.lineSeparator()
				+ "Search Template Found: " + DatatypeConverter.printHexBinary(searchTemplate) + System.lineSeparator()
				+ "Source Offset Shift Bytes: " + Integer.toHexString(shiftBytes).toUpperCase() + System.lineSeparator()
				+ "Scanned Memory Range: " + memoryRange.toString() + System.lineSeparator()
				+ "Ported Offset: " + Integer.toHexString(address).toUpperCase();
	}
}