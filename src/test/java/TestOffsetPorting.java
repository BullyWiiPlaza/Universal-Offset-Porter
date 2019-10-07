import com.bullywiihacks.address.porter.wiiu.OffsetPorter;
import com.bullywiihacks.address.porter.wiiu.OffsetPortingReport;
import lombok.val;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import static java.lang.System.lineSeparator;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class TestOffsetPorting
{
	private OffsetPorter offsetPorter;
	private OffsetPortingReport offsetPortingReport;

	private static final Path MEMORY_DUMP_FILE_PATH = Paths.get("Memory Dumps");

	@Test
	public void portMarioKart8OldLayoutToNewLayout() throws IOException
	{
		portRegularValue();
		portPointer();
		// portPointer2();
	}

	private void portRegularValue() throws IOException
	{
		offsetPorter = new OffsetPorter(MEMORY_DUMP_FILE_PATH.resolve("Mario Kart 8 Old Layout.bin").toString(),
				MEMORY_DUMP_FILE_PATH.resolve("Mario Kart 8 New Layout.bin").toString(), 0x206C53D0, 0.01);
		offsetPortingReport = offsetPorter.port();

		val returnedAddress = offsetPortingReport.getAddress();
		assertHexadecimalEquals(returnedAddress, 0x206C73D0);
		val isAssembly = offsetPortingReport.isAssembly();
		assertFalse(isAssembly);
	}

	private void portPointer() throws IOException
	{
		offsetPorter = new OffsetPorter(MEMORY_DUMP_FILE_PATH.resolve("Mario Kart 8 Old Layout.bin").toString(),
				MEMORY_DUMP_FILE_PATH.resolve("Mario Kart 8 New Layout.bin").toString(), 0x2FB3E7E0, 0.01);
		offsetPortingReport = offsetPorter.port();

		val returnedAddress = offsetPortingReport.getAddress();
		assertHexadecimalEquals(returnedAddress, 0x2FB407E0);
		val isAssembly = offsetPortingReport.isAssembly();
		assertFalse(isAssembly);
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
		offsetPorter = new OffsetPorter(MEMORY_DUMP_FILE_PATH.resolve("Black Ops 2.bin").toString(),
				MEMORY_DUMP_FILE_PATH.resolve("Black Ops 2.bin").toString(), 0x5AA188, 0.5);
		offsetPortingReport = offsetPorter.port();
		val address = offsetPortingReport.getAddress();
		assertHexadecimalEquals(address, 0x5AA188);
		assertTrue(offsetPortingReport.isAssembly());
	}

	@Test
	public void portBlackOps2ToBlackOps2Shifted() throws IOException
	{
		offsetPorter = new OffsetPorter(MEMORY_DUMP_FILE_PATH.resolve("Black Ops 2.bin").toString(),
				MEMORY_DUMP_FILE_PATH.resolve("Black Ops 2 HBL Old.bin").toString(), 0x5AA188, 0.5);
		offsetPortingReport = offsetPorter.port();
		val address = offsetPortingReport.getAddress();
		assertHexadecimalEquals(address, 0x5E5748);
		assertTrue(offsetPortingReport.isAssembly());
	}

	@Test
	public void portGhostsToBlackOps2() throws IOException
	{
		offsetPorter = new OffsetPorter(MEMORY_DUMP_FILE_PATH.resolve("Ghosts.bin").toString(),
				MEMORY_DUMP_FILE_PATH.resolve("Black Ops 2.bin").toString(), 0x75A1C4, 0.5);
		offsetPortingReport = offsetPorter.port();
		val address = offsetPortingReport.getAddress();
		assertHexadecimalEquals(address, 0x5AA188);
		assertTrue(offsetPortingReport.isAssembly());
	}

	@Test
	public void portBlackOps2ToGhosts() throws IOException
	{
		offsetPorter = new OffsetPorter(MEMORY_DUMP_FILE_PATH.resolve("Black Ops 2.bin").toString(),
				MEMORY_DUMP_FILE_PATH.resolve("Ghosts.bin").toString(), 0x5AA188, 0.5);
		offsetPortingReport = offsetPorter.port();
		int address = offsetPortingReport.getAddress();
		assertHexadecimalEquals(address, 0x75A1C4);
		assertTrue(offsetPortingReport.isAssembly());
	}

	@Test
	public void portDuelLinks() throws IOException
	{
		offsetPorter = new OffsetPorter(MEMORY_DUMP_FILE_PATH.resolve("GameAssembly3.10.0.dll").toString(),
				MEMORY_DUMP_FILE_PATH.resolve("GameAssembly4.0.0.dll").toString(), 0xE4BE73, 1.0);
		offsetPortingReport = offsetPorter.port();
		val address = offsetPortingReport.getAddress();
		assertHexadecimalEquals(address, 0x52F713);
	}

	private void assertHexadecimalEquals(int actual, int expected)
	{
		if (actual != expected)
		{
			throw new AssertionError(lineSeparator() +
					"Actual:\t\t" + Long.toHexString(actual).toUpperCase() + lineSeparator()
					+ "Expected:\t" + Long.toHexString(expected).toUpperCase());
		}
	}
}