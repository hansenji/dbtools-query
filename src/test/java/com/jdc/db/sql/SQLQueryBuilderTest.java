package com.jdc.db.sql;

import org.dbtools.query.shared.QueryCompareType;
import org.dbtools.query.shared.QueryJoinType;
import org.dbtools.query.sql.SQLFilterItem;
import org.dbtools.query.sql.SQLQueryBuilder;
import org.junit.*;

import static org.junit.Assert.assertEquals;

/**
 * @author Jeff
 */
public class SQLQueryBuilderTest {

    public static final String C_LAST_NAME = "lastName";

    public SQLQueryBuilderTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test
    public void testBasicQuery() {
        // using default var
        SQLQueryBuilder qb1 = new SQLQueryBuilder();
        qb1.table("Person");
        String query1 = qb1.toString();

        assertEquals("SELECT * FROM Person", query1);
    }

    @Test
    public void testBasicFieldQuery() {
        SQLQueryBuilder sql = new SQLQueryBuilder();
        sql.table("Person");
        sql.field("LastName");
        String query1 = sql.toString();
        assertEquals("SELECT LastName FROM Person", query1);

        SQLQueryBuilder sql2 = new SQLQueryBuilder();
        sql2.table("Person");
        sql2.fields("LastName", "FirstName", "Age");
        assertEquals("SELECT LastName, FirstName, Age FROM Person", sql2.toString());
    }

    @Test
    public void testDistinct() {
        SQLQueryBuilder sql = new SQLQueryBuilder();
        sql.distinct(true);
        sql.table("Person");
        sql.field("LastName");
        assertEquals("SELECT DISTINCT LastName FROM Person", sql.toString());
    }

    @Test
    public void testMultiTable() {
        // using default var
        SQLQueryBuilder sql = new SQLQueryBuilder();
        sql.table("Person", "p");
        sql.table("Status", "s");
        sql.table("Category", "c");

        sql.field("Person.*");
        sql.field("s.name", "stat_name");
        sql.field("c.name", "cat_name");

        sql.filter("p.ID", 5);

        String query1 = sql.toString();

        assertEquals("SELECT Person.*, s.name AS stat_name, c.name AS cat_name FROM Person p, Status s, Category c WHERE p.ID = 5", query1);
    }

    @Test
    public void testJoins() {
        SQLQueryBuilder sql = new SQLQueryBuilder();
        sql.table("Car");
        sql.join("Colors", "Color.ID", "Car.COLOR_ID");
        sql.field("Name");
        sql.orderBy("Color.Name");

        assertEquals("SELECT Name FROM Car JOIN Colors ON Color.ID = Car.COLOR_ID ORDER BY Color.Name", sql.toString());
    }

    @Test
    public void testJoinsWithAnd() {
        SQLQueryBuilder sql = new SQLQueryBuilder();
        sql.table("Car");
        sql.join("Colors", new SQLFilterItem("Color.ID", "Car.COLOR_ID"), new SQLFilterItem("Color.COOL", "1"));
        sql.join("Make", "Car.MAKE_ID", "Make.ID");
        sql.field("Name");
        sql.orderBy("Color.Name");

        assertEquals("SELECT Name FROM Car JOIN Colors ON Color.ID = Car.COLOR_ID AND Color.COOL = 1 JOIN Make ON Car.MAKE_ID = Make.ID ORDER BY Color.Name", sql.toString());
    }

    @Test
    public void testMultiJoins() {
        SQLQueryBuilder sql = new SQLQueryBuilder();
        sql.table("Car");
        sql.join("Color", "Color.ID", "Car.COLOR_ID");
        sql.join(QueryJoinType.LEFT_JOIN, "Owner", "Owner.ID", "Car.OWNER_ID");
        sql.field("Name");
        sql.filter("Car.ID", 5);

        sql.orderBy("Color.Name");

        assertEquals("SELECT Name FROM Car JOIN Color ON Color.ID = Car.COLOR_ID LEFT JOIN Owner ON Owner.ID = Car.OWNER_ID WHERE Car.ID = 5 ORDER BY Color.Name", sql.toString());
    }

