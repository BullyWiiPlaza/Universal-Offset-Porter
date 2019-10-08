import com.bullywiihacks.address.porter.wiiu.OffsetPorter;
import com.bullywiihacks.address.porter.wiiu.ValueRange;
import lombok.val;
import org.junit.Ignore;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.System.lineSeparator;

public class TestOffsetPorting
{
	private static final Path MEMORY_DUMP_FILE_PATH = Paths.get("Memory Dumps");
	private static final String GHOSTS_SOURCE_MEMORY_DUMP = MEMORY_DUMP_FILE_PATH.resolve("Ghosts.bin").toString();
	private static final String BLACK_OPS_II_SOURCE_MEMORY_DUMP = MEMORY_DUMP_FILE_PATH.resolve("Black Ops 2.bin").toString();
	private static final String BLACK_OPS_II_DESTINATION_MEMORY_DUMP = MEMORY_DUMP_FILE_PATH.resolve("Black Ops 2 HBL Old.bin").toString();
	private static final String MARIO_KART_8_SOURCE_MEMORY_DUMP = MEMORY_DUMP_FILE_PATH.resolve("Mario Kart 8 Old Layout.bin").toString();
	private static final String MARIO_KART_8_DESTINATION_MEMORY_DUMP = MEMORY_DUMP_FILE_PATH.resolve("Mario Kart 8 New Layout.bin").toString();
	private static final String DUEL_LINKS_SOURCE_MEMORY_DUMP = MEMORY_DUMP_FILE_PATH.resolve("GameAssembly3.10.0.dll").toString();
	private static final String DUEL_LINKS_DESTINATION_MEMORY_DUMP = MEMORY_DUMP_FILE_PATH.resolve("GameAssembly4.0.0.dll").toString();

	@Ignore
	public void portMarioKart8OldLayoutToNewLayout() throws IOException
	{
		portRegularValue();
		portPointer();
		// portPointer2();
	}

	private void portRegularValue() throws IOException
	{
		val offsetPorter = new OffsetPorter(MARIO_KART_8_SOURCE_MEMORY_DUMP,
				MARIO_KART_8_DESTINATION_MEMORY_DUMP, 0x206C53D0);
		offsetPorter.setSearchTemplateByteErrorThreshold(0.1);
		offsetPorter.setMaximumSearchTemplateLength(20);
		// offsetPorter.setStepSize(4);
		val offsetPortingReport = offsetPorter.port();

		val returnedAddress = offsetPortingReport.getAddress();
		assertHexadecimalEquals(returnedAddress, 0x206C73D0);
	}

	private void portPointer() throws IOException
	{
		val offsetPorter = new OffsetPorter(MARIO_KART_8_SOURCE_MEMORY_DUMP,
				MARIO_KART_8_DESTINATION_MEMORY_DUMP, 0x2FB3E7E0);
		val offsetPortingReport = offsetPorter.port();

		val returnedAddress = offsetPortingReport.getAddress();
		assertHexadecimalEquals(returnedAddress, 0x2FB407E0);
	}

	/* private void portPointer2() throws IOException
	{
		offsetPorter = new OffsetPorter("Mario Kart 8 Old Layout.bin",
				"Mario Kart 8 New Layout.bin", 0x3443BB4C, 0.01);
		offsetPortingReport = offsetPorter.port();

		val returnedAddress = offsetPortingReport.getAddress();
		assertEquals(returnedAddress, 0x3443DB4C);
		val isAssembly = offsetPortingReport.isAssembly();
		assertFalse(isAssembly);
	} */

	@Test
	public void portBlackOps2ToBlackOps2() throws IOException
	{
		val offsetPorter = new OffsetPorter(BLACK_OPS_II_SOURCE_MEMORY_DUMP,
				BLACK_OPS_II_SOURCE_MEMORY_DUMP, 0x5AA188);
		val offsetPortingReport = offsetPorter.port();
		val address = offsetPortingReport.getAddress();
		assertHexadecimalEquals(address, 0x5AA188);
	}

