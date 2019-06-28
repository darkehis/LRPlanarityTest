import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleGraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LRUndirectedGraph extends SimpleGraph<Integer, DefaultEdge>
{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Map<Integer, Integer> _mapVertexHeight;
	private Set<DefaultEdge> _setMarkedEdge;
	
	
	
	
	
	public LRUndirectedGraph()
	{
		super(DefaultEdge.class);
		_mapVertexHeight = new HashMap<>();
		_setMarkedEdge = new HashSet<>();
	}
	
	
	
	@Override
	public boolean addVertex(Integer v)
	{
		_mapVertexHeight.put(v, Integer.MAX_VALUE);
		return super.addVertex(v);
	}



	public static LRUndirectedGraph ErdosRenyiGraph(int n,double p)
	{
		LRUndirectedGraph graph = new LRUndirectedGraph();
		for(int i =0;i<n;i++)
		{
			graph.addVertex(i);
		}
		for(int i =0;i<n;i++)
		{
			for(int j = i+1;j<n;j++)
			{
				if(Math.random() < p)
				{
					graph.addEdge(i,j);
				}
			}
		}
		
		return graph;
		
	}
	
	public LRUndirectedGraph(JSONObject object)
	{
		this();
		try
		{
			JSONArray jArrayVertexId =  object.getJSONArray("vertex");
			for(int i = 0;i<jArrayVertexId.length();i++)
			{
				int vertexId = jArrayVertexId.getInt(i);
				this.addVertex(vertexId);
			}
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try
		{
			JSONArray jArrayEdge = object.getJSONArray("edge");
			for (int i = 0; i < jArrayEdge.length(); i++)
			{
				JSONArray jArrayPair = jArrayEdge.getJSONArray(i);
				if(jArrayPair.length() == 2)
				{
					this.addEdge(jArrayPair.getInt(0), jArrayPair.getInt(1));
				}
				
			}
		} catch (JSONException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		reset();
		
	}
	
	public JSONObject generateJsonObject()
	{
		JSONObject object = new JSONObject();
		JSONArray jArrayVertex =  new JSONArray();
		
		for(Iterator<Integer> itV = vertexSet().iterator();itV.hasNext();)
		{
			int id = itV.next();
			jArrayVertex.put(id);
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
		
		for(Iterator<DefaultEdge> itE = this.edgeSet().iterator();itE.hasNext();)
		{
			DefaultEdge edge = itE.next();
			JSONArray jArrayInfoEdge = new JSONArray();
			jArrayInfoEdge.put(this.getEdgeSource(edge));
			jArrayInfoEdge.put(this.getEdgeTarget(edge));
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
	
	
	
	public Set<Integer> getNeighbors(int id)
	{
		Set<Integer> setNeighbors = new HashSet<>();
		
		Set<DefaultEdge> setEdge =  this.edgesOf(id);
		
		for(DefaultEdge edge : setEdge)
		{
			if(getEdgeSource(edge).equals(id))
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
	
	public SortedSet<Integer> getUnmarkedNeighbors(int v)
	{
		SortedSet<Integer> unmarkerNeighbors = new TreeSet<>();

		Set<Integer> neighbors = getNeighbors(v);
		for(int neighbor : neighbors)
		{
			if(_mapVertexHeight.get(neighbor) == Integer.MAX_VALUE)
			{
				unmarkerNeighbors.add(neighbor);
			}
		}
		
		return unmarkerNeighbors;
	}
	

	
	public void reset()
	{
		Set<Integer> vertices = vertexSet();
		for(int v : vertices)
		{
			_mapVertexHeight.put(v, Integer.MAX_VALUE);
		}
		_setMarkedEdge.clear();
		
	}
	
	public void setVertexHeight(int v,int height)
	{
		_mapVertexHeight.put(v, height);
	}
	
	public int getVertexHeight(int v)
	{
		return _mapVertexHeight.get(v);
	}
	
	public DefaultEdge getUnmarkedEdge(Integer v)
	{
		Set<DefaultEdge> incidentEdges =  new HashSet<>( edgesOf(v));
		incidentEdges.removeAll(_setMarkedEdge);
		if(!incidentEdges.isEmpty())
		{
			return incidentEdges.iterator().next();
		}
		else
		{
			return null;
		}
	}
	
	public void setEdgesMarked(DefaultEdge e)
	{
		_setMarkedEdge.add(e);
	}
	

	

}