    @Test
    public void testQueryParam() {
        SQLQueryBuilder sql = new SQLQueryBuilder();
        sql.table("Car");
        sql.filter("Car.ID", "?");

        assertEquals("SELECT * FROM Car WHERE Car.ID = ?", sql.toString());
    }

    @Test
    public void testFilter() {
        SQLQueryBuilder sql = new SQLQueryBuilder();
        sql.table("Car");
        sql.filter("Car.ID", "?");
        sql.filter("Car.NAME", "Ford");
        sql.filter("Car.WHEELS", QueryCompareType.GREATERTHAN, 4);
        sql.filter("Car.IS_COOL", true);

        assertEquals("SELECT * FROM Car WHERE Car.ID = ? AND Car.NAME = 'Ford' AND Car.WHEELS > 4 AND Car.IS_COOL = 1", sql.toString());
    }

    @Test
    public void testCompareTypeNoneFilter() {
        SQLQueryBuilder sql = new SQLQueryBuilder();
        sql.table("Car");
        sql.filter("Car.ID = ? AND Car.NAME = 'FORD'");

        assertEquals("SELECT * FROM Car WHERE Car.ID = ? AND Car.NAME = 'FORD'", sql.toString());
    }

    @Test
    public void testOr() {
        SQLQueryBuilder sql = new SQLQueryBuilder();
        sql.table("Car");
        sql.filterToGroup("Car.ID", "?", 1);
        sql.filterToGroup("Car.NAME", "Ford", 1);
        sql.filterToGroup("Car.NAME", "Chevy", 1);
        sql.filterToGroup("Car.WHEELS", QueryCompareType.GREATERTHAN, 4, 2);
        sql.filterToGroup("Car.WHEELS", QueryCompareType.LESSTHAN_EQUAL, 2, 2);
        sql.filter("Car.IS_COOL", true);

        assertEquals("SELECT * FROM Car WHERE Car.IS_COOL = 1 AND (Car.ID = ? OR Car.NAME = 'Ford' OR Car.NAME = 'Chevy') AND (Car.WHEELS > 4 OR Car.WHEELS <= 2)", sql.toString());
    }

    @Test
    public void testOrderBy() {
        SQLQueryBuilder sql1 = new SQLQueryBuilder();
        sql1.table("Car");
        sql1.orderBy("Name");
        assertEquals("SELECT * FROM Car ORDER BY Name", sql1.toString());

        SQLQueryBuilder sql2 = new SQLQueryBuilder();
        sql2.table("Car");
        sql2.orderBy("Name", false);
        assertEquals("SELECT * FROM Car ORDER BY Name DESC", sql2.toString());

        SQLQueryBuilder sql3 = new SQLQueryBuilder();
        sql3.table("Car");
        sql3.orderBy("Name");
        sql3.orderBy("Color");
        assertEquals("SELECT * FROM Car ORDER BY Name, Color", sql3.toString());

        SQLQueryBuilder sql4 = new SQLQueryBuilder();
        sql4.table("Car");
        sql4.filter("WHEELS", 4);
        sql4.orderBy("Name");
        sql4.orderBy("Color");
        assertEquals("SELECT * FROM Car WHERE WHEELS = 4 ORDER BY Name, Color", sql4.toString());

        SQLQueryBuilder sql5 = new SQLQueryBuilder();
        sql5.table("Car");
        sql5.filter("WHEELS", 4);
        sql5.orderBy("Name", "Color");
        assertEquals("SELECT * FROM Car WHERE WHEELS = 4 ORDER BY Name, Color", sql5.toString());
    }

    @Test
    public void testGroupBy() {
        SQLQueryBuilder sql1 = new SQLQueryBuilder();
        sql1.table("Car");
        sql1.field("Name");
        sql1.field("Color");
        sql1.groupBy("Name");
        assertEquals("SELECT Name, Color FROM Car GROUP BY Name", sql1.toString());
    }

