package com.bullywiihacks.address.porter.wiiu;

import lombok.val;
import org.apache.logging.log4j.Logger;

import static org.apache.logging.log4j.LogManager.getLogger;

public class AssemblyChecker
{
	private static final Logger LOGGER = getLogger();

	public static double ASSEMBLY_NULL_BYTES_RATIO_THRESHOLD = 0.20;
	private static final int ASSEMBLY_NULL_BYTES_CHECK_SIZE = 0x100;

	/**
	 * Detects whether the <code>offset</code> is an assembly instruction or not
	 *
	 * @return Whether this is an assembly instruction
	 */
	static boolean isAssembly(byte[] sourceMemoryDump, int offset, int startingOffset)
	{
		var nullBytesCount = 0;
		val checkSizeShift = ASSEMBLY_NULL_BYTES_CHECK_SIZE / 2;

		// Check the interval around the offset for null bytes
		for (var sourceBytesIndex = Math.max(offset - startingOffset - checkSizeShift, 0);
		     sourceBytesIndex < Math.min(offset - startingOffset + checkSizeShift, sourceMemoryDump.length);
		     sourceBytesIndex++)
		{
			val sourceByte = sourceMemoryDump[sourceBytesIndex];

			if (sourceByte == 0x00)
			{
				nullBytesCount++;
			}
		}

		val nullBytesRatio = nullBytesCount / (double) ASSEMBLY_NULL_BYTES_CHECK_SIZE;
		LOGGER.info("Null-bytes ratio: " + nullBytesRatio);
		return nullBytesRatio < ASSEMBLY_NULL_BYTES_RATIO_THRESHOLD;
	}
}