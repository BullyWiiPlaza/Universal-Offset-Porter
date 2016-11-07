package com.bullywiihacks.address.porter.wiiu.graphical_interface;

import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Scanner;

public class WindowUtilities
{
	public static void setIconImage(Window window)
	{
		window.setIconImage(Toolkit.getDefaultToolkit().getImage(WindowUtilities.class.getResource("/Icon.jpg")));
	}

	public static String resourceToString(String filePath) throws IOException, URISyntaxException
	{
		InputStream inputStream = WindowUtilities.class.getClassLoader().getResourceAsStream(filePath);
		return toString(inputStream);
	}

	// http://stackoverflow.com/a/5445161/3764804
	private static String toString(InputStream inputStream)
	{
		try (Scanner scanner = new Scanner(inputStream, "UTF-8").useDelimiter("\\A"))
		{
			return scanner.hasNext() ? scanner.next() : "";
		}
	}
}