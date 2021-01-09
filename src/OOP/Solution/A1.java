package OOP.Solution;

@OOPTestClass(value= OOPTestClass.OOPTestClassType.ORDERED)
public class A1 {
    @OOPBefore(value={"getIt2", "getThat1"})
    void getIt1() {
        System.out.println("This is getIt1 in A1");
    }
    @OOPTest(order=4, tag = "KKK")
    void getThat1() {
        System.out.println("This is getThat1 in A1");
    }
}
