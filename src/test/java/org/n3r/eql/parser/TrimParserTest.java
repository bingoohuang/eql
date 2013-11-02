package org.n3r.eql.parser;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.n3r.eql.map.EqlRun;
import org.n3r.eql.util.EqlUtils;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

// refer http://mybatis.github.io/mybatis-3/dynamic-sql.html
public class TrimParserTest {
    @Test
    public void test() {
        String str = EqlUtils.loadClassPathResource("org/n3r/eql/TrimTest.eql");
        EqlParser eqlParser = new EqlParser();
        Map<String, EqlBlock> map = eqlParser.parse(null, "", str);

        EqlBlock updateAuthor = map.get("updateAuthor");
        DynamicSql updateAuthorSql = (DynamicSql) updateAuthor.getSqls().get(0);

        assertThat(updateAuthorSql.getParts().size(), is(3));
        assertThat(((LiteralPart) updateAuthorSql.getParts().part(0)).getSql(), is("update author\n"));
        assertThat(((LiteralPart) updateAuthorSql.getParts().part(2)).getSql(), is("where id=#id#\n"));
        TrimPart trimPart = (TrimPart) updateAuthorSql.getParts().part(1);
        assertThat(trimPart.getParts().size(), is(4));
/*
-- [updateAuthor]
update author
-- trim prefix=SET suffixOverrides=,
  -- iff username != null
         username=#username#,
  -- iff password != null
         PASSWORD=#password#,
  -- iff email != null
         email=#email#,
  -- iff bio != null
          bio=#bio#,
-- end
where id=#id#
 */
        Map<String, Object> context = Maps.newHashMap();

        Object[] params = new Object[]{};
        Object[] dynamics = new Object[]{};
        List<EqlRun> eqlRuns = updateAuthor.createEqlRunsByEqls(context, params, dynamics);
        String runSql = eqlRuns.get(0).getRunSql();
        assertThat(runSql, is("update author\nwhere id=?"));

        Map<String, String> bean = Maps.newHashMap();
        bean.put("username", "bingoo");
        params = new Object[]{bean};
        eqlRuns = updateAuthor.createEqlRunsByEqls(context, params, dynamics);
        runSql = eqlRuns.get(0).getRunSql();
        assertThat(runSql, is("update author\nSET username=?\nwhere id=?"));

        bean.put("password", "huang");
        eqlRuns = updateAuthor.createEqlRunsByEqls(context, params, dynamics);
        runSql = eqlRuns.get(0).getRunSql();
        assertThat(runSql, is("update author\nSET username=?,\nPASSWORD=?\nwhere id=?"));

        EqlBlock selectBlog = map.get("selectBlog");

/*
-- [selectBlog]
SELECT STATE FROM BLOG
-- trim prefix=WHERE prefixOverrides=AND|OR
   -- iff state != null
      and state = #state#
   -- iff title != null
      AND title like #title#
   -- iff author != null and author.name != null
      AND author_name like #author.name#
-- end
GROUP BY STATE
 */
        bean = Maps.newHashMap();
        params = new Object[]{bean};
        eqlRuns = selectBlog.createEqlRunsByEqls(context, params, dynamics);
        runSql = eqlRuns.get(0).getRunSql();
        assertThat(runSql, is("SELECT STATE FROM BLOG\nGROUP BY STATE"));

        bean.put("state", "nanjing");
        eqlRuns = selectBlog.createEqlRunsByEqls(context, params, dynamics);
        runSql = eqlRuns.get(0).getRunSql();
        assertThat(runSql, is("SELECT STATE FROM BLOG\nWHERE state = ?\nGROUP BY STATE"));

        EqlBlock nestedCondition = map.get("nestedCondition");
/*
-- [nestedCondition]
SELECT * FROM BLOG
-- trim prefix=WHERE prefixOverrides=AND|OR
    -- trim prefix=( prefixOverrides=OR suffix=)
      -- iff forLike != null
        OR subject LIKE #forLike#
      -- iff forInt != null
        OR file_id = #forInt#
    -- end
    AND dosya_ref is NULL
-- end
 */
        bean = Maps.newHashMap();
        params = new Object[]{bean};
        eqlRuns = nestedCondition.createEqlRunsByEqls(context, params, dynamics);
        runSql = eqlRuns.get(0).getRunSql();
        assertThat(runSql, is("SELECT * FROM BLOG\nWHERE dosya_ref is NULL"));

        bean.put("forInt", "123");
        eqlRuns = nestedCondition.createEqlRunsByEqls(context, params, dynamics);
        runSql = eqlRuns.get(0).getRunSql();
        assertThat(runSql, is("SELECT * FROM BLOG\nWHERE ( file_id = ? ) AND dosya_ref is NULL"));

        bean.put("forLike", "joke");
        eqlRuns = nestedCondition.createEqlRunsByEqls(context, params, dynamics);
        runSql = eqlRuns.get(0).getRunSql();
        assertThat(runSql, is("SELECT * FROM BLOG\nWHERE ( subject LIKE ?\nOR file_id = ? ) AND dosya_ref is NULL"));
    }
}