    @Test
    public void testApply() {
        // QUERY 1
        SQLQueryBuilder sql1 = new SQLQueryBuilder();
        sql1.table("Car");
        sql1.filter("Car.ID", "?");
        assertEquals("SELECT * FROM Car WHERE Car.ID = ?", sql1.toString());

        // QUERY 2
        SQLQueryBuilder sql2 = new SQLQueryBuilder();
        sql2.filter("Car.NAME", "Ford");
        sql2.filter("Car.WHEELS", QueryCompareType.GREATERTHAN, 4);
        sql2.filter("Car.IS_COOL", true);

        sql1.apply(sql2);
        assertEquals("SELECT * FROM Car WHERE Car.ID = ? AND Car.NAME = 'Ford' AND Car.WHEELS > 4 AND Car.IS_COOL = 1", sql1.toString());
    }

    @Test
    public void testIsNullQuery() {
        SQLQueryBuilder sql = new SQLQueryBuilder();
        sql.table("Person");
        sql.filter("id", QueryCompareType.IS_NULL);

        assertEquals("SELECT * FROM Person WHERE id IS NULL", sql.toString().trim());
    }

    @Test
    public void testNotNullQuery() {
        SQLQueryBuilder sql = new SQLQueryBuilder();
        sql.table("Person");
        sql.filter("id", QueryCompareType.NOT_NULL);

        assertEquals("SELECT * FROM Person WHERE id NOT NULL", sql.toString().trim());
    }

    @Test
    public void testSubSelectTableQuery() {
        SQLQueryBuilder subSql = new SQLQueryBuilder();
        subSql.field("id");
        subSql.table("Person");

        SQLQueryBuilder sql = new SQLQueryBuilder();
        sql.table(subSql);

        assertEquals("SELECT * FROM (SELECT id FROM Person)", sql.toString());
    }

    @Test
    public void testSubSelectInQuery() {
        SQLQueryBuilder subSql = new SQLQueryBuilder();
        subSql.field("id");
        subSql.table("Person");

        SQLQueryBuilder sql = new SQLQueryBuilder();
        sql.table("Family");
        sql.filter("HeadPerson", QueryCompareType.IN, subSql);

        assertEquals("SELECT * FROM Family WHERE HeadPerson IN (SELECT id FROM Person)", sql.toString());
    }

    @Test
    public void testUnionQuery() {
        SQLQueryBuilder sql1 = new SQLQueryBuilder();
        sql1.field("id");
        sql1.table("Person");

        SQLQueryBuilder sql2 = new SQLQueryBuilder();
        sql2.field("id");
        sql2.table("Family");

        assertEquals("(SELECT id FROM Person UNION SELECT id FROM Family)", SQLQueryBuilder.union(sql1, sql2));
    }

    @Test
    public void testUnionAllQuery() {
        SQLQueryBuilder sql1 = new SQLQueryBuilder();
        sql1.field("id");
        sql1.table("Person");

        SQLQueryBuilder sql2 = new SQLQueryBuilder();
        sql2.field("id");
        sql2.table("Family");

        assertEquals("(SELECT id FROM Person UNION ALL SELECT id FROM Family)", SQLQueryBuilder.unionAll(sql1, sql2));
    }

    @Test
    public void testComplexUnionQuery() {
        SQLQueryBuilder sql1 = new SQLQueryBuilder();
        sql1.field("id");
        sql1.table("Person");

        SQLQueryBuilder sql2 = new SQLQueryBuilder();
        sql2.field("id");
        sql2.table("Family");

        SQLQueryBuilder union = new SQLQueryBuilder();
        union.table(SQLQueryBuilder.union(sql1, sql2));

        assertEquals("SELECT * FROM (SELECT id FROM Person UNION SELECT id FROM Family)", union.toString());
    }


}