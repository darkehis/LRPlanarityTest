import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.Vector;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;

public class LRPartitionAlgo
{
	// todo: change it to handle multiple connected part
	public final static int ROOT_ID = 0;

	// function generating oriented DFS graph
	public static LROrientedDFSGraph generateLROrientedDFSGraph(LRUndirectedGraph originalGraph)
	{
		// unmarked <=> height = infinity
		originalGraph.resetHeight();
		LROrientedDFSGraph graph = new LROrientedDFSGraph();
		// for now only one connected part
		originalGraph.setVertexHeight(ROOT_ID, 0);
		// root.set_height(0);
		graph.addVertex(ROOT_ID);
		graph.setVertexHeight(ROOT_ID, 0);
		// -----------recursion------------
		DFS(originalGraph, graph, ROOT_ID);


		return graph;
	}

	// core recursive function to DFS the original graph:
	// DFS + list of return edges for each edge
	public static void DFS(LRUndirectedGraph originalGraph, LROrientedDFSGraph graph, int currentVertex)
	{
		//System.out.println("Recursion on vertex:" + currentVertex + " of height "	+ originalGraph.getVertexHeight(currentVertex) + " or " + graph.getVertexHeight(currentVertex));
		// if not root: update the return points of the parent edge of the current
		// vertex
		DefaultEdge parentEdge = graph.parentEdge(currentVertex);
		// tree edges
		while (!originalGraph.getUnmarkedNeighbors(currentVertex).isEmpty())
		{
			int size = originalGraph.getUnmarkedNeighbors(currentVertex).size();
			//System.out.println("vertex " + currentVertex + ":" + size + " unmarked neighbors.");

			int newVertex = originalGraph.getUnmarkedNeighbors(currentVertex).iterator().next();

			//System.out.println("Previous height of neighbor:" + newVertex + "=" + originalGraph.getVertexHeight(newVertex));
			// updating the height in the oriented graph and in the original undirected
			// graph
			originalGraph.setVertexHeight(newVertex, originalGraph.getVertexHeight(currentVertex) + 1);
			graph.addVertex(newVertex);
			graph.setVertexHeight(newVertex, originalGraph.getVertexHeight(newVertex));

			//System.out.println("New height of vertex:" + newVertex + "=" + originalGraph.getVertexHeight(newVertex));
			size = originalGraph.getUnmarkedNeighbors(currentVertex).size();
			//System.out.println("Vertex " + currentVertex + " has now " + size + " unmarked neighbors");

			DefaultEdge treeEdge = new DefaultEdge(); // treeEdge : newVertex.get_height() = currentVertex.get_height()
														// + 1

			graph.addEdge(currentVertex, newVertex, treeEdge);
			DFS(originalGraph, graph, newVertex);
			if (parentEdge != null)
			{
				graph.addReturnEdgeHeight(parentEdge, treeEdge);
			}
		}
		// back edges
		Set<Integer> neighbors = originalGraph.getNeighbors(currentVertex);
		//System.out.println("handling backedges fo vertex:" + currentVertex);
		for (int v : neighbors)
		{
			//System.out.println("checking neighbor:" + v);

			if (!graph.containsEdge(v, currentVertex) && !graph.containsEdge(currentVertex, v))
			{
				//System.out.println("no edge between " + v + " and " + currentVertex);
				DefaultEdge backEdge = new DefaultEdge();// backEdge : currentVerttex.get_height > v.get_height()
				graph.addEdge(currentVertex, v, backEdge);
				graph.updateBackEdgeReturnEdge(backEdge);
				if (parentEdge != null)
				{
					graph.addReturnEdgeHeight(parentEdge, backEdge);
				}
			}
		}
	}

