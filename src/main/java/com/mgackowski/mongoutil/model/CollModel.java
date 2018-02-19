package com.mgackowski.mongoutil.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A representation of a MongoDB collection and its desired relationship with
 * other collections.
 * @author mgackowski
 */
public class CollModel {
	
	private static final Logger LOG = LogManager.getLogger(CollModel.class);
	
	private String name;
	private List<JoinModel> joins = new ArrayList<JoinModel>();
	
	public CollModel(String collectionName) {
		this.name = collectionName;
	}

	/**
	 * Models a join between collections; e.g. if sourceColl contains a field
	 * referencing targetColl, such as a foreign key, then:<br>
	 * <code>sourceColl.join(new JoinModel(targetColl).options());</code>
	 * @param join a JoinModel object modelling a link between two collections.
	 * @return itself to allow chaining methods (see Fluent API concept).
	 */
	public CollModel join(JoinModel join) {
		joins.add(join);
		return this;
	}
	
	protected boolean validate() {
		boolean valid = true;
		for (JoinModel join : joins) {
			if (!join.validate()) {
				LOG.error("Invalid join model: {}", join);
				valid = false;
			}
		}
		return valid;
	}

	public String getName() {
		return name;
	}

	public List<JoinModel> getJoins() {
		return joins;
	}
	
	@Override
	public String toString() {
		StringBuilder verbose = new StringBuilder("collection '" + name);
		if (!joins.isEmpty()) {
			verbose.append(" and foreign keys: {");
			joins.forEach(join -> verbose.append(" {" + join + "} "));
		}
		return verbose.toString();
	}

}