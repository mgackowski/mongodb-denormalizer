package com.mgackowski.mongoutil;

import static com.mongodb.client.model.Filters.eq;
import static com.mongodb.client.model.Filters.exists;
import static com.mongodb.client.model.Updates.push;

import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.bson.Document;

import com.mgackowski.mongoutil.model.CollModel;
import com.mgackowski.mongoutil.model.DBModel;
import com.mgackowski.mongoutil.model.JoinModel;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoCursor;
import com.mongodb.client.MongoDatabase;

/**
 * An implementation of the {@link Denormalizer} interface.
 * @author mgackowski
 */
public class DefaultDenormalizer implements Denormalizer {
	
	private static final Logger LOG = LogManager.getLogger(Denormalizer.class);
	
	private MongoDatabase db;
	
	/**
	 * Creates a Denormalizer object which will act on a pre-existing MongoDB
	 * Database object – useful for projects which already depend on the
	 * MongoDB java driver and define their own client.
	 * @param db the MongoDB Java driver database object
	 */
	protected DefaultDenormalizer(MongoDatabase db) {
		this.db = db;
	}

	/* (non-Javadoc)
	 * @see com.mgackowski.mongoutil.Denormalizer#denormalize(com.mgackowski.mongoutil.DBModel)
	 */
	@Override
	public boolean denormalize(DBModel model) {
		
		LOG.info("Validating model...");
		
		if(!model.validateAll()) {
			LOG.error("Model validation failed, operation aborted. {}", model);
			return false;
		}
		
		LOG.info("Denormalizing...");
			
		for (CollModel collection : model.getCollections()) {
			
			List<JoinModel> joins = collection.getJoins();
			if(joins.isEmpty()) {
				continue;
			}
			
			LOG.info("Processing collection '{}'", collection);
			
			String sourceCollName = collection.getName();
			MongoCollection<Document> sourceColl = db.getCollection(sourceCollName);

			for (JoinModel join : joins) {
				
				String targetCollName = join.getTargetCollection();
				String targetNewArrayName = join.getTargetNewArrayName();
				String targetKeyName = join.getTargetLinkColumn();
				String sourceForeignKeyName = join.getSourceForeignKeyColumn();
				String sourceLinkColumn = join.getSourceLinkColumn();
				List<String> fieldsToEmbed = join.getEmbeddedFields();
				
				MongoCollection<Document> targetColl = db.getCollection(targetCollName);

				MongoCursor<Document> links = sourceColl
						.find(exists(sourceForeignKeyName))
						.iterator();
				
				while(links.hasNext()) {
					Document doc = links.next();
					Object sourceForeignKeyValue = doc.get(sourceForeignKeyName);
					LOG.debug("   Processing {} : {}", sourceForeignKeyName, sourceForeignKeyValue);
					
					if(fieldsToEmbed.isEmpty()) {
					targetColl.updateMany(
						eq(targetKeyName, sourceForeignKeyValue),
							push(targetNewArrayName, doc.get(sourceLinkColumn)));
					}
					else {
						Document referenceObject = new Document("_id", doc.get(sourceLinkColumn));
						for(String field : fieldsToEmbed) {
							Object value = doc.get(field);
							if(value == null) continue;
							referenceObject.append(field, doc.get(field));
						}
						targetColl.updateMany(
								eq(targetKeyName, sourceForeignKeyValue),
									push(targetNewArrayName, referenceObject));
					}
				}
				LOG.info("Done – array {} in {} is referencing {} documents by {}",
						targetNewArrayName, targetCollName, sourceCollName, sourceLinkColumn);
			}
		}
		LOG.info("Denormalization done on collections provided in the model.");
		return true;
	}

}
