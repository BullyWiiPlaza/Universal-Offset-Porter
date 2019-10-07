package com.bullywiihacks.address.porter.wiiu;

import lombok.val;

import java.io.IOException;
import java.io.RandomAccessFile;

class FileReadingUtilities
{
	static byte[] readBytes(String sourceFilePath, int startingOffset, int endingOffset) throws IOException
	{
		val memoryLength = endingOffset - startingOffset;
		try (val sourceFileReader = new RandomAccessFile(sourceFilePath, "r"))
		{
			sourceFileReader.seek(startingOffset);
			val bytes = new byte[memoryLength];
			sourceFileReader.readFully(bytes);

			return bytes;
		}
	}
}