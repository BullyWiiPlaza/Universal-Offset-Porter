import com.bullywiihacks.address.porter.wiiu.graphical_interface.UniversalOffsetPorterGUI;
import lombok.val;

import static javax.swing.SwingUtilities.invokeLater;
import static javax.swing.UIManager.getSystemLookAndFeelClassName;
import static javax.swing.UIManager.setLookAndFeel;

public class UniversalOffsetPorterClient
{
	public static void main(String[] arguments) throws Exception
	{
		setLookAndFeel(getSystemLookAndFeelClassName());

		invokeLater(() ->
		{
			val universalOffsetPorterGUI = UniversalOffsetPorterGUI.getInstance();
			universalOffsetPorterGUI.setVisible(true);
		});
	}
}