import com.bullywiihacks.address.porter.wii.swing.CodePorterGUI;

import javax.swing.*;

public class GeckoCodePorterGUIClient
{
	public static void main(String[] arguments) throws Exception
	{
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		new CodePorterGUI();
	}
}