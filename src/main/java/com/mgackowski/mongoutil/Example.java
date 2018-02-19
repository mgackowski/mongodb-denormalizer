package com.mgackowski.mongoutil;

import com.mgackowski.mongoutil.model.CollModel;
import com.mgackowski.mongoutil.model.DBModel;
import com.mgackowski.mongoutil.model.JoinModel;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class Example 
{
    public static void main( String[] args )
    {
    	
    	MongoClient client = new MongoClient(new MongoClientURI("mongodb://localhost:27017"));
    	MongoDatabase db = client.getDatabase("mydatabase");
        
    	
    	// Model a relational–style many–to–many relationship (3 tables)
    	// and a separate one–to–many relationship (2 tables)
    	DBModel model = new DBModel()
    			.add(new CollModel("productsreleases")	// former link table
    					.join(new JoinModel("products")
    							.on("product_id", "_id")
    							.reference("release_id")
    							.as("releases"))
    					.join(new JoinModel("releases")
    							.on("release_id", "_id")
    							.reference("product_id")
    							.as("products")
    							.embed("is_account_locked")))
    			.add(new CollModel("address") // the "many" in "one–to–many"
    					.join(new JoinModel("person")
    							.on("person_id", "_id")
    							.reference("_id")
    							.as("addresses")
    							.embed("street")
    							.embed("city")
    							.embed("postcode")));
    	
    	// Turn them into arrays of references and embedded documents, in line
    	// with the preferred NoSQL data model
    	Denormalizer thatsConvenient = new DefaultDenormalizer(db);
    	thatsConvenient.denormalize(model);
        
        client.close();
        
    }
}