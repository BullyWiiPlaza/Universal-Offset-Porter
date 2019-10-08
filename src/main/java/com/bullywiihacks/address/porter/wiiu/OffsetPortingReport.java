package com.bullywiihacks.address.porter.wiiu;

import lombok.Getter;

import java.util.List;

import static com.bullywiihacks.address.porter.wiiu.Conversions.*;
import static java.lang.System.lineSeparator;

public class OffsetPortingReport
{
	private byte[] searchTemplate;
	private int shiftBytes;

	@Getter
	private int address;

	OffsetPortingReport(int offset, List<Byte> searchTemplate, int shiftBytes)
	{
		this.searchTemplate = toByteArray(searchTemplate);
		this.shiftBytes = shiftBytes;
		this.address = offset;
	}

	@Override
	public String toString()
	{
		return "Search template: " + byteArrayToHexadecimal(searchTemplate) + lineSeparator()
				+ "Offset: " + numberToHexadecimal(shiftBytes) + lineSeparator()
				+ "Destination offset: " + numberToHexadecimal(address);
	}
}