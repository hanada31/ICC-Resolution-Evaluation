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

package eu.vranckaert.worktime.service.impl;

import android.content.Context;
import com.google.inject.Inject;
import eu.vranckaert.worktime.constants.TextConstants;
import eu.vranckaert.worktime.enums.Encoding;
import eu.vranckaert.worktime.enums.export.ExportCsvSeparator;
import eu.vranckaert.worktime.exceptions.export.GeneralExportException;
import eu.vranckaert.worktime.service.ExportService;
import eu.vranckaert.worktime.utils.context.Log;
import eu.vranckaert.worktime.utils.file.FileUtil;
import eu.vranckaert.worktime.utils.string.StringUtils;
import jxl.CellView;
import jxl.Workbook;
import jxl.WorkbookSettings;
import jxl.biff.DisplayFormat;
import jxl.format.Colour;
import jxl.write.Formula;
import jxl.write.Label;
import jxl.write.WritableCell;
import jxl.write.WritableCellFormat;
import jxl.write.WritableSheet;
import jxl.write.WritableWorkbook;
import jxl.write.WriteException;
import roboguice.inject.ContextSingleton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * @author Dirk Vranckaert
 *         Date: 5/11/11
 *         Time: 14:46
 */
public class ExportServiceImpl implements ExportService {
    private static final String LOG_TAG = ExportServiceImpl.class.getSimpleName();

    @Inject
    @ContextSingleton
    private Context ctx;

