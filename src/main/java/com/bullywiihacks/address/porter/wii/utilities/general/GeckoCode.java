package com.bullywiihacks.address.porter.wii.utilities.general;

import java.util.Arrays;

public class GeckoCode
{
	public static boolean isPossibleCodeAddress(int value)
	{
		return value < 0x81800000 || value >= 0x90000000 && value < 0x93800000;
	}

	public static boolean isAssembly(String codeType)
	{
		return Arrays.asList(new String[] {"C2", "C3", "F2", "F3"}).contains(codeType);
	}
}