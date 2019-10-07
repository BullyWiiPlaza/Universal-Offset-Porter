package com.bullywiihacks.address.porter.wiiu;

import com.bullywiihacks.address.porter.wiiu.graphical_interface.UniversalOffsetPorterGUI;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import lombok.var;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import static com.bullywiihacks.address.porter.wiiu.AssemblyChecker.isAssembly;
import static java.lang.Integer.toHexString;
import static java.lang.System.currentTimeMillis;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.size;
import static org.apache.commons.codec.binary.Hex.encodeHexString;
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

	private int startingOffset;
	private int endingOffset;

	@Getter
	@Setter
	private double searchTemplateBytesUnequalThreshold = DATA_BYTES_UNEQUAL_THRESHOLD;

	@Getter
	@Setter
	private int stepSize = 4;

	private int offsetAlignment;
	private final String sourceMemoryDumpFilePath;
	private final String destinationMemoryDumpFilePath;
	private byte[] sourceMemoryDump;
	private int matchingBytesCount;
	private int offsetShiftBytes;
	private int maximumTriesCount;
	private int sourceOffset;
	private boolean isAssembly;
	private final boolean readAllBytes;

	public OffsetPorter(String sourceMemoryDumpFilePath, String destinationMemoryDumpFilePath,
	                    int sourceOffset, double variance, boolean readAllBytes) throws IOException
	{
		offsetAlignment = sourceOffset % 4;
		this.sourceOffset = sourceOffset;

		this.sourceMemoryDumpFilePath = sourceMemoryDumpFilePath;
		this.destinationMemoryDumpFilePath = destinationMemoryDumpFilePath;

		if (readAllBytes)
		{
			this.startingOffset = 0;
			this.endingOffset = (int) size(Paths.get(sourceMemoryDumpFilePath));
		} else
		{
			this.startingOffset = (int) (sourceOffset - sourceOffset * variance);
			this.startingOffset -= this.startingOffset % 4;

			this.endingOffset = (int) (sourceOffset + sourceOffset * variance);
			this.endingOffset += this.endingOffset % 4;
		}

		this.readAllBytes = readAllBytes;

		matchingBytesCount = STARTING_MATCHING_BYTES_COUNT;
		offsetShiftBytes = STARTING_OFFSET_SHIFT_BYTES;
		maximumTriesCount = MAXIMUM_SEARCH_TEMPLATE_TRIES;
	}

	public OffsetPorter(String sourceMemoryDumpFilePath, String destinationMemoryDumpFilePath,
	                    int sourceOffset) throws IOException
	{
		this(sourceMemoryDumpFilePath, destinationMemoryDumpFilePath, sourceOffset, DEFAULT_OFFSET_VARIANCE, false);
	}

	private byte[] readBytes(String sourceMemoryDumpFilePath) throws IOException
	{
		return FileReadingUtilities.readBytes(sourceMemoryDumpFilePath, startingOffset, endingOffset);
	}

	/**
	 * Ports the <code>sourceOffset</code> to the destination memory dump
	 *
	 * @return The {@link OffsetPortingReport}
	 */
	public OffsetPortingReport port() throws IOException
	{
		val startingTime = currentTimeMillis();

		LOGGER.info("Porting source offset 0x"
				+ Long.toHexString(sourceOffset).toUpperCase() + " from "
				+ getName(sourceMemoryDumpFilePath) + " to " + getName(destinationMemoryDumpFilePath) + "...");

		sourceMemoryDump = readAllBytes ? readAllBytes(Paths.get(sourceMemoryDumpFilePath))
				: readBytes(sourceMemoryDumpFilePath);
		val destinationMemoryDump = readAllBytes ? readAllBytes(Paths.get(destinationMemoryDumpFilePath))
				: readBytes(destinationMemoryDumpFilePath);

		isAssembly = isAssembly(sourceMemoryDump, sourceOffset, startingOffset);
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

		val endingTime = currentTimeMillis();
		val timeElapsed = (endingTime - startingTime) / (double) 1000;

		// Return the only result
		val ported = destinationTemplateMatches.size() > 0 ? destinationTemplateMatches.get(0) : -1;
		val memoryRange = new MemoryRange(startingOffset, endingOffset);
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
		for (var memoryDumpIndex = offsetAlignment;
		     memoryDumpIndex < memoryDump.length;
		     memoryDumpIndex += stepSize)
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
		val maximumTriesMiddle = maximumTriesCount / 2;

		// Reset to go in reverse now
		if (currentTries == maximumTriesMiddle)
		{
			offsetShiftBytes = STARTING_OFFSET_SHIFT_BYTES;
		}

		if (currentTries < maximumTriesMiddle)
		{
			// Go forward
			offsetShiftBytes += stepSize;
		} else
		{
			// Go backwards
			offsetShiftBytes -= stepSize;
		}

		LOGGER.info("Shift bytes: " + toHexString(offsetShiftBytes).toUpperCase());

		// Also reset the matching assembly instructions count to most likely get results again
		matchingBytesCount = STARTING_MATCHING_BYTES_COUNT;
	}

	private void addTemplateMatch(List<Byte> searchTemplate, List<Integer> searchTemplateMatches,
	                              byte[] memoryDump, int startingIndex, boolean isAssembly)
	{
		var byteEqualFailuresCount = 0;

		for (var searchTemplateIndex = 0;
		     searchTemplateIndex < searchTemplate.size();
		     searchTemplateIndex++)
		{
			val currentIndex = startingIndex + searchTemplateIndex * stepSize;

			if (currentIndex > memoryDump.length - 1)
			{
				return;
			}

			val memoryDumpByte = memoryDump[currentIndex];
			val searchTemplateByte = searchTemplate.get(searchTemplateIndex);

			val failureProbability = (byteEqualFailuresCount / (double) matchingBytesCount);

			if (memoryDumpByte != searchTemplateByte)
			{
				byteEqualFailuresCount++;
				val equalFailuresThresholdExceeded = failureProbability > searchTemplateBytesUnequalThreshold;

				// Did this template fail to match?
				if (isAssembly || equalFailuresThresholdExceeded)
				{
					return;
				}
			}

			// Result found
			if (searchTemplateIndex == searchTemplate.size() - 1)
			{
				LOGGER.info("Failure probability: " + failureProbability);
				val searchTemplateMatch = startingOffset + startingIndex - offsetShiftBytes;
				searchTemplateMatches.add(searchTemplateMatch);
			}
		}
	}

	private List<Byte> getSearchTemplate()
	{
		val searchTemplate = new ArrayList<Byte>();

		for (var searchTemplateBytesIndex = 0;
		     searchTemplateBytesIndex < matchingBytesCount;
		     searchTemplateBytesIndex++)
		{
			val sourceIndex = sourceOffset - startingOffset + offsetShiftBytes + searchTemplateBytesIndex * stepSize;

			// Not enough bytes left
			if (sourceIndex >= sourceMemoryDump.length - 1)
			{
				return null;
			}

			val sourceByte = sourceMemoryDump[sourceIndex];
			searchTemplate.add(sourceByte);
		}

		val byteArray = new byte[searchTemplate.size()];
		for (var searchTemplateIndex = 0; searchTemplateIndex < searchTemplate.size(); searchTemplateIndex++)
		{
			byteArray[searchTemplateIndex] = searchTemplate.get(searchTemplateIndex);
		}

		LOGGER.info("Search template: " + encodeHexString(byteArray).toUpperCase());
		LOGGER.info("Search template length: " + searchTemplate.size());

		return searchTemplate;
	}
}