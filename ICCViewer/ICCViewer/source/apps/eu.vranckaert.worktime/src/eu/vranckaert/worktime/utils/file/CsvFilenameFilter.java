package eu.vranckaert.worktime.utils.file;

import eu.vranckaert.worktime.service.ExportService;

import java.io.File;
import java.io.FilenameFilter;

/**
 * File CSV files
 * @author Dirk Vranckaert
 * Date: 23/11/11
 * Time: 14:14
 */
public class CsvFilenameFilter implements FilenameFilter {
    @Override
    public boolean accept(File dir, String name) {
        String lcName = name.toLowerCase();
        if (lcName.endsWith(ExportService.CSV_EXTENSTION)) {
            return true;
        }

        return false;
    }
}
