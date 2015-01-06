package org.n3r.eql.dbfieldcryptor.parser;

import com.alibaba.druid.sql.dialect.oracle.parser.OracleStatementParser;
import com.alibaba.druid.sql.parser.SQLStatementParser;
import com.google.common.collect.Sets;
import org.junit.Assert;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class OracleSensitiveFieldsParserTest {
    String liuleiSql = "SELECT * FROM ( SELECT ROW__.*, ROWNUM RN__ FROM (SELECT  N.PSPT_NO \"psptNo\",O.ORDER_NO \"orderNo\", CA.PARA_CODE2 \"orderFrom\",\n" +
            "O.PROVINCE_CODE \"provinceCode\", O.CITY_CODE \"cityCode\",\n" +
            "O.PAY_TYPE \"payType\", to_char(O.TOPAY_MONEY/1000,'FM9999999999990.00') \"toPayMoney\",\n" +
            "O.REFERRER_NAME \"referrerName\", O.REFERRER_PHONE \"referrerPhone\",\n" +
            "N.PRE_NUM \"phone\", N.CUST_NAME \"custName\",\n" +
            "N.PSPT_TYPE_CODE \"psptTypeCode\",\n" +
            "G.GOODS_NAME \"goodsName\",N.USER_TAG \"userTag\",\n" +
            "G.TMPL_ID||'' \"tmplId\",\n" +
            "(SELECT A.ATTR_VAL_NAME\n" +
            "FROM TF_B_ORDER_GOODSINS_ATVAL A\n" +
            "WHERE A.ATTR_CODE = 'A000023'\n" +
            "AND A.ORDER_ID = O.ORDER_ID AND O.PARTITION_ID = A.PARTITION_ID) AS \"plan\",\n" +
            "(SELECT LISTAGG(A.ATTR_VAL_NAME, ' ') WITHIN\n" +
            "GROUP(\n" +
            "ORDER BY A.ATTR_CODE)\n" +
            "FROM TF_B_ORDER_GOODSINS_ATVAL A\n" +
            "WHERE ATTR_CODE IN ('A000014', 'A000015', 'A000016')\n" +
            "AND A.ORDER_ID = O.ORDER_ID AND O.PARTITION_ID = A.PARTITION_ID) AS \"terminal\",\n" +
            "(SELECT COUNT(1)\n" +
            "FROM TR_B_VERIFY_RST R\n" +
            "WHERE R.VRST_STATE = 0\n" +
            "AND R.VRST_TYPE_CODE <> 'AUTO_VERIFY_LOG'\n" +
            "AND R.ORDER_ID = O.ORDER_ID AND O.PARTITION_ID = R.PARTITION_ID) AS \"orderState\"\n" +
            "FROM TF_B_ORDER O\n" +
            "LEFT JOIN TD_B_COMMPARA CA ON (CA.PARAM_ATTR = '1009' AND CA.PARAM_CODE = 'MALL_ORDER_FROM' AND CA.PARA_CODE1 = O.ORDER_FROM)\n" +
            "LEFT JOIN TF_B_ORDER_NETIN N ON (O.ORDER_ID = N.ORDER_ID AND O.PARTITION_ID = N.PARTITION_ID )\n" +
            "LEFT JOIN TF_B_ORDER_GOODSINS G ON (O.ORDER_ID = G.ORDER_ID AND O.PARTITION_ID = G.PARTITION_ID )\n" +
            "LEFT JOIN TF_B_ORDER_POST P ON (O.ORDER_ID = P.ORDER_ID AND O.PARTITION_ID = P.PARTITION_ID )\n" +
            "INNER JOIN TF_M_STAFF_BUSIAREA_RES AREAC ON(AREAC.STAFF_ID = ? AND AREAC.BUSIAREA_TYPE = '2'\n" +
            "AND O.PROVINCE_CODE = ? AND O.CITY_CODE = AREAC.BUSIAREA_CODE)\n" +
            "WHERE O.ORDER_STATE = 'CA'\n" +
            "AND O.PROCESS_MERCHANT_ID = ?\n" +
            "AND O.STAFF_ID IS NULL\n" +
            "AND O.PROVINCE_CODE=?\n" +
            "ORDER BY O.PAY_COMPLETE_TIME ) ROW__  WHERE ROWNUM <= ?) WHERE RN__ > ?";

    @Test
    public void testLiulei() {
        HashSet<String> liuleiSecuretFields = Sets.newHashSet("TF_B_ORDER_NETIN.PSPT_NO");
        OracleSensitiveFieldsParser parser;
        parser = OracleSensitiveFieldsParser.parseOracleSql(liuleiSql, liuleiSecuretFields);

        Assert.assertEquals(Sets.newHashSet(1), parser.getSecureResultIndices());
        Assert.assertEquals(Sets.newHashSet("PSPTNO"), parser.getSecureResultLabels());
    }


    String sql = "INSERT\n" +
            "INTO TF_B_ORDER\n" +
            "  (\n" +
            "    ORDER_ID,\n" +
            "    PARTITION_ID,\n" +
            "    ORDER_NO,\n" +
            "    CREATE_TIME,\n" +
            "    CUST_ID,\n" +
            "    ORDER_TIME,\n" +
            "    ORIGINAL_PRICE,\n" +
            "    COUPON_MONEY,\n" +
            "    TOPAY_MONEY,\n" +
            "    INCOME_MONEY,\n" +
            "    ORDER_STATE,\n" +
            "    PAY_TYPE,\n" +
            "    REFERRER_NAME,\n" +
            "    REFERRER_PHONE,\n" +
            "    POST_TAG,\n" +
            "    POST_FEE,\n" +
            "    CANCEL_TAG,\n" +
            "    MERCHANT_ID,\n" +
            "    DELIVER_TYPE_CODE,\n" +
            "    DELIVER_DATE_TYPE,\n" +
            "    CUST_REMARK,\n" +
            "    INVOCE_TITLE,\n" +
            "    INVO_CONT_CODE,\n" +
            "    NEED_AFFIRM,\n" +
            "    PAY_STATE,\n" +
            "    PAY_WAY,\n" +
            "    PROVINCE_CODE,\n" +
            "    CITY_CODE,\n" +
            "    DISTRICT_CODE,\n" +
            "    CUST_IP,\n" +
            "    LOGIN_NAME,\n" +
            "    DELAY_TIME,\n" +
            "    CUST_IP_STR,\n" +
            "    RELEASE_TIME,\n" +
            "    ORDER_FROM,\n" +
            "    ORDER_TOTAL_MONEY,\n" +
            "    WT_FPC_ID,\n" +
            "    WT_MC_ID,\n" +
            "    CONN_CHANNEL,\n" +
            "    PROCESS_MERCHANT_ID\n" +
            "  )\n" +
            "SELECT ?,\n" +
            "  MOD(?,100),\n" +
            "  ?,\n" +
            "  sysdate,\n" +
            "  ?,\n" +
            "  sysdate,\n" +
            "  ?,\n" +
            "  ?,\n" +
            "  ?,\n" +
            "  ?,\n" +
            "  ?,\n" +
            "  ?,\n" +
            "  ?,\n" +
            "  ?,\n" +
            "  ?,\n" +
            "  ?,\n" +
            "  '0',\n" +
            "  ?,\n" +
            "  ?,\n" +
            "  ?,\n" +
            "  ?,\n" +
            "  ?,\n" +
            "  ?,\n" +
            "  '1',\n" +
            "  '0',\n" +
            "  ?,\n" +
            "  NVL(?,T.PROVINCE_CODE),\n" +
            "  NVL(?,T.CITY_CODE),\n" +
            "  NULL,\n" +
            "  ?,\n" +
            "  ?,\n" +
            "  SYSDATE,\n" +
            "  ?,\n" +
            "  DECODE(?,'01',(TRUNC(SYSDATE + 1) + 22 / 24), '02',(TRUNC(SYSDATE + DECODE(?, '1', 8, '0', 20, 8)) + 22 / 24), (TRUNC(SYSDATE + 1) + 22 / 24)),\n" +
            "  ?,\n" +
            "  ?,\n" +
            "  ?,\n" +
            "  ?,\n" +
            "  ?,\n" +
            "  ?\n" +
            "FROM TF_F_MERCHANT T\n" +
            "WHERE T.MERCHANT_ID = ? ";

    HashSet<String> securetFields = Sets.newHashSet("TF_B_ORDER.ORDER_ID",
            "TF_B_ORDER_NETIN.PSPT_ADDR", "TF_F_MERCHANT.MERCHANT_ID", "TF_B_ORDER_GOODSINS_ATVAL.ORDER_ID",
            "TF_B_ORDER_NETIN.PSPT_NO", "TF_B_ORDER_POST.POST_ADDR", "TF_B_ORDER_POST.RECEIVER_PSPT_NO");

    @Test
    public void testInsertSelect() {
        OracleSensitiveFieldsParser parser = OracleSensitiveFieldsParser.parseOracleSql(sql,
                securetFields);

        Assert.assertEquals(Sets.newHashSet(1, 35), parser.getSecureBindIndices());
    }

    String insertAllSql = "INSERT ALL           INTO TF_B_ORDER_GOODSINS_ATVAL              (ORDER_ID,PARTITION_ID,GOODS_INST_ID,GOODS_ID,ATTR_CODE,ATTR_NAME,ATTR_VAL_CODE,ATTR_VAL_NAME,ATTR_VAL_DESC,EOP_ATTR_CODE,EOP_ATTR_VAL_CODE)          VALUES              (?,MOD(?,100),?,?,?,?,?,?,?,?,?)                    INTO TF_B_ORDER_GOODSINS_ATVAL              (ORDER_ID,PARTITION_ID,GOODS_INST_ID,GOODS_ID,ATTR_CODE,ATTR_NAME,ATTR_VAL_CODE,ATTR_VAL_NAME,ATTR_VAL_DESC,EOP_ATTR_CODE,EOP_ATTR_VAL_CODE)          VALUES              (?,MOD(?,100),?,?,?,?,?,?,?,?,?)                    INTO TF_B_ORDER_GOODSINS_ATVAL              (ORDER_ID,PARTITION_ID,GOODS_INST_ID,GOODS_ID,ATTR_CODE,ATTR_NAME,ATTR_VAL_CODE,ATTR_VAL_NAME,ATTR_VAL_DESC,EOP_ATTR_CODE,EOP_ATTR_VAL_CODE)          VALUES              (?,MOD(?,100),?,?,?,?,?,?,?,?,?)                    INTO TF_B_ORDER_GOODSINS_ATVAL              (ORDER_ID,PARTITION_ID,GOODS_INST_ID,GOODS_ID,ATTR_CODE,ATTR_NAME,ATTR_VAL_CODE,ATTR_VAL_NAME,ATTR_VAL_DESC,EOP_ATTR_CODE,EOP_ATTR_VAL_CODE)          VALUES              (?,MOD(?,100),?,?,?,?,?,?,?,?,?)                    INTO TF_B_ORDER_GOODSINS_ATVAL              (ORDER_ID,PARTITION_ID,GOODS_INST_ID,GOODS_ID,ATTR_CODE,ATTR_NAME,ATTR_VAL_CODE,ATTR_VAL_NAME,ATTR_VAL_DESC,EOP_ATTR_CODE,EOP_ATTR_VAL_CODE)          VALUES              (?,MOD(?,100),?,?,?,?,?,?,?,?,?)                    INTO TF_B_ORDER_GOODSINS_ATVAL              (ORDER_ID,PARTITION_ID,GOODS_INST_ID,GOODS_ID,ATTR_CODE,ATTR_NAME,ATTR_VAL_CODE,ATTR_VAL_NAME,ATTR_VAL_DESC,EOP_ATTR_CODE,EOP_ATTR_VAL_CODE)          VALUES              (?,MOD(?,100),?,?,?,?,?,?,?,?,?)                    INTO TF_B_ORDER_GOODSINS_ATVAL              (ORDER_ID,PARTITION_ID,GOODS_INST_ID,GOODS_ID,ATTR_CODE,ATTR_NAME,ATTR_VAL_CODE,ATTR_VAL_NAME,ATTR_VAL_DESC,EOP_ATTR_CODE,EOP_ATTR_VAL_CODE)          VALUES              (?,MOD(?,100),?,?,?,?,?,?,?,?,?)                    INTO TF_B_ORDER_GOODSINS_ATVAL              (ORDER_ID,PARTITION_ID,GOODS_INST_ID,GOODS_ID,ATTR_CODE,ATTR_NAME,ATTR_VAL_CODE,ATTR_VAL_NAME,ATTR_VAL_DESC,EOP_ATTR_CODE,EOP_ATTR_VAL_CODE)          VALUES              (?,MOD(?,100),?,?,?,?,?,?,?,?,?)                    INTO TF_B_ORDER_GOODSINS_ATVAL              (ORDER_ID,PARTITION_ID,GOODS_INST_ID,GOODS_ID,ATTR_CODE,ATTR_NAME,ATTR_VAL_CODE,ATTR_VAL_NAME,ATTR_VAL_DESC,EOP_ATTR_CODE,EOP_ATTR_VAL_CODE)          VALUES              (?,MOD(?,100),?,?,?,?,?,?,?,?,?)                    INTO TF_B_ORDER_GOODSINS_ATVAL              (ORDER_ID,PARTITION_ID,GOODS_INST_ID,GOODS_ID,ATTR_CODE,ATTR_NAME,ATTR_VAL_CODE,ATTR_VAL_NAME,ATTR_VAL_DESC,EOP_ATTR_CODE,EOP_ATTR_VAL_CODE)          VALUES              (?,MOD(?,100),?,?,?,?,?,?,?,?,?)                    INTO TF_B_ORDER_GOODSINS_ATVAL              (ORDER_ID,PARTITION_ID,GOODS_INST_ID,GOODS_ID,ATTR_CODE,ATTR_NAME,ATTR_VAL_CODE,ATTR_VAL_NAME,ATTR_VAL_DESC,EOP_ATTR_CODE,EOP_ATTR_VAL_CODE)          VALUES              (?,MOD(?,100),?,?,?,?,?,?,?,?,?)                    INTO TF_B_ORDER_GOODSINS_ATVAL              (ORDER_ID,PARTITION_ID,GOODS_INST_ID,GOODS_ID,ATTR_CODE,ATTR_NAME,ATTR_VAL_CODE,ATTR_VAL_NAME,ATTR_VAL_DESC,EOP_ATTR_CODE,EOP_ATTR_VAL_CODE)          VALUES              (?,MOD(?,100),?,?,?,?,?,?,?,?,?)          SELECT 1 FROM DUAL ";

    @Test
    public void testInsertAll() {
        OracleSensitiveFieldsParser parser = OracleSensitiveFieldsParser.parseOracleSql(insertAllSql,
                securetFields);
        Assert.assertEquals(Sets.newHashSet(34, 1, 100, 23, 67, 111, 78, 56, 89, 12, 122, 45), parser.getSecureBindIndices());
    }

    String selectSql = "SELECT O.ORDER_NO ORDERNO,\n" +
            "       O.ORDER_ID ORDERID,\n" +
            "       O.CANCEL_TAG CANCELTAG,\n" +
            "       CASE\n" +
            "         WHEN SYSDATE > O.RELEASE_TIME THEN\n" +
            "          '1'\n" +
            "         ELSE\n" +
            "          '0'\n" +
            "       END TIMING_CANCEL,\n" +
            "       O.ORDER_STATE ORDERSTATE,\n" +
            "       DS.TMPL_ID,\n" +
            "       TG.GOODS_ID,\n" +
            "       TW.PAY_WAY_NAME,\n" +
            "       DECODE(O.ORDER_STATE,\n" +
            "              'A0',\n" +
            "              '等待付款',\n" +
            "              'AX',\n" +
            "              '等待付款',\n" +
            "              'B0',\n" +
            "              '待发货',\n" +
            "              'BX',\n" +
            "              '待发货',\n" +
            "              'C0',\n" +
            "              '待发货',\n" +
            "              'CA',\n" +
            "              '待开户',\n" +
            "              'C1',\n" +
            "              '待发货',\n" +
            "              'CX',\n" +
            "              '待发货',\n" +
            "              'CZ',\n" +
            "              '待发货',\n" +
            "              'D0',\n" +
            "              '待发货',\n" +
            "              'D1',\n" +
            "              '待发货',\n" +
            "              'DX',\n" +
            "              '待发货',\n" +
            "              'E0',\n" +
            "              '已发货',\n" +
            "              'EX',\n" +
            "              '已发货',\n" +
            "              'F0',\n" +
            "              '已发货',\n" +
            "              'FX',\n" +
            "              '已发货',\n" +
            "              '00',\n" +
            "              '交易成功',\n" +
            "              'ERROR') ORDERSTATESTR,\n" +
            "       CASE\n" +
            "         WHEN O.DELAY_TIME IS NULL THEN\n" +
            "          '0'\n" +
            "         WHEN O.DELAY_TIME < SYSDATE THEN\n" +
            "          '0'\n" +
            "         ELSE\n" +
            "          '1'\n" +
            "       END ISDELAY,\n" +
            "       CASE\n" +
            "         WHEN O.PROCESS_DELAY_TIME IS NULL THEN\n" +
            "          '0'\n" +
            "         WHEN O.PROCESS_DELAY_TIME < SYSDATE THEN\n" +
            "          '0'\n" +
            "         ELSE\n" +
            "          '1'\n" +
            "       END ISPROCESSDELAY,\n" +
            "       O.REFERRER_NAME,\n" +
            "       O.REFERRER_PHONE,\n" +
            "       OP.RECEIVER_NAME RECEIVERNAME,\n" +
            "       DECODE(O.PAY_TYPE, '02', '02', '01') PAYTYPE,\n" +
            "       I.INVOCE_CONTENT INVOCECONTENT,\n" +
            "       O.INVOCE_TITLE INVOCETITLE,\n" +
            "       O.CUST_REMARK CUSTREMARK,\n" +
            "       TO_CHAR(NVL(O.ORIGINAL_PRICE, 0) / 1000, 'FM9999999990.00') ORIGINALPRICE,\n" +
            "       TO_CHAR(NVL(O.COUPON_MONEY, 0) / 1000, 'FM9999999990.00') COUPONMONEY,\n" +
            "       TO_CHAR(NVL(O.INCOME_MONEY, 0) / 1000, 'FM9999999990.00') INCOMEMONEY,\n" +
            "       TO_CHAR(NVL(O.POST_FEE, 0) / 1000, 'FM9999999990.00') POSTFEE,\n" +
            "       TO_CHAR(NVL(IFE.FEE, 0) / 1000, 'FM9999999990.00') VOUCHERMONEY,\n" +
            "       TO_CHAR(O.INCOME_MONEY) INCOME_MONEY,\n" +
            "       PT.PAY_TYPE_NAME PAYTYPESTR,\n" +
            "       O.DELIVER_TYPE_CODE,\n" +
            "       DP.DISPATCH_NAME DISPATCHNAME,\n" +
            "       LGTS.LGTS_TYPE,\n" +
            "       LGTS.LGTS_ID,\n" +
            "       DECODE(LGTS.LGTS_ID, '1008', '1', '0') LGSTSHOW,\n" +
            "       LGTS.LGTS_NAME LOGISTICNAME,\n" +
            "       LGTS.LGTS_ORDER LOGISTICNUMBER,\n" +
            "       DTPYE.DLVTYPE_EXPLIAN DLVTYPEEXPLIAN,\n" +
            "       CASE\n" +
            "         WHEN OP.MOBILE_PHONE IS NOT NULL THEN\n" +
            "          OP.MOBILE_PHONE\n" +
            "         WHEN OP.FIX_PHONE IS NOT NULL THEN\n" +
            "          OP.FIX_PHONE\n" +
            "         ELSE\n" +
            "          ''\n" +
            "       END PHONE,\n" +
            "       OP.POST_ADDR POSTADDR,\n" +
            "       OP.POST_CODE POSTCODE,\n" +
            "       TO_CHAR(O.CREATE_TIME, 'YYYY-MM-DD') INITSTATEDATE,\n" +
            "       TO_CHAR(O.CREATE_TIME, 'HH24:MI:SS') INITSTATETIME,\n" +
            "       CASE\n" +
            "         WHEN TO_CHAR(O.CREATE_TIME, 'HH24') < '12' THEN\n" +
            "          TO_CHAR(O.CREATE_TIME, 'YYYY-MM-DD') || ' 24:00'\n" +
            "         ELSE\n" +
            "          TO_CHAR(O.CREATE_TIME + NUMTODSINTERVAL(1 * 24, 'HOUR'),\n" +
            "                  'YYYY-MM-DD') || ' 12:00'\n" +
            "       END DETAILINFODATE,\n" +
            "       M.MERCHANT_NAME MERCHANTNAME,\n" +
            "       M.MERCHANT_ID,\n" +
            "       M.CUSTSERVICE_PHONE,\n" +
            "       P.PROVINCE_NAME PROVINCENAME,\n" +
            "       E.CITY_NAME EPARCHYNAME,\n" +
            "       C.DISTRICT_NAME COUNTYNAME,\n" +
            "       INSTALL.INSTALMENT_TERM INSTALMENT_TERM,\n" +
            "       TO_CHAR(ROUND(O.INCOME_MONEY / 1000 / INSTALL.INSTALMENT_TERM, 2)) INSTALL_MONEY\n" +
            "  FROM TF_B_ORDER            O,\n" +
            "       TF_B_ORDER_INSTALMENT INSTALL,\n" +
            "       TF_B_ORDER_GOODSINS   TG,\n" +
            "       TF_B_ORDER_POST       OP,\n" +
            "       TD_B_DLVTYPE          DTPYE,\n" +
            "       TF_F_MERCHANT         M,\n" +
            "       TD_B_DISPATCH         DP,\n" +
            "       TD_B_PAYTYPE          PT,\n" +
            "       TF_M_WEB_PROVINCE     P,\n" +
            "       TF_M_WEB_CITY         E,\n" +
            "       TF_M_WEB_DISTRICT     C,\n" +
            "       TD_B_INVOICE          I,\n" +
            "       TF_B_ORDER_LGTS       LGTS,\n" +
            "       TF_G_GOODS            DS,\n" +
            "       TD_B_PAYWAY           TW,\n" +
            "       TF_B_ORDER_INCOMEFEE  IFE\n" +
            " WHERE O.ORDER_ID = OP.ORDER_ID(+)\n" +
            "   AND O.PARTITION_ID = OP.PARTITION_ID(+)\n" +
            "   AND O.ORDER_ID = INSTALL.ORDER_ID(+)\n" +
            "   AND O.PARTITION_ID = INSTALL.PARTITION_ID(+)\n" +
            "   AND O.DELIVER_DATE_TYPE = DTPYE.DLVTYPE_CODE(+)\n" +
            "   AND O.MERCHANT_ID = M.MERCHANT_ID\n" +
            "   AND O.DELIVER_TYPE_CODE = DP.DISPATCH_CODE(+)\n" +
            "   AND O.PAY_TYPE = PT.PAY_TYPE_CODE(+)\n" +
            "   AND OP.PROVINCE_CODE = P.PROVINCE_CODE(+)\n" +
            "   AND OP.CITY_CODE = E.CITY_CODE(+)\n" +
            "   AND OP.DISTRICT_CODE = C.DISTRICT_CODE(+)\n" +
            "   AND O.INVO_CONT_CODE = I.INVO_CONT_CODE(+)\n" +
            "   AND O.ORDER_ID = ?\n" +
            "   AND O.PARTITION_ID = MOD(?, 100)\n" +
            "   AND O.ORDER_ID = LGTS.ORDER_ID(+)\n" +
            "   AND O.PARTITION_ID = LGTS.PARTITION_ID(+)\n" +
            "   AND O.ORDER_ID = IFE.ORDER_ID(+)\n" +
            "   AND O.PARTITION_ID = IFE.PARTITION_ID(+)\n" +
            "   AND TG.ORDER_ID = O.ORDER_ID\n" +
            "   AND TG.GOODS_ID = DS.GOODS_ID\n" +
            "   AND O.PAY_TYPE = TW.PAY_TYPE_CODE(+)\n" +
            "   AND O.PAY_WAY = TW.PAY_WAY_CODE(+)\n" +
            "   AND IFE.INCOME_TYPE(+) = '02'";

    @Test
    public void testSelect() {
        OracleSensitiveFieldsParser parser = OracleSensitiveFieldsParser.parseOracleSql(selectSql,
                securetFields);
        Assert.assertEquals(Sets.newHashSet(2, 35, 41), parser.getSecureResultIndices());
    }

    String unionSql = "SELECT A.ATTR_CODE,\n" +
            "               A.ATTR_NAME,\n" +
            "               B.ATTR_VAL_CODE,\n" +
            "               B.ATTR_VAL_NAME,\n" +
            "               A.EOP_ATTR_CODE,\n" +
            "               B.EOP_ATTR_VAL_CODE\n" +
            "          FROM TD_G_ATTR A, TD_G_ATTRVAL B\n" +
            "         WHERE A.ATTR_CODE = B.ATTR_CODE(+)\n" +
            "        UNION ALL\n" +
            "        SELECT C.PARA_CODE1 ATTR_CODE,\n" +
            "               C.PARAM_NAME ATTR_NAME,\n" +
            "               '' ATTR_VAL_CODE,\n" +
            "               (CASE\n" +
            "                 WHEN SUBSTR(C.PARA_CODE1, 1, 1) = 'C' THEN\n" +
            "                  ''\n" +
            "                 ELSE\n" +
            "                  C.PARAM_CODE\n" +
            "               END) ATTR_VAL_NAME,\n" +
            "               '' EOP_ATTR_CODE,\n" +
            "               '' EOP_ATTR_VAL_CODE\n" +
            "          FROM TD_B_COMMPARA C\n" +
            "         WHERE C.PARAM_ATTR = '8002'";

    @Test
    public void testUnion() {
        OracleSensitiveFieldsParser parser = OracleSensitiveFieldsParser.parseOracleSql(unionSql,
                Sets.newHashSet("TD_G_ATTR.ATTR_CODE", "TD_B_COMMPARA.PARAM_NAME"));
        Assert.assertEquals(Sets.newHashSet(1, 2), parser.getSecureResultIndices());
    }

    String fnSql = "{ ? = call f_sys_getgoodseqid(?)}";

    @Test
    public void testFunctionSql() {
        OracleSensitiveFieldsParser parser = OracleSensitiveFieldsParser.parseOracleSql(fnSql,
                Sets.newHashSet("F_SYS_GETGOODSEQID.1"));
        Assert.assertEquals(Sets.newHashSet(1), parser.getSecureBindIndices());
    }

    String pagEQL = "SELECT ROW_.*,\n" +
            "  ROWNUM RN\n" +
            "FROM\n" +
            "  (SELECT B.PROVINCE_CODE PROVINCECODE,\n" +
            "    P.PROVINCE_NAME PROVINCENAME,\n" +
            "    B.CITY_CODE CITYCODE,\n" +
            "    C.CITY_NAME CITYNAME,\n" +
            "    B.SPEED SPEED,\n" +
            "    B.PRODUCT_MODE PRODUCTMODE,\n" +
            "    DECODE(B.PRODUCT_MODE,'0','包年','1','包月','2','包时','其他') PRODUCTMODEDESC,\n" +
            "    B.PRODUCT_CODE PRODUCTCODE,\n" +
            "    B.PRODUCT_NAME PRODUCTNAME,\n" +
            "    B.PRODUCT_DESC PRODUCTDESC,\n" +
            "    B.TOTALITEMS_FEE/1000 TOTALFEE,\n" +
            "    (SELECT COUNT(F.FEE_ID)\n" +
            "    FROM TD_P_BROADBAND_FEE F\n" +
            "    WHERE F.PROVINCE_CODE = B.PROVINCE_CODE\n" +
            "    AND F.CITY_CODE       = B.CITY_CODE\n" +
            "    AND F.PRODUCT_CODE    = B.PRODUCT_CODE\n" +
            "    ) FEEITEMCOUNT,\n" +
            "    (SELECT COUNT(G.GOODS_ID)\n" +
            "    FROM TF_G_BROADBAND_PRODUCT P,\n" +
            "      TF_G_GOODS G,\n" +
            "      TF_G_GOODS_ATTRVAL A\n" +
            "    WHERE P.PRODUCT_ID  = B.PRODUCT_CODE\n" +
            "    AND P.GOODS_ID      = G.GOODS_ID\n" +
            "    AND G.GOODS_ID      = A.GOODS_ID\n" +
            "    AND A.ATTR_CODE     = 'A000025'\n" +
            "    AND A.ATTR_VAL_CODE = B.CITY_CODE\n" +
            "    AND G.GOODS_STATE   = '3'\n" +
            "    ) UPGOODSCOUNT,\n" +
            "    (SELECT COUNT(G.GOODS_ID)\n" +
            "    FROM TF_G_BROADBAND_PRODUCT P,\n" +
            "      TF_G_GOODS G,\n" +
            "      TF_G_GOODS_ATTRVAL A\n" +
            "    WHERE P.PRODUCT_ID  = B.PRODUCT_CODE\n" +
            "    AND P.GOODS_ID      = G.GOODS_ID\n" +
            "    AND G.GOODS_ID      = A.GOODS_ID\n" +
            "    AND A.ATTR_CODE     = 'A000025'\n" +
            "    AND A.ATTR_VAL_CODE = B.CITY_CODE\n" +
            "    AND G.GOODS_STATE  != '0'\n" +
            "    ) GOODSCOUNT\n" +
            "  FROM TF_M_PROVINCE P,\n" +
            "    TF_M_CITY C,\n" +
            "    TD_P_BROADBAND B\n" +
            "  WHERE B.PROVINCE_CODE = P.PROVINCE_CODE(+)\n" +
            "  AND B.CITY_CODE       = C.CITY_CODE(+)\n" +
            "  ORDER BY B.CITY_CODE,\n" +
            "    TO_NUMBER(RTRIM(RTRIM(B.SPEED), 'M'))\n" +
            "  ) ROW_\n" +
            "WHERE ROWNUM <= 5";

    @Test
    public void testPagEQL() {
        OracleSensitiveFieldsParser parser = OracleSensitiveFieldsParser.parseOracleSql(pagEQL,
                Sets.newHashSet("TD_P_BROADBAND.CITY_CODE"));
        Assert.assertEquals(Sets.newHashSet(3), parser.getSecureResultIndices());
    }


    String updateSetQuery = "UPDATE TF_R_PHNBR_IDLE T\n" +
            "      SET (T.NET_TYPE_CODE, \n" +
            "           T.IMSI, \n" +
            "           T.SIM_CARD_NO,\n" +
            "           T.CODE_STATE, \n" +
            "           T.TRADE_CATE,\n" +
            "           T.WIRELESS_CARD_TYPE,\n" +
            "           T.CODE_GRADE,\n" +
            "           T.LIMIT_ID, \n" +
            "           T.NICE_RULE,\n" +
            "           T.GROUP_ID,\n" +
            "           T.PROVINCE_CODE,\n" +
            "           T.EPARCHY_CODE,\n" +
            "           T.CITY_CODE,\n" +
            "           T.BATCH_DEF_TAG,\n" +
            "           T.BATCH_ID,\n" +
            "           T.STAFF_IN,\n" +
            "           T.TIME_IN,\n" +
            "           T.STAFF_UPSHELF,\n" +
            "           T.TIME_UPSHELF,\n" +
            "           T.STAFF_DOWNSHELF,\n" +
            "           T.TIME_DOWNSHELF,\n" +
            "           T.DEPART_ID,\n" +
            "           T.STAFF_ID,\n" +
            "           T.POOL_ID,\n" +
            "           T.ADVANCE_LIMIT,\n" +
            "           T.LIMIT_TYPE,\n" +
            "           T.RANK_MONEY_S,\n" +
            "           T.MONTH_LIMIT,\n" +
            "           T.RULE_NAME,\n" +
            "           T.RULE_CODE,\n" +
            "           T.PRE_ORDER_TAG\n" +
            "           ) \n" +
            "       = \n" +
            "          (SELECT I.NET_TYPE_CODE,\n" +
            "                  I.IMSI,\n" +
            "                  I.SIM_CARD_NO,\n" +
            "                  I.CODE_STATE,\n" +
            "                  I.TRADE_CATE,\n" +
            "                  I.WIRELESS_CARD_TYPE,\n" +
            "                  I.CODE_GRADE,\n" +
            "                  I.LIMIT_ID,\n" +
            "                  ?,\n" +
            "                  I.GROUP_ID,\n" +
            "                  I.PROVINCE_CODE,\n" +
            "                  I.EPARCHY_CODE,\n" +
            "                  I.CITY_CODE,\n" +
            "                  I.BATCH_DEF_TAG,\n" +
            "                  I.BATCH_ID,\n" +
            "                  I.STAFF_IN,\n" +
            "                  I.TIME_IN,\n" +
            "                  I.STAFF_UPSHELF,\n" +
            "                  I.TIME_UPSHELF,\n" +
            "                  I.STAFF_DOWNSHELF,\n" +
            "                  I.TIME_DOWNSHELF,\n" +
            "                  I.DEPART_ID,\n" +
            "                  I.STAFF_ID,\n" +
            "                  I.POOL_ID,\n" +
            "                  nvl(L.ADVANCE_LIMIT,'0'),\n" +
            "                  L.LIMIT_TYPE,\n" +
            "                  nvl(L.RANK_MONEY_S,'0'),\n" +
            "                  nvl(L.MONTH_LIMIT,'0'),\n" +
            "                  ?,\n" +
            "                  ?,\n" +
            "                  ?\n" +
            "                  FROM TF_R_PHCODE_IDLE I, TD_R_NICERULE_LIMIT L\n" +
            "                  WHERE I.SERIAL_NUMBER = T.SERIAL_NUMBER\n" +
            "                    AND I.SERIAL_NUMBER = ?\n" +
            "                    AND I.LIMIT_ID = L.LIMIT_ID\n" +
            "                    AND (L.PROVINCE_ID = ? OR L.PROVINCE_ID = '98')\n" +
            "                    AND L.NET_TYPE_CODE = '01'\n" +
            "                    AND (L.TRADE_CATE = '0' OR L.TRADE_CATE = '1')\n" +
            "                    AND L.START_DATE <= SYSDATE\n" +
            "                    AND L.END_DATE >= SYSDATE)\n" +
            "     WHERE T.SERIAL_NUMBER = ?\n" +
            "       AND T.PROVINCE_CODE = ? \n" +
            "       AND T.CODE_STATE = '0'";

    @Test
    public void testUpdateSetQuery() {
        OracleSensitiveFieldsParser parser = OracleSensitiveFieldsParser.parseOracleSql(updateSetQuery,
                Sets.newHashSet("TF_R_PHNBR_IDLE.NICE_RULE", "TF_R_PHCODE_IDLE.SERIAL_NUMBER"));
        Assert.assertEquals(Sets.newHashSet(1, 5), parser.getSecureBindIndices());
    }

    @Test
    public void testQuery1() {
        String sql = "select d, f, a, b, c from table1 where c = ? and a = ? and b = ?";

        final Set<String> securetFieldsConfig = Sets.newHashSet("TABLE1.A", "TABLE1.B");
        OracleSensitiveFieldsParser visitorAdapter = OracleSensitiveFieldsParser.parseOracleSql(sql, securetFieldsConfig);

        Set<Integer> securetResultIndice = visitorAdapter.getSecureResultIndices();
        assertEquals(securetResultIndice, Sets.newHashSet(3, 4));
        Set<Integer> securetBindIndice = visitorAdapter.getSecureBindIndices();
        assertEquals(securetBindIndice, Sets.newHashSet(2, 3));

        sql = "select d, f, a, b, c from table1 where c = ? || ',' || ? and a = ? and b = ?";

        visitorAdapter = OracleSensitiveFieldsParser.parseOracleSql(sql, securetFieldsConfig);

        securetResultIndice = visitorAdapter.getSecureResultIndices();
        assertEquals(securetResultIndice, Sets.newHashSet(3, 4));
        securetBindIndice = visitorAdapter.getSecureBindIndices();
        assertEquals(securetBindIndice, Sets.newHashSet(3, 4));
    }


    @Test
    public void testQuery2() {
        String sql = "select t.a, t.b, t.c from table1 t where t.a = ?";

        final Set<String> securetFieldsConfig = Sets.newHashSet("TABLE1.A", "TABLE1.B");
        OracleSensitiveFieldsParser visitorAdapter = OracleSensitiveFieldsParser
                .parseOracleSql(sql, securetFieldsConfig);

        Set<Integer> securetResultIndice = visitorAdapter.getSecureResultIndices();
        assertEquals(securetResultIndice, Sets.newHashSet(1, 2));
        Set<Integer> securetBindIndice = visitorAdapter.getSecureBindIndices();
        assertEquals(securetBindIndice, Sets.newHashSet(1));
    }

    @Test
    public void testQuery3() {
        String sql = "select t1.a, t1.b, t2.c from table1 t1, table2 t2 where t1.id = ? and t1.id = t2.id and t2.d = ?";
        final Set<String> fields = Sets.newHashSet("TABLE1.A", "TABLE1.B", "TABLE2.D");

        OracleSensitiveFieldsParser visitorAdapter = OracleSensitiveFieldsParser
                .parseOracleSql(sql, fields);

        Set<Integer> securetResultIndice = visitorAdapter.getSecureResultIndices();
        assertEquals(securetResultIndice, Sets.newHashSet(1, 2));
        Set<Integer> securetBindIndice = visitorAdapter.getSecureBindIndices();
        assertEquals(securetBindIndice, Sets.newHashSet(2));

    }

    @Test
    public void testQuery4() {
        String sql = "SELECT   T.PSPT_NO \"CertNum\",\n" +
                "         T.PSPT_TYPE_CODE \"CertType\",\n" +
                "         T.PSPT_ADDR \"CertAdress\", \n" +
                "         T.CUST_NAME \"CustomerName\",\n" +
                "         TO_CHAR(T.PSPT_EXPIRE_DATE,'yyyyMMdd') \"CertExpireDate\",\n" +
                "         T.USER_TAG \"UserType\"\n" +
                "  FROM   TF_B_ORDER_NETIN T\n" +
                " WHERE   T.ORDER_ID = ?";

        final Set<String> securetFieldsConfig = Sets.newHashSet("TF_B_ORDER_NETIN.PSPT_NO",
                "TF_B_ORDER_NETIN.CUST_NAME");

        OracleSensitiveFieldsParser visitorAdapter = OracleSensitiveFieldsParser
                .parseOracleSql(sql, securetFieldsConfig);

        Set<Integer> securetResultIndice = visitorAdapter.getSecureResultIndices();
        assertEquals(securetResultIndice, Sets.newHashSet(1, 4));
        Set<Integer> securetBindIndice = visitorAdapter.getSecureBindIndices();
        assertEquals(securetBindIndice, Sets.newHashSet());
    }

    @Test
    public void testQuery5() {
        String sql = "SELECT O.ORDER_ID,\n" +
                "               O.PROVINCE_CODE,\n" +
                "               O.CITY_CODE,\n" +
                "               O.DISTRICT_ID,\n" +
                "               O.PAY_TYPE,\n" +
                "               O.INCOME_MONEY,\n" +
                "               O.CUST_ID,\n" +
                "               O.MERCHANT_ID,\n" +
                "               O.POST_ADDR_ID,\n" +
                "               M.MERCHANT_ID,\n" +
                "               M.CHANNEL_ID,\n" +
                "               M.CHANNEL_TYPE,\n" +
                "               S.STAFF_ID,\n" +
                "               S.ESS_STAFF_ID,\n" +
                "               A.ATTR_NAME,\n" +
                "               A.ATTR_CODE,\n" +
                "               A.ATTR_VAL_CODE,\n" +
                "               A.ATTR_VAL_NAME,\n" +
                "               A.EOP_ATTR_CODE,\n" +
                "               A.EOP_ATTR_VAL_CODE,\n" +
                "               A.INST_EACH_ID INST_EACH_ID,\n" +
                "               G.AMOUNT_RECEVABLE,\n" +
                "               G.AMOUNT_RECEIVED,\n" +
                "               G.DERATE_REASON,\n" +
                "               G.AMOUNT_DERATE,\n" +
                "               N.ORDER_ID,\n" +
                "               N.CUST_NAME,\n" +
                "               N.PSPT_TYPE_CODE,\n" +
                "               N.PSPT_NO,\n" +
                "               N.CUST_TAG,\n" +
                "               N.AUTH_TAG,\n" +
                "               OP.RECEIVER_NAME,\n" +
                "               OP.MOBILE_PHONE RECEIVER_PHONE,\n" +
                "               OP.FIX_PHONE\n" +
                "          FROM TF_B_ORDER               O,\n" +
                "               TF_F_MECHANT             M,\n" +
                "               TF_M_STAFF               S,\n" +
                "               TF_B_ORDER_GOODS_ATTRVAL A,\n" +
                "               TF_B_ORDER_GOODS         G,\n" +
                "               TF_B_ORDER_NETIN         N,\n" +
                "               TF_B_ORDER_POST  OP\n" +
                "         WHERE M.MERCHANT_ID = O.MERCHANT_ID \n" +
                "           AND O.ORDER_ID = OP.ORDER_ID(+)\n" +
                "           AND O.PARTITION_ID = OP.PARTITION_ID(+)\n" +
                "           AND A.ORDER_ID = O.ORDER_ID\n" +
                "           AND A.GOODS_ID = G.GOODS_ID\n" +
                "           AND G.ORDER_ID = O.ORDER_ID\n" +
                "           AND N.ORDER_ID = O.ORDER_ID\n" +
                "           AND A.GOODS_INST_ID = ?\n" +
                "           AND G.GOODS_ID = ?\n" +
                "           AND O.ORDER_ID = ?\n" +
                "           AND S.STAFF_ID = ? ";

        final Set<String> securetFieldsConfig = Sets.newHashSet("TF_B_ORDER_NETIN.PSPT_NO",
                "TF_B_ORDER_NETIN.CUST_NAME");

        OracleSensitiveFieldsParser visitorAdapter = OracleSensitiveFieldsParser
                .parseOracleSql(sql, securetFieldsConfig);

        Set<Integer> securetResultIndice = visitorAdapter.getSecureResultIndices();
        assertEquals(securetResultIndice, Sets.newHashSet(27, 29));
        Set<Integer> securetBindIndice = visitorAdapter.getSecureBindIndices();
        assertEquals(securetBindIndice, Sets.newHashSet());
    }


    @Test
    public void testQuery6() throws Exception {
        String sql = "SELECT TO_CHAR(O.ORDER_ID) ORDER_ID,\n" +
                "       O.ORDER_NO,\n" +
                "       TO_CHAR(O.ORDER_TIME, 'YYYY/MM/DD HH24:MI') AS ORDER_TIME,\n" +
                "       TRUNC(TO_NUMBER(SYSDATE - O.ORDER_TIME)) || '天' ||\n" +
                "       TRUNC(MOD(TO_NUMBER(SYSDATE - O.ORDER_TIME) * 24, 24)) || '时' ||\n" +
                "       TRUNC(MOD(TO_NUMBER(SYSDATE - O.ORDER_TIME) * 24 * 60, 60)) || '分' DELAY_TIME,\n" +
                "       O.ORDER_TOTAL_MONEY/1000 ORDER_TOTAL_MONEY,\n" +
                "       O.PAY_TYPE,\n" +
                "       O.CUST_ID,\n" +
                "       O.LOGIN_NAME,\n" +
                "       O.CUST_IP,\n" +
                "       O.CUST_IP_STR,\n" +
                "       DECODE(O.DELIVER_TYPE_CODE, '01', '快递', '03', '来联通自提') DELIVER_TYPE_NAME,\n" +
                "       SELFFETCH.SELFGET_ADDR_NAME,\n" +
                "       OG.GOODS_CTLG_CODE,\n" +
                "       T.PAY_TYPE_NAME,\n" +
                "       TC.TMPL_CTGR_RNAME,\n" +
                "       P.POST_ADDR,\n" +
                "       P.PROVINCE_CODE,\n" +
                "       P.CITY_CODE,\n" +
                "       P.DISTRICT_CODE,\n" +
                "       DECODE(OG.TMPL_ID, '10000011', '', '10000012', '', NE.PRE_NUM) ATTR_VAL_CODE,\n" +
                "       CITY.CITY_NAME AS NUM_AREA_NAME,\n" +
                "       O.PROVINCE_CODE ESS_PROVINCE_CODE,\n" +
                "       O.CITY_CODE AS NUM_AREA_CODE,\n" +
                "       DECODE(OG.TMPL_ID, '10000011', ORDERCITY.CITY_NAME, '10000012', ORDERCITY.CITY_NAME, '') WIRECARD_CITYNAME,\n" +
                "       OG.TMPL_ID,\n" +
                "       NVL(CUSTID.PARAM1_VALUE,1) as CUST_ORDER_NUM,\n" +
                "       NVL(CUSTIP.PARAM1_VALUE,1) as IP_ORDER_NUM,\n" +
                "       DECODE(BC.CUST_ID,o.cust_id,'黑名单客户','') AS BLKCUST_ORDER_DESC,\n" +
                "       CASE WHEN O.CONN_CHANNEL = '10' THEN DECODE(O.ORDER_FROM, 'TAOBAO', CA.PARA_CODE2 || '：', 'PAIPAI', CA.PARA_CODE2 || '：', '订单来源：')\n" +
                "            WHEN O.CONN_CHANNEL = '11' THEN '推广联盟：'\n" +
                "            END ORDER_FROM_PRE,\n" +
                "       CASE WHEN O.CONN_CHANNEL = '10' THEN DECODE(O.ORDER_FROM, 'TAOBAO', O.TID, 'PAIPAI', O.TID, CA.PARA_CODE2)\n" +
                "            WHEN O.CONN_CHANNEL = '11' THEN DECODE(LEAGUE.LEAGUE_NAME_ROOT, NULL , LEAGUE.LEAGUE_NAME, LEAGUE.LEAGUE_NAME_ROOT)\n" +
                "            END ORDER_FROM,\n" +
                "       NE.USER_TAG,\n" +
                "       OG.INVENTORY_TYPE\n" +
                "FROM TF_B_ORDER O \n" +
                "LEFT JOIN (SELECT W.ORDER_ID,\n" +
                "                  L.LEAGUE_NAME,\n" +
                "                  T.LEAGUE_NAME LEAGUE_NAME_ROOT\n" +
                "             FROM TF_B_ORDER_WM W, TF_F_LEAGUE L LEFT JOIN TF_F_LEAGUE T ON L.DEPEND_LEAGUE = T.LEAGUE_ID\n" +
                "            WHERE W.WM_P_ID = L.LEAGUE_ID) LEAGUE \n" +
                "                 ON O.ORDER_ID = LEAGUE.ORDER_ID\n" +
                "LEFT JOIN TF_B_ORDER_GOODSINS_ATVAL B ON (O.ORDER_ID = B.ORDER_ID AND O.PARTITION_ID = B.PARTITION_ID AND B.ATTR_CODE = 'A000025')\n" +
                "LEFT JOIN TF_M_CITY CITY ON CITY.CITY_CODE = B.ATTR_VAL_CODE\n" +
                "LEFT JOIN TF_M_CITY ORDERCITY ON ORDERCITY.CITY_CODE = O.CITY_CODE\n" +
                "LEFT JOIN TF_F_CUST_BLKLST BC ON (BC.CUST_ID = O.CUST_ID OR BC.LOGIN_IP = O.CUST_IP)\n" +
                "LEFT JOIN TF_B_ORDER_NETIN NE ON(O.ORDER_ID = NE.ORDER_ID AND O.PARTITION_ID = NE.PARTITION_ID)\n" +
                "LEFT JOIN TS_B_ORDERSTATS_CUSTID CUSTID ON O.CUST_ID = CUSTID.CUST_ID AND CUSTID.PARTITION_ID = MOD(O.CUST_ID, 100)\n" +
                "LEFT JOIN TS_B_ORDERSTATS_CUSTIP CUSTIP ON O.CUST_IP_STR = CUSTIP.CUST_IP_STR AND CUSTIP.PARTITION_ID = MOD(O.CUST_IP_STR, 100)\n" +
                "LEFT JOIN TD_B_COMMPARA CA ON(CA.PARAM_ATTR = '1009'  AND  CA.PARAM_CODE = 'MALL_ORDER_FROM' AND O.ORDER_FROM = CA.PARA_CODE1)\n" +
                "LEFT JOIN TF_B_ORDER_SELFFETCH_RELE SELFFETCH ON(O.ORDER_ID = SELFFETCH.ORDER_ID AND SELFFETCH.PARTITION_ID = O.PARTITION_ID AND SELFFETCH.STATE = '1') \n" +
                "     INNER JOIN TF_M_STAFF_BUSIAREA_RES AREAP ON(AREAP.STAFF_ID = ? AND AREAP.BUSIAREA_TYPE = '1'AND O.PROVINCE_CODE = AREAP.BUSIAREA_CODE),\n" +
                "         TD_B_PAYTYPE T,\n" +
                "         TF_B_ORDER_POST P,\n" +
                "         TF_B_ORDER_GOODSINS OG,\n" +
                "         TD_G_TMPL_CTGR TC \n" +
                " WHERE O.PAY_TYPE = T.PAY_TYPE_CODE\n" +
                "  AND O.ORDER_ID = P.ORDER_ID \n" +
                "  AND O.ORDER_ID = OG.ORDER_ID\n" +
                "  AND O.PARTITION_ID = P.PARTITION_ID \n" +
                "  AND O.PARTITION_ID = OG.PARTITION_ID\n" +
                "  AND OG.GOODS_CTLG_CODE = TC.TMPL_CTGR_CODE \n" +
                "  AND O.ORDER_STATE = ? \n" +
                "  AND O.MERCHANT_ID = ? \n" +
                "  AND O.CLAIM_FLAG = '0'\n" +
                "  AND P.PROVINCE_CODE = ?\n" +
                "  AND P.CITY_CODE = ?\n" +
                "  AND P.DISTRICT_CODE = ?\n" +
                "  AND OG.GOODS_CTLG_CODE = ?\n" +
                "  AND O.PAY_TYPE = ?\n" +
                "  AND O.ORDER_NO = ?\n" +
                "  AND NE.PSPT_NO= ?\n" +
                "ORDER BY O.ORDER_ID";
        final Set<String> securetFieldsConfig = Sets.newHashSet("TF_B_ORDER_NETIN.PSPT_NO",
                "TF_B_ORDER_NETIN.CUST_NAME");

        OracleSensitiveFieldsParser visitorAdapter = OracleSensitiveFieldsParser
                .parseOracleSql(sql, securetFieldsConfig);

        Set<Integer> securetResultIndice = visitorAdapter.getSecureResultIndices();
        assertEquals(securetResultIndice, Sets.newHashSet());
        Set<Integer> securetBindIndice = visitorAdapter.getSecureBindIndices();
        assertEquals(Sets.newHashSet(10), securetBindIndice);
    }

    @Test
    public void testQuery7() throws Exception {
        String sql = "SELECT TO_CHAR(A.ORDER_ID) ORDER_ID,              A.ORDER_NO,              TO_CHAR(A.ORDER_TIME, 'YYYY-MM-DD HH24:MI') ORDER_TIME,\n" +
                "               A.CUST_REMARK,                             A.INVOCE_TITLE,          A.PAY_STATE,    \n" +
                "               A.PAY_TYPE PAY_TYPE_CODE,                  BD.CONTACT_NAME,         BD.CONTACT_PHONE,\n" +
                "               BD.MERGE_ACCOUNT_PHONE,                    BD.ACCEPT_TYPE,           \n" +
                "               C.PARA_CODE2 PSPT_NAME,                    BD.PSPT_ADDR,            BD.PSPT_NO,\n" +
                "               E.GOODS_NAME,                              V.INVOCE_CONTENT,        SPEED.ATTR_VAL_NAME BROADBAND_SPEED,\n" +
                "               PRODU.ATTR_VAL_NAME PRODUCT_NAME,          BD.DETAIL_INS_ADDR,      BD.OFFICE_DIRECTION_NAME,\n" +
                "               BD.CUST_NAME,    PW.PAY_WAY_NAME PAY_WAY_NAME,             BD.BROADBAND_INS_ADDRCODE,         \n" +
                "               DECODE(BD.INSTALL_TYPE,'01','',BD.OFFICE_DIRECTION_NAME) OFFICE_DIRECTION_NAME,\n" +
                "               DECODE(BD.INSTALL_TYPE,'01',S.OFFICE_DIRECTION_CODE,'02',BD.OFFICE_DIRECTION_CODE) OFFICE_DIRECTION_CODE,\n" +
                "               BD.PSPT_TYPE_CODE,                         E.ACT_TYPE,              E.SALEACT_ID,\n" +
                "               E.VER_NO,                                  A.PROVINCE_CODE,         A.CITY_CODE,\n" +
                "               A.ORDER_STATE,                             BD.PROCESS_STATE,        E.GOODS_ID,\n" +
                "               PRODU.ATTR_VAL_CODE PRODUCT_CODE,          BD.INSTALL_TYPE,         BD.ACCESS_MODE,\n" +
                "               S.SHARE_NUMBER,                            S.AREA_CODE,\n" +
                "               DECODE(A.PAY_TYPE, '01','在线支付','02','上门收费')         PAY_TYPE_DESC,\n" +
                "               TO_CHAR(A.ORIGINAL_PRICE / 1000, 'FM9999999990.00')    ORIGINAL_PRICE,\n" +
                "               TO_CHAR(A.INCOME_MONEY / 1000, 'FM9999999990.00')      INCOME_MONEY\n" +
                "          FROM TF_B_ORDER                      A,\n" +
                "               TD_B_PAYWAY                     PW,\n" +
                "               TD_B_INVOICE                    V,\n" +
                "               TF_B_ORDER_GOODSINS             E,\n" +
                "               TF_B_ORDER_BROADBAND            BD,\n" +
                "               TF_B_ORDER_SHARENUM             S,\n" +
                "               (SELECT ORDER_ID, PARTITION_ID, ATTR_VAL_CODE, ATTR_VAL_NAME \n" +
                "                  FROM TF_B_ORDER_GOODSINS_ATVAL \n" +
                "                 WHERE ATTR_CODE='A000060')    SPEED,\n" +
                "                 \n" +
                "               (SELECT ORDER_ID, PARTITION_ID, ATTR_VAL_CODE, ATTR_VAL_NAME\n" +
                "                  FROM TF_B_ORDER_GOODSINS_ATVAL\n" +
                "                 WHERE ATTR_CODE='A000061')    PRODU,\n" +
                "                 \n" +
                "               (SELECT PARA_CODE1, PARA_CODE2 \n" +
                "                  FROM TD_B_COMMPARA \n" +
                "                 WHERE PARAM_ATTR = '1002')    C\n" +
                "         WHERE A.ORDER_ID = E.ORDER_ID\n" +
                "           AND A.PARTITION_ID = E.PARTITION_ID \n" +
                "           AND A.ORDER_ID = BD.ORDER_ID\n" +
                "           AND A.PARTITION_ID = BD.PARTITION_ID\n" +
                "           AND A.ORDER_ID = ?\n" +
                "           AND A.PARTITION_ID = MOD(?,100)\n" +
                "           \n" +
                "           AND A.PAY_WAY = PW.PAY_WAY_CODE(+)\n" +
                "           AND A.PAY_TYPE = PW.PAY_TYPE_CODE(+)\n" +
                "           AND A.INVO_CONT_CODE = V.INVO_CONT_CODE(+)\n" +
                "           AND A.ORDER_ID = SPEED.ORDER_ID(+)\n" +
                "           AND A.PARTITION_ID = SPEED.PARTITION_ID(+)\n" +
                "           AND A.ORDER_ID = PRODU.ORDER_ID(+)\n" +
                "           AND A.PARTITION_ID = PRODU.PARTITION_ID(+)\n" +
                "           AND BD.ORDER_ID = S.ORDER_ID(+)\n" +
                "           AND BD.PSPT_TYPE_CODE = C.PARA_CODE1(+)";
        final Set<String> securetFieldsConfig = Sets.newHashSet("TF_B_ORDER_BROADBAND.PSPT_NO");

        OracleSensitiveFieldsParser visitorAdapter = OracleSensitiveFieldsParser
                .parseOracleSql(sql, securetFieldsConfig);

        Set<Integer> securetResultIndice = visitorAdapter.getSecureResultIndices();
        assertEquals(securetResultIndice, Sets.newHashSet(14));
        Set<Integer> securetBindIndice = visitorAdapter.getSecureBindIndices();
        assertEquals(securetBindIndice, Sets.newHashSet());
    }

    String orderSql = "SELECT TO_CHAR(A.ORDER_ID) ORDER_ID,              A.ORDER_NO,              TO_CHAR(A.ORDER_TIME, 'YYYY-MM-DD HH24:MI') ORDER_TIME,\n" +
            "               A.CUST_REMARK,                             A.INVOCE_TITLE,          A.PAY_STATE,    \n" +
            "               A.PAY_TYPE PAY_TYPE_CODE,                  BD.CONTACT_NAME,         BD.CONTACT_PHONE,\n" +
            "               BD.MERGE_ACCOUNT_PHONE,                    BD.ACCEPT_TYPE,           \n" +
            "               C.PARA_CODE2 PSPT_NAME,                    BD.PSPT_ADDR,            BD.PSPT_NO,\n" +
            "               E.GOODS_NAME,                              V.INVOCE_CONTENT,        SPEED.ATTR_VAL_NAME BROADBAND_SPEED,\n" +
            "               PRODU.ATTR_VAL_NAME PRODUCT_NAME,          BD.DETAIL_INS_ADDR,      BD.OFFICE_DIRECTION_NAME,\n" +
            "               BD.CUST_NAME,    PW.PAY_WAY_NAME PAY_WAY_NAME,             BD.BROADBAND_INS_ADDRCODE,         \n" +
            "               DECODE(BD.INSTALL_TYPE,'01','',BD.OFFICE_DIRECTION_NAME) OFFICE_DIRECTION_NAME,\n" +
            "               DECODE(BD.INSTALL_TYPE,'01',S.OFFICE_DIRECTION_CODE,'02',BD.OFFICE_DIRECTION_CODE) OFFICE_DIRECTION_CODE,\n" +
            "               DECODE(BD.INSTALL_TYPE,'01',S.ADDRESS_TYPE, BD.ACCESS_MODE ) ACCESS_MODE,\n" +
            "               BD.PSPT_TYPE_CODE,                         E.ACT_TYPE,              E.SALEACT_ID,\n" +
            "               E.VER_NO,                                  A.PROVINCE_CODE,         A.CITY_CODE,\n" +
            "               A.ORDER_STATE,                             BD.PROCESS_STATE,        E.GOODS_ID,\n" +
            "               PRODU.ATTR_VAL_CODE PRODUCT_CODE,          BD.INSTALL_TYPE,         A.INVOCE_TITLE,\n" +
            "               S.SHARE_NUMBER,                            S.AREA_CODE,\n" +
            "               DECODE(A.PAY_TYPE, '01','在线支付','02','上门收费')      PAY_TYPE_DESC,\n" +
            "               TO_CHAR(A.ORIGINAL_PRICE / 1000, 'FM9999999990.00')    ORIGINAL_PRICE,\n" +
            "               TO_CHAR(A.INCOME_MONEY / 1000, 'FM9999999990.00')      INCOME_MONEY,\n" +
            "               TRIM(BD.CERT_CHECK_RSP_CODE) CERT_CHECK_RSP_CODE,                    \n" +
            "               TRIM(BD.CERT_CHECK_RSP_DETAIL) CERT_CHECK_RSP_DETAIL\n" +
            "          FROM TF_B_ORDER                      A,\n" +
            "               TD_B_PAYWAY                     PW,\n" +
            "               TD_B_INVOICE                    V,\n" +
            "               TF_B_ORDER_GOODSINS             E,\n" +
            "               TF_B_ORDER_BROADBAND            BD,\n" +
            "               TF_B_ORDER_SHARENUM             S,\n" +
            "               (SELECT ORDER_ID, PARTITION_ID, ATTR_VAL_CODE, ATTR_VAL_NAME \n" +
            "                  FROM TF_B_ORDER_GOODSINS_ATVAL \n" +
            "                 WHERE ATTR_CODE='A000060')    SPEED,\n" +
            "                 \n" +
            "               (SELECT ORDER_ID, PARTITION_ID, ATTR_VAL_CODE, ATTR_VAL_NAME\n" +
            "                  FROM TF_B_ORDER_GOODSINS_ATVAL\n" +
            "                 WHERE ATTR_CODE='A000061')    PRODU,\n" +
            "                 \n" +
            "               (SELECT PARA_CODE1, PARA_CODE2 \n" +
            "                  FROM TD_B_COMMPARA \n" +
            "                 WHERE PARAM_ATTR = '1002')    C\n" +
            "         WHERE A.ORDER_ID = E.ORDER_ID\n" +
            "           AND A.PARTITION_ID = E.PARTITION_ID \n" +
            "           AND A.ORDER_ID = BD.ORDER_ID\n" +
            "           AND A.PARTITION_ID = BD.PARTITION_ID\n" +
            "           AND A.ORDER_ID = ?\n" +
            "           AND A.PARTITION_ID = MOD(?,100)\n" +
            "           \n" +
            "           AND A.PAY_WAY = PW.PAY_WAY_CODE(+)\n" +
            "           AND A.PAY_TYPE = PW.PAY_TYPE_CODE(+)\n" +
            "           AND A.INVO_CONT_CODE = V.INVO_CONT_CODE(+)\n" +
            "           AND A.ORDER_ID = SPEED.ORDER_ID(+)\n" +
            "           AND A.PARTITION_ID = SPEED.PARTITION_ID(+)\n" +
            "           AND A.ORDER_ID = PRODU.ORDER_ID(+)\n" +
            "           AND A.PARTITION_ID = PRODU.PARTITION_ID(+)\n" +
            "           AND BD.ORDER_ID = S.ORDER_ID(+)\n" +
            "           AND BD.PSPT_TYPE_CODE = C.PARA_CODE1(+)";

    @Test
    public void testOrderSelect() {
        final Set<String> securetFieldsConfig = Sets.newHashSet("TF_B_ORDER_BROADBAND.PSPT_NO");

        OracleSensitiveFieldsParser visitorAdapter = OracleSensitiveFieldsParser
                .parseOracleSql(orderSql, securetFieldsConfig);

        Set<Integer> securetResultIndice = visitorAdapter.getSecureResultIndices();
        assertEquals(securetResultIndice, Sets.newHashSet(14));
        Set<Integer> securetBindIndice = visitorAdapter.getSecureBindIndices();
        assertEquals(securetBindIndice, Sets.newHashSet());
    }

    String trimxSql = "SELECT TRIMX(BD.CERT_CHECK_RSP_CODE) CERT_CHECK_RSP_CODE FROM TF_B_ORDER_BROADBAND BD";

    @Test
    public void testTrimxResult() {
        SQLStatementParser parser = new OracleStatementParser(trimxSql);
        parser.parseStatementList();
    }

    String trimSql = "SELECT TRIM(BD.CERT_CHECK_RSP_CODE) CERT_CHECK_RSP_CODE FROM TF_B_ORDER_BROADBAND BD";

    @Test//(expected = ParserException.class)
    // ParserException: syntax error, expect FROM, actual RPAREN CERT_CHECK_RSP_CODE
    public void testTrimResultBug() {
        SQLStatementParser parser = new OracleStatementParser(trimSql);
        parser.parseStatementList();
    }

    @Test
    public void testInsert1() {
        String sql = "insert into table1(a, b, c) values(?, ?, ?)";
        final Set<String> securetFieldsConfig = Sets.newHashSet("TABLE1.A", "TABLE1.B");
        OracleSensitiveFieldsParser visitorAdapter = OracleSensitiveFieldsParser
                .parseOracleSql(sql, securetFieldsConfig);

        Set<Integer> securetResultIndice = visitorAdapter.getSecureResultIndices();
        assertEquals(securetResultIndice, Sets.newHashSet());
        Set<Integer> securetBindIndice = visitorAdapter.getSecureBindIndices();
        assertEquals(securetBindIndice, Sets.newHashSet(1, 2));

        sql = "insert into table1(a, b, c) values(? || 'x' || ?, ?, ?)";
        visitorAdapter = OracleSensitiveFieldsParser.parseOracleSql(sql, securetFieldsConfig);

        securetResultIndice = visitorAdapter.getSecureResultIndices();
        assertEquals(securetResultIndice, Sets.newHashSet());
        securetBindIndice = visitorAdapter.getSecureBindIndices();
        assertEquals(securetBindIndice, Sets.newHashSet(3));
    }

    @Test
    public void testUpdate() {
        String sql = "update table1 t1 set t1.a = ?, t1.b = ?, t1.c = ?";
        final Set<String> securetFieldsConfig = Sets.newHashSet("TABLE1.A", "TABLE1.B");
        OracleSensitiveFieldsParser visitorAdapter = OracleSensitiveFieldsParser
                .parseOracleSql(sql, securetFieldsConfig);

        Set<Integer> securetResultIndice = visitorAdapter.getSecureResultIndices();
        assertEquals(securetResultIndice, Sets.newHashSet());
        Set<Integer> securetBindIndice = visitorAdapter.getSecureBindIndices();
        assertEquals(securetBindIndice, Sets.newHashSet(1, 2));

        sql = "update table1  set a = ? || 'X' || ?, b = ?, c = ?";
        visitorAdapter = OracleSensitiveFieldsParser.parseOracleSql(sql, securetFieldsConfig);

        securetResultIndice = visitorAdapter.getSecureResultIndices();
        assertEquals(securetResultIndice, Sets.newHashSet());
        securetBindIndice = visitorAdapter.getSecureBindIndices();
        assertEquals(securetBindIndice, Sets.newHashSet(3));
    }

    @Test
    public void testMerge() {
        String sql = "MERGE INTO copy_emp c " +
                "USING employees e " +
                "ON (c.employee_id=e.employee_id) " +
                "WHEN MATCHED THEN " +
                "UPDATE SET " +
                "c.first_name=?, " +
                "c.last_name=e.last_name, " +
                "c.department_id=? " +
                "WHEN NOT MATCHED THEN " +
                "INSERT(employee_id,first_name,last_name," +
                "email,phone_number,hire_date,job_id," +
                "salary,commission_pct,manager_id,department_id) " +
                "VALUES(?, ?, ?," +
                "e.email,e.phone_number,e.hire_date,e.job_id, " +
                "e.salary,e.commission_pct,e.manager_id,?)";

        final Set<String> securetFieldsConfig = Sets.newHashSet("COPY_EMP.FIRST_NAME", "COPY_EMP.DEPARTMENT_ID");
        OracleSensitiveFieldsParser visitorAdapter = OracleSensitiveFieldsParser
                .parseOracleSql(sql, securetFieldsConfig);

        Set<Integer> securetResultIndice = visitorAdapter.getSecureResultIndices();
        assertEquals(securetResultIndice, Sets.newHashSet());
        Set<Integer> securetBindIndice = visitorAdapter.getSecureBindIndices();
        assertEquals(securetBindIndice, Sets.newHashSet(1, 2, 4, 6));
    }

    @Test
    public void testProcedure() {
        String sql = "{call abc.myproc(?,?,?)}";
        final Set<String> securetFieldsConfig = Sets.newHashSet("ABC.MYPROC.2");
        OracleSensitiveFieldsParser visitorAdapter = OracleSensitiveFieldsParser
                .parseOracleSql(sql, securetFieldsConfig);

        Set<Integer> securetResultIndice = visitorAdapter.getSecureResultIndices();
        assertEquals(securetResultIndice, Sets.newHashSet());
        Set<Integer> securetBindIndice = visitorAdapter.getSecureBindIndices();
        assertEquals(securetBindIndice, Sets.newHashSet(2));

        sql = "{call abc.myproc(? || 'x' || ?,?,?)}";
        visitorAdapter = OracleSensitiveFieldsParser.parseOracleSql(sql, securetFieldsConfig);

        securetResultIndice = visitorAdapter.getSecureResultIndices();
        assertEquals(securetResultIndice, Sets.newHashSet());
        securetBindIndice = visitorAdapter.getSecureBindIndices();
        assertEquals(securetBindIndice, Sets.newHashSet(3));
    }

    String hintSql = "/*** bind(1,2) result(1) ***/ select 1 from dual";

    @Test
    public void testHintSql() throws Exception {
        OracleSensitiveFieldsParser visitor = OracleSensitiveFieldsParser
                .parseOracleSql(hintSql, Sets.newHashSet(""));

        assertEquals(Sets.newHashSet(1, 2), visitor.getSecureBindIndices());
        assertEquals(Sets.newHashSet(1), visitor.getSecureResultIndices());
        assertEquals(" select 1 from dual", visitor.getSql());
    }


    String funcSql = "\t\tSELECT A.BESPEAK_ID as \"bespeakID\",\n" +
            "\t\t       A.CUST_NAME as \"custname\",\n" +
            "\t\t\t\tA.PCARD_CODE as \"pcardcode\",\n" +
            "\t\t\t\tA.TELEPHONE as \"telephone\",\n" +
            "\t\t\t\tB.PROVINCE_NAME as \"provincecode\",\n" +
            "\t\t\t\tC.CITY_NAME as \"citycode\",\n" +
            "\t\t\t\tDECODE(A.MODEL_CODE,'00','16G','01','32G','02','64G',' ') as \"modelcode\",\n" +
            "\t\t\t\tDECODE(A.COLOR_CODE,'00','白色','01','黑色',' ') as \"colorcode\"\n" +
            "\t\tFROM TF_B_BESPEAK_REG A,\n" +
            "\t\t\t TF_M_PROVINCE B,\n" +
            "\t\t\t TF_M_CITY C\n" +
            "\t\tWHERE A.PROVINCECODE = B.PROVINCE_CODE\n" +
            "\t\t  AND A.CITYCODE = C.CITY_CODE\n" +
            "         AND A.PCARD_CODE = upper(?)\n" +
            "         AND A.TELEPHONE = ?";


    @Test
    public void testFuncSql() throws Exception {
        OracleSensitiveFieldsParser visitor = OracleSensitiveFieldsParser
                .parseOracleSql(funcSql, Sets.newHashSet("TF_B_BESPEAK_REG.PCARD_CODE"));

        assertEquals(Sets.newHashSet(1), visitor.getSecureBindIndices());
        assertEquals(Sets.newHashSet(3), visitor.getSecureResultIndices());
        assertEquals(funcSql, visitor.getSql());
    }

    String subQuery = "SELECT O.ORDER_ID,\n" +
            "                   O.POST_FEE,\n" +
            "                   O.PROVINCE_CODE,\n" +
            "                   O.CITY_CODE,\n" +
            "                   O.PAY_TYPE,\n" +
            "                   O.PAY_WAY,\n" +
            "                   O.MERCHANT_ID,\n" +
            "                   O.INCOME_MONEY,\n" +
            "                   O.INCOME_MONEY PAYFEE,\n" +
            "                   G.GOODS_NAME,\n" +
            "                   T.UPAY_PLATFORM_CODE,\n" +
            "                   T.TMPL_NAME,\n" +
            "                   O.CUST_IP,\n" +
            "                   O.LOGIN_NAME,\n" +
            "                   TO_CHAR(O.CREATE_TIME, 'yyyyMMddHHmmss') CREATE_TIME,\n" +
            "                   PT.RECEIVER_NAME,\n" +
            "                   PT.MOBILE_PHONE,\n" +
            "                   PT.FIX_PHONE,\n" +
            "                   PT.PROVINCE_CODE,\n" +
            "                   PT.PROVINCE_NAME,\n" +
            "                   PT.CITY_CODE,\n" +
            "                   PT.CITY_NAME,\n" +
            "                   PT.POST_ADDR,\n" +
            "                   OI.INSTALMENT_BANK_CODE,\n" +
            "                   OI.INSTALMENT_TERM,\n" +
            "                   N.CUST_NAME AS NETIN_CUST_NAME,\n" +
            "                   N.PSPT_NO AS PSPT_NO,\n" +
            "                   (SELECT CP.PARA_CODE2\n" +
            "                      FROM TD_B_COMMPARA CP\n" +
            "                     WHERE CP.PARAM_ATTR = '1002'\n" +
            "                       AND CP.PARAM_CODE = ?\n" +
            "                       AND CP.PARA_CODE1 = N.PSPT_TYPE_CODE) PSPT_TYPE_NAME,\n" +
            "                   (SELECT CP.PARA_CODE1\n" +
            "                      FROM TD_B_COMMPARA CP\n" +
            "                     WHERE CP.PARAM_ATTR = '1003'\n" +
            "                       AND CP.PARAM_CODE = ?\n" +
            "                       AND CP.PARA_CODE1 = N.PSPT_TYPE_CODE) PSPT_TYPE_NAME2,\n" +
            "                   N.PSPT_TYPE_CODE AS PSPT_TYPE_CODE\n" +
            "              FROM TF_B_ORDER            O,\n" +
            "                   TF_B_ORDER_GOODSINS   OG,\n" +
            "                   TF_G_GOODS            G,\n" +
            "                   TF_G_TEMPLATE         T,\n" +
            "                   TF_B_ORDER_INSTALMENT OI,\n" +
            "                   TF_B_ORDER_NETIN      N,\n" +
            "                   (SELECT TP.ORDER_ID,\n" +
            "                           TP.RECEIVER_NAME, \n" +
            "                           TP.MOBILE_PHONE, \n" +
            "                           TP.FIX_PHONE, \n" +
            "                           TP.PROVINCE_CODE, \n" +
            "                           P.PROVINCE_NAME, \n" +
            "                           TP.CITY_CODE, \n" +
            "                           C.CITY_NAME, \n" +
            "                           TP.POST_ADDR\n" +
            "                      FROM TF_B_ORDER_POST TP, TF_M_WEB_CITY C, TF_M_WEB_PROVINCE P\n" +
            "                     WHERE TP.PROVINCE_CODE = C.PROVINCE_CODE\n" +
            "                       AND TP.PROVINCE_CODE = ?\n" +
            "                       AND TP.CITY_CODE = ?\n" +
            "                       AND TP.PROVINCE_CODE = P.PROVINCE_CODE) PT, \n" +
            "                   (SELECT TP.ORDER_ID\n" +
            "                      FROM TF_B_ORDER_POST TP, TF_M_WEB_CITY C, TF_M_WEB_PROVINCE P\n" +
            "                     WHERE TP.PROVINCE_CODE = C.PROVINCE_CODE\n" +
            "                       AND TP.PROVINCE_CODE = ?\n" +
            "                       AND TP.MOBILE_PHONE = ?\n" +
            "                       AND TP.CITY_CODE = ?\n" +
            "                       AND TP.PROVINCE_CODE = P.PROVINCE_CODE) PT2\n" +
            "             WHERE O.ORDER_ID = OG.ORDER_ID\n" +
            "               AND O.PARTITION_ID = OG.PARTITION_ID\n" +
            "               AND O.PARTITION_ID = MOD(?, 100)\n" +
            "               AND O.ORDER_ID = ?\n" +
            "               AND OG.GOODS_ID = G.GOODS_ID\n" +
            "               AND G.TMPL_ID = T.TMPL_ID\n" +
            "               AND PT.ORDER_ID(+) = O.ORDER_ID\n" +
            "               AND OI.ORDER_ID(+) = O.ORDER_ID\n" +
            "               AND OI.PARTITION_ID(+) = O.PARTITION_ID\n" +
            "               AND N.ORDER_ID(+) = O.ORDER_ID\n" +
            "               AND N.PARTITION_ID(+) = O.PARTITION_ID";

    @Test
    public void testSubQueryIn() throws Exception {
        OracleSensitiveFieldsParser visitor = OracleSensitiveFieldsParser
                .parseOracleSql(subQuery, Sets.newHashSet("TF_B_ORDER_POST.POST_ADDR", "TD_B_COMMPARA.PARA_CODE2",
                        "TF_B_ORDER.ORDER_ID", "TF_B_ORDER_POST.CITY_CODE", "TD_B_COMMPARA.PARAM_CODE"));

        assertEquals(Sets.newHashSet(1, 2, 4, 7, 9), visitor.getSecureBindIndices());
        assertEquals(Sets.newHashSet(1, 21, 23, 28), visitor.getSecureResultIndices());
        assertEquals(subQuery, visitor.getSql());
    }

    String myPagEQL = "select * from (" +
            "select t1.*, rownum rn from (" +
            "select a,b from table t where t.b = ?) t1" +
            " where rownum < ?" +
            ") where rn > ? ";

    @Test
    public void testMyPage() throws Exception {
        OracleSensitiveFieldsParser visitor = OracleSensitiveFieldsParser
                .parseOracleSql(myPagEQL, Sets.newHashSet("TABLE.B"));

        assertEquals(Sets.newHashSet(1), visitor.getSecureBindIndices());
        assertEquals(Sets.newHashSet(2), visitor.getSecureResultIndices());
        assertEquals(myPagEQL, visitor.getSql());
    }

    String starSql = "select 'a', t.* from (select a,b,c from tab) t ";

    @Test
    public void testStartSql() throws Exception {
        OracleSensitiveFieldsParser visitor = OracleSensitiveFieldsParser
                .parseOracleSql(starSql, Sets.newHashSet("TAB.B"));

        assertEquals(Sets.newHashSet(), visitor.getSecureBindIndices());
        assertEquals(Sets.newHashSet(3), visitor.getSecureResultIndices());
        assertEquals(starSql, visitor.getSql());
    }


    String mergEQL = "merge INTO tf_b_taobao_netin \n" +
            "USING dual \n" +
            "ON (tid = ? AND partition_id= MOD(?, 100)) \n" +
            "WHEN matched THEN \n" +
            "  UPDATE SET operator_state = ?, \n" +
            "             err_code = ?, \n" +
            "             err_desc = ?, \n" +
            "             operator_date = SYSDATE, \n" +
            "             num_expire_type = ?, \n" +
            "             num_expire_time = To_date(?, 'YYYYMMDDHH24MISS') \n" +
            "WHEN NOT matched THEN \n" +
            "  INSERT (tid, \n" +
            "          partition_id, \n" +
            "          operator_date, \n" +
            "          operator_state, \n" +
            "          cust_name, \n" +
            "          pspt_type_code, \n" +
            "          pspt_no, \n" +
            "          user_tag, \n" +
            "          account_flag, \n" +
            "          num_proc_id, \n" +
            "          num_expire_type, \n" +
            "          num_expire_time, \n" +
            "          pro_key_mode, \n" +
            "          pspt_addr, \n" +
            "          pre_num, \n" +
            "          err_code, \n" +
            "          err_desc) \n" +
            "  VALUES (?, \n" +
            "          MOD(?, 100), \n" +
            "          SYSDATE, \n" +
            "          ?, \n" +
            "          ?, \n" +
            "          ?, \n" +
            "          ?, \n" +
            "          '1', \n" +
            "          '0', \n" +
            "          ?, \n" +
            "          ?, \n" +
            "          To_date(?, 'YYYYMMDDHH24MISS'), \n" +
            "          ?, \n" +
            "          ?, \n" +
            "          ?, \n" +
            "          ?, \n" +
            "          ?) ";

    @Test
    public void testMergEQL() throws Exception {
        OracleSensitiveFieldsParser visitor = OracleSensitiveFieldsParser
                .parseOracleSql(mergEQL, Sets.newHashSet("TF_B_TAOBAO_NETIN.PSPT_NO"));

        assertEquals(Sets.newHashSet(13), visitor.getSecureBindIndices());
        assertEquals(Sets.newHashSet(), visitor.getSecureResultIndices());
        assertEquals(mergEQL, visitor.getSql());
    }

    String zjSql = "SELECT TO_CHAR(T.PRO_ID)                               \"proID\", \n" +
            "       T.CUST_NAME                                     \"custName\", \n" +
            "       T.PSPT_NO                                       \"pstCode\", \n" +
            "       T.GOODS_ID                                      \"goodsID\", \n" +
            "       T.PSPT_ADDR                                     \"pstAddress\", \n" +
            "       T.LINK_ADDR                                     \"linkAddress\", \n" +
            "       T.LINK_PHONE                                    \"linkPhone\", \n" +
            "       T.ACTIVITY_ID                                   \"activity\", \n" +
            "       TO_CHAR(T.NUM_FRONT_TIME, 'YYYY-MM-DD HH24:MI') \"frontTime\", \n" +
            "       G.GOODS_NAME                                    \"goodsName\", \n" +
            "       T.PRE_NUM                                       \"number\", \n" +
            "       T.PRODUCT_ID, \n" +
            "       T.INNOUT_ID                                     \"innoutID\", \n" +
            "       T.STATE                                         \"orderState\", \n" +
            "       T.CITY_NAME                                     \"cityName\", \n" +
            "       T.PRIVILEGE_PACK                                \"privilegePackCode\", \n" +
            "       TO_CHAR(T.NUM_EXPIRE_TIME, 'YYYY-MM-DD')        \"expireTime\" \n" +
            "FROM   TF_B_BESPEAK_INFO T, \n" +
            "       TF_G_GOODS G \n" +
            "WHERE  T.SYSCODE = 'EMAL' \n" +
            "       AND T.GOODS_ID = G.GOODS_ID \n" +
            "       AND G.PARTITION_ID = MOD(G.GOODS_ID, 100) \n" +
            "       AND T.STATE IN ( '0', '1', '3' ) \n" +
            "       AND T.PRO_ID = ? \n" +
            "       AND ROWNUM < 2 ";

    @Test
    public void testZjSql() throws Exception {
        OracleSensitiveFieldsParser visitor = OracleSensitiveFieldsParser
                .parseOracleSql(zjSql, Sets.newHashSet("TF_B_BESPEAK_INFO.PSPT_NO"));

        assertEquals(Sets.newHashSet(), visitor.getSecureBindIndices());
        assertEquals(Sets.newHashSet(3), visitor.getSecureResultIndices());
        assertEquals(zjSql, visitor.getSql());
    }

    String whcSql = "SELECT DISTINCT GB.GROUP_BRANCH_NAME GROUP_BRANCH_NAME,\n" +
            "        TO_CHAR(O.ORDER_ID) ORDER_ID,\n" +
            "        O.ORDER_NO ORDER_NO,\n" +
            "        O.ORDER_TIME ORDER_TIME,\n" +
            "        DECODE(O.ORDER_STATE,   'A0','待支付',\n" +
            "                                'B0','待分配',\n" +
            "                                'C0','待处理',\n" +
            "                                'C1','处理中',\n" +
            "                                'AX','已取消',\n" +
            "                                '00','已归档',\n" +
            "                                     'ERROR') ORDER_STATE,\n" +
            "        TO_CHAR(O.ORDER_TIME,'YYYY-MM-DD HH24:MI') ORDER_TIME_STR,\n" +
            "        (\n" +
            "         TO_CHAR(FLOOR(TRUNC(ROUND(TO_NUMBER(SYSDATE - O.ORDER_TIME) * 24 * 60 * 60) /(60 * 60)) / 24)) || '天' ||\n" +
            "         TO_CHAR(MOD(TRUNC(ROUND(TO_NUMBER(SYSDATE - O.ORDER_TIME) * 24 * 60 * 60) /(60 * 60)),24)) || '小时'\n" +
            "        ) AS INTERVAL,\n" +
            "        TO_CHAR(NVL(O.ORDER_TOTAL_MONEY,0)/1000,'FM9999999990.00') ORDER_TOTAL_MONEY,\n" +
            "        OP.RECEIVER_NAME || ' ' || NVL(OP.MOBILE_PHONE,OP.FIX_PHONE) CONTACT,\n" +
            "        OP.PROVINCE_CODE PROVINCE_CODE,\n" +
            "        OP.CITY_CODE CITY_CODE,\n" +
            "        OP.DISTRICT_CODE DISTRICT_CODE,\n" +
            "        OP.POST_ADDR POST_ADDR\n" +
            "  FROM TF_B_ORDER O,\n" +
            "        TF_B_ORDER_CCS OC,\n" +
            "        TF_F_GROUP_BRANCH GB,\n" +
            "        TF_B_ORDER_GOODSINS OG,\n" +
            "        TF_B_ORDER_POST OP,\n" +
            "        TF_B_ORDER_NETIN N\n" +
            "  WHERE O.ORDER_FROM='CCS'\n" +
            "  AND   O.ORDER_ID = OC.ORDER_ID \n" +
            "  AND   OC.GROUP_BRANCH_ID = GB.GROUP_BRANCH_ID\n" +
            "  AND   O.ORDER_ID =        OG.ORDER_ID\n" +
            "  AND   O.ORDER_ID =        OP.ORDER_ID\n" +
            "  AND   O.ORDER_ID =        N.ORDER_ID(+)";

    @Test
    public void testWhcIntervalAliasSql() throws Exception {
        OracleSensitiveFieldsParser visitor = OracleSensitiveFieldsParser
                .parseOracleSql(whcSql, Sets.newHashSet("TF_B_ORDER_POST.POST_ADDR"));

        assertEquals(Sets.newHashSet(), visitor.getSecureBindIndices());
        assertEquals(Sets.newHashSet(13), visitor.getSecureResultIndices());
        assertEquals(whcSql, visitor.getSql());
    }


    String intervalSql = "SELECT 1 AS INTERVAL FROM DUAL";

    @Test
    public void testInterval() throws Exception {
        new OracleStatementParser(intervalSql).parseStatementList();
    }

    String psptNoSql = "SELECT ROW_.*,\n" +
            "       ROWNUM RN\n" +
            "FROM\n" +
            "  (SELECT TO_CHAR(OS.ORDER_ID) ORDER_ID,\n" +
            "          OS.Order_No,\n" +
            "          TO_CHAR(OS.ORDER_TIME,'YYYY/MM/DD HH24:MI') ORDER_TIME,\n" +
            "          OS.PAY_TYPE,\n" +
            "          CASE\n" +
            "              WHEN OS.CONN_CHANNEL = '10' THEN DECODE(OS.ORDER_FROM, 'TAOBAO', DECODE(OS.ORDER_SOURCE_FLAG ,'0', '总部淘宝：', '1', PROVINCE.PROVINCE_NAME || '淘宝：', '淘宝商城：'), 'PAIPAI', CA.PARA_CODE2 || '：', '订单来源：')\n" +
            "              WHEN OS.CONN_CHANNEL = '11' THEN '推广联盟：'\n" +
            "          END ORDER_FROM_PRE,\n" +
            "          CASE\n" +
            "              WHEN OS.CONN_CHANNEL = '10' THEN DECODE(OS.ORDER_FROM, 'TAOBAO', OS.TID, 'PAIPAI', OS.TID, CA.PARA_CODE2)\n" +
            "              WHEN OS.CONN_CHANNEL = '11' THEN DECODE(LEAGUE.LEAGUE_NAME_ROOT, NULL , LEAGUE.LEAGUE_NAME, LEAGUE.LEAGUE_NAME_ROOT)\n" +
            "          END ORDER_FROM,\n" +
            "          OS.ORDER_STATE,\n" +
            "          OS.ORDER_TOTAL_MONEY/1000 ORDER_TOTAL_MONEY,\n" +
            "          DECODE(OS.DELIVER_TYPE_CODE, '01', '快递', '03', '来联通自提') DELIVER_TYPE_NAME,\n" +
            "          SELFFETCH.SELFGET_ADDR_NAME,\n" +
            "          P.RECEIVER_NAME,\n" +
            "          P.MOBILE_PHONE,\n" +
            "          P.POST_ADDR,\n" +
            "          P.PROVINCE_CODE,\n" +
            "          P.CITY_CODE,\n" +
            "          P.DISTRICT_CODE,\n" +
            "          PAY.PARA_CODE2 PAY_TYPE_NAME,\n" +
            "          ORDERSTATE.PARA_CODE2 ORDER_STATE_NAME,\n" +
            "          OS.CANCEL_TAG,\n" +
            "          DECODE(OS.CANCEL_TAG, '0','未退单', '1','已退单', 'A','申请退单', 'B','申请退单通过', 'C','正在退单', 'D','退款失败','F','退单驳回',OS.CANCEL_TAG) CANCEL_TAG_NAME,\n" +
            "          OS.PAY_STATE,\n" +
            "          DECODE(OS.PAY_STATE, '0','未支付', '1','已支付', '2','已退款', 'A','申请退款', 'B','申请退款通过', 'C','正在退款', 'D','退款失败',OS.PAY_STATE) PAY_STATE_NAME,\n" +
            "          NE.USER_TAG,\n" +
            "          OGS.INVENTORY_TYPE,\n" +
            "          BH.GROUP_BRANCH_NAME,\n" +
            "          CCS.CHECK_STATUS,\n" +
            "          OS.ORDER_FROM ORDER_FROM_CODE,\n" +
            "          DECODE(CCS.CHECK_STATUS, '00', '不需要审核', '01', '待审核', '02', '审核通过', '03', '审核未通过') CHECK_STATUS_DESC\n" +
            "   FROM TF_B_ORDER OS\n" +
            "   LEFT JOIN\n" +
            "     (SELECT W.ORDER_ID,\n" +
            "             L.LEAGUE_NAME,\n" +
            "             T.LEAGUE_NAME LEAGUE_NAME_ROOT\n" +
            "      FROM TF_B_ORDER_WM W,\n" +
            "           TF_F_LEAGUE L\n" +
            "      LEFT JOIN TF_F_LEAGUE T ON L.DEPEND_LEAGUE = T.LEAGUE_ID\n" +
            "      WHERE W.WM_P_ID = L.LEAGUE_ID) LEAGUE ON OS.ORDER_ID = LEAGUE.ORDER_ID\n" +
            "   LEFT JOIN TD_B_COMMPARA CA ON(CA.PARAM_ATTR='1009'\n" +
            "                                 AND CA.PARAM_CODE='MALL_ORDER_FROM'\n" +
            "                                 AND OS.ORDER_FROM=CA.PARA_CODE1)\n" +
            "   LEFT JOIN TF_M_PROVINCE PROVINCE ON(OS.PROVINCE_CODE=PROVINCE.PROVINCE_CODE)\n" +
            "   LEFT JOIN TF_B_ORDER_NETIN NE ON (OS.ORDER_ID = NE.ORDER_ID\n" +
            "                                     AND OS.PARTITION_ID = NE.PARTITION_ID)\n" +
            "   LEFT JOIN TF_B_ORDER_SELFFETCH_RELE SELFFETCH ON(OS.ORDER_ID = SELFFETCH.ORDER_ID\n" +
            "                                                    AND SELFFETCH.PARTITION_ID = OS.PARTITION_ID\n" +
            "                                                    AND SELFFETCH.STATE = '1')\n" +
            "   LEFT JOIN TF_B_ORDER_CCS CCS ON (OS.ORDER_ID = CCS.ORDER_ID\n" +
            "                                    AND OS.PARTITION_ID = CCS.PARTITION_ID)\n" +
            "   LEFT JOIN TF_F_GROUP_BRANCH BH ON(CCS.GROUP_BRANCH_ID = BH.GROUP_BRANCH_ID)\n" +
            "   INNER JOIN TF_B_ORDER_GOODSINS_ATVAL GA ON (OS.ORDER_ID = GA.ORDER_ID\n" +
            "                                               AND OS.PARTITION_ID = GA.PARTITION_ID\n" +
            "                                               AND GA.ATTR_CODE = 'A000017'\n" +
            "                                               AND GA.ATTR_VAL_CODE = ?)\n" +
            "   INNER JOIN TF_M_STAFF_BUSIAREA_RES AREAC ON(AREAC.STAFF_ID = ?\n" +
            "                                               AND AREAC.BUSIAREA_TYPE = '2'\n" +
            "                                               AND OS.CITY_CODE = AREAC.BUSIAREA_CODE) , TF_B_ORDER_POST P,\n" +
            "                                                                                         TD_B_COMMPARA PAY,\n" +
            "                                                                                         TD_B_COMMPARA ORDERSTATE,\n" +
            "                                                                                         TF_B_ORDER_GOODSINS OGS\n" +
            "   WHERE OS.ORDER_ID = P.ORDER_ID\n" +
            "     AND OS.ORDER_ID = OGS.ORDER_ID\n" +
            "     AND OS.PARTITION_ID = P.PARTITION_ID\n" +
            "     AND OS.PARTITION_ID = OGS.PARTITION_ID\n" +
            "     AND PAY.PARAM_ATTR = ?\n" +
            "     AND PAY.PARAM_CODE = ?\n" +
            "     AND PAY.PARA_CODE1 = OS.PAY_TYPE\n" +
            "     AND ORDERSTATE.PARAM_ATTR = ?\n" +
            "     AND ORDERSTATE.PARAM_CODE = ?\n" +
            "     AND ORDERSTATE.PARA_CODE1 = OS.ORDER_STATE\n" +
            "     AND OS.PROCESS_MERCHANT_ID= ?\n" +
            "     AND OS.ORDER_NO = ?\n" +
            "     AND NE.PSPT_NO = ?\n" +
            "     AND OS.PROVINCE_CODE = ?\n" +
            "   ORDER BY OS.ORDER_TIME,\n" +
            "            OS.ORDER_ID) ROW_\n" +
            "WHERE ROWNUM <= 5";

    @Test
    public void testPsptNoSql() throws Exception {
        OracleSensitiveFieldsParser visitor = OracleSensitiveFieldsParser
                .parseOracleSql(psptNoSql, Sets.newHashSet("TF_B_ORDER_NETIN.PSPT_NO"));

        assertEquals(Sets.newHashSet(9), visitor.getSecureBindIndices());
        assertEquals(Sets.newHashSet(), visitor.getSecureResultIndices());
        assertEquals(psptNoSql, visitor.getSql());
    }

    String menuSql = "SELECT A.STAFF_ID,\n" +
            "       M.MENU_ID,\n" +
            "       M.MENU_CODE,\n" +
            "       M.PARENT_MENU_ID,\n" +
            "       M.RIGHT_CODE,\n" +
            "       M.MENU_NAME MENU_NAME,\n" +
            "       M.MENU_URL,\n" +
            "       M.MENU_SORT,\n" +
            "       M.TARGET_ATTR,\n" +
            "       M.AUTHKEY_TAG,\n" +
            "       M.PROJECT_CODE\n" +
            "FROM TD_S_MENU M,\n" +
            "  (SELECT ME.MENU_ID,\n" +
            "          SR.STAFF_ID\n" +
            "   FROM TF_M_STAFF_ROLE SR,\n" +
            "        TF_M_ROLE_FUNCRIGHT RF,\n" +
            "        TD_M_FUNCRIGHT FU,\n" +
            "        TD_S_MENU ME\n" +
            "   WHERE SR.STAFF_ID = ?\n" +
            "     AND SR.ROLE_ID = RF.ROLE_ID\n" +
            "     AND RF.RIGHT_CODE = FU.RIGHT_CODE\n" +
            "     AND RF.RIGHT_CODE = ME.RIGHT_CODE\n" +
            "     AND FU.RIGHT_STATE = '1'\n" +
            "   GROUP BY ME.MENU_ID,\n" +
            "            SR.STAFF_ID\n" +
            "   UNION SELECT ME.PARENT_MENU_ID,\n" +
            "                SR.STAFF_ID\n" +
            "   FROM TF_M_STAFF_ROLE SR,\n" +
            "        TF_M_ROLE_FUNCRIGHT RF,\n" +
            "        TD_M_FUNCRIGHT FU,\n" +
            "        TD_S_MENU ME\n" +
            "   WHERE SR.STAFF_ID = ?\n" +
            "     AND SR.ROLE_ID = RF.ROLE_ID\n" +
            "     AND RF.RIGHT_CODE = FU.RIGHT_CODE\n" +
            "     AND RF.RIGHT_CODE = ME.RIGHT_CODE\n" +
            "     AND FU.RIGHT_STATE = '1'\n" +
            "   GROUP BY ME.PARENT_MENU_ID,\n" +
            "            SR.STAFF_ID) A\n" +
            "WHERE M.MENU_ID = A.MENU_ID\n" +
            "ORDER BY M.MENU_SORT,\n" +
            "         M.MENU_ID ASC";

    @Test
    public void testMenuSql() throws Exception {
        OracleSensitiveFieldsParser visitor = OracleSensitiveFieldsParser
                .parseOracleSql(menuSql, Sets.newHashSet("TF_B_ORDER_NETIN.PSPT_NO"));

        assertNull(visitor);
    }

    String aliasSql = " SELECT TO_CHAR(T.PRO_ID) \"proID\",\n" +
            "               T.CUST_NAME \"custName\",        \n" +
            "               T.PSPT_NO \"pstCode\",\n" +
            "               T.PSPT_ADDR \"pstAddress\",\n" +
            "               T.LINK_ADDR \"linkAddress\",\n" +
            "               T.LINK_PHONE \"linkPhone\",\n" +
            "               T.ACTIVITY_ID \"activity\",\n" +
            "               TO_CHAR(T.NUM_FRONT_TIME, 'YYYY-MM-DD HH24:MI') \"frontTime\",\n" +
            "               G.GOODS_NAME \"goodsName\",\n" +
            "               T.PRE_NUM \"number\",\n" +
            "               T.PRODUCT_ID,\n" +
            "               T.INNOUT_ID \"innoutID\",\n" +
            "               T.STATE \"orderState\",\n" +
            "               T.CITY_NAME \"cityName\",\n" +
            "               TO_CHAR(T.NUM_EXPIRE_TIME, 'YYYY-MM-DD') \"expireTime\"\n" +
            "        FROM TF_B_BESPEAK_INFO T, TF_G_GOODS G\n" +
            "        WHERE T.SYSCODE = 'EMAL'\n" +
            "        AND T.GOODS_ID = G.GOODS_ID\n" +
            "        AND G.PARTITION_ID = MOD(G.GOODS_ID, 100)\n" +
            "        AND T.STATE IN ('0', '1', '3')\n" +
            "        AND T.PRO_ID = ?\n" +
            "        AND ROWNUM < 2";

    @Test
    public void testAlias() throws Exception {
        OracleSensitiveFieldsParser visitor = OracleSensitiveFieldsParser
                .parseOracleSql(aliasSql, Sets.newHashSet("TF_B_BESPEAK_INFO.LINK_ADDR", "TF_B_BESPEAK_INFO.PSPT_NO"));


        assertEquals(Sets.newHashSet(), visitor.getSecureBindIndices());
        assertEquals(Sets.newHashSet(3, 5), visitor.getSecureResultIndices());
        assertEquals(aliasSql, visitor.getSql());
    }

    String aliasSql2 = "SELECT TO_CHAR(T.PRO_ID) \"proID\",\n" +
            "       T.CUST_NAME \"custName\",\n" +
            "       T.PSPT_NO \"pstCode\",\n" +
            "       T.GOODS_ID \"goodsID\",\n" +
            "       T.PSPT_ADDR \"pstAddress\",\n" +
            "       T.LINK_ADDR \"linkAddress\",\n" +
            "       T.LINK_PHONE \"linkPhone\",\n" +
            "       T.ACTIVITY_ID \"activity\",\n" +
            "       TO_CHAR(T.NUM_FRONT_TIME, 'YYYY-MM-DD HH24:MI') \"frontTime\",\n" +
            "       G.GOODS_NAME \"goodsName\",\n" +
            "       T.PRE_NUM \"number\",\n" +
            "       T.PRODUCT_ID,\n" +
            "       T.INNOUT_ID \"innoutID\",\n" +
            "       T.STATE \"orderState\",\n" +
            "       T.CITY_NAME \"cityName\",\n" +
            "       T.PRIVILEGE_PACK \"privilegePackCode\",\n" +
            "       TO_CHAR(T.NUM_EXPIRE_TIME, 'YYYY-MM-DD') \"expireTime\"\n" +
            "  FROM TF_B_BESPEAK_INFO T, TF_G_GOODS G\n" +
            " WHERE T.SYSCODE = 'EMAL'\n" +
            "   AND T.GOODS_ID = G.GOODS_ID\n" +
            "   AND G.PARTITION_ID = MOD(G.GOODS_ID, 100)\n" +
            "   AND T.STATE IN ('0', '1', '3')\n" +
            "   AND T.PRO_ID = ?\n" +
            "   AND ROWNUM < 2";

    @Test
    public void testAlias2() throws Exception {
        OracleSensitiveFieldsParser visitor = OracleSensitiveFieldsParser
                .parseOracleSql(aliasSql2, Sets.newHashSet("TF_B_BESPEAK_INFO.LINK_ADDR", "TF_B_BESPEAK_INFO.PSPT_NO"));


        assertEquals(Sets.newHashSet(), visitor.getSecureBindIndices());
        assertEquals(Sets.newHashSet(3, 6), visitor.getSecureResultIndices());
        assertEquals(Sets.newHashSet("PSTCODE", "LINKADDRESS"), visitor.getSecureResultLabels());
        assertEquals(aliasSql2, visitor.getSql());
    }

    String partitionSql = "/*** N/A for encryption ***/\r\nDELETE TF_G_ARTICLE_AMOUNT_MIN PARTITION(P1610) ";

    @Test
    public void testPartition() {
        OracleSensitiveFieldsParser visitor = OracleSensitiveFieldsParser
                .parseOracleSql(partitionSql, Sets.newHashSet("TF_B_BESPEAK_INFO.LINK_ADDR", "TF_B_BESPEAK_INFO.PSPT_NO"));
        assertNull(visitor);
    }

}
