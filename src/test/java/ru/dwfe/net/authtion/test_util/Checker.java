package ru.dwfe.net.authtion.test_util;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class Checker
{
    public String resultFieldName;
    public Boolean expectedResult;
    public Map<String, Object> req;
    public int expectedStatus;
    public String expectedErrorContainer;
    public String expectedErrorFieldName;
    public String expectedError;
    public Map<String, Object> expectedResponseMap;

    public void responseHandler(Map<String, Object> map)
    {
        Object details = UtilTest.getValueFromResponse(map, "details");
        assertEquals(true, details.equals(expectedResponseMap));
    }

    public static Checker of(String resultFieldName,
                             Boolean expectedResult,
                             Map<String, Object> req,
                             int expectedStatus,
                             String expectedErrorContainer,
                             String expectedErrorFieldName,
                             String expectedError)
    {
        Checker checker = new Checker();
        checker.resultFieldName = resultFieldName;
        checker.expectedResult = expectedResult;
        checker.req = req;
        checker.expectedStatus = expectedStatus;
        checker.expectedErrorContainer = expectedErrorContainer;
        checker.expectedErrorFieldName = expectedErrorFieldName;
        checker.expectedError = expectedError;

        return checker;
    }

    public static Checker of(String resultFieldName,
                             Boolean expectedResult,
                             Map<String, Object> req,
                             int expectedStatus)
    {
        Checker checker = new Checker();
        checker.resultFieldName = resultFieldName;
        checker.expectedResult = expectedResult;
        checker.req = req;
        checker.expectedStatus = expectedStatus;

        return checker;
    }

    public static Checker of(String resultFieldName,
                             Boolean expectedResult,
                             Map<String, Object> req,
                             int expectedStatus,
                             Map<String, Object> expectedResponseMap)
    {
        Checker checker = new Checker();
        checker.resultFieldName = resultFieldName;
        checker.expectedResult = expectedResult;
        checker.req = req;
        checker.expectedStatus = expectedStatus;
        checker.expectedResponseMap = expectedResponseMap;

        return checker;
    }

}
