package OOP.Solution;

import OOP.Provided.OOPResult;

public class OOPResultImp implements OOPResult{
    private OOPTestResult test_result;
    private String message;

    public OOPResultImp(OOPTestResult test_result, String message) {
        this.test_result = test_result;
        this.message = message;
    }

    @Override
    public OOPTestResult getResultType() {
        return this.test_result;
    }

    @Override
    public String getMessage() {
        return this.message;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof OOPResultImp))
            return false;
        return ((this.message.equals(((OOPResultImp)obj).getMessage())) && (this.test_result == ((OOPResultImp)obj).getResultType()));
    }
}
