package com.bullywiihacks.address.porter.wiiu;

import lombok.AllArgsConstructor;

import static java.lang.Long.*;

@AllArgsConstructor
public class MemoryRange
{
	private final int startingOffset;
	private final int endingOffset;

	@Override
	public String toString()
	{
		return "[0x" + toHexString(startingOffset).toUpperCase()
				+ ", 0x" + toHexString(endingOffset).toUpperCase() + "]";
	}
}