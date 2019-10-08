package com.bullywiihacks.address.porter.wiiu;

import lombok.Getter;
import lombok.val;

import java.util.List;

import static com.bullywiihacks.address.porter.wiiu.Conversions.*;
import static com.bullywiihacks.address.porter.wiiu.Conversions.numberToHexadecimal;
import static java.lang.Long.toHexString;
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

	private final MemoryRange searchedMemoryRange;

	OffsetPortingReport(int offset,
	                    boolean isAssembly,
	                    MemoryRange searchedMemoryRange,
	                    List<Byte> searchTemplate,
	                    int shiftBytes)
	{
		this.isAssembly = isAssembly;
		this.searchTemplate = toByteArray(searchTemplate);
		this.shiftBytes = shiftBytes;
		this.address = offset;
		this.searchedMemoryRange = searchedMemoryRange;
	}

	@Override
	public String toString()
	{
		val searchTemplateString = byteArrayToHexadecimal(searchTemplate).toUpperCase();
		val portedOffsetString = address == FAILED_ADDRESS ? "FAILED"
				: "0x" + toHexString(address).toUpperCase();
		val hexadecimalOffset = numberToHexadecimal(shiftBytes);

		return "Memory range: " + searchedMemoryRange.toString() + lineSeparator()
				+ "Search template: " + searchTemplateString + lineSeparator()
				+ "Offset: " + hexadecimalOffset + lineSeparator()
				+ "Destination offset: " + portedOffsetString;
	}
}