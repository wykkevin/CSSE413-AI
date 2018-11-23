import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;

public class Training {

	public static void main(String[] args) {
		// Get help from TA on the approximate value on the parameters and tries
		// different values with Hao Yang. Because we want to test some different
		// values, but time is limited so each of us run a different group of parameter
		// and share the results.
		Return train = readFile("src/train-labels-idx1-ubyte", "src/train-images-idx3-ubyte");
		Return test = readFile("src/t10k-labels-idx1-ubyte", "src/t10k-images-idx3-ubyte");
		FeedForwardNetwork n = new FeedForwardNetwork(784, 125, 1, 10);
		double[][] trainInputs = train.input;
		double[][] trainOutputs = train.output;
		double[][] testInputs = test.input;
		double[][] testOutputs = test.output;
		n.initNetwork(trainInputs, trainOutputs, 0.3, 0.5);
		n.trainNetwork(50, true);
		// n.printWeights();
		n.testNetwork();
		// n.testNetworkBatch(10000, testInputs, testOutputs, true);
	}

	public static Return readFile(String labelFilename, String imageFilename) {
		double[][] input = null;
		double[][] desiredOutput = null;
		try {
			DataInputStream labels = new DataInputStream(new FileInputStream(labelFilename));
			DataInputStream images = new DataInputStream(new FileInputStream(imageFilename));
			labels.readInt();
			images.readInt();
			int num = labels.readInt();
			images.readInt();
			int numRows = images.readInt();
			int numCols = images.readInt();
			byte[] labelsData = new byte[num];
			labels.read(labelsData);
			desiredOutput = new double[num][10];
			for (int i = 0; i < num; i++) {
				for (int j = 0; j < 10; j++) {
					if (j == labelsData[i]) {
						desiredOutput[i][j] = 1;
					} else {
						desiredOutput[i][j] = 0;
					}
				}
			}
			int imageVectorSize = numCols * numRows;
			byte[] imagesData = new byte[num * imageVectorSize];
			images.read(imagesData);
			input = new double[num][imageVectorSize];
			for (int i = 0; i < num; i++) {
				for (int j = 0; j < imageVectorSize; j++) {
					if (imagesData[i * imageVectorSize + j] > 10) {
						input[i][j] = 1;
					} else {
						input[i][j] = 0;
					}
				}
			}

			images.close();
			labels.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new Return(input, desiredOutput);
	}

	private static double[] toBinArray(byte b) {
		String k = Integer.toBinaryString(b);
		double[] output = new double[4];
		while (k.length() < 4) {
			k = "0" + k;
		}
		for (int i = 0; i < 4; i++) {
			output[i] = k.charAt(i);
		}
		return output;
	}
}

class Return {
	double[][] input;
	double[][] output;

	public Return(double[][] input, double[][] output) {
		this.input = input;
		this.output = output;

	}
}
