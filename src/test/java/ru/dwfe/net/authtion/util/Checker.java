package ru.dwfe.net.authtion.util;

import java.util.Map;

public class Checker
{
    public String resultFieldName;
    public Boolean expectedResult;
    public Map<String, Object> req;
    public int expectedStatus;
    public String expectedErrorContainer;
    public String expectedErrorFieldName;
    public String expectedError;

    public static Checker of(String resultFieldName, Boolean expectedResult, Map<String, Object> req, int expectedStatus, String expectedErrorContainer, String expectedErrorFieldName, String expectedError)
    {
        return new Checker(resultFieldName, expectedResult, req, expectedStatus, expectedErrorContainer, expectedErrorFieldName, expectedError);
    }

    private Checker(String resultFieldName, Boolean expectedResult, Map<String, Object> req, int expectedStatus, String expectedErrorContainer, String expectedErrorFieldName, String expectedError)
    {
        this.resultFieldName = resultFieldName;
        this.expectedResult = expectedResult;
        this.req = req;
        this.expectedStatus = expectedStatus;
        this.expectedErrorContainer = expectedErrorContainer;
        this.expectedErrorFieldName = expectedErrorFieldName;
        this.expectedError = expectedError;
    }
}
