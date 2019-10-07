package com.bullywiihacks.address.porter.wiiu.graphical_interface;

import lombok.val;

import java.awt.*;
import java.io.InputStream;
import java.util.Scanner;

import static java.nio.charset.StandardCharsets.UTF_8;

public class WindowUtilities
{
	static void setWindowIconImage(Window window)
	{
		window.setIconImage(Toolkit.getDefaultToolkit().getImage(WindowUtilities.class.getResource("/Icon.jpg")));
	}

	public static String resourceToString(String filePath)
	{
		val inputStream = WindowUtilities.class.getClassLoader().getResourceAsStream(filePath);
		return toString(inputStream);
	}

	// http://stackoverflow.com/a/5445161/3764804
	private static String toString(InputStream inputStream)
	{
		try (val scanner = new Scanner(inputStream, UTF_8.name()).useDelimiter("\\A"))
		{
			return scanner.hasNext() ? scanner.next() : "";
		}
	}
}