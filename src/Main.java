import org.json.JSONObject;

public class Main
{
	
	public static void main(String[] args)
	{
		String graphName = "K_5";
		JSONObject test = FileHandler.readJson(graphName);
		LRUndirectedGraph graphTest = new LRUndirectedGraph(test);
		LROrientedDFSGraph orientedDFSGraph =  LRPartitionAlgo.generateLROrientedDFSGraph(graphTest);
		FileHandler.writeJson(orientedDFSGraph.generateJsonObject(), graphName + "_oriented");
		LRConstraintGraph constraintGraph = LRPartitionAlgo.generateConstraintGraph(orientedDFSGraph);
		if(constraintGraph != null)
		{
			FileHandler.writeJson(constraintGraph.generateJsonObject(),graphName + "_constraint");
		}
		else 
		{
			System.out.println("Graph is not planar: constrainnt graph is impossible");
		}
		
	}

}
