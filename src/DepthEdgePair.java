import org.jgrapht.graph.DefaultEdge;

public class DepthEdgePair extends Pair<Integer,DefaultEdge> implements Comparable<Pair<Integer,DefaultEdge>>
{

	public DepthEdgePair(Integer first, DefaultEdge second)
	{
		super(first, second);
		// TODO Auto-generated constructor stub
	}

	@Override
	public int compareTo(Pair<Integer, DefaultEdge> o)
	{
		int firstComp = first().compareTo(o.first());
		if(firstComp != 0)
		{
			return firstComp;
		}
		else
		{
			return Integer.compare(second().hashCode(), o.second().hashCode());
			
		}
	}
	



}
