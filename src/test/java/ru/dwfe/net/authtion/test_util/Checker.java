package ru.dwfe.net.authtion.test_util;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class Checker
{
  public Boolean expectedResult;
  public Map<String, Object> req;
  public int expectedStatus;
  public String expectedError;
  public Map<String, Object> expectedResponseMap;

  public void responseHandler(Map<String, Object> map)
  {
    Map<String, Object> details = (Map<String, Object>) UtilTest.getValueFromResponse(map, "data");
    if (details.containsKey("createdOn"))
      details.put("createdOn", "date");
    if (details.containsKey("updatedOn"))
      details.put("updatedOn", "date");

    assertEquals(details, expectedResponseMap);
  }

  public static Checker of(Boolean expectedResult,
                           Map<String, Object> req,
                           int expectedStatus,
                           String expectedError)
  {
    Checker checker = new Checker();
    checker.expectedResult = expectedResult;
    checker.req = req;
    checker.expectedStatus = expectedStatus;
    checker.expectedError = expectedError;

    return checker;
  }

  public static Checker of(Boolean expectedResult,
                           Map<String, Object> req,
                           int expectedStatus)
  {
    Checker checker = new Checker();
    checker.expectedResult = expectedResult;
    checker.req = req;
    checker.expectedStatus = expectedStatus;

    return checker;
  }

  public static Checker of(Boolean expectedResult,
                           Map<String, Object> req,
                           int expectedStatus,
                           Map<String, Object> expectedResponseMap)
  {
    Checker checker = new Checker();
    checker.expectedResult = expectedResult;
    checker.req = req;
    checker.expectedStatus = expectedStatus;
    checker.expectedResponseMap = expectedResponseMap;

    return checker;
  }

}
