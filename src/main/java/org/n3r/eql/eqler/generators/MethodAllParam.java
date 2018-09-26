package org.n3r.eql.eqler.generators;

import com.google.common.collect.Lists;
import lombok.Getter;
import lombok.Setter;
import lombok.val;
import org.n3r.eql.EqlPage;
import org.n3r.eql.EqlTran;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.eqler.annotations.SqlId;
import org.n3r.eql.impl.EqlBatch;
import org.n3r.eql.map.EqlRowMapper;
import org.n3r.eql.pojo.annotations.EqlId;
import org.objectweb.asm.Type;

import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class MethodAllParam {
    private List<MethodParam> methodParams = Lists.newArrayList();
    @Getter private int seqParamsCount;
    @Getter private int seqDynamicsCount;
    @Getter private int namedParamsCount;
    @Getter private int namedDynamicCount;
    @Getter private MethodParam paramEqlId;
    @Getter private MethodParam paramReturnType;
    @Getter private MethodParam eqlTran;
    @Getter private MethodParam eqlBatch;
    @Getter private MethodParam eqlPage;
    @Getter private MethodParam eqlConfig;
    @Getter private MethodParam eqlRowMapper;
    @Getter private int methodParamsCount = 0;

    @Getter private int asmLocalVarNamedParamIndex = -1;
    @Getter int asmLocalVarNamedDynamicIndex = -1;
    @Setter private EqlId methodEqlId;

    public void compute() {
        int offset = 0;
        for (val methodParam : methodParams) {
            computeMethodParam(methodParam);
            methodParam.setOffset(offset);
            if (isWildType(methodParam)) ++offset;
        }

        if (seqParamsCount > 0 && namedParamsCount > 0)
            throw new RuntimeException("@Param should be used to annotate parameters all or none");

        if (seqDynamicsCount > 0 && namedDynamicCount > 0)
            throw new RuntimeException("@Dynamic(name=\"sth.\" should be used to annotate dynamics all or none");

        if (namedParamsCount > 0) {
            asmLocalVarNamedParamIndex = 1 /* this */ + methodParamsCount + 1 /* named param hashmap */;
        }
        if (namedDynamicCount > 0) {
            asmLocalVarNamedDynamicIndex = 1 /* this */ + methodParamsCount + (namedParamsCount > 0 ? 2 : 1) /* named dynamic hashmap */;

        }
    }

    private boolean isWildType(MethodParam methodParam) {
        Type tp = Type.getType(methodParam.getParamType());
        return tp.equals(Type.LONG_TYPE) || tp.equals(Type.DOUBLE_TYPE);
    }

    private void computeMethodParam(MethodParam methodParam) {
        val eqlTranNew = parseNonAnnotationsMethodParam(methodParam, EqlTran.class, eqlTran);
        if (eqlTranNew != null) {
            eqlTran = eqlTranNew;
            return;
        }

        val eqlBatchNew = parseNonAnnotationsMethodParam(methodParam, EqlBatch.class, eqlBatch);
        if (eqlBatchNew != null) {
            eqlBatch = eqlBatchNew;
            return;
        }

        val eqlPageNew = parseNonAnnotationsMethodParam(methodParam, EqlPage.class, eqlPage);
        if (eqlPageNew != null) {
            eqlPage = eqlPageNew;
            return;
        }

        val eqlConfigNew = parseNonAnnotationsMethodParam(methodParam, EqlConfig.class, eqlConfig);
        if (eqlConfigNew != null) {
            eqlConfig = eqlConfigNew;
            return;
        }

        val eqlRowMapperNew = parseNonAnnotationsMethodParam(methodParam, EqlRowMapper.class, eqlRowMapper);
        if (eqlRowMapperNew != null) {
            eqlRowMapper = eqlRowMapperNew;
            return;
        }

        if (isSqlIdAnnotated(methodParam)) return;
        if (isReturnTypeAnnotated(methodParam)) return;

        ++methodParamsCount;

        val param = methodParam.getParam();
        if (param != null) methodParam.setSeqParamIndex(namedParamsCount++);

        val dynamic = methodParam.getDynamic();
        if (dynamic == null) {
            if (param == null) methodParam.setSeqParamIndex(seqParamsCount++);
        } else {
            if (dynamic.sole() && param != null)
                throw new RuntimeException(
                        "@DynamicParam(sole=true) and @NamedParam can not co-exists");

            if (isBlank(dynamic.name()))
                methodParam.setSeqDynamicIndex(seqDynamicsCount++);
            else methodParam.setSeqDynamicIndex(namedDynamicCount++);

            if (!dynamic.sole() && param == null) {
                methodParam.setSeqParamIndex(seqParamsCount++);
            }
        }
    }

    private boolean isReturnTypeAnnotated(MethodParam methodParam) {
        val returnType = methodParam.getReturnType();
        if (returnType == null) return false;


        if (paramReturnType != null)
            throw new RuntimeException("more than one @ReturnType defined");
        if (methodParam.getParamType() != Class.class)
            throw new RuntimeException("bad @ReturnType parameter type, required Class");

        paramReturnType = methodParam;
        return true;
    }

    private boolean isSqlIdAnnotated(MethodParam methodParam) {
        SqlId paramEqlIdThis = methodParam.getSqlId();
        if (paramEqlIdThis == null) return false;

        if (methodEqlId != null || paramEqlId != null)
            throw new RuntimeException("more than one @EqlId defined");
        if (methodParam.getParamType() != String.class)
            throw new RuntimeException("bad @EqlId parameter type, required String");

        paramEqlId = methodParam;
        return true;
    }

    private MethodParam parseNonAnnotationsMethodParam(
            MethodParam methodParam, Class<?> type, MethodParam lastMethodParam) {
        if (type.isAssignableFrom(methodParam.getParamType())) {
            checkNull(lastMethodParam, "only one " + type + " parameter supported");
            checkNonAnnotations(methodParam);
            return methodParam;
        }

        return null;
    }

    private void checkNonAnnotations(MethodParam methodParam) {
        val paramAnnotations = methodParam.getParamAnnotations();
        if (paramAnnotations.length == 0) return;

        throw new RuntimeException("Annotations are not supported for type "
                + methodParam.getParamType());
    }

    private void checkNull(Object object, String msg) {
        if (object == null) return;

        throw new RuntimeException(msg);
    }


    public void addMethodParam(MethodParam methodParam) {
        this.methodParams.add(methodParam);
    }

    public MethodParam getMethodParam(int index) {
        return methodParams.get(index);
    }

    public int getParamsSize() {
        return methodParams.size();
    }
}
