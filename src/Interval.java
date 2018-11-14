import org.jgrapht.graph.DefaultEdge;

public class Interval extends Pair<DefaultEdge,DefaultEdge>
{

	public String toString()
	{
		return "[" + low() + "--" + high() + "]"; 
	}
	public boolean equals(Interval other)
	{
		if(low() != null && high()!=null && other.low() != null && other.high() != null)
		{
			if(low() == other.low() && high() == other.high())
			{
				return true;
			}
			else
			{
				return false;
			}
		}
		else if(low() == null && high() == null && other.low() == null && other.high() == null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	public Interval(Interval other)
	{

		this(other.low(),other.high());
	}
	public Interval(DefaultEdge first, DefaultEdge second)
	{
		super(first, second);
		// TODO Auto-generated constructor stub
	}
	public static Interval emptyInterval()
	{
		return new Interval(null,null);
	}
	public DefaultEdge low()
	{
		return first();
	}
	
	public DefaultEdge high()
	{
		return second();
	}
	
	public void setLow(DefaultEdge low)
	{

		setFirst(low);
		

	}
	
	public void setHigh(DefaultEdge high)
	{

			setSecond(high);

	}
	
	public boolean isEmpty()
	{
		if(high() == null && low() == null)
		{
			return true;
		}
		else
		{
			return false;
		}
	}

}
