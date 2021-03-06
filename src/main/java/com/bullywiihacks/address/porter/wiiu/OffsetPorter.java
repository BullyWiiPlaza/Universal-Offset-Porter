package com.bullywiihacks.address.porter.wiiu;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class OffsetPorter
{
	private static final int STARTING_MATCHING_BYTES_COUNT = 1;
	private static final int STARTING_OFFSET_SHIFT_BYTES = 0;
	public static int MAXIMUM_SEARCH_TEMPLATE_TRIES = 1000;
	public static double DATA_BYTES_UNEQUAL_THRESHOLD = 1 / (double) 20; // Allow some data to differ
	public static double DEFAULT_OFFSET_VARIANCE = 0.05;

	private int offsetAlignment;
	private byte[] sourceMemoryDump;
	private byte[] destinationMemoryDump;
	private int matchingBytesCount;
	private int offsetShiftBytes;
	private int maximumTriesCount;
	private int sourceOffset;
	private boolean isAssembly;
	private double offsetVariancePercentage;

	public OffsetPorter(String sourceMemoryDumpFilePath, String destinationMemoryDumpFilePath, int sourceOffset, double variance) throws IOException
	{
		offsetAlignment = sourceOffset % 4;
		this.sourceOffset = sourceOffset;
		this.offsetVariancePercentage = variance;

		sourceMemoryDump = readBytes(sourceMemoryDumpFilePath);
		destinationMemoryDump = readBytes(destinationMemoryDumpFilePath);

		isAssembly = AssemblyChecker.isAssembly(sourceMemoryDump, sourceOffset, getStartingOffset());

		matchingBytesCount = STARTING_MATCHING_BYTES_COUNT;
		offsetShiftBytes = STARTING_OFFSET_SHIFT_BYTES;
		maximumTriesCount = MAXIMUM_SEARCH_TEMPLATE_TRIES;
	}

	public OffsetPorter(String sourceMemoryDumpFilePath, String destinationMemoryDumpFilePath, int sourceOffset) throws IOException
	{
		this(sourceMemoryDumpFilePath, destinationMemoryDumpFilePath, sourceOffset, DEFAULT_OFFSET_VARIANCE);
	}

	private byte[] readBytes(String sourceMemoryDumpFilePath) throws IOException
	{
		int startOffset = getStartingOffset();
		int endingOffset = getEndingOffset();

		return FileReadingUtilities.readBytes(sourceMemoryDumpFilePath, startOffset, endingOffset);
	}

	private int getEndingOffset()
	{
		int endingOffset = (int) (sourceOffset + sourceOffset * offsetVariancePercentage);
		endingOffset += endingOffset % 4;

		return endingOffset;
	}

	private int getStartingOffset()
	{
		int startingOffset = (int) (sourceOffset - sourceOffset * offsetVariancePercentage);
		startingOffset -= startingOffset % 4;

		return startingOffset;
	}

	/**
	 * Ports the <code>sourceOffset</code> to the destination memory dump
	 *
	 * @return The {@link OffsetPortingReport}
	 */
	public OffsetPortingReport port()
	{
		long startingTime = System.currentTimeMillis();
		System.out.println("Is assembly: " + isAssembly);

		List<Integer> destinationTemplateMatches = new ArrayList<>();
		List<Integer> sourceTemplateMatches = new ArrayList<>();
		int currentTries = 0;
		List<Byte> searchTemplate = null;

		// Iterate till only one search template result is found in both memory dumps
		while (sourceTemplateMatches.size() != 1
				|| destinationTemplateMatches.size() != 1)
		{
			// Start with fresh lists
			sourceTemplateMatches.clear();
			destinationTemplateMatches.clear();

			// Get the current search template
			searchTemplate = getSearchTemplate();

			findTemplateMatches(sourceMemoryDump, sourceTemplateMatches, searchTemplate);
			System.out.println("Ported addresses source: " + sourceTemplateMatches.size());

			if (sourceTemplateMatches.size() == 0)
			{
				handleNoResultsFound(currentTries);
			} else if (sourceTemplateMatches.size() == 1)
			{
				findTemplateMatches(destinationMemoryDump, destinationTemplateMatches, searchTemplate);

				if (destinationTemplateMatches.size() == 0)
				{
					handleNoResultsFound(currentTries);
				}

				System.out.println("Ported addresses destination: " + destinationTemplateMatches.size());
			}

			// Porting failed?
			if (currentTries == maximumTriesCount)
			{
				return null;
			}

			currentTries++;
			System.out.println("Tries: " + currentTries);
		}

		long endingTime = System.currentTimeMillis();
		double timeElapsed = (endingTime - startingTime) / (double) 1000;

		// Return the only result
		int ported = destinationTemplateMatches.get(0);
		MemoryRange memoryRange = new MemoryRange(getStartingOffset(), getEndingOffset());
		OffsetPortingReport offsetPortingReport = new OffsetPortingReport(ported, isAssembly, memoryRange, searchTemplate, offsetShiftBytes, timeElapsed);
		System.out.println("------");
		System.out.println(offsetPortingReport);

		return offsetPortingReport;
	}

	private void findTemplateMatches(byte[] memoryDump,
	                                 List<Integer> templateMatches,
	                                 List<Byte> searchTemplate)
	{
		// Iterate over the entire memory dump
		for (int memoryDumpIndex = offsetAlignment;
		     memoryDumpIndex < memoryDump.length;
		     memoryDumpIndex += getStepSize())
		{
			addTemplateMatch(searchTemplate, templateMatches, memoryDump, memoryDumpIndex, isAssembly);

			// We can bail out if more than one result has been found
			if (templateMatches.size() > 1)
			{
				// Refine to reduce results
				matchingBytesCount++;
				break;
			}
		}
	}

	private void handleNoResultsFound(int currentTries)
	{
		int maximumTriesMiddle = maximumTriesCount / 2;

		// Reset to go in reverse now
		if (currentTries == maximumTriesMiddle)
		{
			offsetShiftBytes = STARTING_OFFSET_SHIFT_BYTES;
		}

		if (currentTries < maximumTriesMiddle)
		{
			// Go forward
			offsetShiftBytes += getStepSize();
		} else
		{
			// Go backwards
			offsetShiftBytes -= getStepSize();
		}

		System.out.println("Shift Bytes: " + Integer.toHexString(offsetShiftBytes).toUpperCase());

		// Also reset the matching assembly instructions count to most likely get results again
		matchingBytesCount = STARTING_MATCHING_BYTES_COUNT;
	}

	private void addTemplateMatch(List<Byte> searchTemplate,
	                              List<Integer> searchTemplateMatches,
	                              byte[] memoryDump,
	                              int startingIndex,
	                              boolean isAssembly)
	{
		int byteEqualFailuresCount = 0;

		for (int searchTemplateIndex = 0;
		     searchTemplateIndex < searchTemplate.size();
		     searchTemplateIndex++)
		{
			int currentIndex = startingIndex + searchTemplateIndex * getStepSize();

			if (currentIndex > memoryDump.length - 1)
			{
				return;
			}

			byte memoryDumpByte = memoryDump[currentIndex];
			byte searchTemplateByte = searchTemplate.get(searchTemplateIndex);

			if (memoryDumpByte != searchTemplateByte)
			{
				byteEqualFailuresCount++;
				double failureProbability = (byteEqualFailuresCount / (double) matchingBytesCount);
				boolean equalFailuresThresholdExceeded = failureProbability > DATA_BYTES_UNEQUAL_THRESHOLD;

				// Did this template fail to match?
				if (isAssembly || equalFailuresThresholdExceeded)
				{
					return;
				}
			}

			// Result found
			if (searchTemplateIndex == searchTemplate.size() - 1)
			{
				searchTemplateMatches.add(getStartingOffset() + startingIndex - offsetShiftBytes);
			}
		}
	}

	private List<Byte> getSearchTemplate()
	{
		List<Byte> searchTemplate = new ArrayList<>();

		for (int searchTemplateBytesIndex = 0;
		     searchTemplateBytesIndex < matchingBytesCount;
		     searchTemplateBytesIndex++)
		{
			int startingOffset = getStartingOffset();
			int sourceIndex = sourceOffset - startingOffset + offsetShiftBytes + searchTemplateBytesIndex * getStepSize();
			Byte sourceByte = sourceMemoryDump[sourceIndex];
			searchTemplate.add(sourceByte);
		}

		byte[] byteArray = new byte[searchTemplate.size()];
		for (int index = 0; index < searchTemplate.size(); index++)
		{
			byteArray[index] = searchTemplate.get(index);
		}

		System.out.println("Search Template: " + DatatypeConverter.printHexBinary(byteArray));
		System.out.println("Search Template Length: " + Integer.toHexString(searchTemplate.size()).toUpperCase());

		return searchTemplate;
	}

	private int getStepSize()
	{
		return 4;
	}
}