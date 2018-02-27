package com.mgackowski.mongoutil.cli;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import org.bson.Document;
import org.junit.Test;

import com.mgackowski.mongoutil.TestUtils;
import com.mgackowski.mongoutil.model.CollModel;
import com.mgackowski.mongoutil.model.DBModel;
import com.mgackowski.mongoutil.model.JoinModel;

public class ModelAdaptorTest {
	
	@Test
	public void toDBModel_correctlyTranslatesModel() {
		
		String modelPathString = TestUtils.TEST_FILE_DIR + "testmodel.json";
		Path modelPath = FileSystems.getDefault().getPath(modelPathString);
		Document modelJSON = null;
		
		try {
			modelJSON = Document.parse(new String(Files.readAllBytes(modelPath)));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		DBModel result = ModelAdaptor.toDBModel(modelJSON);
		
		assertEquals("test-db-name", result.getDBName());
		assertEquals(2, result.getCollections().size());
		
		CollModel collA = result.getCollections().get(0);
		assertEquals("test-source-collection-name-A", collA.getName());
		assertEquals(2, collA.getJoins().size());
		
		JoinModel joinA1 = collA.getJoins().get(0);
		assertEquals("test-target-collection-name-A1", joinA1.getTargetCollection());
		assertEquals("test-onsource-A", joinA1.getSourceForeignKeyColumn());
		assertEquals("test-ontarget-A", joinA1.getTargetLinkColumn());
		assertEquals("test-newArrayName-A", joinA1.getTargetNewArrayName());
		assertEquals("test-reference-A", joinA1.getSourceLinkColumn());
		assertEquals(3, joinA1.getEmbeddedFields().size());
		
		assertEquals("test-embed-A1", joinA1.getEmbeddedFields().get(0));
		assertEquals("test-embed-A2", joinA1.getEmbeddedFields().get(1));
		assertEquals("test-embed-A3", joinA1.getEmbeddedFields().get(2));
		
		JoinModel joinA2 = collA.getJoins().get(1);
		assertEquals("test-target-collection-name-A2", joinA2.getTargetCollection());
		assertEquals("test-onsource-A2", joinA2.getSourceForeignKeyColumn());
		assertEquals("test-ontarget-A2", joinA2.getTargetLinkColumn());
		assertEquals("test-newArrayName-A2", joinA2.getTargetNewArrayName());
		assertEquals("test-reference-A2", joinA2.getSourceLinkColumn());
		assertEquals(3, joinA2.getEmbeddedFields().size());
		
		assertEquals("test-embed-A4", joinA2.getEmbeddedFields().get(0));
		assertEquals("test-embed-A5", joinA2.getEmbeddedFields().get(1));
		assertEquals("test-embed-A6", joinA2.getEmbeddedFields().get(2));
		
		CollModel collB = result.getCollections().get(1);
		assertEquals("test-source-collection-name-B", collB.getName());
		assertEquals(1, collB.getJoins().size());
		
		JoinModel joinB = collB.getJoins().get(0);
		assertEquals("test-target-collection-name-B1", joinB.getTargetCollection());
		assertEquals("test-onsource-B1", joinB.getSourceForeignKeyColumn());
		assertEquals("test-ontarget-B1", joinB.getTargetLinkColumn());
		assertEquals("test-newArrayName-B1", joinB.getTargetNewArrayName());
		assertEquals("test-reference-B1", joinB.getSourceLinkColumn());
		assertEquals(3, joinB.getEmbeddedFields().size());
		
		assertEquals("test-embed-B1", joinB.getEmbeddedFields().get(0));
		assertEquals("test-embed-B2", joinB.getEmbeddedFields().get(1));
		assertEquals("test-embed-B3", joinB.getEmbeddedFields().get(2));
		
	}

}
