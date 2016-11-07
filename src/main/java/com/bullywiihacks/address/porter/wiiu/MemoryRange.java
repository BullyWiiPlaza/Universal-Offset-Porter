package com.bullywiihacks.address.porter.wiiu;

public class MemoryRange
{
	private int startingOffset;
	private int endingOffset;

	public MemoryRange(int startingOffset, int endingOffset)
	{
		this.startingOffset = startingOffset;
		this.endingOffset = endingOffset;
	}

	public int getEndingOffset()
	{
		return endingOffset;
	}

	public int getStartingOffset()
	{
		return startingOffset;
	}

	@Override
	public String toString()
	{
		return "[" + Integer.toHexString(startingOffset).toUpperCase()
				+ ", " + Integer.toHexString(endingOffset).toUpperCase() + "]";
	}
}