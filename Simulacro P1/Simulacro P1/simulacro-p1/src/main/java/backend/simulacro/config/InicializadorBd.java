package backend.simulacro.config;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.*;

public final class InicializadorBd {
  private static final String URL = "jdbc:h2:mem:simulacro_p1";
  private static final String USER = "sa";
  private static final String PASS = "";

  private InicializadorBd() {
  }

  public static void run() throws SQLException, IOException {
    try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
      exec(conn, "src/main/resources/sql/ddl.sql");

      alignSequence(conn, "DESARROLLADORES", "DESA_ID", "SEQ_DESARROLLADORES");
      alignSequence(conn, "GENEROS", "GEN_ID", "SEQ_GENEROS");
      alignSequence(conn, "PLATAFORMAS", "PLAT_ID", "SEQ_PLATAFORMAS");
      alignSequence(conn, "JUEGOS", "JUEGO_ID", "SEQ_JUEGOS");
    }
  }

  private static void exec(Connection conn, String file) throws IOException, SQLException {
    String sql = Files.readString(Path.of(file), StandardCharsets.UTF_8);
    try (Statement st = conn.createStatement()) {
      st.execute(sql);
    }
  }

  private static void alignSequence(Connection conn, String table, String pkColumn, String seqName)
      throws SQLException {
    long next = 1L;
    try (Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT COALESCE(MAX(" + pkColumn + "), 0) + 1 AS nextval FROM " + table)) {
      if (rs.next())
        next = rs.getLong("nextval");
    }
    try (Statement st = conn.createStatement()) {
      st.execute("ALTER SEQUENCE " + seqName + " RESTART WITH " + next);
    }
  }
}
