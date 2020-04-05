/*
========================================================================
SchemaCrawler
http://www.schemacrawler.com
Copyright (c) 2000-2020, Sualeh Fatehi <sualeh@hotmail.com>.
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

package schemacrawler.tools.text.schema;


import static schemacrawler.analysis.counts.TableRowCountsUtility.getRowCountMessage;
import static schemacrawler.analysis.counts.TableRowCountsUtility.hasRowCount;
import static schemacrawler.utility.MetaDataUtility.findForeignKeyCardinality;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import schemacrawler.crawl.NotLoadedException;
import schemacrawler.schema.BaseForeignKey;
import schemacrawler.schema.Column;
import schemacrawler.schema.ColumnDataType;
import schemacrawler.schema.ColumnReference;
import schemacrawler.schema.ForeignKeyColumnReference;
import schemacrawler.schema.Routine;
import schemacrawler.schema.Sequence;
import schemacrawler.schema.Synonym;
import schemacrawler.schema.Table;
import schemacrawler.schemacrawler.SchemaCrawlerException;
import schemacrawler.analysis.associations.WeakAssociationForeignKey;
import schemacrawler.analysis.associations.WeakAssociationsUtility;
import schemacrawler.tools.integration.diagram.DiagramOptions;
import schemacrawler.tools.options.OutputOptions;
import schemacrawler.tools.options.TextOutputFormat;
import schemacrawler.tools.text.base.BaseDotFormatter;
import schemacrawler.tools.text.utility.html.Alignment;
import schemacrawler.tools.text.utility.html.TableRow;
import schemacrawler.tools.traversal.SchemaTraversalHandler;
import schemacrawler.utility.MetaDataUtility.ForeignKeyCardinality;
import schemacrawler.utility.NamedObjectSort;
import sf.util.Color;

/**
 * Graphviz DOT formatting of schema.
 *
 * @author Sualeh Fatehi
 */
