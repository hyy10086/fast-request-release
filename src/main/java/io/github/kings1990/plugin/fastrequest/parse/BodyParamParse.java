package io.github.kings1990.plugin.fastrequest.parse;

import cn.hutool.core.util.RandomUtil;
import com.google.common.collect.Lists;
import io.github.kings1990.plugin.fastrequest.model.DataMapping;
import io.github.kings1990.plugin.fastrequest.model.FastRequestConfiguration;
import io.github.kings1990.plugin.fastrequest.model.ParamKeyValue;
import io.github.kings1990.plugin.fastrequest.model.ParamNameType;
import io.github.kings1990.plugin.fastrequest.util.KV;
import io.github.kings1990.plugin.fastrequest.util.TypeUtil;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

public class BodyParamParse extends AbstractParamParse {
    @Override
    public LinkedHashMap<String, Object> parseParam(FastRequestConfiguration config, List<ParamNameType> paramNameTypeList) {
        List<DataMapping> customDataMappingList = config.getCustomDataMappingList();
        List<DataMapping> defaultDataMappingList = config.getDefaultDataMappingList();

        List<ParamNameType> requestParamList = paramNameTypeList.stream().filter(q -> q.getParseType() == 3).collect(Collectors.toList());
        LinkedHashMap<String, Object> nameValueMap = new LinkedHashMap<>();
        int randomStringLength = config.getRandomStringLength();
        for (ParamNameType paramNameType : requestParamList) {
            String type = paramNameType.getType();
            boolean arrayFlag = type.contains("[]");
            if(arrayFlag){
                type = type.substring(type.indexOf("[")+1,type.indexOf("]"));
            }
            boolean listFlag = type.contains("List<");
            if(listFlag){
                type = type.substring(type.indexOf("<")+1,type.indexOf(">"));
            }
            String name = paramNameType.getName();
            if ("java.lang.String".equals(type)) {
                if(arrayFlag || listFlag){
                    ParamKeyValue paramKeyValue = new ParamKeyValue("", RandomUtil.randomString(randomStringLength), 2, TypeUtil.Type.String.name());
                    ParamKeyValue p = new ParamKeyValue("",Lists.newArrayList(paramKeyValue),2,"Array");
                    nameValueMap.put(name,p);
                } else {
                    nameValueMap.put(name, new ParamKeyValue(name, RandomUtil.randomString(randomStringLength), 2, TypeUtil.Type.String.name()));
                }
                continue;
            }

            String finalType = type;
            DataMapping dataMapping = customDataMappingList.stream().filter(q -> finalType.equals(q.getType())).findFirst().orElse(null);
            if (dataMapping != null) {
                Object value = dataMapping.getValue();
                nameValueMap.put(name, value);
                continue;
//                if(Constant.SpringParamTypeConfig.JSON.getCode().equals(config.getParamGroup().getPostType())){
//                    nameValueMap.put(name, value);
//                } else {
//                    StringBuilder parseOut = new StringBuilder();
//                    if (JsonUtil.isJSON2(value)) {
//                        Map<String, Object> parse = JSON.parseObject(value, Map.class);
//                        for (Map.Entry<String, Object> entry : parse.entrySet()) {
//                            String k = entry.getKey();
//                            String v = entry.getValue().toString();
//                            parseOut.append(k).append("=").append(v).append("&");
//                        }
//                    } else {
//                        break;
//                    }
//                    String parseOutStr = parseOut.toString();
//                    if (parseOutStr.endsWith("&")) {
//                        parseOutStr = parseOutStr.substring(0, parseOutStr.length() - 1);
//                    }
//                    nameValueMap.put(name, parseOutStr);
//                    break;
//                }
            }
            //默认的数据映射解析参数
            dataMapping = defaultDataMappingList.stream().filter(q -> finalType.equals(q.getType())).findFirst().orElse(null);
            if (dataMapping != null) {
                Object value = dataMapping.getValue();
                String defaultType = dataMapping.getType();
                String targetType =  ("boolean".equals(defaultType) ||"java.lang.Boolean".equals(defaultType))?TypeUtil.Type.Boolean.name():TypeUtil.Type.Number.name();
                if(arrayFlag || listFlag){
                    ParamKeyValue paramKeyValue = new ParamKeyValue("", value, 2, targetType);
                    ParamKeyValue p = new ParamKeyValue("",Lists.newArrayList(paramKeyValue),2,"Array");
                    nameValueMap.put(name,p);
                } else {
                    nameValueMap.put(name, new ParamKeyValue(name, RandomUtil.randomString(randomStringLength), 2, targetType));
                }
                continue;
            }
            //json解析
            KV kv = KV.getFields(paramNameType.getPsiClass());

            //String json = kv.toPrettyJson();
//            Map parse = JSON.parseObject(json, Map.class);
//            String queryParam = URLUtil.buildQuery(parse, null);
            String key = (String) kv.keySet().stream().findFirst().orElse(null);
            if(key != null){
                Object firstValue = kv.get(key);
                String targetType = TypeUtil.Type.Object.name();
                if(firstValue instanceof ArrayList || arrayFlag || listFlag){
                    targetType = TypeUtil.Type.Array.name();
                }
                nameValueMap.put(name, new ParamKeyValue(name, kv, 2, targetType));
            }
            break;
        }

        return nameValueMap;
    }
}
