/*import java.util.SortedSet;
import java.util.TreeSet;

import org.jgrapht.graph.DefaultEdge;

/*public class LREdge extends DefaultEdge
{

	/**
	 * 
	 */
	/*private static final long serialVersionUID = 1L;



	
	private double _sourceHeight;
	private double _targetHeight;
	//private double _lowpt;
	//list of return edge identified by their target height because LR-partition <=> aligned LR-partition
	private SortedSet<Double> _returnEdgeHeight;
	//private double _lowpt2;
	
	
	public void addReturnEdgeHeight(SortedSet<Double> returnEdgeHeight)
	{
		_returnEdgeHeight.addAll(returnEdgeHeight.headSet(_sourceHeight));
	}
	




	public LREdge(double sourceHeight,double targetHeight)
	{
		super();
		_returnEdgeHeight = new TreeSet<>();
		
		//---------init phase---------
		set_sourceHeight(sourceHeight);
		set_targetHeight(targetHeight);
		//if the edge is a back edge, we add its target height 
		if(is_backEdge())
			_returnEdgeHeight.add(_targetHeight);
		//set_lowpt(Double.POSITIVE_INFINITY);
	}




	public boolean is_backEdge()
	{
		return _sourceHeight>_targetHeight;
	}




	public double get_sourceHeight()
	{
		return _sourceHeight;
	}




	public void set_sourceHeight(double _sourceHeight)
	{
		this._sourceHeight = _sourceHeight;
	}




	public double get_targetHeight()
	{
		return _targetHeight;
	}




	public void set_targetHeight(double _targetHeight)
	{
		this._targetHeight = _targetHeight;
	}
	
	public SortedSet<Double> get_returnEdgeHeight()
	{
		return _returnEdgeHeight;
	}





	public double get_lowpt()
	{
		if(!_returnEdgeHeight.isEmpty())
			return _returnEdgeHeight.first();
		else
			return Double.POSITIVE_INFINITY;
		
	}




	/*public void set_lowpt(double _lowpt)
	{
		this._lowpt = _lowpt;
	}




	


}*/
