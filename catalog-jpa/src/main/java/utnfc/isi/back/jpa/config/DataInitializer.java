package utnfc.isi.back.jpa.config;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.List;
import java.util.stream.Collectors;

public class DataInitializer {

    private static final String JDBC_URL = "jdbc:h2:mem:chinook;DB_CLOSE_DELAY=-1;MODE=PostgreSQL";
    private static final String USER = "sa";
    private static final String PW = "";

    private static final List<String> SCRIPTS_ORDER = List.of(
            "sql/01_chinook_tables.sql",
            "sql/02_chinook_data.sql",
            "sql/03_chinook_constraints_indexes.sql",
            "sql/03_chinook_sequences.sql" // si no existe, quitar
    );

    public void init() {
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USER, PW)) {
            conn.setAutoCommit(false);
            try (Statement st = conn.createStatement()) {
                for (String path : SCRIPTS_ORDER) {
                    InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
                    if (is == null) {
                        System.out.println("Aviso: no se encontró script: " + path + " (se continúa)");
                        continue;
                    }
                    String content;
                    try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                        content = br.lines().collect(Collectors.joining("\n"));
                    }
                    // Split naive por ';'
                    String[] statements = content.split(";");
                    for (String raw : statements) {
                        String sql = raw.strip();
                        if (sql.isEmpty()) continue;
                        if (sql.startsWith("--")) continue;
                        st.execute(sql);
                    }
                }
                conn.commit();
            } catch (Exception ex) {
                conn.rollback();
                throw new RuntimeException("Error ejecutando scripts de inicialización", ex);
            }
        } catch (Exception e) {
            throw new RuntimeException("No se pudo inicializar la base", e);
        }
    }
}