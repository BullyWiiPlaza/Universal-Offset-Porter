package com.bullywiihacks.address.porter.wii;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class CodeTypes
{
	private String[] supportedCodeTypes;

	public CodeTypes()
	{
		supportedCodeTypes = new String[]{"00", "01", "02", "03", "04", "05", "06",
				"07", "08", "09", "C2", "C3", "F2", "F3", "20", "21", "22",
				"23", "24", "25", "26", "27", "28", "29", "2A", "2B", "2C",
				"2D", "2E", "2F"};
	}

	public List<String> getMem80CodeTypes()
	{
		return getEvery2ndElement(0);
	}

	public List<String> getMem81CodeTypes()
	{
		return getEvery2ndElement(1);
	}

	public List<String> getStringWriteCodeTypes()
	{
		List<String> stringWriteCodeTypes = new ArrayList<>();

		stringWriteCodeTypes.addAll(Arrays.asList(supportedCodeTypes).subList(6, 14));

		return stringWriteCodeTypes;
	}

	private List<String> getEvery2ndElement(int startingIndex)
	{
		List<String> arrayList = new ArrayList<>();

		for (; startingIndex < supportedCodeTypes.length; startingIndex = startingIndex + 2)
		{
			arrayList.add(supportedCodeTypes[startingIndex]);
		}

		return arrayList;
	}
}