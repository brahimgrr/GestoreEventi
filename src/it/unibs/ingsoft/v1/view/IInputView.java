package it.unibs.ingsoft.v1.view;

import java.util.List;

/**
 * ISP sub-interface: pure input / acquisition operations.
 * Implementations that only need to read user input (e.g., a scripted test driver)
 * depend only on this narrow interface.
 */
public interface IInputView
{
    String acquisisciStringa(String prompt);

    /**
     * @pre min &lt;= max
     */
    int acquisisciIntero(String prompt, int min, int max);

    boolean acquisisciSiNo(String prompt);
    List<String> acquisisciListaNomi(String titolo);
}
