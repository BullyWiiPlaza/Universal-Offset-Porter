package com.bullywiihacks.address.porter.wiiu;

public enum SearchDirection
{
	POSITIVE,
	NEGATIVE;

	public SearchDirection invert()
	{
		switch (this)
		{
			case POSITIVE:
				return NEGATIVE;

			case NEGATIVE:
				return POSITIVE;
		}

		throw new IllegalStateException("Unhandled offset direction");
	}

	public int apply(int value, int added)
	{
		switch (this)
		{
			case POSITIVE:
				return value + added;

			case NEGATIVE:
				return value - added;
		}

		throw new IllegalStateException("Unhandled offset direction");
	}
}