	@Test
	public void portBlackOps2ToBlackOps2Shifted() throws IOException
	{
		val offsetPorter = new OffsetPorter(BLACK_OPS_II_SOURCE_MEMORY_DUMP,
				BLACK_OPS_II_DESTINATION_MEMORY_DUMP, 0x5AA188);
		val offsetPortingReport = offsetPorter.port();
		val address = offsetPortingReport.getAddress();
		assertHexadecimalEquals(address, 0x5E5748);
	}

	@Test
	public void portGhostsToBlackOps2() throws IOException
	{
		val offsetPorter = new OffsetPorter(GHOSTS_SOURCE_MEMORY_DUMP,
				BLACK_OPS_II_SOURCE_MEMORY_DUMP, 0x75A1C4);
		offsetPorter.setSourceMemoryDumpOffsetInterval(0.2);
		val destinationOffsetRange = new ValueRange(0x500000, 0x600000);
		offsetPorter.setDestinationOffsetRange(destinationOffsetRange);
		offsetPorter.setStartingMatchingBytesCount(3);
		offsetPorter.setMinimumAcceptedSearchTemplatesCount(2);
		val offsetPortingReport = offsetPorter.port();
		val address = offsetPortingReport.getAddress();
		assertHexadecimalEquals(address, 0x5AA188);
	}

	@Test
	public void portBlackOps2ToGhosts() throws IOException
	{
		val offsetPorter = new OffsetPorter(BLACK_OPS_II_SOURCE_MEMORY_DUMP,
				GHOSTS_SOURCE_MEMORY_DUMP, 0x5AA188);
		offsetPorter.setMaximumSearchTemplateTries(10_000);
		val destinationOffsetRange = new ValueRange(0x700000, 0x800000);
		offsetPorter.setDestinationOffsetRange(destinationOffsetRange);
		offsetPorter.setStartingMatchingBytesCount(3);
		val offsetPortingReport = offsetPorter.port();
		val address = offsetPortingReport.getAddress();
		assertHexadecimalEquals(address, 0x75A1C4);
	}

	// Battle damage blocked: Template: 8D 0C D5 00 00 00 00 83 E1 1F -> 0x10
	@Test
	public void portDuelLinksBattleDamageBlocked() throws IOException
	{
		val offsetPorter = new OffsetPorter(DUEL_LINKS_SOURCE_MEMORY_DUMP,
				DUEL_LINKS_DESTINATION_MEMORY_DUMP, 0xE4BE73);
		val destinationOffsetRange = new ValueRange(0x500000, 0x600000);
		offsetPorter.setDestinationOffsetRange(destinationOffsetRange);
		val offsetPortingReport = offsetPorter.port();
		val address = offsetPortingReport.getAddress();
		assertHexadecimalEquals(address, 0x52F713);
	}

	// Sleeves Patcher: Template: B8 A0 0F 00 00 48 8B 7C 24 38 -> -0x5E
	@Test
	public void portDuelLinksSleevesPatcher() throws IOException
	{
		val offsetPorter = new OffsetPorter(DUEL_LINKS_SOURCE_MEMORY_DUMP,
				DUEL_LINKS_DESTINATION_MEMORY_DUMP, 0x32FC10);
		offsetPorter.setMinimumAcceptedSearchTemplatesCount(2);
		val destinationOffsetRange = new ValueRange(0x300000, 0x400000);
		offsetPorter.setDestinationOffsetRange(destinationOffsetRange);
		val offsetPortingReport = offsetPorter.port();
		val address = offsetPortingReport.getAddress();
		assertHexadecimalEquals(address, 0x340890);
	}

	private void assertHexadecimalEquals(int actual, int expected)
	{
		if (actual != expected)
		{
			throw new AssertionError(lineSeparator() +
					"Actual:\t\t0x" + Long.toHexString(actual).toUpperCase() + lineSeparator()
					+ "Expected:\t0x" + Long.toHexString(expected).toUpperCase());
		}
	}
}