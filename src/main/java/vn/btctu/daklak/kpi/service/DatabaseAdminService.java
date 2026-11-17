package vn.btctu.daklak.kpi.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.DatabaseMetaData;
import java.util.*;

@Service
public class DatabaseAdminService {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    public Map<String, Object> getDatabaseOverview() {
        Map<String, Object> overview = new HashMap<>();
        try {
            jdbcTemplate.execute((java.sql.Connection conn) -> {
                DatabaseMetaData meta = conn.getMetaData();
                overview.put("productName", meta.getDatabaseProductName());
                overview.put("productVersion", meta.getDatabaseProductVersion());
                overview.put("driverName", meta.getDriverName());
                overview.put("url", meta.getURL());
                overview.put("userName", meta.getUserName());
                return null;
            });

            List<Map<String, Object>> tables = getTablesInfo();
            overview.put("tables", tables);
            overview.put("totalTables", tables.size());

            long totalRows = 0;
            for (Map<String, Object> t : tables) {
                Object cnt = t.get("rowCount");
                if (cnt instanceof Number) {
                    totalRows += ((Number) cnt).longValue();
                }
            }
            overview.put("totalRows", totalRows);
            overview.put("status", "HEALTHY");
            overview.put("engineMode", "IN-MEMORY H2 (PostgreSQL Compatible)");
        } catch (Exception e) {
            overview.put("error", e.getMessage());
            overview.put("status", "WARNING");
        }
        return overview;
    }

    public List<Map<String, Object>> getTablesInfo() {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String sql = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES " +
                         "WHERE TABLE_SCHEMA = 'PUBLIC' AND TABLE_TYPE = 'BASE TABLE' " +
                         "ORDER BY TABLE_NAME";
            List<String> tableNames = jdbcTemplate.queryForList(sql, String.class);

            for (String tableName : tableNames) {
                Map<String, Object> tInfo = new HashMap<>();
                tInfo.put("name", tableName);
                try {
                    Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + tableName, Long.class);
                    tInfo.put("rowCount", count != null ? count : 0);
                } catch (Exception ex) {
                    tInfo.put("rowCount", 0);
                }
                result.add(tInfo);
            }
        } catch (Exception e) {
            // ignore
        }
        return result;
    }

    public List<Map<String, Object>> getTableColumns(String tableName) {
        List<Map<String, Object>> columns = new ArrayList<>();
        try {
            String sql = "SELECT COLUMN_NAME, DATA_TYPE AS TYPE_NAME, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE, COLUMN_DEFAULT " +
                         "FROM INFORMATION_SCHEMA.COLUMNS " +
                         "WHERE TABLE_SCHEMA = 'PUBLIC' AND UPPER(TABLE_NAME) = UPPER(?) " +
                         "ORDER BY ORDINAL_POSITION";
            columns = jdbcTemplate.query(sql, (rs, rowNum) -> {
                Map<String, Object> col = new HashMap<>();
                col.put("name", rs.getString("COLUMN_NAME"));
                col.put("type", rs.getString("TYPE_NAME"));
                col.put("maxLength", rs.getObject("CHARACTER_MAXIMUM_LENGTH"));
                col.put("nullable", rs.getString("IS_NULLABLE"));
                col.put("defaultValue", rs.getString("COLUMN_DEFAULT"));
                return col;
            }, tableName);
        } catch (Exception e) {
            Map<String, Object> err = new HashMap<>();
            err.put("error", e.getMessage());
            columns.add(err);
        }
        return columns;
    }

    public Map<String, Object> executeSql(String query) {
        Map<String, Object> response = new HashMap<>();
        if (query == null || query.trim().isEmpty()) {
            response.put("success", false);
            response.put("message", "Câu lệnh SQL không được để trống.");
            return response;
        }

        String trimmed = query.trim();
        long startTime = System.currentTimeMillis();

        try {
            if (trimmed.toUpperCase().startsWith("SELECT") || 
                trimmed.toUpperCase().startsWith("SHOW") || 
                trimmed.toUpperCase().startsWith("EXPLAIN")) {
                
                List<Map<String, Object>> rows = jdbcTemplate.queryForList(trimmed);
                long duration = System.currentTimeMillis() - startTime;
                response.put("success", true);
                response.put("type", "QUERY");
                response.put("rowCount", rows.size());
                response.put("durationMs", duration);
                response.put("data", rows);
                if (!rows.isEmpty()) {
                    response.put("columns", rows.get(0).keySet());
                } else {
                    response.put("columns", Collections.emptyList());
                }
            } else {
                int affected = jdbcTemplate.update(trimmed);
                long duration = System.currentTimeMillis() - startTime;
                response.put("success", true);
                response.put("type", "UPDATE");
                response.put("affectedRows", affected);
                response.put("durationMs", duration);
                response.put("message", "Thực thi thành công! Số dòng bị ảnh hưởng: " + affected);
            }
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            response.put("success", false);
            response.put("durationMs", duration);
            response.put("error", e.getMessage());
        }

        return response;
    }

    public Map<String, Object> optimizeDatabase() {
        Map<String, Object> res = new HashMap<>();
        try {
            jdbcTemplate.execute("ANALYZE");
            res.put("success", true);
            res.put("message", "Đã phân tích lại thống kê chỉ mục (ANALYZE) thành công. Tốc độ truy vấn đã được tối ưu hóa!");
        } catch (Exception e) {
            res.put("success", false);
            res.put("message", "Lỗi tối ưu: " + e.getMessage());
        }
        return res;
    }
}