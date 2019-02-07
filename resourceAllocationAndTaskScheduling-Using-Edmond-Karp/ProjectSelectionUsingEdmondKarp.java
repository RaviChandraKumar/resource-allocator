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


public class ProjectSelectionUsingEdmondKarp 
{
	static int numberOfNodes, numberOfProjects;
	static int[] parent;
	static int[] profitValuesForTasks;
	static long starttime = 0;
	static long endTime = 0;
	static long totalTime = 0;
	
	public static void main(String args[]) throws IOException
	{
		createInputData();
		
        System.out.println("Enter path for the input file:");
        String projectFilePath = "out/production/Project/project.txt";
        projectFilePath = projectFilePath.replace("/", File.separator);
        
        System.out.println("Enter file path for output file:");
        String outputFilePath ="out/production/Project/outputProjectSelection.txt";
        outputFilePath = outputFilePath.replace("/", File.separator);
		File output = new File(outputFilePath);
		PrintWriter writer = new PrintWriter(new FileWriter(output, false));
        
        File timeComplexityFile = new File("out/production/Project/graphData.txt");
		PrintWriter timecomplexityWriter = new PrintWriter(new FileWriter(timeComplexityFile, true));
		
		ProjectSelection ta = new ProjectSelection();
		int graph[][] = ta.getGraph(projectFilePath);
		
		System.out.println("Printing Graph Matrix!!");
		for(int i=1;i<numberOfProjects+3;i++) {
			System.out.println();
			for(int j=1;j<numberOfProjects+3;j++) {
				System.out.print(" " + graph[i][j]);
			}
		}
		
		numberOfNodes = graph.length -1;
		parent = new int[numberOfNodes + 1];

		starttime =System.currentTimeMillis();

		int[][] residualGraph = ta.getAllocation(graph, 1, numberOfProjects + 2);

		endTime=System.currentTimeMillis();
		
		ta.generateOutput(residualGraph, graph, outputFilePath,writer);
		
		totalTime=endTime-starttime;


		System.out.println("Printing Residual Matrix!!");
		for(int i=1;i<numberOfProjects+3;i++) {
			System.out.println();
			for(int j=1;j<numberOfProjects+3;j++) {
				System.out.print(" " + residualGraph[i][j]);
			}
		}
		
		timecomplexityWriter.append("Nodes: " + numberOfNodes + " Time: " +totalTime  + System.lineSeparator());
		timecomplexityWriter.close();
		writer.close();

	}
	
	public void generateOutput(int[][] residualGraph, int[][] originalGraph, String outputFilePath, PrintWriter writer) throws IOException
	{		
		System.out.println("Printing residual graph");
		
		for(int i = 2; i <= numberOfProjects + 1; i++)
		{
			for(int j = numberOfProjects + 3; j < residualGraph.length; j++)
			{
				if(originalGraph[i][j] == 1 && residualGraph[i][j] == 0)
				{												
					writer.append("TaskID " + (i-1) + " : Worker ID " + (j - numberOfProjects - 2) + System.lineSeparator());
					
				}
			}
		}
	}
	
	public static void createInputData() throws IOException
	{
		BufferedReader projectFileContent = null;
		try {
			projectFileContent = new BufferedReader(new FileReader("out\\production\\Project\\projectList.txt"));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}		
		
		String firstLine = projectFileContent.readLine();
		System.out.println(firstLine);
			
		File output = new File("out\\production\\Project\\project.txt");
		PrintWriter writer = new PrintWriter(new FileWriter(output, false));
		writer.append(firstLine+" "+System.lineSeparator());
		profitValuesForTasks=new int[Integer.parseInt(firstLine)];
		int h=0;
		 while ((firstLine = projectFileContent.readLine()) != null) {
	            Pattern p = Pattern.compile("<(.*?)>");
	            Matcher m = p.matcher(firstLine);
	            while (m.find()){
	                String inputLine = m.group(1);
	                System.out.println(inputLine);
	                String[] vals = inputLine.split(",");
	                writer.append(vals[0]);
	                profitValuesForTasks[h]=Integer.parseInt(vals[1]);
	                Pattern pattern = Pattern.compile("'(.*?)'");
	                Matcher matcher = pattern.matcher(inputLine);
	                while (matcher.find()){
	                    String listOfDependentProjIds = matcher.group(1);
	                    System.out.println(listOfDependentProjIds);
	                    String[] vals1 = listOfDependentProjIds.split(",");
	                    for (int i=0; i<vals1.length;i++){
	                        System.out.println(vals1[i]);
	                        writer.append(','+vals1[i]);
	                    }
	                    writer.append(System.lineSeparator());
	                }
	                h++;
	            }
		 }
		 
		 for (int i = 0; i < profitValuesForTasks.length; i++) {
			System.out.println("-- max cons"+profitValuesForTasks[i]);
		 }

		 writer.close();
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
	
	//BFS to find if path exists from given source to sink
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
	
	//generate the Input Graph to pass it to the Algorithm 
	public int[][] getGraph(String taskFilePath) throws IOException
	{
		int i,j;

		BufferedReader br = new BufferedReader(new FileReader(taskFilePath));		
		String firstLine = br.readLine();
		String[] splittedFirstLine = firstLine.split(" ");
		numberOfProjects = Integer.parseInt(splittedFirstLine[0]);
		br.close();
		int totalNumberOfNodes = numberOfProjects+3;
		
		int[][] graph = new int[totalNumberOfNodes][totalNumberOfNodes];
				
		//connecting src node to all task nodes
		for(i = 2,j=0; i <= numberOfProjects + 1; i++,j++)
		{
			//connecting edges from loss making projects to sink(t) with -ve weight
			if(profitValuesForTasks[j]<0) {
				graph[i][numberOfProjects + 2] = -profitValuesForTasks[j];
			}
			// connecting edges from source node to all profit making projects with +ve weight  
			else {
				graph[1][i] = profitValuesForTasks[j];
			}
		}
		
		int[][] taskDetails = transferFileToMatrix(taskFilePath);
		
		//using taskDetails to connect dependent projects
		for(i = 0; i < numberOfProjects; i++)
		{
			for(j = 1; j < numberOfProjects + 1; j++)
			{
				if(taskDetails[i][j] != 0)
				{
					int columnNumber = taskDetails[i][j] + 1;
					graph[i+2][columnNumber] = Integer.MAX_VALUE;
				}
			}
		}
		
		return graph;
	}
	
	public int[][] transferFileToMatrix(String filePath) throws IOException
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
        
		int[][] matrixToFill = new int[numberOfRows][numberOfProjects+3];
		
		for(int i = 0; i< numberOfRows; i++)
		{
			StringTokenizer st = new StringTokenizer(inputLines[i+1],",");
			for(int j = 0; j < numberOfProjects + 3; j++)
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