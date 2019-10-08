package com.bullywiihacks.address.porter.wiiu;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static java.lang.Long.toHexString;

@AllArgsConstructor
public class ValueRange
{
	@Getter
	private final long startValue;

	@Getter
	private final long endValue;

	boolean contains(long value)
	{
		return value >= startValue && value < endValue;
	}

	@Override
	public String toString()
	{
		return "[" + toHexString(startValue).toUpperCase()
				+ ", " + toHexString(endValue).toUpperCase() + "]";
	}
}
