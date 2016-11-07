import com.bullywiihacks.address.porter.wiiu.graphical_interface.UniversalOffsetPorterGUI;

import javax.swing.*;
import java.io.IOException;
import java.util.concurrent.ExecutionException;

public class AddressPorterClient
{
	public static void main(String[] arguments) throws Exception
	{
		UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

		SwingUtilities.invokeLater(() ->
		{
			UniversalOffsetPorterGUI universalOffsetPorterGUI = new UniversalOffsetPorterGUI();
			universalOffsetPorterGUI.setVisible(true);
		});
	}
}