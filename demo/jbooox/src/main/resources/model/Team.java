package model;

import java.util.List;

import com.github.drinkjava2.jdialects.annotation.jdia.UUID25;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.annotation.Sql;

@Table(name = "teams")
public class Team extends ActiveRecord {

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

	@Sql("select t.** from teams t where t.rating<>:rating")
	public List<Team> queryTeamsRatingNotEqual(Integer rating) {
		return guess(rating);
	}

	public List<Team> queryTeamsRatingEqualTo(Integer rating) {
		return guess(rating);
	}
	/*- 
	 select t.**
	    from teams t
	    where t.rating=:rating
	*/
}
