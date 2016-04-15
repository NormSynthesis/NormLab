package es.csic.iiia.normlab.launcher;

import java.awt.EventQueue;

import javax.swing.UIManager;
import javax.swing.UIManager.LookAndFeelInfo;
import javax.swing.WindowConstants;

import es.csic.iiia.normlab.launcher.ui.NormLabFrame;

/**
 * 
 * @author "Javier Morales (jmorales@iiia.csic.es)"
 *
 */
public class NormLabGUILauncher {

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
			java.util.logging.Logger.getLogger(NormLabFrame.class.getName()).
			log(java.util.logging.Level.SEVERE, null, ex);
		} catch (InstantiationException ex) {
			java.util.logging.Logger.getLogger(NormLabFrame.class.getName()).
			log(java.util.logging.Level.SEVERE, null, ex);
		} catch (IllegalAccessException ex) {
			java.util.logging.Logger.getLogger(NormLabFrame.class.getName()).
			log(java.util.logging.Level.SEVERE, null, ex);
		} catch (javax.swing.UnsupportedLookAndFeelException ex) {
			java.util.logging.Logger.getLogger(NormLabFrame.class.getName()).
			log(java.util.logging.Level.SEVERE, null, ex);
		}

		Runnable runnable = new Runnable() {
			public void run() {
				final NormLabFrame frame = new NormLabFrame();
				frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
				frame.setVisible(true);
			}
		};
		EventQueue.invokeLater(runnable);
	}
}
