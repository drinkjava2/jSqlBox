package model;

import java.util.List;

public abstract class AbstractTeam extends Team {

	public abstract List<Team> queryAbstractRatingBiggerThan(Integer rating);
	/*- 
	 select t.**
	    from teams t
	    where t.rating>:rating
	*/ 
}
