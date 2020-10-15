package model;

import static com.github.drinkjava2.jsqlbox.DB.bind;
import static com.github.drinkjava2.jsqlbox.DB.qryEntityList;

import java.util.List;

import com.github.drinkjava2.jdbpro.Text;
import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.DB;

@Table(name = "teams")
public class Team extends ActiveRecord<Team> {

	@Id
	@UUID25
	private String id;

	private String name;

	private Integer rating;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getRating() {
		return rating;
	}

	public void setRating(Integer rating) {
		this.rating = rating;
	}

	public List<Team> queryTeamsRatingNotEqual(Integer rating) {
		return qryEntityList(DB.TEMPLATE,  Team.class, "select * from teams where rating<>:rating", bind("rating", rating));
	}

	public List<Team> queryTeamsRatingEqualTo(Integer rating) {
		return qryEntityList(DB.TEMPLATE, Team.class, TeamsRatingEqualText.class, bind("rating", rating));
	}

	public static class TeamsRatingEqualText extends Text {
		/*- 
		 select t.*
		    from teams t
		    where t.rating=:rating
		*/
	}

	public List<Team> queryTeamsRatingBiggerThan(Integer rating) {
		return qryEntityList(DB.TEMPLATE, Team.class, TeamsRatingBiggerThanText.class, bind("rating", rating));
	}

	public static class TeamsRatingBiggerThanText extends Text {
		/*- 
		 select t.*
		from teams t
		where t.rating>:rating
		*/
	}

}
