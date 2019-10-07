package com.bullywiihacks.address.porter.wiiu;

import com.bullywiihacks.address.porter.wiiu.graphical_interface.UniversalOffsetPorterGUI;
import lombok.val;
import lombok.var;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.bullywiihacks.address.porter.wiiu.AssemblyChecker.isAssembly;
import static java.lang.Integer.toHexString;
import static javax.xml.bind.DatatypeConverter.printHexBinary;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.logging.log4j.LogManager.getLogger;

public class OffsetPorter
{
	private static final Logger LOGGER = getLogger();

	private static final int STARTING_MATCHING_BYTES_COUNT = 1;
	private static final int STARTING_OFFSET_SHIFT_BYTES = 0;
	public static int MAXIMUM_SEARCH_TEMPLATE_TRIES = 1000;
	public static double DATA_BYTES_UNEQUAL_THRESHOLD = 1 / (double) 20; // Allow some data to differ
	public static double DEFAULT_OFFSET_VARIANCE = 0.05;

	private int offsetAlignment;
	private final String sourceMemoryDumpFilePath;
	private final String destinationMemoryDumpFilePath;
	private byte[] sourceMemoryDump;
	private byte[] destinationMemoryDump;
	private int matchingBytesCount;
	private int offsetShiftBytes;
	private int maximumTriesCount;
	private int sourceOffset;
	private boolean isAssembly;
	private double offsetVariancePercentage;

	public OffsetPorter(String sourceMemoryDumpFilePath, String destinationMemoryDumpFilePath,
	                    int sourceOffset, double variance) throws IOException
	{
		offsetAlignment = sourceOffset % 4;
		this.sourceOffset = sourceOffset;
		this.offsetVariancePercentage = variance;

		this.sourceMemoryDumpFilePath = sourceMemoryDumpFilePath;
		this.destinationMemoryDumpFilePath = destinationMemoryDumpFilePath;

		sourceMemoryDump = readBytes(sourceMemoryDumpFilePath);
		destinationMemoryDump = readBytes(destinationMemoryDumpFilePath);

		isAssembly = isAssembly(sourceMemoryDump, sourceOffset, getStartingOffset());

		matchingBytesCount = STARTING_MATCHING_BYTES_COUNT;
		offsetShiftBytes = STARTING_OFFSET_SHIFT_BYTES;
		maximumTriesCount = MAXIMUM_SEARCH_TEMPLATE_TRIES;
	}

	public OffsetPorter(String sourceMemoryDumpFilePath, String destinationMemoryDumpFilePath,
	                    int sourceOffset) throws IOException
	{
		this(sourceMemoryDumpFilePath, destinationMemoryDumpFilePath, sourceOffset, DEFAULT_OFFSET_VARIANCE);
	}

	private byte[] readBytes(String sourceMemoryDumpFilePath) throws IOException
	{
		val startOffset = getStartingOffset();
		val endingOffset = getEndingOffset();

		return FileReadingUtilities.readBytes(sourceMemoryDumpFilePath, startOffset, endingOffset);
	}

	private int getEndingOffset()
	{
		var endingOffset = (int) (sourceOffset + sourceOffset * offsetVariancePercentage);
		endingOffset += endingOffset % 4;

		return endingOffset;
	}

	private int getStartingOffset()
	{
		var startingOffset = (int) (sourceOffset - sourceOffset * offsetVariancePercentage);
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
		LOGGER.info("Porting source offset 0x"
				+ Long.toHexString(sourceOffset).toUpperCase() + " from "
				+ getName(sourceMemoryDumpFilePath) + " to " + getName(destinationMemoryDumpFilePath) + "...");

		val startingTime = System.currentTimeMillis();
		LOGGER.info("Is assembly: " + isAssembly);

		val destinationTemplateMatches = new ArrayList<Integer>();
		var sourceTemplateMatches = new ArrayList<Integer>();
		var currentTries = 0;
		List<Byte> searchTemplate = null;

		// Iterate till only one search template result is found in both memory dumps
		while (sourceTemplateMatches.size() != 1
				|| destinationTemplateMatches.size() != 1)
		{
			// Start with fresh lists
			sourceTemplateMatches.clear();
			destinationTemplateMatches.clear();

			// Get the current search template
			val currentSearchTemplate = getSearchTemplate();

			if (currentSearchTemplate == null)
			{
				break;
			}

			searchTemplate = currentSearchTemplate;

			findTemplateMatches(sourceMemoryDump, sourceTemplateMatches, searchTemplate);
			LOGGER.info("Source template matches count: " + sourceTemplateMatches.size());

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

				LOGGER.info("Destination template matches count: " + destinationTemplateMatches.size());
			}


			// Porting failed?
			if (currentTries == maximumTriesCount)
			{
				break;
			}

			currentTries++;
			LOGGER.info("Amount of tries: " + currentTries);

			if (UniversalOffsetPorterGUI.getInstance().isPortingCanceled())
			{
				return null;
			}
		}

		val endingTime = System.currentTimeMillis();
		val timeElapsed = (endingTime - startingTime) / (double) 1000;

		// Return the only result
		val ported = destinationTemplateMatches.size() > 0 ? destinationTemplateMatches.get(0) : -1;
		val memoryRange = new MemoryRange(getStartingOffset(), getEndingOffset());
		val offsetPortingReport = new OffsetPortingReport(ported, isAssembly, memoryRange,
				searchTemplate, offsetShiftBytes, timeElapsed);
		LOGGER.info("------");
		LOGGER.info(offsetPortingReport);

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

		LOGGER.info("Shift Bytes: " + toHexString(offsetShiftBytes).toUpperCase());

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
			int sourceIndex = sourceOffset - startingOffset + offsetShiftBytes
					+ searchTemplateBytesIndex * getStepSize();

			// Not enough bytes left
			if (sourceIndex >= sourceMemoryDump.length - 1)
			{
				return null;
			}

			Byte sourceByte = sourceMemoryDump[sourceIndex];
			searchTemplate.add(sourceByte);
		}

		byte[] byteArray = new byte[searchTemplate.size()];
		for (int index = 0; index < searchTemplate.size(); index++)
		{
			byteArray[index] = searchTemplate.get(index);
		}

		LOGGER.info("Search Template: " + printHexBinary(byteArray));
		LOGGER.info("Search Template Length: " + toHexString(searchTemplate.size()).toUpperCase());

		return searchTemplate;
	}

	private int getStepSize()
	{
		return 4;
	}
}