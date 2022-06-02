package fr.cnumr.python.checks;

import org.sonar.check.Priority;
import org.sonar.check.Rule;
import org.sonar.plugins.java.api.IssuableSubscriptionVisitor;
import org.sonar.plugins.java.api.semantic.MethodMatchers;
import org.sonar.plugins.java.api.tree.BaseTreeVisitor;
import org.sonar.plugins.java.api.tree.MethodInvocationTree;
import org.sonar.plugins.java.api.tree.Tree;
import org.sonar.plugins.java.api.tree.Tree.Kind;

import java.util.Arrays;
import java.util.List;

import static org.sonar.plugins.java.api.semantic.MethodMatchers.CONSTRUCTOR;

@Rule(key = "S64", name = "Developpement", description = AvoidSQLRequestInLoop.MESSAGERULE, priority = Priority.MINOR,
	tags = {"bug" })
public class AvoidSQLRequestInLoop extends IssuableSubscriptionVisitor {

	protected static final String MESSAGERULE = "Avoid SQL request in loop";
	private final AvoidSQLRequestInLoopVisitor visitorInFile = new AvoidSQLRequestInLoopVisitor();

	@Override
	public List<Kind> nodesToVisit() {
		return Arrays.asList(Tree.Kind.FOR_EACH_STATEMENT, Tree.Kind.FOR_STATEMENT, Tree.Kind.WHILE_STATEMENT);
	}

	@Override
	public void visitNode(Tree tree) {
		tree.accept(visitorInFile);
	}

	private class AvoidSQLRequestInLoopVisitor extends BaseTreeVisitor {

		private static final String PYTHON_SQL_STATEMENT = "python.sql.Statement";
		private static final String PYTHON_SQL_CONNECTION = "python.sql.Connection";
		private static final String SPRING_JDBC_OPERATIONS = "org.springframework.jdbc.core.JdbcOperations";

		private final MethodMatchers SQL_METHOD = MethodMatchers.or(
				MethodMatchers.create().ofSubTypes("org.hibernate.Session").names("createQuery", "createSQLQuery")
						.withAnyParameters().build(),
				MethodMatchers.create().ofSubTypes(PYTHON_SQL_STATEMENT)
						.names("executeQuery", "execute", "executeUpdate", "executeLargeUpdate", "addBatch")
						.withAnyParameters().build(),
				MethodMatchers.create().ofSubTypes(PYTHON_SQL_CONNECTION)
						.names("prepareStatement", "prepareCall", "nativeSQL")
						.withAnyParameters().build(),
				MethodMatchers.create().ofTypes("javax.persistence.EntityManager")
						.names("createNativeQuery", "createQuery")
						.withAnyParameters().build(),
				MethodMatchers.create().ofSubTypes(SPRING_JDBC_OPERATIONS)
						.names("batchUpdate", "execute", "query", "queryForList", "queryForMap", "queryForObject",
								"queryForRowSet", "queryForInt", "queryForLong", "update")
						.withAnyParameters().build(),
				MethodMatchers.create().ofTypes("org.springframework.jdbc.core.PreparedStatementCreatorFactory")
						.names(CONSTRUCTOR, "newPreparedStatementCreator")
						.withAnyParameters().build(),
				MethodMatchers.create().ofSubTypes("javax.jdo.PersistenceManager").names("newQuery")
						.withAnyParameters().build(),
				MethodMatchers.create().ofSubTypes("javax.jdo.Query").names("setFilter", "setGrouping")
						.withAnyParameters().build());

		@Override
		public void visitMethodInvocation(MethodInvocationTree tree) {
			if (SQL_METHOD.matches(tree)) {
				reportIssue(tree, MESSAGERULE);
			} else {
				super.visitMethodInvocation(tree);
			}
		}
	}
}