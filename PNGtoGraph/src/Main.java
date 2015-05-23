import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import javax.management.RuntimeErrorException;

import ar.com.hjg.pngj.IImageLine;
import ar.com.hjg.pngj.ImageLineHelper;
import ar.com.hjg.pngj.ImageLineInt;
import ar.com.hjg.pngj.PngReader;

public class Main {
	public static int fnr = 0;
	public static int snr = 0;
	public static void main(String[] args) {
		if (args.length > 2 || args.length == 0) {
			System.out.println("Usage: PNGtoGraph <input.png> <output.grf>");
			System.out
					.println("If no output file is specified, then it will be the same as the input filename");
		} else {
			File inputFile = new File(args[0]);
			if (inputFile.exists() == false)
				throw new RuntimeException("File " + args[0]
						+ " does not exist!");
			PngReader pngr = new PngReader(inputFile);
			int channels = pngr.imgInfo.channels;
			if (channels < 3 || pngr.imgInfo.bitDepth != 8)
				throw new RuntimeException(
						"This app is only for RGB8/RGBA8 images!");
			String outputFilePath;
			if (args.length == 2)
				outputFilePath = args[1];
			else
				outputFilePath = args[0].substring(0, args[0].length() - 4)
						.concat(".grf");
			BufferedWriter bw;
			try {
				bw = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(outputFilePath)));
			} catch (FileNotFoundException e) {
				throw new RuntimeException("Unable to create output file! "
						+ outputFilePath);
			}

			try {
				createGraphFile(bw, pngr.imgInfo.rows, pngr.imgInfo.cols);
			} catch (IOException e) {
				System.out
						.println("IO Exception in the createGraphGile method");
				e.printStackTrace();
			}
			for (int row = 0; row < pngr.imgInfo.rows; ++row) {
				IImageLine line = pngr.readRow();
				int[] scanLine = ((ImageLineInt) line).getScanline(); // to save typing
				for (int col = 0; col < pngr.imgInfo.cols; ++col) {
					try {
						String red = Integer.toHexString(scanLine[col*channels]).toUpperCase();
						if (red.length() < 2)
							red = "00".substring(0, red.length()).concat(red);
						String green = Integer.toHexString(scanLine[col*channels+1]).toUpperCase();
						if (green.length() < 2)
							green = "00".substring(0, green.length()).concat(green);
						String blue = Integer.toHexString(scanLine[col*channels+2]).toUpperCase();
						if (blue.length() < 2)
							blue = "00".substring(0, blue.length()).concat(blue);
						if (channels == 3 || (channels==4 && scanLine[col*channels+3] == 255) ) {
							String graphColorCode = "0x".concat(blue).concat(green).concat(red);
							line(bw, pngr, -row - 1, -col, graphColorCode);
							shade(bw, pngr, graphColorCode);
							if (col+1 == pngr.imgInfo.cols || channels == 4 && scanLine[(col+1)*channels+3] < 255)
								line(bw, pngr, -row - 1, -col - 1, graphColorCode);
						}
					} catch (IOException e) {
						System.out.println("Failed to write to file, when adding a pixel!");
						e.printStackTrace();
					}
				}
			}
			try {
				endGraphFile(bw, pngr.imgInfo.rows, pngr.imgInfo.cols);
			} catch (IOException e) {
				System.out.println("Unable to save file? Method: endGraphFile");
				e.printStackTrace();
			}
		}
	}

	public static void createGraphFile(BufferedWriter bw, int rows, int cols) throws IOException {
		String eol = System.getProperty("line.separator");
		bw.write(";This file was NOT created by Graph" + eol
				+ ";I HAVE changed this file with other programs." + eol
				+ "[Graph]" + eol
				+ "Version = 4.4.2.543" + eol
				+ "MinVersion = 2.5" + eol
				+ "OS = " + System.getProperty("os.name") + " " + System.getProperty("os.version") + eol
				+ eol
				+ "[Axes]" + eol
				+ "xMin = " + -rows + eol
				+ "xMax = 0" + eol
				+ "xTickUnit = 1" + eol
				+ "xGridUnit = 1" + eol
				+ "yMin = " + -cols + eol
				+ "yMax = 0" + eol
				+ "yTickUnit = 2" +eol
				+ "yGridUnit = 2" + eol
				+ "AxesColor = clBlue" + eol
				+ "GridColor = 0x00FF9999" + eol
				+ "ShowLegend = 0" + eol
				+ "Radian = 1" + eol
				+ eol);
	}

	public static void line(BufferedWriter bw, PngReader pngr, int col, int row, String color) throws IOException {
		String eol = System.getProperty("line.separator");
		bw.write("[Func" + ++fnr + "]" + eol
				+ "FuncType = 1" + eol
				+ "x = t" + eol
				+ "y = " + row + eol
				+ "From = " + col + eol
				+ "To = " + (col+1) + eol
				+ "Steps = 2" + eol
				+ "Color = " + color + eol
				+ eol);
	}
	
	public static void shade(BufferedWriter bw, PngReader pngr, String color) throws IOException {
		String eol = System.getProperty("line.separator");
		bw.write("[Shade" + ++snr + "]" + eol
				+ "LegendText = Shading " + snr + eol
				+ "ShadeStyle = 4" + eol
				+ "BrushStyle = 0" + eol
				+ "Color = " + color + eol
				+ "FuncNo = " + fnr + eol
				+ "Func2No = " + (fnr+1) + eol
				+ "MarkBorder = 0" + eol + eol);
	}
	
	public static void endGraphFile(BufferedWriter bw, int rows, int cols) throws IOException {
		String eol = System.getProperty("line.separator");
		bw.write("[Data]" + eol
				+ "TextLabelCount = 0" + eol
				+ "FuncCount = " + fnr + eol
				+ "PointSeriesCount = 0" + eol
				+ "ShadeCount = " + snr + eol
				+ "RelationCount = 0" + eol
				+ "OleObjectCount = 0" + eol
				+ eol);
		bw.close();
	}
}