public final class SchemaDotFormatter
  extends BaseDotFormatter
  implements SchemaTraversalHandler
{

  private final boolean isVerbose;
  private final boolean isBrief;

  /**
   * Text formatting of schema.
   *
   * @param schemaTextDetailType
   *   Types for text formatting of schema
   * @param options
   *   Options for text formatting of schema
   * @param outputOptions
   *   Options for text formatting of schema
   * @param identifierQuoteString
   *   Quote character for database objects
   * @throws SchemaCrawlerException
   *   On an exception
   */
  public SchemaDotFormatter(final SchemaTextDetailType schemaTextDetailType,
                            final DiagramOptions options,
                            final OutputOptions outputOptions,
                            final String identifierQuoteString)
    throws SchemaCrawlerException
  {
    super(options,
          schemaTextDetailType == SchemaTextDetailType.details,
          outputOptions,
          identifierQuoteString);
    isVerbose = schemaTextDetailType == SchemaTextDetailType.details;
    isBrief = schemaTextDetailType == SchemaTextDetailType.brief;
  }

  @Override
  public void handle(final ColumnDataType columnDataType)
    throws SchemaCrawlerException
  {
    // No output required
  }

  /**
   * Provides information on the database schema.
   *
   * @param routine
   *   Routine metadata.
   */
  @Override
  public void handle(final Routine routine)
  {
    // No output required
  }

  /**
   * Provides information on the database schema.
   *
   * @param sequence
   *   Sequence metadata.
   */
  @Override
  public void handle(final Sequence sequence)
  {
    // No output required
  }

  /**
   * Provides information on the database schema.
   *
   * @param synonym
   *   Synonym metadata.
   */
  @Override
  public void handle(final Synonym synonym)
  {
    // No output required
  }

  @Override
  public void handle(final Table table)
  {

    final String tableName;
    if (options.isShowUnqualifiedNames())
    {
      tableName = identifiers.quoteName(table);
    }
    else
    {
      tableName = identifiers.quoteFullName(table);
    }
    final String tableType = "[" + table.getTableType() + "]";

    final Color tableNameBgColor = colorMap.getColor(table);
    final int colspan = options.isShowOrdinalNumbers()? 3: 2;

    formattingHelper
      .append("  /* ")
      .append(table.getFullName())
      .append(" -=-=-=-=-=-=-=-=-=-=-=-=-=- */")
      .println();
    formattingHelper
      .append("  \"")
      .append(nodeId(table))
      .append("\" [")
      .println();
    formattingHelper
      .append("    label=<")
      .println();
    formattingHelper
      .append(
        "      <table border=\"1\" cellborder=\"0\" cellspacing=\"0\" color=\"#888888\">")
      .println();

    formattingHelper
      .append(new TableRow(TextOutputFormat.html)
                .add(newTableCell(tableName,
                                  Alignment.left,
                                  true,
                                  tableNameBgColor,
                                  colspan))
                .add(newTableCell(tableType,
                                  Alignment.right,
                                  false,
                                  tableNameBgColor,
                                  1))
                .toString())
      .println();

    printTableRemarks(table);

    printTableColumns(table.getColumns());
    if (isVerbose)
    {
      printTableColumns(new ArrayList<>(table.getHiddenColumns()));
    }

    printTableRowCount(table);

    formattingHelper
      .append("      </table>")
      .println();
    formattingHelper
      .append("    >")
      .println();
    formattingHelper
      .append("  ];")
      .println();
    formattingHelper.println();

    printForeignKeys(table);
    printWeakAssociations(table);

    formattingHelper.println();
    formattingHelper.println();
  }

  @Override
  public void handleColumnDataTypesEnd()
  {
    // No output required
  }

  @Override
  public void handleColumnDataTypesStart()
  {
    // No output required
  }

  @Override
  public void handleRoutinesEnd()
    throws SchemaCrawlerException
  {
    // No output required
  }

  @Override
  public void handleRoutinesStart()
    throws SchemaCrawlerException
  {
    // No output required
  }

  @Override
  public void handleSequencesEnd()
    throws SchemaCrawlerException
  {
    // No output required
  }

  @Override
  public void handleSequencesStart()
    throws SchemaCrawlerException
  {
    // No output required
  }

  @Override
  public void handleSynonymsEnd()
    throws SchemaCrawlerException
  {
    // No output required
  }

  @Override
  public void handleSynonymsStart()
    throws SchemaCrawlerException
  {
    // No output required
  }

  @Override
  public void handleTablesEnd()
    throws SchemaCrawlerException
  {
    // No output required
  }

  @Override
  public void handleTablesStart()
    throws SchemaCrawlerException
  {
    // No output required
  }

  private String arrowhead(final ForeignKeyCardinality connectivity)
  {
    switch (connectivity)
    {
      case unknown:
        return "box";
      case zero_one:
        return "teeodot";
      case zero_many:
        return "crowodot";
      case one_one:
        return "teetee";
      default:
        return "box";
    }
  }

  private String[] getPortIds(final Column column, final boolean isNewNode)
  {
    final String[] portIds = new String[2];

    if (!isNewNode)
    {
      portIds[0] = String.format("\"%s\":\"%s.start\"",
                                 nodeId(column.getParent()),
                                 nodeId(column));
      portIds[1] = String.format("\"%s\":\"%s.end\"",
                                 nodeId(column.getParent()),
                                 nodeId(column));
    }
    else
    {
      // Create new node
      final String nodeId = printNewNode(column);
      //
      portIds[0] = nodeId;
      portIds[1] = nodeId;
    }
    return portIds;
  }

  private String printColumnReference(final String fkName,
                                      final ColumnReference columnRef,
                                      final ForeignKeyCardinality fkCardinality,
                                      final boolean isFkColumnFiltered)
  {
    final boolean isForeignKey = columnRef instanceof ForeignKeyColumnReference;

    final Column primaryKeyColumn = columnRef.getPrimaryKeyColumn();
    final Column foreignKeyColumn = columnRef.getForeignKeyColumn();

    final String[] pkPortIds = getPortIds(primaryKeyColumn, false);
    final String[] fkPortIds = getPortIds(foreignKeyColumn, isFkColumnFiltered);

    final DiagramOptions diagramOptions = options;
    final String pkSymbol;
    if (diagramOptions.isShowPrimaryKeyCardinality())
    {
      pkSymbol = "teetee";
    }
    else
    {
      pkSymbol = "none";
    }

    final String fkSymbol;
    if (diagramOptions.isShowForeignKeyCardinality())
    {
      fkSymbol = arrowhead(fkCardinality);
    }
    else
    {
      fkSymbol = "none";
    }

    final String style;
    if (isForeignKey)
    {
      style = "solid";
    }
    else
    {
      style = "dashed";
    }

    final String associationName;
    if (options.isHideForeignKeyNames() || !isForeignKey)
    {
      associationName = "";
    }
    else
    {
      associationName = fkName;
    }

    return String.format(
      "  %s:w -> %s:e [label=<%s> style=\"%s\" dir=\"both\" arrowhead=\"%s\" arrowtail=\"%s\"];%n",
      fkPortIds[0],
      pkPortIds[1],
      associationName,
      style,
      pkSymbol,
      fkSymbol);

  }

  private void printForeignKeys(final Table table)
  {
    printForeignKeys(table, table.getForeignKeys());
  }

  private void printForeignKeys(final Table table,
                                final Collection<? extends BaseForeignKey<?>> foreignKeys)
  {
    for (final BaseForeignKey<? extends ColumnReference> foreignKey : foreignKeys)
    {
      final ForeignKeyCardinality fkCardinality =
        findForeignKeyCardinality(foreignKey);
      for (final ColumnReference columnRef : foreignKey)
      {
        final Table referencedTable = columnRef
          .getForeignKeyColumn()
          .getParent();
        final boolean isForeignKeyFiltered = referencedTable.getAttribute(
          "schemacrawler.table.no_grep_match",
          false);
        if (isForeignKeyFiltered)
        {
          continue;
        }
        final boolean isFkColumnFiltered = referencedTable.getAttribute(
          "schemacrawler.table.filtered_out",
          false);
        if (table.equals(columnRef
                           .getPrimaryKeyColumn()
                           .getParent()))
        {
          formattingHelper.append(printColumnReference(identifiers.quoteName(
            foreignKey.getName()),
                                                       columnRef,
                                                       fkCardinality,
                                                       isFkColumnFiltered));
        }
      }
    }
  }

  private String printNewNode(final Column column)
  {
    final String nodeId = "\"" + nodeId(column) + "\"";
    final String columnName;
    if (options.isShowUnqualifiedNames())
    {
      columnName = identifiers.quoteShortName(column);
    }
    else
    {
      columnName = identifiers.quoteFullName(column);
    }
    final String columnNode =
      String.format("  %s [label=<%s>];%n", nodeId, columnName);

    formattingHelper.append(columnNode);

    return nodeId;
  }

  private void printTableColumnAutoIncremented(final Column column)
  {
    if (column == null)
    {
      return;
    }
    try
    {
      if (!column.isAutoIncremented())
      {
        return;
      }
    }
    catch (final NotLoadedException e)
    {
      // The column may be partial for index pseudo-columns
      return;
    }

    final TableRow row = new TableRow(TextOutputFormat.html);
    if (options.isShowOrdinalNumbers())
    {
      row.add(newTableCell("", Alignment.right, false, Color.white, 1));
    }
    row
      .add(newTableCell("", Alignment.left, false, Color.white, 1))
      .add(newTableCell(" ", Alignment.left, false, Color.white, 1))
      .add(newTableCell("auto-incremented",
                        Alignment.left,
                        false,
                        Color.white,
                        1));
    formattingHelper
      .append(row.toString())
      .println();
  }

  private void printTableColumnGenerated(final Column column)
  {
    if (column == null)
    {
      return;
    }
    try
    {
      if (!column.isGenerated())
      {
        return;
      }
    }
    catch (final NotLoadedException e)
    {
      // The column may be partial for index pseudo-columns
      return;
    }

    final TableRow row = new TableRow(TextOutputFormat.html);
    if (options.isShowOrdinalNumbers())
    {
      row.add(newTableCell("", Alignment.right, false, Color.white, 1));
    }
    row
      .add(newTableCell("", Alignment.left, false, Color.white, 1))
      .add(newTableCell(" ", Alignment.left, false, Color.white, 1))
      .add(newTableCell("generated", Alignment.left, false, Color.white, 1));
    formattingHelper
      .append(row.toString())
      .println();
  }

  private void printTableColumnEnumValues(final Column column)
  {
    if (column == null)
    {
      return;
    }
    try
    {
      if (!column
        .getColumnDataType()
        .isEnumerated())
      {
        return;
      }
    }
    catch (final NotLoadedException e)
    {
      // The column may be partial for index pseudo-columns
      return;
    }

    final String enumValues = String.format("'%s'",
                                            String.join("', ",
                                                        column
                                                          .getColumnDataType()
                                                          .getEnumValues()));

    final TableRow row = new TableRow(TextOutputFormat.html);
    if (options.isShowOrdinalNumbers())
    {
      row.add(newTableCell("", Alignment.right, false, Color.white, 1));
    }
    row
      .add(newTableCell("", Alignment.left, false, Color.white, 1))
      .add(newTableCell(" ", Alignment.left, false, Color.white, 1))
      .add(newTableCell(enumValues,
                        Alignment.left,
                        false,
                        Color.white,
                        1));
    formattingHelper
      .append(row.toString())
      .println();
  }

  private void printTableColumnHidden(final Column column)
  {
    if (column == null)
    {
      return;
    }
    try
    {
      if (!column.isHidden())
      {
        return;
      }
    }
    catch (final NotLoadedException e)
    {
      // The column may be partial for index pseudo-columns
      return;
    }

    final TableRow row = new TableRow(TextOutputFormat.html);
    if (options.isShowOrdinalNumbers())
    {
      row.add(newTableCell("", Alignment.right, false, Color.white, 1));
    }
    row
      .add(newTableCell("", Alignment.left, false, Color.white, 1))
      .add(newTableCell(" ", Alignment.left, false, Color.white, 1))
      .add(newTableCell("hidden", Alignment.left, false, Color.white, 1));
    formattingHelper
      .append(row.toString())
      .println();
  }

  private void printTableColumnRemarks(final Column column)
  {
    if (column == null || !column.hasRemarks() || options.isHideRemarks())
    {
      return;
    }
    final TableRow remarksRow = new TableRow(TextOutputFormat.html);
    if (options.isShowOrdinalNumbers())
    {
      remarksRow.add(newTableCell("", Alignment.right, false, Color.white, 1));
    }
    remarksRow
      .add(newTableCell("", Alignment.left, false, Color.white, 1))
      .add(newTableCell(" ", Alignment.left, false, Color.white, 1))
      .add(newTableCell(column.getRemarks(),
                        Alignment.left,
                        false,
                        Color.white,
                        1));
    formattingHelper
      .append(remarksRow.toString())
      .println();
  }

  private void printTableColumns(final List<Column> columns)
  {
    if (columns.isEmpty())
    {
      return;
    }

    Collections.sort(columns,
                     NamedObjectSort.getNamedObjectSort(options.isAlphabeticalSortForTableColumns()));

    for (final Column column : columns)
    {
      if (isBrief && !isColumnSignificant(column))
      {
        continue;
      }

      final String columnTypeName;
      if (options.isShowStandardColumnTypeNames())
      {
        columnTypeName = column
          .getColumnDataType()
          .getJavaSqlType()
          .getName();
      }
      else
      {
        columnTypeName = column
          .getColumnDataType()
          .getDatabaseSpecificTypeName();
      }
      final String columnType = columnTypeName + column.getWidth();
      final String nullable =
        columnNullable(columnTypeName, column.isNullable());
      final String columnDetails = columnType + nullable;
      final boolean emphasize = column.isPartOfPrimaryKey();

      final TableRow row = new TableRow(TextOutputFormat.html);
      if (options.isShowOrdinalNumbers())
      {
        final String ordinalNumberString =
          String.valueOf(column.getOrdinalPosition());
        row.add(newTableCell(ordinalNumberString,
                             Alignment.right,
                             false,
                             Color.white,
                             1));
      }
      row
        .add(newTableCell(identifiers.quoteName(column.getName()),
                          Alignment.left,
                          emphasize,
                          Color.white,
                          1))
        .add(newTableCell(" ", Alignment.left, false, Color.white, 1))
        .add(newTableCell(columnDetails,
                          Alignment.left,
                          false,
                          Color.white,
                          1));

      row
        .firstCell()
        .addAttribute("port", nodeId(column) + ".start");
      row
        .lastCell()
        .addAttribute("port", nodeId(column) + ".end");
      formattingHelper
        .append(row.toString())
        .println();

      printTableColumnEnumValues(column);
      printTableColumnHidden(column);
      printTableColumnAutoIncremented(column);
      printTableColumnGenerated(column);
      printTableColumnRemarks(column);
    }
  }

  private void printTableRemarks(final Table table)
  {
    if (table == null || !table.hasRemarks() || options.isHideRemarks())
    {
      return;
    }
    formattingHelper
      .append(new TableRow(TextOutputFormat.html)
                .add(newTableCell(table.getRemarks(),
                                  Alignment.left,
                                  false,
                                  Color.white,
                                  3))
                .toString())
      .println();
  }

  private void printTableRowCount(final Table table)
  {
    if (!options.isShowRowCounts() || table == null || !hasRowCount(table))
    {
      return;
    }
    formattingHelper
      .append(new TableRow(TextOutputFormat.html)
                .add(newTableCell(getRowCountMessage(table),
                                  Alignment.right,
                                  false,
                                  Color.white,
                                  3))
                .toString())
      .println();
  }

  private void printWeakAssociations(final Table table)
  {
    if (!options.isShowWeakAssociations())
    {
      return;
    }

    final Collection<WeakAssociationForeignKey> weakFks =
      WeakAssociationsUtility.getWeakAssociations(table);
    printForeignKeys(table, weakFks);
  }

}