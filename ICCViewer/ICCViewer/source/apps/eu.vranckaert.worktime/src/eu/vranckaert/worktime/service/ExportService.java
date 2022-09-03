/*
 * Copyright 2012 Dirk Vranckaert
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package eu.vranckaert.worktime.service;

import android.content.Context;
import eu.vranckaert.worktime.enums.export.ExportCsvSeparator;
import eu.vranckaert.worktime.exceptions.export.GeneralExportException;
import jxl.biff.DisplayFormat;
import jxl.format.Colour;

import java.io.File;
import java.util.List;
import java.util.Map;

/**
 * @author Dirk Vranckaert
 *         Date: 5/11/11
 *         Time: 14:44
 */
public interface ExportService {
    /**
     * The extension to be used for CSV exports
     */
    public final String CSV_EXTENSTION = "csv";
    /**
     * The extension to be used for XLS exports
     */
    public final String XLS_EXTENSTION = "xls";
    /**
     * The color of the headers in an Excel export file
     */
    public final Colour EXCEL_HEADER_COLOR = Colour.RED;

    /**
     * Write some data to a CSV file. The exported data will be stored locally.
     *
     * @param ctx             The context.
     * @param filename        The name of the file <b>WITHOUT</b> the extension. Depending on the implementation the
     *                        extension will be automatically set. If you however specify an extension it will not be
     *                        overridden but the correct extension will just be added to the filename.
     * @param headers         A list of strings with the values to be shown in the headers.
     * @param values          A list with string-arrays containing all the values to be printed. No check is executed if
     *                        the number of values horizontally equals the number of headers you specified. This may be
     *                        different!
     * @param separatorExport The {@link eu.vranckaert.worktime.enums.export.ExportCsvSeparator} to be used in the file.
     *                        Comma is used for MAC/UNIX systems
     * @return The exported file.
     * @throws GeneralExportException This exception means that something went wrong during export but we don't know
     *                                exactly what. Most likely it's due to a file-system issue (SD-card not mounted or
     *                                not writable).
     */
    File exportCsvFile(Context ctx, String filename, List<String> headers, List<String[]> values, ExportCsvSeparator separatorExport) throws GeneralExportException;

