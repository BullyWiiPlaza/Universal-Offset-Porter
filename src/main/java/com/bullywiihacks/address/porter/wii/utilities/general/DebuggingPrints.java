package com.bullywiihacks.address.porter.wii.utilities.general;

public class DebuggingPrints
{
	private static boolean debugging = true;

	public static void printLn(String line)
	{
		if (debugging)
		{
			System.out.println(line);
		}
	}

	public static void printLn(int number)
	{
		if (debugging)
		{
			System.out.println(number);
		}
	}

	public static void printLn()
	{
		if (debugging)
		{
			System.out.println();
		}
	}

	public static void print(String text)
	{
		if (debugging)
		{
			System.out.print(text);
		}
	}

	public static void print(int number)
	{
		if (debugging)
		{
			System.out.print(number);
		}
	}
}