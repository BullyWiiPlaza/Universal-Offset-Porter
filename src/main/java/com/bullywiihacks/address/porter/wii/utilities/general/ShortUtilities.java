package com.bullywiihacks.address.porter.wii.utilities.general;

public class ShortUtilities
{
	public static boolean isLowAssemblyOffset(short value)
	{
		int intValue = Short.toUnsignedInt(value);
		int maximumOffset = 0x1000;

		return intValue < maximumOffset || intValue > 0xFFFF - maximumOffset;
	}
}