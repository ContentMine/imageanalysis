package cesium.op.research;

/*********************************************************************
 FiltroCanny.java ? implements the Canny filter.

 WRITTEN BY: Jos? Iguelmar Miranda & Jo?o Camargo Neto.

 DATE:   September 2008

 Copyright (c) 2009 Embrapa Inform?tica Agropecu?ria

 PERMISSION TO COPY:
 This program is free software, under the GNU General Public License
 (GPL); permission to use, copy and modify this software and its
 documentation for NON-COMMERCIAL purposes is granted, without fee, provided
 that an acknowledgement to the authors, Jos? Iguelmar Miranda & Jo?o
 Camargo Neto, at www.cnptia.embrapa.br, appears in all copies.

 Embrapa Inform?tica Agropecu?ria makes no representations about the
 suitability or fitness of the software for any or for a particular purpose.
 Embrapa Inform?tica Agropecu?ria shall not be liable for any damages
 suffered as a result of using, modifying or distributing this software or
 its derivatives.

 For a copy of GNU General Public License, write to:
 Free Software Foundation, Inc.,
 59 Temple Street, Suite 330, Boston, MA 02111-1307 USA.

 *********************************************************************

 Description:

 This program implements the Canny filter.

 Reference paper:

 CANNY, J. A computational approach to edge detection. IEEE Transactions
 on Pattern Analysis and Machine Intelligence. 8(6):679-698, 1986.

 *********************************************************************/
// Generic packages

import java.awt.GridLayout;
import java.awt.image.BufferedImage;
import java.awt.image.Raster;
import java.awt.image.WritableRaster;
import java.io.File;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

public class FiltroCanny extends JFrame {

    // Attributes
    // Image scale and magnitude
    static double ORI_SCALE = 40.0;
    static double MAG_SCALE = 20.0;

    // Kernel mask maximum size
    static int MAX_MASK_SIZE = 20;

    // Fraction of pixels that should be above the HIGH threshold
    double ratio = 0.1;
    int LARGURA;
	private String[] args;
	private double dp;
	private int inf;
	private int sup;

    public static void main(String args[]) {
        double dp = 1.0;
        int inf = 0, sup = 0;

        // Parameters ok?
        if (args.length != 4) {
            String msga = "Uso: java -cp . FiltroCanny <imagem> <dp> <inf> <sup>";
            String msgb = "\n   dp => standard deviation.";
            String msgc = "\n   inf => LOW threshold.";
            String msgd = "\n   sup => HIGH threshold.";
            System.out.println(msga + msgb + msgc + msgd);
            System.exit(0);
        }

        FiltroCanny canny = new FiltroCanny(args, dp, inf, sup);
        canny.detect();
    }

	public FiltroCanny (String[] args, double dp, int inf, int sup) {
		this.args = args;
		this.dp = dp;
		this.inf = inf;
		this.sup = sup;
	}

	public void detect() {
		// Show JFrame decorated by Swing
        JFrame.setDefaultLookAndFeelDecorated(true);

        try {
            dp = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("Valor do parametro <dp> invalido");
            System.exit(0);
        }

        try {
            inf = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            System.out.println("Valor do parametro <inf> invalido");
            System.exit(0);
        }

        try {
            sup = Integer.parseInt(args[3]);
        } catch (NumberFormatException e) {
            System.out.println("Valor do parametro <sup> invalido");
            System.exit(0);
        }

        // Call the constructor
        if (dp <= 0.0)
            dp = 1.0;
        if (inf < 0)
            inf = 0;
        if (sup > 255)
            sup = 255;

        long eq_time = System.currentTimeMillis();
        FiltroCanny fc = new FiltroCanny(args[0], dp, inf, sup);
        eq_time = System.currentTimeMillis() - eq_time;
        String msg = "Canny: tempo de execucao ";
        System.out.println(msg + eq_time + " milisseg.");

        // If ?X? clicked, close the application
        fc.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        fc.setVisible(true);
	}

