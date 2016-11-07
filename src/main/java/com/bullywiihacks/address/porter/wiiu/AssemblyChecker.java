package com.bullywiihacks.address.porter.wiiu;

public class AssemblyChecker
{
	public static double ASSEMBLY_NULL_BYTES_RATIO_THRESHOLD = 0.20;
	private static final int ASSEMBLY_NULL_BYTES_CHECK_SIZE = 0x100;

	/**
	 * Detects whether the <code>offset</code> is an assembly instruction or not
	 *
	 * @param offset The offset to check
	 * @return Whether this is an assembly instruction
	 */
	public static boolean isAssembly(byte[] sourceMemoryDump, int offset, int startingOffset)
	{
		int nullBytesCount = 0;
		int checkSizeShift = ASSEMBLY_NULL_BYTES_CHECK_SIZE / 2;

		// Check the interval around the offset for null bytes
		for (int sourceBytesIndex = Math.max(offset - startingOffset - checkSizeShift, 0);
		     sourceBytesIndex < Math.min(offset - startingOffset + checkSizeShift, sourceMemoryDump.length);
		     sourceBytesIndex++)
		{
			byte sourceByte = sourceMemoryDump[sourceBytesIndex];

			if (sourceByte == 0x00)
			{
				nullBytesCount++;
			}
		}

		double nullBytesRatio = nullBytesCount / (double) ASSEMBLY_NULL_BYTES_CHECK_SIZE;
		System.out.println("Ratio: " + nullBytesRatio);
		return nullBytesRatio < ASSEMBLY_NULL_BYTES_RATIO_THRESHOLD;
	}
}