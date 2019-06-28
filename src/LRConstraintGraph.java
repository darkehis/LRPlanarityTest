import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
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
	
	private Map<Integer,Boolean> _mapMarkedVertices;
	private Map<Integer,Integer> _mapParentVertex;
	private Map<Integer,Integer> _mapVertexHeight;
	
	
	
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
		//System.out.println("generated:" + jArrayEdge.length() + " edges.");
		
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
		_mapMarkedVertices = new HashMap<>();
		_mapParentVertex = new HashMap<>();
		_mapVertexHeight = new HashMap<>();
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
	
	public void resetMarkedVertices()
	{
		for(int vertex : vertexSet())
		{
			_mapMarkedVertices.put(vertex, false);
		}
	}
	
	public void markVertex(int vertex)
	{
		_mapMarkedVertices.put(vertex, true);
	}
	
	public boolean isMarked(int vertex)
	{
		return _mapMarkedVertices.get(vertex);
	}
	
	public void resetParentVertex()
	{
		for(int vertex : vertexSet())
		{
			_mapParentVertex.put(vertex, -1);
		}
	}
	
	public void resetVertexHeight()
	{
		for(int vertex : vertexSet())
		{
			_mapVertexHeight.put(vertex, -1);
		}
	}
	
	public void setVertexHeight(int vertex,int height)
	{
		_mapVertexHeight.put(vertex, height);
	}
	public int getVertexHeight(int vertex)
	{
		return _mapVertexHeight.get(vertex);
		
	}
	
	public int getParentVertex(int vertex)
	{
		/*if(vertex == -1)
		{
			return -1;
		}
		else
		{*/
			return _mapParentVertex.get(vertex);
		//}

	}
	
	public void setParentVertex(int vertex, int parent)
	{
		_mapParentVertex.put(vertex, parent);
	}
	

}
