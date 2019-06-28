import java.util.AbstractMap.SimpleEntry;
import java.nio.file.attribute.DosFileAttributeView;
import java.sql.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import javax.lang.model.element.NestingKind;
import javax.swing.plaf.synth.SynthScrollPaneUI;

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
	
	
	public void completeDesc()
	{
		System.out.println("Oriented graph:" + vertexSet().size() + " vertices and " + edgeSet().size() + " edges");
		
		System.out.println("Vertex height");
		
		for(int v : vertexSet())
		{
			System.out.println(v + "::" + _mapVertexHeight.get(v));
		}
		System.out.println("\n");
		
		System.out.println("Lowpt:");
		for(DefaultEdge e : edgeSet())
		{
			System.out.println("(" + getEdgeSource(e) + "," + getEdgeTarget(e) + ")::" + _mapEdgeLowpt.get(e));
		}
		
		System.out.println("\n");
		
		System.out.println("Lowpt2:");
		for(DefaultEdge e : edgeSet())
		{
			System.out.println("(" + getEdgeSource(e) + "," + getEdgeTarget(e) + ")::" + _mapEdgeLowpt2.get(e));
		}
		System.out.println("\n");
		
		System.out.println("Nesting Depth:");
		for(DefaultEdge e : edgeSet())
		{
			System.out.println("(" + getEdgeSource(e) + "," + getEdgeTarget(e) + ")::" + _mapEdgeNestingDepth.get(e));
		}
		System.out.println("\n");
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
	
	public void updateLowpt(DefaultEdge backEdge)
	{
		//System.out.println("Updating lowpt for backedge:" + backEdge);
		int backEdgeLowpt = targetHeight(backEdge);
		setLowpt(backEdge, backEdgeLowpt);
		DefaultEdge parentEdge = parentEdge(backEdge);
		while(parentEdge != null && sourceHeight(parentEdge) > backEdgeLowpt)
		{
			if(getLowpt(parentEdge) > backEdgeLowpt)
			{
				setLowpt(parentEdge, backEdgeLowpt);
			}
			parentEdge = parentEdge(parentEdge);
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
			//System.out.println("We have lowpt(e1)=" + getLowpt(e1) + " and lowpt(e2)=" + getLowpt(e2) );
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
		if(!e1.equals(e2))
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
	

	
	public void DFSVertexPosition(int currentVertex,int childNb,Map<Integer,Pair<Double,Integer>> mapPosition)
	{
		Pair<Double,Integer> position = new Pair<>(0.0,getVertexHeight(currentVertex));
		DefaultEdge parentEdge = parentEdge(currentVertex);
		if(parentEdge != null)
		{
			int parentVertex = getEdgeSource(parentEdge);
			Pair<Double,Integer> parentPosition = mapPosition.get(parentVertex);
			double posX = parentPosition.first() - (1.0/Math.pow(2.0, new Double(getVertexHeight(parentVertex))))*(1.0/(Math.pow(2, new Double(childNb+1)))-1.0/2.0);
			
			//System.out.println("position of vertex:" + currentVertex + " " + childNb + " child of parent:" + parentVertex + " get xPosition:" + posX);
			position.setFirst(posX);
		}
		else
		{
			position.setFirst(0.0);
		}
		mapPosition.put(currentVertex, position);
		int i = 0;
		for(DepthEdgePair depthEdgePair : getSortedOutgoingEdges(currentVertex))
		{
			if(!is_backEdge(depthEdgePair.second()))
			{
				int childVertex = getEdgeTarget(depthEdgePair.second());
				DFSVertexPosition(childVertex, i, mapPosition);
				i++;
			}
		}
	}
	
	
	
	
	public String generateDOTString2()
	{
		String dotString = "strict graph G {\n";
		
		Map<Integer,Pair<Double,Integer>> mapPosition = new HashMap<>();
		DFSVertexPosition(0, 0, mapPosition);
		SortedSet<Double> xPositon = new TreeSet<>();
		for(Entry<Integer,Pair<Double,Integer>> entry : mapPosition.entrySet())
		{
			xPositon.add(entry.getValue().first());
		}
		Map<Double,Integer> xPositionIntEquivalent = new HashMap<>();
		
		int integerXPosition = 0;
		for(double xPos:xPositon)
		{
			xPositionIntEquivalent.put(xPos, integerXPosition);
			integerXPosition++;
		}
		
		
		//adding edges :beware the order of writing-> impose clockwise order of outgoing edges
		for(int u : vertexSet())
		{
			Pair<Double, Integer> position = mapPosition.get(u);
			dotString += u + "[pos=\"" + xPositionIntEquivalent.get(position.first()) + "," + position.second() + "!\"];\n";
		}
		
		for(DefaultEdge e : edgeSet())
		{
			dotString += getEdgeSource(e) + "--" + getEdgeTarget(e) + "[color=";
			if(!is_backEdge(e))//tree edge are in blue
			{
				dotString += "\"blue\"";
				
			}
			else //backEdge are in red if left, and green if right
			{
				dotString += "\"red\"";
			}
			
			dotString += " taillabel=" + _mapEdgeNestingDepth.get(e);
			
			dotString += "];";
			
			dotString += "\n";
		}
		
		
		dotString += "}\n";
		return dotString;
	}
	
	public int DFSVertexPosition2(int currentVertex,int nbLeafParentSubtreeLeft,Map<Integer,Pair<Integer,Integer>> mapPosition,
			Map<DefaultEdge,Pair<Integer,Integer>> mapPosBackEdge,
			Map<Integer,Pair<Integer,Integer>> mapSubtreeDimension,
			int maxNestingDepth,
			Map<Integer,Integer> mapMaxDiffNestingDepth
			
			)
	{
		Pair<Integer,Integer> position = new Pair<>(0,getVertexHeight(currentVertex));
		DefaultEdge parentEdge = parentEdge(currentVertex);
		if(parentEdge != null)
		{
			int parentVertex = getEdgeSource(parentEdge);
			Pair<Integer,Integer> parentPosition = mapPosition.get(parentVertex);
			//double posX = parentPosition.first() - (1.0/Math.pow(2.0, new Double(getVertexHeight(parentVertex))))*(1.0/(Math.pow(2, new Double(childNb+1)))-1.0/2.0);
			int posX = parentPosition.first() + nbLeafParentSubtreeLeft;
			//System.out.println("position of vertex:" + currentVertex + " " + childNb + " child of parent:" + parentVertex + " get xPosition:" + posX);
			position.setFirst(posX);
		}
		else
		{
			position.setFirst(0);
		}
		mapPosition.put(currentVertex, position);
		int i = 0;
		int nbBackEdge = 0;
		int nbLeafSubtreeLeft = 0;
		int maxDiffNestingDepth = maxNestingDepth;
		int previousChildMaxDiffNestingDepth = 0;
		int maxWidth = 0;
		int maxHeight = 0;
		for(DepthEdgePair depthEdgePair : getSortedOutgoingEdges(currentVertex))
		{
			if(!is_backEdge(depthEdgePair.second()))
			{
				int childVertex = getEdgeTarget(depthEdgePair.second());
				nbLeafSubtreeLeft += DFSVertexPosition2(childVertex, nbLeafSubtreeLeft, mapPosition,mapPosBackEdge, mapSubtreeDimension,maxNestingDepth,mapMaxDiffNestingDepth);
				Pair<Integer,Integer> childSubtreeDimension = mapSubtreeDimension.get(childVertex);
				maxWidth += childSubtreeDimension.first();
				
				if(maxHeight < childSubtreeDimension.second())
				{
					maxHeight = childSubtreeDimension.second();
				}
				previousChildMaxDiffNestingDepth = mapMaxDiffNestingDepth.get(childVertex);
			}
			else
			{
				//only used for passage points of backedge
				Pair<Integer,Integer> posEdge = new Pair<>(position.first() + nbLeafSubtreeLeft + previousChildMaxDiffNestingDepth,position.second() + +1);
				mapPosBackEdge.put(depthEdgePair.second(), posEdge);
				nbLeafSubtreeLeft++;
				nbBackEdge++;
				if(maxDiffNestingDepth < maxNestingDepth-depthEdgePair.first())
					maxDiffNestingDepth = maxNestingDepth-depthEdgePair.first();
			}
			i++;
		}
		
		maxWidth += nbBackEdge;
		//maxWidth += maxNestingDepth;
		if(maxWidth == 0)
			maxWidth = 1;
		//maxHeight+= nbBackEdge;
		Pair<Integer,Integer> subtreeDimension = new Pair<>(maxWidth,maxHeight+1);
		mapSubtreeDimension.put(currentVertex, subtreeDimension);
		mapMaxDiffNestingDepth.put(currentVertex, maxDiffNestingDepth);
		if(nbLeafSubtreeLeft == 0)
		{
			
			return 1;
		}
			
		else
		{
			return nbLeafSubtreeLeft;
		}
			
		
	}
	
	public String generateDOTString3()
	{
		String dotString = "strict graph G {\n";
		dotString += "graph [layout=\"fdp\",splines=line,overlap=true];\n";
		
		Map<Integer,Pair<Integer,Integer>> mapPosition = new HashMap<>();
		Map<Integer,Pair<Integer,Integer>> mapSubtreeDimension = new HashMap<>();
		Map<DefaultEdge,Pair<Integer,Integer>> mapPosBackEdge = new HashMap<>();
		Map<Integer,Integer> mapMaxDiffNestingDepth = new HashMap<>();
		
		
		int maxNestingDepth = 0;
		for(DefaultEdge e : edgeSet())
		{
			if(is_backEdge(e))
			{
				if(maxNestingDepth< getNestingDepth(e))
				{
					maxNestingDepth = getNestingDepth(e);
				}
			}
		}
		
		DFSVertexPosition2(0, 0, mapPosition,mapPosBackEdge,mapSubtreeDimension,maxNestingDepth,mapMaxDiffNestingDepth);
		for(int v: vertexSet() )
		{
			System.out.println("vertex subtree of :" + v + " is " + mapSubtreeDimension.get(v));
		}
		
		
		//adding edges :outgoing edges in clockwise order
		int nbTotal = 0;
		for(int u : vertexSet())
		{
			Pair<Integer, Integer> position = mapPosition.get(u);
			dotString += u + "[pos=\"" + position.first()+ "," + position.second() + "!\"];\n";
			nbTotal++;
		}
		
		
		
		//adding new passage points for backedges
		Map<DefaultEdge, Integer[]> mapBackEdgePassagePoint = new HashMap<>(); 
		
		for(DefaultEdge e : edgeSet())
		{
			if(is_backEdge(e))
			{
				Integer[] passagePoint = new Integer[6];
				int sourceVertex = getEdgeSource(e);
				int targetVertex = getEdgeTarget(e);
				passagePoint[0] = sourceVertex;
				passagePoint[5] = targetVertex;
				int lastButOneAncestor = sourceVertex;
				int previousVertex = lastButOneAncestor;
				while(lastButOneAncestor != targetVertex)
				{
					previousVertex = lastButOneAncestor;
					lastButOneAncestor = getEdgeSource(parentEdge(lastButOneAncestor));
				}
				lastButOneAncestor = previousVertex;
				System.out.println("last but on ancestor of " + e + " is " + lastButOneAncestor);
				System.out.println("pos back edge:" + mapPosBackEdge.get(e));
				passagePoint[1] = nbTotal;
				dotString += nbTotal + "[pos=\"" + mapPosBackEdge.get(e).first()+ "," + mapPosBackEdge.get(e).second() + "!\", shape=\"point\", width=0];\n";
				nbTotal++;
				
				passagePoint[2] = nbTotal;
				int posY = mapPosition.get(lastButOneAncestor).second() + mapSubtreeDimension.get(lastButOneAncestor).second() + (maxNestingDepth - getNestingDepth(e))/2;
				dotString += nbTotal + "[pos=\"" + mapPosBackEdge.get(e).first() + "," + posY + "!\", shape=\"point\", width=0];\n";
				nbTotal++;
				
				
				passagePoint[3] = nbTotal;
				int posX = mapPosition.get(lastButOneAncestor).first() + mapSubtreeDimension.get(lastButOneAncestor).first() + (maxNestingDepth - getNestingDepth(e))/2;
				dotString += nbTotal + "[pos=\"" + posX + "," + posY + "!\", shape=\"point\", width=0];\n";
				nbTotal++;
				
				passagePoint[4] = nbTotal;
				dotString += nbTotal + "[pos=\"" + posX + "," + mapPosition.get(targetVertex).second() + "!\", shape=\"point\", width=0];\n";
				nbTotal++;
				
				
				
				
				mapBackEdgePassagePoint.put(e, passagePoint);
			}
		}
		
		System.out.println(nbTotal);
		
		for(DefaultEdge e : edgeSet())
		{
			
			if(!is_backEdge(e))//tree edge are in blue
			{
				dotString += getEdgeSource(e) + "--" + getEdgeTarget(e) + "[color=\"blue\"";

				dotString += ", taillabel=" + _mapEdgeNestingDepth.get(e);
				
				dotString += "];";
				
				dotString += "\n";			}
			else //backEdge are in red if left and green if right
			{
				Integer[] passagePoint = mapBackEdgePassagePoint.get(e);
				
					
				dotString += passagePoint[0] + "--" + passagePoint[1] + "[color=";
				if(LRPartitionAlgo.sign(this, e) == 1)
					dotString += "\"red\"";
				else
					dotString += "\"green\"";
				dotString += "];";
				dotString += "\n";
				
				
				
				dotString += passagePoint[1] + "--" + passagePoint[2] + "[color=";
				if(LRPartitionAlgo.sign(this, e) == 1)
					dotString += "\"red\"";
				else
					dotString += "\"green\"";

				dotString += "];";
				dotString += "\n";
				
				
				dotString += passagePoint[2] + "--" + passagePoint[3] + "[color=";
				if(LRPartitionAlgo.sign(this, e) == 1)
					dotString += "\"red\"";
				else
					dotString += "\"green\"";

				dotString += "];";
				dotString += "\n";
				
				
				
				dotString += passagePoint[3] + "--" + passagePoint[4] + "[color=";
				if(LRPartitionAlgo.sign(this, e) == 1)
					dotString += "\"red\"";
				else
					dotString += "\"green\"";
	
				dotString += "];";
				dotString += "\n";
				
				
				dotString += passagePoint[4] + "--" + passagePoint[5] + "[color=";
				if(LRPartitionAlgo.sign(this, e) == 1)
					dotString += "\"red\"";
				else
					dotString += "\"green\"";
				
				dotString += ", taillabel=" + _mapEdgeNestingDepth.get(e);
				dotString += "];";
				dotString += "\n";
				
				
				
				
			}
			
			
		}
		
		
		dotString += "}\n";
		return dotString;
	}
	
	public void DFSVertexEdgePosition(int currentVertex,
			Map<Integer,Pair<Integer,Integer>> mapSizeSubTree,
			Map<Integer,Pair<Integer,Integer>> mapVertexPosition,
			Map<DefaultEdge,ArrayList<Pair<Integer,Integer>>> mapBackEdgePostion,
			Map<DefaultEdge,Pair<Integer,Integer>> mapSizeLeftSubTree,
			Map<DefaultEdge,Pair<Integer,Integer>> mapSizeRightSubTree
			)
	{
		mapSizeSubTree.put(currentVertex, new Pair<>(1,1));
		//iterating on the outgoing edges in the clockwise order
		for(DepthEdgePair depthEdgePair: getSortedOutgoingEdges(currentVertex))
		{
			DefaultEdge edge = depthEdgePair.second();
			
			//if it is a tree edge: recursion 
			if(!is_backEdge(edge))
			{
				int childVertex = getEdgeTarget(edge);
				int posXChildVertex = mapVertexPosition.get(currentVertex).first() + mapSizeSubTree.get(currentVertex).first();
				int posYChildVertex = mapVertexPosition.get(currentVertex).second() + 1;
				mapVertexPosition.put(childVertex, new Pair<>(posXChildVertex,posYChildVertex));
				DFSVertexEdgePosition(childVertex, mapSizeSubTree, mapVertexPosition,mapBackEdgePostion,mapSizeLeftSubTree, mapSizeRightSubTree);
				int newSizeSubTreeX = mapSizeSubTree.get(currentVertex).first() + mapSizeSubTree.get(childVertex).first();
				int newSizeSubTreeY = mapSizeSubTree.get(currentVertex).second();
				if(newSizeSubTreeY <  1 + mapSizeSubTree.get(childVertex).second())
				{
					newSizeSubTreeY = 1 + mapSizeSubTree.get(childVertex).second();
				}
				mapSizeSubTree.put(currentVertex, new Pair<>(newSizeSubTreeX,newSizeSubTreeY));
			}
			//if it is a backedge : need to compute the 4 passage points in another DFS, only computing size of subtrees
			//left backedge positions can be computed directly in this step, but for the consistency of the algorithm, it is also done in the second DFS
			else
			{
				
				mapSizeLeftSubTree.put(edge, mapSizeSubTree.get(currentVertex));
				ArrayList<Pair<Integer,Integer>> passagePoint = new ArrayList<>();
				int newSizeSubTreeX = mapSizeSubTree.get(currentVertex).first() +1 ;
				mapSizeSubTree.put(currentVertex,new Pair<>(newSizeSubTreeX,mapSizeSubTree.get(currentVertex).second()));
				
				
				Pair<Integer,Integer> posSource = mapVertexPosition.get(currentVertex);
				passagePoint.add(posSource);
				
				Pair<Integer,Integer> posTarget = mapVertexPosition.get(getEdgeTarget(edge));
				passagePoint.add(posTarget);
				
				
			}
			
		}
		
		//computing the right subtree size
		for(DefaultEdge e : edgeSet())
		{
			if(is_backEdge(e))
			{
				
			}
		}
		
	}
	
	
	
	public String generateDOTString4()
	{
		String dotString = "strict graph G {\n";
		dotString += "graph [layout=\"fdp\",splines=line,overlap=true];\n";
		
		Map<Integer,Pair<Integer,Integer>> mapVertexPosition = new HashMap<>();
		Map<Integer,Pair<Integer,Integer>> mapSizeSubTree = new HashMap<>();
		Map<DefaultEdge, ArrayList<Pair<Integer,Integer>>> mapBackEdgePostion = new HashMap<>();
		Map<DefaultEdge,Pair<Integer,Integer>> mapSizeLeftSubTree = new HashMap<>();
		Map<DefaultEdge,Pair<Integer,Integer>> mapSizeRightSubTree = new HashMap<>();
		//Map<DefaultEdge,Integer> mapEdgeSign = new HashMap<>();
		
		
		mapVertexPosition.put(0, new Pair<>(0,0));
		
		
		DFSVertexEdgePosition(0, mapSizeSubTree, mapVertexPosition, mapBackEdgePostion, mapSizeLeftSubTree, mapSizeRightSubTree);
		
		
		//description of the position of the vertices
		for( int u : vertexSet())
		{
			Pair<Integer, Integer> position = mapVertexPosition.get(u);
			dotString += u + "[pos=\"" + position.first()+ "," + position.second() + "!\"];\n";
		}
		
		
		
		/* For now only drawing the edges like that
		 * 
		 * 
		 * 
		 * 
		 * 
		 */
		
		
		
		for(DefaultEdge e : edgeSet())
		{
			if(!is_backEdge(e))
			{
				dotString += getEdgeSource(e) + "--" + getEdgeTarget(e) + "[color=\"blue\"];\n";
			}
			else
			{
				if(getNestingDepth(e) >= 0)
				{
					dotString += getEdgeSource(e) + "--" + getEdgeTarget(e) + "[color=\"red\"];\n";
				}
				else
				{
					dotString += getEdgeSource(e) + "--" + getEdgeTarget(e) + "[color=\"green\"];\n";
				}
			}
			
		}
		
		
		
		/*
		
		

		
		
		
		//adding new passage points for backedges
		Map<DefaultEdge, Integer[]> mapBackEdgePassagePoint = new HashMap<>(); 
		
		for(DefaultEdge e : edgeSet())
		{
			if(is_backEdge(e))
			{
				Integer[] passagePoint = new Integer[6];
				int sourceVertex = getEdgeSource(e);
				int targetVertex = getEdgeTarget(e);
				passagePoint[0] = sourceVertex;
				passagePoint[5] = targetVertex;
				int lastButOneAncestor = sourceVertex;
				int previousVertex = lastButOneAncestor;
				while(lastButOneAncestor != targetVertex)
				{
					previousVertex = lastButOneAncestor;
					lastButOneAncestor = getEdgeSource(parentEdge(lastButOneAncestor));
				}
				lastButOneAncestor = previousVertex;
				System.out.println("last but on ancestor of " + e + " is " + lastButOneAncestor);
				System.out.println("pos back edge:" + mapPosBackEdge.get(e));
				passagePoint[1] = nbTotal;
				dotString += nbTotal + "[pos=\"" + mapPosBackEdge.get(e).first()+ "," + mapPosBackEdge.get(e).second() + "!\", shape=\"point\", width=0];\n";
				nbTotal++;
				
				passagePoint[2] = nbTotal;
				int posY = mapPosition.get(lastButOneAncestor).second() + mapSubtreeDimension.get(lastButOneAncestor).second() + (maxNestingDepth - getNestingDepth(e))/2;
				dotString += nbTotal + "[pos=\"" + mapPosBackEdge.get(e).first() + "," + posY + "!\", shape=\"point\", width=0];\n";
				nbTotal++;
				
				
				passagePoint[3] = nbTotal;
				int posX = mapPosition.get(lastButOneAncestor).first() + mapSubtreeDimension.get(lastButOneAncestor).first() + (maxNestingDepth - getNestingDepth(e))/2;
				dotString += nbTotal + "[pos=\"" + posX + "," + posY + "!\", shape=\"point\", width=0];\n";
				nbTotal++;
				
				passagePoint[4] = nbTotal;
				dotString += nbTotal + "[pos=\"" + posX + "," + mapPosition.get(targetVertex).second() + "!\", shape=\"point\", width=0];\n";
				nbTotal++;
				
				
				
				
				mapBackEdgePassagePoint.put(e, passagePoint);
			}
		}
		
		System.out.println(nbTotal);
		
		for(DefaultEdge e : edgeSet())
		{
			
			if(!is_backEdge(e))//tree edge are in blue
			{
					}
			else //backEdge are in red if left and green if right
			{
				Integer[] passagePoint = mapBackEdgePassagePoint.get(e);
				
					
				dotString += passagePoint[0] + "--" + passagePoint[1] + "[color=";
				if(LRPartitionAlgo.sign(this, e) == 1)
					dotString += "\"red\"";
				else
					dotString += "\"green\"";
				dotString += "];";
				dotString += "\n";
				
				
				
				dotString += passagePoint[1] + "--" + passagePoint[2] + "[color=";
				if(LRPartitionAlgo.sign(this, e) == 1)
					dotString += "\"red\"";
				else
					dotString += "\"green\"";

				dotString += "];";
				dotString += "\n";
				
				
				dotString += passagePoint[2] + "--" + passagePoint[3] + "[color=";
				if(LRPartitionAlgo.sign(this, e) == 1)
					dotString += "\"red\"";
				else
					dotString += "\"green\"";

				dotString += "];";
				dotString += "\n";
				
				
				
				dotString += passagePoint[3] + "--" + passagePoint[4] + "[color=";
				if(LRPartitionAlgo.sign(this, e) == 1)
					dotString += "\"red\"";
				else
					dotString += "\"green\"";
	
				dotString += "];";
				dotString += "\n";
				
				
				dotString += passagePoint[4] + "--" + passagePoint[5] + "[color=";
				if(LRPartitionAlgo.sign(this, e) == 1)
					dotString += "\"red\"";
				else
					dotString += "\"green\"";
				
				dotString += ", taillabel=" + _mapEdgeNestingDepth.get(e);
				dotString += "];";
				dotString += "\n";
				
				
				
				
			}
			
			
		}
		*/
		
		dotString += "}\n";
		return dotString;
	}
	
	
	public String generateDOTStringTEST()
	{
		String DOTString = "strict graph G {\n";
		DOTString += "graph [layout=\"fdp\",splines=line,overlap=true];\n";
		
		Map<Integer,Pair<Integer, Integer>> map_vertexSizeSubtree = new HashMap<>();
		Map<Integer,SortedMap<Integer, DefaultEdge>> map_allSortedEdge = new HashMap<>();
		Map<DefaultEdge,Integer> map_yOffsetBackedge = new HashMap<>();
		Set<DefaultEdge> set_currentReturningBackEdge = new HashSet<>();
		
		
		DFSVertexEdgePositionTEST(0, map_vertexSizeSubtree, map_allSortedEdge,map_yOffsetBackedge, set_currentReturningBackEdge);
		
		Map<Integer,Pair<Integer,Integer>> map_positionVertex = new HashMap<>();
		Map<DefaultEdge,ArrayList<Pair<Integer,Integer>>> map_positionPassagePoint = new HashMap<>();
		
		
		DFSVertexEdgePositionTEST2(0, map_positionVertex, map_positionPassagePoint, map_allSortedEdge,map_yOffsetBackedge, map_vertexSizeSubtree);
		
		/*System.out.println("We have:" + map_positionVertex.size() + " different positions.");
		for(Entry<Integer,Pair<Integer,Integer>> entry : map_positionVertex.entrySet())
		{
			System.out.println("The vertex:" + entry.getKey() + " at position:" + entry.getValue());
		}*/
		
		
		//description of the position of the vertices
		for( int u : vertexSet())
		{
			Pair<Integer, Integer> position = map_positionVertex.get(u);
			DOTString += u + "[pos=\"" + position.first()+ "," + position.second() + "!\"];\n";
		}
		
		Map<DefaultEdge,ArrayList<Integer>> map_passagePoint = new HashMap<>();
		int nextVertexId = vertexSet().size();
		//description of the passage point for the backedges
		for(Entry<DefaultEdge,ArrayList<Pair<Integer,Integer>>> entry : map_positionPassagePoint.entrySet())
		{
			ArrayList<Pair<Integer,Integer>> list_passagePointPosition = entry.getValue();
			/*System.out.println("For backEdge:" + entry.getKey() + " we have " + list_passagePointPosition.size() + " passage points:");
			for(Pair<Integer,Integer> pair : list_passagePointPosition)
			{
				System.out.println("Passage Point:"+ pair);
			}*/
			Pair<Integer,Integer> lastPassagePoint = new Pair<>( list_passagePointPosition.get(3).first(),list_passagePointPosition.get(1).second());
			list_passagePointPosition.set(2, lastPassagePoint);
			ArrayList<Integer> list_passagePoint = new ArrayList<>();
			for(int i = 0;i<4;i++)
			{	
				list_passagePoint.add(i,nextVertexId);
				DOTString += nextVertexId + "[pos=\"" + list_passagePointPosition.get(i).first()+ "," + list_passagePointPosition.get(i).second() + "!\",label=\"\",penwidth=0,sep=0, shape=point,style=\"invis\",fixedsize=true,width=0];\n";
				nextVertexId++;
			}
			map_passagePoint.put(entry.getKey(), list_passagePoint);				
		}
		
		for(DefaultEdge edge : edgeSet())
		{
			if(!is_backEdge(edge))
			{
				DOTString += getEdgeSource(edge) + "--" + getEdgeTarget(edge) + "[color=\"blue\"];\n";
			}
			else
			{
				int sourceVertex = getEdgeSource(edge);
				int targetVertex = getEdgeTarget(edge);

				if(getVertexHeight(sourceVertex) < getVertexHeight(targetVertex))
				{
					sourceVertex = getEdgeTarget(edge);
					targetVertex = getEdgeSource(edge);
				}
				ArrayList<Integer> list_passagePoint = map_passagePoint.get(edge);
				
				String color;
				if(getNestingDepth(edge) >=0)
				{
					color = "\"green\"";
				}
				else
				{
					color = "\"red\"";
				}
				
				DOTString += sourceVertex + "--" + list_passagePoint.get(0) + "[color=" + color + "];\n";
				DOTString += list_passagePoint.get(0) + "--" + list_passagePoint.get(1) + "[color=" + color + "];\n";
				DOTString += list_passagePoint.get(1) + "--" + list_passagePoint.get(2) + "[color=" + color + "];\n";
				DOTString += list_passagePoint.get(2) + "--" + list_passagePoint.get(3) + "[color=" + color + "];\n";
				DOTString += list_passagePoint.get(3) + "--" + targetVertex + "[color=" + color + "];\n";
				
				
			}
		}
		
		
		
		
		DOTString += "}\n";
		return DOTString;
	}
	
	
	
	public void DFSVertexEdgePositionTEST(int currentVertex,
			Map<Integer,Pair<Integer,Integer>> map_vertexSizeSubtree,
			Map<Integer,SortedMap<Integer,DefaultEdge>> map_allSortedEdge,
			Map<DefaultEdge,Integer> map_yOffsetBackedge,
			Set<DefaultEdge> set_currentReturningBackEdge
			)
	{
		int widthSubtree = 0;
		int heightSubtree = getVertexHeight(currentVertex);
		SortedMap<Integer, DefaultEdge> map_sortedEdge = new TreeMap<>();
		int currentChildEdgePosition = 0;
		//System.out.println("DFS1 for:" + currentVertex);
		
		//tranversing the spanning tree in the nesting depth order of outgoing edges
		for(DepthEdgePair depthEdgePair : getSortedOutgoingEdges(currentVertex))
		{
			DefaultEdge edge = depthEdgePair.second();
			
			//if it is a tree edge: we recurse on the child vertex
			if(!is_backEdge(edge))
			{
				int childVertex = getEdgeTarget(edge);
				if(childVertex == currentVertex)
					childVertex = getEdgeSource(edge);
				//recursion on the child vertex
				//System.out.println("Recursing on childVertex:" + childVertex);
				DFSVertexEdgePositionTEST(childVertex, map_vertexSizeSubtree, map_allSortedEdge,map_yOffsetBackedge, set_currentReturningBackEdge);
				
				//updating the size of the subtree
				if(map_vertexSizeSubtree.get(childVertex).second() > heightSubtree)
					heightSubtree = map_vertexSizeSubtree.get(childVertex).second();
				widthSubtree += map_vertexSizeSubtree.get(childVertex).first();
				
				//handling the backedges s.t. currentVertex is the return point
				SortedMap<Integer,DefaultEdge> map_leftReturnEdge = new TreeMap<>(Collections.reverseOrder());
				SortedMap<Integer,DefaultEdge> map_rightReturnEdge = new TreeMap<>(Collections.reverseOrder());
				
				//offsets are needed to get a total ordering even if there is multiple backedges with the same nesting depth
				int leftOffset = 0;
				int rightOffset = 0;
				for (DefaultEdge edge2 : set_currentReturningBackEdge)
				{
					//check if it is such a backedge
					if(getEdgeSource(edge2) == currentVertex || getEdgeTarget(edge2) == currentVertex)
					{
						int sourceVertex;
						if(getEdgeSource(edge2) == currentVertex)
						{
							sourceVertex = getEdgeTarget(edge2);
						}
						else
						{
							sourceVertex= getEdgeSource(edge2);
						}
						if(getVertexHeight(currentVertex) < getVertexHeight(sourceVertex))
						{
							//System.out.println("CHECKING backedge returning at:" + currentVertex);
							//then it is the case: size of the subtree is increased, and the edge is removed, and the backedges is sorted in one of the two maps
							//for RIGHT backedges
							if(getNestingDepth(edge2) >=0)
							{
								int offsetedNestingDepth = getNestingDepth(edge2) + rightOffset;
								if(map_rightReturnEdge.containsKey(offsetedNestingDepth))
								{
									rightOffset++;
									offsetedNestingDepth++;
								}
								map_rightReturnEdge.put(offsetedNestingDepth, edge2);
							}
							//left backEdges
							else
							{
								int offsetedNestingDepth = getNestingDepth(edge2) + leftOffset;
								if(map_leftReturnEdge.containsKey(offsetedNestingDepth))
								{
									leftOffset--;
									offsetedNestingDepth--;
								}
								map_leftReturnEdge.put(offsetedNestingDepth, edge2);
							}
						}
						
					}
					else
					{
						//System.out.println("Backedge:" + edge2 + " does not return at " + currentVertex);
					}
				}
				
				//iterating on the two maps in reverse order to get the position of this edge for the return vertex (currentVertex)
				//first the left backedges
				Set<Entry<Integer,DefaultEdge>> set_leftEdge = map_leftReturnEdge.entrySet();
				int backedgeYOffset = map_leftReturnEdge.size();
				Iterator<Entry<Integer, DefaultEdge>> it = set_leftEdge.iterator();
				Set<DefaultEdge> set_edgeToRemove = new HashSet<>();
				
				while(it.hasNext())
				{
					Entry<Integer,DefaultEdge> entry = (Entry<Integer, DefaultEdge>) it.next();
					map_sortedEdge.put(currentChildEdgePosition, entry.getValue());
					map_yOffsetBackedge.put(entry.getValue(), backedgeYOffset);
					backedgeYOffset--;
					set_edgeToRemove.add(entry.getValue());
					currentChildEdgePosition ++;
					widthSubtree++;
				}
				
				
				//then the tree edge
				map_sortedEdge.put(currentChildEdgePosition, edge);
				currentChildEdgePosition++;
				//the update of the size of the subtree has already been made, so no need to do it for the tree edge
				
				//finally the right backedges
				Set<Entry<Integer,DefaultEdge>> set_rightEdge = map_rightReturnEdge.entrySet();
				backedgeYOffset = map_rightReturnEdge.size();
				it = set_rightEdge.iterator();
				while(it.hasNext())
				{
					Entry<Integer,DefaultEdge> entry = (Entry<Integer, DefaultEdge>) it.next();
					map_sortedEdge.put(currentChildEdgePosition, entry.getValue());
					map_yOffsetBackedge.put(entry.getValue(), backedgeYOffset);
					backedgeYOffset--;
					set_edgeToRemove.add(entry.getValue());
					currentChildEdgePosition ++;
					widthSubtree++;
				}
				
				//removing the backedges returning at currentVertex from currentReturningBackEdge
				set_currentReturningBackEdge.removeAll(set_edgeToRemove);
				
				//updating the height of the subtree
				int offsetheightSubtree = map_leftReturnEdge.size();
				if(offsetheightSubtree< map_rightReturnEdge.size())
					offsetheightSubtree = map_rightReturnEdge.size();
				heightSubtree+=offsetheightSubtree;
				
			}
			//the edge is a backedge
			else
			{
				//System.out.println("Vertex:" + currentVertex + " has backedge: " + currentChildEdgePosition + ":::" + edge);
				map_sortedEdge.put(currentChildEdgePosition, edge);
				set_currentReturningBackEdge.add(edge);
				//System.out.println("BACKEDGE:" + edge + " is returning");
				currentChildEdgePosition++;
				widthSubtree++;
			}
		}
		
		//updating size of the subtree
		map_vertexSizeSubtree.put(currentVertex, new Pair<>(widthSubtree,heightSubtree));
		//and updating the list of edges
		map_allSortedEdge.put(currentVertex, map_sortedEdge);
		/*System.out.println("FINALLY, the vertex:" + currentVertex + " has: " + map_sortedEdge.size() + " edges.");
		for(Entry<Integer,DefaultEdge > entry : map_sortedEdge.entrySet())
		{
			System.out.println("The edge:" + entry.getKey() + "::" + entry.getValue());
			
		}*/
		
	}
	
	
	void DFSVertexEdgePositionTEST2(int currentVertex,
			Map<Integer, Pair<Integer,Integer>> map_positionVertex,
			Map<DefaultEdge,ArrayList<Pair<Integer,Integer>>> map_positionPassagePoint,
			Map<Integer,SortedMap<Integer,DefaultEdge>> map_allSortedEdge,
			Map<DefaultEdge,Integer> map_yOffsetBackedge,
			Map<Integer,Pair<Integer,Integer>> map_vertexSizeSubtree)
	{
		//initialization of the root vertex of the spanning tree
		//System.out.println("DFS2 for:" + currentVertex);
		if(currentVertex == 0)
			map_positionVertex.put(currentVertex, new Pair<Integer,Integer>(0,0));
		Pair<Integer,Integer> currentVertexPosition = map_positionVertex.get(currentVertex);
		Map<Integer,DefaultEdge> map_sortedEdge = map_allSortedEdge.get(currentVertex);
		//initial offset: to have a centered tree
		int centeringXOffset = 0;//(int)(-1*map_sortedEdge.size()/2) +1;
		//offset corresponding to the edge ordering
		int currentXOffset = 0;
		int currentReverseXOffset = map_vertexSizeSubtree.get(currentVertex).first()-1;
		//iterating on the list of edges to give them a position 
		for (Entry<Integer,DefaultEdge> entry : map_sortedEdge.entrySet())
		{
			
			DefaultEdge edge = entry.getValue();
			int childVertex = getEdgeSource(edge);
			if(childVertex == currentVertex)
				childVertex = getEdgeTarget(edge);
			//if it is a tree edge
			if(!is_backEdge(edge))
			{
				int childPosX = currentVertexPosition.first() + currentXOffset + centeringXOffset ;
				currentXOffset += map_vertexSizeSubtree.get(childVertex).first();
				int childPosY = currentVertexPosition.second() + 1;
				Pair<Integer,Integer> childPosition = new Pair<>(childPosX,childPosY);
				map_positionVertex.put(childVertex, childPosition);
				DFSVertexEdgePositionTEST2(childVertex, map_positionVertex, map_positionPassagePoint, map_allSortedEdge,map_yOffsetBackedge, map_vertexSizeSubtree);
			}
			//it is a backedge
			else
			{
				//System.out.println("Checking backedge:" + edge);
				if(!map_positionPassagePoint.containsKey(edge))
				{
					ArrayList<Pair<Integer,Integer>> list_passagePoint = new ArrayList<>();
					for (int i =0;i<4;i++)
					{
						list_passagePoint.add(i,new Pair<>(-1,-1));
					}
					map_positionPassagePoint.put(edge, list_passagePoint);
				}
				int otherVertex = getEdgeSource(edge);
				if(otherVertex == currentVertex)
				{
					otherVertex = getEdgeTarget(edge);
				}
				
				//if current vertex is the source of the backedge
				if(getVertexHeight(currentVertex) > getVertexHeight(otherVertex))
				{
					ArrayList<Pair<Integer,Integer>> list_passagePoint = map_positionPassagePoint.get(edge);
					//the first passage point
					int passagePointX = currentVertexPosition.first() + currentXOffset + centeringXOffset;
					currentXOffset ++;
					int passagePointY = currentVertexPosition.second() + 1;
					Pair<Integer,Integer> passagePointPosition = new Pair<>(passagePointX,passagePointY);
					list_passagePoint.set(0,passagePointPosition);
					
					//the second passage point
					int parentVertex = currentVertex;
					int previousVertex = currentVertex;
					while(parentVertex != otherVertex)
					{
						previousVertex = parentVertex;
						DefaultEdge parentEdge = parentEdge(parentVertex);
						if(getEdgeSource(parentEdge) == parentVertex)
						{
							parentVertex = getEdgeTarget(parentEdge);
						}
						else
						{
							parentVertex = getEdgeSource(parentEdge);
						}
					}
					 passagePointY = map_vertexSizeSubtree.get(previousVertex).second() + map_yOffsetBackedge.get(edge);
					 
					 passagePointPosition = new Pair<>(passagePointX, passagePointY);
					 list_passagePoint.set(1,passagePointPosition);
					
				}
				//if current vertex is the target of the backedge
				else
				{
					ArrayList<Pair<Integer,Integer>> list_passagePoint = map_positionPassagePoint.get(edge);
					/*int passagePointX = currentVertexPosition.first() + currentXOffset + centeringXOffset;
					currentXOffset ++;*/
					int passagePointX = currentVertexPosition.first() + currentReverseXOffset + centeringXOffset;
					currentReverseXOffset--;
					int passagePointY = currentVertexPosition.second() + 1;
					Pair<Integer,Integer> passagePointPosition = new Pair<>(passagePointX,passagePointY);
					list_passagePoint.set(3,passagePointPosition);
				}
			}
		}
		
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
