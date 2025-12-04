package com.attendease.backend.validation.rules;

public class UserValidationRules {

    public static final String FIRSTNAME_FORMAT_REGEX = "^[A-Za-zÀ-ÖØ-öø-ÿ'\\-\\s]+$";
    public static final String LASTNAME_FORMAT_REGEX = "^[A-Za-zÀ-ÖØ-öø-ÿ'\\-\\s]+$";
    public static final String PASSWORD_FORMAT_REGEX = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[^A-Za-z\\d]).{8,128}$";
    public static final String CONTACT_NUMBER_FORMAT_REGEX = "^[+]?[0-9]{11,15}$";
    public static final String STUDENT_NUMBER_FORMAT_REGEX = "^CT\\d{2}-\\d{4}$";
    public static final String EMAIL_FORMAT_REGEX = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
    public static final String SECTION_NAME_FORMAT_REGEX = "^[A-Z0-9]+-[0-9]{3}$";
    public static final String COURSE_NAME_FORMAT_REGEX = "^[A-Z0-9-]+$";
}