    public FiltroCanny(String aFile, double s, int inf, int sup) {
        BufferedImage imagem = null,
                imagemMagnitude = null,
                imagemOriginal = null;
        int w, h, tipo;
        JLabel img1;

        // Does the image file exist?
        File file = new File(aFile);
        try {
            imagem = ImageIO.read(file);
        } catch (Exception e) {
            System.out.println("Imagem '" + aFile + "' nao existe.");
            System.exit(0);
        }

        String msga = "\nParametros para Canny:\n";
        String msgb = "==> limiar inferior " + inf + "\n";
        String msgc = "==> limiar superior " + sup + "\n";
        String msgd = "==> desvio padrao   " + s + "\n";
        System.out.println(msga + msgb + msgc + msgd);

        // Atribui nome ao frame
        setTitle("Canny: " + file.getName());

        // Cria imagem local
        w = imagem.getWidth();
        h = imagem.getHeight();
        tipo = BufferedImage.TYPE_BYTE_GRAY;
        imagemMagnitude = new BufferedImage(w, h, tipo);
        imagemOriginal = new BufferedImage(w, h, tipo);
        WritableRaster imWR = imagem.getRaster();

        // Call the Canny method
        canny(s, imagem, imagemMagnitude, imagemOriginal);

        // Define edge pixels using threshold values HIGH and LOW
        linhasBordas(sup, inf, imagem, imagemMagnitude, imagemOriginal);

        for (int i = 0; i < LARGURA; i++)
            for (int j = 0; j < w; j++)
                imWR.setSample(j, i, 0, 255);

        for (int i = h - 1; i > h - 1 - LARGURA; i--)
            for (int j = 0; j < w; j++)
                imWR.setSample(j, i, 0, 255);

        for (int i = 0; i < h; i++)
            for (int j = 0; j < LARGURA; j++)
                imWR.setSample(j, i, 0, 255);

        for (int i = 0; i < h; i++)
            for (int j = w - LARGURA - 1; j < w; j++)
                imWR.setSample(j, i, 0, 255);

        // Create gridLayout de 1 x 1
        getContentPane().setLayout(new GridLayout(1, 1));
        img1 = new JLabel(new ImageIcon(imagem));
        setSize(w, h);
        getContentPane().add(new JScrollPane(img1));
    }

    private void canny(double s, BufferedImage imagem,
                       BufferedImage imagemMag, BufferedImage imagemOrig) {

        int width = 0, k, n, nc, nr;
        double[][] componenteX,  // x component of the original image convolved
                // with the Gaussian.

                componenteY,  // y component of the original image convolved
                // with the Gaussian.
                derivadaX,    // x component of the convolved image
                // (componenteX) with Gaussian derivative
                derivadaY;    // y component of the convolved image
        // (componenteY) with Gaussian derivative
        double funcGauss[],      // Gaussian values
                derivadaGauss[],  // Values of the Gaussian first derivative
                z;

        funcGauss = new double[MAX_MASK_SIZE];
        derivadaGauss = new double[MAX_MASK_SIZE];
        nc = imagem.getWidth();
        nr = imagem.getHeight();

        // Create a mask of the Gaussian filter and its derivative
        for (int i = 0; i < MAX_MASK_SIZE; i++) {
            funcGauss[i] = mediaGauss((double) i, s);
            if (funcGauss[i] < 0.005) {
                width = i;
                break;
            }
            derivadaGauss[i] = dGauss((double) i, s);
        }

        n = 2 * width + 1;
        LARGURA = (int) width / 2;
        System.out.println("Suavizando com uma Gaussiana (largura = " +
                n + ") ...\n");

        componenteX = new double[nc][nr];
        componenteY = new double[nc][nr];

        // Convolve original image with Gaussian mask in x and y directions
        convolveImagemXY(imagem, funcGauss, width, componenteX, componenteY);

        // Convolve smoothed image with derivative
        System.out.println("Convolucao com a derivada da Gaussiana...\n");
        derivadaX = convolveDerivadaXY(componenteX, nr, nc, derivadaGauss,
                width, 1);
        derivadaY = convolveDerivadaXY(componenteY, nr, nc, derivadaGauss,
                width, 0);

        WritableRaster magWR = imagemMag.getRaster();
        // Create magnitude image from the x and y derivatives (gradient)
        for (int i = 0; i < nr; i++)
            for (int j = 0; j < nc; j++) {
                z = norma(derivadaX[j][i], derivadaY[j][i]);

                magWR.setSample(j, i, 0, (int) z * MAG_SCALE);
            }

        // Suppress false maxima
        removeFalsoMax(derivadaX, derivadaY, nr, nc, imagemMag, imagemOrig);
    }

    // Compute mean of Gaussian function
    private double mediaGauss(double x, double sigma) {
        double z;

        z = (gauss(x, sigma) + gauss(x + 0.5, sigma) + gauss(x - 0.5, sigma)) / 3.0;
        z = z / (Math.PI * 2.0 * sigma * sigma);
        return z;
    }

    // Compute value for Gaussian function
    private double gauss(double x, double sigma) {
        double expoente;

        if (sigma == 0)
            return 0.0;
        expoente = Math.exp(((-x * x) / (2 * sigma * sigma)));
        return expoente;
    }

    // Compute first derivative value of Gaussian function
    private double dGauss(double x, double sigma) {
        return (-x / (sigma * sigma) * gauss(x, sigma));
    }

