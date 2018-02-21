package com.mgackowski.mongoutil;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import org.bson.Document;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mgackowski.mongoutil.Denormalizer;
import com.mgackowski.mongoutil.model.CollModel;
import com.mgackowski.mongoutil.model.DBModel;
import com.mgackowski.mongoutil.model.JoinModel;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientOptions;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class DenormalizerTest {
	
	private List<String> collections = Arrays.asList
			("planets", "moons", "organizations", "affiliations");
	
	private static String TEST_SERVER = "localhost:27017";
	private static String TEST_DB_NAME  = "denormalizerTest";
	private static String TEST_FILE_DIR = "src/test/resources/";
	private static int MAX_WAIT = 100;
	
	private static MongoDatabase db;
	private static MongoClient client;
	private Denormalizer subject;
	
	@Test
	public void denormalize_skipsIfEmptyModel() {		
		List<String> before = getJsonListOfDb();
		
		assertTrue(subject.denormalize(new DBModel()));
		
		List<String> after = getJsonListOfDb();
		assertTrue(before.equals(after));
	}
	
	@Test
	public void denormalize_skipsIfInvalidModel() {
		List<String> before = getJsonListOfDb();
		DBModel badModel;
		
		badModel = new DBModel().add(new CollModel("coll")
				.join(new JoinModel("targetColl").on("src", "tgt")));
		assertFalse(subject.denormalize(badModel));
		
		badModel = new DBModel().add(new CollModel("coll")
				.join(new JoinModel("targetColl").on("src", "tgt")
						.as("newName")));
		assertFalse(subject.denormalize(badModel));
		
		badModel = new DBModel().add(new CollModel("coll")
				.join(new JoinModel("targetColl")))
				.add(new CollModel("emptyColl"));
		assertFalse(subject.denormalize(badModel));
		
		List<String> after = getJsonListOfDb();
		assertTrue(before.equals(after));
	}
	
	@Test
	public void denormalize_acceptsCollModelWithMandatoryFieldsOnly() {
		
		DBModel model = new DBModel().add(new CollModel("affiliations"));
		assertTrue(subject.denormalize(model));
		
		assertEquals(getJsonListFromFile("affiliations"),
				getJsonListOfColl("affiliations"));
	}
	
	@Test
	public void denormalize_createsRefs() {
		
		DBModel model = new DBModel().add(new CollModel("moons")
				.join(new JoinModel("planets").on("planet_id", "_id")
						.as("moons").reference("_id")));
		assertTrue(subject.denormalize(model));
		
		assertEquals(getJsonListFromFile("planets_ref"),
				getJsonListOfColl("planets"));
	}
	
	@Test
	public void denormalize_createsEmbeds() {
		DBModel model = new DBModel().add(new CollModel("moons")
				.join(new JoinModel("planets").on("planet_id", "_id")
						.as("moons").reference("_id").embed("name")));
		
		assertTrue(subject.denormalize(model));
	
		assertEquals(getJsonListFromFile("planets_embed"),
				getJsonListOfColl("planets"));
	}
	
	@Test
	public void denormalize_createsMultipleEmbeds() {
		DBModel model = new DBModel().add(new CollModel("moons")
				.join(new JoinModel("planets").on("planet_id", "_id")
						.as("moons").reference("_id")
						.embed("name").embed("native")));
		
		assertTrue(subject.denormalize(model));
	
		assertEquals(getJsonListFromFile("planets_embed_multi"),
				getJsonListOfColl("planets"));
	}
	
	@Test
	public void denormalize_doesntModifyOtherColls() {
		DBModel model = new DBModel()
				.add(new CollModel("planets"))
				.add(new CollModel("moons").join(new JoinModel("planets")
						.on("planet_id", "_id").as("moons").reference("_id")
						.embed("name")))
				.add(new CollModel("organizations"));
		
		assertTrue(subject.denormalize(model));
		
		assertEquals(getJsonListFromFile("planets_embed"),
				getJsonListOfColl("planets"));
		assertEquals(getJsonListFromFile("organizations"),
				getJsonListOfColl("organizations"));
		assertEquals(getJsonListFromFile("moons"),
				getJsonListOfColl("moons"));
	}
	
	@Test
	public void denormalize_repeatsForMultipleColls() {
		DBModel model = new DBModel()
				.add(new CollModel("moons").join(new JoinModel("planets")
						.on("planet_id", "_id").as("moons").reference("_id")
						.embed("name")))
				.add(new CollModel("affiliations").join(
						new JoinModel("organizations")
						.on("organization_id", "_id")
						.as("affiliations").reference("_id")));
		
		assertTrue(subject.denormalize(model));
		
		assertEquals(getJsonListFromFile("planets_embed"),
				getJsonListOfColl("planets"));
		assertEquals(getJsonListFromFile("organizations_one_to_many"),
				getJsonListOfColl("organizations"));
	}
	
	@Test
	public void denormalize_splitsLinkTable() {
		DBModel model = new DBModel()
				.add(new CollModel("affiliations")
						.join(new JoinModel("organizations")
								.on("organization_id", "_id")
								.as("planets").reference("planet_id"))
						.join(new JoinModel("planets")
								.on("planet_id", "_id")
								.as("organizations")
								.reference("organization_id")));
		
		assertTrue(subject.denormalize(model));
		
		assertEquals(getJsonListFromFile("organizations_many_to_many"),
				getJsonListOfColl("organizations"));
		assertEquals(getJsonListFromFile("planets_many_to_many"),
				getJsonListOfColl("planets"));
		assertEquals(getJsonListFromFile("affiliations"),
				getJsonListOfColl("affiliations"));
	}
	
	@Test
	public void denormalize_splitsLinkTableAndEmbedsFields() {
		DBModel model = new DBModel()
				.add(new CollModel("affiliations")
						.join(new JoinModel("organizations")
								.on("organization_id", "_id")
								.as("planets").reference("planet_id"))
						.join(new JoinModel("planets")
								.on("planet_id", "_id")
								.as("organizations")
								.reference("organization_id")
								.embed("relationship")));
		
		assertTrue(subject.denormalize(model));
		
		assertEquals(getJsonListFromFile("organizations_many_to_many"),
				getJsonListOfColl("organizations"));
		assertEquals(getJsonListFromFile("planets_many_to_many_embed"),
				getJsonListOfColl("planets"));
		assertEquals(getJsonListFromFile("affiliations"),
				getJsonListOfColl("affiliations"));
	}
	
	@BeforeClass
	public static void setUpClass() {
        client = new MongoClient(new ServerAddress(TEST_SERVER),
        		MongoClientOptions.builder()
        		.serverSelectionTimeout(MAX_WAIT)
        		.build());
        db = client.getDatabase(TEST_DB_NAME);
	}
	
    @Before
    public void setUp() {
    	loadTestData();
        subject = new DefaultDenormalizer(db);
    }
    
    @After
    public void tearDown() {
    	db.drop();
    }
    
    @AfterClass
    public static void tearDownClass() {
        client.close();
    }
    
	private void loadTestData() {
		for(String colName : collections) {
			MongoCollection<Document> col = db.getCollection(colName);
			try(Stream<String> stream = Files.lines(
					Paths.get(TEST_FILE_DIR + colName))) {
				stream.forEach((String jsonDoc)
						-> col.insertOne(Document.parse(jsonDoc)));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}	
	}
	
	private List<String> getJsonListOfColl(String colName) {
		List<String> list = new ArrayList<>();
		db.getCollection(colName).find().forEach(
				(Document doc) -> list.add(doc.toJson()));
		return list;
	}
	
	private List<String> getJsonListOfDb() {
		List<String> list = new ArrayList<>();
		for(String colName : collections) {
			list.addAll(getJsonListOfColl(colName));
		}
		return list;
	}
	
	private List<String> getJsonListFromFile(String filename) {
		final List<String> list = new ArrayList<>();
		try(Stream<String> stream = Files.lines(Paths.get(TEST_FILE_DIR + filename))) {
			stream.forEach((String jsonDoc) -> list.add(Document.parse(jsonDoc).toJson()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

}
