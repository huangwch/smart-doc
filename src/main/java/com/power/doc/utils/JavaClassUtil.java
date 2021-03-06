/*
 * smart-doc
 *
 * Copyright (C) 2019-2020 smart-doc
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package com.power.doc.utils;

import com.power.common.util.StringUtil;
import com.thoughtworks.qdox.model.JavaClass;
import com.thoughtworks.qdox.model.JavaField;
import com.thoughtworks.qdox.model.JavaMethod;
import com.thoughtworks.qdox.model.JavaType;
import com.thoughtworks.qdox.model.impl.DefaultJavaField;
import com.thoughtworks.qdox.model.impl.DefaultJavaParameterizedType;

import java.util.ArrayList;
import java.util.List;

/**
 * Handle JavaClass
 *
 * @author yu 2019/12/21.
 */
public class JavaClassUtil {

    /**
     * Get fields
     *
     * @param cls1 The JavaClass object
     * @param i    Recursive counter
     * @return list of JavaField
     */
    public static List<JavaField> getFields(JavaClass cls1, int i) {
        List<JavaField> fieldList = new ArrayList<>();
        if (null == cls1) {
            return fieldList;
        } else if ("Object".equals(cls1.getSimpleName()) || "Timestamp".equals(cls1.getSimpleName()) ||
                "Date".equals(cls1.getSimpleName()) || "Locale".equals(cls1.getSimpleName())) {
            return fieldList;
        } else {
            String className = cls1.getFullyQualifiedName();
            if (cls1.isInterface() &&
                    !JavaClassValidateUtil.isCollection(className) &&
                    !JavaClassValidateUtil.isMap(className)) {
                List<JavaMethod> methods = cls1.getMethods();
                for (JavaMethod javaMethod : methods) {
                    String methodName = javaMethod.getName();
                    if (!methodName.startsWith("get")) {
                        continue;
                    }
                    methodName = StringUtil.firstToLowerCase(methodName.substring(3, methodName.length()));
                    JavaField javaField = new DefaultJavaField(javaMethod.getReturns(), methodName);
                    fieldList.add(javaField);
                }
            }
            JavaClass pcls = cls1.getSuperJavaClass();
            fieldList.addAll(getFields(pcls, i));
            fieldList.addAll(cls1.getFields());
        }
        return fieldList;
    }


    /**
     * get enum value
     *
     * @param javaClass    enum class
     * @param formDataEnum is return method
     * @return Object
     */
    public static Object getEnumValue(JavaClass javaClass, boolean formDataEnum) {
        List<JavaField> javaFields = javaClass.getEnumConstants();
        List<JavaMethod> methodList = javaClass.getMethods();
        int annotation = 0;
        for (JavaMethod method : methodList) {
            if (method.getAnnotations().size() > 0) {
                annotation = 1;
                break;
            }
        }
        Object value = null;
        int index = 0;
        for (JavaField javaField : javaFields) {
            String simpleName = javaField.getType().getSimpleName();
            StringBuilder valueBuilder = new StringBuilder();
            valueBuilder.append("\"").append(javaField.getName()).append("\"").toString();
            if (formDataEnum) {
                value = valueBuilder.toString();
                return value;
            }
            if (!JavaClassValidateUtil.isPrimitive(simpleName) && index < 1) {
                if (null != javaField.getEnumConstantArguments() && annotation > 0) {
                    value = javaField.getEnumConstantArguments().get(0);
                } else {
                    value = valueBuilder.toString();
                }
            }
            index++;
        }
        return value;
    }


    /**
     * Get annotation simpleName
     *
     * @param annotationName annotationName
     * @return String
     */
    public static String getAnnotationSimpleName(String annotationName) {
        return getClassSimpleName(annotationName);
    }

    /**
     * Get className
     *
     * @param className className
     * @return String
     */
    public static String getClassSimpleName(String className) {
        if (className.contains(".")) {
            int index = className.lastIndexOf(".");
            className = className.substring(index + 1, className.length());
        }
        return className;
    }

    /**
     * get Actual type
     *
     * @param javaClass JavaClass
     * @return JavaClass
     */
    public static JavaClass getActualType(JavaClass javaClass) {
        return getActualTypes(javaClass).get(0);
    }

    /**
     * get Actual type list
     *
     * @param javaClass JavaClass
     * @return JavaClass
     */
    public static List<JavaClass> getActualTypes(JavaClass javaClass) {
        if (null == javaClass) {
            return new ArrayList<>(0);
        }
        List<JavaClass> javaClassList = new ArrayList<>();
        List<JavaType> actualTypes = ((DefaultJavaParameterizedType) javaClass).getActualTypeArguments();
        actualTypes.forEach(javaType -> {
            JavaClass actualClass = (JavaClass) javaType;
            javaClassList.add(actualClass);
        });
        return javaClassList;
    }
}
