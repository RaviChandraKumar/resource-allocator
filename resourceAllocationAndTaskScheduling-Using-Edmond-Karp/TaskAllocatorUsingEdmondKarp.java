package EdmondKarp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TaskAllocatorUsingEdmondKarp 
{
	static int numberOfNodes, numberOfTasks;
	static int[] parent;
	static int[] values;
	static long starttime = 0;
	static long endTime = 0;
	static long totalTime = 0;

	/**
	 * Main method where execution starts and ends
	 * @param args
	 * @throws IOException
	 */
	public static void main(String args[]) throws IOException
	{
		createInputData();

        System.out.println("Path for the input file:");
        String taskFilePath = "out/production/Project/task.txt";
		System.out.println(taskFilePath);
        taskFilePath = taskFilePath.replace("/", File.separator);
        
        System.out.println("Path for output file:");
        String outputFilePath ="out/production/Project/output.txt";
		System.out.println(outputFilePath);
        outputFilePath = outputFilePath.replace("/", File.separator);
		
		TaskAllocator ta = new TaskAllocator();
		int graph[][] = ta.getGraph(taskFilePath);
		
		numberOfNodes = graph.length -1;
		parent = new int[numberOfNodes + 1];
		
		int[][] residualGraph = ta.getAllocation(graph, 1, numberOfTasks + 2);
		
		ta.generateOutput(residualGraph, graph, outputFilePath);
		totalTime=endTime-starttime;
		System.out.println("Recording time taken for algorithm in graphData.txt file.");
		File output = new File("out/production/Project/graphData.txt");
		PrintWriter writer = new PrintWriter(new FileWriter(output, true));
		writer.append("Nodes: " + numberOfNodes + " Time: " +totalTime  + System.lineSeparator());
		writer.close();

	}

	/**
	 * Generate the output to a file named: output.txt in the form : taskID : WorkerID/s
	 *
	 * @param residualGraph
	 * @param originalGraph
	 * @param outputFilePath
	 * @throws IOException
	 */
	public void generateOutput(int[][] residualGraph, int[][] originalGraph, String outputFilePath) throws IOException
	{		
		File output = new File(outputFilePath);
		PrintWriter writer = new PrintWriter(new FileWriter(output, false));
		try
		{
			if(output.exists() == false)
			{
	            output.createNewFile();
			}
		}	
		catch(IOException e)
		{
	        System.out.println("Error in writing output to file.");
		}
		
		for(int i = 2; i <= numberOfTasks + 1; i++)
		{
			for(int j = numberOfTasks + 3; j < residualGraph.length; j++)
			{
				if(originalGraph[i][j] == 1 && residualGraph[i][j] == 0)
				{												
					writer.append("TaskID " + (i-1) + " : Worker ID " + (j - numberOfTasks - 2) + System.lineSeparator());
					
				}
			}
		}
		writer.close();
		endTime=System.currentTimeMillis();
	}


	/**
	 * generating the inputData from the files where the input is specified
	 *
	 * @throws IOException
	 */
	public static void createInputData() throws IOException
	{
		
		BufferedReader br = null,br1 = null;
		try {
			br = new BufferedReader(new FileReader("out\\production\\Project\\TaskList.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
		
		String firstLine = br.readLine();
		try {
			br1 = new BufferedReader(new FileReader("out\\production\\Project\\worker.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
		
		String firstLine1 = br1.readLine();
		File output = new File("out\\production\\Project\\task.txt");
		PrintWriter writer = new PrintWriter(new FileWriter(output, false));
		writer.append(firstLine+" "+firstLine1+ System.lineSeparator());
		
		 while ((firstLine = br.readLine()) != null) {
	            Pattern p = Pattern.compile("<(.*?)>");
	            Matcher m = p.matcher(firstLine);
	            while (m.find()){
	                String inputLine = m.group(1);
	                System.out.println(inputLine);
	                String[] vals = inputLine.split(",");
	                writer.append(vals[0]);
	                Pattern pattern = Pattern.compile("'(.*?)'");
	                Matcher matcher = pattern.matcher(inputLine);
	                while (matcher.find()){
	                    String listOfWorkerIds = matcher.group(1);
	                    System.out.println(listOfWorkerIds);
	                    String[] vals1 = listOfWorkerIds.split(",");
	                    for (int i=0; i<vals1.length;i++){

	                        System.out.println(vals1[i]);
	                        writer.append(','+vals1[i]);
	                    }
	                    writer.append(System.lineSeparator());
	                }
	            }
	
		}

		writer.close();
		values=new int[Integer.parseInt(firstLine1)];
		int i=0;
		while ((firstLine1 = br1.readLine()) != null) {

			Pattern p = Pattern.compile("<(.*?)>");
			Matcher m = p.matcher(firstLine1);
			while (m.find()){
				 String inputLine = m.group(1);
					System.out.println(inputLine);
					String[] vals = inputLine.split(",");
					System.out.println(vals);
					values[i]=Integer.parseInt(vals[1]);
					i++;
			}
		}
	
	}

	/**
	 * Run EdmundKarp Algorithm to find the Maximum Bipartitite matching between tasks and workers
	 *
	 * @param graph
	 * @param source
	 * @param sink
	 * @return ResidualGraph
	 */
	public int[][] getAllocation(int[][] graph, int source, int sink)
	{
		int u, v, pathFlow = 0;
		int[][] residualGraph = new int[numberOfNodes + 1][numberOfNodes + 1];
		
        for (int sourceNode = 1; sourceNode <= numberOfNodes; sourceNode++)
        {
            for (int destinationNode = 1; destinationNode <= numberOfNodes; destinationNode++)
            {
                residualGraph[sourceNode][destinationNode] = graph[sourceNode][destinationNode];
            }
        }

        starttime =System.currentTimeMillis();
        while(hasPath(residualGraph, source, sink))
        {
        	pathFlow = Integer.MAX_VALUE;
        	for (v = sink; v != source; v = parent[v])
            {
                u = parent[v];
                pathFlow = Math.min(pathFlow, residualGraph[u][v]);
            }

            for (v = sink; v != source; v = parent[v])
            {
                u = parent[v];
                residualGraph[u][v] -= pathFlow;
                residualGraph[v][u] += pathFlow;
            }         	
        }
		return residualGraph;
	}

	/**
	 * BFS to find if path exists from given source to destination
	 *
	 * @param graph
	 * @param source
	 * @param sink
	 * @return
	 */

	public boolean hasPath(int[][] graph, int source, int sink)
	{
		boolean pathFound = false;
		boolean[] visited = new boolean[numberOfNodes + 1];
		Queue<Integer> queue = new LinkedList<Integer>();
		int currentElement, destination;
		
		for(int i = 1; i <= numberOfNodes; i++)
		{
			parent[i] = -1;
		} 
		
		queue.add(source);
		visited[source] = true;
		parent[source] = -1;
		
		while(!queue.isEmpty())
		{
			currentElement = queue.remove();
			destination = 1;
			
			while(destination <= numberOfNodes)
			{
				if(visited[destination] == false && graph[currentElement][destination] > 0)
				{
                    parent[destination] = currentElement;
                    queue.add(destination);
                    visited[destination] = true;
				}
				destination++;
			}
		}

		if(visited[sink] == true)
		{
			pathFound = true;
		}

		return pathFound;
	}


	/**
	 *  Construct the graph as a adjacency matrix from the input files
	 *
	 * @param taskFilePath - path to the file containing information of the tasks
	 * @return
	 * @throws IOException
	 */
	public int[][] getGraph(String taskFilePath) throws IOException
	{
		int i,j;
		
		BufferedReader br = new BufferedReader(new FileReader(taskFilePath));		
		String firstLine = br.readLine();
		String[] splittedFirstLine = firstLine.split(" ");
		int numberOfWorkers = Integer.parseInt(splittedFirstLine[1]);
		br.close();

		int[][] taskDetails = transferFileToMatrix(taskFilePath, numberOfWorkers);
		
		numberOfTasks = taskDetails.length;				
		int totalNumberOfNodes = numberOfWorkers + numberOfTasks;
		
		int[][] graph = new int[totalNumberOfNodes + 3][totalNumberOfNodes + 3];
				
		//connecting src node to all task nodes
		for(i = 2; i <= numberOfTasks + 1; i++)
		{
			graph[1][i] = 1;
		}

		//connecting all worker nodes to sink node
		for(i = numberOfTasks + 3,j=0; i < graph.length; i++,j++)
		{
			graph[i][numberOfTasks + 2] = values[j];
		}
		
		//using taskDetails to connect task and worker nodes
		for(i = 0; i < numberOfTasks; i++)
		{
			for(j = 1; j < numberOfWorkers + 1; j++)
			{
				if(taskDetails[i][j] != 0)
				{
					int columnNumber = taskDetails[i][j] + numberOfTasks + 2;
					graph[i+2][columnNumber] = 1;
				}
			}
		}
		return graph;
	}

	/**
	 *
	 * @param filePath
	 * @param numberOfWorkers
	 * @return
	 * @throws IOException
	 */
	public int[][] transferFileToMatrix(String filePath, int numberOfWorkers) throws IOException
	{
		BufferedReader br = new BufferedReader(new FileReader(filePath));
		String fileContents;
		try 
	    {
	        StringBuilder sb = new StringBuilder();
	        String line = br.readLine();

	        while (line != null) 
	        {
	            sb.append(line);
	            sb.append(System.lineSeparator());
	            line = br.readLine();
	        }
	        fileContents = sb.toString();
	    } 
	    finally 
	    {
	        br.close();
	    }
			
		String[] inputLines = fileContents.split(System.lineSeparator());
		String splittedFirstLine[] = inputLines[0].split(" ");
		int numberOfRows = Integer.parseInt(splittedFirstLine[0]);
        
		int[][] matrixToFill = new int[numberOfRows][numberOfWorkers + 3];
		
		for(int i = 0; i< numberOfRows; i++)
		{
			StringTokenizer st = new StringTokenizer(inputLines[i+1],",");
			for(int j = 0; j < numberOfWorkers + 3; j++)
			{
				if(st.hasMoreTokens())
				{
					matrixToFill[i][j] = Integer.parseInt(st.nextToken(","));
				}
			}
		}
		
		return matrixToFill;
	}
}
