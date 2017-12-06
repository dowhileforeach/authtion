package ru.dwfe.net.authtion.util;

public class Checker
{
    public String sendValue;
    public boolean expectedResult;
    public String fieldName;
    public String expectedError;

    public static Checker of(boolean expectedResult, String sendValue, String fieldName, String expectedError)
    {
        return new Checker(expectedResult, sendValue, fieldName, expectedError);
    }

    private Checker(boolean expectedResult, String sendValue, String fieldName, String expectedError)
    {
        this.sendValue = sendValue;
        this.expectedResult = expectedResult;
        this.fieldName = fieldName;
        this.expectedError = expectedError;
    }
}
