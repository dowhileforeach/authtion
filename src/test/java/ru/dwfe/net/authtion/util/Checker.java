package ru.dwfe.net.authtion.util;

import java.util.Map;

public class Checker
{
    public boolean expectedResult;
    public Map<String, Object> req;
    public String expectedErrorFieldName;
    public String expectedError;

    public static Checker of(boolean expectedResult, Map<String, Object> req, String expectedErrorFieldName, String expectedError)
    {
        return new Checker(expectedResult, req, expectedErrorFieldName, expectedError);
    }

    private Checker(boolean expectedResult, Map<String, Object> req, String expectedErrorFieldName, String expectedError)
    {
        this.expectedResult = expectedResult;
        this.req = req;
        this.expectedErrorFieldName = expectedErrorFieldName;
        this.expectedError = expectedError;
    }
}
