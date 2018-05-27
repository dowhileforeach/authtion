package ru.dwfe.net.authtion.test;

import java.util.Map;

import static org.junit.Assert.assertEquals;

public class AuthtionTestChecker
{
  public Boolean expectedResult;
  public Map<String, Object> req;
  public int expectedStatus;
  public String expectedError;
  public Map<String, Object> expectedResponseMap;

  public void responseHandler(Map<String, Object> map)
  {
    var details = (Map<String, Object>) AuthtionTestUtil.getValueFromResponse(map, "data");
    if (details.containsKey("createdOn"))
      details.put("createdOn", "date");
    if (details.containsKey("updatedOn"))
      details.put("updatedOn", "date");

    assertEquals(details, expectedResponseMap);
  }

  public static AuthtionTestChecker of(Boolean expectedResult,
                                       Map<String, Object> req,
                                       int expectedStatus,
                                       String expectedError)
  {
    var checker = new AuthtionTestChecker();
    checker.expectedResult = expectedResult;
    checker.req = req;
    checker.expectedStatus = expectedStatus;
    checker.expectedError = expectedError;

    return checker;
  }

  public static AuthtionTestChecker of(Boolean expectedResult,
                                       Map<String, Object> req,
                                       int expectedStatus)
  {
    var checker = new AuthtionTestChecker();
    checker.expectedResult = expectedResult;
    checker.req = req;
    checker.expectedStatus = expectedStatus;

    return checker;
  }

  public static AuthtionTestChecker of(Boolean expectedResult,
                                       Map<String, Object> req,
                                       int expectedStatus,
                                       Map<String, Object> expectedResponseMap)
  {
    var checker = new AuthtionTestChecker();
    checker.expectedResult = expectedResult;
    checker.req = req;
    checker.expectedStatus = expectedStatus;
    checker.expectedResponseMap = expectedResponseMap;

    return checker;
  }

}
