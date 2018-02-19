package com.mgackowski.mongoutil;

import com.mgackowski.mongoutil.model.DBModel;

public interface Denormalizer {

	/**
	 * For all modelled collections, takes fields containing references
	 * to other collections (defined here as target collections) and creates
	 * arrays in those target collections that reference or embed documents
	 * from the source collections, as defined by the user-provided model.
	 * <p>
	 * If embedded fields are provided in the model, this method will copy them
	 * from the source collection to the target collection – effectively
	 * denormalizing the collections by creating embedded documents in the
	 * target.
	 * <p>
	 * If the model defines a join such that the field in the source collection
	 * is not a primary key of the source collection (index), but rather a
	 * reference to a third collection, then the newly created arrays in the
	 * target collection will reference that third collection; this is useful
	 * for processing a collection that was previously a link table modelling
	 * a many-to-many relationship in a relational database.
	 * <p>
	 * This method does not remove any existing fields or collections.
	 * 
	 * @author mgackowski
	 */
	boolean denormalize(DBModel model);

}