    // Convolve components x and y of the image.
    private void convolveImagemXY(BufferedImage imagem, double[] funcGauss,
                                  int width, double[][] compX,
                                  double[][] compY) {

        int i1, i2, nr, nc;
        double x, y;

        nc = imagem.getWidth();
        nr = imagem.getHeight();

        Raster imR = imagem.getRaster();
        for (int i = 0; i < nr; i++)
            for (int j = 0; j < nc; j++) {
                x = funcGauss[0] * imR.getSample(j, i, 0);
                y = funcGauss[0] * imR.getSample(j, i, 0);
                for (int k = 1; k < width; k++) {
                    i1 = (i + k) % nr;
                    i2 = (i - k + nr) % nr;
                    y += funcGauss[k] * imR.getSample(j, i1, 0) +
                            funcGauss[k] * imR.getSample(j, i2, 0);

                    i1 = (j + k) % nc;
                    i2 = (j - k + nc) % nc;
                    x += funcGauss[k] * imR.getSample(i1, i, 0) +
                            funcGauss[k] * imR.getSample(i2, i, 0);
                }
                compX[j][i] = x;
                compY[j][i] = y;
            }
    }

    // Do the convolution in the derivatives of the x and y components of the
    // convolved image
    private double[][] convolveDerivadaXY(double[][] imagem, int nr,
                                          int nc, double[] funcGauss, int width, int compXY) {

        int i1, i2;
        double x;
        double[][] componente = new double[nc][nr];

        for (int i = 0; i < nr; i++)
            for (int j = 0; j < nc; j++) {
                x = 0.0;
                for (int k = 1; k < width; k++) {
                    switch (compXY) {
                        case 0:            // y component
                            i1 = (i + k) % nr;
                            i2 = (i - k + nr) % nr;
                            x += -funcGauss[k] * imagem[j][i1] + funcGauss[k] * imagem[j][i2];
                            break;
                        case 1:            // x component
                            i1 = (j + k) % nc;
                            i2 = (j - k + nc) % nc;
                            x += -funcGauss[k] * imagem[i1][i] + funcGauss[k] * imagem[i2][i];
                            break;
                    }
                }
                componente[j][i] = x;
            }
        return componente;
    }

    // Suppress false maxima
    private void removeFalsoMax(double[][] derivX, double[][] derivY,
                                int nr, int nc, BufferedImage imagemMag, BufferedImage imagemOrig) {

        int k, n, m, top, bottom, left, right;
        double xx, yy, grad1, grad2, grad3, grad4, gradiente, compX, compY;

        nc = imagemMag.getWidth();
        nr = imagemMag.getHeight();

        WritableRaster magWR = imagemMag.getRaster();
        WritableRaster oriWR = imagemOrig.getRaster();

        for (int i = 1; i < nr - 1; i++) {
            for (int j = 1; j < nc - 1; j++) {
                magWR.setSample(j, i, 0, 0);

                // x and y derivatives are components of a vector (gradient)
                compX = derivX[j][i];
                compY = derivY[j][i];
                if (Math.abs(compX) < 0.01 && Math.abs(compY) < 0.01)
                    continue;
                gradiente = norma(compX, compY);

                // Fallow gradient direction, vector (compX, compY).
                // Keep edge pixels (local maxima).
                if (Math.abs(compY) > Math.abs(compX)) {
                    // First case: y component is bigger. Gradient direction is
                    // upward or downward.
                    xx = Math.abs(compX) / Math.abs(compY);
                    yy = 1.0;
                    grad2 = norma(derivX[j][i - 1], derivY[j][i - 1]);
                    grad4 = norma(derivX[j][i + 1], derivY[j][i + 1]);
                    if (compX * compY > 0.0) {
                        grad1 = norma(derivX[j - 1][i - 1], derivY[j - 1][i - 1]);
                        grad3 = norma(derivX[j + 1][i + 1], derivY[j + 1][i + 1]);
                    } else {
                        grad1 = norma(derivX[j + 1][i - 1], derivY[j + 1][i - 1]);
                        grad3 = norma(derivX[j - 1][i + 1], derivY[j - 1][i + 1]);
                    }
                } else {
                    // Second case: y component is bigger. Gradient direction is
                    // left or right.
                    xx = Math.abs(compY) / Math.abs(compX);
                    yy = 1.0;
                    grad2 = norma(derivX[j + 1][i], derivY[j + 1][i]);
                    grad4 = norma(derivX[j - 1][i], derivY[j - 1][i]);
                    if (compX * compY > 0.0) {
                        grad1 = norma(derivX[j + 1][i + 1], derivY[j + 1][i + 1]);
                        grad3 = norma(derivX[j - 1][i - 1], derivY[j - 1][i - 1]);
                    } else {
                        grad1 = norma(derivX[j + 1][i - 1], derivY[j + 1][i - 1]);
                        grad3 = norma(derivX[j - 1][i + 1], derivY[j - 1][i + 1]);
                    }
                }

                // Compute the interpolated value of the gradient magnitude
                if ((gradiente > (xx * grad1 + (yy - xx) * grad2)) &&
                        (gradiente > (xx * grad3 + (yy - xx) * grad4))) {

                    if (gradiente * MAG_SCALE <= 255)
                        magWR.setSample(j, i, 0, (int) gradiente * MAG_SCALE);
                    else
                        magWR.setSample(j, i, 0, 255);
                    oriWR.setSample(j, i, 0,
                            (int) Math.atan2(compY, compX) * ORI_SCALE);
                } else {
                    magWR.setSample(j, i, 0, 0);
                    oriWR.setSample(j, i, 0, 0);
                }
            }
        }
    }

