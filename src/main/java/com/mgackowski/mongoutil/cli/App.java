package com.mgackowski.mongoutil.cli;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.bson.BSON;
import org.bson.BSONObject;

import com.mgackowski.mongoutil.DenormalizerFactory;
import com.mgackowski.mongoutil.model.DBModel;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class App {
	
	public static void main(String args[]) {
		
		//TODO: Validate arguments.
		
		System.out.println("This is not implemented yet.");
		System.exit(1);

		Path modelPath = FileSystems.getDefault().getPath(args[1]);
		
		BSONObject modelBSON = null;
		try {
			modelBSON = BSON.decode(Files.readAllBytes(modelPath));
		} catch (IOException e) {
			System.out.println("Model path invalid! Please provide BSON model.");
			e.printStackTrace();
			System.exit(2);
		}
		
		DBModel model = ModelAdaptor.toDBModel(modelBSON);	
		
		MongoClient client = new MongoClient(new MongoClientURI(args[0]));
		MongoDatabase db = client.getDatabase(model.getDBName());
		
		DenormalizerFactory.getDenormalizer(db).denormalize(model);
		
		client.close();
		System.exit(3);
		
	}

}