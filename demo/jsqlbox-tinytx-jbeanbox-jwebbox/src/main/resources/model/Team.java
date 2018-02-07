package model;

import java.util.List;

import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jsqlbox.ActiveRecord;

@Table(name = "teams")
public class Team extends ActiveRecord {

	@Id
	private Integer id;

	private String name;

	private Integer rating;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
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

	public List<Team> getTeamsRatingBiggerThan(Integer rating) {
		// If use guess method to read annotation or text SQL, make sure model class be
		// put in resources folder instead of in src folder, detail see Readme.md
		return guess(rating);
	}
	/*- 
	 select t.**
	    from teams t
	    where t.rating>:rating
	*/
}