    @Override
    public File exportCsvFile(Context ctx, String filename, List<String> headers, List<String[]> values, ExportCsvSeparator separatorExport) throws GeneralExportException {
        Character separatorChar = separatorExport.getSeparator();
        String emptyValue = "\"\"";

        StringBuilder result = new StringBuilder();

        if (headers != null && headers.size() > 0) {
            for (String header : headers) {
                if (StringUtils.isNotBlank(header)) {
                    result.append("\"" + header + "\"");
                } else {
                    result.append(emptyValue);
                }
                result.append(separatorChar);
            }
            result.append(TextConstants.NEW_LINE);
        }

        for (String[] valuesRecord : values) {
            for (int i = 0; i < valuesRecord.length; i++) {
                String value = valuesRecord[i];
                if (StringUtils.isNotBlank(value)) {
                    result.append("\"" + value + "\"");
                } else {
                    result.append(emptyValue);
                }
                result.append(separatorChar);
            }

            result.append(TextConstants.NEW_LINE);
        }

        File file = getExportFile(ctx, filename, CSV_EXTENSTION);

        FileOutputStream fos = null;
        try {
            Encoding encoding = Encoding.UTF_8;
            byte[] textBytes = encoding.encodeString(result.toString());
            byte[] bom = encoding.getByteOrderMarker();

            fos = new FileOutputStream(file);
            fos.write(bom);
            fos.write(textBytes);
        } catch (FileNotFoundException e) {
            Log.e(ctx, LOG_TAG, "The file is not found", e);
            throw new GeneralExportException("The file is not found, probably a file-system issue...", e);
        } catch (IOException e) {
            Log.e(ctx, LOG_TAG, "Exception occurred during export...", e);
            throw new GeneralExportException("Exception occurred during export", e);
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(ctx, LOG_TAG, "Could not close the stream", e);
                }
            }
        }

        FileUtil.enableForMTP(ctx, file);

        return file;
    }

    @Override
    public File exportXlsFile(Context ctx, String filename, Map<String, List<Object>> headers, Map<String, List<Object[]>> values, Map<String, Map<Integer, DisplayFormat>> headersColumnFormat, Map<String, Map<Integer, DisplayFormat>> valuesColumnFormat, Map<String, List<Integer>> hiddenColumns, Map<String, List<Integer[]>> mergeCells, boolean autoSizeColumns) throws GeneralExportException {
        File file = getExportFile(ctx, filename, XLS_EXTENSTION);

        int sheetIndex = 0;
        WritableWorkbook workbook = null;

        WorkbookSettings settings = new WorkbookSettings();
        settings.setLocale(Locale.US);

        try {
            workbook = Workbook.createWorkbook(file, settings);
            Log.d(ctx, LOG_TAG, "Excel workbook created for file " + file.getAbsolutePath());
        } catch (IOException e) {
            String msg = "Something went wrong during the export";
            Log.e(ctx, LOG_TAG, msg, e);
            throw new GeneralExportException(msg + ": " + e.getMessage(), e);
        }

        for (Map.Entry<String, List<Object[]>> entry : values.entrySet()) {
            String sheetName = entry.getKey();
            List<Object[]> sheetValues = entry.getValue();

            Integer[] maximumColumnLengths = new Integer[getNumberOfColumns(sheetName, headers, sheetValues)];

            WritableSheet sheet = workbook.createSheet(sheetName, sheetIndex);
            Log.d(ctx, LOG_TAG, "Sheet with name " + sheetName + " created for workbook at index " + sheetIndex);
            sheetIndex++;

            // Get the maps of display formats for this sheet
            Map<Integer, DisplayFormat> headerDisplayFormats = headersColumnFormat !=null ? headersColumnFormat.get(sheetName) : new HashMap<Integer, DisplayFormat>();
            if (headerDisplayFormats == null)
                headerDisplayFormats = new HashMap<Integer, DisplayFormat>();
            Map<Integer, DisplayFormat> valuesDisplayFormats = valuesColumnFormat !=null ? valuesColumnFormat.get(sheetName) : new HashMap<Integer, DisplayFormat>();
            if (valuesDisplayFormats == null)
                valuesDisplayFormats = new HashMap<Integer, DisplayFormat>();
            // Get the hidden column numbers for this sheet
            List<Integer> hiddenColumnNumbers = hiddenColumns.get(sheetName);
            if (hiddenColumnNumbers == null) {
                hiddenColumnNumbers = new ArrayList<Integer>();
            }

            final int headerRow = 0;
            int firstDataRow = 1;

            if (headers == null || headers.get(sheetName) == null || headers.get(sheetName).size() == 0) {
                firstDataRow = headerRow;
                Log.d(ctx, LOG_TAG, "No headers information found so the headers will start at row " + firstDataRow);
            } else {
                Log.d(ctx, LOG_TAG, "Header information found, processing headers now...");
                List<Object> headerValues = headers.get(sheetName);

                for (int i = 0; i < headerValues.size(); i++) {
                    // Find the formatting for this header cell
                    DisplayFormat headerDisplayFormat = null;
                    if (headerDisplayFormats.containsKey(i)) {
                        headerDisplayFormat = headerDisplayFormats.get(i);
                    }

                    WritableCell headerCell = createExcelCell(i, headerRow, headerValues.get(i), headerDisplayFormat, ExportService.EXCEL_HEADER_COLOR, maximumColumnLengths);
                    if (headerCell != null) {
                        Log.d(ctx, LOG_TAG, "Writing content to header cell at column " + i + ", row " + headerRow + ".");
                        try {
                            sheet.addCell(headerCell);
                        } catch (WriteException e) {
                            Log.w(ctx, LOG_TAG, "For some reason the header cell for column " + i + " and row " + headerRow + " cannot be added", e);
                        }
                    } else {
                        Log.d(ctx, LOG_TAG, "No header data found to be displayed in cell with column " + i + " and row " + headerRow);
                    }
                }
                Log.d(ctx, LOG_TAG, "Header takes all place at row " + headerRow + ", data will start at row " + firstDataRow);
            }

            int row = firstDataRow;
            int highestColumnNumberInUse = 0;

            for (Object[] sheetRowValues : sheetValues) {
                int column = 0;
                for (Object cellValue : sheetRowValues) {
                    if (row > highestColumnNumberInUse)
                        highestColumnNumberInUse = row;

                    // Find the formatting for this value cell
                    DisplayFormat valueDisplayFormat = null;
                    if (valuesDisplayFormats.containsKey(column)) {
                        valueDisplayFormat = valuesDisplayFormats.get(column);
                    }

                    WritableCell cell = createExcelCell(column, row, cellValue, valueDisplayFormat, null, maximumColumnLengths);
                    if (cell != null) {
                        Log.d(ctx, LOG_TAG, "Writing data to Excel workbook at sheet " + sheetName + " in cell at column " + column + " and row " + row);
                        try {
                            sheet.addCell(cell);
                        } catch (WriteException e) {
                            Log.w(ctx, LOG_TAG, "For some reason the cell for column " + column + " and row " + row + " cannot be added", e);
                        }
                    } else {
                        Log.d(ctx, LOG_TAG, "No data found to be displayed in cell at column " + column + " and row " + row);
                    }
                    column++;
                }
                row++;
            }

            if (mergeCells != null) {
                List<Integer[]> mergeRanges = mergeCells.get(sheetName);
                if (mergeRanges != null) {
                    for (Integer[] mergeRange : mergeRanges) {
                        if (mergeRange != null && mergeRange.length == 4) {
                            try {
                                sheet.mergeCells(mergeRange[0], mergeRange[1], mergeRange[2], mergeRange[3]);
                            } catch (WriteException e) {
                                Log.w(ctx, LOG_TAG, "Cells cannot be merged!");
                            }
                        } else {
                            Log.w(ctx, LOG_TAG, "No or not enough data found for merging cells!");
                        }
                    }
                }
            }

            /*
             * issue 113:  Auto-size all columns in which we entered data on all the sheets we created so the cells match
             * their content
             */
            if (autoSizeColumns) {
                for (int sheetColumn = 0; sheetColumn < highestColumnNumberInUse; sheetColumn++) {
                    if (sheetColumn < maximumColumnLengths.length) {
                        Integer columnLength = maximumColumnLengths[sheetColumn];

                        CellView cellView = new CellView();
                        cellView.setSize(columnLength * 256); // Always multiply by 256, see the JXL documentation!

                        Log.d(ctx, LOG_TAG, "Resizing cells in column " + sheetColumn + " on sheet " + sheetName);
                        sheet.setColumnView(sheetColumn, cellView);
                    }
                }
            }

            // Hide all columns for which the column number has been defined
            for (Integer column : hiddenColumnNumbers) {
                CellView hiddenCellView = new CellView();
                hiddenCellView.setHidden(true);
                Log.d(ctx, LOG_TAG, "Hiding column " + column + " on sheet " + sheetName);
                sheet.setColumnView(column, hiddenCellView);
            }
        }

        Log.d(ctx, LOG_TAG, "Writing workbook to local storage at " + file.getAbsolutePath());
        try {
            workbook.write();
            workbook.close();
        } catch (IOException e) {
            String msg = "A general IO Exception occured!";
            Log.e(ctx, LOG_TAG, msg, e);
            throw new GeneralExportException(msg, e);
        } catch (WriteException e) {
            String msg = "Could not write the Excel file to disk!";
            Log.e(ctx, LOG_TAG, msg, e);
            throw new GeneralExportException(msg, e);
        }

        FileUtil.enableForMTP(ctx, file);

        return file;
    }

    private int getNumberOfColumns(String sheetName, Map<String, List<Object>> headers, List<Object[]> sheetValues) {
        int numberOfColumns = 0;
        for (Object[] sheetRowValues : sheetValues) {
            int cols = sheetRowValues.length;
            if (cols > numberOfColumns)
                numberOfColumns = cols;
        }
        if (headers != null && headers.get(sheetName) != null && headers.get(sheetName).size() != 0) {
            int colHeaders = headers.get(sheetName).size();
            if (colHeaders > numberOfColumns) {
                numberOfColumns = colHeaders;
            }
        }
        return numberOfColumns;
    }

    /**
     * Method to get, based on the filename and the extension, the actual {@link File} instance.
     *
     * @param ctx               The context from which this method is launched.
     * @param filename          The name of the file.
     * @param filenameExtension The extension of the file.
     * @return The {@link File} reference to which the content of the export can be written.
     * @throws GeneralExportException If something goes wrong while checking if the file (and file path) already exists
     *                                then this exception is thrown.
     */
    private File getExportFile(Context ctx, String filename, String filenameExtension) throws GeneralExportException {
        File exportDir = FileUtil.getExportDir(ctx);
        FileUtil.enableForMTP(ctx, exportDir);

        File file = new File(
                exportDir,
                filename + "." + filenameExtension
        );
        FileUtil.applyPermissions(file, true, true, false);

        try {
            boolean fileAlreadyExists = file.createNewFile();

            if (fileAlreadyExists) {
                file.delete();
                file.createNewFile();
            }
        } catch (IOException e) {
            Log.e(ctx, LOG_TAG, "Probably a file-system issue...", e);
            throw new GeneralExportException("Probably a file-system issue...", e);
        }

        return file;
    }

    /**
     * Creates an Excel cell for a certain column and row. This method dynamically checks what kind of data to be
     * entered in the cell and determines how to handle that data (as number, string, date,...). If a
     * {@link DisplayFormat} is provided it is applied as well as the colour of cell that is set when the
     * {@link Colour} is provided.
     *
     * @param c             The column (zero-based) to enter the data in. Required!
     * @param r             The row (zero-based) to enter the data in. Required!
     * @param value         The value to be entered in the cell. Required!
     * @param displayFormat This parameter is optional. If provided the cell will be formatted to display the data
     *                      correctly according to the {@link DisplayFormat} specified.
     * @param cellColor     This parameter is optional. If provided the background of the cell will formatted in the
     *                      specified {@link Colour}.
     * @param columnLengths The maximum length per column (in number of characters). This var needs to be updated every
     *                      time if a longer content is entered in the column.
     * @return An instance of {@link WritableCell} containing the data and the cell parameters (row, column). If
     *         provided it contains also the display format and the cell's background color.
     */
    private WritableCell createExcelCell(int c, int r, Object value, DisplayFormat displayFormat, Colour cellColor,
                                         Integer[] columnLengths) {
        int currentColumnLength = 0;
        WritableCell cell = null;

        if (value == null) {
            return null;
        }

        if (value instanceof java.lang.Double) {
            cell = new jxl.write.Number(c, r, (Double) value);
            currentColumnLength = ((Double) value).toString().length();
        } else if (value instanceof Integer) {
            Integer iValue = (Integer) value;
            cell = new jxl.write.Number(c, r, Double.valueOf(iValue.toString()));
            currentColumnLength = iValue.toString().length();
        } else if (value instanceof Boolean) {
            cell = new jxl.write.Boolean(c, r, (Boolean) value);
            currentColumnLength = 5;
        } else if (value instanceof Date) {
            cell = new jxl.write.DateTime(c, r, (java.util.Date) value);
            currentColumnLength = 10;
        } else if (value instanceof String && ((String) value).startsWith("=") && ((String) value).length() > 1) {
            // Now we know it should be a function
            String formula = (String) value;
            formula = formula.replace("[CR]", "" + (r+1));
            formula = formula.replace("[CC]", getExcelColumnName(c));
            Log.d(ctx, LOG_TAG, "Formula for cell with column " + c + " and row " + r + " is " + formula);
            formula = formula.substring(1);
            cell = new Formula(c, r, formula);
            currentColumnLength = 10;
        } else {
            // Default handling as String!
            cell = new Label(c, r, value.toString());
            currentColumnLength = value.toString().length() + 3;
        }

        if (displayFormat != null || cellColor != null) {
            WritableCellFormat cellFormat = null;
            if (displayFormat != null) {
                cellFormat = new WritableCellFormat(displayFormat);
            } else {
                cellFormat = new WritableCellFormat();
            }

            if (cellColor != null) {
                try {
                    cellFormat.setBackground(cellColor);
                } catch (WriteException e) {
                    Log.w(ctx, LOG_TAG, "Cannot change the background color of the cell at column " + c + " and row " + r, e);
                }
            }

            cell.setCellFormat(cellFormat);
        }

        Integer columnLength = columnLengths[c];
        if (columnLength == null || currentColumnLength > columnLength) {
            columnLengths[c] = currentColumnLength;
        }

        return cell;
    }

    public String getExcelColumnName (int columnNumber) {
        columnNumber++;
        int dividend = columnNumber;
        int i;
        String columnName = "";
        int modulo;
        while (dividend > 0)
        {
            modulo = (dividend - 1) % 26;
            i = 65 + modulo;
            columnName = Character.valueOf((char) i).toString() + columnName;
            dividend = (int)((dividend - modulo) / 26);
        }
        return columnName;
    }

    final byte[] HEX_CHAR_TABLE = {
            (byte) '0', (byte) '1', (byte) '2', (byte) '3',
            (byte) '4', (byte) '5', (byte) '6', (byte) '7',
            (byte) '8', (byte) '9', (byte) 'a', (byte) 'b',
            (byte) 'c', (byte) 'd', (byte) 'e', (byte) 'f'
    };

    public String getHexString(byte[] raw) throws UnsupportedEncodingException {
        byte[] hex = new byte[2 * raw.length];
        int index = 0;

        for (byte b : raw) {
            int v = b & 0xFF;
            hex[index++] = HEX_CHAR_TABLE[v >>> 4];
            hex[index++] = HEX_CHAR_TABLE[v & 0xF];
        }
        return new String(hex, "ASCII");
    }
}
