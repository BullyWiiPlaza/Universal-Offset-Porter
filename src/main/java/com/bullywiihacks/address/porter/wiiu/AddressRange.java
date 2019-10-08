package com.bullywiihacks.address.porter.wiiu;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static java.lang.Long.toHexString;

@AllArgsConstructor
public class AddressRange
{
	@Getter
	private final long startAddress;

	@Getter
	private final long endAddress;

	boolean contains(long value)
	{
		return value >= startAddress && value < endAddress;
	}

	@Override
	public String toString()
	{
		return "[" + toHexString(startAddress).toUpperCase()
				+ ", " + toHexString(endAddress).toUpperCase() + "]";
	}
}
