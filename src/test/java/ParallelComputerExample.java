import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.ParallelComputer;
import org.junit.runner.JUnitCore;

public class ParallelComputerExample
{
	@Test
	public void runAllTests()
	{
		Class<?>[] classes = {ParallelTest1.class, ParallelTest2.class};

		// ParallelComputer(true,true) will run all classes and methods
		// in parallel.  (First arg for classes, second arg for methods)
		JUnitCore.runClasses(new ParallelComputer(true, true), classes);
	}

	public static class ParallelTest1
	{
		@Test
		public void test1a()
		{
			lookBusy(3000);
			Assert.fail("FAILED!");
		}

		@Test
		public void test1b()
		{
			lookBusy(3000);
		}
	}

	public static class ParallelTest2
	{
		@Test
		public void test2a()
		{
			lookBusy(3000);
		}

		@Test
		public void test2b()
		{
			lookBusy(3000);
		}
	}

	private static void lookBusy(long ms)
	{
		try
		{
			Thread.sleep(ms);
		} catch (InterruptedException e)
		{
			System.out.println("interrupted");
		}
	}
}