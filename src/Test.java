import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.Vector;

public class Test
{
	public static void main(String[] args)
	{
		LRUndirectedGraph g = LRUndirectedGraph.ErdosRenyiGraph(5, 1.0);
		FileHandler.writeJson(g.generateJsonObject(), "K_5ER");
		
	}

	
	

}
