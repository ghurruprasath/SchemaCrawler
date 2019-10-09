/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2019, Sualeh Fatehi <sualeh@hotmail.com>.
All rights reserved.
------------------------------------------------------------------------

SchemaCrawler is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.

SchemaCrawler and the accompanying materials are made available under
the terms of the Eclipse Public License v1.0, GNU General Public License
v3 or GNU Lesser General Public License v3.

You may elect to redistribute this code under any of these licenses.

The Eclipse Public License is available at:
http://www.eclipse.org/legal/epl-v10.html

The GNU General Public License v3 and the GNU Lesser General Public
License v3 are available at:
http://www.gnu.org/licenses/

========================================================================
*/
package schemacrawler.integration.test;


import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;
import static schemacrawler.test.utility.ExecutableTestUtility.executableExecution;
import static schemacrawler.test.utility.FileHasContent.*;
import static sf.util.DatabaseUtility.checkConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.junit.Ignore;
import org.junit.jupiter.api.*;
import schemacrawler.crawl.SchemaCrawler;
import schemacrawler.schema.Catalog;
import schemacrawler.schema.Property;
import schemacrawler.schemacrawler.*;
import schemacrawler.server.db2.DB2DatabaseConnector;
import schemacrawler.test.utility.BaseAdditionalDatabaseTest;
import schemacrawler.test.utility.DatabaseServerContainer;
import schemacrawler.tools.databaseconnector.DatabaseConnector;
import schemacrawler.tools.executable.SchemaCrawlerExecutable;
import schemacrawler.tools.text.schema.SchemaTextOptions;
import schemacrawler.tools.text.schema.SchemaTextOptionsBuilder;


public class DB2Test
  extends BaseAdditionalDatabaseTest
{

  private DatabaseServerContainer databaseServer;

  @BeforeEach
  public void createDatabase()
    throws SQLException, SchemaCrawlerException
  {
    databaseServer = new DB2DatabaseServerContainer();
    databaseServer.startServer();

    createDataSource(databaseServer);
    createDatabase("/db2.scripts.txt");
  }

  @AfterEach
  public void stopDatabaseServer()
  {
    databaseServer.stopServer();
  }

  @Disabled("TODO: Locate database identifier for DB2")
  @Test
  public void testDB2Catalog()
    throws Exception
  {
    final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = SchemaCrawlerOptionsBuilder
      .builder();
    schemaCrawlerOptionsBuilder
      .withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum())
      .includeSchemas(new RegularExpressionInclusionRule("DB2INST1"))
      .includeAllSequences().includeAllSynonyms().includeAllRoutines()
      .tableTypes("TABLE,VIEW,MATERIALIZED VIEW");
    final SchemaCrawlerOptions options = schemaCrawlerOptionsBuilder
      .toOptions();

    final Connection connection = checkConnection(getConnection());
    final DatabaseConnector db2DatabaseConnector = new DB2DatabaseConnector();

    final SchemaRetrievalOptions schemaRetrievalOptions = db2DatabaseConnector
      .getSchemaRetrievalOptionsBuilder(connection).toOptions();

    final SchemaCrawler schemaCrawler = new SchemaCrawler(getConnection(),
                                                          schemaRetrievalOptions,
                                                          options);
    final Catalog catalog = schemaCrawler.crawl();
    final List<Property> serverInfo = new ArrayList<>(catalog.getDatabaseInfo()
                                                        .getServerInfo());

    assertThat(serverInfo.size(), equalTo(1));
    assertThat(serverInfo.get(0).getName(), equalTo("current_database"));
    assertThat(serverInfo.get(0).getValue(), equalTo("test"));
  }

  @Test
  public void testDB2WithConnection()
    throws Exception
  {
    final SchemaCrawlerOptionsBuilder schemaCrawlerOptionsBuilder = SchemaCrawlerOptionsBuilder
      .builder();
    schemaCrawlerOptionsBuilder
      .withSchemaInfoLevel(SchemaInfoLevelBuilder.maximum())
      .includeSchemas(new RegularExpressionInclusionRule("DB2INST1"))
      .includeAllSequences().includeAllSynonyms()
      .includeRoutines(new RegularExpressionInclusionRule("[0-9a-zA-Z_\\.]*"))
      .tableTypes("TABLE,VIEW,MATERIALIZED VIEW");
    final SchemaCrawlerOptions options = schemaCrawlerOptionsBuilder
      .toOptions();

    final SchemaTextOptionsBuilder textOptionsBuilder = SchemaTextOptionsBuilder
      .builder().portableNames();
    textOptionsBuilder.showDatabaseInfo().showJdbcDriverInfo();
    final SchemaTextOptions textOptions = textOptionsBuilder.toOptions();

    final SchemaCrawlerExecutable executable = new SchemaCrawlerExecutable(
      "details");
    executable.setSchemaCrawlerOptions(options);
    executable
      .setAdditionalConfiguration(SchemaTextOptionsBuilder.builder(textOptions)
                                    .toConfig());

    assertThat(outputOf(executableExecution(getConnection(), executable)),
               hasSameContentAs(classpathResource(
                 "testDB2WithConnection.txt")));
  }

}