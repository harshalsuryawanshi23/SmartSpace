package com.smartspace.kyc;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.fail;

public class KycSecurityTest {

    @Test
    public void testNoAadhaarOrBiometricColumnsInEntities() throws ClassNotFoundException {
        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AnnotationTypeFilter(Entity.class));

        for (BeanDefinition beanDef : provider.findCandidateComponents("com.smartspace")) {
            Class<?> entityClass = Class.forName(beanDef.getBeanClassName());
            
            for (Field field : entityClass.getDeclaredFields()) {
                String fieldName = field.getName().toLowerCase();
                
                if (fieldName.contains("aadhaar") || fieldName.contains("biometric") || fieldName.contains("idnumber")) {
                    fail("Found restricted field name '" + field.getName() + "' in entity " + entityClass.getName());
                }

                if (field.isAnnotationPresent(Column.class)) {
                    Column column = field.getAnnotation(Column.class);
                    String colName = column.name().toLowerCase();
                    if (colName.contains("aadhaar") || colName.contains("biometric") || colName.contains("id_number")) {
                        fail("Found restricted column name '" + column.name() + "' in entity " + entityClass.getName());
                    }
                }
            }
        }
    }
}
