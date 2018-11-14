import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.NestingKind;

import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.commons.lang3.time.DateFormatUtils;
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
	private Map<DefaultEdge,Integer> _mapEdgeLowpt;
	private Map<DefaultEdge,Integer> _mapEdgeLowpt2;
	private Map<DefaultEdge,Integer> _mapEdgeNestingDepth;
	private Map<DefaultEdge,DefaultEdge> _mapEdgeRef;
	//private Map<DefaultEdge,Integer> _mapEgdeSign;
	private Deque<ConflictPair> _stackConflicPair;
	private Map<DefaultEdge,Integer> _mapStackBottom;
	private Map<Integer,DefaultEdge> _mapParentEdge;
	//private Map<Integer,SortedMap<Integer, DefaultEdge>> _sortedMapOutgoingEdge;
	//private Map<Integer,MultiValuedMap<Integer, DefaultEdge>> _sortedMapOutgoingEdge;
	private Map<Integer,SortedSet<DepthEdgePair>> _sortedMapOutgoingEdge;
	private Map<DefaultEdge,DefaultEdge> _mapEdgeLowptEdge;
	private Map<DefaultEdge,Integer> _mapEdgeSide;
	private Map<Integer,Deque<DefaultEdge>> _mapEdgeDrawingOrder;
	
	
	private Map<Integer,DefaultEdge> _mapLeftRef;
	private Map<Integer,DefaultEdge> _mapRightRef;
	private Map<Integer,EdgeList> _mapOrderedAdjList;
	
	//useless now
	private Map<DefaultEdge,SortedSet<Integer>> _mapReturnEdgeHeight;
	private Map<Entry<DefaultEdge,Integer>,DefaultEdge> _mapReturnEdgeRepresentant;
	
	public LROrientedDFSGraph()
	{
		super(DefaultEdge.class);
	
		_mapVertexHeight = new HashMap<>();
		_mapEdgeLowpt = new HashMap<>();
		_mapEdgeLowpt2 = new HashMap<>();
		_mapEdgeNestingDepth = new HashMap<>();
		_mapEdgeRef = new HashMap<>();
		_stackConflicPair = new ArrayDeque<>();
		_mapStackBottom = new HashMap<>();
		_mapParentEdge = new HashMap<>();
		_sortedMapOutgoingEdge = new HashMap<>();
		_mapEdgeLowptEdge = new HashMap<>();
		_mapEdgeSide = new HashMap<>();
		_mapEdgeDrawingOrder = new HashMap<>();
		
		_mapLeftRef = new HashMap<>();
		_mapRightRef = new HashMap<>();
		_mapOrderedAdjList = new HashMap<>();
		
		
		
		
		
		
		
		
		
		
		_mapReturnEdgeHeight = new HashMap<>();
		_mapReturnEdgeRepresentant = new HashMap<>();
	}
	
	
	public LROrientedDFSGraph(LRUndirectedGraph graph)
	{
		this();
		Set<Integer> setVertices = graph.vertexSet();
		for(int v : setVertices)
		{
			addVertex(v);
		}
	}
	public int targetHeight(DefaultEdge e)
	{
		return getVertexHeight(getEdgeTarget(e));
	}
	
	public int sourceHeight(DefaultEdge e)
	{
		return getVertexHeight(getEdgeSource(e));
	}
	
	public boolean is_backEdge(DefaultEdge e)
	{
		return sourceHeight(e)> targetHeight(e);
	}
	
	/*public int lowpt(DefaultEdge e)
	{
		if(_mapReturnEdgeHeight.get(e).isEmpty())
		{
			return -1;
		}
		else
		{
			return _mapReturnEdgeHeight.get(e).first();
		}
	}*/
	
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
		_mapEdgeSide.put(e, 1);
		//System.out.println("adding edge e:" + e + " with side:" + _mapEdgeSide.get(e));
		return super.addEdge(sourceVertex, targetVertex, e);
	}

	@Override
	public boolean addVertex(Integer v)
	{
		_mapVertexHeight.put(v, Integer.MAX_VALUE);
		_mapEdgeDrawingOrder.put(v, new ArrayDeque<>());
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
			if(getLowpt(e1)<Integer.MAX_VALUE && getLowpt(e2) < Integer.MAX_VALUE)
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
			jArrayInfoEdge.put(getLowpt(edge));
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
	

	public DefaultEdge parentEdge(int v)
	{
		if(_mapParentEdge.containsKey(v))
		{
			return _mapParentEdge.get(v);
		}
		else
		{
			return null;
		}
	}
	
	public void setParentEdge(int v,DefaultEdge e)
	{
		_mapParentEdge.put(v, e);
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
		if(_mapVertexHeight.containsKey(v))
		{
			return _mapVertexHeight.get(v);
		}
		else
		{
			return Integer.MAX_VALUE;
		}
	
	}
	
	public void resetVertexHeight()
	{
		/*for(int v : vertexSet())
		{
			_mapVertexHeight.put(v, -1);
		}*/
		_mapVertexHeight.clear();
	}
	
	public void reset()
	{
		_mapVertexHeight.clear();
		_mapEdgeNestingDepth.clear();
		_mapEdgeLowpt.clear();
		_mapEdgeLowpt2.clear();
		_mapEdgeRef.clear();
		//_mapEgdeSign.clear();
		_mapParentEdge.clear();
		_mapStackBottom.clear();
		_stackConflicPair.clear();
		_mapEdgeLowptEdge.clear();
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

	
	public void setLowpt(DefaultEdge e,int lowpt)
	{
		_mapEdgeLowpt.put(e, lowpt);
	}
	
	public int getLowpt(DefaultEdge e)
	{
		if(_mapEdgeLowpt.containsKey(e))
		{
			return _mapEdgeLowpt.get(e);
		}
		else
		{
			return Integer.MAX_VALUE;
		}
	}
	
	public void setLowpt2(DefaultEdge e,int lowpt2)
	{
		_mapEdgeLowpt2.put(e, lowpt2);
	}
	
	public int getLowpt2(DefaultEdge e)
	{
		if(_mapEdgeLowpt2.containsKey(e))
		{
			return _mapEdgeLowpt2.get(e);
		}
		else
		{
			return Integer.MAX_VALUE;
		}
	}
	
	public void setNestingDepth(DefaultEdge e,int depth)
	{
		_mapEdgeNestingDepth.put(e, depth);
	}
	
	public int getNestingDepth(DefaultEdge e)
	{
		if(_mapEdgeNestingDepth.containsKey(e))
		{
			return _mapEdgeNestingDepth.get(e);
		}
		else
		{
			return -1;
		}
	}
	
	public void sortOutGoingEdge()
	{
		//System.out.println("sorting edges according to nesting depth:");
		for(int v : vertexSet())
		{
			//System.out.println("sorting edges of:" + v + " with:" + outgoingEdgesOf(v).size() + " outgoing edges");
			SortedSet<DepthEdgePair> edgeSet = new TreeSet<>();
			for(DefaultEdge e : outgoingEdgesOf(v))
			{
				int depth = getNestingDepth(e);
				//System.out.println("checking edge:" + e + " of depth:" + depth);
				DepthEdgePair p = new DepthEdgePair(depth, e);
				edgeSet.add(p);

			}
			//System.out.println("so sorted edge:");
			for(DepthEdgePair p : edgeSet)
			{
				//System.out.println(p.second() + "at depth "+ p.first());
			}
			_sortedMapOutgoingEdge.put(v, edgeSet);
		}
	}
	
	public SortedSet<DepthEdgePair> getSortedOutgoingEdges(int v)
	{
		if(_sortedMapOutgoingEdge.containsKey(v))
		{
			
			return _sortedMapOutgoingEdge.get(v);
		}
		else
		{
			return new TreeSet<>();
		}
	}
	
	public void setStackBottom(DefaultEdge e, int i)
	{
		_mapStackBottom.put(e, i);
	}
	
	public Deque<ConflictPair> getStackConflictPair()
	{
		return _stackConflicPair;
	}
	
	public void setLowptEdge(DefaultEdge e1,DefaultEdge e2)
	{
		_mapEdgeLowptEdge.put(e1, e2);
	}
	
	public DefaultEdge getLowptEdge(DefaultEdge e)
	{
		if(_mapEdgeLowptEdge.containsKey(e))
		{
			return _mapEdgeLowptEdge.get(e);
		}
		else
		{
			return null;
		}
	}
	
	public void pushStack(ConflictPair pair)
	{
		_stackConflicPair.push(pair);
	}
	
	
	public ConflictPair getStackTop()
	{
		if(!_stackConflicPair.isEmpty())
		{
			return _stackConflicPair.getFirst();
		}
		else
		{
			return null;
		}
	}
	
	public void setEdgeRef(DefaultEdge e1,DefaultEdge e2)
	{
		//System.out.println("setting REF of:" + e1 + " at:" + e2);
		_mapEdgeRef.put(e1, e2);
	}
	
	public DefaultEdge getEdgeRef(DefaultEdge e)
	{
		if(_mapEdgeRef.containsKey(e))
		{
			return _mapEdgeRef.get(e);
		}
		else
		{
			return null;
		}
	}
	
	public int getStackBottom(DefaultEdge e)
	{
		if(_mapStackBottom.containsKey(e))
		{
			return _mapStackBottom.get(e);
		}
		else
		{
			return -1;
		}
	}
	
	public ConflictPair popStack()
	{
		if(!_stackConflicPair.isEmpty())
		{
			return _stackConflicPair.pop();
		}
		else
		{
			return null;
		}

	}
	
	public Deque<ConflictPair> getStack()
	{
		return _stackConflicPair;
	}
	
	public void setSide(DefaultEdge e, int side)
	{
		_mapEdgeSide.put(e, side);
	}
	
	public Integer getSide(DefaultEdge e)
	{
		return _mapEdgeSide.get(e);
	}

	public String getStackDesc()
	{
		String desc = "";
		for(ConflictPair p : _stackConflicPair)
		{
			desc += p + "\n";
		}
		return desc;
	}
	
	public void drawingOrderAddFirst(int vertex,DefaultEdge e)
	{
		_mapEdgeDrawingOrder.get(vertex).addFirst(e);
	}
	
	public void drawingOrderAddLast(int vertex,DefaultEdge e)
	{
		_mapEdgeDrawingOrder.get(vertex).addLast(e);
	}
	
	public String generateDOTString()
	{
		String dotString = "strict digraph G {\n";
		
		//rank direction
		dotString += "rankdir = BT;\n";
		
		//edge order
		dotString += "ordering = out;\n";
		
		//rank = height of vertex
		SortedMap<Integer, Set<Integer>> vertexRanks = new TreeMap<>();
		
		
		//adding vertices
		for(int u : vertexSet())
		{
			dotString += u + ";\n";
			int height = getVertexHeight(u);
			if(vertexRanks.containsKey(height))
			{
				vertexRanks.get(height).add(u);
			}
			else
			{
				Set<Integer> list = new HashSet<>();
				list.add(u);
				vertexRanks.put(height, list);
			}
		}
		
		//adding edges :beware the order of writing-> impose clockwise order of outgoing edges
		for(int u : vertexSet())
		{
			for(DepthEdgePair pair : _sortedMapOutgoingEdge.get(u))
			{
				DefaultEdge e = pair.second();
				dotString += getEdgeSource(e) + " -> " + getEdgeTarget(e);
				if(targetHeight(e) < sourceHeight(e))
				{
					//p.second is a back edge
				}
				else
				{
					
				}
				
				dotString += " [taillabel = " + Integer.toString(getNestingDepth(e)) + "];\n";
			}
		}
		
		for(Entry<Integer, Set<Integer>> entry : vertexRanks.entrySet())
		{
			dotString += "{ rank = same;";
			for(int v : entry.getValue())
			{
				dotString += v + ";";
			}
			
			
			dotString += "}\n";
		}
		
		
		
		dotString += "}\n";
		return dotString;
		
	}
	
	
	public DefaultEdge getLeftRef(int v)
	{
		return _mapLeftRef.get(v);
	}
	
	public DefaultEdge getRightRef(int v)
	{
		return _mapRightRef.get(v);
	}
	
	public void setLeftRef(int v,DefaultEdge l)
	{
		_mapLeftRef.put(v, l);
	}
	
	public void setRightRef(int v, DefaultEdge l)
	{
		_mapRightRef.put(v, l);
	}
	
	
	public void setOrderedAdjLis(int v,EdgeList l)
	{
		_mapOrderedAdjList.put(v, l);
	}
	
	public EdgeList getOrderedAdjList(int v)
	{
		return _mapOrderedAdjList.get(v);
	}
	
	
	

	

}