    // Define edge pixels
    private void linhasBordas(int sup, int inf, BufferedImage imagem,
                              BufferedImage imagemMag, BufferedImage imagemOrig) {

        int nr, nc;

        nc = imagem.getWidth();
        nr = imagem.getHeight();
        WritableRaster imWR = imagem.getRaster();
        Raster imR = imagem.getRaster();
        Raster magR = imagemMag.getRaster();

        System.out.println("Iniciando corte com limiares...\n");
        for (int i = 0; i < nr; i++)
            for (int j = 0; j < nc; j++)
                imWR.setSample(j, i, 0, 0);

        if (sup < inf) {
            estimaLimiar(imagemMag, sup, inf);
            String str = "Limiar de corte (da imagem): SUPERIOR ";
            System.out.println(str + sup + " INFERIOR\n" + inf);
        }

        // For each edge with magnitude above HIGH threshold, draw the edge
        // pixels that are above the LOW threshold
        for (int i = 0; i < nr; i++)
            for (int j = 0; j < nc; j++)
                if (magR.getSample(j, i, 0) >= sup)
                    trace(i, j, inf, imagem, imagemMag, imagemOrig);

        // Make the edge black
        for (int i = 0; i < nr; i++)
            for (int j = 0; j < nc; j++)
                if (imR.getSample(j, i, 0) == 0)

                    imWR.setSample(j, i, 0, 255);
                else
                    imWR.setSample(j, i, 0, 0);
    }

    // Estimate the HIGH threshold
    private void estimaLimiar(BufferedImage imagemMag, int hi, int inf) {

        int histograma[], count, nr, nc, i, j, k;

        nc = imagemMag.getWidth();
        nr = imagemMag.getHeight();
        histograma = new int[256];
        Raster magR = imagemMag.getRaster();

        // Image histogram
        for (k = 0; k < 256; k++)
            histograma[k] = 0;

        for (i = LARGURA; i < nr - LARGURA; i++)
            for (j = LARGURA; j < nc - LARGURA; j++)
                histograma[magR.getSample(j, i, 0)]++;

        // O limiar superior deveria ser maior que 80 ou 90% dos pixels
        j = nr;
        if (j < nc)
            j = nc;
        j = (int) (0.9 * j);
        k = 255;

        count = histograma[255];
        while (count < j) {
            k--;
            if (k < 0)
                break;
            count += histograma[k];
        }

        hi = k;
        i = 0;
        while (histograma[i] == 0)
            i++;
        inf = (int) (hi + i) / 2;
    }

    // Trace, recursively, the edge pixels
    private int trace(int i, int j, int inf, BufferedImage imagem,
                      BufferedImage imagemMag, BufferedImage imagemOrig) {

        int n, m;
        int flag = 0;

        Raster magR = imagemMag.getRaster();
        Raster imR = imagem.getRaster();
        WritableRaster imWR = imagem.getRaster();

        if (imR.getSample(j, i, 0) == 0) {
            imWR.setSample(j, i, 0, 255);
            flag = 0;
            for (n = -1; n <= 1; n++) {
                for (m = -1; m <= 1; m++) {
                    if (i == 0 && m == 0)
                        continue;
                    if ((range(imagemMag, i + n, j + m) == 1) &&
                            (magR.getSample(j + m, i + n, 0)) >= inf)
                        if (trace(i + n, j + m, inf, imagem, imagemMag, imagemOrig) == 1) {
                            flag = 1;
                            break;
                        }
                }
                if (flag == 1)
                    break;
            }
            return 1;
        }
        return 0;
    }

    // Certificate that the pixel belongs to the image
    private int range(BufferedImage imagem, int i, int j) {
        int nc, nr;

        nc = imagem.getWidth();
        nr = imagem.getHeight();
        if ((i < 0) || (i >= nr))
            return 0;
        if ((j < 0) || (j >= nc))
            return 0;
        return 1;
    }

    // Compute the gradient magnitude
    private double norma(double x, double y) {
        return Math.sqrt(x * x + y * y);
    }

}
