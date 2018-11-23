
public class Perceptron {
	
	private static int[][] trainingData = {		
		{0,0,0},
		{0,1,1},
		{1,0,1},
		{1,1,0}}; 
	
	private static int trainingSetSize = 0;
	private static int networkSize = 0;
	private static double threshold = 0.5; // experiment
	private static double[] weights;
	private static double learningRate = 0.1; //experiment 
	
	private static void initNetwork() {
		trainingSetSize = trainingData.length;
		if (trainingSetSize == 0) {
			System.out.println("No training data.");
			return;
		}
		networkSize = trainingData[0].length - 1;
		if (networkSize == 0) {
			System.out.println("Training data does not contain desired output.");
			return;
		}
		if (networkSize == -1) {
			System.out.println("The network is empty.");
			return;
		}
		weights = new double[networkSize]; 
		for (int i = 0; i < networkSize; i++){
			weights[i] = 0; // experiment with small (and large) weights
		}
	}
	
	private static double stepActivationFunction(double input){
		if (input >= threshold) return 1;
		return 0;
	}
	
	private static void trainNetwork(){
		int count=0;
		// 10 runs, print weights after each run
		for (int k = 0; k < 10; k++){
			//Run through training set once.
			for (int i = 0; i < trainingSetSize; i++){
				double inputToNeuron = 0;
				for (int j = 0; j < networkSize; j++){
					inputToNeuron += weights[j] * trainingData[i][j];
				}
				double activationOfNeuron = stepActivationFunction(inputToNeuron);
				double errorOfNeuron = trainingData[i][networkSize] - activationOfNeuron;
				for (int j = 0; j < networkSize; j++){
					weights[j] += learningRate * errorOfNeuron * trainingData[i][j];
				}			
			}
			count++;
		printWeights();
		System.out.println("count:"+count);
		}
	}
	
	private static void printWeights(){
		for (int i = 0; i < networkSize; i++){
			System.out.printf("Weight %d is %f.\n", i, weights[i]);
		}
	}
	
	private static void testNetwork(){
		for (int i = 0; i < trainingSetSize; i++){
			double inputToNeuron = 0;
			for (int j = 0; j < networkSize; j++){
				inputToNeuron += weights[j] * trainingData[i][j];
			}
			double activationOfNeuron = stepActivationFunction(inputToNeuron);
			if (activationOfNeuron != trainingData[i][networkSize])
				System.out.println("Network did not learn training set " + i);	
		}
		System.out.println("Done testing.");
	}
	
	
	public static void main(String[] args){
		initNetwork();
		trainNetwork();
		testNetwork();
	}
	
}
