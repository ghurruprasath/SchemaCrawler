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

package schemacrawler.test.utility;


import static org.hamcrest.MatcherAssert.assertThat;
import static schemacrawler.test.utility.CommandlineTestUtility.commandlineExecution;
import static schemacrawler.test.utility.ExecutableTestUtility.executableExecution;
import static schemacrawler.test.utility.ExecutableTestUtility.hasSameContentAndTypeAs;
import static schemacrawler.test.utility.FileHasContent.classpathResource;
import static schemacrawler.test.utility.FileHasContent.hasSameContentAs;
import static schemacrawler.test.utility.FileHasContent.outputOf;
import static schemacrawler.test.utility.TestUtility.copyResourceToTempFile;
import static sf.util.Utility.isBlank;

import java.nio.file.Path;
import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.extension.ExtendWith;

import schemacrawler.schemacrawler.Config;
import schemacrawler.tools.executable.SchemaCrawlerExecutable;
import schemacrawler.tools.lint.executable.LintOptionsBuilder;
import schemacrawler.tools.options.OutputFormat;

@ExtendWith(TestAssertNoSystemErrOutput.class)
public abstract class BaseLintExecutableTest
{

  protected void executableLint(final Connection connection,
                                final String linterConfigsResource,
                                final Config additionalConfig,
                                final String referenceFileName)
    throws Exception
  {
    final SchemaCrawlerExecutable lintExecutable = new SchemaCrawlerExecutable("lint");
    if (!isBlank(linterConfigsResource))
    {
      final Path linterConfigsFile = copyResourceToTempFile(linterConfigsResource);
      final LintOptionsBuilder optionsBuilder = LintOptionsBuilder.builder();
      optionsBuilder.withLinterConfigs(linterConfigsFile.toString());

      final Config config = optionsBuilder.toConfig();
      if (additionalConfig != null)
      {
        config.putAll(additionalConfig);
      }
      lintExecutable.setAdditionalConfiguration(config);
    }

    assertThat(outputOf(executableExecution(connection, lintExecutable)),
               hasSameContentAs(classpathResource(referenceFileName + ".txt")));
  }

  protected void executeLintCommandLine(final DatabaseConnectionInfo connectionInfo,
                                        final OutputFormat outputFormat,
                                        final String linterConfigsResource,
                                        final Config additionalConfig,
                                        final String referenceFileName)
    throws Exception
  {
    final Map<String, String> argsMap = new HashMap<>();

    argsMap.put("infolevel", "standard");
    argsMap.put("sortcolumns", "true");

    if (!isBlank(linterConfigsResource))
    {
      final Path linterConfigsFile = copyResourceToTempFile(linterConfigsResource);
      argsMap.put("linterconfigs", linterConfigsFile.toString());
    }

    if (additionalConfig != null)
    {
      argsMap.putAll(additionalConfig);
    }

    assertThat(outputOf(commandlineExecution(connectionInfo,
                                             "lint",
                                             argsMap,
                                             additionalConfig,
                                             outputFormat)),
               hasSameContentAndTypeAs(classpathResource(referenceFileName
                                                         + ".txt"),
                                       outputFormat));
  }

}
