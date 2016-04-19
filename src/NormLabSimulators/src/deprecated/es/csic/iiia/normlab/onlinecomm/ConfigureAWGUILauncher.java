package es.csic.iiia.normlab.onlinecomm.context;

import java.awt.EventQueue;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;

/**
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class ConfigureAWGUILauncher {

	/**
	 * 
	 * @param args
	 */
	public static void main(String[] args) {

		/* Set Nimbus look and feel */
		try {
			for (LookAndFeelInfo info : UIManager.getInstalledLookAndFeels()) {
				if ("Nimbus".equals(info.getName())) {
					UIManager.setLookAndFeel(info.getClassName());
					break;
				}
			}
		} catch (ClassNotFoundException ex) {
			java.util.logging.Logger.getLogger(ConfigureAgentWindow.class.getName()).
			log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(ConfigureAgentWindow.class.getName()).
			log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(ConfigureAgentWindow.class.getName()).
			log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(ConfigureAgentWindow.class.getName()).
			log(java.util.logging.Level.SEVERE, null, ex);
		}

		Runnable runnable = new Runnable() {
			public void run() {
				final ConfigureAgentWindow frame = new ConfigureAgentWindow();
				frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				frame.setVisible(true);
			}
		};
		EventQueue.invokeLater(runnable);
	}
}
