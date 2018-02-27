package com.mgackowski.mongoutil;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.bson.Document;

public class TestUtils {
	
	public final static String TEST_FILE_DIR = "src/test/resources/";
	
	protected static List<String> getJsonListFromFile(String filename) {
		final List<String> list = new ArrayList<>();
		try(Stream<String> stream = Files.lines(Paths.get(TEST_FILE_DIR + filename))) {
			stream.forEach((String jsonDoc) -> list.add(Document.parse(jsonDoc).toJson()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		return list;
	}

}
