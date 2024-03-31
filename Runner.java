package ie.atu.sw;

import java.util.*;

import java.io.*;
import java.lang.*;
import java.lang.reflect.Array;

public class Runner {
	
	// created Map that will store the embedded data that we can then search through later
	public static Map<String, double[]> embeddingsMap = new HashMap<>();	
	
	 // Method to write results to a file
    public static void writeResultsToFile(String fileName, String content) {
    	
    	 try (FileWriter writer = new FileWriter(fileName, true)) { // Set append mode to true
             writer.write(content);
             
             System.out.println("Results appended to " + fileName);
             
         } catch (IOException e) {
        	 
             System.err.println("Error appending results to file: " + e.getMessage());
         }
    }
    
	//created constants for color and resetting color for UI
	static String RESET = "\u001B[0m";
	static String RED = "\u001B[31m";
	static String GREEN = "\u001B[32m";
	static String CYAN = "\u001B[36m";
	static String YELLOW = "\u001B[33m";
	
	// created function to load the main menu
	public static void loadMainMenu() {		
		//used the color codes to change the UI
		System.out.println(CYAN+"************************************************************"+RESET);
		System.out.println("*    "+GREEN+"ATU - Dept. of Computer Science & Applied Physics"+RESET+"     *");
		System.out.println("*                                                          *");
		System.out.println("*         Similarity Search With Word Embeddings           *");
		System.out.println("*                                                          *");
		System.out.println(CYAN+"************************************************************"+RESET);
		System.out.println("(1) Specify Embedding File ( recommended: word-embedding.txt )");
		System.out.println("(2) Specify an output File (default: ./out.txt");
		System.out.println("(3) Enter a Word or Sentence");
		System.out.println(RED+"(4) Quit"+RESET);
		System.out.println(YELLOW+"Select Option [1 - 4]"+RESET);
	}
	
	//Search using a Cosine algorithm and return the top n
	public static double cosineSimilarity(double[] vector1, double[] vector2) {
		//declare variables
	    double dotProduct = calculateDotProduct(vector1, vector2);
	    double magnitude1 = Math.sqrt(calculateMagnitude(vector1));
	    double magnitude2 = Math.sqrt(calculateMagnitude(vector2));

	    return dotProduct / (magnitude1 * magnitude2);
		
	}
	//method to calculate magnitude
	public static double calculateMagnitude(double[] vector) {
	    double sum = 0.0;
	    for (double v : vector) {
	        sum += v * v;
	    }
	    return sum;
	}
	
