package com.tanyou.toolservice.util;

import java.util.ArrayList;
import java.util.List;

public enum TanyoudbboxReportFilenameEnum {
    cnnvd_no("cnnvd_no_sql.txt"),

    name("name_sql.txt"),

    severity("severity_sql.txt"),
    vul_type("vul_type_sql.txt"),

    source("source_sql.txt"),

    description("description_sql.txt"),
    date_exposure("date_exposure_sql.txt"),
    date_created("date_created_sql.txt"),
    fanchaSql("CNNVD反查CVE的SQL.txt"),
    cveError("CVE_ERROR.txt"),
    deleteRowSql("delete_row_sql.txt"),
    cveNotFound("cve_notfound.txt"),
    cveMultidata("cve_multidata.txt");

    public static List<String> getAllFilename() {
        List<String> returnList = new ArrayList<>();
        for (TanyoudbboxReportFilenameEnum tanyoudbboxReportFilenameEnum :values()) {
            returnList.add(tanyoudbboxReportFilenameEnum.fileName);
        }
        return returnList;
    }

    private String fileName;

    TanyoudbboxReportFilenameEnum(String fileName) {
        this.fileName = fileName;
    }

}
