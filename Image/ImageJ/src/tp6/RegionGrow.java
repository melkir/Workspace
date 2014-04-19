package tp6;

import ij.ImagePlus;
import ij.gui.NewImage;
import ij.process.ImageProcessor;
import tp5.Outils;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by melkir on 18/04/14.
 */
public class RegionGrow implements Runnable {
    // Image d'entrée
    private final ImageProcessor in;
    // La taille de l'image
    private final int width, height;
    // Matrice des pixels étiquetés
    private final int[][] labels;
    // Le nombre de régions dans l'image
    private int numberOfRegions;
    // Liste des coordonnées de chaque segments dans l'image
    private ArrayList<LinkedList<Coords>> collection;

    public RegionGrow(ImageProcessor in) {
        this.in = in;
        this.width = in.getWidth();
        this.height = in.getHeight();
        this.numberOfRegions = 0;
        this.labels = new int[width][height];
        this.collection = new ArrayList<LinkedList<Coords>>();
    }

    public static void main(String[] args) {
        ImagePlus imp = Outils.openImage("i1Binaire.jpg");
        RegionGrow rg = new RegionGrow(imp.getProcessor());
        rg.regionGrowing();
        int[][] labels = rg.getLabels();
        rg.storeValues(labels);
        System.out.println("Nombre de dès détecté : " + rg.collection.size());

        int falsePos = 0;
        for (LinkedList<Coords> list : rg.collection) {
            // On n'affiche pas les faux positifs
            if (list.size() > 20) {
                rg.showSegment(list);
            } else {
                ++falsePos;
            }
        }
        if (falsePos > 0) {
            System.out.println("Nombre de faux positifs : " + falsePos);
            System.out.println("Nombre de dès réels : " + (rg.collection.size() - falsePos));
        }

    }

    public void storeValues(int[][] labels) {
        int max = numberOfRegions;
        LinkedList<Coords> coords;
        while (max > 0) {
            coords = new LinkedList<Coords>();
            for (int x = 0; x < labels.length; x++) {
                for (int y = 0; y < labels[0].length; y++) {
                    if (max == labels[x][y]) {
                        coords.add(new Coords(x, y));
                    }
                }
            }
            collection.add(coords);
            --max;
        }
    }

    public void showLabelsResult(int[][] labels) {
        ImagePlus imp = NewImage.createByteImage("Segment", width, height, 1, NewImage.GRAY8);
        ImageProcessor ip = imp.getProcessor();

        for (int x = 0; x < labels.length; x++) {
            for (int y = 0; y < labels[0].length; y++) {
                ip.putPixel(x, y, labels[x][y] * 20);
            }
        }

        imp.show();
    }

    public int[][] getLabels() {
        return labels;
    }

    /**
     * Return the dimension of the segment and his offset
     *
     * @param list List of coords
     * @return Dimension from origin and x, y offset
     */
    private int[] getSegmentDimension(LinkedList<Coords> list) {
        int[] dim = new int[4];
        int xmin = list.get(0).x, xmax = list.get(0).x, ymin = list.get(0).x, ymax = list.get(0).x;
        for (Coords c : list) {
            if (c.x > xmax)
                xmax = c.x;
            else if (c.x < xmin)
                xmin = c.x;
            if (c.y > ymax)
                ymax = c.y;
            else if (c.y < ymin)
                ymin = c.y;
        }
        dim[0] = xmax - xmin;
        dim[1] = ymax - ymin;
        dim[2] = xmin;
        dim[3] = ymin;
//        System.out.println("dim = " + Arrays.toString(dim));
        return dim;
    }

    @Override
    public void run() {
        regionGrowing();
    }

    /**
     * Affiche la liste de coordonnées dans une image
     *
     * @param list Liste de coordonnées
     */
    public void showSegment(LinkedList<Coords> list) {
        int[] dim = getSegmentDimension(list);
        int offsetx = dim[2];
        int offsety = dim[3];
        ImagePlus imp = NewImage.createByteImage("Segment", dim[0], dim[1], 1, NewImage.GRAY8);
        ImageProcessor ip = imp.getProcessor();

        for (int y = 0; y < ip.getHeight(); y++) {
            for (int x = 0; x < ip.getWidth(); x++) {
                ip.putPixel(x, y, in.getPixel(x + offsetx, y + offsety));
            }
        }
        imp.show();
    }

    /**
     * Algorithme de region-growing:
     * 1 - Pour chacun des pixels de l'image :
     * 2 - Ajouter le premier pixel blanc non étiqueté dans la pile et l'étiqueter
     * 3 - Tant qu'il y a des pixels dans la pile :
     * 4 - Récupérer le premier pixel de la pile et le supprimer de la pile
     * 5 - Pour chacun des pixels voisins du pixel courant
     * S'ils ne sont pas étiquetés les ajouter à la pile et les étiqueter
     */
    public void regionGrowing() {
        // La liste des pixels à traiter
        LinkedList<Coords> listeATraiter = new LinkedList<Coords>();
//        LinkedList<Coords> listASauvegarder = new LinkedList<Coords>();
        // On parcours tous les pixels de l'images
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                // Si le pixel n'est pas étiqueté et que c'est un pixel blanc
                if (0 == labels[x][y] && 255 == in.getPixel(x, y)) {
                    // C'est une nouvelle région que l'on ajoute dans la liste à traiter
                    listeATraiter.add(new Coords(x, y));
                    labels[x][y] = (++numberOfRegions);
                }
                // Tant qu'il reste des pixels à traiter dans la liste
                while (!listeATraiter.isEmpty()) {
                    // On récupére le premier pixel de la liste
                    Coords currentPoint = listeATraiter.getFirst();
                    // On l'enlève de la liste a traiter
                    listeATraiter.removeFirst();
                    LinkedList<Coords> listVoisins = getNeighbours(currentPoint.x, currentPoint.y);
                    // On ajoute les voisins du pixels courant
                    listeATraiter.addAll(listVoisins);
                }
            }
        }
    }

    /**
     * Récupére la liste des voisins proche du pixel dans le rayon donnée
     *
     * @param x Abscisse du pixel
     * @param y Ordonnée du pixel
     * @return Liste des pixels voisins
     */
    private LinkedList<Coords> getNeighbours(int x, int y) {
        // La liste des pixels voisin à traiter dans le rayon
        LinkedList<Coords> listeVoisinATraiter = new LinkedList<Coords>();
        int rx, ry;
        for (int th = -1; th <= 1; th++) {
            for (int tw = -1; tw <= 1; tw++) {
                rx = x + tw;
                ry = y + th;
                // Ne pas parcourir les pixels hors de l'image
                if ((rx < 0) || (ry < 0) || (ry >= height) || (rx >= width)) continue;
                // Si le pixel n'a pas déjà été traité et qu'il est blanc
                if (0 == labels[rx][ry] && 255 == in.getPixel(rx, ry)) {
                    // L'ajouter à la liste
                    listeVoisinATraiter.add(new Coords(rx, ry));
                    labels[rx][ry] = numberOfRegions;
                }
            }
        }
        return listeVoisinATraiter;
    }
}