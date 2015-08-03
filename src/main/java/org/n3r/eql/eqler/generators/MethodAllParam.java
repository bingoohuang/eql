package org.n3r.eql.eqler.generators;

import com.google.common.collect.Lists;
import org.n3r.eql.EqlPage;
import org.n3r.eql.EqlTran;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.eqler.annotations.Dynamic;
import org.n3r.eql.eqler.annotations.Param;
import org.n3r.eql.eqler.annotations.ReturnType;
import org.n3r.eql.eqler.annotations.SqlId;
import org.n3r.eql.impl.EqlBatch;
import org.n3r.eql.map.EqlRowMapper;
import org.n3r.eql.pojo.annotations.EqlId;
import org.objectweb.asm.Type;

import java.lang.annotation.Annotation;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class MethodAllParam {
    private List<MethodParam> methodParams = Lists.newArrayList();
    private int seqParamsCount;
    private int seqDynamicsCount;
    private int namedParamsCount;
    private int namedDynamicCount;
    private MethodParam paramEqlId;
    private MethodParam paramReturnType;
    private MethodParam eqlTran;
    private MethodParam eqlBatch;
    private MethodParam eqlPage;
    private MethodParam eqlConfig;
    private MethodParam eqlRowMapper;
    private int methodParamsCount = 0;

    private int asmLocalVarNamedParamIndex = -1;
    private int asmLocalVarNamedDynamicIndex = -1;
    private EqlId methodEqlId;

    public void compute() {
        int offset = 0;
        for (MethodParam methodParam : methodParams) {
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

    public int getAsmLocalVarNamedParamIndex() {
        return asmLocalVarNamedParamIndex;
    }

    public int getAsmLocalVarNamedDynamicIndex() {
        return asmLocalVarNamedDynamicIndex;
    }

    public int getMethodParamsCount() {
        return methodParamsCount;
    }

    public int getNamedParamsCount() {
        return namedParamsCount;
    }

    public int getNamedDynamicCount() {
        return namedDynamicCount;
    }

    public MethodParam getEqlTran() {
        return eqlTran;
    }

    public MethodParam getEqlBatch() {
        return eqlBatch;
    }

    public MethodParam getEqlPage() {
        return eqlPage;
    }

    public MethodParam getEqlConfig() {
        return eqlConfig;
    }

    public MethodParam getEqlRowMapper() {
        return eqlRowMapper;
    }

    private boolean isWildType(MethodParam methodParam) {
        Type tp = Type.getType(methodParam.getParamType());
        return tp.equals(Type.LONG_TYPE) || tp.equals(Type.DOUBLE_TYPE);
    }

    private void computeMethodParam(MethodParam methodParam) {
        MethodParam eqlTranNew = parseNonAnnotationsMethodParam(methodParam, EqlTran.class, eqlTran);
        if (eqlTranNew != null) { eqlTran = eqlTranNew; return; }

        MethodParam eqlBatchNew = parseNonAnnotationsMethodParam(methodParam, EqlBatch.class, eqlBatch);
        if (eqlBatchNew != null) { eqlBatch = eqlBatchNew; return; }

        MethodParam eqlPageNew = parseNonAnnotationsMethodParam(methodParam, EqlPage.class, eqlPage);
        if (eqlPageNew != null) { eqlPage = eqlPageNew; return; }

        MethodParam eqlConfigNew = parseNonAnnotationsMethodParam(methodParam, EqlConfig.class, eqlConfig);
        if (eqlConfigNew != null) { eqlConfig = eqlConfigNew; return; }

        MethodParam eqlRowMapperNew = parseNonAnnotationsMethodParam(methodParam, EqlRowMapper.class, eqlRowMapper);
        if (eqlRowMapperNew != null) { eqlRowMapper = eqlRowMapperNew; return; }

        if (isSqlIdAnnotated(methodParam)) return;
        if (isReturnTypeAnnotated(methodParam)) return;

        ++methodParamsCount;

        Param param = methodParam.getParam();
        if (param != null) methodParam.setSeqParamIndex(namedParamsCount++);

        Dynamic dynamic = methodParam.getDynamic();
        if (dynamic == null) {
            if (param == null) methodParam.setSeqParamIndex(seqParamsCount++);
        } else {
            if (dynamic.sole() && param != null)
                throw new RuntimeException("@DynamicParam(sole=true) and @NamedParam can not co-exists");

            if (isBlank(dynamic.name())) methodParam.setSeqDynamicIndex(seqDynamicsCount++);
            else methodParam.setSeqDynamicIndex(namedDynamicCount++);

            if (!dynamic.sole() && param == null) {
                methodParam.setSeqParamIndex(seqParamsCount++);
            }
        }
    }

    private boolean isReturnTypeAnnotated(MethodParam methodParam) {
        ReturnType returnType = methodParam.getReturnType();
        if (returnType == null) return false;


        if (paramReturnType != null) throw new RuntimeException("more than one @ReturnType defined");
        if (methodParam.getParamType() != Class.class) throw new RuntimeException("bad @ReturnType parameter type, required Class");

        paramReturnType = methodParam;
        return true;
    }

    private boolean isSqlIdAnnotated(MethodParam methodParam) {
        SqlId paramEqlIdThis = methodParam.getSqlId();
        if (paramEqlIdThis == null) return false;

        if (methodEqlId != null || paramEqlId != null) throw new RuntimeException("more than one @EqlId defined");
        if (methodParam.getParamType() != String.class) throw new RuntimeException("bad @EqlId parameter type, required String");

        paramEqlId = methodParam;
        return true;
    }

    private MethodParam parseNonAnnotationsMethodParam(MethodParam methodParam, Class<?> type, MethodParam lastMethodParam) {
        if (type.isAssignableFrom(methodParam.getParamType())) {
            checkNull(lastMethodParam, "only one " + type +  " parameter supported");
            checkNonAnnotations(methodParam);
            return methodParam;
        }

        return null;
    }

    private void checkNonAnnotations(MethodParam methodParam) {
        Annotation[] paramAnnotations = methodParam.getParamAnnotations();
        if (paramAnnotations.length == 0) return;

        throw new RuntimeException("Annotations are not supported for type " + methodParam.getParamType());
    }

    private void checkNull(Object object, String msg) {
        if (object == null) return;

        throw new RuntimeException(msg);
    }


    public void addMethodParam(MethodParam methodParam) {
        this.methodParams.add(methodParam);
    }

    public int getSeqParamsCount() {
        return seqParamsCount;
    }


    public int getSeqDynamicsCount() {
        return seqDynamicsCount;
    }

    public MethodParam getMethodParam(int index) {
        return methodParams.get(index);
    }

    public void setMethodEqlId(EqlId methodEqlId) {
        this.methodEqlId = methodEqlId;
    }

    public MethodParam getParamEqlId() {
        return paramEqlId;
    }

    public int getParamsSize() {
        return methodParams.size();
    }

    public MethodParam getParamReturnType() {
        return paramReturnType;
    }
}
