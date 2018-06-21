package model;

import java.util.List;

import com.github.drinkjava2.jbeanbox.BeanBox;
import com.github.drinkjava2.jsqlbox.annotation.Ioc;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;
 

public abstract class AbstractTeam extends Team {

	@Ioc(EntityListHandlerBox.class)
	public abstract List<Team> queryAbstractRatingBiggerThan(Integer rating);
	/*- 
	 select t.*
	    from teams t
	    where t.rating>:rating
	*/ 
	
	public static class EntityListHandlerBox extends BeanBox {
		public EntityListHandler create() {
			return new EntityListHandler(Team.class);
		}
	}
}
