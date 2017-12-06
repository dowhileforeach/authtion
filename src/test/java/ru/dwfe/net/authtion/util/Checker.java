package ru.dwfe.net.authtion.util;

public class Checker
{
    public String sendValue;
    public boolean expectedResult;
    public String expectedError;

    public static Checker of(boolean expectedResult, String sendValue, String expectedError)
    {
        return new Checker(expectedResult, sendValue, expectedError);
    }

    private Checker(boolean expectedResult, String sendValue, String expectedError)
    {
        this.sendValue = sendValue;
        this.expectedResult = expectedResult;
        this.expectedError = expectedError;
    }
}
