package fr.husta.test.sqlinjection;

import org.h2.api.ErrorCode;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.jdbc.JdbcTestUtils;

import javax.sql.DataSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

@RunWith(SpringRunner.class)
@SpringBootTest
public class DestructiveSqlInjectionWithPreparedStmtTest {

    private static final Logger log = LoggerFactory.getLogger(DestructiveSqlInjectionWithPreparedStmtTest.class);

    @Autowired
    private DataSource dataSource;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    public void contextLoads() {
    }

    /**
     * Inspired from XKCD : <a href="https://www.xkcd.com/327/">Exploits of a Mom</a>.
     * <br>
     * See also : <a href="https://stackoverflow.com/questions/332365/how-does-the-sql-injection-from-the-bobby-tables-xkcd-comic-work">ref in StackOverflow</a>.
     */
   
    
    @Test
    public void doSqlInjectionWithDropTable() {
        // assert table student exists
        int count = JdbcTestUtils.countRowsInTable(jdbcTemplate, "student");
        assertThat(count).isGreaterThan(0);
        System.out.println("Number of rows : " + count);

        
        String param1 = "3";
        String param2 = "SHYAM'); DROP TABLE student; --";

      
        String sqlInsertPreparedStmtQuery = "insert into student (id, name) values(?,?)";
      
        jdbcTemplate.execute(sqlInsertPreparedStmtQuery,new PreparedStatementCallback<Boolean>(){  
            @Override  
            public Boolean doInPreparedStatement(PreparedStatement ps)  
                    throws SQLException, DataAccessException {  
                      
                ps.setString(1, param1);  
                ps.setString(2,param2);              
                return ps.execute();  
                      
            }  
            });  
       
       count = JdbcTestUtils.countRowsInTable(jdbcTemplate, "student");
       System.out.println("Number of rows : " + count);
       selectQuery();
       
    }
    
    
    private void selectQuery() {
        String sql = "select id, name from student";
        List<String> results =
                jdbcTemplate.query(sql,
                        createSingleStringRowMapper());
        assertThat(results).isNotEmpty();
        System.out.println(results.size());
        results.forEach(log::debug);
    }
    
    private static RowMapper<String> createSingleStringRowMapper() {
        return (rs, rowNum) -> String.format("%d - %s",
                rs.getInt(1),
                rs.getString(2));
    }

}
