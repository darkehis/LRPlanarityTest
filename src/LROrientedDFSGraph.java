import java.util.AbstractMap.SimpleEntry;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.SimpleDirectedGraph;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class LROrientedDFSGraph extends SimpleDirectedGraph<Integer	, DefaultEdge>
{

	private static final long serialVersionUID = 1L;
	
	/*private Map<DefaultEdge, Integer> _mapSourceHeight;
	private Map<DefaultEdge,Integer> _mapTargetHeight;*/
	private Map<Integer,Integer> _mapVertexHeight;
	private Map<DefaultEdge,SortedSet<Integer>> _mapReturnEdgeHeight;
	private Map<Entry<DefaultEdge,Integer>,DefaultEdge> _mapReturnEdgeRepresentant;
	
	public LROrientedDFSGraph()
	{
		super(DefaultEdge.class);
	
		_mapVertexHeight = new HashMap<>();
		_mapReturnEdgeHeight = new HashMap<>();
		_mapReturnEdgeRepresentant = new HashMap<>();
	}
	
	public int targetHeight(DefaultEdge e)
	{
		return _mapVertexHeight.get(getEdgeTarget(e));
	}
	
	public int sourceHeight(DefaultEdge e)
	{
		return _mapVertexHeight.get(getEdgeSource(e));
	}
	
	public boolean is_backEdge(DefaultEdge e)
	{
		return sourceHeight(e)> targetHeight(e);
	}
	
	public int lowpt(DefaultEdge e)
	{
		if(_mapReturnEdgeHeight.get(e).isEmpty())
		{
			return Integer.MAX_VALUE;
		}
		else
		{
			return _mapReturnEdgeHeight.get(e).first();
		}
	}
	
	public void addReturnEdgeHeight(DefaultEdge parentEdge,DefaultEdge childEdge)
	{
		SortedSet<Integer> childReturnEdge = _mapReturnEdgeHeight.get(childEdge);
		SortedSet<Integer> parentReturnEdge = _mapReturnEdgeHeight.get(parentEdge);
		SortedSet<Integer> newReturnEdge = childReturnEdge.headSet(sourceHeight(parentEdge));
		parentReturnEdge.addAll(newReturnEdge);
		for(int height : newReturnEdge)
		{
			_mapReturnEdgeRepresentant.put(new SimpleEntry<>(parentEdge,height), _mapReturnEdgeRepresentant.get(new SimpleEntry<>(childEdge,height)));
		}
	
	}
	

	@Override
	public boolean addEdge(Integer sourceVertex, Integer targetVertex, DefaultEdge e)
	{
		_mapReturnEdgeHeight.put(e, new TreeSet<>());
		return super.addEdge(sourceVertex, targetVertex, e);
	}

	@Override
	public boolean addVertex(Integer v)
	{
		_mapVertexHeight.put(v, Integer.MAX_VALUE);
		return super.addVertex(v);
	}
	


	public boolean isFork(DefaultEdge e)
	{
		if(!is_backEdge(e) && this.outDegreeOf(this.getEdgeTarget(e)) > 1)
		{
			DefaultEdge e1,e2;
			Set<DefaultEdge> outgoingEdge = this.outgoingEdgesOf(this.getEdgeTarget(e));
			Iterator<DefaultEdge> itEdge = outgoingEdge.iterator();
			e1 = itEdge.next();
			e2 = itEdge.next();
			if(lowpt(e1)<Integer.MAX_VALUE && lowpt(e2) < Integer.MAX_VALUE)
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else
		{
			return false;
		}
	}
	
	
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
		
		for(Iterator<DefaultEdge> itE = this.edgeSet().iterator();itE.hasNext();)
		{
			DefaultEdge edge = itE.next();
			JSONArray jArrayInfoEdge = new JSONArray();
			jArrayInfoEdge.put(this.getEdgeSource(edge));
			jArrayInfoEdge.put(this.getEdgeTarget(edge));
			jArrayInfoEdge.put(sourceHeight(edge));
			jArrayInfoEdge.put(targetHeight(edge));
			jArrayInfoEdge.put(lowpt(edge));
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
	
	/*public LREdge parentEdge(LRVertex v)
	{
		if(incomingEdgesOf(v).isEmpty())
		{
			return null;
		}
		else
		{
			return incomingEdgesOf(v).iterator().next();
		}
	}*/
	
	public DefaultEdge parentEdge(int v)
	{
		if(incomingEdgesOf(v).isEmpty())
		{
			return null;
		}
		else
		{
			return incomingEdgesOf(v).iterator().next();
		}
	}
	
	public DefaultEdge parentEdge(DefaultEdge e)
	{
		return parentEdge(getEdgeSource(e));
	}
	


	
	public void setVertexHeight(int v,int height)
	{
		_mapVertexHeight.put(v, height);
	}
	
	public int getVertexHeight(int v) 
	{
		return _mapVertexHeight.get(v);
	}
	
	//adding return point height to the return edge of a backedge (backedges have only one return edge: themselve)
	public void updateBackEdgeReturnEdge(DefaultEdge e)
	{
		SortedSet<Integer> returnEdge = _mapReturnEdgeHeight.get(e);
		returnEdge.add(targetHeight(e));
		_mapReturnEdgeRepresentant.put(new SimpleEntry<>(e, targetHeight(e)), e);
		
	}
	
	public SortedSet<Integer> getReturnEdge(DefaultEdge e)
	{
		return _mapReturnEdgeHeight.get(e);
	}
	
	
	public DefaultEdge getReturnRepresentant(DefaultEdge edge,int height)
	{
		return _mapReturnEdgeRepresentant.get(new SimpleEntry<>(edge,height));
	}

	

	
	
	
	

	

}
