package IGImage;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

class PanelSelectFiltre extends JPanel {

    public PanelSelectFiltre() {
        setLayout(new FlowLayout());
        JButton bFiltreMoyen, bFiltreMedian, bFiltreGaussien, bFiltreSobel;
        add(new JLabel("Filtre"));
        bFiltreMoyen = new JButton("Moyenneur");
        bFiltreMedian = new JButton("Median");
        bFiltreGaussien = new JButton("Gaussien");
        bFiltreSobel = new JButton("Sobel");

        add(bFiltreMoyen);
        add(bFiltreMedian);
        add(bFiltreGaussien);
        add(bFiltreSobel);

        ActionListener auditeur = new AuditeurFiltre();
        bFiltreMoyen.addActionListener(auditeur);
        bFiltreMedian.addActionListener(auditeur);
        bFiltreGaussien.addActionListener(auditeur);
        bFiltreSobel.addActionListener(auditeur);
    }
}
