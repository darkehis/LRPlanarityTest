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
		Pair<Integer,Integer> right = new Pair<>(0,1);
		Pair<Integer, Integer> left = new Pair<>(1,2);
		Pair<Pair<Integer,Integer>,Pair<Integer,Integer>> conflictPair = new Pair<>(left,right);
		
		
		
		System.out.println("(" + conflictPair.second().second());
		
	}

	
	

}
