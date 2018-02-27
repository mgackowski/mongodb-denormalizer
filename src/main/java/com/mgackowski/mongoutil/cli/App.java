package com.mgackowski.mongoutil.cli;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.bson.Document;

import com.mgackowski.mongoutil.DenormalizerFactory;
import com.mgackowski.mongoutil.model.DBModel;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.client.MongoDatabase;

public class App {
	
	public static void main(String args[]) {
		
		if (args.length != 2) {
			printManual();
			System.exit(1);
		}

		Path modelPath = FileSystems.getDefault().getPath(args[1]);
		
		Document documentJson = null;
		try {
			documentJson = Document.parse(new String(Files.readAllBytes(modelPath)));
		} catch (IOException e) {
			System.out.println("Model path invalid! Please provide JSON model.");
			e.printStackTrace();
			System.exit(-1);
		}
		
		DBModel model = ModelAdaptor.toDBModel(documentJson);	
		
		MongoClient client = new MongoClient(new MongoClientURI(args[0]));
		MongoDatabase db = client.getDatabase(model.getDBName());
		
		DenormalizerFactory.getDenormalizer(db).denormalize(model);
		
		client.close();
		System.exit(0);
		
	}
	
	private static void printManual() {
		
		System.out.println("\nmongodb-denormalizer by @mgackowski\n"
				+ "For documentation visit: github.com/mgackowski/mongodb-denormalizer\n");
		
		System.out.println("Parameters: (1) MongoURI e.g. 'localhost:27017'");
		System.out.println("            (1) path of JSON file with model.");
	}

}