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
import static java.lang.System.lineSeparator;
import static java.nio.file.Files.readAllBytes;
import static java.nio.file.Files.size;
import static org.apache.commons.codec.binary.Hex.encodeHexString;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.logging.log4j.LogManager.getLogger;

public class OffsetPorter
{
	private static final Logger LOGGER = getLogger();

	// TODO: Only take the same destination addresses into account
	// The minimum amount of accepted search templates before the port is considered successful
	@Getter
	@Setter
	private int minimumAcceptedSearchTemplatesCount = 1;

	// The search template bytes count to start at
	@Getter
	@Setter
	private int startingMatchingBytesCount = 4;

	// The search template offset to start at
	@Getter
	@Setter
	private int startingSearchTemplateOffset = 0;

	// How many search template tries in total before giving up
	@Getter
	@Setter
	private int maximumSearchTemplateTries = 10_000;

	// How much percent of the search template may be wrong to still be considered as acceptable
	@Getter
	@Setter
	private double dataBytesUnequalThreshold = 1 / (double) 20;

	// Whether the entire memory dump should be read for processing
	@Getter
	@Setter
	private boolean readAllBytes = false;

	// The interval percentage of file offsets around the starting offset to read from for the source and destination
	@Getter
	@Setter
	private double fileOffsetsIntervalPercentage = 0.05;

	// TODO: Mark the placeholders in the search template with ??
	// The percentage of how much in the search template may differ without failing the comparison
	@Getter
	@Setter
	private double searchTemplateBytesUnequalThreshold = dataBytesUnequalThreshold;

	// The step size of progressing the search template bytes
	@Getter
	@Setter
	private int stepSize = 1;

	private int startingOffset;
	private int endingOffset;
	private int offsetAlignment;
	private final String sourceMemoryDumpFilePath;
	private final String destinationMemoryDumpFilePath;
	private byte[] sourceMemoryDump;
	private int matchingBytesCount;
	private int offsetShiftBytes;
	private int maximumTriesCount;
	private int sourceOffset;
	private boolean isAssembly;

	public OffsetPorter(String sourceMemoryDumpFilePath, String destinationMemoryDumpFilePath,
	                    int sourceOffset)
	{
		this.sourceMemoryDumpFilePath = sourceMemoryDumpFilePath;
		this.destinationMemoryDumpFilePath = destinationMemoryDumpFilePath;
		this.sourceOffset = sourceOffset;
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

		matchingBytesCount = startingMatchingBytesCount;
		offsetShiftBytes = startingSearchTemplateOffset;
		maximumTriesCount = maximumSearchTemplateTries;

		offsetAlignment = sourceOffset % stepSize;

		if (readAllBytes)
		{
			this.startingOffset = 0;
			this.endingOffset = (int) size(Paths.get(sourceMemoryDumpFilePath));
		} else
		{
			this.startingOffset = (int) (sourceOffset - sourceOffset * fileOffsetsIntervalPercentage);
			this.startingOffset -= this.startingOffset % stepSize;

			this.endingOffset = (int) (sourceOffset + sourceOffset * fileOffsetsIntervalPercentage);
			this.endingOffset += this.endingOffset % stepSize;
		}

		LOGGER.info("Porting source offset 0x"
				+ Long.toHexString(sourceOffset).toUpperCase() + lineSeparator() + " from "
				+ getName(sourceMemoryDumpFilePath) + " to " + getName(destinationMemoryDumpFilePath) + "...");

		sourceMemoryDump = readAllBytes ? readAllBytes(Paths.get(sourceMemoryDumpFilePath))
				: readBytes(sourceMemoryDumpFilePath);
		val destinationMemoryDump = readAllBytes ? readAllBytes(Paths.get(destinationMemoryDumpFilePath))
				: readBytes(destinationMemoryDumpFilePath);

		isAssembly = isAssembly(sourceMemoryDump, sourceOffset, startingOffset);
		LOGGER.info("Is assembly: " + isAssembly + lineSeparator());

		val destinationTemplateMatches = new ArrayList<Integer>();
		var sourceTemplateMatches = new ArrayList<Integer>();
		var currentTries = 0;
		List<Byte> searchTemplate = null;

		var acceptedSearchTemplateCount = 0;
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

			if (sourceTemplateMatches.size() == 1 && destinationTemplateMatches.size() == 1)
			{
				acceptedSearchTemplateCount++;
				LOGGER.info("Accepted search templates count: " + acceptedSearchTemplateCount);

				if (acceptedSearchTemplateCount < minimumAcceptedSearchTemplatesCount)
				{
					sourceTemplateMatches.clear();
					destinationTemplateMatches.clear();

					handleNoResultsFound(currentTries);
				}
			}

			currentTries++;
			LOGGER.info("Amount of tries: " + currentTries + lineSeparator());

			if (UniversalOffsetPorterGUI.getInstance().isPortingCanceled())
			{
				return null;
			}

			// Porting failed?
			if (currentTries == maximumTriesCount)
			{
				break;
			}
		}

		val endingTime = currentTimeMillis();
		val timeElapsed = (endingTime - startingTime) / (double) 1000;

		// Return the only result
		val ported = destinationTemplateMatches.size() > 0 ? destinationTemplateMatches.get(0) : -1;
		val memoryRange = new MemoryRange(startingOffset, endingOffset);
		val offsetPortingReport = new OffsetPortingReport(ported, isAssembly, memoryRange,
				searchTemplate, -1 * offsetShiftBytes, timeElapsed);
		LOGGER.info(offsetPortingReport);

		return offsetPortingReport;
	}

	private void findTemplateMatches(byte[] memoryDump, List<Integer> templateMatches, List<Byte> searchTemplate)
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
			offsetShiftBytes = startingSearchTemplateOffset;
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
		matchingBytesCount = startingMatchingBytesCount;
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
		val searchTemplateOffset = sourceOffset - startingOffset + offsetShiftBytes;
		LOGGER.info("Search template offset: 0x" + Long.toHexString(searchTemplateOffset).toUpperCase());

		for (var searchTemplateBytesIndex = 0;
		     searchTemplateBytesIndex < matchingBytesCount;
		     searchTemplateBytesIndex++)
		{
			val sourceIndex = searchTemplateOffset + searchTemplateBytesIndex * stepSize;

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