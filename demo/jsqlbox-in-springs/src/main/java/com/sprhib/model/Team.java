package com.sprhib.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.TableGenerator;

@Entity
@Table(name = "teams")
public class Team {
	static {
		for (int i = 0; i < 200; i++) {
			System.out.println("55 ");
		}
	}

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

}
