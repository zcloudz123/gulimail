package com.gulimall.common.valid;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.lang.annotation.Annotation;
import java.util.HashSet;
import java.util.Set;

/**
 * @decription:
 * @author: zyy
 * @date 2020-06-10-21:20
 */
public class BooleanNumConstraintValidator implements ConstraintValidator<BooleanNum,Integer> {

    private Set<Integer> set = new HashSet<>();

    @Override
    public void initialize(BooleanNum constraintAnnotation) {
        int[] vals = constraintAnnotation.vals();
        for (int val :
                vals) {
            set.add(val);
        }
    }

    //判断是否校验成功
    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return set.contains(value);
    }
}
