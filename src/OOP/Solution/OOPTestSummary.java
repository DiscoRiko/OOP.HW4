package OOP.Solution;

import OOP.Provided.OOPResult;

import java.util.*;

public class OOPTestSummary {
    Map<String, OOPResult> testMap;

    OOPTestSummary(Map<String, OOPResult> testMap) {
        this.testMap = testMap;
    }

    private int getNumResults(OOPResult.OOPTestResult result) {
        int result_num = 0;

        for (Map.Entry<String, OOPResult> entry : this.testMap.entrySet()) {
            if (entry.getValue().getResultType() == result)
                result_num++;
        }
        return result_num;
    }

    public int getNumSuccesses() {
        return this.getNumResults(OOPResult.OOPTestResult.SUCCESS);
    }

    public int getNumFailures() {
        return this.getNumResults(OOPResult.OOPTestResult.FAILURE);
    }

    public int getNumExceptionMismatches() {
        return this.getNumResults(OOPResult.OOPTestResult.EXPECTED_EXCEPTION_MISMATCH);
    }

    public int getNumErrors() {
        return this.getNumResults(OOPResult.OOPTestResult.ERROR);
    }
}
