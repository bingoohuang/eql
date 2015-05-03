package org.n3r.eql.eqler.generators;

import com.google.common.collect.Lists;
import org.n3r.eql.EqlPage;
import org.n3r.eql.EqlTran;
import org.n3r.eql.config.EqlConfig;
import org.n3r.eql.eqler.annotations.Dynamic;
import org.n3r.eql.eqler.annotations.Param;
import org.n3r.eql.impl.EqlBatch;

import java.lang.annotation.Annotation;
import java.util.List;

import static org.apache.commons.lang3.StringUtils.isBlank;

public class MethodAllParam {
    private List<MethodParam> methodParams = Lists.newArrayList();
    private int seqParamsCount;
    private int seqDynamicsCount;
    private int namedParamsCount;
    private int namedDynamicCount;
    private MethodParam eqlTran;
    private MethodParam eqlBatch;
    private MethodParam eqlPage;
    private MethodParam eqlConfig;
    private int methodParamsCount;

    private int asmLocalVarNamedParamIndex = -1;
    private int asmLocalVarNamedDynamicIndex = -1;

    public void compute() {
        for (MethodParam methodParam : methodParams) {
            computeMethodParam(methodParam);
        }

        methodParamsCount = methodParams.size();

        if (seqParamsCount > 0 && namedParamsCount > 0)
            throw new RuntimeException("@NamedParam should be used to annotate parameters all or none");

        if (seqDynamicsCount > 0 && namedDynamicCount > 0)
            throw new RuntimeException("@DynamicParam(name=\"sth.\" should be used to annotate dynamics all or none");

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

    private void computeMethodParam(MethodParam methodParam) {
        MethodParam eqlTranNew = parseNonAnnotationsMethodType(methodParam, EqlTran.class, eqlTran);
        if (eqlTranNew != null) { eqlTran = eqlTranNew; return; }

        MethodParam eqlBatchNew = parseNonAnnotationsMethodType(methodParam, EqlBatch.class, eqlBatch);
        if (eqlBatchNew != null) { eqlBatch = eqlBatchNew; return; }

        MethodParam eqlPageNew = parseNonAnnotationsMethodType(methodParam, EqlPage.class, eqlPage);
        if (eqlPageNew != null) { eqlPage = eqlPageNew; return; }

        MethodParam eqlConfigNew = parseNonAnnotationsMethodType(methodParam, EqlConfig.class, eqlConfig);
        if (eqlConfigNew != null) { eqlConfig = eqlConfigNew; return; }

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

    private MethodParam parseNonAnnotationsMethodType(MethodParam methodParam, Class<?> type, MethodParam lastMethodParam) {
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

    public List<MethodParam> getMethodParams() {
        return methodParams;
    }

    public void addMethodParam(MethodParam methodParam) {
        this.methodParams.add(methodParam);
    }

    public int getSeqParamsCount() {
        return seqParamsCount;
    }

    public void setSeqParamsCount(int seqParamsCount) {
        this.seqParamsCount = seqParamsCount;
    }

    public int getSeqDynamicsCount() {
        return seqDynamicsCount;
    }

    public void setSeqDynamicsCount(int seqDynamicsCount) {
        this.seqDynamicsCount = seqDynamicsCount;
    }

    public MethodParam getMethodParam(int index) {
        return methodParams.get(index);
    }
}