	//method that then compares the users word with those in the map
	public static String compareWithMap(String usersWord, Map<String, double[]> embeddingsMap) {
		
		//get the vector of the users word 
		double[] usersWordAsVector = searchWordInEmbeddingsMap(usersWord,embeddingsMap);
		
		//check if the word is in the map
		
		if(usersWordAsVector == null) {
			
			return "word could not be found";
			
		}
		
		// Create a TreeMap to store word-similarity pairs in sorted order
        TreeMap<Double, String> similarityMap = new TreeMap<>(Collections.reverseOrder());

        
        // Calculate cosine similarity for each word in the embeddings map
        for (Map.Entry<String, double[]> entry : embeddingsMap.entrySet()) {
        	
            String word = entry.getKey();
            
            double[] embedding = entry.getValue();
            
            if (!word.equals(usersWord)) { // Exclude the user's word itself
                double similarity = cosineSimilarity(usersWordAsVector, embedding);
                similarityMap.put(similarity, word);
            }
        }
        
        //return the result string with top similar words and their similarity scores
        StringBuilder result = new StringBuilder();
        result.append("Top similar words to '").append(usersWord).append("':\n");
        int count = 0;
        for (Map.Entry<Double, String> entry : similarityMap.entrySet()) {
            if (count >= 5) { // Limit to top 5 similar words
                break;
            }
            result.append(entry.getValue()).append(" (Similarity Score: ").append(entry.getKey()).append(")\n");
            count++;
        }

        return result.toString();
	} 
	
	
	//calculate dotProduct
	public static double calculateDotProduct(double[] vectorA , double[] vectorB) {
		//set to zero
		double dotProduct = 0.0;
		//for as long as the length of the vector (always 50)
		for(int i =0; i< vectorA.length;i++) {
			dotProduct += vectorA[i] * vectorB[i];
		}
		
		return dotProduct;
	}
	
	
	//created a method to fill the map with embedded words based on the filename given
	public static void mapFiller(String filePath)throws IOException{		
	
		// using try to open the file path and create a buffer reader to read throguh it , the try closes the buffer reader even if an exception is thrown
		try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;           
            // while loop to run through the file until all the lines have been read in
            while ((line = reader.readLine()) != null) {
            	String[] parts = line.split(","); //remove leading/trailing space
            	//seperate the string part of the line from the double
               
                String word = parts[0];
                double[] embedding = new double[50];
                for (int i = 1; i < parts.length; i++) {
                	String valueStr = parts[i].replaceAll(",", ""); // Remove commas if present
                    embedding[i - 1] = Double.parseDouble(valueStr);
                }
                // I then add the word and embedding to the Map
                embeddingsMap.put(word, embedding);              
            }
            System.out.println("Embedded file succesfully parsed into map");
            
			} catch (IOException e) {
            System.err.println("Error reading the embedding file: " + e.getMessage());
            // If error print message
        }
	}
	
	// Method to search for a word in the embeddings map and return its embedding
    public static double[] searchWordInEmbeddingsMap(String word, Map<String, double[]> embeddingsMap) {
        // Check if the embeddings map contains the users word
    	
        if (embeddingsMap.containsKey(word)) {
            return embeddingsMap.get(word); // Return the embedding if the word is found
        } else {
            System.out.println("Word '" + word + "' not found in the embeddings map.");
            return null; // Return null if the word is not found
        }
    }
	
	//main 
	public static void main(String[] args) throws InterruptedException {
		
		//create an instance of the scanner class
		Scanner keyboard = new Scanner(System.in);
		
		//Declare Variables
		int UsersInput;
		boolean appRunning = true;
		String EmbeddingFileName = "word-embeddings.txt";
		String OutputfileName = "./out.txt";
		//begin application by creating the menu 
		loadMainMenu();
		
		//create a while loop to run app until user chooses to exit 
		while(appRunning == true) {			
		// take users input 			
			UsersInput = keyboard.nextInt(); 
			keyboard.nextLine();
			
			switch(UsersInput){
			
			//allow user to choose what file of embedded words they want to search through 	
			case 1:
				System.out.println("What Embedding File would you like to use? ");
				System.out.print("Embedding FileName : ");
				
				EmbeddingFileName = keyboard.nextLine();
				System.out.println("Embedding FileName saved");
				Thread.sleep(1000);
				
				//attempt to use the users specified file
				try {
					mapFiller(EmbeddingFileName);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
								
				loadMainMenu();
				break;
				
			//allow user to specify where the results will be outputted to
			case 2:
				System.out.println("What File would you like to output to? ");
				System.out.print("Output FileName : ");
				
				OutputfileName = keyboard.nextLine();
				System.out.println("OutPut FileName saved");
				Thread.sleep(1000);
				loadMainMenu();
				break;
				
			//have user specify the word they want to search for in the file
			case 3:				
				 System.out.println("Enter a 1 to enter one Word or 2 to enter a sentence you would like to search for:");
				 //read in the users choice
				 int usersWordOrSentence = keyboard.nextInt();
				 keyboard.nextLine();
				 
				 //use a switch statement to differentiate which the user wants to use
				 switch(usersWordOrSentence) {
				 	
				 case 1:
					 String usersTextInput1 = keyboard.nextLine();	
					    usersTextInput1.toLowerCase();
					    String results = compareWithMap(usersTextInput1,embeddingsMap);
					    
					    writeResultsToFile(OutputfileName, results);
					    System.out.println(GREEN+results+RESET);
					 break;
				
				 case 2:
					    System.out.println("Enter a sentence you would like to search for:");
			            String usersTextInput2 = keyboard.nextLine().toLowerCase();
			            List<String> tokens = Arrays.asList(usersTextInput2.split("\\s+"));

			            // Process each token using compareWithMap method
			            for (String token : tokens) {
			                String results2 = compareWithMap(token, embeddingsMap);
			                System.out.println(GREEN + results2 + RESET);
			        	    writeResultsToFile(OutputfileName, results2);
			            }
					 break;
				 }
			 
				 //get the embedding of the users word
				      
				    loadMainMenu();
				break;
				
			//exit the application 
			case 4:
				System.out.print(RED+"Exiting Application"+RESET);
				Thread.sleep(300);
				System.out.print(".");
				Thread.sleep(300);
				System.out.print(".");
				Thread.sleep(300);
				System.out.print(".");
				System.out.println(GREEN+"\nGoodBye :)"+RESET);
				appRunning = false;
				System.exit(0);
				break;
				
			//default choice if user enters an invalid character	
			default:
				System.out.println(RED+"Incorrect Value entered Try Again"+RESET);
				Thread.sleep(1000);
				loadMainMenu();
				break;
			}//end of switch
			
		}//end of while
		
	}// end of main

}