	// function to generate the constraint graph on which the balance check is
	// computed.
	// Vertices of constraint graph are backedges of oriented graph (in fact we only
	// consider their lowpt because LR-Partition <=> aligned LR-Partition.
	// Edges of the constraint graph are constraint: weighted edge with weight = -1
	// for different-constraint; and 1 for same-constraint.
	public static LRConstraintGraph generateConstraintGraph(LROrientedDFSGraph orientedGraph)
	{
		/*
		 * for each edge e in E(orientedGraph) 
		 * { 
		 * 	if(e is a fork (e = uv)) 
		 * 	{ 
		 * 		for each pair(e1,e2) in outcomingEdges(v) 
		 * 		{ 
		 * 			for each pair(b1,b2) in retEdge(e1) X retEdge(e2) 
		 * 			{ 
		 * 				//different constraint check 
		 * 				if(b1>lowpt(e2) && b2>lowpt(e1)) 
		 * 				{
		 * 					add difConstraint(b1,b2); 
		 * 				}
		 * 
		 * 				//same constraint check 
		 * 				bMin = min(b1,b2); 
		 * 				e' = e; 
		 * 				while(height(e')>bMin) 
		 * 				{
		 * 					if(lowpt(e')<bMin) 
		 * 					{ 
		 * 						add sameConstraint(b1,b2); 
		 * 						break while; 
		 * 					}
		 * 					e' = parentEdge(e'); 
		 * 				}
		 * 			 }
		 * 		 }
		 * 	 }
		 * }
		 */

		LRConstraintGraph constraintGraph = new LRConstraintGraph();

		Set<DefaultEdge> setEdge = orientedGraph.edgeSet();
		Iterator<DefaultEdge> itEdge = setEdge.iterator();
		int nextIdVertex = 0;
		Map<Integer,Integer> mapVertexId = new HashMap<>();
		while (itEdge.hasNext())
		{
			DefaultEdge e = itEdge.next();
			if (orientedGraph.isFork(e))
			{
				List<DefaultEdge> listOutgoingEdge = new ArrayList<>(
						orientedGraph.outgoingEdgesOf(orientedGraph.getEdgeTarget(e)));
				List<List<DefaultEdge>> listPairOutgoingEdge = subsets(listOutgoingEdge, 2, listOutgoingEdge.get(0));
				Iterator<List<DefaultEdge>> itPair = listPairOutgoingEdge.iterator();
				while (itPair.hasNext())
				{
					List<DefaultEdge> pair = itPair.next();
					DefaultEdge e1 = pair.get(0);
					DefaultEdge e2 = pair.get(1);
					SortedSet<Integer> returnEdge1 = orientedGraph.getReturnEdge(e1);
					SortedSet<Integer> returnEdge2 = orientedGraph.getReturnEdge(e2);
					Iterator<Integer> itB1 = returnEdge1.iterator();
					while (itB1.hasNext())
					{
						int heightB1 = itB1.next();
						int hashB1 = orientedGraph.getReturnRepresentant(e1, heightB1).hashCode();
						int b1;
						if(mapVertexId.containsKey(hashB1))
						{
							b1 = mapVertexId.get(hashB1);
						}
						else
						{
							b1 = nextIdVertex;
							nextIdVertex++;
							mapVertexId.put(hashB1, b1);
						}
						constraintGraph.addVertex(b1);
						Iterator<Integer> itB2 = returnEdge2.iterator();
						while (itB2.hasNext())
						{
							int heightB2 = itB2.next();
							int hashB2 = orientedGraph.getReturnRepresentant(e2, heightB2).hashCode();
							int b2;
							if(mapVertexId.containsKey(hashB2))
							{
								b2 = mapVertexId.get(hashB2);
							}
							else
							{
								b2 = nextIdVertex;
								nextIdVertex++;
								mapVertexId.put(hashB2, b2);
							}
							constraintGraph.addVertex(b2);
							// checking different constraint;
//System.out.println("checking constraint: (" + orientedGraph.getEdgeSource(e1) + "," + orientedGraph.getEdgeTarget(e1) + ") b" + b1 + "::lowpt=" + heightB1 + " AND (" +
							//orientedGraph.getEdgeSource(e2) + "," + orientedGraph.getEdgeTarget(e2) + ") b" + b2 + "::lowpt=" + heightB2);
							if (heightB1 > orientedGraph.lowpt(e2) && heightB2 > orientedGraph.lowpt(e1))
							{
								///System.out.println("dif constraint");
								// check if there is no different sign edge between the two vertices yet
								if (constraintGraph.containsEdge(b1, b2))
								{
									DefaultWeightedEdge previousConstraintEdge = constraintGraph.getEdge(b1, b2);
									if (constraintGraph.getEdgeWeight(previousConstraintEdge) != -1)
									{
										// There is already a same-constraint edge between the 2 vertices : impossible
										// to be planar 
										//edge marked with weight 0
										System.out.println("not planar : not previously dif-constraint");
										constraintGraph.setEdgeWeight(previousConstraintEdge, 0);
										//return null;
									}
								} 
								else
								{
									DefaultWeightedEdge difConstraintEdge = new DefaultWeightedEdge();
									constraintGraph.addEdge(b1, b2, difConstraintEdge);
									constraintGraph.setEdgeWeight(difConstraintEdge, -1);
								}
							}

							// checking same constraint
							int heightBMin = Math.min(heightB1, heightB2);
							int heightBMax = Math.max(heightB1, heightB2);
							DefaultEdge e3 = e;
							boolean foundSameConstraint = false;
							while (orientedGraph.sourceHeight(e3) > heightBMax)
							{
								Set<DefaultEdge> setChildEdge = orientedGraph.outgoingEdgesOf(orientedGraph.getEdgeSource(e3));
								for(DefaultEdge childEdge : setChildEdge)
								{
									if(!childEdge.equals(e3) && orientedGraph.lowpt(childEdge) < heightBMin)
									{
										//System.out.println("same constraint");
										foundSameConstraint = true;
										// check if there is no different sign edge between the two vertices yet
										if (constraintGraph.containsEdge(b1, b2))
										{
											DefaultWeightedEdge previousConstraintEdge = constraintGraph.getEdge(b1, b2);
											if (constraintGraph.getEdgeWeight(previousConstraintEdge) != 1)
											{
												// There is already a different-constraint edge between the 2 vertices :
												// impossible to be planar
												//edge marked with weight 0
												constraintGraph.setEdgeWeight(previousConstraintEdge, 0);
												System.out.println("not planar not previously same-constraint");
												//return null;
											}
										} 
										else
										{
											DefaultWeightedEdge sameConstraintEdge = new DefaultWeightedEdge();
											constraintGraph.addEdge(b1, b2, sameConstraintEdge);
											constraintGraph.setEdgeWeight(sameConstraintEdge, 1);
										}
										break;
									}
								}
								if(foundSameConstraint)
									break;
								e3 = orientedGraph.parentEdge(e3);
							}
						}
					}
				}
			}
		}
		return constraintGraph;
	}

