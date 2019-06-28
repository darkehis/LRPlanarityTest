import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.io.Attribute;
import org.jgrapht.io.DOTExporter;
import org.jgrapht.io.DOTImporter;
import org.jgrapht.io.EdgeProvider;
import org.jgrapht.io.ImportException;
import org.jgrapht.io.VertexProvider;
import org.json.JSONException;
import org.json.JSONObject;

public class FileHandler
{
	
	public static void writeJson(JSONObject object,String filename)
	{
        try (FileWriter file = new FileWriter("./graphs/" + filename + ".json"))
        {

            file.write(object.toString());
            file.flush();

        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
	}
	
	public static JSONObject readJson(String filename)
	{
		String graphString = readFile(filename);
		JSONObject object = null;
		try
		{
			object = new JSONObject(graphString);
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		return object;
		
	}
	
	public static String readFile(String filename) 
	{
		String result = "";
		String directory = "./graphs/";
		String extension = ".json";
		try {
			BufferedReader br = new BufferedReader(new FileReader(directory + filename + extension));
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			while (line != null) {
				sb.append(line);
				line = br.readLine();
			}
			result = sb.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	public static void saveUndirectedGraphToDOT(LRUndirectedGraph graph,String filename)
	{
		FileWriter file;
		try
		{
			file = new FileWriter("./graphs/" + filename + ".dot");
			DOTExporter<Integer, DefaultEdge> originalGraphExporter = new DOTExporter<>();
			originalGraphExporter.exportGraph(graph, file);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}


	}
	
	public static void saveOrientedGraphToDOT(LROrientedDFSGraph graph,String filename)
	{
		FileWriter file;
		try
		{
			file = new FileWriter("./graphs/" + filename + ".dot");
			DOTExporter<Integer, DefaultEdge> exporter = new DOTExporter<>();
			exporter.exportGraph(graph, file);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void saveConstraintGraphToDOT(LRConstraintGraph graph,String filename)
	{
		FileWriter file;
		try
		{
			file = new FileWriter("./graphs/" + filename + ".dot");
			DOTExporter<Integer, DefaultWeightedEdge> exporter = new DOTExporter<>();
			exporter.exportGraph(graph, file);
		} catch (IOException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void saveDotFile(String dotString,String filename)
	{
		try (FileWriter file = new FileWriter("./graphs/" + filename + ".dot"))
        {

            file.write(dotString);
            file.flush();

        } 
        catch (IOException e) 
        {
            e.printStackTrace();
        }
	}
	
	public static LRUndirectedGraph readDotGraph(String filename)
	{
		DOTImporter<Integer, DefaultEdge> importer = new DOTImporter<>(new VertexProvider<Integer>()
		{

			@Override
			public Integer buildVertex(String id, Map<String, Attribute> mapAttr)
			{
				return Integer.decode(id);
			}
		}, new EdgeProvider<Integer, DefaultEdge>()
		{

			@Override
			public DefaultEdge buildEdge(Integer idFrom, Integer idTo, String label, Map<String, Attribute> mapAttr)
			{
				return new DefaultEdge();
			}
		});
		
		LRUndirectedGraph graph = new LRUndirectedGraph();
		FileReader reader;
		try
		{
			reader = new FileReader("graphs/" + filename + ".dot");
			importer.importGraph(graph, reader);
		} catch (FileNotFoundException e1)
		{
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ImportException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		graph.reset();
		return graph;
		
		
	}
	
	public static void writePerf(LRUndirectedGraph graph, long time)
	{
		try (FileWriter file = new FileWriter("performance.csv",true))
	    {

			String perf = graph.edgeSet().size() + "," + time + "\n";
	        file.write(perf);
	        file.flush();

	    } 
	    catch (IOException e) 
	    {
	        e.printStackTrace();
	    }
	}


}
