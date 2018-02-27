package com.mgackowski.mongoutil.cli;

import java.util.List;

import org.bson.Document;

import com.mgackowski.mongoutil.model.CollModel;
import com.mgackowski.mongoutil.model.DBModel;
import com.mgackowski.mongoutil.model.JoinModel;

public class ModelAdaptor {
		
	@SuppressWarnings("unchecked")
	public static DBModel toDBModel(Document json) {

		DBModel model = new DBModel(json.getString("database"));
		
		List<Document> collections = (List<Document>) json.get("collections");
		for (Document collection : collections) {
			model.add(toCollModel(collection));
		}
		
		return model;	
	}
	
	@SuppressWarnings("unchecked")
	private static CollModel toCollModel(Document coll) {
		
		CollModel collModel = new CollModel(coll.getString("name"));
		List<Document> joins = (List<Document>) coll.get("joins");
		for(Document join : joins) {
			collModel.join(toJoinModel(join));
		}
		
		return collModel;
	}
	
	@SuppressWarnings("unchecked")
	private static JoinModel toJoinModel(Document join) {
		
		JoinModel joinModel = new JoinModel(join.getString("collection"));
		
		joinModel.on(
				(String) join.get("onSource"),
				(String) join.get("onTarget"));
		joinModel.as((String) join.get("as"));
		joinModel.reference((String) join.get("reference"));
		List<Object> embed = (List<Object>) join.get("embed");
		for(Object e : embed) {
			joinModel.embed((String) e);
		}
		
		return joinModel;
	}

}