	// graph is balanced <=> no cycle with a odd number of (-1)-edge
	// DFS to catch cycle and simple multiplication to know if there is an odd
	// number or (-1)-edge in a cycle:
	
	  public static boolean isBalanced(LRConstraintGraph graph) 
	  { 
		  int rootId = graph.vertexSet().iterator().next(); 
		  return DFSBalanced(graph,rootId,new HashSet<>(),new HashSet<>(),new HashMap<>()); 
		  
	  }
	 
	  
  // core DFS algorithm 
	  //bad code: not optimised nor complying with previous standard:REFACTOR!!!!!!!!!!!!!!
   public static boolean DFSBalanced(LRConstraintGraph constraintGraph,int vertex,Set<DefaultWeightedEdge> treeEdge,Set<Integer> markedVertex, Map<Integer,Integer> parentVertex) 
   {
	   Set<Integer> neighbors = constraintGraph.getNeighbors(vertex);
	   Iterator<Integer> itV = neighbors.iterator(); 
	   boolean balanced = true;
	   while(itV.hasNext()) 
	   {
		   int neighbor = itV.next();
		   if(!markedVertex.contains(neighbor)) 
		   {
			   markedVertex.add(neighbor);
			   treeEdge.add(constraintGraph.getEdge(vertex, neighbor));
			   parentVertex.put(neighbor,vertex); 
			   //----------recursion------- 
			   balanced = balanced && DFSBalanced(constraintGraph,neighbor,treeEdge,markedVertex,parentVertex); 
		   } 
		   
	   }
	   Set<DefaultWeightedEdge> setEdge = constraintGraph.edgesOf(vertex);
	   Iterator<DefaultWeightedEdge> itE = setEdge.iterator(); 
	   while(itE.hasNext())
	   { 
		   DefaultWeightedEdge backedge = itE.next(); 
		   if(!treeEdge.contains(backedge))
		   {
			   int returnPoint = constraintGraph.getEdgeTarget(backedge);
			   if(constraintGraph.getEdgeTarget(backedge).equals(vertex)) 
			   { 
				   returnPoint = constraintGraph.getEdgeSource(backedge); 
			   } 
			   
			   double product = constraintGraph.getEdgeWeight(backedge); 
			   int u,v;
			   u = vertex; 
			   while(u !=returnPoint) 
			   { 
				   v = parentVertex.get(u); product *= constraintGraph.getEdgeWeight(constraintGraph.getEdge(u, v));
				   u = v;  
			   }
			   if(product == -1) 
			   { 
				   return false;
			   } 
		   }  
	   } 
	   return balanced; 
   }
	 

	// getting the set of all subset of size k composed with elements of original
	// set
	public static <V> List<List<V>> subsets(List<V> set, int k, V start)
	{
		// System.out.println("set of size:" + k + ":: from : " + start);
		// set of size :k, composed of element of :set, starting at element :start.
		List<List<V>> subsets = new Vector<>();
		// k=0 => returning singleton "emptyset"
		if (k == 0)
		{
			subsets.add(new Vector<>());
			return subsets;
		}
		// iterating on the list until finding element start
		ListIterator<V> it = set.listIterator();
		V v = null;
		while (it.hasNext())
		{
			v = it.next();
			if (v == start)
				break;

		}
		// backing one step to retrieve the iterator of element start (not so nice but
		// didn't find out how to do it a better way: this is the reason for
		// using List Interface)
		it.previous();
		while (it.hasNext())
		{
			v = it.next();
			// recursion: getting the k-1 sized subsets starting at start+1
			List<List<V>> smallerSubsets = subsets(set, k - 1, v);
			// adding element start to every subsets of size k-1 => all subsets are of size
			// k
			for (List<V> s : smallerSubsets)
			{
				// check for unicity of element in the set(not so nice either, but
				// the Vector class does not implement Set Interface)
				if (!s.contains(v))
				{
					s.add(v);
					subsets.add(s);
				}
			}
		}
		return subsets;
	}
}
