package tp4;

import ij.ImagePlus;
import ij.gui.ImageCanvas;

import java.awt.BorderLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class PanelImage extends JPanel {
	
	public PanelImage(ImagePlus imp, ImagePlus histo, ImagePlus imp2, ImagePlus histo2) {
		BorderLayout mainLayout = new BorderLayout();
		mainLayout.setHgap(5);
		setLayout(mainLayout);

		String title = imp.getTitle();

		add(new JLabel("Nom de l'image : " + title), BorderLayout.NORTH);
		add(new ImageCanvas(imp), BorderLayout.WEST);
		add(new ImageCanvas(histo), BorderLayout.EAST);

		JPanel pane2 = new JPanel(new BorderLayout());
		pane2.add(new JLabel(title + " après la transformation"), BorderLayout.NORTH);
		pane2.add(new ImageCanvas(imp2), BorderLayout.WEST);
		pane2.add(new ImageCanvas(histo2), BorderLayout.EAST);

		add(pane2, BorderLayout.SOUTH);
	}
	
}
