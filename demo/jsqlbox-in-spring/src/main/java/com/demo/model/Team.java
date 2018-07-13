package com.demo.model;

import java.util.List;

import com.github.drinkjava2.jdialects.annotation.jpa.Entity;
import com.github.drinkjava2.jdialects.annotation.jpa.GeneratedValue;
import com.github.drinkjava2.jdialects.annotation.jpa.GenerationType;
import com.github.drinkjava2.jdialects.annotation.jpa.Id;
import com.github.drinkjava2.jdialects.annotation.jpa.Table;
import com.github.drinkjava2.jdialects.annotation.jpa.TableGenerator;
import com.github.drinkjava2.jsqlbox.ActiveRecord;
import com.github.drinkjava2.jsqlbox.annotation.Sql;
import com.github.drinkjava2.jsqlbox.handler.EntityListHandler;

@Entity
@Table(name = "teams")
public class Team extends ActiveRecord<Team> {
	@Id
	@TableGenerator(name = "ID_GENERATOR", table = "pk_table", pkColumnName = "pk_col", pkColumnValue = "pk_val", valueColumnName = "val_col", initialValue = 1, allocationSize = 1)
	@GeneratedValue(strategy = GenerationType.TABLE, generator = "ID_GENERATOR")
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

	@Sql("select * from teams")
	public List<Team> finaAllTeams() {
		return guess(new EntityListHandler(), Team.class);
	}

}
