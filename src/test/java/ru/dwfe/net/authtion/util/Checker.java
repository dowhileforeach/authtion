package ru.dwfe.net.authtion.util;

public class Checker
{
    public String sendValue;
    public boolean expectedResult;
    public String expectedErrorField;
    public String expectedError;

    public static Checker of(boolean expectedResult, String sendValue, String expectedErrorField, String expectedError)
    {
        return new Checker(expectedResult, sendValue, expectedErrorField, expectedError);
    }

    private Checker(boolean expectedResult, String sendValue, String expectedErrorField, String expectedError)
    {
        this.sendValue = sendValue;
        this.expectedResult = expectedResult;
        this.expectedErrorField = expectedErrorField;
        this.expectedError = expectedError;
    }
}
