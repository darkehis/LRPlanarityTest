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
			graphName = "paperExample";
		else
			graphName = args[0];

		
		LRUndirectedGraph graph = FileHandler.readDotGraph(graphName);
		

		System.out.println("Testing planarity of " + graphName);
		
		LRPartitionAlgo.LRCriterionPlanarityTest(graph,graphName);
		
		
		
		
		
	}

}
