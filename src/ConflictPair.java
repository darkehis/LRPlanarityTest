
public class ConflictPair extends Pair<Interval, Interval>
{

	public ConflictPair(Interval left, Interval right)
	{
		super(left,right);
		// TODO Auto-generated constructor stub
	}
	
	public String toString()
	{
		return "{" + left() + "::" + right() + "}"; 
	}
	public boolean equals(ConflictPair other)
	{
		if(!right().equals(Interval.emptyInterval()) && !left().equals(Interval.emptyInterval()) 
				&& !other.right().equals(Interval.emptyInterval()) && !other.left().equals(Interval.emptyInterval()))
		{
			if(right().equals(other.right()) && left().equals(other.left()))
			{
				return true;
			}
		}
		else if(right().equals(Interval.emptyInterval()) && other.right().equals(Interval.emptyInterval()) 
				&& !left().equals(Interval.emptyInterval()) && !other.left().equals(Interval.emptyInterval()))
		{
			return left().equals(other.left());
		}
		else if(!right().equals(Interval.emptyInterval()) && !other.right().equals(Interval.emptyInterval()) 
				&& left().equals(Interval.emptyInterval()) && other.left().equals(Interval.emptyInterval()))
		{
			return right().equals(other.right());
		}
		else if(right().equals(Interval.emptyInterval()) && other.right().equals(Interval.emptyInterval()) 
				&& left().equals(Interval.emptyInterval()) && other.left().equals(Interval.emptyInterval()))
		{
			return true;
		}
		{
			return false;
		}
	}
	
	
	public static ConflictPair emptyPair()
	{
		return new ConflictPair(Interval.emptyInterval(),Interval.emptyInterval());
	}
	
	public Interval left()
	{
		return first();
	}
	
	public Interval right()
	{
		return second();
	}
	
	public void setLeft(Interval left)
	{
		setFirst(left);
	}
	
	public void setRight(Interval right)
	{
		setSecond(right);
	}
	
	public void swapLR()
	{
		Interval auxInt = left();
		setLeft(right());
		setRight(auxInt);		
	}

}
