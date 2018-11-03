
/*public class LRVertex
{
	
	
	
	
	
	@Override
	public String toString()
	{
		return Integer.toString(_id);
	}
	private boolean _marked;
	//vertices are identified by their id: -1 is not a valid id, this mean unidentified vertex
	private int _id;
	private double _lowpt;

	private double _height;
	
	
	public LRVertex()
	{
		//_marked = false;
		set_height(Double.POSITIVE_INFINITY);
		set_lowpt(Double.POSITIVE_INFINITY);
		_id =  -1;
	}
	@Override
	public int hashCode()
	{
		return _id;
	}
	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		LRVertex other = (LRVertex) obj;
		if (_id != other._id ||_id == -1 || other._id == -1)
			return false;
		return true;
	}
	public LRVertex(int id)
	{
		this();
		_id = id;
	}
	
	public LRVertex(LRVertex v)
	{
		this(v.get_id());
		_height = v._height;
	}
	

	public boolean is_marked()
	{
		return _marked;
	}

	public void set_marked(boolean _marked)
	{
		this._marked = _marked;
	}
	public double get_height()
	{
		return _height;
	}
	public void set_height(double _height)
	{
		this._height = _height;
	}
	public int get_id()
	{
		return _id;
	}
	public double get_lowpt()
	{
		return _lowpt;
	}
	public void set_lowpt(double _lowpt)
	{
		this._lowpt = _lowpt;
	}

}*/
