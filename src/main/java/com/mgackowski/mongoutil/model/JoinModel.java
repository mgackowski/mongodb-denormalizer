package com.mgackowski.mongoutil.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Models a desired relationship between two collections.
 * <p>
 * The source collection is a collection that contains a field referencing
 * another collection (similar to a foreign key in relational databases). In a
 * one–to–many relationship, it is the "of–many"–type entity.
 * <p>
 * The target collection is the entity referenced by the source collection;
 * in other words, it is the "one" in "one–to–many". After being processed by
 * the Denormalizer, the target collection being moddeled will gain an array
 * referencing or embedding documents from the source collection, in line with
 * NoSQL data model practices.
 * @author mgackowski
 *
 */
public class JoinModel {
	
	private static final Logger LOG = LogManager.getLogger(JoinModel.class);
	
	private String targetCollection;
	private String targetLinkColumn;
	private String sourceForeignKeyColumn;
	private String sourceLinkColumn;
	private String targetNewArrayName;
	private List<String> embeddedFields = new ArrayList<String>();


	/**
	 * Traditional constructor which ensures that all mandatory fields are
	 * provided.
	 */
	public JoinModel(String targetCollection, String targetLinkColumn,
			String sourceForeignKeyColumn, String sourceLinkColumn,
			String targetNewArrayName) {
		this.targetCollection = targetCollection;
		this.targetLinkColumn = targetLinkColumn;
		this.sourceForeignKeyColumn = sourceForeignKeyColumn;
		this.sourceLinkColumn = sourceLinkColumn;
		this.targetNewArrayName = targetNewArrayName;
	}
	
	/**
	 * Partial constructor. Chain the on(), as(), reference() and embed()
	 * methods to create the full object in a readable way.
	 * @param targetCollection name of the target collection for the join
	 */
	public JoinModel(String targetCollection) {
		this.targetCollection = targetCollection;
	}

	/**
	 * (<b>Mandatory</b>) Designates the fields on which to join the two
	 * collections, similarly to an SQL join.
	 * @param sourceForeignKey the field in the source collection containing a
	 * reference to the target collection
	 * @param targetPrimaryKey the uniquely identifying field in the target
	 * collection that the source collection is referencing
	 * @return this JoinModel for chaining more commands (see Fluent APIs)
	 */
	public JoinModel on(String sourceForeignKey, String targetPrimaryKey) {
		this.sourceForeignKeyColumn = sourceForeignKey;
		this.targetLinkColumn = targetPrimaryKey;
		return this;
	}
	
	/**
	 * (<b>Mandatory</b>) Designates the name of the array in the target
	 * collection which will store references/documents of the source
	 * collection. If it does not yet exist in the target collection, it will be
	 * created during the denormalization process.
	 * @param targetArrayName the name of the array
	 * @return this JoinModel for chaining more commands (see Fluent APIs)
	 */
	public JoinModel as(String targetArrayName) {
		this.targetNewArrayName = targetArrayName;
		return this;
	}
	
	/**
	 * (<b>Mandatory</b>) Designates the name of the field in the source
	 * collection which will be referenced in the new array within the target
	 * collection. In a one–to–one or a one–to–many relationship, this is
	 * typically the primary key / unique index of the source collection.
	 * <p>
	 * If the source collection was modelling a link table in a many–to–many
	 * relationship, this field can be pointed to the other foreign key in the
	 * source collection, so that the resulting array in the target collection
	 * "skips" the link table and directly references a third collection with
	 * which it has a many–to–many relationship with.
	 * @param sourceLinkColumn the name of the column
	 * @return this JoinModel for chaining more commands (see Fluent APIs)
	 */
	public JoinModel reference(String sourceLinkColumn) {
		this.sourceLinkColumn = sourceLinkColumn;
		return this;
	}
	
	/**
	 * (Optional) Designates a field in the source collection that should be
	 * inserted into the target collection's array as part of an embedded
	 * document.
	 * <p>
	 * Calling this method at least once on a join will change the target data
	 * structure from an array of IDs to an array of documents. The ID will
	 * still be a field in the document, in addition to any fields added by this
	 * method.
	 * @param field name of the field to copy over
	 * @return this JoinModel for chaining more commands (see Fluent APIs)
	 */
	public JoinModel embed(String field) {
		embeddedFields.add(field);
		return this;
	}

	public String getTargetCollection() {
		return targetCollection;
	}

	public String getTargetLinkColumn() {
		return targetLinkColumn;
	}

	public String getSourceForeignKeyColumn() {
		return sourceForeignKeyColumn;
	}

	public String getSourceLinkColumn() {
		return sourceLinkColumn;
	}

	public String getTargetNewArrayName() {
		return targetNewArrayName;
	}

	public List<String> getEmbeddedFields() {
		return embeddedFields;
	}

	@Override
	public String toString() {
		StringBuilder verbose = new StringBuilder(
				"JoinModel [targetCollection=" + targetCollection +
				", sourceForeignKeyColumn=" + sourceForeignKeyColumn +
				", sourceLinkColumn=" + sourceLinkColumn +
				", targetNewArrayName=" + targetNewArrayName);
		if (!embeddedFields.isEmpty()) {
			verbose.append(", embeddedFields=[");
			embeddedFields.forEach(field -> verbose.append(" " + field));
			verbose.append("]");
		}
		verbose.append("]");
		return verbose.toString();
	}
	
	protected boolean validate() {
		boolean valid = true;
		
		if (sourceForeignKeyColumn == null) {
			LOG.error("Missing field name in source; join={}", this);
			valid = false;
		}
		if (sourceLinkColumn == null) {
			LOG.error("Missing field name in source; join={}", this);
			valid = false;
		}
		if (targetCollection == null) {
			LOG.error("Missing collection name; join={}", this);
			valid = false;
		}
		if (targetLinkColumn == null) {
			LOG.error("Missing field name in target; join={}", this);
			valid = false;
		}
		if (targetNewArrayName == null) {
			LOG.error("Missing field name in target; join={}", this);
			valid = false;
		}
		return valid;
	}	
	
}