    /**
     * Writes some specific data to an Excel workbook and store that file on the local file storage.
     *
     * @param ctx                 The context from which the call is launched.
     * @param filename            The name of the file (without extension) to be saved.
     * @param headers             A map containing a list of string. Different list are available (or can be) for each
     *                            sheet. If for some sheet no header-data is available the print-out of the data will
     *                            start on the first row. If there is header-data for the sheet the rest of the data
     *                            will start on the second row. The elements in the list represent once cell. The first
     *                            element found will be placed in cell with column 1, and row 1. The second element in
     *                            column 2 and row 1,...<br/>
     *                            If for a certain column on the sheet (for example column 3) a key is found in the
     *                            headersColumnFormat variable (the key for column 3 needs to be 3) the data in this
     *                            header cell will be formatted using the formatter found for this key in the
     *                            headersColumnFormat map.<br/>
     *                            The cell values are not defined as {@link String} but as {@link Object}. This is
     *                            because Excel supports multiple data types such as dates, times and numbers. When
     *                            running the Excel export a check is done on the type of the values specified. If a
     *                            recognized Excel data-format is found for a cell value it will be handled as that
     *                            data-format. Otherwise the {@link Object#toString()} will be called for the cell
     *                            value. Currently supported Excel data-format's are:<br/>
     *                            <ul>
     *                            <li>{@link java.lang.Integer}</li>
     *                            <li>{@link java.lang.Double}</li>
     *                            <li>{@link java.lang.Boolean}</li>
     *                            <li>{@link java.util.Date}</li>
     *                            <li>{@link java.lang.String} (default)</li>
     *                            </ul><br/>
     *                            To use formulas specify a {@link String} that starts with the equal sign (<b>=</b>).
     *                            The string should contain the exact same function-string as would be done in Excel.
     *                            Hard-reference to the columns is no problem (make sure you letters to reference the
     *                            columns!). Hard-references to the row are not-done (however it's possible but at your
     *                            own risk!). It's also possible to make the formula string dynamic and reference to the
     *                            current row and/or column using the tags <b>[CC]</b> for the <b>C</b>urrent
     *                            <b>C</b>olumn and <b>[CR]</b> for the <b>C</b>urrent <b>R</b>ow. Example: <i>we check
     *                            if a certain cell on the same row is empty:</i>
     *                            <b>=IF(F[CR]=\"\";\"Cell is emtpy\";\"Cell is not empty\")</b>.
     * @param values              A map containing a list of {@link Object} arrays. Just as for the header-data the map
     *                            contains a key with the name of each sheet (if headers are needed for this data you
     *                            need to make sure that the keys in the headers can be mapped (is the same) to the keys
     *                            in this values-map).<br/>
     *                            Per sheet a {@link List} is specified containing an {@link Object} array. The list
     *                            represents an entire row on the sheet. If an entirely empty row is needed just add an
     *                            empty {@Link Object} array or null at the position of the empty row. The first element
     *                            in the list represents row 2 (or 1 if no headers are defined for this sheet), the
     *                            second element represents row 3 (or 2 if no headers), the second element for row 4 (or
     *                            3 if no headers), ...<br/>
     *                            The {@link Object} array represents every cell of the sheet. The first element in the
     *                            cell will be placed in column 1, the second element in column 2, the third in
     *                            column 3,...<br/>
     *                            If for a certain column on the sheet (for example column 3) a key is found in the
     *                            valuesColumnFormat variable (the key for column 3 needs to be 3) all the data in this
     *                            column will be formatted using the formatter found for this key in the
     *                            valuesColumnFormat map.<br/>
     *                            The cell values are not defined as {@link String} but as {@link Object}. This is
     *                            because Excel supports multiple data types such as dates, times and numbers. When
     *                            running the Excel export a check is done on the type of the values specified. If a
     *                            recognized Excel data-format is found for a cell value it will be handled as that
     *                            data-format. Otherwise the {@link Object#toString()} will be called for the cell
     *                            value. Currently supported Excel data-format's are:<br/>
     *                            <ul>
     *                            <li>{@link java.lang.Integer}</li>
     *                            <li>{@link java.lang.Double}</li>
     *                            <li>{@link java.lang.Boolean}</li>
     *                            <li>{@link java.util.Date}</li>
     *                            <li>{@link java.lang.String} (default)</li>
     *                            </ul> <br/>
     *                            To use formulas specify a {@link String} that starts with the equal sign (<b>=</b>).
     *                            The string should contain the exact same function-string as would be done in Excel.
     *                            Hard-reference to the columns is no problem (make sure you letters to reference the
     *                            columns!). Hard-references to the row are not-done (however it's possible but at your
     *                            own risk!). It's also possible to make the formula string dynamic and reference to the
     *                            current row and/or column using the tags <b>[CC]</b> for the <b>C</b>urrent
     *                            <b>C</b>olumn and <b>[CR]</b> for the <b>C</b>urrent <b>R</b>ow. Example: <i>we check
     *                            if a certain cell on the same row is empty:</i>
     *                            <b>=IF(F[CR]=\"\";\"Cell is emtpy\";\"Cell is not empty\")</b>.
     * @param headersColumnFormat Per sheet (key of the root map), per column (key of the second map, columns are
     *                            zero-based) it's possible to define how the data should be formatted. The formatter
     *                            used here needs to extend {@link DisplayFormat} such as {@link jxl.write.DateFormat}
     *                            or {@link jxl.write.NumberFormat}. These will only be applied on the cells created
     *                            using the headers-map.
     * @param valuesColumnFormat  Per sheet (key of the root map), per column (key of the second map, columns are
     *                            zero-based) it's possible to define how the data should be formatted. The formatter
     *                            used here needs to extend {@link DisplayFormat} such as {@link jxl.write.DateFormat}
     *                            or {@link jxl.write.NumberFormat}. These will only be applied on the cells created
     *                            using the headers-map.
     * @param hiddenColumns       A list of columns ({@Link java.lang.Integer} values, zero-based) can be specified per
     *                            sheet (the key in the map, should match the sheet-names defined in the values-map) for
     *                            columns that should be hidden on that sheet. <b>Caution: this does not hide the value
     *                            in a single cell but the entire column! Also the header!</b>
     * @param mergeCells          This map contains a key for the sheet and allows for every sheet you create to merge
     *                            certain cells together. The value of the map, being an list with {@link Integer}
     *                            arrays, <b>always</b> needs to contain <b>4 values</b>:<br/>
     *                            1: The first value being the left upper boundary column of the merge<br/>
     *                            2: The second value is the left upper boundary row of the merge<br/>
     *                            3: The third value is the right lower boundary column of the merge<br/>
     *                            4: The fourth value is the right lower boundary row of the merge
     * @param autoSizeColumns     If {@link Boolean#TRUE} auto-resizing will be applied on all the columns. If
     *                            {@link Boolean#FALSE} the cells will have default width and heights.
     * @return The exported file.
     * @throws GeneralExportException This exception means that something went wrong during export but we don't know
     *                                exactly what. Most likely it's due to a file-system issue (SD-card not mounted or
     *                                not writable).
     */
    File exportXlsFile(Context ctx, String filename, Map<String, List<Object>> headers, Map<String, List<Object[]>> values, Map<String, Map<Integer, DisplayFormat>> headersColumnFormat, Map<String, Map<Integer, DisplayFormat>> valuesColumnFormat, Map<String, List<Integer>> hiddenColumns, Map<String, List<Integer[]>> mergeCells,  boolean autoSizeColumns) throws GeneralExportException;

}
