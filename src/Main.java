import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.json.JSONObject;

public class Main
{
	
	public static void main(String[] args)
	{
		String graphName;
		if(args.length == 0)
			graphName = "paperExample";
		else
			graphName = args[0];
		JSONObject test = FileHandler.readJson(graphName);
		LRUndirectedGraph graphTest = new LRUndirectedGraph(test);
		LROrientedDFSGraph orientedDFSGraph =  LRPartitionAlgo.generateLROrientedDFSGraph(graphTest);
		Set<DefaultEdge> edges = orientedDFSGraph.edgeSet();
		int nbBackEdge =0;
		for(DefaultEdge e : edges)
		{
			if(orientedDFSGraph.is_backEdge(e))
				nbBackEdge++;
		}
		System.out.println("DFS-Oriented has " + nbBackEdge + " back edges");
		FileHandler.writeJson(orientedDFSGraph.generateJsonObject(), graphName + "_oriented");
		LRConstraintGraph constraintGraph = LRPartitionAlgo.generateConstraintGraph(orientedDFSGraph);
		if(constraintGraph != null)
		{
			FileHandler.writeJson(constraintGraph.generateJsonObject(),graphName + "_constraint");
		}
		else 
		{
			System.out.println("Graph is not planar: constraint graph is impossible");
		}
		
	}

}
