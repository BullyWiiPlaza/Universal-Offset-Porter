package com.bullywiihacks.address.porter.wiiu;

import java.io.IOException;
import java.io.RandomAccessFile;

public class FileReadingUtilities
{
	public static byte[] readBytes(String sourceFilePath, int startingOffset, int endingOffset) throws IOException
	{
		int memoryLength = endingOffset - startingOffset;

		try (RandomAccessFile sourceFileReader = new RandomAccessFile(sourceFilePath, "r"))
		{
			sourceFileReader.seek(startingOffset);
			byte[] bytes = new byte[memoryLength];
			sourceFileReader.readFully(bytes);

			return bytes;
		}
	}
}