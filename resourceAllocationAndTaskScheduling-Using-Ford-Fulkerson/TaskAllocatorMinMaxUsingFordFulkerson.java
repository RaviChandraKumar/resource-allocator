package EdmondKarp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class TaskAllocatorMinMaxUsingFordFulkerson
{
	static int numberOfNodes, numberOfTasks;
	static int[] parent;
	static int[] minValuesForWorkers;
	static int[] maxValuesForWorkers;
	static int[] minValuesForTasks;
	static int[] maxValuesForTasks;
	static long starttime = 0;
	static long endTime = 0;
	static long totalTime = 0;
	
	public static void main(String args[]) throws IOException
	{
		createInputData();
		
        System.out.println("Enter path for the input file:");
        String taskFilePath = "out/production/Project/task.txt";
        taskFilePath = taskFilePath.replace("/", File.separator);
        
        System.out.println("Enter file path for output file:");
        String outputFilePath ="out/production/Project/outputMinMax.txt";
        outputFilePath = outputFilePath.replace("/", File.separator);
		File output = new File(outputFilePath);
		PrintWriter writer = new PrintWriter(new FileWriter(output, false));
        
        File timeComplexityFile = new File("out/production/Project/graphData.txt");
		PrintWriter timecomplexityWriter = new PrintWriter(new FileWriter(timeComplexityFile, true));
		
        TaskAllocatorMinMax ta = new TaskAllocatorMinMax();
		int graph[][] = ta.getGraph(taskFilePath);
		
		numberOfNodes = graph.length -1;
		parent = new int[numberOfNodes + 1];

		starttime =System.currentTimeMillis();
		// step 1 pass actual input graph with task capacity as Max Worker capacity as Min
		int[][] residualGraph = ta.getAllocation(graph, 1, numberOfTasks + 2);
		ta.generateOutput(residualGraph, graph, outputFilePath,1,writer);

		// prepare residual graph for step 2
		residualGraph = prepareResidualGraphForStep2(residualGraph);
		residualGraph = ta.getAllocation(residualGraph, 1, numberOfTasks + 2);
		ta.generateOutput(residualGraph, graph, outputFilePath,2,writer);

		// prepare residual graph for step 3		
		residualGraph = prepareResidualGraphForStep3(residualGraph);
		residualGraph = ta.getAllocation(residualGraph, 1, numberOfTasks + 2);
		endTime=System.currentTimeMillis();
		
		ta.generateOutput(residualGraph, graph, outputFilePath,3,writer);
		
		totalTime=endTime-starttime;
		timecomplexityWriter.append("Nodes: " + numberOfNodes + " Time: " +totalTime  + System.lineSeparator());
		timecomplexityWriter.close();
		writer.close();

	}
	
	public void generateOutput(int[][] residualGraph, int[][] originalGraph, String outputFilePath, int step, PrintWriter writer) throws IOException
	{		
		writer.append(System.lineSeparator() + "Step :: " + step + System.lineSeparator());
		writer.append("-----------------\n"+System.lineSeparator());
		
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
	}
	
	public static void createInputData() throws IOException
	{
		BufferedReader taskFileContent = null, workerFileContent = null;
		try {
			taskFileContent = new BufferedReader(new FileReader("out\\production\\Project\\TaskList.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
		
		String firstLine = taskFileContent.readLine();
		System.out.println(firstLine);
		try {
			workerFileContent = new BufferedReader(new FileReader("out\\production\\Project\\worker.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
			
		String firstLineOfWorkerInput = workerFileContent.readLine();
		
		File output = new File("out\\production\\Project\\task.txt");
		PrintWriter writer = new PrintWriter(new FileWriter(output, false));
		writer.append(firstLine+" "+firstLineOfWorkerInput+ System.lineSeparator());
		minValuesForTasks=new int[Integer.parseInt(firstLine)];
		maxValuesForTasks=new int[Integer.parseInt(firstLine)];
		int h=0;
		 while ((firstLine = taskFileContent.readLine()) != null) {
	            Pattern p = Pattern.compile("<(.*?)>");
	            Matcher m = p.matcher(firstLine);
	            while (m.find()){
	                String inputLine = m.group(1);
	                System.out.println(inputLine);
	                String[] vals = inputLine.split(",");
	                writer.append(vals[0]);
	                minValuesForTasks[h]=Integer.parseInt(vals[1]);
	                maxValuesForTasks[h]=Integer.parseInt(vals[2]);
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
	                h++;
	            }
	
		 }
		 
		 for (int i = 0; i < maxValuesForTasks.length; i++) {
			System.out.println("-- max cons"+maxValuesForTasks[i]);
		 }
		writer.close();
		
		minValuesForWorkers=new int[Integer.parseInt(firstLineOfWorkerInput)];
		maxValuesForWorkers=new int[Integer.parseInt(firstLineOfWorkerInput)];
		int i=0;
		while ((firstLineOfWorkerInput = workerFileContent.readLine()) != null) {
		
			Pattern p = Pattern.compile("<(.*?)>");
            Matcher m = p.matcher(firstLineOfWorkerInput);
            while (m.find()){
            	 String inputLine = m.group(1);
	                System.out.println(inputLine);
	                String[] vals = inputLine.split(",");
	                System.out.println(vals);
	                minValuesForWorkers[i]=Integer.parseInt(vals[1]);
	                maxValuesForWorkers[i]=Integer.parseInt(vals[2]);
	                i++;
            }
		}

	}
	
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

	public boolean hasPath(int[][] graph, int source, int sink)
	{
		boolean pathFound = false;
		boolean[] visited = new boolean[numberOfNodes + 1];
		Stack<Integer> stack = new Stack<Integer>();
		int currentElement, destination;

		for(int i = 1; i <= numberOfNodes; i++)
		{
			parent[i] = -1;
		}

		stack.push(source);
		visited[source] = true;
		parent[source] = -1;

		while(!stack.isEmpty())
		{
			currentElement = stack.pop();
			destination = 1;

			while(destination <= numberOfNodes)
			{
				if(visited[destination] == false && graph[currentElement][destination] > 0)
				{
					parent[destination] = currentElement;
					stack.push(destination);
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
	
	//generate the Input Graph to pass it to the Algorithm 
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
		for(i = 2,j=0; i <= numberOfTasks + 1; i++,j++)
		{
			graph[1][i] = maxValuesForTasks[j];
		}

		//connecting all worker nodes to sink node
		for(i = numberOfTasks + 3,j=0; i < graph.length; i++,j++)
		{
			graph[i][numberOfTasks + 2] = minValuesForWorkers[j];
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
	
	public static int[][] prepareResidualGraphForStep2(int[][] residualG)
	{

		for(int i=2, j=0; i <= numberOfTasks+1 ;i++, j++) {

			//for each task connected to source
			// check if the backedge is >= Min of that task
			if(residualG[i][1] >= minValuesForTasks[j]) {
				//remove all connections between src and taski
				residualG[1][i] = 0;
				residualG[i][1] = 0;
			}
			else {
				// adding edge weights from Source to Tasks as Min-BackEdgeWeight 
				residualG[1][i]= minValuesForTasks[j] - residualG[i][1];
				//remove all connections between src and taski
				residualG[i][1]=0;
			}
			
		}

		// adding edge weights from Worker to sink(t) as Max-Min of worker
		for(int i = numberOfTasks + 3,j=0; i < residualG.length; i++,j++)
		{
			residualG[i][numberOfTasks + 2] = maxValuesForWorkers[j] - minValuesForWorkers[j];
		}
		
		return residualG;
		
	}
	
	public static int[][] prepareResidualGraphForStep3(int[][] residualG)
	{

		for(int i=2, j=0; i <= numberOfTasks+1 ;i++, j++) {

			//for each task connected to source
			// check if the backedge is < Max of that task
			if(residualG[i][1] < maxValuesForTasks[j]) {
				//add forward edge between src and taski with MaxofTaski-backedgeofTaski
				residualG[1][i] = maxValuesForTasks[j]-residualG[i][1];
			}
		}

		return residualG;
		
	}
}