import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.graph.SimpleWeightedGraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LRConstraintGraph extends SimpleWeightedGraph<Integer, DefaultWeightedEdge>
{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	
	public JSONObject generateJsonObject()
	{
		JSONObject object = new JSONObject();
		JSONArray jArrayVertex =  new JSONArray();
		
		for(Iterator<Integer> itV = vertexSet().iterator();itV.hasNext();)
		{
			int v = itV.next();
			jArrayVertex.put(v);
		}
		
		try
		{
			object.put("vertex", jArrayVertex);
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		JSONArray jArrayEdge = new JSONArray();
		
		for(Iterator<DefaultWeightedEdge> itE = this.edgeSet().iterator();itE.hasNext();)
		{
			DefaultWeightedEdge edge = itE.next();
			JSONArray jArrayInfoEdge = new JSONArray();
			jArrayInfoEdge.put(this.getEdgeSource(edge));
			jArrayInfoEdge.put(this.getEdgeTarget(edge));
			try
			{
				jArrayInfoEdge.put(this.getEdgeWeight(edge));
			} catch (JSONException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			jArrayEdge.put(jArrayInfoEdge);
		}
		
		try
		{
			object.put("edge", jArrayEdge);
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		
		return object;
	}

	public LRConstraintGraph()
	{
		super(DefaultWeightedEdge.class);
	}
	
	public Set<Integer> getNeighbors(int v)
	{
		Set<Integer> setNeighbors = new HashSet<>();
		
		Set<DefaultWeightedEdge> setEdge =  this.edgesOf(v);
		
		for(DefaultWeightedEdge edge : setEdge)
		{
			if(getEdgeSource(edge).equals(v))
			{
				setNeighbors.add(getEdgeTarget(edge));
			}
			else
			{
				setNeighbors.add(getEdgeSource(edge));
			}
		}
		
		return setNeighbors;
		
	}

}
