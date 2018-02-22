package com.mgackowski.mongoutil;

import com.mongodb.client.MongoDatabase;

/**
 * Provides a {@link Denormalizer}.
 * @see Denormalizer
 * @author mgackowski
 *
 */
public class DenormalizerFactory {
	
	/**
	 * Returns a new Denormalizer object which will act on a pre-existing
	 * MongoDB Database object.
	 * @param db the MongoDB Java driver database object
	 */
	public static Denormalizer getDenormalizer(MongoDatabase db) {
		return new DefaultDenormalizer(db);
	}

}