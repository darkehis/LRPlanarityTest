import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Vector;

import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.graph.DefaultWeightedEdge;
import org.jgrapht.io.DOTExporter;

import com.google.common.graph.Graph;

public class LRPartitionAlgo
{
	// todo: change it to handle multiple connected part
	public final static int ROOT_ID = 1;

	
	/*-------------------------- polynomial version-------------------------------*/
	
	
	// function generating oriented DFS graph
	public static LROrientedDFSGraph generateLROrientedDFSGraph(LRUndirectedGraph originalGraph)
	{
		// unmarked <=> height = infinity
		originalGraph.reset();
		LROrientedDFSGraph graph = new LROrientedDFSGraph();
		// for now only one connected part
		originalGraph.setVertexHeight(ROOT_ID, 0);
		// root.set_height(0);
		graph.addVertex(ROOT_ID);
		graph.setVertexHeight(ROOT_ID, 0);
		// -----------recursion------------
		DFSOld(originalGraph, graph, ROOT_ID);


		return graph;
	}

	// core recursive function to DFS the original graph:
	// DFS + list of return edges for each edge
	public static void DFSOld(LRUndirectedGraph originalGraph, LROrientedDFSGraph graph, int currentVertex)
	{
		// if not root: update the return points of the parent edge of the current
		// vertex
		DefaultEdge parentEdge = graph.parentEdge(currentVertex);
		// tree edges
		while (!originalGraph.getUnmarkedNeighbors(currentVertex).isEmpty())
		{
			int newVertex = originalGraph.getUnmarkedNeighbors(currentVertex).iterator().next();

			// updating the height in the oriented graph and in the original undirected
			// graph
			originalGraph.setVertexHeight(newVertex, originalGraph.getVertexHeight(currentVertex) + 1);
			graph.addVertex(newVertex);
			graph.setVertexHeight(newVertex, originalGraph.getVertexHeight(newVertex));

	

			DefaultEdge treeEdge = new DefaultEdge(); // treeEdge : newVertex.get_height() = currentVertex.get_height() +1
											

			graph.addEdge(currentVertex, newVertex, treeEdge);
			DFSOld(originalGraph, graph, newVertex);
			if (parentEdge != null)
			{
				graph.addReturnEdgeHeight(parentEdge, treeEdge);
			}
		}
		// back edges
		Set<Integer> neighbors = originalGraph.getNeighbors(currentVertex);
		for (int v : neighbors)
		{

			if (!graph.containsEdge(v, currentVertex) && !graph.containsEdge(currentVertex, v))
			{
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
							if (heightB1 > orientedGraph.getLowpt(e2) && heightB2 > orientedGraph.getLowpt(e1))
							{
								// check if there is no different sign edge between the two vertices yet
								if (constraintGraph.containsEdge(b1, b2))
								{
									DefaultWeightedEdge previousConstraintEdge = constraintGraph.getEdge(b1, b2);
									if (constraintGraph.getEdgeWeight(previousConstraintEdge) != -1)
									{
										// There is already a same-constraint edge between the 2 vertices : impossible
										// to be planar 
										//edge marked with weight 0
										constraintGraph.setEdgeWeight(previousConstraintEdge, 0);
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
									if(!childEdge.equals(e3) && orientedGraph.getLowpt(childEdge) < heightBMin)
									{
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
	
	
	/*----------------------------linear version---------------------------*/
	
	public static void LRCriterionPlanarityTest(LRUndirectedGraph originalGraph,String graphName)
	{
		// unmarked <=> height = infinity
		originalGraph.reset();
		
		// for now only one connected part
		originalGraph.setVertexHeight(ROOT_ID, 0);
		
		//orientation
		LROrientedDFSGraph orientedGraph = new LROrientedDFSGraph(originalGraph);
		
		System.out.println("oriented graph generated");
		


		// root.set_height(0);
		orientedGraph.setVertexHeight(ROOT_ID, 0);
		// -----------recursion------------
		DFS1(originalGraph, orientedGraph, ROOT_ID);
		
		System.out.println("first dfs done: sorting edges");
		
		
		//sorting edges according to nesting depth
		orientedGraph.sortOutGoingEdge();
		
		
		
		//testing
		System.out.println("Edges sorted: beginning second DFS: testing planarity");
		String dotString;
		dotString = orientedGraph.generateDOTString();
		
		if(DFS2(orientedGraph, ROOT_ID))
		{
			System.out.println("graph is planar!");
			//embedding
			for(DefaultEdge e : orientedGraph.edgeSet())
			{
				orientedGraph.setNestingDepth(e, orientedGraph.getNestingDepth(e)*sign(orientedGraph,e));
			}
			
			
			orientedGraph.sortOutGoingEdge();
			dotString = orientedGraph.generateDOTString();
			FileHandler.saveDotFile(dotString,graphName +  "_oriented");
		}
		
		
			
		
	}
	
	
	
	// core recursive function to DFS the original graph:
	// DFS + list of return edges for each edge
	public static void DFS1(LRUndirectedGraph originalGraph, LROrientedDFSGraph graph, int currentVertex)
	{
		// if not root: update the return points of the parent edge of the current
		// vertex
		DefaultEdge parentEdge = graph.parentEdge(currentVertex);
		
		while(originalGraph.edgeSet().size() > graph.edgeSet().size())
		{
			DefaultEdge e = originalGraph.getUnmarkedEdge(currentVertex);
			if(e != null)
			{
				originalGraph.setEdgesMarked(e);

				int target;
				if(originalGraph.getEdgeSource(e).equals(currentVertex))
				{
					target = originalGraph.getEdgeTarget(e);
				}
				else
				{
					target = originalGraph.getEdgeSource(e);
				}
				DefaultEdge eO = new DefaultEdge();
				graph.addEdge(currentVertex, target,eO);
				graph.setLowpt(eO, graph.getVertexHeight(currentVertex));
				graph.setLowpt2(eO, graph.getVertexHeight(currentVertex));
				if(graph.getVertexHeight(target) == Integer.MAX_VALUE) //tree edge
				{
					graph.setParentEdge(target, eO);
					graph.setVertexHeight(target, graph.getVertexHeight(currentVertex)+1);
					//-------------recursion-------------
					DFS1(originalGraph,graph,target);
				}
				else //back edge
				{
					graph.setLowpt(eO, graph.getVertexHeight(target));
				}
				
				//determine nesting order
				graph.setNestingDepth(eO, graph.getLowpt(eO) * 2);
				
				if(graph.getLowpt2(eO)<graph.getVertexHeight(currentVertex))
				{
					graph.setNestingDepth(eO, graph.getNestingDepth(eO) +1);
				}
				
				//update lowpt of parent edge 
				if(parentEdge != null)
				{
					if(graph.getLowpt(eO)<graph.getLowpt(parentEdge))
					{
						graph.setLowpt2(parentEdge, Math.min(graph.getLowpt(parentEdge), graph.getLowpt2(eO)));
						graph.setLowpt(parentEdge, graph.getLowpt(eO));
					}
					else if(graph.getLowpt(eO)>graph.getLowpt(parentEdge))
					{
						graph.setLowpt2(parentEdge, Math.min(graph.getLowpt2(parentEdge), graph.getLowpt(eO)));
					}
					else
					{
						graph.setLowpt2(parentEdge, Math.min(graph.getLowpt2(parentEdge), graph.getLowpt2(eO)));
					}
					
				}
			}
			else
			{
				break;
			}
			
		}
	}
	
	public static boolean DFS2(LROrientedDFSGraph graph,int currentVertex)
	{
		DefaultEdge parentEdge = graph.parentEdge(currentVertex);
		SortedSet<DepthEdgePair> sortedOutgoingEdges = graph.getSortedOutgoingEdges(currentVertex);
		for(DepthEdgePair pair : sortedOutgoingEdges)
		{
			int depth = pair.first();

			DefaultEdge e = pair.second();
			graph.setStackBottom(e, graph.getStackConflictPair().size());
			if(e.equals(graph.parentEdge(graph.getEdgeTarget(e)))) //tree edge
			{
				if(!DFS2(graph,graph.getEdgeTarget(e)))
					return false;
			}
			else //backEdge
			{
				graph.setLowptEdge(e, e);
				Interval left = Interval.emptyInterval();
				Interval right = new Interval(e,e);
				ConflictPair p = new ConflictPair(left, right);
				graph.pushStack(p);
			}
			
			//integrate new edges
			if(graph.getLowpt(e)<graph.getVertexHeight(currentVertex)) //e has return edge
			{
				if(depth == sortedOutgoingEdges.first().first())
				{
					if(parentEdge != null)
						graph.setLowptEdge(parentEdge, e);
				}
				else
				{

					if(!addConstraint(graph, e, parentEdge))
					{
						System.out.println("Graph is not planar");
						return false;
					}
				}
			}
				
			
			
			
		}
		//remove backedges returning to parent
		if(parentEdge != null)
		{

			//trim back edges ending at parent u = source(parentEdge): algo 5
			int u = graph.getEdgeSource(parentEdge);
			trimBackEdges(graph, u);
			
			
			//side of e is the side of the highest return edge
			if(graph.getLowpt(parentEdge)<graph.getVertexHeight(u)) //parent edge has return edges
			{
				ConflictPair top = graph.getStackTop();
				if(top != null)
				{
					DefaultEdge hL = top.left().high();
					DefaultEdge hR = top.right().high();
					if(hL != null && (hR==null || graph.getLowpt(hL)>graph.getLowpt(hR)))
					{
						graph.setEdgeRef(parentEdge, hL);
					}
					else
					{
						graph.setEdgeRef(parentEdge, hR);
					}

				}
			}
		}
		

		
		return true;
		
	}
	
	//algorithm 4
	public static boolean addConstraint(LROrientedDFSGraph graph, DefaultEdge e,DefaultEdge parentEdge)
	{
		ConflictPair p = new ConflictPair(Interval.emptyInterval(), Interval.emptyInterval());
		//merge return edges of e into p.right
		while(graph.getStackConflictPair().size() != graph.getStackBottom(e))
		{
			ConflictPair q = graph.popStack();
			if(!q.left().equals(Interval.emptyInterval()))
			{
				q.swapLR();
			}
			if(!q.left().equals(Interval.emptyInterval()))
			{
				return false;
				//Halt not planar!!!
			}
			else
			{
				if(graph.getLowpt(q.right().low()) > graph.getLowpt(parentEdge)) //merge intervals
				{				
					Interval newPRight;
					if(p.right().equals(Interval.emptyInterval())) //topmost interval
					{
						newPRight = Interval.emptyInterval();
						newPRight.setHigh(q.right().high());
					}
					else
					{
						newPRight = p.right();
						graph.setEdgeRef(p.right().low(), q.right().high());
					}
					newPRight.setLow(q.right().low());
					p.setRight(newPRight);
			
				}
				else //allign
				{
					graph.setEdgeRef(q.right().low(), graph.getLowptEdge(parentEdge));
				}
			}
		}
		//merge conflict return edges of previous sibling edges into p.l

		while(conflicting(graph, graph.getStackTop().left(),e) || conflicting(graph, graph.getStackTop().right(), e))
		{
			ConflictPair q = graph.popStack();
			if(conflicting(graph, q.right(), e))
			{
				q.swapLR();
			}
			if(conflicting(graph, q.right(), e))
			{
				return false;
				//HALT NOT PLANAR
			}
			else //merge interval below lowpt(e) into p.right
			{
				graph.setEdgeRef(p.right().low(), q.right().high());
				if(q.right().low() != null)
				{
					p.right().setLow(q.right().low());
				}
			}
			if(p.left().equals(Interval.emptyInterval())) //topmost interval
			{
				p.left().setHigh(q.left().high());
			}
			else
			{
				graph.setEdgeRef(p.left().low(), q.left().high());
			}
			p.left().setLow(q.left().low());
		}
		
		if(!p.equals(ConflictPair.emptyPair()))
		{
			graph.pushStack(p);
		}
		
		return true;
	}
	
	public static boolean conflicting(LROrientedDFSGraph graph, Interval i, DefaultEdge e)
	{
		return(!i.equals(Interval.emptyInterval()) && graph.getLowpt(i.high())>graph.getLowpt(e));
	}
	
	
	//algo 5

	public static void trimBackEdges(LROrientedDFSGraph graph,int u)
	{
		//drop entire pair of conflict
		Deque<ConflictPair> stack = graph.getStack();
		while(!stack.isEmpty() && lowest(graph,graph.getStackTop()) == graph.getVertexHeight(u))
		{
			ConflictPair p = stack.pop();
			if(p.left().low() != null)
			{
				graph.setSide(p.left().low(), -1);
			}
		}
		if(!stack.isEmpty())
		{
			ConflictPair p = stack.pop();
			//trim left interval
			while(p.left().high() != null && graph.getEdgeTarget(p.left().high()) == u)
			{
				p.left().setHigh(graph.getEdgeRef(p.left().high()));
			}
			
			if(p.left().high() == null && p.left().low() != null) //just emptied
			{
				graph.setEdgeRef(p.left().low(), p.right().low());
				graph.setSide(p.left().low(), -1);
				p.left().setLow(null);
				
			}
			
			//trim right interval
			
			while(p.right().high() != null && graph.getEdgeTarget(p.right().high()) == u)
			{
				p.right().setHigh(graph.getEdgeRef(p.right().high()));
			}
			
			if(p.right().high() == null && p.right().low() != null) //just emptied
			{
				graph.setEdgeRef(p.right().low(), p.right().low());
				graph.setSide(p.right().low(), -1);
				p.right().setLow(null);
				
			}
			stack.push(p);
			
		}
		
		
		
	}
	
	public static int lowest(LROrientedDFSGraph graph,ConflictPair p)
	{
		if(p.left().equals(Interval.emptyInterval()))
		{
			return graph.getLowpt(p.right().low());
			
		}
		if(p.right().equals(Interval.emptyInterval()))
		{
			return graph.getLowpt(p.left().low());
		}
		return Math.min(graph.getLowpt(p.left().low()), graph.getLowpt(p.right().low()));
	}
	
	
	//for embedding
	//algo 6
	public static void DFS3(LROrientedDFSGraph graph,int currentVertex)
	{
		SortedSet<DepthEdgePair> sortedOutgoingEdges = graph.getSortedOutgoingEdges(currentVertex);
		for(DepthEdgePair pair : sortedOutgoingEdges)
		{
			int w = graph.getEdgeTarget(pair.second());
			if(pair.second().equals(graph.parentEdge(w)))
			{
				//pair.second is a tree edge
				EdgeList l = graph.getOrderedAdjList(w);
				if(l != null)
				{
					EdgeList newL = new EdgeList();
					newL._edge = pair.second();
					newL._next = l;
					graph.setOrderedAdjLis(w, newL);
				}
				else
				{
					l = new EdgeList();
					l._edge = pair.second();
					graph.setOrderedAdjLis(w, l);
					
				}
				
				graph.setLeftRef(w, pair.second());
				graph.setRightRef(w, pair.second());

		
				DFS3(graph, w);
			}
			else
			{
				//pair.second() is a backedge
				if(graph.getSide(pair.second()) == 1)
				{
					EdgeList l = graph.getOrderedAdjList(w);
					if(l != null)
					{
						while(l != null && l._edge != graph.getRightRef(w))
						{
							l = l._next;
						}
						EdgeList newL = new EdgeList();
						newL._edge = pair.second();
						newL._next = l._next;
						l._next = newL;
					}
					else
					{
						l = new EdgeList();
						l._edge = pair.second();
						graph.setOrderedAdjLis(w, l);
					}
				}
				else
				{;
					EdgeList l = graph.getOrderedAdjList(w);
					EdgeList prevL = null;

					while(l._edge != graph.getLeftRef(w))
					{
						prevL = l;
						l = l._next;
					}

					EdgeList newL = new EdgeList();
					newL._edge = pair.second();
					if(prevL != null)
					{
						prevL._next = newL;
					}
					newL._next = l;
					graph.setLeftRef(w, pair.second());
				}
			}
			
		}
		
	}

	
	public static int sign(LROrientedDFSGraph graph,DefaultEdge e)
	{
		if(graph.getEdgeRef(e) != null)
		{
			graph.setSide(e, graph.getSide(e)*sign(graph,graph.getEdgeRef(e)));
			graph.setEdgeRef(e, null);
		}
		return graph.getSide(e);
	}

	
}



