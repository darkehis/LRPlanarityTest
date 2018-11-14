import java.util.Map;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.DOTImporter;
import org.jgrapht.io.EdgeProvider;
import org.jgrapht.io.VertexProvider;
import org.json.JSONObject;

public class Main
{
	
	public static void main(String[] args)
	{
		String graphName;
		if(args.length == 0)
			graphName = "petersen";
		else
			graphName = args[0];
		

		//graphName = "K_5";
		//JSONObject test = FileHandler.readJson(graphName);
		
		LRUndirectedGraph graph = FileHandler.readDotGraph(graphName);
		

		System.out.println("Testing planarity of " + graphName);
		
		LRPartitionAlgo.LRCriterionPlanarityTest(graph,graphName);
		
		
		

		
		/*FileHandler.saveUndirectedGraphToDOT(graphTest, graphName);
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
		System.out.println("DFS graph created:" + graphName + "_oriented.json");
		LRConstraintGraph constraintGraph = LRPartitionAlgo.generateConstraintGraph(orientedDFSGraph);
		if(constraintGraph != null)
		{
			FileHandler.writeJson(constraintGraph.generateJsonObject(),graphName + "_constraint");
			System.out.println("Constraint graph created:" + graphName + "_constraint.json");
			///TODO: test with odd cycles of the constraint graph
		}
		else 
		{
			System.out.println("Graph is not planar: constraint graph is impossible");
		}*/
		
		
		
	}

}
