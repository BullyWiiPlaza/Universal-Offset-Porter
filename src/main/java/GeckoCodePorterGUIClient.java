import com.bullywiihacks.address.porter.wii.swing.CodePorterGUI;

import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.UIManager.getSystemLookAndFeelClassName;
import static javax.swing.UIManager.setLookAndFeel;

public class GeckoCodePorterGUIClient
{
	public static void main(String[] arguments) throws Exception
	{
		setLookAndFeel(getSystemLookAndFeelClassName());

		invokeLater(() ->
		{
			try
			{
				new CodePorterGUI();
			} catch (Exception exception)
			{
				exception.printStackTrace();
			}
		});
	}
}