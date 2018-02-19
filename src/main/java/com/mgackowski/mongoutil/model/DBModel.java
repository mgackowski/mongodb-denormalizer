package com.mgackowski.mongoutil.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Models the MongoDB collections and relationships between them.
 * @author mgackowski
 *
 */
public class DBModel {
	
	private static final Logger LOG = LogManager.getLogger(DBModel.class);
	
	private List<CollModel> collections = new ArrayList<>();
	
	public DBModel add(CollModel collection){
		collections.add(collection);
		return this;
	}
	
	public List<CollModel> getCollections() {
		return collections;
	}

	/**
	 * Ensures all entities in the model are in a complete state. Allows the
	 * API to retain a readable, fluid format without the need to invoke build
	 * methods.
	 * @return true if all mandatory fields in the model and nested objects are
	 * populated
	 */
	public boolean validateAll() {
		boolean valid = true;
		for (CollModel collection : collections) {
			if(!collection.validate()) {
				LOG.error("Invalid collection model: {}", collection);
				valid = false;
			}
		}
		return valid;
	}
	
	@Override
	public String toString() {
		StringBuilder verbose = new StringBuilder("Collection model:");
		if (!collections.isEmpty()) {
			collections.forEach(coll -> verbose.append("{" + coll + "}\n"));
		}
		return verbose.toString();
	}

}
